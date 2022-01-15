package org.chaosdragon.stegovideo.BWBitmap;

import org.chaosdragon.stegovideo.exceptions.EncodingException;

import java.util.ArrayList;

/**
 * A class for averaging out multiple BWBitmaps into a single one
 *
 * @author David Griberman
 */
public class BMWAverager {

    //The arraylist containing all of the BWBitmaps to be compared
    private final ArrayList<BWBitmap> input = new ArrayList<>();

    /**
     * Adds a bitmap to the comparison list
     *
     * @param b The bitmap to add to the list
     */
    public void add(BWBitmap b) {
        input.add(b);
    }

    /**
     * Calculates the averaged BWBItmap from the items in the list
     *
     * @return Averaged BWBitmap
     */
    public BWBitmap getAverage() {

        if (input.isEmpty()) {
            throw new EncodingException("Cannot calculate watermark cause of missing data. " +
                    "Nothing might have been embedded as the file was too small.");
        }

        int width = input.get(0).getWidth();
        int height = input.get(0).getHeight();
        int[][] data = new int[width][height];

        BWBitmap result = new BWBitmap(width, height);

        for (BWBitmap bitmap : input) {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    data[i][j] += bitmap.getPixel(i, j);
                }
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                if (data[i][j] > input.size() / 2) { //FIXED, not >=, because 1 is black completely.
                    result.setPixel(i, j, (byte) 1);
                } else {
                    result.setPixel(i, j, (byte) 0);
                }
            }
        }
        return result;
    }

}
