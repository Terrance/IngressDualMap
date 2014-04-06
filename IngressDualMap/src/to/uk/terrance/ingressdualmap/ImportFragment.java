package to.uk.terrance.ingressdualmap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import android.support.v4.app.Fragment;

/**
 * Fragment for downloading portal lists.
 */
public class ImportFragment extends Fragment implements ILocationServiceFragment {

    private Activity mActivity;
    private LocationServiceWrap mService;
    private ListView mList;
    private Menu mMenu;
    private boolean mDelay = false;
    private List<File> mFiles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_import, container, false);
        mList = (ListView) view.findViewById(R.id.list_import);
        setHasOptionsMenu(true);
        if (mMenu == null) {
            mDelay = true;
        } else {
            refresh();
        }
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMenu = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(Utils.APP_TAG, "Created menu.");
        mMenu = menu;
        inflater.inflate(R.menu.frag_import, menu);
        if (mDelay) {
            refresh();
            mDelay = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
            case R.id.menu_import:
                importFiles();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refresh the list of available files on the device.
     */
    public void refresh() {
        File[] files = Utils.extStore().listFiles();
        mFiles = new ArrayList<File>();
        Log.d(Utils.APP_TAG, "Searching for local list files...");
        for (File file : files) {
            String name = file.getName();
            if (!name.equals(".backup.csv") && !name.substring(name.length() - 4).equals(".log")) {
                Log.d(Utils.APP_TAG, "Found " + name + ".");
                mFiles.add(file);
            }
        }
        int size = mFiles.size();
        if (size > 0) {
            String[] filteredNames = new String[size];
            for (int i = 0; i < size; i++) {
                filteredNames[i] = mFiles.get(i).getName();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                    android.R.layout.simple_list_item_multiple_choice, filteredNames);
            mList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            mList.setAdapter(adapter);
            mMenu.findItem(R.id.menu_import).setVisible(true);
        } else {
            mMenu.findItem(R.id.menu_import).setVisible(false);
            Toast.makeText(mActivity, "No lists can be found on the device.\nGo to the Download section to add some.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Process the selected imports.
     */
    public void importFiles() {
        SparseBooleanArray positions = mList.getCheckedItemPositions();
        List<File> selectedFiles = new ArrayList<File>();
        for (int i = 0; i < mFiles.size(); i++) {
            if (positions.get(i)) {
                selectedFiles.add(mFiles.get(i));
            }
        }
        if (selectedFiles.size() > 0) {
            final ProgressDialog progress = new ProgressDialog(mActivity);
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
                public void onImportFinish(boolean success, List<Portal> portals) {
                    mService.setPortals(portals);
                    LocationService.clearNotifs(mActivity);
                    if (success) {
                        Toast.makeText(mActivity, "Portal lists imported successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, "Errors occurred whilst importing portal lists.\nCheck the log files for more information.", Toast.LENGTH_LONG).show();
                    }
                    progress.dismiss();
                }
            });
        } else {
            Toast.makeText(mActivity, "No lists have been selected.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setServiceConnection(LocationServiceWrap service) {
        mService = service;
    }

}
