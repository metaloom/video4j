package io.metaloom.video4j.fingerprint.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.KnnVectorsFormat;
import org.apache.lucene.codecs.lucene103.Lucene103Codec;
import org.apache.lucene.codecs.lucene99.Lucene99HnswVectorsFormat;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.MMapDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.metaloom.video4j.fingerprint.Fingerprint;
import io.metaloom.video4j.fingerprint.index.query.QueryResult;
import io.metaloom.video4j.fingerprint.index.query.QueryResultEntry;
import io.metaloom.video4j.fingerprint.index.query.QueryResultFactory;
import io.metaloom.video4j.fingerprint.v2.MultiSectorFingerprint;

public abstract class AbstractFingerprintIndexer<T extends QueryResult<R>, R extends QueryResultEntry> implements FingerprintIndexer<T, R> {

	public static final Logger log = LoggerFactory.getLogger(AbstractFingerprintIndexer.class);

	public static final String VECTOR_FIELD_KEY = "fingerprint";

	public static final String HASH_FIELD_KEY = "sha512sum";

	private final Path indexPath;

	private final QueryResultFactory<T, R> factory;

	private final boolean useQuadVectoring;

	private static final Codec codec = new Lucene103Codec() {
		@Override
		public KnnVectorsFormat getKnnVectorsFormatForField(String field) {
			int maxConn = 200;
			int beamWidth = 100;
			KnnVectorsFormat knnFormat = new Lucene99HnswVectorsFormat(maxConn, beamWidth);
			return new HighDimensionKnnVectorsFormat(knnFormat, MultiSectorFingerprint.FINGERPRINT_VECTOR_SIZE);
		}
	};

	public AbstractFingerprintIndexer(Path indexPath, QueryResultFactory<T, R> factory, boolean useQuadVectoring) {
		this.indexPath = indexPath;
		this.factory = factory;
		this.useQuadVectoring = useQuadVectoring;
	}

	public void writer(Consumer<IndexWriter> consumer) throws IOException {
		try (MMapDirectory dir = new MMapDirectory(indexPath)) {
			try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig().setCodec(codec))) {
				consumer.accept(writer);
			}
		}
	}

	@Override
	public T query(Fingerprint fingerprint, int limit) throws IOException {
		try (MMapDirectory dir = new MMapDirectory(indexPath)) {
			try (IndexReader reader = DirectoryReader.open(dir)) {
				return queryIndex(reader, fingerprint, limit);
			}
		}
	}

	private T queryIndex(IndexReader reader, Fingerprint fingerprint, int limit) throws IOException {
		IndexSearcher searcher = new IndexSearcher(reader);
		float[] queryVector;

		// Check whether search should utilize the compacted vector form of the fingerprint.
		if (isQuadVectoringEnabled()) {
			queryVector = fingerprint.quadVector();
		} else {
			queryVector = fingerprint.vector();
		}
		TopDocs results = searcher.search(new KnnFloatVectorQuery(VECTOR_FIELD_KEY, queryVector, limit), 10);
		return factory.from(reader, results);
	}

	/**
	 * Check whether the quad vectoring feature is enabled.
	 * 
	 * @return
	 */
	protected boolean isQuadVectoringEnabled() {
		return useQuadVectoring;
	}

}
