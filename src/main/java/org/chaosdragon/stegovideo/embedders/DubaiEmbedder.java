package org.chaosdragon.stegovideo.embedders;

import org.chaosdragon.stegovideo.encoders.MessageEncoder;
import org.chaosdragon.stegovideo.tools.Rounding;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class that does embedding using the Dubai approach
 *
 * @author David Griberman
 */
public class DubaiEmbedder implements DTCEmbedder {

    private final int strength;
    private MessageEncoder encoder;
    private OutputStream decoder;

    public DubaiEmbedder(int strength) {
        this.strength = strength;
    }

    @Override
    public void reset() {
        encoder.reset();
    }

    @Override
    public int[][] embed(int[][] n, int channel) {
        if (willHide(channel)) {
            return embed(n);
        }
        return n;
    }

    //Will hide in channel 1
    @Override
    public boolean willHide(int channel) {
        return channel == 1;
    }

    @Override
    public void setEncoder(MessageEncoder encoder) {
        this.encoder = encoder;
    }

    /**
     * A helper method for transforming a matrix to a sorted TreeMap
     *
     * @param n the input matrix
     * @return the sorted TreeMap
     */
    private TreeMap<Integer, Point> matrixToMap(int[][] n) {

        Comparator<Integer> cc = (o1, o2) -> {
            if (o1 > o2) {
                return -1;
            }
            if (o2 > o1) {
                return 1;
            }
            return 0;
        };

        TreeMap<Integer, Point> map = new TreeMap<>(cc);

        for (int i = 0; i < n.length; i++) {

            for (int j = 0; j < n[0].length; j++) {

                //Skip DTC coefficient 0,0
                if (i != 0 && j != 0) {
                    map.put(n[i][j], new Point(i, j));
                }
            }
        }

        return map;
    }

    //Embed into 8 max coefficients in each of the blocks
    public int[][] embed(int[][] n) {

        //Check block eligibility, don't embed if it is monotone
        //Find two best places to replace base on something
        byte embedMe = 0;
        if (encoder == null) {
            return n; // Throw error instead?
        }

        embedMe = encoder.getNextBit();

        if (embedMe < 0) {
            return n;
        }

        TreeMap<Integer, Point> map = matrixToMap(n);

        int counter = 0;
        boolean isEven = embedMe == 1;

        for (Map.Entry<Integer, Point> entry : map.entrySet()) {
            int x = entry.getValue().x;
            int y = entry.getValue().y;
            n[x][y] = strength * Rounding.roundToNearestOddEven((n[x][y] * 1.0) / strength, isEven);

            counter++;

            if (counter == 9) {
                return n;
            }
        }

        return n;
    }

    @Override
    public void decodeBlock(int[][] n, int channel) throws IOException {

        if (!willHide(channel)) {
            return;
        }
        int average = 0;

        TreeMap<Integer, Point> map = matrixToMap(n);

        int counter = 0;

        for (Map.Entry<Integer, Point> entry : map.entrySet()) {
            int x = entry.getValue().x;
            int y = entry.getValue().y;
            if (Math.round((n[x][y] * 1.0) / strength) % 2 == 1) {
                average++;
            }
            counter++;
            if (counter == 9) {
                break;
            }
        }

        if (average >= 1) {
            decoder.write(0);
        } else {
            decoder.write(1);
        }
    }

    @Override
    public void setOutputStream(OutputStream decoder) {
        this.decoder = decoder;
    }

}
