package org.chaosdragon.stegovideo.algorithms;

import org.chaosdragon.stegovideo.embedders.KothariEmbedder;

import java.io.IOException;

/**
 * Implements the Kothari algorithm
 *
 * @author David Griberman
 */
public class KothariAlgorithm extends RGBOnlyDCTEmbeddingAlgorithm {

    int block;

    public KothariAlgorithm(int blockSize, int compression, int str) {
        super(blockSize, compression);
        dtc = new KothariEmbedder(str);
        block = blockSize;
    }

    @Override
    public boolean supportsAdaptive() {
        return true;
    }

    @Override
    protected int[][] hideAfterQuantisation(int[][] data, int channel, int[][] str) {
        int s = str[0][0];
        return ((KothariEmbedder) dtc).embed(data, channel, s); //Move to algorithm?
    }

    @Override
    protected void extractBeforeQuantisation(int[][] data, int channel) throws IOException {
        // NOOP
    }

    @Override
    protected int[][] hideBeforeQuantisation(int[][] data, int channel) throws IOException {
        return data;
    }

    @Override
    protected int[][] hideAfterQuantisation(int[][] data, int channel) {
        return dtc.embed(data, channel);
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
