package rmillett.pitchbender;

/**
 * The <code>Music</code> class is a helper class which functions as multipurpose tool for performing
 * acoustic-/music-related calculations and conversions such as parsing ratios, converting to and from
 * cents, measuring intervallic distances in various formats, and pitch-detection based on multiple
 * equal-tempered dodecaphonic and microtonal systems.
 *
 * From Wikipedia:
 *
 * In music theory, an interval is the difference between two pitches. An interval may be described
 * as horizontal, linear, or melodic if it refers to successively sounding tones, such as two adjacent
 * pitches in a melody, and vertical or harmonic if it pertains to simultaneously sounding tones, such
 * as in a chord.
 *
 * In physical terms, an interval is the ratio between two sonic frequencies. For example, any two
 * notes an octave apart have a frequency ratio of 2:1. This means that successive increments of pitch
 * by the same interval result in an exponential increase of frequency, even though the human ear
 * perceives this as a linear increase in pitch. For this reason, intervals are often measured in
 * cents, a unit derived from the logarithm of the frequency ratio.
 *
 * The cent is a logarithmic unit of measure used for musical intervals. Twelve-tone equal
 * temperament divides the octave into 12 semitones of 100 cents each. Typically, cents are used
 * to express small intervals, or to compare the sizes of comparable intervals in different
 * tuning systems, and in fact the interval of one cent is too small to be heard between
 * successive notes.
 *
 * @author Ryan Millett
 * @version 1.5
 */
public class Music {

    private static String TAG = "Music";

    // TODO: make constant Strings and arrays compatible with 24-TET, 48-TET, 36-TET, 53-TET systems

    public static final String UNKNOWN_NOTE = "";
    public static final String FLAT = "\u266D";
    public static final String SHARP = "\u266F";
    public static final String C = "C";
    public static final String C_SHARP = C + SHARP;
    public static final String D = "D";
    public static final String D_FLAT = D + FLAT;
    public static final String D_SHARP = D + SHARP;
    public static final String E = "E";
    public static final String E_FLAT = E + FLAT;
    public static final String F = "F";
    public static final String F_SHARP = F + SHARP;
    public static final String G = "G";
    public static final String G_FLAT = G + FLAT;
    public static final String G_SHARP = G + SHARP;
    public static final String A = "A";
    public static final String A_FLAT = A + FLAT;
    public static final String A_SHARP = A + SHARP;
    public static final String B = "B";
    public static final String B_FLAT = B + FLAT;

    /**
     * Constant array of double values representing frequencies in Hertz that correspond to a pitch
     * class in the 12-tone equal-temperament system.
     *
     * A4 (A above middle-C) corresponds to 440Hz.
     *
     */
    public static final double[] _12_TET_PITCH_FREQUENCIES =
            new double[]{
                27.5000, 29.1352, 30.8677, 32.7032, 34.6478, 36.7081, 38.8909, 41.2034, 43.6535,
                46.2493, 48.9994, 51.9131, 55.0000, 58.2705, 61.7354, 65.4064, 69.2957, 73.4162,
                77.7817, 82.4069, 87.3071, 92.4986, 97.9989, 103.826, 110.000, 116.541, 123.471,
                130.813, 138.591, 146.832, 155.563, 164.814, 174.614, 184.997, 195.998, 207.652,
                220.000, 233.082, 246.942, 261.626, 277.183, 293.665, 311.127, 329.628, 349.228,
                369.994, 391.995, 415.305, 440.000, 466.164, 493.883, 523.251, 554.365, 587.330,
                622.254, 659.255, 698.456, 739.989, 783.991, 830.609, 880.000, 932.328, 987.767,
                1046.50, 1108.73, 1174.66, 1244.51, 1318.51, 1396.91, 1479.98, 1567.98, 1661.22,
                1760.00, 1864.66, 1975.53, 2093.00, 2217.46, 2349.32, 2489.02, 2637.02, 2793.83,
                2959.96, 3135.96, 3322.44, 3520.00, 3729.31, 3951.07, 4186.01};

    /**
     * Constant array of String values representing 12-TET pitch class names.
     */
    public static final String[] NOTES =
            new String[]{A, A_SHARP, B, C, C_SHARP, D, D_SHARP, E, F, F_SHARP, G, G_SHARP};

    /**
     * Converts a decimal (double value) to a string representing the approximate fraction equivalent
     *
     * This method is used to express the relationship between two frequencies as a simple fraction
     *
     * @param decimal a double value representing the decimal form of a whole-number ratio
     * @return A String representing an approximate whole-number ratio
     */
    public static String convertDecimalToRatio(double decimal) {
        if (decimal < 0){
            return "-" + convertDecimalToRatio(-decimal);
        }
        double tolerance = 1.0E-6;
        double m1 = 1; double m2 = 0;
        double n1 = 0; double n2 = 1;
        double b = decimal;
        do {
            double a = Math.floor(b);
            double aux = m1; m1 = a * m1 + m2; m2 = aux;
            aux = n1; n1 = a * n1 + n2; n2 = aux;
            b = 1 / (b - a);
        } while (Math.abs(decimal - m1 / n1) > decimal * tolerance);

        return (int) m1 + "/" + (int) n1;
    }

    /**
     * Converts a String representing a whole-number ratio into a decimal (double value).
     *
     * This ratio value is used to calculate a specified interval above a given fundamental frequency
     *
     * @param ratio A String representing a whole-number ratio
     * @return Converted decimal (double value) representing the decimal form of a whole-number ratio
     */
    public static double convertRatioToDecimal(String ratio) {
        if (ratio.contains("/")) {
            // TODO: make split either "/" or ":"
            String[] rat = ratio.split("/");
            return Double.parseDouble(rat[0]) / Double.parseDouble(rat[1]);
        }
        else
            return Double.parseDouble(ratio);
    }

    public static boolean isRatio(String ratio) {
        if (ratio.contains("/")) {
            return true;
        }
        else {
            return false;
        }

    }

    /**
     * Takes a String representing an interval expressed as a whole-number ratio and returns the
     * interval size expressed in cents.
     *
     * Formula used (where m/n is the ratio): 1200*log(m/n)/log(2)
     *
     * @param ratio A String representing a whole-number ratio
     * @return double value of ratio expressed in cents
     */
    public static double convertRatioToCents(String ratio) {
        return 1200 * Math.log(Music.convertRatioToDecimal(ratio)) / Math.log(2);
    }

    /**
     * Converts an intervallic distance in cents into a decimal (double value).
     *
     * This method is best used when chained with the <code>convertDecimalToRatio()</code>
     * method or when creating a new interval by multiplying a fundamental frequency by the
     * returned decimal (double) value.
     *
     * @param cents A double value representing an interval in cents
     * @return Converted decimal (double value) representing the decimal form of an interval
     */
    public static double convertCentsToDecimal(double cents) {
        return Math.pow(2, cents / 1200);
    }

    /**
     * Determines if a String can be parsed as an Integer
     *
     * @param str String to be parsed
     * @return true of String can be parsed as Integer, false otherwise
     */
    public static boolean isInteger(String str) {
        str = str.trim();
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static int[] parseTET(String tet) {
        if (tet.contains(",")) {
            String[]tets = tet.split(",");
            int[] tetArray = new int[tets.length];
            for (int i = 0; i < tetArray.length; ++i) {
                tetArray[i] = Integer.parseInt(tets[i]);
            }

            return tetArray;
        }

        return new int[]{Integer.parseInt(tet)};
    }

    /**
     * Reads a String representing a line corresponding to interval size in a Scale (.scl) file and
     * parses it to a decimal value.
     *
     * The method first determines if the line contains a "." (which indicates the interval is
     * expressed in cents or if the line contains a "/" (which indicates the interval is expressed
     * as a ratio) and calls either the <code>convertCentsToDecimal()</code> or the
     * <code>convertRatioToDecimal</code> respectively.
     *
     *
     * @param line a String representing a line corresponding to interval size in a Scale (.scl) file
     * and parses it to a decimal value.
     *
     * @return Converted decimal (double value) representing the decimal form of an interval
     */
    public static double parseDecimalFromScalaLine(String line) {
        double interval = 0.0;

        // trim leading white-space
        line = line.trim();

        // remove any text other than a double or a ratio value
        if (line.contains(" ")){
            line = line.substring(0, line.indexOf(" "));
        }
        else if (line.contains("!")) {
            line = line.substring(0, line.indexOf("!"));
        }

        // parse intervals, add to scale
        if (line.contains(".")) { // interval is in CENTS
            interval = Music.convertCentsToDecimal(Double.parseDouble(line));
        }
        else if (line.contains("/")) { // interval is a RATIO
            interval = Music.convertRatioToDecimal(line);
        }

        return interval;
    }

    /**
     * Takes a double value representing frequency in Hertz and finds it's approximate pitch-class.
     *
     * The method works by performing a binary recursive search on an array containing the corresponding
     * frequencies in Hertz to a ten-octave spread of the conventional 12-TET system.
     *
     * @param frequencyInHz double value representing frequency in Hertz
     * @return a String value representing an approximate pitch-class corresponding to the passed
     *          frequency in Hertz
     */
    public static String parsePitchClassFromFrequency(double frequencyInHz, double[] freqArray) {

        // Binary search crashes, use iteration instead :(
//        int i = binSearch(frequencyInHz, _12_TET_PITCH_FREQUENCIES,
//                0, _12_TET_PITCH_FREQUENCIES.length);

        int i = -1;
        for (int j = 0; j < freqArray.length; ++i) {
            // finds approx range without going over
            if (frequencyInHz >= freqArray[j]
                    && frequencyInHz < freqArray[j+1]) {
                i = j;
            }
        }

        if (i == -1) {
            return "No pitch detected";
        }

        if (i < 12) {
            return NOTES[i];
        }
        else {
            return NOTES[i % 12];
        }
    }

    // broken for now :(
//    private static int binSearch(double frequencyInHz, double[] freqsArray, int minIdx, int maxIdx) {
//        // base case
//        if (minIdx + 1 >= maxIdx) {
//            return minIdx;
//        }
//
//        // binary search
//        int pivot = (minIdx + (maxIdx - 1) / 2);
//        double midVal = freqsArray[pivot];
//        if (frequencyInHz == midVal) {
//            return pivot;
//        }
//        else if (frequencyInHz < midVal) {
//            return binSearch(frequencyInHz, freqsArray, minIdx, pivot);
//        }
//        else {
//            return binSearch(frequencyInHz, freqsArray, pivot + 1, maxIdx);
//        }
//    }
}
