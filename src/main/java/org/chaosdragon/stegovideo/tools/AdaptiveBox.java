package org.chaosdragon.stegovideo.tools;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * A class that calculates the movement between different frames, also
 * calculates the difference matrix
 *
 * @author David Griberman
 */
public class AdaptiveBox {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdaptiveBox.class);

    private final int[][] strengthMatrix;
    private final ArrayList<BufferedImage> pictures;
    private final ArrayList<int[][][]> matrices;
    private final ArrayList<Long> timeStamps;

    public static final int FRAME_BUFFER_SIZE = 6; //Number of frames to use
    public static final int SENSITIVITY = 40; //Minimum difference for drawing    
    public static final int THRESHOLD = SENSITIVITY / 2; //
    boolean[] channels = {false, false, false};
    boolean yCbCr = false;
    private static final int FRAMES_TO_SKIP = 45; //Number of frames to skip if monotone
    private int framesLeftToSkip = 0; //Number of frames left to skip

    /**
     * Creates an adaptive box with the given size
     *
     * @param x frame width
     * @param y frame height
     */
    public AdaptiveBox(int x, int y) {
        strengthMatrix = new int[x][y];
        pictures = new ArrayList<>();
        timeStamps = new ArrayList<>();
        matrices = new ArrayList<>();
    }

    /**
     * Create a Adaptive box with the first frame
     *
     * @param first first frame
     * @param time  timestamp
     */
    public AdaptiveBox(BufferedImage first, long time, boolean yCbCr) {
        this(first.getWidth(), first.getHeight());
        setyCbCr(yCbCr);
        addPicture(first);
        timeStamps.add(time);
    }

    /**
     * Sets the YCbCr mode. If false, RGB mode is used instead
     *
     * @param on YCbCr mode status
     */
    public void setyCbCr(boolean on) {
        yCbCr = on;
    }

    /**
     * Disables all channels for calculations
     */
    public void disableChannels() {
        channels[0] = false;
        channels[1] = false;
        channels[2] = false;
    }

    /**
     * Enables a channel for calculations
     *
     * @param c channel number (0-2)
     */
    public void enableChannel(int c) {
        channels[c] = true;
    }

    /**
     * Add a BufferedImage to the input data, a helper method
     *
     * @param img Frame to add
     */
    protected void addPicture(BufferedImage img) {
        int[][][] n = converToRGBMatrix(img);

        if (yCbCr) {
            n = convertToYCbCrMatrix(n);
        }

        if (isMonotone(n)) {
            framesLeftToSkip = FRAMES_TO_SKIP;
            log.info("Monotone frame detected");
        }

        matrices.add(n);
        pictures.add(img);
    }

    public boolean isMonotone(int[][][] rgb) {

        int r = rgb[0][0][0];

        int t = 10;

        for (int i = 0; i < rgb[0].length; i += 8) {
            for (int j = 0; j < rgb[0][0].length; j += 8) {
                if (Math.abs(r - rgb[0][i][j]) > t) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Add a picture with it's timestamp to the data
     *
     * @param first picture
     * @param time  timestamp
     */
    public void add(BufferedImage first, long time) {
        addPicture(first);
        timeStamps.add(time);
    }

    /**
     * @return next frame from the buffer
     */
    public BufferedImage getNext() {
        return pictures.get(0);
    }

    /**
     * @return next timestamp from the buffer
     */
    public long getNextStamp() {
        return timeStamps.get(0);
    }

    /**
     * Calculates the strength matrix based on the movement in the frames in the
     * buffer
     *
     * @param remove whenever to remove the first frame. If False will shift it
     *               to the last position
     * @return
     */
    public int[][] calculateStrengthMatrix(boolean remove) {

        //Empty the matrix
        for (int i = 0; i < strengthMatrix.length; i++) {
            for (int j = 0; j < strengthMatrix[0].length; j++) {
                strengthMatrix[i][j] = 0;
            }
        }

        for (int i = 0; i < strengthMatrix.length; i++) {
            for (int j = 0; j < strengthMatrix[0].length; j++) {

                int[] difference = new int[3];

                for (int k = 1; k < pictures.size(); k++) {

                    if (channels[0]) {
                        difference[0] += Math.abs(matrices.get(0)[0][i][j] - matrices.get(k)[0][i][j]);
                    }
                    if (channels[1]) {
                        difference[1] += Math.abs(matrices.get(0)[1][i][j] - matrices.get(k)[1][i][j]);
                    }
                    if (channels[2]) {
                        difference[2] += Math.abs(matrices.get(0)[2][i][j] - matrices.get(k)[2][i][j]);
                    }
                }

                strengthMatrix[i][j] = (difference[0] + difference[1] + difference[2]);

            }
        }

        if (remove) {
            pictures.remove(0);
            timeStamps.remove(0);
            matrices.remove(0);
        } else {
            BufferedImage temp = pictures.get(0);
            int[][][] tt = matrices.get(0);
            Long t2 = timeStamps.get(0);
            pictures.remove(0);
            timeStamps.remove(0);
            matrices.remove(0);
            pictures.add(temp);
            timeStamps.add(t2);
            matrices.add(tt);
        }
        return strengthMatrix;
    }

    private static boolean hasZeroNeighbour(int[][] n, int x, int y) {
        for (int i = 0; i < n.length; i++) {
            for (int j = 0; j < n[0].length; j++) {
                if (Math.abs(i - x) == 1 || Math.abs(j - y) == 1) {
                    if (Math.abs(i - x) <= 1 && Math.abs(j - y) <= 1) {
                        if (n[i][j] == 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Normalizes the strength matrix for embedding
     *
     * @param n         strengthmatrix
     * @param blockSize DTC block size
     * @return normalized matrix
     */
    public int[][] normalizeMatrix(int[][] n, int blockSize) {
        int[][] normalized = new int[n.length / blockSize][n[0].length / blockSize];
        double coef = 1;
        //If need to skip frames
        if (framesLeftToSkip > 0) {
            coef /= framesLeftToSkip;
            framesLeftToSkip--;
        }

        for (int l = 0; l < n.length; l = l + blockSize) {
            for (int k = 0; k < n[0].length; k = k + blockSize) {
                int average = 0;
                for (int i = 0; i < blockSize; i++) {
                    for (int j = 0; j < blockSize; j++) {
                        average += n[l + i][k + j];
                    }
                }
                average = average / (blockSize * blockSize);
                if (average * coef >= SENSITIVITY) {
                    normalized[l / blockSize][k / blockSize] = SENSITIVITY;
                }
            }
        }

        for (int i = 0; i < normalized.length; i++) {
            for (int j = 0; j < normalized[0].length; j++) {
                if ((normalized[i][j] == SENSITIVITY)
                        && (hasZeroNeighbour(normalized, i, j))) {
                    normalized[i][j] = THRESHOLD;
                }
            }
        }
        return normalized;
    }

    /**
     * @return is the buffer full
     */
    public boolean isFull() {
        return pictures.size() >= FRAME_BUFFER_SIZE;
    }

    /**
     * Converst a image to color matrix
     *
     * @param img the input imae
     * @return color matrix [Channel-x-y]
     */
    protected int[][][] converToRGBMatrix(BufferedImage img) {

        Color c;
        int[][] r = new int[img.getWidth()][img.getHeight()];
        int[][] g = new int[img.getWidth()][img.getHeight()];
        int[][] b = new int[img.getWidth()][img.getHeight()];

        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {
                try {
                    c = new Color(img.getRGB(i, j));
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

    /**
     * Converts colour matrix from RGB to YCBR
     *
     * @param rgb input RGB matrix
     * @return output YCbCr matrix
     */
    protected int[][][] convertToYCbCrMatrix(int[][][] rgb) { //Channel, x, y        
        int[][] y2 = new int[rgb[0].length][rgb[0][0].length];
        int[][] cb2 = new int[rgb[0].length][rgb[0][0].length];
        int[][] cr2 = new int[rgb[0].length][rgb[0][0].length];

        for (int i = 0; i < rgb[0].length; i++) {
            for (int j = 0; j < rgb[0][0].length; j++) {
                double y = (0.299 * rgb[0][i][j] + 0.587 * rgb[1][i][j] + 0.114 * rgb[2][i][j]);
                double cb = (128 + -0.16874 * rgb[0][i][j] - 0.33126 * rgb[1][i][j] + 0.50000 * rgb[2][i][j]);
                double cr = (128 + 0.50000 * rgb[0][i][j] - 0.41869 * rgb[1][i][j] - 0.08131 * rgb[2][i][j]);
                y2[i][j] = (int) y;
                cb2[i][j] = (int) cb;
                cr2[i][j] = (int) cr;
            }
        }
        return new int[][][]{y2, cb2, cr2};
    }
}
