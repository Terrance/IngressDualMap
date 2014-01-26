package to.uk.terrance.ingressdualmap;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.support.v4.app.Fragment;

/**
 * Fragment for downloading portal lists.
 */
public class ImportFragment extends Fragment implements ILocationServiceFragment {

    private Activity mActivity;
    private boolean mDelay = false;

    private LocationServiceWrap mService;
    private ListView mList;
    private Button mButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_import, container, false);
        mList = (ListView) view.findViewById(R.id.list_import);
        mButton = (Button) view.findViewById(R.id.btn_import);
        if (mDelay) {
            autorun();
        }
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        // Delay autorun until view is created
        if (mList == null) {
            mDelay = true;
        } else {
            autorun();
        }
    }

    public void autorun() {
        if (!mService.isRunning()) {
            Toast.makeText(mActivity, "You need to start the service before importing lists.", Toast.LENGTH_LONG).show();
            mButton.setEnabled(false);
            return;
        }
        final File folder = new File(Environment.getExternalStorageDirectory() + "/IngressDualMap");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        final File[] files = folder.listFiles();
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
        String[] filteredNames = new String[size];
        for (int i = 0; i < size; i++) {
            filteredNames[i] = filteredFiles.get(i).getName();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                android.R.layout.simple_list_item_multiple_choice, filteredNames);
        final ListView fList = mList;
        fList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        fList.setAdapter(adapter);
        mButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                SparseBooleanArray positions = fList.getCheckedItemPositions();
                ArrayList<File> selectedFiles = new ArrayList<File>();
                for (int i = 0; i < files.length; i++) {
                    if (positions.get(i)) {
                        selectedFiles.add(files[i]);
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
                        public void onImportFinish(boolean success, ArrayList<Portal> portals) {
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
        });
    }

    @Override
    public void setServiceConnection(LocationServiceWrap service) {
        mService = service;
    }

    @Override
    public void onServiceConnected() {
        if (mButton != null) {
            mButton.setEnabled(true);
        }
    }

    @Override
    public void onServiceDisconnected() {
        if (mButton != null) {
            mButton.setEnabled(false);
        }
    }

}
