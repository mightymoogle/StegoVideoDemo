package org.chaosdragon.stegovideo.encoders;

/**
 * An interface for the message encoders
 *
 * @author David Griberman
 */
public interface MessageEncoder {

    byte getNextBit();

    void reset();
}
