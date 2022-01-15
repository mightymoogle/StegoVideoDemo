package org.chaosdragon.stegovideo.algorithms;

/**
 * An algorithm that only embeds in RGB, not YCbCr
 *
 * @author David Griberman
 */
public abstract class RGBOnlyDCTEmbeddingAlgorithm extends DCTEmbeddingAlgorithm {

    protected RGBOnlyDCTEmbeddingAlgorithm(int blockSize, int compression) {
        super(blockSize, compression);
    }

    @Override
    protected int[][][] convertToYCbCrMatrix(int[][][] rgb) {
        return rgb;
    }

    @Override
    protected int[][][] convertToRGBMatrix(int[][][] ycbr) {
        return ycbr;
    }

}
