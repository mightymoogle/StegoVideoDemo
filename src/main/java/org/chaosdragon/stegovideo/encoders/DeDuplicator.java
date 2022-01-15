package org.chaosdragon.stegovideo.encoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Companion class for Duplicator, does the transformation backwards
 *
 * @author David Griberman
 * @see Duplicator
 */
public class DeDuplicator extends OutputStream {

    int position;
    ByteArrayOutputStream baos;
    OutputStream decorator;
    int maxSize;
    int watermarkSize;
    int count;

    public static int getSize(int maxSize, int watermarkSize) {

        int a = maxSize / watermarkSize;

        if (a % 2 == 0) {
            a--;
        }

        return a * watermarkSize;
    }

    public DeDuplicator(OutputStream decorator, int maxSize, int watermarkSize) {
        this.decorator = decorator;
        baos = new ByteArrayOutputStream();
        this.maxSize = maxSize;
        this.watermarkSize = watermarkSize;

        count = maxSize / watermarkSize;
        if (count % 2 == 0) {
            count--;
        }
        /*if (count <= 0) {
            System.out.println("There was a problem writing the mark as it is too large");
        }*/
    }

    @Override
    public void write(int b) throws IOException {
        baos.write(b);
        position++;

        if (position == count * watermarkSize) {

            long[] averages = new long[watermarkSize];
            byte[] temp = baos.toByteArray();

            for (int i = 0; i < temp.length; i++) {
                averages[i % watermarkSize] += temp[i];
            }

            for (int i = 0; i < averages.length; i++) {
                if (averages[i] > count / 2 / 2) { //Extra /2
                    averages[i] = 1;
                } else {
                    averages[i] = 0;
                }
            }

            for (position = 0; position < averages.length; position++) {
                decorator.write((int) averages[position]);
            }
        }
    }
}
