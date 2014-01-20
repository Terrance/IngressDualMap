package to.uk.terrance.ingressdualmap;

import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private boolean mBound = false;
    private ServiceConnection mConnection;
    private ILocationService mLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        ((ToggleButton) findViewById(R.id.main_btn_service)).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                intent.addCategory(Utils.TAG);
                try {
                    if (mBound && mLocationService != null && mLocationService.isRunning()) {
                        stopService(intent);
                    } else {
                        startService(intent);
                    }
                } catch (RemoteException e) {
                    startService(intent);
                }
            }
        });
        ((Button) findViewById(R.id.main_btn_test)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                Location location = LocationService.getLastLocation();
                try {
                    if (mBound && mLocationService != null && mLocationService.isRunning()) {
                        if (location == null) {
                            Toast.makeText(MainActivity.this, "Waiting for a location...", Toast.LENGTH_SHORT).show();
                        } else {
                            Portal portal = new Portal("Test", location.getLatitude(), location.getLongitude());
                            portal.setDistance(0);
                            Portal.PORTALS.add(portal);
                            LocationService.notifyPortal(MainActivity.this, Portal.PORTALS.size() - 1, true);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "You need to start the service first!", Toast.LENGTH_SHORT).show();
                    }
                } catch (RemoteException e) {
                    Toast.makeText(MainActivity.this, "You need to start the service first!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        ((Button) findViewById(R.id.main_btn_notif)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                LocationService.clearNotifs();
            }
        });
        ((Button) findViewById(R.id.main_btn_ingress)).setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                startActivity(getPackageManager().getLaunchIntentForPackage("com.nianticproject.ingress"));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        connectService();
    }

    @Override
    public void onPause() {
        super.onPause();
        disconnectService();
    }

    public void connectService() {
        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder service) {
                mLocationService = ILocationService.Stub.asInterface(service);
                ((ToggleButton) findViewById(R.id.main_btn_service)).setChecked(true);
            }
            public void onServiceDisconnected(ComponentName className) {
                mLocationService = null;
                ((ToggleButton) findViewById(R.id.main_btn_service)).setChecked(false);
                connectService();
            }
        };
        mBound = bindService(new Intent(this, LocationService.class), mConnection, 0);
    }

    public void disconnectService() {
        unbindService(mConnection);
        mBound = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.act_main, menu);
        return true;
    }

}
