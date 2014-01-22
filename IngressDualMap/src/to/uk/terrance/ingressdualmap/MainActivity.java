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

/** 
 * UI activity for the main and portal menus and options.
 */
public class MainActivity extends Activity {

    /**
     * Action to display all options.
     */
    public static final String ACTION_OPTS = "opts";
    /**
     * Action to mark a given portal as hacked.
     */
    public static final String ACTION_HACK = "hack";
    /**
     * Action to reset a given portal.
     */
    public static final String ACTION_RESET = "reset";
    /**
     * Action to pin the notification of a given portal.
     */
    public static final String ACTION_PIN = "pin";

    private boolean mFromNotif = false;
    private boolean mBound = false;
    private AlertDialog mMainMenu, mImportLists, mDownloadLists, mPortalMenu, mAlignment;
    private NumberPickerDialog mKeyCount;
    private ServiceConnection mConnection;
    private ILocationService mLocationService;

    @Override
    public void onResume() {
        super.onResume();
        String action = getIntent().getAction();
        // Portal notification action (wait for service)
        if (action.startsWith(Utils.APP_PACKAGE)) {
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

    /**
     * Bind the {@link LocationService} to this activity, providing access to methods defined
     * in the {@link ILocationService} binder.
     */
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

    /**
     * Unbind the service when pausing or destroying the activity.
     */
    public void disconnectService() {
        unbindService(mConnection);
        mBound = false;
    }

    /**
     * Wrapper for {@link ILocationService#isRunning} to handle service exceptions.
     */
    public boolean isRunning() {
        try {
            // Test if the service is bound and running
            return mBound && mLocationService != null && mLocationService.isRunning();
        } catch (RemoteException e) {
            return false;
        }
    }

    /**
     * Wrapper for {@link ILocationService#setPortals} to handle service exceptions.
     */
    public void setPortals(ArrayList<Portal> portals) {
        if (mBound && mLocationService != null) {
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

    /**
     * Wrapper for {@link ILocationService#updatePortal} to handle service exceptions.
     */
    public void updatePortal(int i, Portal portal) {
        if (mBound && mLocationService != null) {
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
        if (mBound && mLocationService != null) {
            try {
                // Refresh the notification
                mLocationService.notifyPortal(i);
            } catch (RemoteException e) {}
        }
    }

    /**
     * Show an {@link AlertDialog} menu with the main options (start, stop, etc.).
     */
    public void showMainMenu() {
        hideAll();
        AlertDialog.Builder menuBuilder = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.app_name) + " | " + getString(isRunning() ? R.string.started : R.string.stopped))
            .setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.cancel();
                        finish();
                    }
                    return false;
                }
            });
        if (isRunning()) {
            menuBuilder.setItems(new String[]{
                    getString(R.string.stop_service),
                    getString(R.string.import_portal_lists)
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                                intent.addCategory(Utils.APP_TAG);
                                Toast.makeText(MainActivity.this, "Service stopped!", Toast.LENGTH_LONG).show();
                                stopService(intent);
                                break;
                            case 1:
                                showImportLists();
                                break;
                        }
                    }
                });
        } else {
            menuBuilder.setItems(new String[]{
                    getString(R.string.start_service),
                    getString(R.string.download_portal_lists),
                    getString(R.string.clear_notifications)
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                                intent.addCategory(Utils.APP_TAG);
                                Toast.makeText(MainActivity.this, "Service started!", Toast.LENGTH_LONG).show();
                                startService(intent);
                                break;
                            case 1:
                                showQueryLists();
                                break;
                            case 2:
                                LocationService.clearNotifs(MainActivity.this);
                                showMainMenu();
                                break;
                        }
                    }
                });
        }
        mMainMenu = menuBuilder.create();
        mMainMenu.show();
    }

    /**
     * Close the main menu dialog if open.
     */
    public void hideMainMenu() {
        if (mMainMenu != null) {
            mMainMenu.cancel();
            mMainMenu = null;
        }
    }

    /**
     * Show an {@link AlertDialog} menu for importing portal lists into the service.
     */
    public void showImportLists() {
        hideAll();
        final File folder = new File(Environment.getExternalStorageDirectory() + "/IngressDualMap");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File[] files = folder.listFiles();
        final ArrayList<File> filteredFiles = new ArrayList<File>();
        Log.d(Utils.APP_TAG, "Searching for local list files...");
        for (File file : files) {
            String name = file.getName();
            if (!name.substring(name.length() - 4).equals(".log")) {
                Log.d(Utils.APP_TAG, "Found " + name + ".");
                filteredFiles.add(file);
            }
        }
        final int size = filteredFiles.size();
        final boolean[] checked = new boolean[size];
        String[] filteredNames = new String[size];
        for (int i = 0; i < size; i++) {
            filteredNames[i] = filteredFiles.get(i).getName();
        }
        mImportLists = new AlertDialog.Builder(this)
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
                            public void onImportProgress(String fileName, int percent) {
                                progress.setMessage(fileName);
                                progress.setProgress(percent);
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
        mImportLists.show();
    }

    /**
     * Close the import lists dialog if open.
     */
    public void hideImportLists() {
        if (mImportLists != null) {
            mImportLists.cancel();
            mImportLists = null;
        }
    }

    /**
     * Show an {@link AlertDialog} menu for downloading portal lists from the server.
     */
    public void showQueryLists() {
        hideAll();
        final ProgressDialog progress = new ProgressDialog(MainActivity.this);
        progress.setTitle(getString(R.string.download_portal_lists));
        progress.setMessage("Checking available files...");
        progress.setCancelable(false);
        progress.show();
        (new PortalStore.QueryFilesTask()).execute(new PortalStore.QueryListener() {
            @Override
            public void onQueryFinish(boolean success, final String[] files) {
                if (success) {
                    final boolean[] checked = new boolean[files.length];
                    mDownloadLists = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.download_portal_lists))
                        .setMultiChoiceItems(files, checked, new DialogInterface.OnMultiChoiceClickListener() {
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
                        .setPositiveButton(R.string.download_lists, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<String> selectedFiles = new ArrayList<String>();
                                for (int i = 0; i < files.length; i++) {
                                    if (checked[i]) {
                                        selectedFiles.add(files[i]);
                                    }
                                }
                                if (selectedFiles.size() > 0) {
                                    final ProgressDialog progress = new ProgressDialog(MainActivity.this);
                                    progress.setTitle(getString(R.string.download_portal_lists));
                                    progress.setMessage("Downloading lists...");
                                    progress.setCancelable(false);
                                    progress.show();
                                    (new PortalStore.DownloadFilesTask(selectedFiles)).execute(new PortalStore.DownloadListener() {
                                        @Override
                                        public void onDownloadProgress(String fileName, int percent) {
                                            progress.setMessage(fileName);
                                            progress.setProgress(percent);
                                        }
                                        @Override
                                        public void onDownloadFinish(boolean success) {
                                            if (success) {
                                                Toast.makeText(MainActivity.this, "The portal lists were downloaded successfully.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Errors occurred whilst downloading the portal lists.", Toast.LENGTH_LONG).show();
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
                    mDownloadLists.show();
                    progress.dismiss();
                } else {
                    progress.dismiss();
                    Toast.makeText(MainActivity.this, "Unable to query for available lists.\nCheck your connection and try again.", Toast.LENGTH_LONG).show();
                    showMainMenu();
                }
            }
        });
    }

    /**
     * Close the download lists menu if open.
     */
    public void hideDownloadLists() {
        if (mDownloadLists != null) {
            mDownloadLists.cancel();
            mDownloadLists = null;
        }
    }

    /**
     * Show an {@link AlertDialog} menu with all options relating to specific portals.
     */
    public void showPortalMenu() {
        hideAll();
        String[] params = getIntent().getAction().substring(Utils.APP_PACKAGE.length() + 1).split("\\.");
        String action = params[0];
        final int i = Integer.valueOf(params[1]);
        final Portal portal = getPortal(i);
        Log.d(Utils.APP_TAG, portal.toString());
        if (action.equals("opts")) {
            mPortalMenu = new AlertDialog.Builder(this)
                .setTitle(portal.getName())
                .setItems(new String[]{
                    getString(R.string.mark_hacked),
                    getString(R.string.mark_burned_out),
                    getString(R.string.reset_status),
                    getString(R.string.set_alignment),
                    getString(R.string.set_key_count),
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
                                showAlignment(i, portal);
                                return;
                            case 4:
                                showKeyCount(i, portal);
                                return;
                            case 5:
                                pinPortal(i, portal);
                                break;
                            case 6:
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

    /**
     * Close the portal menu if open.
     */
    public void hidePortalMenu() {
        if (mPortalMenu != null) {
            mPortalMenu.cancel();
            mPortalMenu = null;
        }
    }

    /**
     * Show an {@link AlertDialog} menu for setting a portal's alignment.
     */
    public void showAlignment(final int i, final Portal portal) {
        final String[] alignments = getResources().getStringArray(R.array.alignment);
        mAlignment = new AlertDialog.Builder(this)
            .setTitle(R.string.set_alignment)
            .setSingleChoiceItems(alignments, portal.getAlignment() + 1, null)
            .setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            })
            .setPositiveButton(R.string.save, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    portal.setAlignment(((AlertDialog) dialog).getListView().getCheckedItemPosition() - 1);
                    updatePortal(i, portal);
                    Toast.makeText(MainActivity.this, "Updated alignment to " + alignments[portal.getAlignment() + 1] + ".", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
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
        mAlignment.show();
    }

    /**
     * Close the alignment menu if open.
     */
    public void hideAlignment() {
        if (mAlignment != null) {
            mAlignment.cancel();
            mAlignment = null;
        }
    }

    /**
     * Show an {@link AlertDialog} menu for setting a portal's key count.
     */
    public void showKeyCount(final int i, final Portal portal) {
        mKeyCount = new NumberPickerDialog(this, i, portal.getKeys(), getString(R.string.set_key_count), null, null);
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

    /**
     * Close the key count menu if open.
     */
    public void hideKeyCount() {
        if (mKeyCount != null) {
            mKeyCount.cancel();
            mKeyCount = null;
        }
    }

    /**
     * Close any open menus or dialogs.
     */
    public void hideAll() {
        hideMainMenu();
        hideImportLists();
        hideDownloadLists();
        hidePortalMenu();
        hideAlignment();
        hideKeyCount();
    }

    /**
     * Mark a portal as hacked through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
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

    /**
     * Mark a portal as burnt out through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
    public void burnOutPortal(int i, Portal portal) {
        // Burnt out, wait 4 hours
        portal.setBurnedOut();
        updatePortal(i, portal);
        Toast.makeText(this, portal.getName() + " burned out.", Toast.LENGTH_SHORT).show();
        notifyPortal(i);
    }

    /**
     * Reset a portal through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
    public void resetPortal(int i, Portal portal) {
        // Clear any timers or hack counts
        portal.reset();
        updatePortal(i, portal);
        Toast.makeText(this, "Reset " + portal.getName() + ".", Toast.LENGTH_SHORT).show();
        notifyPortal(i);
    }

    /**
     * Pin a portal to the notification drawer through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
    public void pinPortal(int i, Portal portal) {
        // Toggle pinned notification (doesn't disappear when out of range)
        portal.setPinned(!portal.isPinned());
        updatePortal(i, portal);
        Toast.makeText(this, (portal.isPinned() ? "P" : "Unp") + "inned " + portal.getName() + ".", Toast.LENGTH_SHORT).show();
        notifyPortal(i);
    }

    /**
     * Toggle resonator buzz on a portal through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
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
