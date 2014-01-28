package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.Fragment;

/**
 * Fragment for configuring app settings.
 */
public class SettingsFragment extends Fragment implements ILocationServiceFragment {

    /**
     * Set of default settings to use before writing for the first time.
     */
    @SuppressWarnings("serial")
    public static final HashMap<String, Integer> DEFAULTS = new HashMap<String, Integer>() {
        {
            put("notifRange", 500);
            put("resoBuzz1", 350);
            put("resoBuzz2", 400);
        }
    };

    /**
     * Multi-process mode is implied for Gingerbread and below, must be explicitly declared otherwise.
     */
    @SuppressLint("InlinedApi")
    public static int PREFS_MODE = android.os.Build.VERSION.SDK_INT >= 11 ? Context.MODE_MULTI_PROCESS : Context.MODE_PRIVATE;

    private Activity mActivity;
    private boolean mDelay = false;
    private LocationServiceWrap mService;

    private SeekBar mSeekNotifRange;
    private TextView mTextNotifRange;
    private SeekBar mSeekResoBuzz1, mSeekResoBuzz2;
    private TextView mTextResoBuzz1, mTextResoBuzz2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings, container, false);
        mSeekNotifRange = (SeekBar) view.findViewById(R.id.seek_notifrange);
        mTextNotifRange = (TextView) view.findViewById(R.id.text_notifrange);
        mSeekResoBuzz1 = (SeekBar) view.findViewById(R.id.seek_resobuzz1);
        mSeekResoBuzz2 = (SeekBar) view.findViewById(R.id.seek_resobuzz2);
        mTextResoBuzz1 = (TextView) view.findViewById(R.id.text_resobuzz1);
        mTextResoBuzz2 = (TextView) view.findViewById(R.id.text_resobuzz2);
        mSeekNotifRange.setMax(1000);
        mSeekResoBuzz1.setMax(1000);
        mSeekResoBuzz2.setMax(1000);
        // Move other sliders on change to keep values consistent (ensure max >= min etc.)
        mSeekNotifRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextNotifRange.setText((Float.valueOf(progress) / 10) + "m");
                if (seekBar.getProgress() < mSeekResoBuzz1.getProgress()) {
                    mSeekResoBuzz1.setProgress(progress);
                }
                if (seekBar.getProgress() < mSeekResoBuzz2.getProgress()) {
                    mSeekResoBuzz2.setProgress(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        mSeekResoBuzz1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextResoBuzz1.setText((Float.valueOf(progress) / 10) + "m");
                if (seekBar.getProgress() > mSeekResoBuzz2.getProgress()) {
                    mSeekResoBuzz2.setProgress(progress);
                }
                if (seekBar.getProgress() > mSeekNotifRange.getProgress()) {
                    mSeekNotifRange.setProgress(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        mSeekResoBuzz2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextResoBuzz2.setText((Float.valueOf(progress) / 10) + "m");
                if (seekBar.getProgress() < mSeekResoBuzz1.getProgress()) {
                    mSeekResoBuzz1.setProgress(progress);
                }
                if (seekBar.getProgress() > mSeekNotifRange.getProgress()) {
                    mSeekNotifRange.setProgress(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        if (mDelay) {
            autorun();
        }
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        // Delay autorun until view is created
        if (mSeekNotifRange == null) {
            mDelay = true;
        } else {
            autorun();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frag_settings, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void save() {
        SharedPreferences prefs = mActivity.getSharedPreferences("settings", PREFS_MODE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("notifRange", mSeekNotifRange.getProgress());
        editor.putInt("resoBuzz1", mSeekResoBuzz1.getProgress());
        editor.putInt("resoBuzz2", mSeekResoBuzz2.getProgress());
        editor.commit();
        List<String> keys = new ArrayList<String>(SettingsFragment.DEFAULTS.keySet());
        Collections.sort(keys);
        int[] values = new int[DEFAULTS.size()];
        int i = 0;
        for (String key : keys) {
            values[i] = prefs.getInt(key, DEFAULTS.get(key));
            i++;
        }
        mService.refreshSettings(values);
        Toast.makeText(mActivity, "Settings have been saved.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Run as soon as the fragment is loaded and ready.
     */
    public void autorun() {
        SharedPreferences prefs = mActivity.getSharedPreferences("settings", PREFS_MODE);
        mSeekNotifRange.setProgress(prefs.getInt("notifRange", DEFAULTS.get("notifRange")));
        mSeekResoBuzz1.setProgress(prefs.getInt("resoBuzz1", DEFAULTS.get("resoBuzz1")));
        mSeekResoBuzz2.setProgress(prefs.getInt("resoBuzz2", DEFAULTS.get("resoBuzz2")));
        mTextNotifRange.setText((Float.valueOf(mSeekNotifRange.getProgress()) / 10) + "m");
        mTextResoBuzz1.setText((Float.valueOf(mSeekResoBuzz1.getProgress()) / 10) + "m");
        mTextResoBuzz2.setText((Float.valueOf(mSeekResoBuzz2.getProgress()) / 10) + "m");
    }

    @Override
    public void setServiceConnection(LocationServiceWrap service) {
        mService = service;
    }

    @Override
    public void onServiceConnected() {}

    @Override
    public void onServiceDisconnected() {}

}
