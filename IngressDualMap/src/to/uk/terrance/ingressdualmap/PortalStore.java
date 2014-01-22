package to.uk.terrance.ingressdualmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import au.com.bytecode.opencsv.CSVReader;

import com.goebl.david.Webb;
import com.goebl.david.WebbException;

public class PortalStore {

    public static interface ImportListener {
        void onImportProgress(String fileName, int percent);
        void onImportFinish(boolean success, ArrayList<Portal> portals);
    }

    public static class ImportFilesTask extends AsyncTask<ImportListener, String, Void> {

        private ImportListener[] mListeners;
        private ArrayList<File> mFiles;
        private int mPos = 0;
        private ArrayList<Portal> mPortals = new ArrayList<Portal>();
        private boolean mSuccess = true;

        public ImportFilesTask(ArrayList<File> files) {
            mFiles = files;
        }

        @Override
        public Void doInBackground(ImportListener... listeners) {
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
                } catch (FileNotFoundException e) {
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

    public static interface QueryListener {
        void onQueryFinish(boolean success, String[] files);
    }

    public static class QueryFilesTask extends AsyncTask<QueryListener, Void, Void> {

        private QueryListener[] mListeners;
        private String[] mFiles;
        private boolean mSuccess = true;

        @Override
        public Void doInBackground(QueryListener... listeners) {
            mListeners = listeners;
            Webb webb = Webb.create();
            webb.setBaseUri(Utils.URL_LISTS);
            try {
                mFiles = webb.post("/lists.txt").ensureSuccess().asString().getBody().split("\r\n");
                Log.i(Utils.APP_TAG, "Lists queried successfully.");
            } catch (WebbException e) {
                Log.e(Utils.APP_TAG, "Failed to query list.", e);
                mSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            for (QueryListener listener : mListeners) {
                listener.onQueryFinish(mSuccess, mFiles);
            }
        }

    }

    public static interface DownloadListener {
        void onDownloadProgress(String fileName, int percent);
        void onDownloadFinish(boolean success);
    }

    public static class DownloadFilesTask extends AsyncTask<DownloadListener, String, Void> {

        private DownloadListener[] mListeners;
        private ArrayList<String> mFiles;
        private int mPos = 0;
        private boolean mSuccess = true;

        public DownloadFilesTask(ArrayList<String> files) {
            mFiles = files;
        }

        @Override
        public Void doInBackground(DownloadListener... listeners) {
            mListeners = listeners;
            Webb webb = Webb.create();
            webb.setBaseUri(Utils.URL_LISTS);
            final File folder = new File(Environment.getExternalStorageDirectory() + "/IngressDualMap");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            ArrayList<String> messages = new ArrayList<String>();
            for (mPos = 0; mPos < mFiles.size(); mPos++) {
                String file = mFiles.get(mPos);
                publishProgress(file);
                try {
                    String list = webb.post("/" + file).ensureSuccess().asString().getBody();
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
                    OutputStream outputStream = new FileOutputStream(new File("[Download].log"), true);
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
