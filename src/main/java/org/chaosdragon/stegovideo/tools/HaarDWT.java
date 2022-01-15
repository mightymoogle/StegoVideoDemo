package org.chaosdragon.stegovideo.tools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;


/**
 * Based on
 * http://www.codeproject.com/Articles/683663/Discrete-Haar-Wavelet-Transformation
 * https://github.com/primaryobjects/Accord.NET/blob/master/Sources/Accord.Math/Wavelets/Haar.cs
 *
 * @author David Griberman
 */
public class HaarDWT {

    private static final int STEP = 5;

    private HaarDWT() {
        // Prevent initialization
    }

    private static final float w0 = 0.5f;
    private static final float w1 = -0.5f;
    private static final float s0 = 0.5f;
    private static final float s1 = 0.5f;

    public static void IWT(double[][] data, int iterations) {
        int rows = data.length;
        int cols = data[0].length;

        double[] col = new double[rows];
        double[] row = new double[cols];

        for (int l = 0; l < iterations; l++) {
            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < col.length; i++) { //BUG HERE!
                    col[i] = data[i][j];
                }

                IWT(col);

                for (int i = 0; i < col.length; i++) {
                    data[i][j] = col[i];
                }
            }

            for (int i = 0; i < rows; i++) {
                System.arraycopy(data[i], 0, row, 0, row.length);
                IWT(row);
                System.arraycopy(row, 0, data[i], 0, row.length);
            }
        }
    }

    public static void IWT(double[] data) {
        double[] temp = new double[data.length];

        int h = data.length >> 1;
        for (int i = 0; i < h; i++) {
            int k = (i << 1);
            temp[k] = (data[i] * s0 + data[i + h] * w0) / w0;
            temp[k + 1] = (data[i] * s1 + data[i + h] * w1) / s0;
        }

        for (int i = 0; i < data.length; i++) {
            data[i] = temp[i];
        }
    }

    public static void FWT(double[][] data, int iterations) {
        int rows = data.length;
        int cols = data[0].length;

        double[] row = new double[cols];
        double[] col = new double[rows];

        for (int k = 0; k < iterations; k++) {
            for (int i = 0; i < rows; i++) {
                System.arraycopy(data[i], 0, row, 0, row.length);
                FWT(row);
                System.arraycopy(row, 0, data[i], 0, row.length);
            }

            for (int j = 0; j < cols; j++) {
                for (int i = 0; i < col.length; i++) {
                    col[i] = data[i][j];
                }

                FWT(col);

                for (int i = 0; i < col.length; i++) {
                    data[i][j] = col[i];
                }
            }
        }
    }

    public static void FWT(double[] data) {
        double[] temp = new double[data.length];

        int h = data.length >> 1;
        for (int i = 0; i < h; i++) {
            int k = (i << 1);
            temp[i] = data[k] * s0 + data[k + 1] * s1;
            temp[i + h] = data[k] * w0 + data[k + 1] * w1;
        }

        System.arraycopy(temp, 0, data, 0, data.length);
    }
  
    //WAS INT BEFORE, SO MUCH RAM WASTED
    public static double[][][] converToRGBMatrix(BufferedImage img) {

        int w = img.getWidth();
        int h = img.getHeight();

        Color c;
        double[][] r = new double[w][h];
        double[][] g = new double[w][h];
        double[][] b = new double[w][h];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                try {
                    c = new Color(img.getRGB(i, j));
                } catch (Exception e) {
                    c = new Color(0, 0, 0);
                }
                r[i][j] = (int) c.getRed();
                g[i][j] = (int) c.getGreen();
                b[i][j] = (int) c.getBlue();
            }
        }

        return new double[][][]{r, g, b};
    }

    public static boolean largerThanMedian(double[] data, int m) {

        ArrayList<Double> temp = new ArrayList<>();

        for (int i = m; i < m+STEP; i++) {
            temp.add(data[i]);
        }

        Collections.sort(temp);

        return data[m] > temp.get(3);
    }

    public static void setRGBBlock(double[][][] rgb, BufferedImage img) {

        for (int i = 0; i < rgb[0].length; i++) {
            for (int j = 0; j < rgb[0][0].length; j++) {

                int r = (int) rgb[0][i][j];
                int g = (int) rgb[1][i][j];
                int b = (int) rgb[2][i][j];

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

                Color temp = new Color(r, g, b);

                if (i < img.getWidth() && j < img.getHeight()) {
                    img.setRGB(i, j, temp.getRGB());
                }
            }
        }

    }

    public static boolean isLHHLBand(int i, int j, int w, int h, int iterations) {
        for (int k = 0; k < iterations; k++) {
            //do first iteration manually here
            if (i >= 0 && i < w && j >= 0 && j < h && k == 0) {
                return false;
            }

            if (i >= w && i < (w * 2)) {
                if (j >= h && j < (h * 2)) { //FIX with H
                    return false;
                }
            }

            w = w * 2;
            h = h * 2;
        }

        return true;
    }

     public static boolean isOK(int i, int j, int w, int h, int iterations) {
         return isLHHLBand(i,j,w,h,iterations);
     }

    //FAIL WITH HH1, HH2, HH3
    public static double[] to1DArray(double[][] data, int iterations) {
        int w = (int) (data.length / (Math.pow(2, iterations)));
        int h = (int) (data[0].length / (Math.pow(2, iterations)));

        ArrayList<Double> dd = new ArrayList<>();

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (isOK(i, j, w, h, iterations)) {
                    dd.add(data[i][j]);
                }
            }
        }

        double[] arr = new double[dd.size()];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = dd.get(i);
        }

        return arr;
    }

    public static void paste1DArray(double[][] data, double[] copy, int iterations) {
        int flow = 0;

        int w = (int) (data.length / (Math.pow(2, iterations)));
        int h = (int) (data[0].length / (Math.pow(2, iterations)));      

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (isOK(i, j, w, h, iterations)) {
                    data[i][j] = copy[flow++];
                }
            }
        }
    }

    public static void swapWithMax(double[] magic, int i) {
        double max = magic[i];
        int maxID = i;
        for (int j = i + 1; j < i + STEP; j++) {
            if (max < magic[j]) {
                max = magic[j];
                maxID = j;
            }
        }

        //SWAP WITH MAX IF 1
        double temp = magic[i];
        magic[i] = magic[maxID];
        magic[maxID] = temp;
    }

    public static void swapWithMin(double[] magic, int i) {
        double min = magic[i];
        int minID = i;
        for (int j = i + 1; j < i + STEP; j++) {
            if (min > magic[j]) {
                min = magic[j];
                minID = j;
            }
        }

        //SWAP WITH MAX IF 1
        double temp = magic[i];
        magic[i] = magic[minID];
        magic[minID] = temp;
    }
    
    public static double[][][] convertToYCbCrMatrix(double[][][] rgb) { //Channel, x, y
        int w = rgb[0].length;
        int h = rgb[0][0].length;

        double[][] _y2 = new double[w][h];
        double[][] _cb2 = new double[w][h];
        double[][] _cr2 = new double[w][h];

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                double y = (0.299 * rgb[0][i][j] + 0.587 * rgb[1][i][j] + 0.114 * rgb[2][i][j]);
                double cb = (128 + -0.16874 * rgb[0][i][j] - 0.33126 * rgb[1][i][j] + 0.50000 * rgb[2][i][j]);
                double cr = (128 + 0.50000 * rgb[0][i][j] - 0.41869 * rgb[1][i][j] - 0.08131 * rgb[2][i][j]);
                _y2[i][j] = y;
                _cb2[i][j] = cb;
                _cr2[i][j] = cr;
            }
        }
        return new double[][][]{_y2, _cb2, _cr2};
    }

    public static double[][][] convertToRGBMatrix(double[][][] ycbr) {
        double[][] R = new double[ycbr[0].length][ycbr[0][0].length];
        double[][] G = new double[ycbr[1].length][ycbr[1][0].length];
        double[][] B = new double[ycbr[2].length][ycbr[2][0].length];

        for (int i = 0; i < ycbr[0].length; i++) {
            for (int j = 0; j < ycbr[0][0].length; j++) {

                double r = ycbr[0][i][j] + 1.402 * (ycbr[2][i][j] - 128);
                double g = ycbr[0][i][j] - 0.34414 * (ycbr[1][i][j] - 128) - 0.71414 * (ycbr[2][i][j] - 128);
                double b = ycbr[0][i][j] + 1.772 * (ycbr[1][i][j] - 128);

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

        return new double[][][]{R, G, B};
    }
}
