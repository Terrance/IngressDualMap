package to.uk.terrance.ingressdualmap;

import java.io.File;
import java.text.DecimalFormat;

import android.os.Environment;

/**
 * Miscellaneous utility and helper functions.
 */
public class Utils {

    /**
     * Logging tag for use with {@link android.util.Log}.
     */
    public static final String APP_TAG = "IngressDualMap";
    /**
     * Package name to prefix to intent actions.
     */
    public static final String APP_PACKAGE = "to.uk.terrance.ingressdualmap";
    /**
     * Address of the server to query for downloadable portal lists.
     */
    public static final String URL_LISTS = "http://idm.uk.to/lists";

    /**
     * Colours for portal levels.
     */
    public static final String[] COLOUR_LEVEL = new String[]{
        null, "#fece5a", "#ffa630", "#ff7315", "#e40000", "#fd2992", "#eb26cd", "#c124e0", "#9627f4"
    };
    /**
     * Colours for portal alignment.
     */
    public static final String[] COLOUR_ALIGNMENT = new String[]{
        null, "#808080", "#0491D0", "#01BF01"
    };

    /**
     * Helper method for easily writing plurals if needed.
     * @param count Number of "things" to consider.
     * @return An empty string if only one, otherwise <code>"s"</code> to be plural.
     */
    public static String plural(int count) {
        return count == 1 ? "" : "s";
    }

    /**
     * Output a short timestamp in terms of hours, minutes or seconds as required.
     * @param time The time to represent, in seconds.
     * @return A string representation with appropriate units.
     */
    public static String shortTime(int time) {
        if (time < 60) {
            return time + "sec";
        } else if (time < 60 * 60) {
            return Math.round(time / 60) + "min" + (time % 60) + "sec";
        } else {
            return Math.round(time / (60 * 60)) + "hr" + Math.round((time / 60) % 60) + "min";
        }
    }

    /**
     * Output a short distance in terms of metres or kilometres as required.
     * @param dist The distance to represent, in metres.
     * @return A string representation with appropriate units.
     */
    public static String shortDist(double dist) {
        if (dist < 100) {
            return new DecimalFormat("0.0").format(dist) + "m";
        } else if (dist < 1000) {
            return new DecimalFormat("0").format(dist) + "m";
        } else {
            return new DecimalFormat("0.00").format(dist / 1000) + "km";
        }
    }
    
    /**
     * Get a Unicode character by number.
     * @param code The Unicode code point.
     * @return A string representation of the number.
     */
    public static String unicode(int point) {
        return new String(Character.toChars(point));
    }
    
    /**
     * Create and return the external storage folder
     * @return A {@link File} object for the designated storage location.
     */
    public static File extStore() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/IngressDualMap");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

}
