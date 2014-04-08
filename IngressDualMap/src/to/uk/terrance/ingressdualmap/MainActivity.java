package to.uk.terrance.ingressdualmap;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    private boolean mIntent = false;
    private boolean mInit = false;
    private boolean mShowing = false;
    private AlertDialog mPortalMenu, mAlignment, mLevel, mKeyCount, mEdit, mDelete;
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
            mIntent = true;
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
                mInit = true;
                if (mIntent && !mShowing) {
                    mShowing = true;
                    showPortalMenu();
                }
            }
            public void onServiceDisconnected(ComponentName className) {
                mLocationService = null;
                mLocationServiceWrap.set(null);
                if (mIntent) {
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
        if (!mIntent) {
            // Serialize the current dropdown position
            outState.putInt("actionbar_selected", getSupportActionBar().getSelectedNavigationIndex());
        }
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
        String[] params = getIntent().getAction().substring(Utils.APP_PACKAGE.length() + 1).split("\\.");
        String action = params[0];
        final int i = Integer.valueOf(params[1]);
        final Portal portal = mLocationServiceWrap.getPortal(i);
        Log.d(Utils.APP_TAG, portal.toString());
        if (action.equals("opts")) {
            View view = getLayoutInflater().inflate(R.layout.dialog_portal, null);
            final Button btnHack = (Button) view.findViewById(R.id.btn_hack);
            final Button btnBurn = (Button) view.findViewById(R.id.btn_burn);
            Button btnAlign = (Button) view.findViewById(R.id.btn_align);
            Button btnLevel = (Button) view.findViewById(R.id.btn_level);
            Button btnKeys = (Button) view.findViewById(R.id.btn_keys);
            final Button btnPin = (Button) view.findViewById(R.id.btn_pin);
            final Button btnBuzz = (Button) view.findViewById(R.id.btn_buzz);
            mPortalMenu = new AlertDialog.Builder(this)
                .setTitle(portal.getName())
                .setView(view)
                .setOnCancelListener(dialogClose)
                .create();
            ((Button) view.findViewById(R.id.btn_edit)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showEdit(i, portal);
                    mPortalMenu.dismiss();
                }
            });
            ((Button) view.findViewById(R.id.btn_delete)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDelete(i, portal);
                    mPortalMenu.dismiss();
                }
            });
            View.OnLongClickListener reset = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    resetPortal(i, portal);
                    btnHack.setText(Utils.unicode(Utils.UNICODE_BOLT) + " 4 hacks remaining");
                    btnBurn.setText("");
                    btnBurn.setHint(R.string.not_burned_out);
                    return true;
                }
            };
            btnHack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hackPortal(i, portal);
                    int hacks = portal.getHacksRemaining();
                    if (hacks > 0) {
                        btnHack.setText("");
                        btnHack.setHint(hacks + " hacks  |  Wait " + Utils.shortTime(portal.checkRunningHot()));
                    } else {
                        btnHack.setText("");
                        btnHack.setHint(R.string.no_hacks_remaining);
                        btnBurn.setHint("Burned out for " + Utils.shortTime(portal.checkBurnedOut()));
                    }
                }
            });
            btnHack.setOnLongClickListener(reset);
            int hot = portal.checkRunningHot();
            if (hot > 0) {
                btnHack.setHint("Next hack in " + Utils.shortTime(hot));
            } else {
                int hacks = portal.getHacksRemaining();
                if (hacks > 0) {
                    btnHack.setText(Utils.unicode(Utils.UNICODE_BOLT) + " " + hacks + " hacks remaining");
                }
            }
            btnBurn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    burnOutPortal(i, portal);
                    btnHack.setText("");
                    btnHack.setHint(R.string.no_hacks_remaining);
                    btnBurn.setHint("Burned out for " + Utils.shortTime(portal.checkBurnedOut()));
                }
            });
            btnBurn.setOnLongClickListener(reset);
            int burn = portal.checkBurnedOut();
            if (burn > 0) {
                btnBurn.setHint("Burned out for " + Utils.shortTime(burn));
            }
            btnAlign.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlignment(i, portal);
                    mPortalMenu.dismiss();
                }
            });
            int align = portal.getAlignment();
            if (align > 0) {
                String label = null;
                switch (align) {
                    case Portal.ALIGN_NEUTRAL:
                        label = "Neutral";
                        break;
                    case Portal.ALIGN_RESISTANCE:
                        label = "Resistance";
                        break;
                    case Portal.ALIGN_ENLIGHTENED:
                        label = "Enlightened";
                        break;
                }
                btnAlign.setText(label);
                btnAlign.setTextColor(Color.parseColor(Utils.COLOUR_ALIGNMENT[align]));
            }
            btnLevel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showLevel(i, portal);
                    mPortalMenu.dismiss();
                }
            });
            int level = portal.getLevel();
            if (level > 0) {
                btnLevel.setText("L" + level);
                btnLevel.setTextColor(Color.parseColor(Utils.COLOUR_LEVEL[level]));
            }
            btnKeys.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showKeyCount(i, portal);
                    mPortalMenu.dismiss();
                }
            });
            int keys = portal.getKeys();
            if (keys > 0) {
                btnKeys.setText(Utils.unicode(Utils.UNICODE_KEY) + " " + keys);
                btnKeys.setTextColor(Color.parseColor(Utils.COLOUR_KEYS));
            }
            btnPin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pinPortal(i, portal);
                    btnPin.setText(portal.isPinned() ? Utils.unicode(Utils.UNICODE_PIN) : "");
                }
            });
            if (portal.isPinned()) {
                btnPin.setText(Utils.unicode(Utils.UNICODE_PIN));
            }
            btnBuzz.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resoBuzzPortal(i, portal);
                    btnBuzz.setText(portal.isResoBuzz() ? Utils.unicode(Utils.UNICODE_BELL) : "");
                }
            });
            if (portal.isResoBuzz()) {
                btnBuzz.setText(Utils.unicode(Utils.UNICODE_BELL));
            }
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
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
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
            .setTitle(R.string.set_level)
            .setView(view)
            .setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    portal.setLevel(0);
                    mLocationServiceWrap.updatePortal(i, portal);
                    Toast.makeText(MainActivity.this, "Cleared current level.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                    finish();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
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
            .setTitle(R.string.set_key_count)
            .setView(view)
            .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
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
            .setNeutralButton(R.string.reset, null)
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
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
            message += "Portal burned out!";
        }
        mLocationServiceWrap.updatePortal(i, portal);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
    }

    /**
     * Show a custom dialog for editing a portal's name or location.
     */
    public void showEdit(final int i, final Portal portal) {
        final View view = getLayoutInflater().inflate(R.layout.dialog_edit, null);
        final EditText editName = (EditText) view.findViewById(R.id.edit_name);
        final EditText editLat = (EditText) view.findViewById(R.id.edit_lat);
        final EditText editLng = (EditText) view.findViewById(R.id.edit_lng);
        mEdit = new AlertDialog.Builder(this)
            .setTitle(R.string.edit_portal)
            .setView(view)
            .setPositiveButton(R.string.save, null)
            .setNeutralButton(R.string.here, null)
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
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
        editName.setText(portal.getName());
        editLat.setText(String.valueOf(portal.getLatitude()));
        editLng.setText(String.valueOf(portal.getLongitude()));
        mEdit.show();
        mEdit.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean ok = true;
                editName.setError(null);
                editLat.setError(null);
                editLng.setError(null);
                String name = editName.getText().toString();
                if (name.trim().equals("")) {
                    editName.setError("Portal name required!");
                    ok = false;
                }
                double lat = 0;
                double lng = 0;
                try {
                    lat = Double.valueOf(editLat.getText().toString());
                    lng = Double.valueOf(editLng.getText().toString());
                } catch (NumberFormatException e) {
                    editLat.setError("Valid location required!");
                    editLng.setError("Valid location required!");
                    ok = false;
                }
                if (ok) {
                    portal.setName(name);
                    portal.setLatitude(lat);
                    portal.setLongitude(lng);
                    mLocationServiceWrap.updatePortal(i, portal);
                    Toast.makeText(MainActivity.this, "Portal information updated.", Toast.LENGTH_LONG).show();
                    mEdit.dismiss();
                    finish();
                }
            }
        });
        mEdit.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLocationServiceWrap.hasLastLocation()) {
                    double[] location = mLocationServiceWrap.getLastLocation();
                    editLat.setText(String.valueOf(location[0]));
                    editLng.setText(String.valueOf(location[1]));
                } else {
                    Toast.makeText(MainActivity.this, "No location data available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Close the edit dialog if open.
     */
    public void hideEdit() {
        if (mEdit != null) {
            mEdit.cancel();
            mEdit = null;
        }
    }

    /**
     * Show a confirmation dialog for deleting a portal.
     */
    public void showDelete(final int i, final Portal portal) {
        mDelete = new AlertDialog.Builder(this)
            .setMessage("Delete this portal?  This will just remove it from the currently imported list.")
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mLocationServiceWrap.removePortals(new int[]{i});
                    dialog.dismiss();
                    finish();
                }
            })
            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            })
            .setOnKeyListener(new DialogInterface.OnKeyListener() {
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
        mDelete.show();
    }

    /**
     * Close the edit dialog if open.
     */
    public void hideDelete() {
        if (mDelete != null) {
            mDelete.cancel();
            mDelete = null;
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
        hideEdit();
        hideDelete();
    }

}
