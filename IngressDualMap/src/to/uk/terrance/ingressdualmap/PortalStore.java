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

import au.com.bytecode.opencsv.CSVReader;

public class PortalStore {

    public static interface ImportListener {
        void onImportProgress(String fileName);
        void onImportFinish(boolean success, ArrayList<Portal> portals);
    }

    public static class ImportFilesTask extends AsyncTask<ImportListener, String, Void> {

        private ImportListener[] mListeners;
        private ArrayList<File> mFiles;
        private ArrayList<Portal> mPortals = new ArrayList<Portal>();
        private boolean mSuccess = true;

        public ImportFilesTask(ArrayList<File> files) {
            mFiles = files;
        }

        @Override
        public Void doInBackground(ImportListener... listeners) {
            mListeners = listeners;
            for (int i = 0; i < mFiles.size(); i++) {
                File file = mFiles.get(i);
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
                        OutputStream outputStream = new FileOutputStream(new File(file.getAbsolutePath() + ".log"));
                        SimpleDateFormat formatter = new SimpleDateFormat("[dd/MM/yyyy HH:mm:ss]", Locale.getDefault());
                        outputStream.write(formatter.format(Calendar.getInstance().getTime()).getBytes());
                        for (String message : messages.toArray(new String[messages.size()])) {
                            outputStream.write(message.getBytes());
                        }
                        outputStream.close();
                    } catch (Exception e) {}
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... names) {
            for (ImportListener listener : mListeners) {
                listener.onImportProgress(names[0]);
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            for (ImportListener listener : mListeners) {
                listener.onImportFinish(mSuccess, mPortals);
            }
        }

    }

}
