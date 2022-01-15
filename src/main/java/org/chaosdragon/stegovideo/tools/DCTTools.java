package org.chaosdragon.stegovideo.tools;

/**
 * Based on
 * https://github.com/rkjc/jpgComponentTest/blob/master/src/DCT.java
 *
 * @author David Griberman
 */
public class DCTTools {

    /**
     * DCT Block Size - default 8
     */
    public int N = 8;

    /**
     * The ZigZag matrix.
     */
    public int[][] zigZag = new int[64][2];

    /**
     * Cosine matrix. N * N.
     */
    public double[][] c = new double[N][N];

    /**
     * Transformed cosine matrix, N*N.
     */
    public double[][] cT = new double[N][N];

    /**
     * Quantitization Matrix.
     */
    public int[][] quantum = new int[N][N];

    /**
     * Constructs a new DCT object. Initializes the cosine transform matrix
     * these are used when computing the DCT and it's inverse. This also
     * initializes the run length counters and the ZigZag sequence. Note that
     * the image quality can be worse than 25 however the image will be extemely
     * pixelated, usually to a block size of N.
     *
     * @param QUALITY The quality of the image (0 best - 25 worst)
     *
     */
    public DCTTools(int QUALITY) {
        initZigZag();
        initMatrix(QUALITY);
    }

    /**
     * This method sets up the quantization matrix using the Quality parameter
     * and then sets up the Cosine Transform Matrix and the Transposed CT. These
     * are used by the forward and inverse DCT. The RLE encoding variables are
     * set up to track the number of consecutive zero values that have output or
     * will be input.
     *
     * @param quality The quality scaling factor
     */
    private void initMatrix(int quality) {
        int i;
        int j;

        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) {
                quantum[i][j] = (1 + ((1 + i + j) * quality));
            }
        }

        for (j = 0; j < N; j++) {
            double nn = N;
            c[0][j] = 1.0 / Math.sqrt(nn);
            cT[j][0] = c[0][j];
        }

        for (i = 1; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                c[i][j] = Math.sqrt(2.0 / 8.0) * Math.cos(((2.0 * (double) j + 1.0) * (double) i * Math.PI) / (2.0 * 8.0));
                cT[j][i] = c[i][j];
            }
        }
    }

    /**
     * Initializes the ZigZag matrix.
     */
    private void initZigZag() {
        zigZag[0][0] = 0; // 0,0
        zigZag[0][1] = 0;
        zigZag[1][0] = 0; // 0,1
        zigZag[1][1] = 1;
        zigZag[2][0] = 1; // 1,0
        zigZag[2][1] = 0;
        zigZag[3][0] = 2; // 2,0
        zigZag[3][1] = 0;
        zigZag[4][0] = 1; // 1,1
        zigZag[4][1] = 1;
        zigZag[5][0] = 0; // 0,2
        zigZag[5][1] = 2;
        zigZag[6][0] = 0; // 0,3
        zigZag[6][1] = 3;
        zigZag[7][0] = 1; // 1,2
        zigZag[7][1] = 2;
        zigZag[8][0] = 2; // 2,1
        zigZag[8][1] = 1;
        zigZag[9][0] = 3; // 3,0
        zigZag[9][1] = 0;
        zigZag[10][0] = 4; // 4,0
        zigZag[10][1] = 0;
        zigZag[11][0] = 3; // 3,1
        zigZag[11][1] = 1;
        zigZag[12][0] = 2; // 2,2
        zigZag[12][1] = 2;
        zigZag[13][0] = 1; // 1,3
        zigZag[13][1] = 3;
        zigZag[14][0] = 0; // 0,4
        zigZag[14][1] = 4;
        zigZag[15][0] = 0; // 0,5
        zigZag[15][1] = 5;
        zigZag[16][0] = 1; // 1,4
        zigZag[16][1] = 4;
        zigZag[17][0] = 2; // 2,3
        zigZag[17][1] = 3;
        zigZag[18][0] = 3; // 3,2
        zigZag[18][1] = 2;
        zigZag[19][0] = 4; // 4,1
        zigZag[19][1] = 1;
        zigZag[20][0] = 5; // 5,0
        zigZag[20][1] = 0;
        zigZag[21][0] = 6; // 6,0
        zigZag[21][1] = 0;
        zigZag[22][0] = 5; // 5,1
        zigZag[22][1] = 1;
        zigZag[23][0] = 4; // 4,2
        zigZag[23][1] = 2;
        zigZag[24][0] = 3; // 3,3
        zigZag[24][1] = 3;
        zigZag[25][0] = 2; // 2,4
        zigZag[25][1] = 4;
        zigZag[26][0] = 1; // 1,5
        zigZag[26][1] = 5;
        zigZag[27][0] = 0; // 0,6
        zigZag[27][1] = 6;
        zigZag[28][0] = 0; // 0,7
        zigZag[28][1] = 7;
        zigZag[29][0] = 1; // 1,6
        zigZag[29][1] = 6;
        zigZag[30][0] = 2; // 2,5
        zigZag[30][1] = 5;
        zigZag[31][0] = 3; // 3,4
        zigZag[31][1] = 4;
        zigZag[32][0] = 4; // 4,3
        zigZag[32][1] = 3;
        zigZag[33][0] = 5; // 5,2
        zigZag[33][1] = 2;
        zigZag[34][0] = 6; // 6,1
        zigZag[34][1] = 1;
        zigZag[35][0] = 7; // 7,0
        zigZag[35][1] = 0;
        zigZag[36][0] = 7; // 7,1
        zigZag[36][1] = 1;
        zigZag[37][0] = 6; // 6,2
        zigZag[37][1] = 2;
        zigZag[38][0] = 5; // 5,3
        zigZag[38][1] = 3;
        zigZag[39][0] = 4; // 4,4
        zigZag[39][1] = 4;
        zigZag[40][0] = 3; // 3,5
        zigZag[40][1] = 5;
        zigZag[41][0] = 2; // 2,6
        zigZag[41][1] = 6;
        zigZag[42][0] = 1; // 1,7
        zigZag[42][1] = 7;
        zigZag[43][0] = 2; // 2,7
        zigZag[43][1] = 7;
        zigZag[44][0] = 3; // 3,6
        zigZag[44][1] = 6;
        zigZag[45][0] = 4; // 4,5
        zigZag[45][1] = 5;
        zigZag[46][0] = 5; // 5,4
        zigZag[46][1] = 4;
        zigZag[47][0] = 6; // 6,3
        zigZag[47][1] = 3;
        zigZag[48][0] = 7; // 7,2
        zigZag[48][1] = 2;
        zigZag[49][0] = 7; // 7,3
        zigZag[49][1] = 3;
        zigZag[50][0] = 6; // 6,4
        zigZag[50][1] = 4;
        zigZag[51][0] = 5; // 5,5
        zigZag[51][1] = 5;
        zigZag[52][0] = 4; // 4,6
        zigZag[52][1] = 6;
        zigZag[53][0] = 3; // 3,7
        zigZag[53][1] = 7;
        zigZag[54][0] = 4; // 4,7
        zigZag[54][1] = 7;
        zigZag[55][0] = 5; // 5,6
        zigZag[55][1] = 6;
        zigZag[56][0] = 6; // 6,5
        zigZag[56][1] = 5;
        zigZag[57][0] = 7; // 7,4
        zigZag[57][1] = 4;
        zigZag[58][0] = 7; // 7,5
        zigZag[58][1] = 5;
        zigZag[59][0] = 6; // 6,6
        zigZag[59][1] = 6;
        zigZag[60][0] = 5; // 5,7
        zigZag[60][1] = 7;
        zigZag[61][0] = 6; // 6,7
        zigZag[61][1] = 7;
        zigZag[62][0] = 7; // 7,6
        zigZag[62][1] = 6;
        zigZag[63][0] = 7; // 7,7
        zigZag[63][1] = 7;
    }

    /**
     * This method preforms a matrix multiplication of the input pixel data
     * matrix by the transposed cosine matrix and store the result in a
     * temporary N * N matrix. This N * N matrix is then multiplied by the
     * cosine matrix and the result is stored in the output matrix.
     *
     * @param input The Input Pixel Matrix
     * @returns output The DCT Result Matrix
     */
    public int[][] forwardDCT(int[][] input) {
        int[][] output = new int[N][N];
        double[][] temp = new double[N][N];
        double temp1;
        int i;
        int j;
        int k;

        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) {
                temp[i][j] = 0.0;
                for (k = 0; k < N; k++) {
                    temp[i][j] += ((input[i][k] - 128) * cT[k][j]);
                }
            }
        }

        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) {
                temp1 = 0.0;

                for (k = 0; k < N; k++) {
                    temp1 += (c[i][k] * temp[k][j]);
                }

                output[i][j] = (int) Math.round(temp1);
            }
        }

        return output;
    }

    /**
     * This method reads in DCT codes dequanitizes them and places them in the
     * correct location. The codes are stored in the zigzag format so they need
     * to be redirected to a N * N block through simple table lookup. After
     * dequantitization the data needs to be run through an inverse DCT.
     *
     * @param inputData 8x8 Array of quantitized image data
     * @param zigzag Boolean switch to enable/disable zigzag path.
     * @returns outputData A N * N array of de-quantitized data
     *
     */
    public int[][] dequantitizeImage(int[][] inputData, boolean zigzag) {
        int i;
        int j;
        int row;
        int col;
        int[][] outputData = new int[N][N];

        double result;

        if (zigzag) {
            for (i = 0; i < (N * N); i++) {
                row = zigZag[i][0];
                col = zigZag[i][1];

                result = inputData[row][col] * quantum[row][col];
                outputData[row][col] = (int) (Math.round(result));
            }
        } else {
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    result = inputData[i][j] * quantum[i][j];
                    outputData[i][j] = (int) (Math.round(result));
                }
            }
        }

        return outputData;
    }

    /**
     * This method orders the DCT result matrix into a zigzag pattern and then
     * quantitizes the data. The quantitized value is rounded to the nearest
     * integer. Pixels which round or divide to zero are the loss associated
     * with quantitizing the image. These pixels do not display in the AWT.
     * (null) Long runs of zeros and the small ints produced through this
     * technique are responsible for the small image sizes. For block sizes < or
     * > 8, disable the zigzag optimization. If zigzag is disabled on encode it
     * must be disabled on decode as well.
     *
     * @param inputData 8x8 array of DCT image data.
     * @param zigzag Boolean switch to enable/disable zigzag path.
     * @returns outputData The quantitized output data
     */
    public int[][] quantitizeImage(int[][] inputData, boolean zigzag) {
        int[][] outputData = new int[N][N];
        int i;
        int j;
        int row;
        int col;

        double result;

        if (zigzag) {
            for (i = 0; i < (N * N); i++) {
                row = zigZag[i][0];
                col = zigZag[i][1];
                result = (inputData[row][col] / quantum[row][col]);
                outputData[row][col] = (int) (Math.round(result));
            }

        } else {
            for (i = 0; i < N; i++) {
                for (j = 0; j < N; j++) {
                    result = inputData[i][j] / quantum[i][j];
                    outputData[i][j] = (int) (Math.round(result));
                }
            }
        }

        return outputData;
    }

    /**
     * This method is preformed using the reverse of the operations preformed in
     * the DCT. This restores a N * N input block to the corresponding output
     * block with values scaled to 0 to 255 and then stored in the input block
     * of pixels.
     *
     * @param input N * N input block
     * @returns output The pixel array output
     */
    public int[][] inverseDCT(int[][] input) {
        int[][] output = new int[N][N];
        double[][] temp = new double[N][N];
        double temp1;
        int i;
        int j;
        int k;

        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) {
                temp[i][j] = 0.0;

                for (k = 0; k < N; k++) {
                    temp[i][j] += input[i][k] * c[k][j];
                }
            }
        }

        for (i = 0; i < N; i++) {
            for (j = 0; j < N; j++) {
                temp1 = 0.0;

                for (k = 0; k < N; k++) {
                    temp1 += cT[i][k] * temp[k][j];
                }

                temp1 += 128.0;

                if (temp1 < 0) {
                    output[i][j] = 0;
                } else if (temp1 > 255) {
                    output[i][j] = 255;
                } else {
                    output[i][j] = (int) Math.round(temp1);
                }
            }
        }

        return output;
    }
}