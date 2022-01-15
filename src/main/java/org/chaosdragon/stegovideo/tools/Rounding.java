package org.chaosdragon.stegovideo.tools;

/**
 * Provides rounding to nearest odd or even integer
 *
 * @author David Griberman
 */
public class Rounding {

    private Rounding() {
        // Utility class
    }

    /**
     * Round to nearest odd or even integer
     *
     * @param input number to round
     * @param even  if true - round to even, otherwise round to odd
     * @return
     */
    public static int roundToNearestOddEven(double input, boolean even) {
        double temp;
        if (even) {
            temp = Math.round(input * 0.5f) * 2.0;
        } else {
            temp = Math.round((input + 1) / 2) * 2.0 - 1;
        }
        return (int) temp;
    }
}
