package to.uk.terrance.ingressdualmap;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;

import au.com.bytecode.opencsv.CSVReader;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;

/**
 * Background tasks related to managing portal lists.
 */
public class PortalStore {

    /**
     * Callback for an {@link ImportFilesTask} instance.
     */
    public static interface ImportListener {
        /**
         * Called at the start of importing each selected file.
         * @param fileName The file currently being processed.
         * @param percent A percentage of progress out of all files in the task. 
         */
        void onImportProgress(String fileName, int percent);
        /**
         * Called once all files have been processed.
         * @param success <code>True</code> if all files were imported successfully.
         * @param portals A collection of all successfully imported portals. 
         */
        void onImportFinish(boolean success, List<Portal> portals);
    }

    /**
     * A background task to import portals from their lists into the service.
     */
    public static class ImportFilesTask extends AsyncTask<ImportListener, String, Void> {

        private ImportListener[] mListeners;
        private List<File> mFiles;
        private int mPos = 0;
        private List<Portal> mPortals = new ArrayList<Portal>();
        private boolean mSuccess = true;

        /**
         * A background task to import portals from their lists into the service.
         * @param files An array of files to import portals from.
         */
        public ImportFilesTask(List<File> files) {
            mFiles = files;
        }

        @Override
        protected Void doInBackground(ImportListener... listeners) {
            mListeners = listeners;
            for (mPos = 0; mPos < mFiles.size(); mPos++) {
                File file = mFiles.get(mPos);
                publishProgress(file.getName());
                ArrayList<String> messages = new ArrayList<String>();
                try {
                    CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file)));
                    try {
                        int line = 1;
                        while (true) {
                            try {
                                String[] params = reader.readNext();
                                if (params == null) {
                                    break;
                                } else {
                                    Portal portal = new Portal(params[0], Float.valueOf(params[1]), Float.valueOf(params[2]));
                                    if (!mPortals.contains(portal)) {
                                        mPortals.add(portal);
                                    }
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                messages.add(line + ") Insufficient arguments.");
                            } catch (NumberFormatException e) {
                                messages.add(line + ") Unable to parse coordinates.");
                            }
                            line++;
                        }
                    } catch (IOException e) {
                        messages.add("IO exception on file: " + e.getLocalizedMessage());
                    }
                    reader.close();
                } catch (IOException e) {
                    messages.add("Unable to find file: " + e.getLocalizedMessage());
                }
                if (messages.size() > 0) {
                    mSuccess = false;
                    try {
                        OutputStream outputStream = new FileOutputStream(new File(file.getAbsolutePath() + ".log"), true);
                        SimpleDateFormat formatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]\n", Locale.getDefault());
                        outputStream.write(formatter.format(Calendar.getInstance().getTime()).getBytes());
                        for (String message : messages.toArray(new String[messages.size()])) {
                            outputStream.write((message + "\n").getBytes());
                        }
                        outputStream.close();
                    } catch (Exception e) {
                        Log.e(Utils.APP_TAG, "Failed to write out error log.", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... names) {
            for (ImportListener listener : mListeners) {
                listener.onImportProgress(names[0], (mPos / mFiles.size()) * 100);
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            for (ImportListener listener : mListeners) {
                listener.onImportFinish(mSuccess, mPortals);
            }
        }

    }

    /**
     * Helper class to represent available files.
     */
    public static class Download {

        public static final int STATE_NONE = 0;
        public static final int STATE_OLD = 1;
        public static final int STATE_CURRENT = 2;

        private String mLocation;
        private String mCategory;
        private String mFilename;
        private Date mLastUpdate;
        private int mLocalState = STATE_NONE;

        /**
         * Helper class to represent notification actions.
         * @param localFiles A list of local files to compare with the server.
         * @param params A list of location, category, filename and last update time.
         */
        public Download(Map<String, File> localFiles, String[] params) {
            mLocation = params[0];
            mCategory = params[1];
            mFilename = params[2];
            try {
                mLastUpdate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(params[3]);
            } catch (ParseException e) {
                mLastUpdate = new Date();
            }
            if (localFiles.containsKey(mFilename)) {
                File file = localFiles.get(mFilename);
                mLocalState = (mLastUpdate.getTime() > file.lastModified() ? STATE_OLD : STATE_CURRENT);
            }
        }

        /**
         * @return The location that the file covers.
         */
        public String getLocation() {
            return mLocation;
        }

        /**
         * @return The category of the file.
         */
        public String getCategory() {
            return mCategory;
        }

        /**
         * @return The name of the actual file.
         */
        public String getFilename() {
            return mFilename;
        }

        /**
         * @return The date the file was last updated on the server.
         */
        public Date getLastUpdate() {
            return mLastUpdate;
        }

        /**
         * @return The state of the local version.
         */
        public int getLocalState() {
            return mLocalState;
        }

    }

    /**
     * Callback for a {@link QueryFilesTask} instance.
     */
    public static interface QueryListener {
        void onQueryFinish(boolean success, List<Download> downloads);
    }

    /**
     * A background task to query available portal lists from the server.
     */
    public static class QueryFilesTask extends AsyncTask<QueryListener, Void, Void> {

        private QueryListener[] mListeners;
        private List<Download> mDownloads;
        private boolean mSuccess = true;

        @Override
        protected Void doInBackground(QueryListener... listeners) {
            mListeners = listeners;
            Webb webb = Webb.create();
            webb.setBaseUri(Utils.URL_LISTS);
            File folder = Utils.extStore();
            Map<String, File> localFiles = new HashMap<String, File>();
            for (File file : folder.listFiles()) {
                localFiles.put(file.getName(), file);
            }
            try {
                String content = webb.post("/dir.php").ensureSuccess().asString().getBody();
                mDownloads = new ArrayList<Download>();
                CSVReader reader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes("UTF-8"))));
                while (true) {
                    String[] params;
                    try {
                        params = reader.readNext();
                        if (params == null) {
                            break;
                        }
                        mDownloads.add(new Download(localFiles, params));
                    } catch (IOException e) {
                        Log.e(Utils.APP_TAG, "Failed to decode list.", e);
                        mSuccess = false;
                    }
                }
                reader.close();
                Log.i(Utils.APP_TAG, "Lists queried successfully.");
            } catch (IOException e) {
                Log.e(Utils.APP_TAG, "Failed to decode list.", e);
                mSuccess = false;
            } catch (WebbException e) {
                Log.e(Utils.APP_TAG, "Failed to query list.", e);
                mSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            for (QueryListener listener : mListeners) {
                listener.onQueryFinish(mSuccess, mDownloads);
            }
        }

    }

    /**
     * Callback for an {@link DownloadFilesTask} instance.
     */
    public static interface DownloadListener {
        void onDownloadProgress(String fileName, int percent);
        void onDownloadFinish(boolean success);
    }

    /**
     * A background task to download portal lists from the server.
     */
    public static class DownloadFilesTask extends AsyncTask<DownloadListener, String, Void> {

        private DownloadListener[] mListeners;
        private ArrayList<String> mFiles;
        private int mPos = 0;
        private boolean mSuccess = true;

        /**
         * A background task to download portal lists from the server.
         * @param files An array of file names to download.
         */
        public DownloadFilesTask(ArrayList<String> files) {
            mFiles = files;
        }

        @Override
        protected Void doInBackground(DownloadListener... listeners) {
            mListeners = listeners;
            Webb webb = Webb.create();
            webb.setBaseUri(Utils.URL_LISTS);
            File folder = Utils.extStore();
            ArrayList<String> messages = new ArrayList<String>();
            for (mPos = 0; mPos < mFiles.size(); mPos++) {
                String file = mFiles.get(mPos);
                publishProgress(file);
                try {
                    String list = webb.post("/" + file.replace(" ", "%20")).ensureSuccess().asString().getBody();
                    OutputStream outputStream = new FileOutputStream(new File(folder.getAbsolutePath() + "/" + file));
                    outputStream.write(list.getBytes());
                    outputStream.close();
                    Log.i(Utils.APP_TAG, "Successfully downloaded " + file + ".");
                } catch (WebbException e) {
                    messages.add("Failed to download list " + file + ".");
                    Log.e(Utils.APP_TAG, messages.get(messages.size() - 1), e);
                } catch (Exception e) {
                    Log.e(Utils.APP_TAG, "Failed to write list " + file + " to file.", e);
                    Log.e(Utils.APP_TAG, messages.get(messages.size() - 1), e);
                }
            }
            if (messages.size() > 0) {
                mSuccess = false;
                try {
                    OutputStream outputStream = new FileOutputStream(new File(folder.getPath() + "/.download.log"), true);
                    SimpleDateFormat formatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]\n", Locale.getDefault());
                    outputStream.write(formatter.format(Calendar.getInstance().getTime()).getBytes());
                    for (String message : messages.toArray(new String[messages.size()])) {
                        outputStream.write((message + "\n").getBytes());
                    }
                    outputStream.close();
                } catch (Exception e) {
                    Log.e(Utils.APP_TAG, "Failed to write out error log.", e);
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... names) {
            for (DownloadListener listener : mListeners) {
                listener.onDownloadProgress(names[0], (mPos / mFiles.size()) * 100);
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            for (DownloadListener listener : mListeners) {
                listener.onDownloadFinish(mSuccess);
            }
        }

    }

}
