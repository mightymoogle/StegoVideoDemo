package org.chaosdragon.stegovideo.algorithms;

import org.chaosdragon.stegovideo.embedders.DubaiEmbedder;

import java.io.IOException;

/**
 * A class implementing the Dubai algorithm
 *
 * @author David Griberman
 */
public class DubaiAlgorithm extends RGBOnlyDCTEmbeddingAlgorithm {

    private final int block;

    public DubaiAlgorithm(int blockSize, int compression, int strength) {
        super(blockSize, compression);
        dtc = new DubaiEmbedder(strength);
        block = blockSize;
    }

    @Override
    public int calculateMaxSize(int w, int h) {
        return w / block * h / block;
    }

    @Override
    protected int[][] hideBeforeQuantisation(int[][] data, int channel) {
        return dtc.embed(data, channel);
    }

    @Override
    protected int[][] hideAfterQuantisation(int[][] data, int channel) throws IOException {
        return data;
    }

    @Override
    protected int[][] hideAfterQuantisation(int[][] data, int channel, int[][] str) throws IOException {
        return data;
    }

    @Override
    protected void extractBeforeQuantisation(int[][] data, int channel) throws IOException {
        dtc.decodeBlock(data, channel);
    }

    @Override
    protected void extractAfterQuantisation(int[][] data, int channel) throws IOException {
        // NOOP
    }

    @Override
    protected int[][] quantitizeImage(int[][] data) {
        return data;
    }

    @Override
    protected int[][] dequantitizeImage(int[][] data) {
        return data;
    }

}
