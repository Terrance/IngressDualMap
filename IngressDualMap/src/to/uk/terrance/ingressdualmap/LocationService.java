package to.uk.terrance.ingressdualmap;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import android.support.v4.app.NotificationCompat;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/** 
 * Background service to manage displaying notifications for portals.
 */
public class LocationService extends Service {

    private static Location mLastLocation;

    private static PowerManager mPowerManager;
    private static PowerManager.WakeLock mLock;
    private static LocationManager mLocationManager;
    private static NotificationManager mNotificationManager;
    private static Vibrator mVibrator;
    private HashMap<String, Integer> mSettings = new HashMap<String, Integer>();
    private boolean[] mFilters = new boolean[13];
    {
        for (int i = 0; i < mFilters.length; i++) {
            mFilters[i] = true;
        }
    }
    private SharedPreferences mPrefs;
    private List<Portal> mPortals = new ArrayList<Portal>();
    private UpdateThread mUpdateThread;

    // Two listeners, one for each type of location provider
    private IDMLocationListener[] mLocationListeners = new IDMLocationListener[] {
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

    /**
     * A thread to refresh notifications, toggle their visibility, and handle resonator buzz.
     */
    private class UpdateThread extends Thread {

        private boolean mGo = true;

        /**
         * Stop the thread's main loop.
         */
        public void end() {
            mGo = false;
        }

        @Override
        public void run() {
            Log.d(Utils.APP_TAG, "Update thread started.");
            try {
                while (mGo) {
                    for (int i = 0; i < mPortals.size(); i++) {
                        Portal portal = mPortals.get(i);
                        // Show if in range, and not blocked by filters
                        float distance = portal.getDistance();
                        boolean show = distance <= (mSettings.get("notifRange") / 10);
                        show &= mFilters[portal.getAlignment()] && mFilters[4 + portal.getLevel()];
                        show |= portal.isPinned();
                        // Update notifications
                        notifyPortal(i, show);
                        // Handle resonator buzzer
                        if (portal.isResoBuzz() && distance >= (mSettings.get("resoBuzz1") / 10)
                            && distance <= (mSettings.get("resoBuzz2") / 10)) {
                            mVibrator.vibrate(new long[]{0, 100, 100, 100}, -1);
                        }
                    }
                    // Wait a bit
                    sleep(500);
                }
                Log.d(Utils.APP_TAG, "Update thread stopped.");
            } catch (InterruptedException e) {
                Log.d(Utils.APP_TAG, "Update thread interrupted.");
            }
        }

    }

    /**
     * Helper class to represent notification actions.
     */
    private static class Action {

        private int mLabel;
        private String mAction;
        private int mDrawable;

        /**
         * Helper class to represent notification actions.
         * @param label Resource ID for text to display for the action.
         * @param action Actual action to perform, passed to the intent.
         * @param drawable Resource ID for an icon to display with the label.
         */
        public Action(int label, String action, int drawable) {
            mLabel = label;
            mAction = action;
            mDrawable = drawable;
        }

        /**
         * @return The label of the notification action.
         */
        public int getLabel() {
            return mLabel;
        }
        /**
         * @return The action string of the notification action.
         */
        public String getAction() {
            return mAction;
        }
        /**
         * @return The drawable of the notification action.
         */
        public int getDrawable() {
            return mDrawable;
        }

    }

    /**
     * Update the notification for the given portal.
     * @param i The index of the portal to update.
     * @param show <code>True</code> to show the notification in the drawer, <code>False</code> to hide.
     */
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
                    new Action(R.string.hack, MainActivity.ACTION_HACK, R.drawable.ic_hack),
                    new Action(R.string.reset, MainActivity.ACTION_RESET, R.drawable.ic_reset),
                    new Action(R.string.pin, MainActivity.ACTION_PIN, R.drawable.ic_pin)
                };
                for (Action action : actions) {
                    Intent actionIntent = new Intent(this, MainActivity.class);
                    actionIntent.setAction(Utils.APP_PACKAGE + "." + action.getAction() + "." + i);
                    notif.addAction(action.getDrawable(), getString(action.getLabel()),
                            PendingIntent.getActivity(this, 0, actionIntent, 0));
                }
                portal.setNotificationBuilder(notif);
            }
            // Distance from current location to portal
            float[] distance = new float[1];
            Location.distanceBetween(mLastLocation.getLatitude(), mLastLocation.getLongitude(),
                                     portal.getLatitude(), portal.getLongitude(), distance);
            // Generate notification text
            String text = Utils.shortDist(distance[0]) + " away | ";
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
            String icons = (portal.isPinned() ? Utils.unicode(Utils.UNICODE_PIN) : "")
                         + (portal.getKeys() > 0 ? Utils.unicode(Utils.UNICODE_KEY) : "")
                         + (portal.isResoBuzz() ? Utils.unicode(Utils.UNICODE_BELL) : "");
            if (icons.length() > 0 && portal.getAlignment() != Portal.ALIGN_UNDEFINED) {
                icons += " ";
            }
            String brackets = "";
            switch (portal.getAlignment()) {
                case Portal.ALIGN_NEUTRAL:
                    brackets += "N";
                    break;
                case Portal.ALIGN_RESISTANCE:
                    brackets += "R";
                    break;
                case Portal.ALIGN_ENLIGHTENED:
                    brackets += "E";
                    break;
            }
            if (portal.getLevel() > 0) {
                brackets += portal.getLevel();
            }
            if (brackets.length() > 0) {
                icons += "[" + brackets + "]";
            }
            NotificationCompat.Builder builder = portal.getNotificationBuilder()
                .setContentTitle((icons.length() > 0 ? icons + " " : "") + portal.getName())
                .setContentText(text);
            mNotificationManager.notify(i, new NotificationCompat.BigTextStyle(builder).bigText(longText).build());
        } else {
            // Hide notification
            mNotificationManager.cancel(i);
        }
    }

    /**
     * Clear all notifications from the drawer.  If the service is running, active ones will reappear.
     * @param context A {@link Context} to initialize the {@link NotificationManager} with.
     */
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
        @Override
        public boolean isThreadRunning() {
            return mUpdateThread != null;
        }
        @Override
        public void startThread() {
            if (mUpdateThread == null) {
                mUpdateThread = new UpdateThread();
                mUpdateThread.start();
            }
        }
        @Override
        public void stopThread() {
            if (mUpdateThread != null) {
                mUpdateThread.end();
                mUpdateThread = null;
                LocationService.clearNotifs(LocationService.this);
            }
        }
        @Override
        public boolean hasLastLocation() {
            return mLastLocation != null;
        }
        @Override
        public double[] getLastLocation() {
            return new double[]{mLastLocation.getLatitude(), mLastLocation.getLongitude()};
        }
        @Override
        public void setPortals(List<Portal> portals) {
            mPortals.clear();
            mPortals.addAll(portals);
            backup();
        }
        @Override
        public Portal getPortal(int i) {
            return mPortals.get(i);
        }
        @Override
        public List<Portal> getAllPortals() {
            return mPortals;
        }
        @Override
        public void addPortal(Portal portal) {
            mPortals.add(portal);
            backup();
        }
        @Override
        public void updatePortal(int i, Portal portal) {
            // Recycle the notification
            portal.setNotificationBuilder(mPortals.get(i).getNotificationBuilder());
            mPortals.set(i, portal);
            backup();
        }
        @Override
        public void removePortals(int[] indexes) {
            Arrays.sort(indexes);
            for (int i = indexes.length - 1; i >= 0; i--) {
                mPortals.remove(indexes[i]);
            }
            backup();
        }
        @Override
        public void refreshSettings(int[] settings, boolean[] filters) {
            List<String> keys = new ArrayList<String>(SettingsFragment.DEFAULTS.keySet());
            Collections.sort(keys);
            int i = 0;
            for (String key : keys) {
                mSettings.put(key, settings[i]);
                Log.d(Utils.APP_TAG, "notifRange: " + mSettings.get(key));
                i++;
            }
            mFilters = filters;
        }
    };

    /**
     * Backup currently imported portals in case of a forced exit.
     */
    public void backup() {
        File backup = new File(Utils.extStore().getPath() + "/.backup.csv");
        if (mPortals.size() > 0) {
            Log.i(Utils.APP_TAG, "Backing up imported list...");
            try {
                CSVWriter writer = new CSVWriter(new FileWriter(backup));
                for (Portal portal : mPortals) {
                    String[] row = new String[]{
                        portal.getName(), String.valueOf(portal.getLatitude()), String.valueOf(portal.getLongitude()),
                        String.valueOf(portal.getAlignment()), String.valueOf(portal.getLevel()), String.valueOf(portal.getKeys())
                    };
                    writer.writeNext(row);
                }
                writer.close();
                Log.i(Utils.APP_TAG, "List backed up.");
            } catch (IOException e) {
                Log.e(Utils.APP_TAG, "Unable to write backup list.", e);
            }
        } else if (backup.exists()) {
            backup.delete();
        }
    }

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
        // Load settings
        mPrefs = getSharedPreferences("settings", SettingsFragment.PREFS_MODE);
        for (String key : SettingsFragment.DEFAULTS.keySet()) {
            mSettings.put(key, mPrefs.getInt(key, SettingsFragment.DEFAULTS.get(key)));
        }
        // Acquire wake lock
        mLock.acquire();
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
        // Restore backup if one existing
        File folder = Utils.extStore();
        File backup = new File(folder.getPath() + "/.backup.csv");
        if (backup.exists()) {
            Log.i(Utils.APP_TAG, "Restoring backed up list...");
            try {
                CSVReader reader = new CSVReader(new FileReader(backup));
                while (true) {
                    String[] row = reader.readNext();
                    if (row == null) {
                        break;
                    }
                    Portal portal = new Portal(row[0], Double.valueOf(row[1]), Double.valueOf(row[2]));
                    portal.setAlignment(Integer.valueOf(row[3]));
                    portal.setLevel(Integer.valueOf(row[4]));
                    portal.setKeys(Integer.valueOf(row[5]));
                    mPortals.add(portal);
                }
                reader.close();
                Log.i(Utils.APP_TAG, "List restored from backup.");
            } catch (IOException e) {
                Log.e(Utils.APP_TAG, "Unable to read backup list.", e);
            }
        }
        Log.i(Utils.APP_TAG, "Service is now running!");
    }

    @Override
    public void onDestroy() {
        Log.i(Utils.APP_TAG, "Stopping location service...");
        initApp(this);
        // Release wake lock
        mLock.release();
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
        Log.i(Utils.APP_TAG, "Service is no longer running!");
    }

    /**
     * Initialize the {@link PowerManager}, {@link LocationManager}, {@link NotificationManager} and {@link Vibrator} services.
     * @param context A {@link Context} to initialize components with.
     */
    public static void initApp(Context context) {
        if (mPowerManager == null) {
            mPowerManager = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        }
        if (mLock == null) {
            mLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, Utils.APP_TAG);
            mLock.setReferenceCounted(false);
        }
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
