package to.uk.terrance.ingressdualmap;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.app.ActionBarActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Fragment for downloading portal lists.
 */
public class MapsFragment extends SupportMapFragment implements ILocationServiceFragment {

    private ActionBarActivity mActivity;
    private LocationServiceWrap mService;
    private static View mView;
    private GoogleMap mMap;
    private List<Portal> mPortals;
    private HashMap<Marker, Portal> mPairs = new HashMap<Marker, Portal>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Recycle GoogleMap to avoid duplicate error
        if (mView != null) {
            ViewGroup parent = (ViewGroup) mView.getParent();
            if (parent != null) {
                parent.removeView(mView);
            }
        }
        try {
            mView = inflater.inflate(R.layout.frag_maps, container, false);
        } catch (InflateException e) {
            // Map already loaded, just return as-is
        }
        setHasOptionsMenu(true);
        return mView;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mActivity = (ActionBarActivity) activity;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.frag_maps, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_layer:
                layer();
                return true;
            case R.id.menu_plot:
                plot();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        SupportMapFragment mapFrag = (SupportMapFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = mapFrag.getMap();
        mMap.setMyLocationEnabled(true);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            private final View view = getLayoutInflater(null).inflate(R.layout.info_maps, null);
            @Override
            public View getInfoContents(Marker mark) {
                Portal portal = mService.getPortal(mPortals.indexOf(mPairs.get(mark)));
                int hacks = portal.getHacksRemaining();
                String text = "<b>" + portal.getName() + "</b><br/>";
                text += hacks + " more hack" + Utils.plural(hacks);
                int keys = portal.getKeys();
                if (keys > 0) {
                    text += " | " + keys + " key" + Utils.plural(keys);
                }
                int burnedOutTime = portal.checkBurnedOut();
                if (burnedOutTime > 0) {
                    String time = Utils.shortTime(burnedOutTime);
                    text += "<br/>Burned out: wait " + time;
                } else {
                    int runningHotTime = portal.checkRunningHot();
                    if (runningHotTime > 0) {
                        String time = Utils.shortTime(runningHotTime);
                        text += "<br/>Running hot: wait " + time;
                    }
                }
                ((TextView) view.findViewById(R.id.text_info)).setText(Html.fromHtml(text));
                return view;
            }
            @Override
            public View getInfoWindow(Marker mark) {
                return null;
            }
        });
    }

    public void layer() {
        String type = "Normal";
        switch (mMap.getMapType()) {
            case GoogleMap.MAP_TYPE_NORMAL:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                type = "Hybrid";
                break;
            case GoogleMap.MAP_TYPE_HYBRID:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                type = "Terrain";
                break;
            case GoogleMap.MAP_TYPE_TERRAIN:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
        }
        Toast.makeText(mActivity, "Changed map layer to " + type + ".", Toast.LENGTH_SHORT).show();
    }

    public void plot() {
        mMap.clear();
        mPortals = mService.getAllPortals();
        if (mPortals != null) {
            Log.d(Utils.APP_TAG, "Plotting " + mPortals.size() + " portals.");
            for (Portal portal : mPortals) {
                LatLng pos = new LatLng(portal.getLatitude(), portal.getLongitude());
                MarkerOptions opts = new MarkerOptions().position(pos).title(portal.getName()).visible(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_portal));
                Marker mark = mMap.addMarker(opts);
                mPairs.put(mark, portal);
            }
        }
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker mark) {
                Portal portal = mPairs.get(mark);
                Intent optsIntent = new Intent(mActivity, MainActivity.class);
                optsIntent.setAction(Utils.APP_PACKAGE + ".opts." + mPortals.indexOf(portal));
                startActivity(optsIntent);
            }
        });
    }

    @Override
    public void setServiceConnection(LocationServiceWrap service) {
        mService = service;
    }

    @Override
    public void onServiceConnected() {
    }

    @Override
    public void onServiceDisconnected() {
    }

}
