package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;

import android.os.RemoteException;

/**
 * Wrapper for {@link ILocationService} to handle service exceptions.
 */
public class LocationServiceWrap {

    private ILocationService mLocationService;

    public void set(ILocationService locationService) {
        mLocationService = locationService;
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
    public void setPortals(ArrayList<Portal> portals) {
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

}
