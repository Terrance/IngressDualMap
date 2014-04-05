package to.uk.terrance.ingressdualmap;

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
     * Get a Unicode character by number.
     * @param code The Unicode code point.
     * @return A string representation of the number.
     */
    public static String unicode(int point) {
        return new String(Character.toChars(point));
    }

}
