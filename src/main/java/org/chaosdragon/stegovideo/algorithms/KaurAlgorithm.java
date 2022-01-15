package org.chaosdragon.stegovideo.algorithms;

import org.chaosdragon.stegovideo.embedders.KaurEmbedder;

import java.io.IOException;

/**
 * Implements the Kaur algorithm
 *
 * @author David Griberman
 */
public class KaurAlgorithm extends DCTEmbeddingAlgorithm {

    int block;

    public KaurAlgorithm(int blockSize, int compression, int str) {
        super(blockSize, compression);
        dtc = new KaurEmbedder(str);
        block = blockSize;
    }

    @Override
    protected int[][] hideBeforeQuantisation(int[][] data, int channel) {
        return data;
    }

    @Override
    protected int[][] hideAfterQuantisation(int[][] data, int channel) {
        return dtc.embed(data, channel);
    }

    @Override
    public boolean supportsAdaptive() {
        return true;
    }

    @Override
    protected int[][] hideAfterQuantisation(int[][] data, int channel, int[][] str) {
        int s = str[0][0];
        return ((KaurEmbedder) dtc).embed(data, channel, s); //Move to algorithm?
    }

    @Override
    protected void extractBeforeQuantisation(int[][] data, int channel) {
        // NOOP
    }

    @Override
    protected void extractAfterQuantisation(int[][] data, int channel) throws IOException {
        dtc.decodeBlock(data, channel);
    }

    @Override
    public int calculateMaxSize(int w, int h) {
        return w / block * h / block;
    }

}
