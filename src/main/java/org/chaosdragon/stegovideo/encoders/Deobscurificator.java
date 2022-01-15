package org.chaosdragon.stegovideo.encoders;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Companion class for Obscurificator, does the transformation backwards
 * @author David Griberman
 * @see Obscurificator
 */
public class Deobscurificator extends OutputStream {

    int position;
    int size;
    OutputStream decorator;

    public Deobscurificator(OutputStream s, int size) {
        decorator = s;
        this.size = size;
        position = 0;
    }

    @Override
    public void write(int b) throws IOException {
        if (position < size) {
            position++;
            decorator.write(b);
        }
    }
}
