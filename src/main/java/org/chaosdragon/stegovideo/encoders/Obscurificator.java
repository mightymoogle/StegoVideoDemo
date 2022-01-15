package org.chaosdragon.stegovideo.encoders;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Obscurifies the message by adding random noises to the empty regions
 *
 * @author David Griberman
 * @see Deobscurificator
 */
public class Obscurificator implements MessageEncoder {

    byte[] values;
    int position;

    public Obscurificator(MessageEncoder en, long maxSize, boolean fillNoise) {

        position = 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int s;
        while ((s = en.getNextBit()) != -1) {
            baos.write((byte) s);
        }

        while (baos.size() < maxSize) {

            if (fillNoise) {
                baos.write((byte) new Random().nextInt(2));
            } else {
                baos.write((byte) -2);
            }

        }

        values = baos.toByteArray();
        baos = null;
    }

    @Override
    public byte getNextBit() {
        if (position < values.length) {
            return values[position++];
        }

        return -1;
    }

    @Override
    public void reset() {
        position = 0;
    }

}
