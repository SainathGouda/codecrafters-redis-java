package util;

import constant.Constant;

public class Encode {
    private static final double LATITUDE_RANGE = Constant.MAX_LATITUDE - Constant.MIN_LATITUDE;
    private static final double LONGITUDE_RANGE = Constant.MAX_LONGITUDE - Constant.MIN_LONGITUDE;

//    Before spread: x1  x2  ...  x31  x32
//    After spread:  0   x1  ...   0   x16  ... 0  x31  0  x32
    private static long spreadInt32ToInt64(int v) {
        long result = v & 0xFFFFFFFFL;
        //Bitwise operations to spread 32 bits into 64 bits with zeros in-between
        result = (result | (result << 16)) & 0x0000FFFF0000FFFFL;
        result = (result | (result << 8)) & 0x00FF00FF00FF00FFL;
        result = (result | (result << 4)) & 0x0F0F0F0F0F0F0F0FL;
        result = (result | (result << 2)) & 0x3333333333333333L;
        result = (result | (result << 1)) & 0x5555555555555555L;
        return result;
    }

    private static long interleave(int x, int y) {
        long xSpread = spreadInt32ToInt64(x);
        long ySpread = spreadInt32ToInt64(y);
        long yShifted = ySpread << 1;
//        Before bitwise OR (x): 0   x1   0   x2   ...  0   x31    0   x32
//        Before bitwise OR (y): y1  0    y2  0    ...  y31  0    y32   0
//        After bitwise OR     : y1  x2   y2  x2   ...  y31  x31  y32  x32
        return xSpread | yShifted;
    }

    public static long encode(double latitude, double longitude) {
        // Normalize to the range 0-2^26
        double normalizedLatitude = Math.pow(2, 26) * (latitude - Constant.MIN_LATITUDE) / LATITUDE_RANGE;
        double normalizedLongitude = Math.pow(2, 26) * (longitude - Constant.MIN_LONGITUDE) / LONGITUDE_RANGE;

        // Truncate to integers
        int latInt = (int) normalizedLatitude;
        int lonInt = (int) normalizedLongitude;

        return interleave(latInt, lonInt);
    }
}
