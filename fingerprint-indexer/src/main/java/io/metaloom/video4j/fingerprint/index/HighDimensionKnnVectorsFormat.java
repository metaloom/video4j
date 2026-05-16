package io.metaloom.video4j.fingerprint.index;

import java.io.IOException;

import org.apache.lucene.codecs.KnnVectorsFormat;
import org.apache.lucene.codecs.KnnVectorsReader;
import org.apache.lucene.codecs.KnnVectorsWriter;
import org.apache.lucene.index.SegmentReadState;
import org.apache.lucene.index.SegmentWriteState;

public class HighDimensionKnnVectorsFormat
        extends KnnVectorsFormat {
    private final KnnVectorsFormat knnFormat;
    private final int maxDimensions;

    public HighDimensionKnnVectorsFormat(KnnVectorsFormat knnFormat, int maxDimensions) {
        super(knnFormat.getName());
        this.knnFormat = knnFormat;
        this.maxDimensions = maxDimensions;
    }

    @Override
    public KnnVectorsWriter fieldsWriter(SegmentWriteState state) throws IOException {
        return knnFormat.fieldsWriter(state);
    }

    @Override
    public KnnVectorsReader fieldsReader(SegmentReadState state) throws IOException {
        return knnFormat.fieldsReader(state);
    }

    @Override
    public int getMaxDimensions(String fieldName) {
        return maxDimensions;
    }

}
