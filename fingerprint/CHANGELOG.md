## 1.1.0

* Fingerprint have been optimized and will now contain version information.
* Added (v1) `BinaryFingerprint` and (v2) `MultiSectorFingerprint`. The `MultiSectorFingerprint` will utilize multiple areas/sectors of the video in order to generate a more robust fingerprint.
* Added interfaces for fingerprinter, fingerprint.
* Reworked fingerprinting generation (Added codec, fingerprinter and fingerprint implementation)
* Fingerprints are now represented by `Fingerprint` objects.

## 1.0.0

Initial release