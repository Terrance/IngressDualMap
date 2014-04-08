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
     * Unicode symbol for lightning.
     */
    public static final int UNICODE_BOLT = 0x26A1;
    /**
     * Unicode symbol for up arrow.
     */
    public static final int UNICODE_UP = 0x2B06;
    /**
     * Unicode symbol for tick.
     */
    public static final int UNICODE_CHECK = 0x2714;
    /**
     * Unicode symbol for pushpin.
     */
    public static final int UNICODE_PIN = 0x1F4CC;
    /**
     * Unicode symbol for key.
     */
    public static final int UNICODE_KEY = 0x1F511;
    /**
     * Unicode symbol for alert bell.
     */
    public static final int UNICODE_BELL = 0x1F514;
    /**
     * Unicode symbol for fire.
     */
    public static final int UNICODE_FIRE = 0x1F514;
    /**
     * Unicode symbol for clock.
     */
    public static final int UNICODE_CLOCK = 0x1F551;
    /**
     * Unicode symbol for not allowed.
     */
    public static final int UNICODE_NO = 0x1F6AB;

    /**
     * Colour for keys.
     */
    public static final String COLOUR_KEYS = "#f7d34a";
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
        null, "#A0A0A0", "#0491D0", "#01BF01"
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
            return Math.round(time / 60) + "min " + (time % 60) + "sec";
        } else {
            return Math.round(time / (60 * 60)) + "hr " + Math.round((time / 60) % 60) + "min";
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
