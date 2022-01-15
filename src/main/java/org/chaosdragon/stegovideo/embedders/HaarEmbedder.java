package org.chaosdragon.stegovideo.embedders;

import org.chaosdragon.stegovideo.encoders.MessageEncoder;
import org.chaosdragon.stegovideo.tools.HaarDWT;

import java.io.IOException;
import java.io.OutputStream;

import static org.chaosdragon.stegovideo.tools.HaarDWT.paste1DArray;
import static org.chaosdragon.stegovideo.tools.HaarDWT.swapWithMax;
import static org.chaosdragon.stegovideo.tools.HaarDWT.swapWithMin;
import static org.chaosdragon.stegovideo.tools.HaarDWT.to1DArray;

/**
 * Not really DTC. Uses Haar Wavelet to do the embedding
 *
 * @author David Griberman
 */
public class HaarEmbedder implements DTCEmbedder {

    private MessageEncoder encoder;
    private OutputStream decoder;
    private int iterations;
    private static final int STEP = 5;

    @Override
    public int[][] embed(int[][] n, int channel) {
        throw new UnsupportedOperationException("Please use the double constructor");
    }

    public int calculateMaxSize(int w, int h) {
        double[][] temp = new double[w][h];
        double[] magic = to1DArray(temp, iterations);

        int channels = 0;

        for (int i = 0; i < 3; i++) {
            if (willHide(i)) {
                channels++;
            }
        }
        return (magic.length / STEP - STEP) * channels;
    }

    //Determine channel to hide into, 0 = Y
    @Override
    public boolean willHide(int channel) {
        return channel == 0;
    }

    @Override
    public void setEncoder(MessageEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void reset() {
        encoder.reset();
    }

    public HaarEmbedder(int iterations) {
        this.iterations = iterations;
    }

    //USE THIS INSTEAD
    public double[][] embed(double[][] n, int ch) {
        if (!willHide(ch)) {
            return n;
        }

        byte emb;
        if (encoder == null) {
            return n; //Throw error instead?
        }

        double[] magic = to1DArray(n, iterations);

        if (magic.length > STEP) {
            for (int i = 0; i < magic.length - STEP; i = i + STEP) {
                emb = encoder.getNextBit();
                if (emb >= 0) {
                    if (emb == 1) {
                        swapWithMax(magic, i);
                    } else {
                        if (emb == 0) {
                            swapWithMin(magic, i);
                        }
                    }
                }
            }

            paste1DArray(n, magic, iterations);

        }

        return n;
    }

    //Use double one
    @Override
    public void decodeBlock(int[][] n, int channel) {
        // NOOP
    }

    //Use this one instead
    public void decode(double[][] n, int channel) throws IOException {

        if (!willHide(channel)) {
            return;
        }

        double[] magic = HaarDWT.to1DArray(n, iterations);

        for (int i = 0; i < magic.length - STEP; i += STEP) {
            if (HaarDWT.largerThanMedian(magic, i)) {
                decoder.write(1);
            } else {
                decoder.write(0);
            }
        }
    }

    @Override
    public void setOutputStream(OutputStream decoder) {
        this.decoder = decoder;
    }

}
