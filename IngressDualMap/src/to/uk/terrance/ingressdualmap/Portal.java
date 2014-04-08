package to.uk.terrance.ingressdualmap;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

import android.support.v4.app.NotificationCompat.Builder;

/**
 * Represents a portal on the map.
 */
public class Portal implements Parcelable {

    /**
     * Default portal alignment when not set by the user.
     */
    public static final int ALIGN_UNDEFINED = 0;
    /**
     * Portal currently does not belong to either team.
     */
    public static final int ALIGN_NEUTRAL = 1;
    /**
     * Portal belongs to the Resistance.
     */
    public static final int ALIGN_RESISTANCE = 2;
    /**
     * Portal belongs to the Enlightened.
     */
    public static final int ALIGN_ENLIGHTENED = 3;

    private String mName;
    private double mLatitude;
    private double mLongitude;
    private int mAlignment;
    private int mLevel;
    private int mKeys;
    private int mHacksRemaining;
    private float mDistance = Float.MAX_VALUE;
    private boolean mPinned = false;
    private boolean mResoBuzz = false;
    private Builder mNotificationBuilder = null;

    private Calendar mHackReset = null;
    private Calendar mBurnoutReset = null;

    /**
     * Create a new portal with the given name and position, and default options.
     * @param name
     * @param latitude
     * @param longitude
     */
    public Portal(String name, double latitude, double longitude) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
        mAlignment = ALIGN_UNDEFINED;
        mLevel = 0;
        mKeys = 0;
        mHacksRemaining = 4;
    }

    /**
     * @return The portal's name as defined in a portal list.
     */
    public String getName() {
        return mName;
    }
    /**
     * Update the name of the portal.
     */
    public void setName(String name) {
        mName = name;
    }
    /**
     * @return The first coordinate of the portal's location.
     */
    public double getLatitude() {
        return mLatitude;
    }
    /**
     * Update the first coordinate of the portal's location.
     */
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }
    /**
     * @return The second coordinate of the portal's location.
     */
    public double getLongitude() {
        return mLongitude;
    }
    /**
     * Update the second coordinate of the portal's location.
     */
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
    /**
     * @return The portal's current alignment (one of {@link #ALIGN_UNDEFINED},
     *      {@link #ALIGN_NEUTRAL}, {@link #ALIGN_RESISTANCE} or {@link #ALIGN_ENLIGHTENED}).
     */
    public int getAlignment() {
        return mAlignment;
    }
    /**
     * Update the portal's current alignment.
     * @param alignment One of {@link #ALIGN_UNDEFINED}, {@link #ALIGN_NEUTRAL},
     *      {@link #ALIGN_RESISTANCE} or {@link #ALIGN_ENLIGHTENED}.
     */
    public void setAlignment(int alignment) {
        mAlignment = alignment;
    }
    /**
     * @return The portal's current level.
     */
    public int getLevel() {
        return mLevel;
    }
    /**
     * Update the portal's current level.
     * @param level Between 1 and 8.
     */
    public void setLevel(int level) {
        mLevel = level;
    }
    /**
     * @return The number of keys held for this portal.
     */
    public int getKeys() {
        return mKeys;
    }
    /**
     * Update the number of keys held for this portal.
     */
    public void setKeys(int keys) {
        mKeys = keys;
    }
    /**
     * @return The number of hacks that can be done before burnout.
     */
    public int getHacksRemaining() {
        return mHacksRemaining;
    }
    /**
     * Update the number of hacks that can be done before burnout.
     */
    public void setHacksRemaining(int hacksRemaining) {
        mHacksRemaining = hacksRemaining;
    }
    /**
     * @return The user's last known distance from the portal.
     */
    public float getDistance() {
        return mDistance;
    }
    /**
     * Update the user's last known distance from the portal.
     */
    public void setDistance(float distance) {
        mDistance = distance;
    }
    /**
     * @return <code>True</code> if the portal's notification is pinned.
     */
    public boolean isPinned() {
        return mPinned;
    }
    /**
     * Sets the portal's notification to be pinned.
     */
    public void setPinned(boolean pinned) {
        mPinned = pinned;
    }
    /**
     * @return <code>True</code> if resonator buzz is enabled for this portal.
     */
    public boolean isResoBuzz() {
        return mResoBuzz;
    }
    /**
     * Sets resonator buzz enabled or disabled for this portal.
     */
    public void setResoBuzz(boolean resoBuzz) {
        mResoBuzz = resoBuzz;
    }
    /**
     * @return The {@link Builder} instance for making the portal notification.
     */
    public Builder getNotificationBuilder() {
        return mNotificationBuilder;
    }
    /**
     * Update the {@link Builder} instance for making the portal notification.
     */
    public void setNotificationBuilder(Builder notificationBuilder) {
        mNotificationBuilder = notificationBuilder;
    }

    /**
     * Test if the portal is running hot by comparing the current time to the reset time.
     * @return <code>0</code> if not running hot, otherwise the number of seconds left.
     */
    public int checkRunningHot() {
        if (mHackReset == null) {
            return 0;
        }
        Calendar now = Calendar.getInstance();
        long diff = mHackReset.getTimeInMillis() - now.getTimeInMillis();
        if (diff < 0) {
            mHackReset = null;
            return 0;
        } else {
            return Math.round(diff / 1000);
        }
    }
    /**
     * Mark the portal as running hot, setting the reset time to 5 minutes from now.
     */
    public void setRunningHot() {
        mHackReset = Calendar.getInstance();
        mHackReset.set(Calendar.MINUTE, mHackReset.get(Calendar.MINUTE) + 5);
    }
    /**
     * Test if the portal is burned out by comparing the current time to the reset time.
     * @return <code>0</code> if not burned out, otherwise the number of seconds left.
     */
    public int checkBurnedOut() {
        if (mBurnoutReset == null) {
            return 0;
        }
        Calendar now = Calendar.getInstance();
        long diff = mBurnoutReset.getTimeInMillis() - now.getTimeInMillis();
        if (diff < 0) {
            mHacksRemaining = 4;
            mBurnoutReset = null;
            return 0;
        } else {
            return Math.round(diff / 1000);
        }
    }
    /**
     * Mark the portal as burned out, setting the reset time to 4 hours from now.
     */
    public void setBurnedOut() {
        mHacksRemaining = 0;
        mBurnoutReset = Calendar.getInstance();
        mBurnoutReset.set(Calendar.HOUR, mBurnoutReset.get(Calendar.HOUR) + 4);
    }
    /**
     * Clear the running hot and burned out timers, and reset the hack counter to 4.
     */
    public void reset() {
        mHacksRemaining = 4;
        mHackReset = null;
        mBurnoutReset = null;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[name=" + mName + ", lat=" + mLatitude + ", lng=" + mLongitude + ", hacks=" + mHacksRemaining + "]";
    }

    /**
     * Reconstruct a portal from a {@link Parcel}.
     * @param src A parcel of the original portal.
     */
    public Portal(Parcel src) {
        mName = src.readString();
        mLatitude = src.readDouble();
        mLongitude = src.readDouble();
        mAlignment = src.readInt();
        mLevel = src.readInt();
        mKeys = src.readInt();
        mHacksRemaining = src.readInt();
        mDistance = src.readFloat();
        mPinned = src.readInt() == 1;
        mResoBuzz = src.readInt() == 1;
        long hackReset = src.readLong();
        if (hackReset != -1) {
            mHackReset = Calendar.getInstance();
            mHackReset.setTimeInMillis(hackReset);
        }
        long burnoutReset = src.readLong();
        if (burnoutReset != -1) {
            mBurnoutReset = Calendar.getInstance();
            mBurnoutReset.setTimeInMillis(burnoutReset);
        }
    }

    public static final Parcelable.Creator<Portal> CREATOR = new Parcelable.Creator<Portal>() {
        public Portal createFromParcel(Parcel src) {
            return new Portal(src);
        }
        public Portal[] newArray(int size) {
            return new Portal[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
        dest.writeInt(mAlignment);
        dest.writeInt(mLevel);
        dest.writeInt(mKeys);
        dest.writeInt(mHacksRemaining);
        dest.writeFloat(mDistance);
        dest.writeInt(mPinned ? 1 : 0);
        dest.writeInt(mResoBuzz ? 1 : 0);
        dest.writeLong(mHackReset == null ? -1 : mHackReset.getTimeInMillis());
        dest.writeLong(mBurnoutReset == null ? -1 : mBurnoutReset.getTimeInMillis());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Portal)) {
            return false;
        }
        Portal other = (Portal) obj;
        return mName == other.getName() && mLatitude == other.getLatitude() && mLongitude == other.getLongitude();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (mName == null ? 0 : mName.hashCode());
        hash = 31 * hash + (int) (Double.doubleToLongBits(mLatitude));
        hash = 31 * hash + (int) (Double.doubleToLongBits(mLongitude));
        return hash;
    }

}
