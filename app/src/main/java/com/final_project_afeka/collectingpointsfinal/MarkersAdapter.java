package com.final_project_afeka.collectingpointsfinal;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idoshapira-mbp on 29/07/2018.
 */

public class MarkersAdapter extends ArrayAdapter<Marker> {

    private Context context;
    private List<Marker> markerNames = new ArrayList<>();

    public MarkersAdapter(Context applicationContext, int textViewResourceId, List<Marker> markers) {
        super(applicationContext,textViewResourceId,markers);
        this.context = applicationContext;
        this.markerNames = markers;
    }

    @Override
    public int getCount() {
        return markerNames.size();
    }

    @Override
    public Marker getItem(int position) {
        return markerNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView label = (TextView) super.getView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(markerNames.get(position).getTitle());
        return label;
    }
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = (TextView) super.getDropDownView(position, convertView, parent);
        label.setTextColor(Color.BLACK);
        label.setText(markerNames.get(position).getTitle());

        return label;
    }
}
