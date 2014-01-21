package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
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
    private static Vibrator mVibrator;
    private static ArrayList<Portal> mPortals = new ArrayList<Portal>();
    private UpdateThread mUpdateThread;

    // Two listeners, one for each type of location provider
    IDMLocationListener[] mLocationListeners = new IDMLocationListener[] {
        new IDMLocationListener(LocationManager.GPS_PROVIDER),
        new IDMLocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private class IDMLocationListener implements LocationListener {

        public IDMLocationListener(String provider) {
            Log.d(Utils.TAG, "Initialized " + provider + " provider.");
        }

        @Override
        public void onLocationChanged(Location location) {
            // New location received
            Log.d(Utils.TAG, "Location update: " + location.getLatitude() + ", " + location.getLongitude());
            mLastLocation = location;
            // Refresh distances to each portal
            for (int i = 0; i < mPortals.size(); i++) {
                Portal portal = mPortals.get(i);
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
            // Stop the thread cleanly
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
                    for (int i = 0; i < mPortals.size(); i++) {
                        Portal portal = mPortals.get(i);
                        Float distance = portal.getDistance();
                        // Update notifications
                        notifyPortal(mContext, i, (distance <= 50 || portal.isPinned()));
                        // Handle resonator buzzer
                        if (portal.isResoBuzz() && distance >= 35 && distance <= 40) {
                            mVibrator.vibrate(new long[]{0, 100, 100, 100}, -1);
                        }
                    }
                    // Wait a bit
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
            Portal portal = mPortals.get(i);
            // Notification not yet created (generated as needed)
            if (portal.getNotificationBuilder() == null) {
                // Show menu on click
                Intent optsIntent = new Intent(context, MainActivity.class);
                optsIntent.setAction(Utils.PACKAGE + ".opts." + i);
                NotificationCompat.Builder notif = new NotificationCompat.Builder(context).setOngoing(true)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                    .setContentIntent(PendingIntent.getActivity(context, 0, optsIntent, 0));
                // Quick access notification actions
                Action[] actions = new Action[]{
                    new Action(context.getString(R.string.hack), MainActivity.ACTION_HACK, R.drawable.ic_hack),
                    new Action(context.getString(R.string.reset), MainActivity.ACTION_RESET, R.drawable.ic_reset),
                    new Action(context.getString(R.string.pin_unpin), MainActivity.ACTION_PIN, R.drawable.ic_pin)
                };
                for (Action action : actions) {
                    Intent actionIntent = new Intent(context, MainActivity.class);
                    actionIntent.setAction(Utils.PACKAGE + "." + action.getAction() + "." + i);
                    notif.addAction(action.getDrawable(), action.getLabel(), PendingIntent.getActivity(context, 0, actionIntent, 0));
                }
                portal.setNotificationBuilder(notif);
            }
            // Distance from current location to portal
            float[] distance = new float[1];
            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                     portal.getLatitude(), portal.getLongitude(), distance);
            // Generate notification text
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
            // Show notification
            String icons = (portal.isPinned() ? "\uD83D\uDCCC" : "") + (portal.isResoBuzz() ? "\uD83D\uDD14" : "");
            Notification notif = portal.getNotificationBuilder()
                .setContentTitle((icons.length() > 0 ? icons + " " : "") + portal.getName())
                .setContentText(text).build();
            mNotificationManager.notify(i, notif);
        } else {
            // Hide notification
            mNotificationManager.cancel(i);
        }
    }

    public static void clearNotifs(Context context) {
        initApp(context);
        mNotificationManager.cancelAll();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // Methods exposed outside the service (for MainActivity)
    private final ILocationService.Stub mBinder = new ILocationService.Stub() {
        public boolean isRunning() {
            return mRunning;
        }
        public void setPortals(List<Portal> portals) {
            mPortals.clear();
            mPortals.addAll(portals);
        }
        public Portal getPortal(int i) {
            return mPortals.get(i);
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
        initApp(this);
        // Start notification updater
        mUpdateThread = new UpdateThread(this);
        mUpdateThread.start();
        // Start location updates
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
        initApp(this);
        // Stop notification updater
        if (mUpdateThread != null) {
            mUpdateThread.end();
        }
        super.onDestroy();
        // Stop location updates
        for (int i = 0; i < mLocationListeners.length; i++) {
            mLocationManager.removeUpdates(mLocationListeners[i]);
        }
        // Clear all notifications
        mNotificationManager.cancelAll();
        mRunning = false;
        Log.i(Utils.TAG, "Service is no longer running!");
    }

    public static void initApp(Context context) {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mVibrator == null) {
            mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

}
