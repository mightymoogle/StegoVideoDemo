package org.chaosdragon.stegovideo.BWBitmap;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A OutputStream class for writing a BWBitap bit by bit
 *
 * @author David Griberman
 */
public class BWBitmapStreamWriter extends OutputStream {

    protected BWBitmap img; //Underlying BWBitmap
    //The current position
    protected int x;
    protected int y;

    /**
     *
     * @param img The BWBitmap to write to
     */
    public BWBitmapStreamWriter(BWBitmap img) {
        this.img = img;
        x = 0;
        y = 0;
    }

    /**
     * Writes a pixel to the BWBitmap
     *
     * @param value the pixel value to write (1 or 0)
     * @throws IOException
     */
    @Override
    public void write(int value) throws IOException {

        if (x >= img.getWidth()) {
            x = 0;
            y++;
        }

        if (y >= img.getHeight()) {
            return;
        }

        if (value == 1 || value == 0) {
            img.setPixel(x++, y, (byte) value);
        }
    }
}
