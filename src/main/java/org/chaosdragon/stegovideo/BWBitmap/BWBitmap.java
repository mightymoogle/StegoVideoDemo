package org.chaosdragon.stegovideo.BWBitmap;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * BWBitmap.java - a class representing a black and white BufferedImage as an
 * binary array. Created for steganographic purposes.
 *
 * @author David Griberman
 * @version 1.0
 * @see BufferedImage
 */
public class BWBitmap {

    /**
     * The bitmap, 1 representing black, 0 representing white
     */
    private final byte[][] image;

    /**
     * The default constructor
     *
     * @param img The input BufferedImage that is to be transformed to black and
     * white array.
     */
    public BWBitmap(BufferedImage img) {
        BufferedImage black = new BufferedImage(img.getWidth(), img.getHeight(),
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = black.createGraphics();
        g.drawImage(img, 0, 0, null);

        image = new byte[img.getWidth()][img.getHeight()];
        for (int y = 0; y < black.getHeight(); y++) {
            for (int x = 0; x < black.getWidth(); x++) {
                int color = black.getRGB(x, y);
                if (color == -1) {
                    image[x][y] = 0; // White
                }

                if (color == -16777216) {
                    image[x][y] = 1; // Black
                }
            }
        }
    }

    /**
     * Retrieve a BufferedImage representing the current BWBitmap
     *
     * @return A TYPE_BYTE_BINARY bufferedImage
     */
    public BufferedImage toBufferedImage() {

        BufferedImage img = new BufferedImage(image.length, image[0].length,
                BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < image[0].length; y++) {
            for (int x = 0; x < image.length; x++) {
                int color = image[x][y];
                if (color == 0) {
                    img.setRGB(x, y, -1); // White
                }
                if (color ==1) {
                    img.setRGB(x, y, -16777216); // Black
                }
            }
        }
        return img;
    }

    /**
     * Returns the color of the current pixel
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return 0 if current pixel is white, 1 if it is black
     */
    public byte getPixel(int x, int y) {
        return image[x][y];
    }

    /**
     * Sets the pixel in a BWBitmap
     *
     * @param x
     * @param y
     * @param value Color value to set (1 or 0)
     */
    public void setPixel(int x, int y, byte value) {
        image[x][y] = value;
    }

    public int getWidth() {
        return image.length;
    }

    public int getHeight() {
        return image[0].length;
    }

    /**
     * Creates an empty BWBitmap of the given size
     *
     * @param width
     * @param height
     */
    public BWBitmap(int width, int height) {
        image = new byte[width][height];
    }
}
