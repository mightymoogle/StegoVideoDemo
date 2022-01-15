package org.chaosdragon.stegovideo.encoders;

import java.io.ByteArrayOutputStream;
import java.util.Random;

/**
 * Scrammbles a MessageEncoders input using seed. Used Descrambler to
 * descramble.
 *
 * @author David Griberman
 * @see Descrambler
 * @see MessageEncoder
 */
public class Scrambler implements MessageEncoder {

    byte[] values;
    int position;

    /**
     * @param en   - the input message encoder to decorate
     * @param seed - the input seed for the scrambling. Use -1 to disable.
     */
    public Scrambler(MessageEncoder en, long seed) {

        position = 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int s;
        while ((s = en.getNextBit()) != -1) {
            baos.write((byte) s);
        }

        values = baos.toByteArray();
        if (seed != -1) {
            Random rnd = new Random();

            rnd.setSeed(seed);

            for (int i = values.length - 1; i > 0; i--) {
                int index = rnd.nextInt(i + 1);

                // Simple swap
                byte a = values[index];
                values[index] = values[i];
                values[i] = a;
            }
        }
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
