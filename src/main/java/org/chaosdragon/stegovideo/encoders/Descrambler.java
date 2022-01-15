package org.chaosdragon.stegovideo.encoders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Stack;

/**
 * Desrammbles a Scramblers input using the same seed as Scrambler.
 *
 * @author David Griberman
 * @see Scrambler
 */
public class Descrambler extends OutputStream {

    private byte[] values;
    private int position;
    private Stack<Integer> stack;
    private ByteArrayOutputStream baos;
    private OutputStream decorator;
    private int size;
    private long seed;

    /**
     * @param en   - the input message encoder to decorate
     * @param seed - the input seed for the descrambling. Use -1 to disable.
     */
    public Descrambler(OutputStream decorator, long seed, int size) {

        this.decorator = decorator;
        baos = new ByteArrayOutputStream();
        this.size = size;
        this.seed = seed;

        Random rnd = new Random(); //SecureRandom different on different platforms

        //Do nothing if -1, potential bug if password hash is -1..
        if (seed != -1) {
            rnd.setSeed(seed);

            stack = new Stack<>();

            for (int i = size - 1; i > 0; i--) {
                stack.add(rnd.nextInt(i + 1));
            }
        }
    }

    @Override
    public void write(int b) throws IOException {

        if (seed == -1) {
            decorator.write(b);
            return;
        }
        baos.write(b);
        position++;

        // -1 here
        if (position == size - 1) {
            values = baos.toByteArray();

            //-1 here
            for (int i = 1; i <= values.length - 1; i++) {
                int index = stack.pop();

                // Simple swap
                byte a = values[index];
                values[index] = values[i];
                values[i] = a;
            }

            //-1 here, why?
            for (position = 0; position < size - 1; position++) {
                decorator.write(values[position]);
            }
        }
    }
}
