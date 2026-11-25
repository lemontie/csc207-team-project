package data;

import java.util.Collection;

/**
 * Converts one or more hex colors into a normalized 8 x 8 x 8 Oklab histogram.
 * The histogram index is L * 64 + a * 8 + b.
 */
public class OklabEmbeder {
    /** Number of histogram bins along each Oklab axis. */
    public static final int BINS_PER_AXIS = 8;
    /** Total histogram size: 8 cubed bins. */
    public static final int HISTOGRAM_SIZE = BINS_PER_AXIS * BINS_PER_AXIS * BINS_PER_AXIS;

    private static final double[] AXIS_MIN = {0.0, -0.2339, -0.3116};
    private static final double[] AXIS_MAX = {1.0, 0.2763, 0.1986};
    private static final double GAUSSIAN_SIGMA = 0.06;
    private static final double[][] BIN_CENTERS = computeBinCenters();

    /**
     * Creates a normalized Oklab histogram for one hexadecimal color.
     * @param hexCode a six-digit hexadecimal RGB value, optionally prefixed with {@code #}
     * @return a 512-value normalized histogram
     */
    public double[] embed(String hexCode) {
        return embed(new String[]{hexCode});
    }

    /**
     * Combines several hexadecimal colors into a normalized Oklab histogram.
     * Each valid color contributes equal weight before normalization.
     * @param hexCodes at least one six-digit hexadecimal RGB value
     * @return a 512-value normalized histogram
     * @throws IllegalArgumentException if no codes are supplied or a code is invalid
     */
    public double[] embed(String[] hexCodes) {
        if (hexCodes == null || hexCodes.length == 0) {
            throw new IllegalArgumentException("At least one hex code is required");
        }

        double[] histogram = new double[HISTOGRAM_SIZE];
        for (String hexCode : hexCodes) {
            double[] oklab = hexToOklab(hexCode);
            addToHistogram(histogram, oklab, 1.0);
        }
        normalize(histogram);
        return histogram;
    }

    /**
     * Combines a collection of hexadecimal colors into a normalized Oklab histogram.
     * @param hexCodes at least one six-digit hexadecimal RGB value
     * @return a 512-value normalized histogram
     * @throws IllegalArgumentException if the collection is null or empty
     */
    public double[] embed(Collection<String> hexCodes) {
        if (hexCodes == null) {
            throw new IllegalArgumentException("Hex codes must not be null");
        }
        return embed(hexCodes.toArray(new String[0]));
    }

    /**
     * Converts a hexadecimal sRGB color to CIE-like Oklab coordinates.
     * @param hexCode a six-digit hexadecimal RGB value, optionally prefixed with {@code #}
     * @return coordinates {@code [L, a, b]}
     * @throws IllegalArgumentException if the code is malformed
     */
    public double[] hexToOklab(String hexCode) {
        int rgb = parseHex(hexCode);
        double red = linearRgb(((rgb >> 16) & 0xff) / 255.0);
        double green = linearRgb(((rgb >> 8) & 0xff) / 255.0);
        double blue = linearRgb((rgb & 0xff) / 255.0);

        double l = 0.4122214708 * red + 0.5363325363 * green + 0.0514459929 * blue;
        double m = 0.2119034982 * red + 0.6806995451 * green + 0.1073969566 * blue;
        double s = 0.0883024619 * red + 0.2817188376 * green + 0.6299787005 * blue;

        double cubeL = Math.cbrt(l);
        double cubeM = Math.cbrt(m);
        double cubeS = Math.cbrt(s);

        return new double[]{
                0.2104542553 * cubeL + 0.7936177850 * cubeM - 0.0040720468 * cubeS,
                1.9779984951 * cubeL - 2.4285922050 * cubeM + 0.4505937099 * cubeS,
                0.0259040371 * cubeL + 0.7827717662 * cubeM - 0.8086757660 * cubeS
        };
    }

    private static int parseHex(String hexCode) {
        if (hexCode == null) {
            throw new IllegalArgumentException("Hex code must not be null");
        }
        String value = hexCode.trim();
        if (value.startsWith("#")) {
            value = value.substring(1);
        }
        if (value.length() != 6) {
            throw new IllegalArgumentException(
                    "Hex code must contain exactly six digits: " + hexCode);
        }
        try {
            return Integer.parseInt(value, 16);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid hex code: " + hexCode, exception);
        }
    }

    private static double linearRgb(double channel) {
        return channel <= 0.04045
                ? channel / 12.92
                : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    private static void addToHistogram(double[] histogram, double[] oklab, double weight) {
        double[] shares = new double[HISTOGRAM_SIZE];
        double totalShare = 0.0;
        for (int index = 0; index < HISTOGRAM_SIZE; index++) {
            double[] center = BIN_CENTERS[index];
            double dl = oklab[0] - center[0];
            double da = oklab[1] - center[1];
            double db = oklab[2] - center[2];
            double share = Math.exp(
                    -(dl * dl + da * da + db * db)
                            / (2.0 * GAUSSIAN_SIGMA * GAUSSIAN_SIGMA));
            shares[index] = share;
            totalShare += share;
        }

        for (int index = 0; index < HISTOGRAM_SIZE; index++) {
            histogram[index] += weight * shares[index] / totalShare;
        }
    }

    private static double[][] computeBinCenters() {
        double[][] centers = new double[HISTOGRAM_SIZE][3];
        for (int l = 0; l < BINS_PER_AXIS; l++) {
            for (int a = 0; a < BINS_PER_AXIS; a++) {
                for (int b = 0; b < BINS_PER_AXIS; b++) {
                    int index = (l * BINS_PER_AXIS + a) * BINS_PER_AXIS + b;
                    centers[index][0] = binCenter(0, l);
                    centers[index][1] = binCenter(1, a);
                    centers[index][2] = binCenter(2, b);
                }
            }
        }
        return centers;
    }

    private static double binCenter(int axis, int bin) {
        return AXIS_MIN[axis]
                + (bin + 0.5) * (AXIS_MAX[axis] - AXIS_MIN[axis]) / BINS_PER_AXIS;
    }

    private static void normalize(double[] histogram) {
        double sum = 0;
        for (double value : histogram) {
            sum += value;
        }
        if (sum == 0) {
            throw new IllegalStateException("Cannot normalize an empty Oklab histogram");
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] /= sum;
        }
    }

}
