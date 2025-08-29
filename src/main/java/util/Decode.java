package util;

import constant.Constant;

public class Decode {
    private static final double LATITUDE_RANGE = Constant.MAX_LATITUDE - Constant.MIN_LATITUDE;
    private static final double LONGITUDE_RANGE = Constant.MAX_LONGITUDE - Constant.MIN_LONGITUDE;

    static class Coordinates {
        double latitude;
        double longitude;

        Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    private static int compactInt64ToInt32(long v) {
        v = v & 0x5555555555555555L;
        v = (v | (v >> 1)) & 0x3333333333333333L;
        v = (v | (v >> 2)) & 0x0F0F0F0F0F0F0F0FL;
        v = (v | (v >> 4)) & 0x00FF00FF00FF00FFL;
        v = (v | (v >> 8)) & 0x0000FFFF0000FFFFL;
        v = (v | (v >> 16)) & 0x00000000FFFFFFFFL;
        return (int) v;
    }

    private static Coordinates convertGridNumbersToCoordinates(int gridLatitudeNumber, int gridLongitudeNumber) {
        // Calculate the grid boundaries
        double gridLatitudeMin = Constant.MIN_LATITUDE + LATITUDE_RANGE * (gridLatitudeNumber / Math.pow(2, 26));
        double gridLatitudeMax = Constant.MIN_LATITUDE + LATITUDE_RANGE * ((gridLatitudeNumber + 1) / Math.pow(2, 26));
        double gridLongitudeMin = Constant.MIN_LONGITUDE + LONGITUDE_RANGE * (gridLongitudeNumber / Math.pow(2, 26));
        double gridLongitudeMax = Constant.MIN_LONGITUDE + LONGITUDE_RANGE * ((gridLongitudeNumber + 1) / Math.pow(2, 26));

        // Calculate the center point of the grid cell
        double latitude = (gridLatitudeMin + gridLatitudeMax) / 2;
        double longitude = (gridLongitudeMin + gridLongitudeMax) / 2;

        return new Coordinates(latitude, longitude);
    }

    public static Coordinates decode(long geoCode) {
        // Align bits of both latitude and longitude to take even-numbered position
        long y = geoCode >> 1;
        long x = geoCode;

        // Compact bits back to 32-bit ints
        int gridLatitudeNumber = compactInt64ToInt32(x);
        int gridLongitudeNumber = compactInt64ToInt32(y);

        return convertGridNumbersToCoordinates(gridLatitudeNumber, gridLongitudeNumber);
    }
}
