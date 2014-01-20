package to.uk.terrance.ingressdualmap;

import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.KeyEvent;
import android.widget.Toast;

public class MainActivity extends Activity {

    private boolean mBound = false;
    private AlertDialog mMenu;
    private ServiceConnection mConnection;
    private ILocationService mLocationService;

    @Override
    public void onResume() {
        super.onResume();
        showMenu();
        connectService();
    }

    @Override
    public void onPause() {
        super.onPause();
        hideMenu();
        disconnectService();
    }

    public void connectService() {
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                Toast.makeText(MainActivity.this, "Connected!", Toast.LENGTH_SHORT).show();
                mLocationService = ILocationService.Stub.asInterface(service);
                showMenu();
            }
            public void onServiceDisconnected(ComponentName className) {
                mLocationService = null;
                showMenu();
                connectService();
            }
        };
        mBound = bindService(new Intent(this, LocationService.class), mConnection, 0);
        Toast.makeText(MainActivity.this, mBound ? "Bound!" : "Not bound!", Toast.LENGTH_SHORT).show();
    }

    public void disconnectService() {
        unbindService(mConnection);
        mBound = false;
    }

    public boolean isRunning() {
        try {
            return mBound && mLocationService != null && mLocationService.isRunning();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void showMenu() {
        if (mMenu != null) {
            hideMenu();
        }
        mMenu = new AlertDialog.Builder(this)
            .setTitle("Ingress Dual Map | " + (isRunning() ? "Started" : "Stopped"))
            .setItems(new String[]{
                (isRunning() ? "Stop service" : "Start service"),
                "Spawn test portal",
                "Clear notifications"
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            Intent intent = new Intent(getApplicationContext(), LocationService.class);
                            intent.addCategory(Utils.TAG);
                            if (isRunning()) {
                                stopService(intent);
                            } else {
                                startService(intent);
                            }
                            return;
                        case 1:
                            Location location = LocationService.getLastLocation();
                            if (isRunning()) {
                                if (location == null) {
                                    Toast.makeText(MainActivity.this, "Waiting for a location...", Toast.LENGTH_SHORT).show();
                                } else {
                                    Portal portal = new Portal("Test", location.getLatitude(), location.getLongitude());
                                    portal.setDistance(0);
                                    Portal.PORTALS.add(portal);
                                    LocationService.notifyPortal(MainActivity.this, Portal.PORTALS.size() - 1, true);
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "You need to start the service first!", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 2:
                            LocationService.clearNotifs();
                            break;
                    }
                    showMenu();
                }
            })
            .setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.cancel();
                        finish();
                    }
                    return false;
                }
            })
            .create();
        mMenu.show();
    }

    public void hideMenu() {
        mMenu.dismiss();
        mMenu = null;
    }

}
