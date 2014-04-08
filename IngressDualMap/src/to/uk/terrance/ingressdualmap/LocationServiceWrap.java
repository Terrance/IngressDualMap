package to.uk.terrance.ingressdualmap;

import java.util.List;

import android.os.RemoteException;
import android.util.Log;

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
     * Check if the {@link ILocationService} has been set.
     */
    public boolean isSet() {
        return mLocationService != null;
    }

    /**
     * Wrapper for {@link ILocationService#isThreadRunning} to handle service exceptions.
     */
    public boolean isThreadRunning() {
        try {
            // Test if the service is bound and running
            return mLocationService != null && mLocationService.isThreadRunning();
        } catch (RemoteException e) {
            Log.e(Utils.APP_TAG, "Remote exception.", e);
            return false;
        }
    }

    /**
     * Wrapper for {@link ILocationService#startThread} to handle service exceptions.
     */
    public void startThread() {
        if (mLocationService != null) {
            try {
                mLocationService.startThread();
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
            }
        }
    }

    /**
     * Wrapper for {@link ILocationService#stopThread} to handle service exceptions.
     */
    public void stopThread() {
        if (mLocationService != null) {
            try {
                mLocationService.stopThread();
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
            }
        }
    }

    /**
     * Wrapper for {@link ILocationService#hasLastLocation} to handle service exceptions.
     */
    public boolean hasLastLocation() {
        if (mLocationService != null) {
            try {
                return mLocationService.hasLastLocation();
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Wrapper for {@link ILocationService#getLastLocation} to handle service exceptions.
     */
    public double[] getLastLocation() {
        if (mLocationService != null) {
            try {
                return mLocationService.getLastLocation();
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Wrapper for {@link ILocationService#setPortals} to handle service exceptions.
     */
    public void setPortals(List<Portal> portals) {
        if (mLocationService != null) {
            try {
                mLocationService.setPortals(portals);
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
            }
        }
    }

    /**
     * Wrapper for {@link ILocationService#getPortal} to handle service exceptions.
     */
    public Portal getPortal(int i) {
        if (mLocationService != null) {
            try {
                return mLocationService.getPortal(i);
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
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
                return mLocationService.getAllPortals();
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Wrapper for {@link ILocationService#addPortal} to handle service exceptions.
     */
    public void addPortal(Portal portal) {
        if (mLocationService != null) {
            try {
                mLocationService.addPortal(portal);
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
            }
        }
    }

    /**
     * Wrapper for {@link ILocationService#updatePortal} to handle service exceptions.
     */
    public void updatePortal(int i, Portal portal) {
        if (mLocationService != null) {
            try {
                mLocationService.updatePortal(i, portal);
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
            }
        }
    }

    /**
     * Wrapper for {@link ILocationService#removePortals} to handle service exceptions.
     */
    public void removePortals(int[] indexes) {
        if (mLocationService != null) {
            try {
                mLocationService.removePortals(indexes);
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
            }
        }
    }

    /**
     * Wrapper for {@link ILocationService#refreshSettings} to handle service exceptions.
     */
    public void refreshSettings(int[] settings, boolean[] filters) {
        if (mLocationService != null) {
            try {
                mLocationService.refreshSettings(settings, filters);
            } catch (RemoteException e) {
                Log.e(Utils.APP_TAG, "Remote exception.", e);
            }
        }
    }

}
