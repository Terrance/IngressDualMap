package to.uk.terrance.ingressdualmap;

import java.util.List;

import android.os.RemoteException;

/**
 * Wrapper for {@link ILocationService} to handle service exceptions.
 */
public class LocationServiceWrap {

    private ILocationService mLocationService;

    /**
     * Update the pointer to the raw {@link ILocationService} object.
     */
    public void set(ILocationService locationService) {
        mLocationService = locationService;
    }

    /**
     * Return the pointer to the raw {@link ILocationService} object.
     */
    public ILocationService get() {
        return mLocationService;
    }

    /**
     * Wrapper for {@link ILocationService#isRunning} to handle service exceptions.
     */
    public boolean isRunning() {
        try {
            // Test if the service is bound and running
            return mLocationService != null && mLocationService.isRunning();
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Wrapper for {@link ILocationService#setPortals} to handle service exceptions.
     */
    public void setPortals(List<Portal> portals) {
        if (mLocationService != null) {
            try {
                // Update the service portal list
                mLocationService.setPortals(portals);
            } catch (RemoteException e) {}
        }
    }

    /**
     * Wrapper for {@link ILocationService#getPortal} to handle service exceptions.
     */
    public Portal getPortal(int i) {
        if (mLocationService != null) {
            try {
                // Fetch a portal from the list
                return mLocationService.getPortal(i);
            } catch (RemoteException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Wrapper for {@link ILocationService#getAllPortals} to handle service exceptions.
     */
    public List<Portal> getAllPortals() {
        if (mLocationService != null) {
            try {
                // Fetch a portal from the list
                return mLocationService.getAllPortals();
            } catch (RemoteException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Wrapper for {@link ILocationService#updatePortal} to handle service exceptions.
     */
    public void updatePortal(int i, Portal portal) {
        if (mLocationService != null) {
            try {
                // Update a portal in the service
                mLocationService.updatePortal(i, portal);
            } catch (RemoteException e) {}
        }
    }

    /**
     * Wrapper for {@link ILocationService#notifyPortal} to handle service exceptions.
     */
    public void notifyPortal(int i) {
        if (mLocationService != null) {
            try {
                // Refresh the notification
                mLocationService.notifyPortal(i);
            } catch (RemoteException e) {}
        }
    }

    /**
     * Wrapper for {@link ILocationService#refreshSettings} to handle service exceptions.
     * @param settings
     * @param filters 
     */
    public void refreshSettings(int[] settings, boolean[] filters) {
        if (mLocationService != null) {
            try {
                // Refresh preferences
                mLocationService.refreshSettings(settings, filters);
            } catch (RemoteException e) {}
        }
    }

}
