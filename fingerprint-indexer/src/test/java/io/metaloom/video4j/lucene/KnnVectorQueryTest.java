package io.metaloom.video4j.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.KnnFloatVectorField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.KnnFloatVectorQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.MMapDirectory;
import org.junit.jupiter.api.Test;

public class KnnVectorQueryTest {

	public static final Path indexPath = Paths.get("target/index");
	public static final float[] queryVector = new float[] { 0.98f, 0.01f };

	// Goal vector is very close to our actual query vector
	public static final float[] goalVector = new float[] { queryVector[0] - 0.01f, queryVector[1] + 0.01f };

	@Test
	public void testQuery() throws IOException {

		File file = indexPath.toFile();
		if (file.exists()) {
			FileUtils.deleteDirectory(file);
		}

		try (MMapDirectory dir = new MMapDirectory(indexPath)) {
			try (IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig())) {
				for (int i = 0; i < 10; i++) {
					float a = (float) Math.random();
					float b = (float) Math.random();
					Document doc = new Document();
					doc.add(new StoredField("id", i));
					doc.add(new KnnFloatVectorField("field", new float[] { a, b }));
					writer.addDocument(doc);
					System.out.println("[" + i + "] => [" + String.format("%.2f", a) + ", " + String.format("%.2f", b) + "]");
				}
				// Add final doc which is very close to the target
				int goalId = 10;
				Document goalDoc = new Document();
				goalDoc.add(new StoredField("id", goalId));
				goalDoc.add(new KnnFloatVectorField("field", goalVector));

				writer.addDocument(goalDoc);
				System.out.println(
					"[" + goalId + "] => [" + String.format("%.2f", goalVector[0]) + ", " + String.format("%.2f", goalVector[1]) + "] <-- Goal");
			}
			System.out.println();
			try (IndexReader reader = DirectoryReader.open(dir)) {
				IndexSearcher searcher = new IndexSearcher(reader);
				System.out
					.println("Query: [" + String.format("%.2f", queryVector[0]) + ", " + String.format("%.2f", queryVector[1]) + "]");
				TopDocs results = searcher.search(new KnnFloatVectorQuery("field", queryVector, 3), 10);
				System.out.println("Hits: " + results.totalHits);

				StoredFields storedFields = reader.storedFields();
				for (ScoreDoc sdoc : results.scoreDocs) {
					Document doc = storedFields.document(sdoc.doc);
					StoredField idField = (StoredField) doc.getField("id");
					System.out.println("Found: " + idField.numericValue() + " = " + String.format("%.1f", sdoc.score));
				}
			}
		}

	}
}
