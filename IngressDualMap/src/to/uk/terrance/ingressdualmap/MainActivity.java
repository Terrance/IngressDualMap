package to.uk.terrance.ingressdualmap;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

import android.support.v4.app.Fragment;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

/**
 * Main UI activity for enabling the service, downloading portal lists or editing setings.
 */
public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

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
    private boolean mInit = false;
    private AlertDialog mPortalMenu, mAlignment, mLevel, mKeyCount;
    private ServiceConnection mConnection;
    private ILocationService mLocationService;
    private LocationServiceWrap mLocationServiceWrap = new LocationServiceWrap();
    private Fragment[] mFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        // Portal notification action (wait for service)
        if (action.startsWith(Utils.APP_PACKAGE)) {
            mFromNotif = true;
        // Main app UI
        } else {
            setTheme(R.style.Theme_IDM);
            setContentView(R.layout.activity_main);
            // Set up the action bar to show a dropdown list
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            // Specify a SpinnerAdapter to populate the dropdown list
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    getActionBarThemedContextCompat(), android.R.layout.simple_list_item_1,
                    android.R.id.text1, getResources().getStringArray(R.array.actionbar_dropdown));
            // Set up the dropdown list navigation in the action bar
            actionBar.setListNavigationCallbacks(adapter, this);
            // Initialise fragments
            mFragments = new Fragment[]{new ServiceFragment(), new DownloadFragment(),
                    new ImportFragment(), new ListFragment(), new MapsFragment(), new SettingsFragment()};
            for (Fragment fragment : mFragments) {
                if (fragment instanceof ILocationServiceFragment) {
                    ((ILocationServiceFragment) fragment).setServiceConnection(mLocationServiceWrap); 
                }
            }
        }
        mInit = true;
        connectService();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mInit) {
            connectService();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Close all open dialogs
        if (!mFromNotif) {
            hideAll();
        }
        unbindService(mConnection);
        mInit = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Bind the {@link LocationService} to this activity, providing access to {@link ILocationService} methods.
     */
    public void connectService() {
        // Connect to the service
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                mLocationService = ILocationService.Stub.asInterface(service);
                mLocationServiceWrap.set(mLocationService);
                if (mFromNotif) {
                    showPortalMenu();
                }
            }
            public void onServiceDisconnected(ComponentName className) {
                mLocationService = null;
                mLocationServiceWrap.set(null);
                if (mFromNotif) {
                    // Lost connection, close
                    hideAll();
                    finish();
                } else {
                    // Reload service
                    connectService();
                }
            }
        };
        bindService(new Intent(this, LocationService.class), mConnection, 0);
    }

    /**
     * Backward-compatible version of {@link ActionBar#getThemedContext()} that simply returns the
     * {@link android.app.Activity} if <code>getThemedContext</code> is unavailable.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Context getActionBarThemedContextCompat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? getActionBar().getThemedContext() : this;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Restore the previously serialized current dropdown position
        if (savedInstanceState.containsKey("actionbar_selected")) {
            getSupportActionBar().setSelectedNavigationItem(savedInstanceState.getInt("actionbar_selected"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position
        outState.putInt("actionbar_selected", getSupportActionBar().getSelectedNavigationIndex());
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // Show the fragment selected in the dropdown
        getSupportFragmentManager().beginTransaction().replace(R.id.container, (Fragment) mFragments[position]).commit();
        return true;
    }

    /**
     * Helper class for closing the dialog/activity on dismiss or cancel.
     */
    private class DialogClose implements DialogInterface.OnCancelListener {
        @Override
        public void onCancel(DialogInterface dialog) {
            finish();
        }
    }
    private DialogClose dialogClose = new DialogClose();

    /**
     * Show an {@link AlertDialog} menu with all options relating to specific portals.
     */
    public void showPortalMenu() {
        hideAll();
        String[] params = getIntent().getAction().substring(Utils.APP_PACKAGE.length() + 1).split("\\.");
        String action = params[0];
        final int i = Integer.valueOf(params[1]);
        final Portal portal = mLocationServiceWrap.getPortal(i);
        Log.d(Utils.APP_TAG, portal.toString());
        if (action.equals("opts")) {
            mPortalMenu = new AlertDialog.Builder(this)
                .setTitle(portal.getName())
                .setItems(new String[]{
                    getString(R.string.mark_hacked),
                    getString(R.string.mark_burned_out),
                    getString(R.string.reset_status),
                    getString(R.string.set_alignment),
                    getString(R.string.set_level),
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
                                showLevel(i, portal);
                                return;
                            case 5:
                                showKeyCount(i, portal);
                                return;
                            case 6:
                                pinPortal(i, portal);
                                break;
                            case 7:
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
                    }
                })
                .setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.cancel();
                        }
                        return false;
                    }
                })
                .setOnCancelListener(dialogClose)
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
            .setSingleChoiceItems(alignments, portal.getAlignment(), null)
            .setNegativeButton(R.string.cancel, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setPositiveButton(R.string.save, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    portal.setAlignment(((AlertDialog) dialog).getListView().getCheckedItemPosition());
                    mLocationServiceWrap.updatePortal(i, portal);
                    Toast.makeText(MainActivity.this, "Updated alignment to " + alignments[portal.getAlignment()] + ".", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    finish();
                }
            })
            .setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.cancel();
                    }
                    return false;
                }
            })
            .setOnCancelListener(dialogClose)
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
     * Show a custom dialog for setting a portal's level.
     */
    public void showLevel(final int i, final Portal portal) {
        View view = getLayoutInflater().inflate(R.layout.dialog_level, null);
        mLevel = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.set_level))
            .setView(view)
            .setNeutralButton(getString(R.string.clear), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    portal.setLevel(0);
                    mLocationServiceWrap.updatePortal(i, portal);
                    Toast.makeText(MainActivity.this, "Cleared current level.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    finish();
                }
            })
            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.cancel();
                    }
                    return false;
                }
            })
            .setOnCancelListener(dialogClose)
            .create();
        View[] buttons = new View[]{
            view.findViewById(R.id.btn_level1), view.findViewById(R.id.btn_level2),
            view.findViewById(R.id.btn_level3), view.findViewById(R.id.btn_level4),
            view.findViewById(R.id.btn_level5), view.findViewById(R.id.btn_level6),
            view.findViewById(R.id.btn_level7), view.findViewById(R.id.btn_level8)  
        };
        int level = 0;
        for (View button : buttons) {
            level++;
            final int thisLevel = level;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    portal.setLevel(thisLevel);
                    mLocationServiceWrap.updatePortal(i, portal);
                    Toast.makeText(MainActivity.this, "Updated level to L" + portal.getLevel() + ".", Toast.LENGTH_LONG).show();
                    mLevel.dismiss();
                    finish();
                }
            });
        }
        mLevel.show();
    }

    /**
     * Close the level dialog if open.
     */
    public void hideLevel() {
        if (mLevel != null) {
            mLevel.cancel();
            mLevel = null;
        }
    }

    /**
     * Show a custom dialog for setting a portal's key count.
     */
    public void showKeyCount(final int i, final Portal portal) {
        final View view = getLayoutInflater().inflate(R.layout.dialog_keycount, null);
        final EditText edit = (EditText) view.findViewById(R.id.edit_keys);
        mKeyCount = new AlertDialog.Builder(this)
            .setTitle(getString(R.string.set_key_count))
            .setView(view)
            .setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int val = 0;
                    try {
                        val = Integer.valueOf(edit.getText().toString());
                        if (val < 0) {
                            val = 0;
                        }
                    } catch (NumberFormatException e) {}
                    portal.setKeys(val);
                    mLocationServiceWrap.updatePortal(i, portal);
                    if (portal.getKeys() > 0) {
                        Toast.makeText(MainActivity.this, "Updated key count to " + portal.getKeys() + ".", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Cleared key count.", Toast.LENGTH_LONG).show();
                    }
                    dialog.dismiss();
                    finish();
                }
            })
            .setNeutralButton(getString(R.string.reset), null)
            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.cancel();
                    }
                    return false;
                }
            })
            .setOnCancelListener(dialogClose)
            .create();
        view.findViewById(R.id.btn_minus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                try {
                    val = Integer.valueOf(edit.getText().toString()) - 1;
                    if (val < 0) {
                        val = 0;
                    }
                } catch (NumberFormatException e) {}
                edit.setText(String.valueOf(val));
            }
        });
        view.findViewById(R.id.btn_plus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int val = 0;
                try {
                    val = Integer.valueOf(edit.getText().toString()) + 1;
                } catch (NumberFormatException e) {}
                edit.setText(String.valueOf(val));
            }
        });
        edit.setText(String.valueOf(portal.getKeys()));
        mKeyCount.show();
        mKeyCount.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit.setText("0");
            }
        });
    }

    /**
     * Close the key count dialog if open.
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
        hidePortalMenu();
        hideAlignment();
        hideLevel();
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
        mLocationServiceWrap.updatePortal(i, portal);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        mLocationServiceWrap.notifyPortal(i);
    }

    /**
     * Mark a portal as burnt out through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
    public void burnOutPortal(int i, Portal portal) {
        // Burnt out, wait 4 hours
        portal.setBurnedOut();
        mLocationServiceWrap.updatePortal(i, portal);
        Toast.makeText(this, portal.getName() + " burned out.", Toast.LENGTH_SHORT).show();
        mLocationServiceWrap.notifyPortal(i);
    }

    /**
     * Reset a portal through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
    public void resetPortal(int i, Portal portal) {
        // Clear any timers or hack counts
        portal.reset();
        mLocationServiceWrap.updatePortal(i, portal);
        Toast.makeText(this, "Reset " + portal.getName() + ".", Toast.LENGTH_SHORT).show();
        mLocationServiceWrap.notifyPortal(i);
    }

    /**
     * Pin a portal to the notification drawer through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
    public void pinPortal(int i, Portal portal) {
        // Toggle pinned notification (doesn't disappear when out of range)
        portal.setPinned(!portal.isPinned());
        mLocationServiceWrap.updatePortal(i, portal);
        Toast.makeText(this, (portal.isPinned() ? "P" : "Unp") + "inned " + portal.getName() + ".", Toast.LENGTH_SHORT).show();
        mLocationServiceWrap.notifyPortal(i);
    }

    /**
     * Toggle resonator buzz on a portal through the UI.
     * @param i The index of the portal.
     * @param portal The portal object.
     */
    public void resoBuzzPortal(int i, Portal portal) {
        // Toggle resonator buzzing (vibrate when at optimum resonator distance, 35-40m)
        portal.setResoBuzz(!portal.isResoBuzz());
        mLocationServiceWrap.updatePortal(i, portal);
        if (portal.isResoBuzz()) {
            Toast.makeText(this, "Resonator buzz enabled.  Will vibrate when at optimal distance from portal.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Resonator buzz disabled.", Toast.LENGTH_SHORT).show();
        }
        mLocationServiceWrap.notifyPortal(i);
    }

}
