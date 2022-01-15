package org.chaosdragon.stegovideo.embedders;

import org.chaosdragon.stegovideo.encoders.MessageEncoder;
import org.chaosdragon.stegovideo.tools.AdaptiveBox;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * A class that embeds data using the Kothari method
 *
 * @author David Griberman
 */
public class KothariEmbedder implements DTCEmbedder {

    private int strength;
    private static final int POS1 = 5;
    private static final int POS2 = 2;
    private static final int POS3 = 4;
    private static final int POS4 = 3;
    private MessageEncoder encoder;
    private OutputStream decoder;

    public KothariEmbedder(int strength) {
        this.strength = strength;
    }

    @Override
    public int[][] embed(int[][] n, int channel) {

        //Embedd in CB-CR
        if (willHide(channel)) {
            return embed(n);
        }
        return n;
    }

    //Determine channel to hide into, 0 = Y
    @Override
    public boolean willHide(int channel) {
        return channel == 1;
    }

    @Override
    public void setEncoder(MessageEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void reset() {
        encoder.reset();
    }

    public int[][] embed(int[][] n, int channel, int str) {
        int temp = strength;

        //If not movement
        if (str < AdaptiveBox.SENSITIVITY) {
            if (str == AdaptiveBox.THRESHOLD) {
                strength *= 0.75;
            } else {
                strength /= 2;
            }
        }
        int[][] result = embed(n, channel);

        strength = temp;
        return result;
    }

    public int[][] embed(int[][] n) {
        //Check block eligibility, don't embed if it is monotone
        //Find two best places to replace base on something
        byte embedMe = 0;
        if (encoder == null) {
            return n; //Throw error instead?
        }

        embedMe = encoder.getNextBit();

        if (embedMe < 0) {
            return n;
        }

        if (Math.abs(n[POS1][POS2] - n[POS3][POS4]) < strength) {
            //Should be random noise here.
            Random p = new Random();

            int noise = p.nextInt(strength);

            n[POS1][POS2] += noise;
            n[POS3][POS4] -= strength - noise;
        }

        //Flip if not correct
        if ((n[POS1][POS2] > n[POS3][POS4] && embedMe == 1)
                || n[POS1][POS2] < n[POS3][POS4] && embedMe == 0) {
            int temp = n[POS1][POS2];
            n[POS1][POS2] = n[POS3][POS4];
            n[POS3][POS4] = temp;
        }

        return n;
    }

    //One block can contain multiple bits!
    @Override
    public void decodeBlock(int[][] n, int channel) throws IOException {
        if (!willHide(channel)) {
            return;
        }

        if (n[POS1][POS2] < n[POS3][POS4]) {
            decoder.write(1);
        } else {
            decoder.write(0);
        }
    }

    @Override
    public void setOutputStream(OutputStream decoder) {
        this.decoder = decoder;
    }

}
