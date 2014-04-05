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
    private ToggleButton mToggle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_service, container, false);
        mToggle = (ToggleButton) view.findViewById(R.id.btn_toggle);
        mToggle.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService.isThreadRunning()) {
                    Toast.makeText(mActivity, "Thread stopped!", Toast.LENGTH_LONG).show();
                    mService.stopThread();
                    mToggle.setChecked(false);
                } else {
                    Toast.makeText(mActivity, "Thread started!", Toast.LENGTH_LONG).show();
                    mService.startThread();
                    mToggle.setChecked(true);
                }
            }
        });
        ((Button) view.findViewById(R.id.btn_clear)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationService.clearNotifs(mActivity);
            }
        });
        ((Button) view.findViewById(R.id.btn_exit)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity.getApplicationContext(), LocationService.class);
                intent.addCategory(Utils.APP_TAG);
                mActivity.stopService(intent);
                mActivity.finish();
            }
        });
        if (mService.isThreadRunning()) {
            mToggle.setChecked(true);
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
