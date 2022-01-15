package org.chaosdragon.stegovideo.algorithms;

import org.chaosdragon.stegovideo.tools.AdaptiveBox;
import org.chaosdragon.stegovideo.tools.DCTTools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A DTC embedding algorithm skeleton. Does all the transformations, no
 * embedding. Override hide methods while extending for embedding.
 *
 * @author David Griberman
 */
public abstract class DCTEmbeddingAlgorithm extends EmbeddingAlgorithm {

    private final int blockSize;
    private final DCTTools d;

    @Override
    public void setStrengthMatrix(int[][] matr, AdaptiveBox box) {
        strMatrix = matr;
        normalizedStrMatrix = box.normalizeMatrix(matr, blockSize);
    }

    // HOOKS for embedding algorithms
    protected abstract int[][] hideBeforeQuantisation(int[][] data, int channel) throws IOException;
    protected abstract int[][] hideAfterQuantisation(int[][] data, int channel) throws IOException;
    protected abstract int[][] hideAfterQuantisation(int[][] data, int channel, int[][] str) throws IOException;
    protected abstract void extractBeforeQuantisation(int[][] data, int channel) throws IOException;
    protected abstract void extractAfterQuantisation(int[][] data, int channel) throws IOException;

    protected DCTEmbeddingAlgorithm(int blockSize, int compressionLevel) {
        this.blockSize = blockSize;
        d = new DCTTools(compressionLevel);
    }

    protected int[][][] convertToYCbCrMatrix(int[][][] rgb) { //Channel, x, y        
        int[][] _y2 = new int[blockSize][blockSize];
        int[][] _cb2 = new int[blockSize][blockSize];
        int[][] _cr2 = new int[blockSize][blockSize];

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                double y = (0.299 * rgb[0][i][j] + 0.587 * rgb[1][i][j] + 0.114 * rgb[2][i][j]);
                double cb = (128 + -0.16874 * rgb[0][i][j] - 0.33126 * rgb[1][i][j] + 0.50000 * rgb[2][i][j]);
                double cr = (128 + 0.50000 * rgb[0][i][j] - 0.41869 * rgb[1][i][j] - 0.08131 * rgb[2][i][j]);
                _y2[i][j] = (int) y;
                _cb2[i][j] = (int) cb;
                _cr2[i][j] = (int) cr;
            }
        }
        return new int[][][]{_y2, _cb2, _cr2};
    }

    protected int[][][] converToRGBMatrix(BufferedImage img, int offsetX, int offsetY) {

        Color c;
        int[][] r = new int[blockSize][blockSize];
        int[][] g = new int[blockSize][blockSize];
        int[][] b = new int[blockSize][blockSize];

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                try {
                    c = new Color(img.getRGB(offsetX + i, offsetY + j));
                } catch (Exception e) {
                    c = new Color(0, 0, 0);
                }
                r[i][j] = c.getRed();
                g[i][j] = c.getGreen();
                b[i][j] = c.getBlue();
            }
        }

        return new int[][][]{r, g, b};
    }

    protected int[][][] convertToRGBMatrix(int[][][] ycbr) {

        int[][] R = new int[ycbr[0].length][ycbr[0][0].length];
        int[][] G = new int[ycbr[1].length][ycbr[1][0].length];
        int[][] B = new int[ycbr[2].length][ycbr[2][0].length];

        for (int i = 0; i < ycbr[0].length; i++) {
            for (int j = 0; j < ycbr[0][0].length; j++) {

                int r = (int) (ycbr[0][i][j] + 1.402 * (ycbr[2][i][j] - 128));
                int g = (int) (ycbr[0][i][j] - 0.34414 * (ycbr[1][i][j] - 128) - 0.71414 * (ycbr[2][i][j] - 128));
                int b = (int) (ycbr[0][i][j] + 1.772 * (ycbr[1][i][j] - 128));

                //FIX for overflow
                if (r > 255) {
                    r = 255;
                }
                if (r < 0) {
                    r = 0;
                }
                if (b > 255) {
                    b = 255;
                }
                if (b < 0) {
                    b = 0;
                }
                if (g > 255) {
                    g = 255;
                }
                if (g < 0) {
                    g = 0;
                }

                //Setting the big array
                R[i][j] = r;
                B[i][j] = b;
                G[i][j] = g;
            }
        }

        return new int[][][]{R, G, B};
    }

    protected void setRGBBlock(int[][][] rgb, BufferedImage img,
            int offsetX, int offsetY) {

        for (int i = 0; i < rgb[0].length; i++) {
            for (int j = 0; j < rgb[0][0].length; j++) {

                //Could crash? over 255, lower than 0?
                Color temp = new Color(rgb[0][i][j], rgb[1][i][j], rgb[2][i][j]);

                if (offsetX + i < img.getWidth() && offsetY + j < img.getHeight()) {
                    img.setRGB(offsetX + i, offsetY + j, temp.getRGB());
                }
            }
        }

    }

    protected int[][] forwardDCT(int[][] data) {
        return d.forwardDCT(data);
    }

    protected int[][] inverseDCT(int[][] data) {
        return d.inverseDCT(data);
    }

    protected int[][] quantitizeImage(int[][] data) {
        return d.quantitizeImage(data, blockSize <= 8);
    }

    protected int[][] dequantitizeImage(int[][] data) {
        return d.dequantitizeImage(data, blockSize <= 8);
    }

    @Override
    public BufferedImage embed(BufferedImage img) throws IOException {

        //For each block of size BLOCKSIZE
        for (int offsetX = 0; offsetX < img.getWidth(); offsetX += blockSize) {
            for (int offsetY = 0; offsetY < img.getHeight(); offsetY += blockSize) {

                //Convert to RGB Matrix
                int[][][] rgb = converToRGBMatrix(img, offsetX, offsetY);
                //Conver to YCBR matrix
                int[][][] ycbr = convertToYCbCrMatrix(rgb); //Return the same to disable

                //For channel storage
                ArrayList<int[][]> channels = new ArrayList<>();

                //Run per channel of the block
                int ch = 0;
                for (int[][] channel : ycbr) {
                    int[][] p = forwardDCT(channel);

                    if (strMatrix == null) {
                        p = hideBeforeQuantisation(p, ch); //Hook
                    }

                    int[][] n = quantitizeImage(p);
                    if (strMatrix == null) {
                        n = hideAfterQuantisation(n, ch); //Hook
                    } else {
                        int[][] str = {{normalizedStrMatrix[offsetX / blockSize][offsetY / blockSize]}};
                        n = hideAfterQuantisation(n, ch, str);
                    }

                    int[][] f = dequantitizeImage(n); //Optimisation
                    int[][] f2 = inverseDCT(f);
                    channels.add(f2);
                    ch++;
                }

                int[][][] resulting = {channels.get(0), channels.get(1), channels.get(2)};
                resulting = convertToRGBMatrix(resulting);
                //Set data of this block to the image
                setRGBBlock(resulting, img, offsetX, offsetY);
            }
        }

        return img;
    }

    @Override
    public void extract(BufferedImage img) throws IOException {
        //For each block of size BLOCKSIZE
        for (int offsetX = 0; offsetX < img.getWidth(); offsetX += blockSize) {
            for (int offsetY = 0; offsetY < img.getHeight(); offsetY += blockSize) {

                //Convert to RGB Matrix
                int[][][] rgb = converToRGBMatrix(img, offsetX, offsetY);
                //Conver to YCBR matrix
                int[][][] ycbr = convertToYCbCrMatrix(rgb); //Return the same to disable

                //Run per channel of the block
                int ch = 0;
                for (int[][] channel : ycbr) {
                    int[][] p = forwardDCT(channel);
                    extractBeforeQuantisation(p, ch);
                    int[][] n = quantitizeImage(p); //Optimisation
                    extractAfterQuantisation(n, ch);
                    ch++;
                }
            }
        }
    }

    @Override
    public int calculateMaxSize(int w, int h) {
        return 0;
    }

}
