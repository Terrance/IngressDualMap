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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
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

    private SeekBar mSeekFilterRange;
    private TextView mTextFilterRange;
    private RadioButton mRadioFilterAlignOff;
    private CheckBox[] mCheckFilterAligns;
    private RadioButton mRadioFilterLevelOff;
    private CheckBox[] mCheckFilterLevels;
    private SeekBar mSeekResoBuzz1, mSeekResoBuzz2;
    private TextView mTextResoBuzz1, mTextResoBuzz2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_settings, container, false);
        mSeekFilterRange = (SeekBar) view.findViewById(R.id.seek_filterRange);
        mTextFilterRange = (TextView) view.findViewById(R.id.text_filterRange);
        mRadioFilterAlignOff = (RadioButton) view.findViewById(R.id.radio_filterAlignOff);
        mCheckFilterAligns = new CheckBox[]{
            (CheckBox) view.findViewById(R.id.check_filterAlignUndefined),
            (CheckBox) view.findViewById(R.id.check_filterAlignNeutral),
            (CheckBox) view.findViewById(R.id.check_filterAlignRes),
            (CheckBox) view.findViewById(R.id.check_filterAlignEnl)
        };
        mRadioFilterLevelOff = (RadioButton) view.findViewById(R.id.radio_filterLevelOff);
        mCheckFilterLevels = new CheckBox[]{
            (CheckBox) view.findViewById(R.id.check_filterLevelUndefined),
            (CheckBox) view.findViewById(R.id.check_filterLevel1),
            (CheckBox) view.findViewById(R.id.check_filterLevel2),
            (CheckBox) view.findViewById(R.id.check_filterLevel3),
            (CheckBox) view.findViewById(R.id.check_filterLevel4),
            (CheckBox) view.findViewById(R.id.check_filterLevel5),
            (CheckBox) view.findViewById(R.id.check_filterLevel6),
            (CheckBox) view.findViewById(R.id.check_filterLevel7),
            (CheckBox) view.findViewById(R.id.check_filterLevel8)
        };
        mSeekResoBuzz1 = (SeekBar) view.findViewById(R.id.seek_resobuzz1);
        mSeekResoBuzz2 = (SeekBar) view.findViewById(R.id.seek_resobuzz2);
        mTextResoBuzz1 = (TextView) view.findViewById(R.id.text_resobuzz1);
        mTextResoBuzz2 = (TextView) view.findViewById(R.id.text_resobuzz2);
        // Distance ranges up to 100m, in 0.1m intervals
        mSeekFilterRange.setMax(1000);
        mSeekResoBuzz1.setMax(1000);
        mSeekResoBuzz2.setMax(1000);
        // Move other sliders on change to keep values consistent (ensure max >= min etc.)
        mSeekFilterRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTextFilterRange.setText((Float.valueOf(progress) / 10) + "m");
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
                if (seekBar.getProgress() > mSeekFilterRange.getProgress()) {
                    mSeekFilterRange.setProgress(progress);
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
                if (seekBar.getProgress() > mSeekFilterRange.getProgress()) {
                    mSeekFilterRange.setProgress(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // Selecting a radio clears all checkboxes
        mRadioFilterAlignOff.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean checked) {
                for (CheckBox filterAlign : mCheckFilterAligns) {
                    filterAlign.setChecked(false);
                }
                view.setChecked(checked);
            }
        });
        mRadioFilterLevelOff.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean checked) {
                for (CheckBox filterLevel : mCheckFilterLevels) {
                    filterLevel.setChecked(false);
                }
                view.setChecked(checked);
            }
        });
        // Selecting a checkbox clears the radio, clearing all checkboxes should re-check radio
        for (CheckBox filterAlign : mCheckFilterAligns) {
            filterAlign.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton view, boolean checked) {
                    if (checked) {
                        mRadioFilterAlignOff.setChecked(false);
                    } else {
                        boolean none = true;
                        for (CheckBox filterAlign : mCheckFilterAligns) {
                            if (filterAlign.isChecked()) {
                                none = false;
                            }
                        }
                        if (none) {
                            mRadioFilterAlignOff.setChecked(true);
                        }
                    }
                    view.setChecked(checked);
                }
            });
        }
        for (CheckBox filterLevel : mCheckFilterLevels) {
            filterLevel.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton view, boolean checked) {
                    if (checked) {
                        mRadioFilterLevelOff.setChecked(false);
                    } else {
                        boolean none = true;
                        for (CheckBox filterLevel : mCheckFilterLevels) {
                            if (filterLevel.isChecked()) {
                                none = false;
                            }
                        }
                        if (none) {
                            mRadioFilterLevelOff.setChecked(true);
                        }
                    }
                    view.setChecked(checked);
                }
            });
        }
        // Prefill with settings from SharedPreferences
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
        if (mSeekFilterRange == null) {
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
        editor.putInt("filterRange", mSeekFilterRange.getProgress());
        editor.putInt("resoBuzz1", mSeekResoBuzz1.getProgress());
        editor.putInt("resoBuzz2", mSeekResoBuzz2.getProgress());
        editor.commit();
        List<String> keys = new ArrayList<String>(SettingsFragment.DEFAULTS.keySet());
        Collections.sort(keys);
        int[] settings = new int[DEFAULTS.size()];
        int i = 0;
        for (String key : keys) {
            settings[i] = prefs.getInt(key, DEFAULTS.get(key));
            i++;
        }
        // 4 alignment filters + 9 level filters
        boolean[] filters = new boolean[13];
        i = 0;
        boolean filterAlignAll = mRadioFilterAlignOff.isChecked();
        for (CheckBox filterAlign : mCheckFilterAligns) {
            filters[i] = filterAlignAll || filterAlign.isChecked();
            i++;
        }
        boolean filterLevelAll = mRadioFilterLevelOff.isChecked();
        for (CheckBox filterLevel : mCheckFilterLevels) {
            filters[i] = filterLevelAll || filterLevel.isChecked();
            i++;
        }
        mService.refreshSettings(settings, filters);
        Toast.makeText(mActivity, "Settings have been saved.", Toast.LENGTH_SHORT).show();
    }

    /**
     * Run as soon as the fragment is loaded and ready.
     */
    public void autorun() {
        SharedPreferences prefs = mActivity.getSharedPreferences("settings", PREFS_MODE);
        mSeekFilterRange.setProgress(prefs.getInt("notifRange", DEFAULTS.get("notifRange")));
        mSeekResoBuzz1.setProgress(prefs.getInt("resoBuzz1", DEFAULTS.get("resoBuzz1")));
        mSeekResoBuzz2.setProgress(prefs.getInt("resoBuzz2", DEFAULTS.get("resoBuzz2")));
        mTextFilterRange.setText((Float.valueOf(mSeekFilterRange.getProgress()) / 10) + "m");
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
