package to.uk.terrance.ingressdualmap;

import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListArrayAdapter extends ArrayAdapter<Portal> {

    private final Context mContext;
    private List<Portal> mPortals;
    private double[] mLastLocation;
    private SparseBooleanArray mSelected;

    // Fragment text views
    private TextView textAlignment, textLevel, textName, textMeta1;

    public ListArrayAdapter(Context context, List<Portal> portals, double[] lastLocation) {
        super(context, R.layout.list_list, portals);
        mContext = context;
        mPortals = portals;
        mLastLocation = lastLocation;
        mSelected = new SparseBooleanArray();
    }

    public void setPortals(List<Portal> portals) {
        mPortals = portals;
        notifyDataSetChanged();
    }

    public void setLastLocation(double[] lastLocation) {
        mLastLocation = lastLocation;
        notifyDataSetChanged();
    }

    @Override
    public boolean isEmpty() {
        return mPortals.size() == 0;
    }

    @Override
    public int getCount() {
        return mPortals.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_list, parent, false);
        textAlignment = (TextView) rowView.findViewById(R.id.text_alignment);
        textLevel = (TextView) rowView.findViewById(R.id.text_level);
        textName = (TextView) rowView.findViewById(R.id.text_name);
        textMeta1 = (TextView) rowView.findViewById(R.id.text_meta1);
        Portal portal = mPortals.get(position);
        if (portal.getAlignment() > 0) {
            String[] aligns = new String[]{"N", "R", "E"};
            textAlignment.setText(aligns[portal.getAlignment() - 1]);
            textAlignment.setTextColor(Color.parseColor(Utils.COLOUR_ALIGNMENT[portal.getAlignment()]));
            textAlignment.setVisibility(View.VISIBLE);
        }
        if (portal.getLevel() > 0) {
            textLevel.setText(String.valueOf(portal.getLevel()));
            textLevel.setTextColor(Color.parseColor(Utils.COLOUR_LEVEL[portal.getLevel()]));
            textLevel.setVisibility(View.VISIBLE);
        }
        textName.setText(portal.getName());
        String meta = "";
        if (portal.getKeys() > 0) {
            meta += Utils.unicode(0x1F511) + " " + portal.getKeys() + "  |  ";
        }
        if (mLastLocation != null) {
            float[] distance = new float[1];
            Location.distanceBetween(mLastLocation[0], mLastLocation[1],
                    portal.getLatitude(), portal.getLongitude(), distance);
            meta += Utils.shortDist(distance[0]);
        } else {
            DecimalFormat format = new DecimalFormat("0.000000");
            meta += format.format(portal.getLatitude()) + ", " + format.format(portal.getLongitude());
        }
        textMeta1.setText(meta);
        if (mSelected.get(position)) {
            rowView.setBackgroundColor(Color.parseColor("#60ccee"));
        }
        return rowView;
    }

    public void setSelection(int position, boolean selected) {
        mSelected.put(position, selected);
    }

    public void toggleSelection(int position) {
        mSelected.put(position, !mSelected.get(position));
    }

    public void clearSelections() {
        mSelected.clear();
    }

    public int getSelectionCount() {
        int count = 0;
        for (int i = 0; i < mPortals.size(); i++) {
            if (mSelected.get(i)) {
                count++;
            }
        }
        return count;
    }

}
