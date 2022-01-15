package org.chaosdragon.stegovideo.encoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Duplicates the message till maxSize
 *
 * @author David Griberman
 * @see DeDuplicator
 */
public class Duplicator implements MessageEncoder {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Duplicator.class);

    byte[] values;
    int position;

    public Duplicator(MessageEncoder en, long maxSize) {
        position = 0;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            int s;
            while ((s = en.getNextBit()) != -1) {
                baos.write((byte) s);
            }

            byte[] temp = baos.toByteArray();

            int size = (int) (maxSize / temp.length);

            if (size % 2 == 0) {
                size--;
            }

            //Avoids crashing
            if (size <= 0) {
                values = new byte[1];
                /*System.out.println("There was a problem writing the mark as it is too large");*/
                return;
            }

            values = new byte[size * temp.length];

            //Can be changed to write 3 times!
            for (int i = 0; i < size; i++) {
                System.arraycopy(temp, 0, values, i * temp.length, temp.length);
            }

            log.info("*** Watermark copies per frame ***: {}", size);
        } catch (IOException e) {
            log.error("Duplicator initialization!", e);
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
        values = null;
    }
}
