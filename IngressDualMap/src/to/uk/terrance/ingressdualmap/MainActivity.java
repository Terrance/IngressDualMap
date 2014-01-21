package to.uk.terrance.ingressdualmap;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;
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

import com.michaelnovakjr.numberpicker.NumberPickerDialog;

public class MainActivity extends Activity {

    public static final String ACTION_OPTS = "opts";
    public static final String ACTION_HACK = "hack";
    public static final String ACTION_RESET = "reset";
    public static final String ACTION_PIN = "pin";

    private boolean mFromNotif = false;
    private boolean mBound = false;
    private AlertDialog mMainMenu, mImportList, mPortalMenu;
    private NumberPickerDialog mKeyCount;
    private ServiceConnection mConnection;
    private ILocationService mLocationService;

    @Override
    public void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        // Portal notification action (wait for service)
        if (action.startsWith(Utils.PACKAGE)) {
            mFromNotif = true;
            hideAll();
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
            hideAll();
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
                    hideAll();
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

    public void updatePortal(int i, Portal portal) {
        if (mBound && mLocationService != null) {
            try {
                // Update a portal in the service
                mLocationService.updatePortal(i, portal);
            } catch (RemoteException e) {}
        }
    }

    public void notifyPortal(int i) {
        if (mBound && mLocationService != null) {
            try {
                // Refresh the notification
                mLocationService.notifyPortal(i);
            } catch (RemoteException e) {}
        }
    }

    public void showMainMenu() {
        hideAll();
        mMainMenu = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name) + " | " + getString(isRunning() ? R.string.started : R.string.stopped))
            .setItems(new String[]{
                getString(isRunning() ? R.string.stop_service : R.string.start_service),
                getString(isRunning() ? R.string.import_portal_lists : R.string.clear_notifications)
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            Intent intent = new Intent(getApplicationContext(), LocationService.class);
                            intent.addCategory(Utils.TAG);
                            if (isRunning()) {
                                Toast.makeText(MainActivity.this, "Service stopped!", Toast.LENGTH_LONG).show();
                                stopService(intent);
                            } else {
                                Toast.makeText(MainActivity.this, "Service started!", Toast.LENGTH_LONG).show();
                                startService(intent);
                            }
                            break;
                        case 1:
                            if (isRunning()) {
                                showImportList();
                            } else {
                                LocationService.clearNotifs(MainActivity.this);
                                showMainMenu();
                            }
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
        if (mMainMenu != null) {
            mMainMenu.cancel();
            mMainMenu = null;
        }
    }

    public void showImportList() {
        hideAll();
        final File folder = new File(Environment.getExternalStorageDirectory() + "/IngressDualMap");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File[] files = folder.listFiles();
        final ArrayList<File> filteredFiles = new ArrayList<File>();
        for (File file : files) {
            String name = file.getName();
            if (name.substring(name.length() - 4) != ".log") {
                filteredFiles.add(file);
            }
        }
        final int size = filteredFiles.size();
        final boolean[] checked = new boolean[size];
        String[] filteredNames = new String[size];
        for (int i = 0; i < size; i++) {
            filteredNames[i] = filteredFiles.get(i).getName();
        }
        mImportList = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.import_portal_lists))
            .setMultiChoiceItems(filteredNames, checked, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    checked[which] = isChecked;
                }
            })
            .setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    showMainMenu();
                }
            })
            .setPositiveButton(R.string.import_lists, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ArrayList<File> selectedFiles = new ArrayList<File>();
                    for (int i = 0; i < size; i++) {
                        if (checked[i]) {
                            selectedFiles.add(filteredFiles.get(i));
                        }
                    }
                    if (selectedFiles.size() > 0) {
                        final ProgressDialog progress = new ProgressDialog(MainActivity.this);
                        progress.setTitle(getString(R.string.import_portal_lists));
                        progress.setMessage("Searching for files...");
                        progress.setCancelable(false);
                        progress.show();
                        (new PortalStore.ImportFilesTask(selectedFiles)).execute(new PortalStore.ImportListener() {
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
                                    Toast.makeText(MainActivity.this, "Errors occurred whilst importing portal lists.\nCheck the log files for more information.", Toast.LENGTH_LONG).show();
                                }
                                progress.dismiss();
                                showMainMenu();
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "No files were selected.", Toast.LENGTH_SHORT).show();
                        showMainMenu();
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
        mImportList.show();
    }

    public void hideImportList() {
        if (mImportList != null) {
            mImportList.cancel();
            mImportList = null;
        }
    }

    public void showPortalMenu() {
        hideAll();
        String[] params = getIntent().getAction().substring(Utils.PACKAGE.length() + 1).split("\\.");
        String action = params[0];
        final int i = Integer.valueOf(params[1]);
        final Portal portal = getPortal(i);
        Log.d(Utils.TAG, portal.toString());
        if (action.equals("opts")) {
            mPortalMenu = new AlertDialog.Builder(this)
                .setTitle(portal.getName())
                .setItems(new String[]{
                    getString(R.string.mark_hacked),
                    getString(R.string.mark_burned_out),
                    getString(R.string.reset_status),
                    getString(R.string.edit_key_count),
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
                                showKeyCount(i, portal);
                                return;
                            case 4:
                                pinPortal(i, portal);
                                break;
                            case 5:
                                resoBuzzPortal(i, portal);
                                break;
                        }
                        dialog.dismiss();
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
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
                .create();
            mPortalMenu.show();
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

    public void hidePortalMenu() {
        if (mPortalMenu != null) {
            mPortalMenu.cancel();
            mPortalMenu = null;
        }
    }

    public void showKeyCount(final int i, final Portal portal) {
        mKeyCount = new NumberPickerDialog(this, i, portal.getKeys(), getString(R.string.edit_key_count), null, null);
        mKeyCount.getNumberPicker().setRange(0, 99);
        mKeyCount.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                portal.setKeys(((NumberPickerDialog) dialog).getNumberPicker().getCurrent());
                updatePortal(i, portal);
                Toast.makeText(MainActivity.this, "Updated key count to " + portal.getKeys() + ".", Toast.LENGTH_LONG).show();
                dialog.dismiss();
                finish();
            }
        });
        mKeyCount.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        mKeyCount.setOnKeyListener(new Dialog.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dialog.cancel();
                    finish();
                }
                return false;
            }
        });
        mKeyCount.show();
    }

    public void hideKeyCount() {
        if (mKeyCount != null) {
            mKeyCount.cancel();
            mKeyCount = null;
        }
    }

    public void hideAll() {
        hideMainMenu();
        hideImportList();
        hidePortalMenu();
        hideKeyCount();
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
        updatePortal(i, portal);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        notifyPortal(i);
    }

    public void burnOutPortal(int i, Portal portal) {
        // Burnt out, wait 4 hours
        portal.setBurnedOut();
        updatePortal(i, portal);
        Toast.makeText(this, portal.getName() + " burned out.", Toast.LENGTH_SHORT).show();
        notifyPortal(i);
    }

    public void resetPortal(int i, Portal portal) {
        // Clear any timers or hack counts
        portal.reset();
        updatePortal(i, portal);
        Toast.makeText(this, "Reset " + portal.getName() + ".", Toast.LENGTH_SHORT).show();
        notifyPortal(i);
    }

    public void pinPortal(int i, Portal portal) {
        // Toggle pinned notification (doesn't disappear when out of range)
        portal.setPinned(!portal.isPinned());
        updatePortal(i, portal);
        Toast.makeText(this, (portal.isPinned() ? "P" : "Unp") + "inned " + portal.getName() + ".", Toast.LENGTH_SHORT).show();
        notifyPortal(i);
    }

    public void resoBuzzPortal(int i, Portal portal) {
        // Toggle resonator buzzing (vibrate when at optimum resonator distance, 35-40m)
        portal.setResoBuzz(!portal.isResoBuzz());
        updatePortal(i, portal);
        if (portal.isResoBuzz()) {
            Toast.makeText(this, "Resonator buzz enabled.  Will vibrate at 35-40m from portal.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Resonator buzz disabled.", Toast.LENGTH_SHORT).show();
        }
        notifyPortal(i);
    }

}
