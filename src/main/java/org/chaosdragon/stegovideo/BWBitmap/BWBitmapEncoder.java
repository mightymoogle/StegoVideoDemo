package org.chaosdragon.stegovideo.BWBitmap;

import org.chaosdragon.stegovideo.encoders.MessageEncoder;

import java.io.IOException;

/**
 * A class for encoding a BWBitmap using a BWBItmapStream
 *
 * @author David Griberman
 */
public class BWBitmapEncoder implements MessageEncoder {

    protected BWBItmapStream stream; //The stream used    

    /**
     * @param s The stream to use for encoding
     */
    public BWBitmapEncoder(BWBItmapStream s) {
        stream = s;
    }


    /**
     * Get the next bit from the stream
     *
     * @return next bit from the stream
     */
    @Override
    public byte getNextBit() {
        try {
            return (byte) stream.read();
        } catch (IOException ex) {
            return -1;
        }
    }

    /**
     * Resets the stream to starting position
     */
    @Override
    public void reset() {
        stream.reset();
    }

}
