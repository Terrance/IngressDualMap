package to.uk.terrance.ingressdualmap;

import to.uk.terrance.ingressdualmap.Portal;

/**
 * Methods available to activities for controlling the {@link LocationService}.
 */
interface ILocationService {
    /**
     * @return <code>True</code> if the notification thread is currently running.
     */
    boolean isThreadRunning();
    /**
     * Start the notification thread.
     */
    void startThread();
    /**
     * Stop the notification thread.
     */
    void stopThread();
    /**
     * @return <code>True</code> if a previous location is available.
     */
    boolean hasLastLocation();
    /**
     * @return The previous location as recorded by the Location service.
     */
    double[] getLastLocation();
    /**
     * Replace all portals stored in the service with the given {@link List}.
     * @param portals A replacement list of portals.
     */
    void setPortals(in java.util.List<Portal> portals);
    /**
     * @param i The index of the portal to find.
     * @return The portal, after being {@link Parcel}'d.
     */
    Portal getPortal(int i);
    /**
     * @return A {@link List} of all currently imported portals.
     */
    java.util.List<Portal> getAllPortals();
    /**
     * Add a new portal to the service.
     * @param portal The new portal (will be {@link Parcel}'d).
     */
    void addPortal(in Portal portal);
    /**
     * Replace a portal in the service with an updated version.
     * @param i The index of the portal to replace.
     * @param portal The replacement portal (will be {@link Parcel}'d).
     */
    void updatePortal(int i, in Portal portal);
    /**
     * Delete multiple portals from the service.
     * @param indexes An array of indexes of portals to delete.
     */
    void removePortals(in int[] indexes);
    /**
     * Refresh the service settings from {@link SharedPreferences}.
     * @param settings An array of integer settings as defined in {@link SettingsFragment#DEFAULTS}.
     * @param filters An array of boolean filters for notifications.
     */
    void refreshSettings(in int[] settings, in boolean[] filters);
}
