package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import android.support.v4.app.Fragment;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;

/**
 * Fragment for showing currently imported portals.
 */
public class ListFragment extends Fragment implements ILocationServiceFragment {

    private Activity mActivity;
    private boolean mDelay = false;
    private boolean mPending = false;
    private ListView mList;

    private LocationServiceWrap mService;
    private ListArrayAdapter mAdapter;
    private ActionMode mActionMode;
    private List<Portal> mPortals;
    private double[] mLastLocation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_list, container, false);
        mList = (ListView) view.findViewById(R.id.list_list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode == null) {
                    mPending = true;
                    Intent optsIntent = new Intent(mActivity, MainActivity.class);
                    optsIntent.setAction(Utils.APP_PACKAGE + ".opts." + position);
                    startActivity(optsIntent);
                } else {
                    view.setSelected(!view.isSelected());
                    mAdapter.toggleSelection(position);
                    if (mAdapter.getSelectionCount() == 0) {
                        mActionMode.finish();
                    }
                    mList.invalidateViews();
                }
            }
        });
        registerForContextMenu(mList);
        if (mDelay) {
            init();
        }
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Delay autorun until view is created
        if (mList == null) {
            mDelay = true;
        } else if (mPending) {
            mPending = false;
            refresh();
        } else {
            init();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frag_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                add();
                return true;
            case R.id.menu_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Collection of handlers for managing the action mode.
     */
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.frag_list_action, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // Get selected items
            SparseBooleanArray checked = mList.getCheckedItemPositions();
            ArrayList<Portal> portals = new ArrayList<Portal>();
            for (int i = 0; i < mAdapter.getCount(); i++) {
                if (checked.get(i)) {
                    portals.add((Portal) mAdapter.getItem(i));
                }
            }
            // Handle menu option click
            switch (item.getItemId()) {
                case R.id.menu_refresh:
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            // Clear selections
            mList.clearChoices();
            mList.setChoiceMode(ListView.CHOICE_MODE_NONE);
            mAdapter.clearSelections();
            mList.invalidateViews();
        }
    };

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (mActionMode == null) {
            // Start the context menu using the defined callback
            mActionMode = ((ActionBarActivity) mActivity).startSupportActionMode(mActionModeCallback);
            mList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            view.setSelected(true);
        }
    }

    /**
     * Initialize the list of imported portals. 
     */
    public void init() {
        get();
        mAdapter = new ListArrayAdapter(getActivity(), mPortals, mLastLocation);
        mList.setAdapter(mAdapter);
    }

    /**
     * Refresh the list without destroying the adapter. 
     */
    public void refresh() {
        get();
        mAdapter.setPortals(mPortals);
        mAdapter.setLastLocation(mLastLocation);
        mList.invalidateViews();
    }

    /**
     * Get values from the service.
     */
    public void get() {
        mPortals = mService.getAllPortals();
        mLastLocation = mService.hasLastLocation() ? mService.getLastLocation() : null;
    }

    /**
     * Show a custom dialog for adding a new portal.
     */
    public void add() {
        final View view = mActivity.getLayoutInflater().inflate(R.layout.dialog_portal, null);
        final EditText editName = (EditText) view.findViewById(R.id.edit_name);
        final EditText editLat = (EditText) view.findViewById(R.id.edit_lat);
        final EditText editLng = (EditText) view.findViewById(R.id.edit_lng);
        final AlertDialog add = new AlertDialog.Builder(mActivity)
            .setTitle(getString(R.string.add_new_portal))
            .setView(view)
            .setPositiveButton(getString(R.string.save), null)
            .setNeutralButton(getString(R.string.here), null)
            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
            .create();
        add.show();
        add.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
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
                    Portal portal = new Portal(name, lat, lng);
                    mService.addPortal(portal);
                    Toast.makeText(mActivity, "New portal added.", Toast.LENGTH_LONG).show();
                    add.dismiss();
                    refresh();
                }
            }
        });
        add.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService.hasLastLocation()) {
                    double[] location = mService.getLastLocation();
                    editLat.setText(String.valueOf(location[0]));
                    editLng.setText(String.valueOf(location[1]));
                } else {
                    Toast.makeText(mActivity, "No location data available!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void setServiceConnection(LocationServiceWrap service) {
        mService = service;
    }

}
