package org.chaosdragon.stegovideo.encoders;

import java.io.OutputStream;

/**
 * Interface for an embedding factory object
 *
 * @author David Griberman
 */
public interface EncoderFactory {

    MessageEncoder makeEncoder();

    OutputStream makeOutputStream();

    int getBlockSize(); //Not used

    EncoderFactory setBlockSize(int size);

    EncoderFactory setFillNoise(boolean noise);

    void setMaxEmbeddableSize(int bits);

    EncoderFactory setWatermarkSize(int x, int y);

    EncoderFactory setKey(long k);

    EncoderFactory setMultipleCopies(boolean t);
}
