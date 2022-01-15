package org.chaosdragon.stegovideo.BWBitmap;

import java.io.IOException;
import java.io.InputStream;

/**
 * A class representing an input stream for a BWBitmap
 *
 * @author David Griberman
 */
public class BWBItmapStream extends InputStream {

    private final BWBitmap img; //The bitmap
    //Current position
    private int x;
    private int y;

    /**
     * The main constructor
     *
     * @param img The input BWBitmap to read from
     */
    public BWBItmapStream(BWBitmap img) {
        this.img = img;
    }

    /**
     * Read a pixel from the BWBitmap
     *
     * @return current pixel value (black&white, 1 or 0)
     * @throws IOException
     */
    @Override
    public int read() throws IOException {

        if (x >= img.getWidth()) {
            x = 0;
            y++;
        }

        if (y >= img.getHeight()) {
            return -1;
        }
        return img.getPixel(x++, y)  & 0xFF;
    }

    /**
     * Resets the current position in the BWBitmap to the starting point (0,0)
     */
    @Override
    public synchronized void reset() {
        x = 0;
        y = 0;
    }
}
