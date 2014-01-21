package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;

import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final String ACTION_OPTS = "opts";
    public static final String ACTION_HACK = "hack";
    public static final String ACTION_RESET = "reset";
    public static final String ACTION_PIN = "pin";

    private boolean mFromNotif = false;
    private boolean mBound = false;
    private AlertDialog mMainMenu;
    private ServiceConnection mConnection;
    private ILocationService mLocationService;

    @Override
    public void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        // Portal notification action (wait for service)
        if (action.startsWith(Utils.PACKAGE)) {
            mFromNotif = true;
            if (mMainMenu != null) {
                hideMainMenu();
            }
        // Main menu (show, refresh on service connect)
        } else {
            showMainMenu();
        }
        connectService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!mFromNotif) {
            hideMainMenu();
        }
        disconnectService();
    }

    public void connectService() {
        // Connect to the service
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                mLocationService = ILocationService.Stub.asInterface(service);
                if (mFromNotif) {
                    showPortalMenu();
                } else {
                    showMainMenu();
                }
            }
            public void onServiceDisconnected(ComponentName className) {
                mLocationService = null;
                if (mFromNotif) {
                    // Lost connection, close
                    finish();
                } else {
                    // Reload service
                    showMainMenu();
                    connectService();
                }
            }
        };
        mBound = bindService(new Intent(this, LocationService.class), mConnection, 0);
    }

    public void disconnectService() {
        unbindService(mConnection);
        mBound = false;
    }

    public boolean isRunning() {
        try {
            // Test if the service is bound and running
            return mBound && mLocationService != null && mLocationService.isRunning();
        } catch (RemoteException e) {
            return false;
        }
    }

    public void setPortals(ArrayList<Portal> portals) {
        if (mBound && mLocationService != null) {
            try {
                // Update the service portal list
                mLocationService.setPortals(portals);
            } catch (RemoteException e) {}
        }
    }

    public Portal getPortal(int i) {
        if (mBound && mLocationService != null) {
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

    public void showMainMenu() {
        if (mMainMenu != null) {
            hideMainMenu();
        }
        mMainMenu = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name) + " | " + getString(isRunning() ? R.string.started : R.string.stopped))
            .setItems(new String[]{
                getString(isRunning() ? R.string.stop_service : R.string.start_service),
                getString(R.string.import_portal_lists),
                getString(R.string.clear_notifications)
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
                            break;
                        case 1:
                            final ProgressDialog progress = new ProgressDialog(MainActivity.this);
                            progress.setTitle(getString(R.string.import_portal_lists));
                            progress.setMessage("Searching for files...");
                            progress.setCancelable(false);
                            progress.show();
                            (new PortalStore.ImportFilesTask()).execute(new PortalStore.ImportListener() {
                                @Override
                                public void onImportProgress(String fileName) {
                                    progress.setMessage(fileName);
                                }
                                @Override
                                public void onImportFinish(boolean success, ArrayList<Portal> portals) {
                                    setPortals(portals);
                                    LocationService.clearNotifs(MainActivity.this);
                                    if (success) {
                                        Toast.makeText(MainActivity.this, "Portal lists imported successfully.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Errors occurred whilst importing portal lists.\nCheck the log files for more information.", Toast.LENGTH_SHORT).show();
                                    }
                                    progress.dismiss();
                                    showMainMenu();
                                }
                            });
                            break;
                        case 2:
                            LocationService.clearNotifs(MainActivity.this);
                            showMainMenu();
                            break;
                    }
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
        mMainMenu.show();
    }

    public void hideMainMenu() {
        mMainMenu.dismiss();
        mMainMenu = null;
    }

    public void showPortalMenu() {
        String[] params = getIntent().getAction().substring(Utils.PACKAGE.length() + 1).split("\\.");
        String action = params[0];
        final int i = Integer.valueOf(params[1]);
        final Portal portal = getPortal(i);
        Log.d(Utils.TAG, portal.toString());
        if (action.equals("opts")) {
            new AlertDialog.Builder(this)
                .setTitle(portal.getName())
                .setItems(new String[]{
                    getString(R.string.mark_hacked),
                    getString(R.string.mark_burned_out),
                    getString(R.string.reset_status),
                    getString(portal.isPinned() ? R.string.unpin_notification : R.string.pin_notification),
                    getString(portal.isResoBuzz() ? R.string.disable_resonator_buzzer : R.string.enable_resonator_buzzer)
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                hackPortal(i, portal);
                                break;
                            case 1:
                                burnOutPortal(i, portal);
                                break;
                            case 2:
                                resetPortal(i, portal);
                                break;
                            case 3:
                                pinPortal(i, portal);
                                break;
                            case 4:
                                resoBuzzPortal(i, portal);
                                break;
                        }
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
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
                .create().show();
        } else if (action.equals("hack")) {
            hackPortal(i, portal);
            finish();
        } else if (action.equals("reset")) {
            resetPortal(i, portal);
            finish();
        } else if (action.equals("pin")) {
            pinPortal(i, portal);
            finish();
        }
    }

    public void hackPortal(int i, Portal portal) {
        // Decrease hack count
        int hacks = portal.getHacksRemaining() - 1;
        portal.setHacksRemaining(hacks);
        String message = "Hacked " + portal.getName() + ".\n";
        // Just running hot, wait 5 mins
        if (hacks > 0) {
            portal.setRunningHot();
            message += hacks + " hack" + Utils.plural(hacks) + " remaining before burnout.";
        // Burnt out, wait 4 hours
        } else {
            portal.setBurnedOut();
            message += "Portal burnt out!";
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        LocationService.notifyPortal(this, i, true);
    }

    public void burnOutPortal(int i, Portal portal) {
        // Burnt out, wait 4 hours
        portal.setBurnedOut();
        Toast.makeText(this, portal.getName() + " burned out.", Toast.LENGTH_SHORT).show();
        LocationService.notifyPortal(this, i, true);
    }

    public void resetPortal(int i, Portal portal) {
        // Clear any timers or hack counts
        portal.reset();
        Toast.makeText(this, "Reset " + portal.getName() + ".", Toast.LENGTH_SHORT).show();
        LocationService.notifyPortal(this, i, true);
    }

    public void pinPortal(int i, Portal portal) {
        // Toggle pinned notification (doesn't disappear when out of range)
        portal.setPinned(!portal.isPinned());
        Toast.makeText(this, (portal.isPinned() ? "P" : "Unp") + "inned " + portal.getName() + ".", Toast.LENGTH_SHORT).show();
        LocationService.notifyPortal(this, i, (portal.getDistance() <= 50 || portal.isPinned()));
    }

    public void resoBuzzPortal(int i, Portal portal) {
        // Toggle resonator buzzing (vibrate when at optimum resonator distance, 35-40m)
        portal.setResoBuzz(!portal.isResoBuzz());
        if (portal.isResoBuzz()) {
            Toast.makeText(this, "Resonator buzz enabled.  Will vibrate at 35-40m from portal.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Resonator buzz disabled.", Toast.LENGTH_SHORT).show();
        }
        LocationService.notifyPortal(this, i, true);
    }

}
