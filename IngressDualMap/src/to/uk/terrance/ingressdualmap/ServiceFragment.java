package to.uk.terrance.ingressdualmap;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.support.v4.app.Fragment;

/**
 * Fragment for controlling the service status.
 */
public class ServiceFragment extends Fragment implements ILocationServiceFragment {

    private Activity mActivity;
    private LocationServiceWrap mService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_service, container, false);
        final ToggleButton btnToggle = (ToggleButton) view.findViewById(R.id.btn_toggle);
        btnToggle.setOnClickListener(new ToggleButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService.isThreadRunning()) {
                    Toast.makeText(mActivity, "Notifications stopped.", Toast.LENGTH_SHORT).show();
                    mService.stopThread();
                    btnToggle.setChecked(false);
                } else {
                    Toast.makeText(mActivity, "Notifications started!", Toast.LENGTH_SHORT).show();
                    mService.startThread();
                    btnToggle.setChecked(true);
                }
            }
        });
        View btnDismiss = (View) view.findViewById(R.id.btn_dismiss);
        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationService.clearNotifs(mActivity);
            }
        });
        btnDismiss.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(mActivity, R.string.dismiss_notifications, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        View btnClear = (View) view.findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mActivity)
                    .setMessage("This will clear all currently imported lists, including the state of all portals.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mService.setPortals(new ArrayList<Portal>());
                            LocationService.clearNotifs(mActivity);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        btnClear.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(mActivity, R.string.clear_imported_lists, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        View btnExit = (View) view.findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(mActivity)
                    .setMessage("Stop the service and quit?  The state of all currently imported portals will be saved.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(mActivity.getApplicationContext(), LocationService.class);
                            intent.addCategory(Utils.APP_TAG);
                            mActivity.stopService(intent);
                            mActivity.finish();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        btnExit.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(mActivity, R.string.stop_and_exit, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        if (mService.isThreadRunning()) {
            btnToggle.setChecked(true);
        }
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        Intent intent = new Intent(activity.getApplicationContext(), LocationService.class);
        intent.addCategory(Utils.APP_TAG);
        activity.startService(intent);
        mActivity = activity;
    }

    @Override
    public void setServiceConnection(LocationServiceWrap service) {
        mService = service;
    }

}
