package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;
import java.util.List;

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
import android.os.RemoteException;
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
            Log.d(Utils.APP_TAG, "Initialized " + provider + " provider.");
        }

        @Override
        public void onLocationChanged(Location location) {
            // New location received
            Log.d(Utils.APP_TAG, "Location update: " + location.getLatitude() + ", " + location.getLongitude());
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
            Log.i(Utils.APP_TAG, "Provider disabled for " + provider + ".");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(Utils.APP_TAG, "Provider enabled for " + provider + ".");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(Utils.APP_TAG, "Provider status change for " + provider + ": " + status);
        }

    }

    private class UpdateThread extends Thread {

        private boolean mGo = true;

        public void end() {
            // Stop the thread cleanly
            mGo = false;
        }

        @Override
        public void run() {
            Log.d(Utils.APP_TAG, "Update thread started.");
            try {
                while (mGo) {
                    for (int i = 0; i < mPortals.size(); i++) {
                        Portal portal = mPortals.get(i);
                        Float distance = portal.getDistance();
                        // Update notifications
                        notifyPortal(i, (distance <= 50 || portal.isPinned()));
                        // Handle resonator buzzer
                        if (portal.isResoBuzz() && distance >= 35 && distance <= 40) {
                            mVibrator.vibrate(new long[]{0, 100, 100, 100}, -1);
                        }
                    }
                    // Wait a bit
                    sleep(500);
                }
            } catch (InterruptedException e) {
                Log.d(Utils.APP_TAG, "Update thread interrupted.");
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

    public void notifyPortal(int i, boolean show) {
        if (show) {
            Portal portal = mPortals.get(i);
            // Notification not yet created (generated as needed)
            if (portal.getNotificationBuilder() == null) {
                // Show menu on click
                Intent optsIntent = new Intent(this, MainActivity.class);
                optsIntent.setAction(Utils.APP_PACKAGE + ".opts." + i);
                NotificationCompat.Builder notif = new NotificationCompat.Builder(this).setOngoing(true)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                    .setContentIntent(PendingIntent.getActivity(this, 0, optsIntent, 0));
                // Quick access notification actions
                Action[] actions = new Action[]{
                    new Action(getString(R.string.hack), MainActivity.ACTION_HACK, R.drawable.ic_hack),
                    new Action(getString(R.string.reset), MainActivity.ACTION_RESET, R.drawable.ic_reset),
                    new Action(getString(R.string.pin), MainActivity.ACTION_PIN, R.drawable.ic_pin)
                };
                for (Action action : actions) {
                    Intent actionIntent = new Intent(this, MainActivity.class);
                    actionIntent.setAction(Utils.APP_PACKAGE + "." + action.getAction() + "." + i);
                    notif.addAction(action.getDrawable(), action.getLabel(), PendingIntent.getActivity(this, 0, actionIntent, 0));
                }
                portal.setNotificationBuilder(notif);
            }
            // Distance from current location to portal
            float[] distance = new float[1];
            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                     portal.getLatitude(), portal.getLongitude(), distance);
            // Generate notification text
            String text = Math.round(distance[0]) + "m away | ";
            int hacks = portal.getHacksRemaining();
            text += hacks + " more hack" + Utils.plural(hacks);
            String longText = text;
            int keys = portal.getKeys();
            if (keys > 0) {
                longText += " | " + keys + " key" + Utils.plural(keys);
            }
            int burnedOutTime = portal.checkBurnedOut();
            if (burnedOutTime > 0) {
                String time = Utils.shortTime(burnedOutTime);
                longText += "\nBurned out: wait " + time;
                text += " | " + time;
            } else {
                int runningHotTime = portal.checkRunningHot();
                if (runningHotTime > 0) {
                    String time = Utils.shortTime(runningHotTime);
                    longText += "\nRunning hot: wait " + time;
                    text += " | " + time;
                }
            }
            // Show notification
            String icons = (portal.isPinned() ? "\uD83D\uDCCC" : "")
                         + (portal.getKeys() > 0 ? "\uD83D\uDD11" : "")
                         + (portal.isResoBuzz() ? "\uD83D\uDD14" : "");
            NotificationCompat.Builder builder = portal.getNotificationBuilder()
                .setContentTitle((icons.length() > 0 ? icons + " " : "") + portal.getName())
                .setContentText(text);
            mNotificationManager.notify(i, new NotificationCompat.BigTextStyle(builder).bigText(longText).build());
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
        @Override
        public void updatePortal(int i, Portal portal) {
            // Recycle the notification
            portal.setNotificationBuilder(mPortals.get(i).getNotificationBuilder());
            mPortals.set(i, portal);
        }
        @Override
        public void notifyPortal(int i) throws RemoteException {
            Portal portal = mPortals.get(i);
            LocationService.this.notifyPortal(i, portal.getDistance() <= 50 || portal.isPinned());
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Utils.APP_TAG, "Service start command called!");
        super.onStartCommand(intent, flags, startId);       
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(Utils.APP_TAG, "Starting location service...");
        initApp(this);
        // Start notification updater
        mUpdateThread = new UpdateThread();
        mUpdateThread.start();
        // Start location updates
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListeners[0]);
        } catch (java.lang.SecurityException e) {
            Log.e(Utils.APP_TAG, "GPS location permission unavailable.", e);
        } catch (IllegalArgumentException e) {
            Log.w(Utils.APP_TAG, "GPS location provider unavailable.");
        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListeners[1]);
        } catch (java.lang.SecurityException e) {
            Log.e(Utils.APP_TAG, "Network location permission unavailable.", e);
        } catch (IllegalArgumentException e) {
            Log.w(Utils.APP_TAG, "Network location provider unavailable.");
        }
        mRunning = true;
        Log.i(Utils.APP_TAG, "Service is now running!");
    }

    @Override
    public void onDestroy() {
        Log.i(Utils.APP_TAG, "Stopping location service...");
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
        clearNotifs(this);
        mRunning = false;
        Log.i(Utils.APP_TAG, "Service is no longer running!");
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
