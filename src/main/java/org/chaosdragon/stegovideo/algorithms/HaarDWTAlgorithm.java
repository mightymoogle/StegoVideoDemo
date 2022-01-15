package org.chaosdragon.stegovideo.algorithms;

import org.chaosdragon.stegovideo.embedders.HaarEmbedder;
import org.chaosdragon.stegovideo.tools.HaarDWT;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Implements the Haar DWT algorithm (Barely)
 *
 * @author David Griberman
 */
public class HaarDWTAlgorithm extends EmbeddingAlgorithm {

    private final int iterations;

    public HaarDWTAlgorithm(int iterations) {
        dtc = new HaarEmbedder(iterations);
        this.iterations = iterations;
    }

    protected double[][] hideDWT(double[][] data, int ch) {
        return ((HaarEmbedder) dtc).embed(data, ch);
    }

    @Override
    public int calculateMaxSize(int w, int h) {
        return ((HaarEmbedder) dtc).calculateMaxSize(w, h);
    }

    public BufferedImage embed(BufferedImage img) throws IOException {

        //Convert to RGB Matrix
        double[][][] rgb = HaarDWT.converToRGBMatrix(img);
        //Conver to YCBR matrix
        double[][][] ycbr = HaarDWT.convertToYCbCrMatrix(rgb); //Return the same to disable

        //For channel storage
        ArrayList<double[][]> channels = new ArrayList<>();

        //Run per channel of the block
        int ch = 0;
        for (int i = 0; i < ycbr.length; i++) {
            double[][] p = ycbr[i];
            HaarDWT.FWT(p, iterations);
            p = hideDWT(p, ch);
            HaarDWT.IWT(p, iterations);
            channels.add(p);
            ch++;
        }

        double[][][] resulting = {channels.get(0), channels.get(1), channels.get(2)};
        resulting = HaarDWT.convertToRGBMatrix(resulting);
        //Set data of this block to the image
        HaarDWT.setRGBBlock(resulting, img);
        return img;
    }

    public void extractDWT(double[][] data, int ch) throws IOException {
        ((HaarEmbedder) dtc).decode(data, ch);
    }

    @Override
    public void extract(BufferedImage img) throws IOException {

        //Convert to RGB Matrix
        double[][][] rgb = HaarDWT.converToRGBMatrix(img);
        //Convert to YCBR matrix
        double[][][] ycbr = HaarDWT.convertToYCbCrMatrix(rgb); //Return the same to disable

        //Run per channel of the block
        int ch = 0;
        for (double[][] channel : ycbr) {
            double[][] p = channel;
            HaarDWT.FWT(p, iterations);
            extractDWT(p, ch);
            ch++;
        }
    }
}
