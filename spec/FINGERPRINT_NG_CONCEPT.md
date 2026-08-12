# Fingerprint NG — a scene-anchored concept for video4j

> **Status:** 🔴 Concept — nothing here is built. No code-level plan, deliberately.
> **Scope:** What the current `MultiSectorVideoFingerprinter` actually does, how the field
> solves the same problem, and a recommended next-generation design. Covers the classical
> core and the seam where an optional neural descriptor would plug in.
> **Modules:** [`fingerprint/`](../fingerprint), [`fingerprint-indexer/`](../fingerprint-indexer),
> [`video4j/`](../video4j) (`CVUtils`, `ExtendedVideoCapture`, `PreviewGenerator`)
> **Audience:** whoever implements the successor, and whoever has to defend the choice later.
> **Out of scope:** the metaloom-side producer/index work. That lives in
> `metaloom/spec/tasks/NODE_FINGERPRINT_TASKS.md` and `metaloom/spec/loom/SEARCH_LUCENE.md`;
> this document must reconcile with them, not restate them.

---

## 0. Executive summary

| Question | Short answer |
|---|---|
| Can we fingerprint **scenes** instead of the whole video, and match parts? | Yes, and it is the only structural way to get excerpt matching. Every published scheme that can find a clip inside a video keeps **per-segment descriptors**; every fixed-length whole-video code cannot, no matter how it is built. |
| Would that combine into a **more robust whole-video** hash? | Yes — two complementary rollups: an order-free duration-weighted SimHash over scene codes (trim-robust), and an order-aware "shot rhythm" landmark index (gives O(1) candidate lookup *and* the time offset). |
| Does it cost **drastically more compute**? | No — but the intuition needs one correction. Hashing 10 frames per scene is free (a 64×64 DCT is microseconds). The cost is *finding* the scenes, which needs one linear pass over the timeline instead of three 0.5 s seeks. That pass is decode-bound and runs faster than real time at a reduced decode rate. |
| Are **Matryoshka** embeddings useful? | The *principle* yes, the *model* not yet. MRL is a training-time property and cannot be retrofitted to a DCT hash — but DCT coefficients are already frequency-ordered, so a 64-bit prefix is a free coarse code. Make prefix-truncatability an explicit property; adopt a real MRL descriptor only if the neural tier lands. |
| Are **quaternions** useful? | No, not for this. The cited KG work buys its win from modelling *relation types* as 4D rotations; fingerprints have no relation taxonomy — the only relation is a scalar time shift, which needs one angle, not three. The relevant quaternion literature (QDFT/Q-SVD hashing) is about joint **colour** processing, and there are cheaper ways to get colour back. |
| Can the **angle between components** encode semantics? | Yes, and this is well-established — but complex-valued, not hypercomplex. TMK and Circulant Temporal Encoding encode frame descriptors jointly with timestamps so that **phase encodes time**; matching then returns similarity *and* the relative offset. Worth adopting as an optional rollup, not as the primary mechanism. |
| Would a **domain transform** (Fourier) make the embedding robust to temporal clipping? | Not a global one. `|FFT|` is shift-invariant but clipping is a *support* change, not a shift. The obstacle is aggregation, not domain — see [§8](#8-domain-transforms--what-they-can-and-cannot-buy). Localise the transform (STFT, not FFT) and it works; that *is* the scene design. |

**One-line recommendation.** Keep the classical core. Replace *temporal averaging of raw pixels*
with *per-frame DCT codes aggregated per shot*, index shots individually, and recover the
whole-video code as a rollup rather than as the primitive.

---

## 1. What the current fingerprint actually is

Measured from [`MultiSectorVideoFingerprinterImpl`](../fingerprint/src/main/java/io/metaloom/video4j/fingerprint/v2/impl/MultiSectorVideoFingerprinterImpl.java)
and [`AbstractVideoFingerprinter`](../fingerprint/src/main/java/io/metaloom/video4j/fingerprint/AbstractVideoFingerprinter.java),
not from the 2021 blog post — the blog describes the v1 single-seek variant and is no longer
an accurate account of the shipped code.

### 1.1 Sampling

```java
public static int len         = 15 * 4;                  // 60
public static int sectorCount = 4;
public double  skipFactor     = 1f / sectorCount - 0.1f;  // 0.15
protected int  speedUp        = 8;                        // AbstractVideoFingerprinter

for (int i = 1; i < sectorCount; i++) {   // i = 1, 2, 3
    double factor = i * skipFactor;       // 0.15, 0.30, 0.45
    video.seekToFrameRatio(factor);
    ...
    if (frameNo % speedUp == 0) continue; // KEEPS 7 of 8, does not take 1 of 8
    if (++useableFrameNo >= sectorLen) break;   // sectorLen = 15 → 14 stacked
}
```

Consequences, in order of severity:

1. **The loop starts at `i = 1` and stops at `sectorCount - 1`.** Three sample points, not four,
   at **15 %, 30 % and 45 %** of the frame count. **Nothing past ~45 % of any video ever
   contributes a single bit.** A film and the same film with a different second half hash
   identically.
2. **`speedUp` is a skip, not a stride.** `frameNo % 8 == 0 → continue` discards one frame in
   eight and keeps the other seven. Each "sector" is therefore ~17 *consecutive* decoded frames
   ≈ **0.57 s of contiguous footage at 30 fps**, not a spread sample of that region. The whole
   fingerprint sees roughly **1.7 seconds of video**, from ~51 decoded frames, 42 of which
   are stacked.
3. **Sample points are ratio-anchored, so they are not trim-invariant.** Trimming 10 % off the
   head moves every seek point in absolute time. This is the exact property the design was
   trying to buy and does not have.
4. **The accumulator is shared across sectors.** `stack` is allocated once and all three regions
   sum into it. Sectors are not hashed separately and merged — the name promises a structure
   the code does not build. (Note the collision with `asset_fingerprint_comp.sector_index` on
   the metaloom side, which means a *timeline window*. Two unrelated meanings, one word.)

### 1.2 Per-frame processing

```
frame → resize 512×512 (INTER_LANCZOS4, aspect ratio discarded)
      → grayscale → normalize(0,205)
      → drop if mean < 10 (black frame)
      → box blur 15×15
      → resize 16×16 → normalize(0,255)
      → stack += frame * (1/len * stackFactor)
```

then, once per frame and only the last survives:

```java
double meanV = mean(src);
Imgproc.threshold(src, result, meanV, 255f, Imgproc.THRESH_BINARY);
```

This is **aHash over a temporal average**: a mean-threshold of a 16×16 blurred luma image.
There is no DCT anywhere in video4j (`grep -r 'dct\|DCT\|pHash'` → zero hits), no letterbox or
black-bar detection (`CVUtils.boxFrame` *adds* bars, it does not remove them), and no colour —
`COLOR_RGB2GRAY` discards it at step two.

### 1.3 Encoding and matching

- Wire format: 38 bytes / 76 hex chars — `short version | 0x00 | short vectorSize | 0xFF |
  32 bytes BitSet`. 256 bits, row-major, bit index = `row * 16 + col`.
- `vector()` expands the bits to `float[256]` of literal `0f`/`1f`.
- Search is Lucene HNSW with the **default `EUCLIDEAN`** similarity. On 0/1 vectors,
  squared L2 *is* Hamming distance, so `score = 1 / (1 + hamming)`. The 0.10 threshold used
  downstream is crossed at 9 differing bits — this is a near-duplicate index, not a similarity
  index, and the useful dynamic range is about nine bits out of 256.
- **There is no Hamming function in the codebase.** The only distance ever computed in-repo is
  `FingerprintUtils.levenshteinDistance` over *hex characters*, used exclusively by tests.
  Edit distance on a hex rendering is not a metric on the underlying code — a one-bit flip in a
  high nibble and a 4-bit flip in the same nibble both cost 1.
- The only behavioural test hashes one clip at three resolutions and asserts Levenshtein ≤ 5.
  **There is no negative test** — nothing anywhere asserts that two *different* videos produce
  *different* fingerprints. False-positive rate is therefore unmeasured.

### 1.4 The structural diagnosis

Two properties, not bugs, explain most of the observed weakness:

- **Temporal averaging destroys temporal information.** Summing frames into one accumulator is a
  projection that is invariant to frame order — deliberately, per the blog, to survive frame-rate
  changes. But it also means a fingerprint cannot localise, cannot match an excerpt, and cannot
  distinguish a video from its own shuffle. Robustness was bought by discarding the signal.
- **Whole-video pooling dilutes short content.** A 10 s clip inside a 2 h video contributes
  `10/7200 ≈ 0.14 %` of the accumulated energy. No threshold recovers it. See [§8](#8-domain-transforms--what-they-can-and-cannot-buy)
  for why this is a property of pooling itself and not of the pixel domain.

The enumerated implementation defects (three-of-four sectors, the duplicated `hash1`, the dead
`combineStack`, the `Mat` leak on `finalStep`, `public static` non-final tuning shared with v1)
are catalogued in `metaloom/spec/tasks/NODE_FINGERPRINT_TASKS.md` and are not repeated here.
They are worth fixing regardless of whether this concept is adopted — the design below does not
depend on them being fixed first.

---

## 2. How the field solves this

| Scheme | Descriptor | Clip-in-video | Length | Indexable | Trim-robust | Compute | Deps |
|---|---|---|---|---|---|---|---|
| **metaloom multisector** (today) | 1 × aHash of a temporal average | ✗ | fixed 256 b | HNSW k-NN | ✗ (ratio-anchored) | very low | none |
| **videohash** (akamhy) | 1 fps → tiled collage → wavelet hash | ✗ (documented) | fixed 64 b | Hamming | partial | low | ffmpeg |
| **pHash** | DCT-based, per image | n/a | fixed 64 b | Hamming | n/a | low | — |
| **PDQ** (Meta) | Jarosz filter → 64×64 luma → DCT → 16×16 block | n/a | fixed 256 b | Hamming | n/a | low | — |
| **vPDQ** (Meta) | per-frame PDQ, **unordered bag** | ✓ | variable | linear scan | ✓ | low | — |
| **TMK+PDQF** (Meta) | frame descriptors ⊗ sinusoidal time maps | ✗ | fixed ~256 KB | FAISS, ~O(1) | ✓ for offset | medium | — |
| **MPEG-7 Video Signature / ISCC Video-Code** | 380-value frame signature, summed → WTA hash | ✗ (rollup) | fixed | Hamming | partial | low | — |
| **SSCD + temporal alignment** (VSC'22 winners) | 512-d learned descriptor @ 1 fps + Δt alignment | ✓ | variable | ANN | ✓ | high | ONNX model |
| **ViSiL / TransVCL / Temporal Network** | frame-to-frame similarity matrix + alignment net | ✓ | variable | rerank only | ✓ | very high | model |

Three lessons the table makes hard to avoid:

1. **Excerpt matching requires per-segment descriptors.** Every ✓ in the clip-in-video column
   belongs to a scheme that stores many descriptors per video. Meta ships *both* TMK and vPDQ
   precisely because one fixed-length signature cannot do both jobs — that split is the field's
   own admission, from the people with the most data.
2. **DCT beats spatial averaging for the per-frame code.** pHash, PDQ and MPEG-7 all transform
   before thresholding. The current scheme is the only one in the table that thresholds raw
   blurred pixels, and blur-then-threshold is exactly what a low-pass DCT does, but worse — it
   keeps the DC term and discards phase structure the DCT would have retained.
3. **Alignment is a separate, cheap stage.** Once you have per-segment matches, verification is
   voting over relative offsets. TMK does it with Fourier feature maps, CTE with an FFT
   cross-correlation, the VSC'22 winners with a similarity matrix. All three are the same idea
   at different price points, and the cheapest version is a histogram.

---

## 3. Recommended concept — scene-anchored, multi-scale

```
                   ┌──────────────────────── ONE DECODE PASS ─────────────────────────┐
  video ──────────►│ decode @ 2–4 fps, letterbox-cropped, 64×64 luma                  │
                   │   ├─► shot detector  (histogram χ² + edge change ratio)          │
                   │   └─► per-frame DCT code (256 b) + quality score                 │
                   └──────────────────────────────┬──────────────────────────────────┘
                                                  ▼
                        ┌───────── per shot: majority-vote 256-bit SCENE CODE ────────┐
                        │  { code, t_from, t_to, quality, frameCount }                │
                        └──────────────┬───────────────────────────┬─────────────────┘
                                       ▼                           ▼
                          ROLLUP A: duration-weighted    ROLLUP B: shot-rhythm landmarks
                          SimHash over scene codes       (d₁,d₂,d₃) triplets → inverted index
                          → fixed-length video code      → O(1) candidates + Δt vote
                                       │                           │
                                       └────────────┬──────────────┘
                                                    ▼
                       MATCH  (A) recall: scene-code k-NN  +  rhythm lookup
                              (B) verify: Hough vote over Δt across matched scenes
                                  → score + matched time ranges
```

### 3.1 Tier 0 — one decode pass

Decode the video exactly once, at a reduced rate (2–4 fps is enough for shot detection and
far more than enough for hashing) and at low resolution. Shot detection, frame hashing and both
rollups all consume that same stream. Decode dominates the cost of everything in this document,
so the marginal cost of each additional consumer of the stream is near zero. Anything that
requires a *second* pass should be rejected on that basis alone.

Preprocessing that the current pipeline lacks and that belongs here:

- **Letterbox / pillarbox removal** before anything else. Today the frame is squashed to 512×512
  regardless, so a 16:9 clip and its 4:3 letterboxed re-upload produce structurally different
  images. Detecting and cropping constant-luma borders is a handful of row/column means and is
  the single cheapest robustness win available.
- **Aspect-ratio-preserving downscale** to the hash input size, after cropping.

### 3.2 Shot segmentation

Content-based, from the already-downscaled luma: per-frame colour/luma histogram χ² distance
plus an edge-change ratio, thresholded adaptively — PySceneDetect's `ContentDetector` in
spirit. The primitives already exist in `CVUtils` (`canny`, `mean`, `blurriness`, `normalize`).

This is deliberately the cheap detector, not the accurate one. Published F1 for
histogram-difference detectors on hard gradual transitions is under 0.6, versus ~0.9 for
TransNetV2. That gap does not matter here: a missed dissolve merges two shots, which the
oversized-shot subdivision in §3.4 handles anyway. Shot boundaries are a *segmentation prior*,
not ground truth, and the design must not become brittle if the prior is wrong.

**Why shot-anchored beats a fixed grid:** shot boundaries are *content-anchored*, so they land
at the same absolute content position in the original and in a trimmed copy. A fixed grid
anchored at `t = 0` does not — a clip starting at 03:07 carries a 7 s grid offset against every
reference window. That is the reason `NODE_FINGERPRINT_TASKS.md` Task 3 has to propose
overlapping windows (W=10 s, S=2 s) to make a grid work at all, at a cost of W/S ≈ 5× the rows.
Scene anchoring gets the alignment for free and produces far fewer rows.

### 3.3 Per-frame code — DCT, with a quality score

Replace `mean-threshold of blurred 16×16` with a PDQ-shaped construction:

```
letterbox-cropped luma → 64×64 (tent/Jarosz-filtered box average)
                       → 2-D DCT
                       → keep the 16×16 low-frequency block, excluding DC
                       → threshold at the median of that block → 256 bits
```

Two things come with it:

- **Median rather than mean threshold** guarantees a balanced code (128 set bits), which fixes a
  real pathology of mean-thresholding: near-uniform frames produce codes dominated by whichever
  side of the mean noise fell on.
- **A gradient-energy quality score**, as PDQ computes. This is what lets the pipeline *discard*
  fades, black frames, plain title cards and near-static logo screens — content that is
  low-information, matches everything, and is the most likely source of false positives. The
  current pipeline has only `isBlackFrame` (mean < 10), which catches pure black and nothing else.

This step is the largest robustness gain available without introducing a model, and it costs
microseconds per frame.

### 3.4 Scene code — majority vote over the shot

For each shot, take K frames (8–12, spread evenly, quality-filtered) and produce the scene code
by **bitwise majority vote** over their per-frame codes — a median hash of the shot. Majority
voting is the right aggregation here and averaging is not: a single mid-shot flash or subtitle
frame moves an average, but cannot outvote 7 of 11.

Each scene carries `{ code, t_from, t_to, quality, frameCount, voteMargin }`. `voteMargin`
(how decisive the vote was, per bit) is worth keeping — bits with a thin margin are the unstable
ones, and a matcher that knows which bits are unstable can weight them down.

**Hybrid segmentation, and the honest weak point.** Pure shot anchoring fails on content with no
cuts: screen recordings, static webcams, single-take phone video, long interviews. The design
must therefore be hybrid:

- shots longer than `W_max` (≈20 s) are subdivided into overlapping fixed windows within the shot;
- videos where the detector finds no boundaries at all fall back entirely to the overlapping grid
  of `NODE_FINGERPRINT_TASKS.md` Task 3(a).

This is not a footnote. A meaningful fraction of any real corpus is single-shot, and a design
that only works on edited content is a design that works on half the library. The grid fallback
is the price of covering the other half, and it should be stated in the config, not hidden.

### 3.5 Rollups — recovering a whole-video code

Two, because they answer different queries:

**A. Order-free — duration-weighted SimHash over the multiset of scene codes.** Each scene code
votes on each output bit with weight proportional to its duration and quality; sign the sum.
This is trim-robust for the reason the current stack is not: trimming removes *whole scenes* from
the multiset while leaving the rest bit-identical, so the code degrades in proportion to the
fraction removed. The current stack, by contrast, shifts all three of its sample points and
changes completely.

**B. Order-aware — "shot rhythm" landmarks.** Quantise shot durations on a log scale and hash
triplets of consecutive shots `(d₁, d₂, d₃)` into an inverted index, Shazam-style. Shot-duration
sequences are remarkably discriminative and survive re-encoding, rescaling, colour grading and
frame-rate change entirely, because they are a property of the edit, not of the pixels. Two
properties make this attractive:

- **O(1) candidate lookup.** A hash-table probe, not a k-NN search — the same reason Shazam can
  identify a track from five seconds against millions of references.
- **The offset comes free.** Each landmark hit carries `(t_query, t_reference)`; the histogram of
  `t_reference − t_query` peaks at the true alignment, which is the verification step of §3.6
  already done.

Log-scale quantisation is what makes B survive speed changes: a 1.05× re-upload is an additive
shift in log-duration, which stays inside one quantisation bucket for reasonable bucket widths.

Rollup B degenerates on single-shot content (no rhythm to hash), which is precisely where the
grid fallback of §3.4 and rollup A carry the load. The two mechanisms fail in different places,
which is the point of having both.

### 3.6 Matching — two stages

**Stage A, recall.** Per-scene code k-NN over a segment-level index (the metaloom schema already
has `sector_index` + `time_from`/`time_to` and DAO support; only the producer and the
`sectorIndex == 0` guards stand in the way), optionally seeded by rhythm-landmark hits.

**Stage B, verify and localise.** Collect matched scene pairs `(t_query, t_reference)`, histogram
`Δt = t_reference − t_query`, and require N scenes agreeing within a tolerance bucket. Report the
score and the covered time ranges on both sides.

This is Temporal Network / TMK alignment reduced to a histogram. It is a few dozen lines of plain
Java, it needs no matrix library, and it converts a bag of noisy scene matches into a statement
of the form *"query 00:12–00:47 corresponds to reference 04:31–05:06"* — which is what a dedup
UI actually needs to show a human, and something no fixed-length code can ever produce.

### 3.7 The optional neural tier

Define the frame descriptor behind an SPI so the DCT code can be swapped for a learned one
without touching segmentation, aggregation, rollups or matching. `facedetect4j` and `yolo4j`
already establish the ONNX loading pattern in this stack.

- **What it buys:** SSCD (ResNet50, 512-d, self-supervised for copy detection) reports µAP 98.3 /
  mAP 84.9 on DISC21, and was the *baseline* for the Meta Video Similarity Challenge, whose
  matching-track winner reached µAP 0.92 against a 0.44 baseline. Learned descriptors also
  handle the attacks the classical tier cannot: heavy overlays, picture-in-picture, crops,
  and — with the right augmentation — mirroring.
- **What it costs:** model weights, an ONNX runtime dependency in a module that currently has
  none, a GPU preference, and a hard versioning obligation (the descriptor becomes part of the
  algorithm identity).
- **Why the structure survives the swap:** everything in §3.4–§3.6 operates on "a descriptor per
  frame". Majority-vote binarisation becomes vector averaging plus optional binarisation;
  k-NN and Δt voting are unchanged.

---

## 4. Compute analysis

The intuition in the original question — *"if scenes were known the hash could just take 10
frames per scene, this would be fast"* — is right about the hashing and wrong about where the
cost lives.

**Hashing is free.** A 64×64 2-D DCT is on the order of microseconds. Ten frames per scene, a
hundred scenes, is a thousand DCTs — single-digit milliseconds for an entire feature film. If
scene boundaries were handed to you, the hashing genuinely costs nothing.

**Finding the scenes is the cost.** Boundaries cannot be known without looking at the timeline,
so the pass changes shape:

| | today | proposed |
|---|---|---|
| decoded frames | ~51 | duration × decode rate (2–4 fps) |
| coverage | ~1.7 s, all in the first 45 % | whole timeline |
| per-frame work | Lanczos resize to 512×512, 15×15 blur, two per-pixel `Mat.get`/`put` loops (256 native calls × 2) | crop + downscale + 64×64 DCT |
| seeks | 3 | 1 (sequential) |

Three observations that keep this bounded:

1. **The proposed per-frame work is cheaper than today's.** The current pipeline resizes to
   512×512 with `INTER_LANCZOS4`, box-blurs at that resolution, then walks 256 pixels twice per
   frame through individual native `get`/`put` calls, allocating a fresh `CV_64FC1` `Mat` each
   time. A DCT on an already-small buffer is less work than that. The cost increase is entirely
   in the *number* of frames, not the work per frame.
2. **The pass is decode-bound and sequential.** One linear read beats three seeks on any
   container that isn't fully indexed, and reduced-rate decoding of low-resolution luma runs
   faster than real time on a single desktop core. Published scene-detection pipelines that
   decode at ≤2 fps and 256×144 report runtimes close to raw decode speed.
3. **The decode rate is a knob.** 1 fps is enough for scene codes if boundaries are approximate;
   4 fps buys better boundary precision. A keyframe-only first pass is available as an even
   cheaper coarse mode, since I-frames decode independently.

**Amortisation.** `PreviewGenerator` already walks the timeline to build contact sheets. Any
pipeline that generates previews is already paying for a full traversal, and fusing the
fingerprint pass into it makes the marginal cost of fingerprinting close to zero. This is worth
designing for explicitly rather than discovering later.

**Verdict.** Not free, but a bounded constant-factor increase on a pass that is I/O- and
decode-bound — in exchange for excerpt matching, scene-level retrieval and time-range
localisation, none of which are achievable at any price with the current design.

---

## 5. Matryoshka embeddings

**Adopt the principle now; adopt the model only with the neural tier.**

Matryoshka Representation Learning trains a single embedding so that every prefix
(64/128/256/512-d…) is independently a good representation, by adding a loss term at each
granularity. Reported gains are real — up to ~14× reduction/speedup for equal accuracy — and the
canonical retrieval use is exactly the two-stage matcher of §3.6: shortlist on the short prefix,
re-rank on the full vector.

Two things follow for this design:

- **MRL cannot be retrofitted onto a DCT hash.** It is a property of *training*, and there is no
  training in the classical tier. Claims to the contrary should be treated with suspicion.
- **But the classical tier gets the same property for free, and should claim it explicitly.**
  DCT coefficients are already ordered by frequency, and low frequencies carry the coarse
  structure. A 64-bit code built from the lowest 8×8 block is a genuine coarse version of the
  256-bit code built from the 16×16 block — the same image content, less detail. **Make
  prefix-truncatability an explicit, documented property of the code layout**: order the bits so
  that any prefix is a valid, self-consistent shorter code. Then cheap recall on 64 bits and
  verification on 256 bits work without storing two codes.

  This also happens to be what the existing `compactVector()` gestures at (64 floats, each the
  popcount of 4 consecutive bits) — but that construction sums *spatially adjacent* bits of a
  row-major image, not frequency-ordered ones, so its coarse code is a blur rather than a
  low-pass. Frequency ordering makes the same trick principled.

If the neural tier lands, prefer an MRL-trained descriptor for exactly this reason: 64-d for the
Lucene shortlist, 512-d for verification, one model, one stored vector.

---

## 6. Quaternions

**Not recommended.** The reasoning matters more than the verdict, because the idea is a good one
in its home domain.

**The cited work solves a different problem.** *Quaternion Knowledge Graph Embeddings*
(arXiv:1904.10281) represents entities as quaternions and **relations as 4-D rotations** applied
via the Hamilton product. The win comes from what rotations in ℍ can express about *relation
types* — symmetry, anti-symmetry, inversion, composition — with more degrees of freedom than the
complex plane gives ComplEx. That expressiveness is about a *taxonomy of relations*.

Video fingerprints have no such taxonomy. There is exactly one relation between two fingerprints
and it is a scalar: *this one occurs Δt after that one*. Encoding a one-parameter group needs one
angle. Three imaginary components buy degrees of freedom with nothing to spend them on, and the
Hamilton product's non-commutativity — the source of its power for asymmetric relations — is
actively unhelpful when the relation you are modelling *is* commutative in the way time shifts
compose.

**The relevant quaternion literature is a different line entirely.** Quaternion Discrete Fourier
Transform and quaternion SVD *are* used for perceptual **colour** image hashing (QDFT + log-polar
for rotation robustness; Q-SVD; quaternion polar complex exponential transforms), and the win
there is genuine: encoding R, G, B as the three imaginary parts processes colour channels jointly
rather than collapsing them, preserving inter-channel structure.

That is a real gap here — the current pipeline discards colour at step two, and colour is
discriminative. But the cheap fixes dominate:

- append a small MPEG-7-style **colour layout descriptor** (DCT of an 8×8 average-colour grid)
  as extra bits alongside the luma code; or
- hash channels separately and concatenate; or
- hash on an opponent-colour channel in addition to luma.

Each is a few hours of work with existing OpenCV calls. Against that, quaternions bring: no Java
hypercomplex library in this stack, a substantially harder implementation, and — decisively — no
published evidence of quaternion codes winning on any video copy-detection benchmark. The
technique lives in image-authentication papers, not in the systems that actually run at scale
(PDQ, TMK, SSCD, MPEG-7 all avoid it).

**Revisit if:** colour-graded re-uploads turn out to be a dominant failure mode *and* the cheap
colour descriptors above prove insufficient.

---

## 7. Angles and phase — yes, but complex, not hypercomplex

The instinct that *the angle between components could carry semantics* is correct, is
well-established, and the semantics it carries is **time**.

**TMK — Temporal Match Kernel.** Poullot et al. convert a sequence of frame descriptors into a
single visual-temporal descriptor by encoding each descriptor **jointly with its timestamp**
through periodic sinusoidal feature maps. Because the time encoding is periodic, a component's
*phase* records *when* the content occurred. Matching two videos yields a **score function over
all possible relative timestamps**, whose maximum gives both the similarity and the time offset.
The signature is fixed length regardless of frame count, which is what makes it indexable.
Meta's TMK+PDQF is this construction with PDQ-float frame descriptors, producing ~256 KB per
video.

**Circulant Temporal Encoding.** Douze, Revaud, Verbeek, Jégou and Schmid encode frame
descriptors to represent appearance and temporal order jointly, then exploit the properties of
circulant matrices to compare videos **in the frequency domain** — one FFT-based operation
returns the correlation across all offsets at once, giving score and localisation together, with
a product quantiser adapted to complex numbers for compressed storage.

So: phase encodes time, and the mechanism is complex-valued. Two dimensions suffice, because a
time shift is a one-parameter group. This is the concrete, defensible version of the
"angle encodes semantics" idea — and it does not need quaternions.

**Recommendation.** Treat phase-encoded temporal aggregation as an **optional Tier-1.5 rollup**,
a principled replacement for the naive "stack" if a fixed-length whole-video signature is wanted
alongside the scene codes. But keep per-scene codes plus Δt voting as the **primary** mechanism:

- it localises (TMK gives a global offset; scene matching gives per-range correspondence);
- it degrades gracefully (a partially-matching video still matches partially);
- it survives insertion and reordering, which shift-based alignment does not;
- it is far simpler to implement correctly in Java than a complex-valued kernel with a product
  quantiser.

And note again that Meta, having built TMK, still had to build vPDQ. A fixed-length
phase-encoded signature does not remove the need for per-segment codes; it complements them.

---

## 8. Domain transforms — what they can and cannot buy

Directly addressing: *could transforming the signal into another domain make the embedding less
prone to temporal clipping, so a short section can find its source?*

### 8.1 Why a global Fourier transform does not do this

`|FFT|` is invariant to **translation** — a time shift changes only phase, leaving magnitudes
untouched. That is a genuine invariance and it is the reason the idea is appealing.

But clipping is not a translation. **Clipping is a change of support**, and in the frequency
domain that is destructive:

- Truncating to a window is *multiplication by a rectangle in time*, hence **convolution with a
  sinc in frequency**. Every coefficient is smeared by every other. Nothing is preserved
  exactly.
- If the frequency axis is normalised by duration (as it must be, for codes of different-length
  videos to be comparable), trimming **rescales the axis**. Bin *k* of the clip and bin *k* of
  the source no longer describe the same temporal frequency.
- The shift-invariance of `|FFT|` is *circular* in the first place; linear shift on a finite
  record already leaks.

A global Fourier code is therefore **less** trim-robust than the naive intuition suggests, not
more.

### 8.2 The real obstacle is aggregation, not domain

This is the load-bearing argument, and it is basis-independent.

The Fourier transform is **linear**: the transform of the whole is the sum of the transforms of
the parts. So a clip's contribution to any whole-video code built by pooling is proportional to
`clip_length / video_length` — about **0.1 %** for a 10 s excerpt of a 2 h video. That holds in
the pixel domain, the DCT domain, the wavelet domain, and any other invertible linear basis you
choose. Choosing a better basis cannot recover a signal that pooling attenuated by three orders
of magnitude.

**The clip-matching problem is not a domain problem. It is an aggregation problem.** There are
exactly two escapes:

1. **Don't pool globally** — keep local descriptors (windows, scenes). This is §3.
2. **Pool non-linearly over sparse salient events** — peak picking, landmarks. Shazam works
   because peak selection is nonlinear and each landmark is local; the loud peaks of a 5-second
   excerpt are the same peaks as in the full track. Rollup B in §3.5 is this idea applied to
   shot boundaries.

Meta shipping both TMK and vPDQ is the empirical confirmation. So is videohash's own
documentation, which states plainly that it cannot determine whether one video is contained in
another — its collage-plus-wavelet-hash is a global pooling, and no amount of wavelet cleverness
changes that.

### 8.3 Where a transform genuinely pays off

Localise it. The correct analogue is the **STFT, not the FFT** — short overlapping windows, each
transformed independently. Three concrete placements, all adopted above:

1. **Spatially, per frame — DCT.** pHash and PDQ. Robustness to blur, compression, rescaling and
   mild geometric change comes from discarding high frequencies while keeping the low-frequency
   *structure*, rather than blurring and thresholding pixels. Largest classical win available
   (§3.3).
2. **Temporally, per window — DCT or DWT over the descriptor sequence.** Resample a window's
   frame-descriptor sequence to a fixed number of samples — **that resampling is the frame-rate
   normalisation**, exactly and for free — then transform along the time axis and keep low-order
   coefficients. These survive mild time-warping and small speed changes. This is the
   TIRI-2D-DWT line of work (temporal informative representative image + wavelet transform),
   and it is a natural refinement of the majority-vote scene code in §3.4 if per-scene *motion*
   turns out to be discriminative.
3. **As an alignment engine, not an invariance.** Circulant Temporal Encoding uses the FFT to
   evaluate cross-correlation at every offset in one `O(n log n)` pass. Here the frequency
   domain buys **speed**, not robustness — a different and much better-founded reason to use it.
   If the Δt histogram of §3.6 ever becomes a bottleneck, this is its principled replacement.

### 8.4 One elegant extra: log-time for speed invariance

Speed-changed re-uploads (0.95×, 1.05× — common for evading content ID) are a *scaling* of the
time axis. Fourier magnitude on a **logarithmic** time axis turns scaling into translation and
therefore into a phase change, giving scale invariance in magnitude — the Mellin trick, the same
one used with log-polar coordinates for rotation-invariant image hashing.

Offered as an option, not a recommendation. It needs resampling onto a log grid, it trades away
temporal resolution at the tail, and rollup B's log-quantised shot durations already provide
most of the same robustness far more cheaply. Reach for it only if speed-change attacks are shown
to matter.

---

## 9. Robustness matrix

Target behaviour of the recommended design. **These are design intentions, not measurements** —
nothing here has been built, and §10 exists because claims like this are worthless unverified.

| Attack | Today | Proposed (classical) | With neural tier | Mechanism |
|---|---|---|---|---|
| Re-encode / bitrate change | good | good | good | DCT low-frequency block |
| Resolution change | good | good | good | fixed-size normalisation |
| Aspect-ratio change | accidental | good | good | crop then AR-preserving downscale |
| Letterbox add/remove | poor | **good** | good | border detection + crop (§3.1) |
| Moderate crop | poor | fair | **good** | learned descriptors handle crop; DCT does not |
| Logo / burn-in overlay | poor | fair | **good** | quality gate rejects worst frames; majority vote absorbs some |
| Brightness / contrast / gamma | good | good | good | median threshold on DCT coefficients |
| Colour grading | n/a (colour discarded) | fair | good | luma-only unless a colour descriptor is added (§6) |
| Frame-rate change | good | good | good | fixed-count resampling per window |
| Speed change (±5 %) | fair | **good** | good | log-quantised shot durations (§3.5 B) |
| Trim head/tail | **poor** | **good** | good | content-anchored scenes; multiset rollup |
| Clip extraction (find excerpt) | **impossible** | **good** | good | per-scene index + Δt voting |
| Scene reordering / recut | poor | good | good | order-free rollup A + per-scene matching |
| Mirroring | **fails** | **fails** | fair | needs code augmentation or a trained descriptor |
| Rotation | **fails** | **fails** | fair | same; log-polar would help but costs a second transform |
| Single-shot content | fair | fair | fair | grid fallback (§3.4); rhythm rollup degenerates |

Mirroring and rotation stay unsolved in the classical tier.
[`fingerprint/README.md`](../fingerprint/README.md) already concedes this — *"there is no
robustness against image rotation or mirroring of the image. Other solutions like `pHash` might
be better"* — and the honest options are: hash the mirrored frame too and store both codes (2× rows,
solves mirroring exactly), adopt a log-polar transform (solves rotation, costs a second
transform and discrimination), or wait for the neural tier. Recommendation: **store the mirrored
code** if mirroring matters — it is the cheapest exact solution — and accept rotation as
out of scope, since rotated video re-uploads are rare compared to mirrored ones.

---

## 10. Evaluation — what "better" would have to mean

**No comparative claim in this document should be believed until a harness exists.** None does.
Today's entire evidence base is one test that hashes one clip at three resolutions and asserts
Levenshtein ≤ 5 over hex characters — with **no negative set at all**, so the false-positive rate
of the shipped fingerprint is literally unknown. Any successor that is not measured against the
same corpus is a different guess, not an improvement.

The shape to build, modelled on the sibling `face-eval` and `audio-eval` harnesses in this
workspace (`harness/` SPI + `engines/` + `scoring/` + `report/` → a generated results markdown,
gitignored `assets/`):

- **Corpus.** Clips of varied length, shot density and genre, including single-shot content.
  A negative set of *unrelated* videos is mandatory, and should be at least an order of magnitude
  larger than the positive set — false positives are the failure mode that matters for dedup,
  because they delete files.
- **Attack generation.** ffmpeg-scripted variants covering every row of §9: re-encode at several
  CRFs, rescale, crop, letterbox, overlay a logo, shift brightness/gamma, change fps, change
  speed, trim head/tail at several ratios, extract clips of 5/10/30 s from random offsets,
  mirror, rotate.
- **Metrics.** µAP for ranking quality (comparable to the published DISC21/VSC numbers),
  **recall at a fixed false-positive rate** for the operational question, and for excerpt
  queries the temporal IoU of the reported time range against ground truth. Report per-attack,
  not just aggregate — an average hides that one attack fails completely.
- **Engines.** The current multisector fingerprinter, the proposed scheme, and ideally vPDQ as an
  external reference point, all behind the same SPI so numbers are comparable.
- **Cost measurement alongside accuracy.** Wall-clock per minute of video and bytes stored per
  minute. §4's argument is a prediction; the harness is what turns it into a fact.

---

## 11. Migration and compatibility notes

Brief and non-prescriptive — the producer- and index-side work belongs in the metaloom spec tree.

- **A new scheme needs a new `algorithm` identifier.** In metaloom, `algorithm` is the *only*
  discriminator separating vector populations inside the single shared Lucene index, and
  `producer_version` currently defaults to `''` and is never written. Two schemes under one
  identifier silently pollute one index.
- **Keep the existing whole-video code emitting under its current identifier.** It is cheap, it
  is what the dedup path uses today, and rollup A can be introduced beside it rather than
  instead of it.
- **The segment-level schema already exists.** `asset_fingerprint_comp` has `sector_index`,
  `time_from` and `time_to`, and the DAO already round-trips windowed rows
  (`AssetFingerprintSegmentCompDaoTest` writes eight). What blocks it is the producer, which
  hardcodes `sector_index = 0`, and the `sectorIndex == 0` guards in
  `FingerprintCompEndpointService` and `SimilarityEndpointService`.
- **Retire the word "sector".** It means *temporal sample point folded into one vector* in
  video4j and *timeline window* in the metaloom schema. `NODE_FINGERPRINT_TASKS.md` Task 5
  already asks for this. This document uses **scene** and **window** and never "sector" for
  anything new.
- **Scene anchoring is the cheaper storage option, and this is worth stating.** A 2 s-stride
  overlapping grid produces roughly `duration / stride` documents — ~1200 for a 40-minute video.
  Scene anchoring produces one row per shot, typically a few hundred for the same video and far
  fewer for slow-cut content, while aligning better. The storage argument and the accuracy
  argument point the same way, which is unusual and worth taking.
- **Score semantics change.** Today `score = 1/(1 + hamming)` over 256 0/1 floats, and the 0.10
  threshold means "≤ 9 differing bits". Any change to code length, encoding or aggregation
  invalidates that threshold, the demo fixtures and `dedup_group.score` simultaneously. A new
  algorithm identifier is the natural place to carry new thresholds.

---

## 12. References

**Implementations and standards**
- [videohash (akamhy)](https://github.com/akamhy/videohash) — 1 fps → collage → wavelet hash, 64 bit; documents its own inability to do clip-in-video
- [pHash](https://www.phash.org/)
- [Meta ThreatExchange — PDQ, vPDQ, TMK+PDQF](https://github.com/facebook/ThreatExchange) · [vpdq](https://github.com/facebook/ThreatExchange/tree/main/vpdq) · [tmk](https://github.com/facebook/ThreatExchange/tree/main/tmk)
- [PHVSpec: A Benchmark-based Analysis of Perceptual Hash Systems for Videos](https://technologycoalition.org/wp-content/uploads/Tech-Coalition-Video-Hash-Benchmark-Paper.pdf)
- [ISCC Video-Code (IEP-0006)](https://ieps.iscc.codes/iep-0006/) · [ISCC codec docs](https://core.iscc.codes/units/content/code_content_video/) — MPEG-7 Video Signature + WTA hash
- [MPEG-7 Video Signature Tools for Content Identification](https://www.researchgate.net/publication/241638907_The_MPEG7_Video_Signature_Tools_for_Content_Identification)

**Temporal encoding and alignment**
- [Temporal Matching Kernel with Explicit Feature Maps (Poullot et al., ACM MM 2015)](https://inria.hal.science/hal-01842277v1/file/p381-poullot.pdf)
- [Circulant Temporal Encoding for Video Retrieval and Temporal Alignment (Douze et al., IJCV 2015)](https://inria.hal.science/hal-01162603/file/paper.pdf)
- [facebookresearch/videoalignment](https://github.com/facebookresearch/videoalignment)
- [How Shazam works — landmark hashing and offset voting](https://www.cameronmacleod.com/blog/how-does-shazam-work)

**Learned descriptors and benchmarks**
- [A Self-Supervised Descriptor for Image Copy Detection (SSCD)](https://arxiv.org/pdf/2202.10261) · [code](https://github.com/facebookresearch/sscd-copy-detection)
- [The 2023 Video Similarity Dataset and Challenge](https://arxiv.org/pdf/2306.09489) · [winners](https://drivendata.co/blog/meta-vsc-winners) · [3rd place solution](https://arxiv.org/abs/2304.11964)
- [VCSL: segment-level video copy detection dataset and protocol](https://arxiv.org/pdf/2203.02654)
- [ViSiL: Fine-grained Spatio-Temporal Video Similarity Learning](https://arxiv.org/pdf/1908.07410)
- [TransVCL: Attention-enhanced Video Copy Localization](https://arxiv.org/pdf/2211.13090)
- [Counteracting temporal attacks in Video Copy Detection](https://arxiv.org/pdf/2501.11171)

**Shot boundaries and scene policy**
- [TransNet V2: fast shot transition detection](https://github.com/soCzech/TransNetV2) · [paper](https://ar5iv.labs.arxiv.org/html/2008.04838)
- [AutoShot: A Short Video Dataset and SOTA Shot Boundary Detection](https://ar5iv.labs.arxiv.org/html/2304.06116)
- [Scene Detection Policies and Keyframe Extraction Strategies for Large-Scale Video Analysis](https://arxiv.org/html/2506.00667v1)
- [Video fingerprinting based on TIRI-2D-DWT of shot boundary neighbourhoods](https://www.researchgate.net/publication/293460425_Video_fingerprinting_based_on_TIRI-2D-DWT_of_brut_cut_shot_boundary's_neighborhood)
- [Graph-Based Fingerprinting for Robust Video Sequence Identification under Temporal Reordering](https://journal.50sea.com/index.php/IJIST/article/view/1839)

**Nested representations**
- [Matryoshka Representation Learning](https://arxiv.org/abs/2205.13147)
- [SMEC: Rethinking Matryoshka Representation Learning for Retrieval Embedding Compression](https://arxiv.org/pdf/2510.12474)

**Quaternion / hypercomplex**
- [Quaternion Knowledge Graph Embeddings (arXiv:1904.10281)](https://arxiv.org/abs/1904.10281) — the paper that prompted the question
- [Robust hashing for image authentication using quaternion DFT and log-polar transform](https://hal.science/hal-01222780)
- [Robust perceptual colour image hashing using quaternion SVD](https://ieeexplore.ieee.org/document/6854311/)
- [Robust Colour Image Hashing Using Quaternion Polar Complex Exponential Transform](https://link.springer.com/article/10.1007/s00034-018-0822-8)

**Hamming-space search**
- [Fast Exact Search in Hamming Space with Multi-Index Hashing](https://arxiv.org/pdf/1307.2982) · [code](https://github.com/norouzi/mih)

**In-workspace prior art**
- `metaloom/spec/tasks/NODE_FINGERPRINT_TASKS.md` — producer-side audit; Task 3 window options (a)/(b)/(c)
- `metaloom/spec/loom/SEARCH_LUCENE.md` — index lifecycle, score arithmetic, §7 gotchas
- `metaloom/spec/features/nodes/dedup/NODE_DEDUP.md` — how fingerprint scores become dedup decisions
- `metaloom/website/content/english/blog/video-fingerprinting/index.adoc` — the 2021 write-up (describes v1, now outdated)

---

_Concept only. Nothing in this document is implemented._
_Last updated: 2026-08-12_
