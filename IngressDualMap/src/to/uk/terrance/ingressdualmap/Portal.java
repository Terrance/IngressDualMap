package to.uk.terrance.ingressdualmap;

import java.util.Calendar;

import android.os.Parcel;
import android.os.Parcelable;

import android.support.v4.app.NotificationCompat.Builder;

public class Portal implements Parcelable {

    private String mName;
    private double mLatitude;
    private double mLongitude;
    private int mKeys;
    private int mHacksRemaining;
    private float mDistance = Float.MAX_VALUE;
    private boolean mPinned = false;
    private boolean mResoBuzz = false;
    private Builder mNotificationBuilder = null;

    private Calendar mHackReset = null;
    private Calendar mBurnoutReset = null;

    public Portal(String name, double latitude, double longitude) {
        mName = name;
        mHacksRemaining = 4;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }
    public double getLatitude() {
        return mLatitude;
    }
    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }
    public float getDistance() {
        return mDistance;
    }
    public void setDistance(float distance) {
        mDistance = distance;
    }
    public int getHacksRemaining() {
        return mHacksRemaining;
    }
    public void setHacksRemaining(int hacksRemaining) {
        mHacksRemaining = hacksRemaining;
    }
    public int getKeys() {
        return mKeys;
    }
    public void setKeys(int keys) {
        mKeys = keys;
    }
    public boolean isPinned() {
        return mPinned;
    }
    public void setPinned(boolean pinned) {
        mPinned = pinned;
    }
    public boolean isResoBuzz() {
        return mResoBuzz;
    }
    public void setResoBuzz(boolean resoBuzz) {
        mResoBuzz = resoBuzz;
    }
    public Builder getNotificationBuilder() {
        return mNotificationBuilder;
    }
    public void setNotificationBuilder(Builder notificationBuilder) {
        mNotificationBuilder = notificationBuilder;
    }

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
    public void setRunningHot() {
        mHackReset = Calendar.getInstance();
        mHackReset.set(Calendar.MINUTE, mHackReset.get(Calendar.MINUTE) + 5);
    }
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
    public void setBurnedOut() {
        mHacksRemaining = 0;
        mBurnoutReset = Calendar.getInstance();
        mBurnoutReset.set(Calendar.HOUR, mBurnoutReset.get(Calendar.HOUR) + 4);
    }
    public void reset() {
        mHacksRemaining = 4;
        mHackReset = null;
        mBurnoutReset = null;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[name=" + mName + ", lat=" + mLatitude + ", lng=" + mLongitude + ", hacks=" + mHacksRemaining + "]";
    }

    public Portal(Parcel src) {
        mName = src.readString();
        mLatitude = src.readDouble();
        mLongitude = src.readDouble();
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
