package to.uk.terrance.ingressdualmap;

public class Utils {

    public static final String APP_TAG = "IngressDualMap";
    public static final String APP_PACKAGE = "to.uk.terrance.ingressdualmap";
    public static final String URL_LISTS = "http://terrance.uk.to/labs/idm/lists";

    public static String plural(int count) {
        return count == 1 ? "" : "s";
    }

    public static String shortTime(int time) {
        if (time < 60) {
            return time + "sec";
        } else if (time < 60 * 60) {
            return Math.round(time / 60) + "min" + (time % 60) + "sec";
        } else {
            return Math.round(time / (60 * 60)) + "hr" + Math.round((time / 60) % 60) + "min";
        }
    }

}
