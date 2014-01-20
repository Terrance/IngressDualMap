package to.uk.terrance.ingressdualmap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import android.support.v4.app.NotificationCompat;

public class LocationService extends Service {

    private static Location mLastLocation;

    public static Location getLastLocation() {
        return mLastLocation;
    }

    private boolean mRunning = false;
    private static LocationManager mLocationManager;
    private static NotificationManager mNotificationManager;
    private UpdateThread mUpdateThread;

    IDMLocationListener[] mLocationListeners = new IDMLocationListener[] {
        new IDMLocationListener(LocationManager.GPS_PROVIDER, true),
        new IDMLocationListener(LocationManager.NETWORK_PROVIDER, false)
    };

    private class IDMLocationListener implements LocationListener {

        public IDMLocationListener(String provider, boolean enabled) {
            Log.d(Utils.TAG, "Initialized " + provider + " provider.");
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(Utils.TAG, "Location update: " + location.getLatitude() + ", " + location.getLongitude());
            mLastLocation = location;
            for (int i = 0; i < Portal.PORTALS.size(); i++) {
                Portal portal = Portal.PORTALS.get(i);
                float[] distance = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                                         portal.getLatitude(), portal.getLongitude(), distance);
                portal.setDistance(distance[0]);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(Utils.TAG, "Provider disabled for " + provider + ".");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(Utils.TAG, "Provider enabled for " + provider + ".");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(Utils.TAG, "Provider status change for " + provider + ": " + status);
        }

    }

    private class UpdateThread extends Thread {

        private Context mContext = null;
        private boolean mGo = true;
    
        public void end() {
            mGo = false;
        }

        public UpdateThread(Context context) {
            mContext = context;
        }
        @Override
        public void run() {
            Log.d(Utils.TAG, "Update thread started.");
            try {
                while (mGo) {
                    for (int i = 0; i < Portal.PORTALS.size(); i++) {
                        Portal portal = Portal.PORTALS.get(i);
                        notifyPortal(mContext, i, (portal.getDistance() <= 50 || portal.isPinned()));
                    }
                    sleep(500);
                }
            } catch (InterruptedException e) {
                Log.d(Utils.TAG, "Update thread interrupted.");
            }
        }

    }

    private static class Action {

        private String mLabel;
        private String mAction;
        private int mDrawable;

        public Action(String label, String action, int drawable) {
            mLabel = label;
            mAction = action;
            mDrawable = drawable;
        }

        public String getLabel() {
            return mLabel;
        }
        public String getAction() {
            return mAction;
        }
        public int getDrawable() {
            return mDrawable;
        }

    }

    public static void notifyPortal(Context context, int i, boolean show) {
        if (show) {
            Portal portal = Portal.PORTALS.get(i);
            if (portal.getNotificationBuilder() == null) {
                Intent optsIntent = new Intent(context, NotificationActivity.class);
                optsIntent.setAction(Utils.PACKAGE + ".opts." + i);
                NotificationCompat.Builder notif = new NotificationCompat.Builder(context)
                    .setOngoing(true).setContentTitle(portal.getName()).setSmallIcon(R.drawable.ic_notif)
                    .setContentIntent(PendingIntent.getActivity(context, 0, optsIntent, 0));
                Action[] actions = new Action[]{
                    new Action("Hack", NotificationActivity.ACTION_HACK, R.drawable.ic_hack),
                    new Action("Reset", NotificationActivity.ACTION_RESET, R.drawable.ic_reset)
                };
                for (Action action : actions) {
                    Intent actionIntent = new Intent(context, NotificationActivity.class);
                    actionIntent.setAction(Utils.PACKAGE + "." + action.getAction() + "." + i);
                    notif.addAction(action.getDrawable(), action.getLabel(), PendingIntent.getActivity(context, 0, actionIntent, 0));
                }
                portal.setNotificationBuilder(notif);
            }
            float[] distance = new float[1];
            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                     portal.getLatitude(), portal.getLongitude(), distance);
            String text = Math.round(distance[0]) + "m away | ";
            int burnedOutTime = portal.checkBurnedOut();
            if (burnedOutTime > 0) {
                text += "burned out (wait " + Utils.shortTime(burnedOutTime) + ")";
            } else {
                int hacks = portal.getHacksRemaining();
                text += hacks + " more hack" + Utils.plural(hacks);
                int runningHotTime = portal.checkRunningHot();
                if (runningHotTime > 0) {
                    text += " (wait " + Utils.shortTime(runningHotTime) + ")";
                }
            }
            Notification notif = portal.getNotificationBuilder().setContentText(text).build();
            mNotificationManager.notify(i, notif);
        } else {
            mNotificationManager.cancel(i);
        }
    }

    public static void clearNotifs() {
        mNotificationManager.cancelAll();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final ILocationService.Stub mBinder = new ILocationService.Stub() {
        public boolean isRunning() {
            return mRunning;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Utils.TAG, "Service start command called!");
        super.onStartCommand(intent, flags, startId);       
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(Utils.TAG, "Starting location service...");
        initApp();
        mUpdateThread = new UpdateThread(this);
        mUpdateThread.start();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListeners[0]);
        } catch (java.lang.SecurityException e) {
            Log.e(Utils.TAG, "GPS location permission unavailable.", e);
        } catch (IllegalArgumentException e) {
            Log.w(Utils.TAG, "GPS location provider unavailable.");
        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListeners[1]);
        } catch (java.lang.SecurityException e) {
            Log.e(Utils.TAG, "Network location permission unavailable.", e);
        } catch (IllegalArgumentException e) {
            Log.w(Utils.TAG, "Network location provider unavailable.");
        }
        mRunning = true;
        Log.i(Utils.TAG, "Service is now running!");
    }

    @Override
    public void onDestroy() {
        Log.i(Utils.TAG, "Stopping location service...");
        initApp();
        if (mUpdateThread != null) {
            mUpdateThread.end();
        }
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                mLocationManager.removeUpdates(mLocationListeners[i]);
            }
        }
        mNotificationManager.cancelAll();
        mRunning = false;
        Log.i(Utils.TAG, "Service is no longer running!");
    }

    public void initApp() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

}
