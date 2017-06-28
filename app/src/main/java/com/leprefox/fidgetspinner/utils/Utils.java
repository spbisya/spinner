package com.leprefox.fidgetspinner.utils;

/**
 * Project FIdgetSpinner. Created by Izya Pitersky on 5/4/17.
 */

public class Utils {

    public static int getUnsignedInt(int valueToFormat) {
        if (String.valueOf(valueToFormat).contains("-")) {
            return Integer.parseInt(String.valueOf(valueToFormat).substring(1));
        }
        return valueToFormat;
    }

    /**
     * @return The selected quadrant.
     */
    public static int getQuadrant(double x, double y) {
        if (x >= 0) {
            return y >= 0 ? 1 : 4;
        } else {
            return y >= 0 ? 2 : 3;
        }

    }

    /**
     * @return The angle of the unit circle with the image view's center
     */
    public static double getAngle(double xTouch, double yTouch, int spinnerWidth, int spinnerHeight) {
        double x = xTouch - (spinnerWidth / 2d);
        double y = spinnerHeight - yTouch - (spinnerHeight / 2d);

        switch (getQuadrant(x, y)) {
            case 1:
                return Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;
            case 2:
            case 3:
                return 180 - (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);

            case 4:
                return 360 + Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI;

            default:
                // ignore, does not happen
                return 0;
        }
    }

}
