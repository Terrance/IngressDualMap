package to.uk.terrance.ingressdualmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.support.v4.app.Fragment;

/**
 * Fragment for controlling the service status.
 */
public class ServiceFragment extends Fragment implements ILocationServiceFragment {

    private Activity mActivity;
    private LocationServiceWrap mService;
    private ToggleButton mButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_service, container, false);
        mButton = (ToggleButton) view.findViewById(R.id.btn_toggle);
        mButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity.getApplicationContext(), LocationService.class);
                intent.addCategory(Utils.APP_TAG);
                if (mService.isRunning()) {
                    Toast.makeText(mActivity, "Service stopped!", Toast.LENGTH_LONG).show();
                    mActivity.stopService(intent);
                    mButton.setChecked(false);
                } else {
                    Toast.makeText(mActivity, "Service started!", Toast.LENGTH_LONG).show();
                    mActivity.startService(intent);
                    mButton.setChecked(true);
                }
            }
        });
        ((Button) view.findViewById(R.id.btn_clear)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationService.clearNotifs(mActivity);
            }
        });
        if (mService.isRunning()) {
            mButton.setChecked(true);
        }
        return view;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void setServiceConnection(LocationServiceWrap service) {
        mService = service;
    }

    @Override
    public void onServiceConnected() {
        if (mButton != null) {
            mButton.setChecked(true);
        }
    }

    @Override
    public void onServiceDisconnected() {
        if (mButton != null) {
            mButton.setChecked(false);
        }
    }

}
