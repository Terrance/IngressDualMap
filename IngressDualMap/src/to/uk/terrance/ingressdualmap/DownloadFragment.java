package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
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
public class DownloadFragment extends Fragment {

    private Activity mActivity;
    private boolean mDelay = false;
    private ListView mList;
    private Menu mMenu;
    private List<PortalStore.Download> mDownloads;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_download, container, false);
        mList = (ListView) view.findViewById(R.id.list_download);
        if (mDelay) {
            refresh();
        }
        setHasOptionsMenu(true);
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
            refresh();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
        inflater.inflate(R.menu.frag_download, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refresh();
                return true;
            case R.id.menu_download:
                download();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Refresh the list of available downloads from the server. 
     */
    public void refresh() {
        final ProgressDialog progress = new ProgressDialog(mActivity);
        progress.setTitle(getString(R.string.download_portal_lists));
        progress.setMessage("Checking available files...");
        progress.setCancelable(false);
        progress.show();
        (new PortalStore.QueryFilesTask()).execute(new PortalStore.QueryListener() {
            @Override
            public void onQueryFinish(boolean success, final List<PortalStore.Download> downloads) {
                if (success) {
                    mDownloads = downloads;
                    String[] mLabels = new String[mDownloads.size()];
                    SparseBooleanArray check = new SparseBooleanArray();
                    for (int i = 0; i < mDownloads.size(); i++) {
                        PortalStore.Download dl = mDownloads.get(i);
                        switch (dl.getLocalState()) {
                            case PortalStore.Download.STATE_NONE:
                                mLabels[i] = Utils.unicode(0x1F6AB);
                                break;
                            case PortalStore.Download.STATE_OLD:
                                mLabels[i] = Utils.unicode(0x2B06);
                                check.append(i, true);
                                break;
                            case PortalStore.Download.STATE_CURRENT:
                                mLabels[i] = Utils.unicode(0x2714);
                                break;
                        }
                        mLabels[i] += " " + dl.getLocation() + ", " + dl.getCategory();
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity,
                            android.R.layout.simple_list_item_multiple_choice, mLabels);
                    mList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                    mList.setAdapter(adapter);
                    for (int i = 0; i < mDownloads.size(); i++) {
                        if (check.get(i)) {
                            mList.setItemChecked(i, true);
                        }
                    }
                    progress.dismiss();
                    mMenu.findItem(R.id.menu_download).setVisible(true);
                } else {
                    progress.dismiss();
                    mMenu.findItem(R.id.menu_download).setVisible(false);
                    Toast.makeText(mActivity, "Unable to query for available lists.  Check your connection and try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Process the selected downloads.
     */
    public void download() {
        SparseBooleanArray positions = mList.getCheckedItemPositions();
        ArrayList<String> selectedFiles = new ArrayList<String>();
        for (int i = 0; i < mDownloads.size(); i++) {
            if (positions.get(i)) {
                selectedFiles.add(mDownloads.get(i).getFilename());
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
                        Toast.makeText(mActivity, "All downloads completed successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mActivity, "Some errors occurred whilst downloading the portal lists.  Check your connection and try again.", Toast.LENGTH_LONG).show();
                    }
                    progress.dismiss();
                    refresh();
                }
            });
        } else {
            Toast.makeText(mActivity, "No lists have been selected.", Toast.LENGTH_SHORT).show();
        }
    }

}
