package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
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
public class DownloadFragment extends Fragment {

    private Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_download, container, false);
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        autorun();
    }

    public void autorun() {
        final ProgressDialog progress = new ProgressDialog(mActivity);
        progress.setTitle(getString(R.string.download_portal_lists));
        progress.setMessage("Checking available files...");
        progress.setCancelable(false);
        progress.show();
        (new PortalStore.QueryFilesTask()).execute(new PortalStore.QueryListener() {
            @Override
            public void onQueryFinish(boolean success, final String[] files) {
                if (success) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                            android.R.layout.simple_list_item_multiple_choice, files);
                    final ListView list = (ListView) getView().findViewById(R.id.list_import);
                    list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                    list.setAdapter(adapter);
                    ((Button) getView().findViewById(R.id.btn_download)).setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SparseBooleanArray positions = list.getCheckedItemPositions();
                            ArrayList<String> selectedFiles = new ArrayList<String>();
                            for (int i = 0; i < files.length; i++) {
                                if (positions.get(i)) {
                                    selectedFiles.add(files[i]);
                                }
                            }
                            if (selectedFiles.size() > 0) {
                                final ProgressDialog progress = new ProgressDialog(mActivity);
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
                                            Toast.makeText(mActivity, "The portal lists were downloaded successfully.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(mActivity, "Errors occurred whilst downloading the portal lists.", Toast.LENGTH_LONG).show();
                                        }
                                        progress.dismiss();
                                    }
                                });
                            } else {
                                Toast.makeText(mActivity, "No lists have been selected.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    progress.dismiss();
                } else {
                    final Button button = ((Button) getView().findViewById(R.id.btn_download));
                    button.setText(getString(R.string.retry));
                    button.setOnClickListener(new Button.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            button.setText(getString(R.string.download_lists));
                            autorun();
                        }
                    });
                    progress.dismiss();
                    Toast.makeText(mActivity, "Unable to query for available lists.\nCheck your connection and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
