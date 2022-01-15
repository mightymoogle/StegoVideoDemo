package org.chaosdragon.stegovideo.algorithms;

import org.chaosdragon.stegovideo.embedders.KaurEmbedder;

import java.io.IOException;

/**
 * Does nothing, no embedding. Use for compressing.
 *
 * @author David Griberman
 */
public class NullAlgorithm extends RGBOnlyDCTEmbeddingAlgorithm {

    @Override
    protected int[][] hideBeforeQuantisation(int[][] data, int channel) throws IOException {
        return data;
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
        // NOOP
    }

    @Override
    protected void extractAfterQuantisation(int[][] data, int channel) throws IOException {
        // NOOP
    }

    public NullAlgorithm(int blockSize, int compression) {
        super(blockSize, compression);
        dtc = new KaurEmbedder(0); //Does nothing, needed so no NULL exception
    }

    @Override
    protected int[][] forwardDCT(int[][] data) {
        return data;
    }

    @Override
    protected int[][] inverseDCT(int[][] data) {
        return data;
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
