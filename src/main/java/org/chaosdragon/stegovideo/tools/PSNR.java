package org.chaosdragon.stegovideo.tools;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Helper class - a PSNR and MSE calculator, based on:
 * https://code.google.com/p/rastertovector/source/browse/trunk/src/com/sxz/math/PSNR.java?spec=svn11&r=11
 *
 * @author David Griberman
 */
public final class PSNR {

    private PSNR() {
        // prevent initialization
    }

    public static double calculatePSNR(double meanSquaredError) {
        if (meanSquaredError == 0) {
            return 0;
        }
        return 10 * StrictMath.log10((255 * 255) / meanSquaredError);
    }

    public static double measureMSE(BufferedImage image1, BufferedImage image2) {
        if (image1.getWidth() != image2.getWidth()) {
            return -2;
        }
        if (image1.getHeight() != image2.getHeight()) {
            return -2;
        }
        int totalRed = 0;
        int totalGreen = 0;
        int totalBlue = 0;
        int maxRed = -1;
        int maxGreen = -1;
        int maxBlue = -1;
        double maxDistance = -1;

        for (int i = 0; i < image1.getWidth(); i++) {
            for (int j = 0; j < image1.getHeight(); j++) {
                final Color color1 = new Color(image1.getRGB(i, j));
                final Color color2 = new Color(image2.getRGB(i, j));
                final double distance = getColorDistance(color1, color2);
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
                final int redDiff = color1.getRed() - color2.getRed();
                if (redDiff > maxRed) {
                    maxRed = redDiff;
                }
                final int greenDiff = color1.getGreen() - color2.getGreen();
                if (greenDiff > maxGreen) {
                    maxGreen = greenDiff;
                }
                final int blueDiff = color1.getBlue() - color2.getBlue();
                if (blueDiff > maxBlue) {
                    maxBlue = blueDiff;
                }
                totalRed += redDiff * redDiff;
                totalGreen += greenDiff * greenDiff;
                totalBlue += blueDiff * blueDiff;
            }
        }

        return (totalRed + totalGreen + totalBlue) / (image1.getWidth() * image1.getHeight() * 3.0f);
    }

    public static double measurePSNR(BufferedImage image1, BufferedImage image2) {
        return calculatePSNR(measureMSE(image1, image2));
    }

    private static double getColorDistance(Color source, Color target) {
        if (source.equals(target)) {
            return 0.0d;
        }
        final double red = source.getRed() - target.getRed();
        final double green = source.getGreen() - target.getGreen();
        final double blue = source.getBlue() - target.getBlue();
        return Math.sqrt(red * red + blue * blue + green * green);
    }
}
