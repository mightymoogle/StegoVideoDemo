package org.chaosdragon.stegovideo.algorithms;

import org.chaosdragon.stegovideo.embedders.DTCEmbedder;
import org.chaosdragon.stegovideo.encoders.MessageEncoder;
import org.chaosdragon.stegovideo.tools.AdaptiveBox;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Defines a superclass of a general embedding algorithm
 *
 * @author David Griberman
 */
public abstract class EmbeddingAlgorithm {

    protected DTCEmbedder dtc; //Make more abstract later
    protected int[][] strMatrix;
    protected int[][] normalizedStrMatrix;

    public void setOutputStream(OutputStream s) {
        dtc.setOutputStream(s);
    }

    public void setEncoder(MessageEncoder encoder) {
        dtc.setEncoder(encoder);
    }

    public void setStrengthMatrix(int[][] matr, AdaptiveBox box) {
        strMatrix = matr;
    }

    public void reset() {
        dtc.reset();
    }

    public boolean supportsAdaptive() {
        return false;
    }

    public abstract int calculateMaxSize(int w, int h);

    public abstract BufferedImage embed(BufferedImage img) throws IOException;

    public abstract void extract(BufferedImage img) throws IOException;

}
