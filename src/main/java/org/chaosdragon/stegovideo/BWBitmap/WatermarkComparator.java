package org.chaosdragon.stegovideo.BWBitmap;

import org.chaosdragon.stegovideo.exceptions.EncodingException;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Compares extracted watermarks and the original one
 *
 * @author David Griberman
 */
public class WatermarkComparator {

    protected double maxDifference;
    protected double minDifference;

    protected ArrayList<Double> values = new ArrayList<>();

    public WatermarkComparator() {
        maxDifference = 0;
        minDifference = 2;
    }

    /**
     * Compare two watermarks
     *
     * @param original The original watermark
     * @param second The extracted watermark
     * @return the difference
     */
    public double compare(BufferedImage original, BufferedImage second) {

        if (original.getWidth() != second.getWidth()
                || original.getHeight() != second.getHeight()) {
            return -1;
        }

        long result = 0;
        long totalPixels = 0;

        for (int i = 0; i < original.getWidth(); i++) {
            for (int j = 0; j < original.getHeight(); j++) {

                if (original.getRGB(i, j) == (second.getRGB(i, j))) {
                    result++;
                }

                totalPixels++;
            }
        }

        if (totalPixels == 0) {
            throw new EncodingException("Error during watermark comparison, 0 pixels detected");
        }

        double finalResult = 1.0 * result / totalPixels;

        if (finalResult > maxDifference) {
            maxDifference = finalResult;
        }
        if (finalResult < minDifference) {
            minDifference = finalResult;
        }

        values.add(finalResult);
        return finalResult;
    }

    /**
     *
     * @return the max difference recorded in this object
     */
    public double getMaxDifference() {
        return maxDifference;
    }

    /**
     *
     * @return the in difference recorded in this object
     */
    public double getMinDifference() {
        return minDifference;
    }

    /**
     *
     * @return the average difference between all records in this object
     */
    public double getAverageDifference() {
        //Can overflow!
        double sum = 0;
        for (double d : values) {
            sum += d;
        }

        return sum / values.size();
    }
}
