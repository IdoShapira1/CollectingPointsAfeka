package com.final_project_afeka.collectingpointsfinal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminShelterAdpter extends ArrayAdapter<Shelter> {
    private static final String TAG = AdminShelterAdpter.class.getSimpleName();
    private Context mContext;
    private int mResource;
    private ArrayList<Shelter> pendingSheltersList;
    private ArrayList<Marker> pendingMarkersList;
    private ArrayList<String> shelterIdPending;
    private GoogleMap mMap;
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    /**
     * Default constructor for the PersonListAdapter
     * @param context
     * @param resource
     * @param pendingSheltersList
     */
    public AdminShelterAdpter(Context context, int resource , ArrayList<Shelter> pendingSheltersList, ArrayList<Marker> pendingMarkersList, ArrayList<String> shelterIdPending ,GoogleMap mMap)
    {
        super(context, resource, pendingSheltersList);
        mContext = context;
        mResource = resource;
        this.pendingSheltersList=pendingSheltersList;
        this.pendingMarkersList=pendingMarkersList;
        this.shelterIdPending = shelterIdPending;
        this.mMap = mMap;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        String address = getItem(position).getAddress();
        String email = getItem(position).getEmail();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);


        TextView tx_address = (TextView) convertView.findViewById(R.id.listAddress);
        TextView tx_email = (TextView) convertView.findViewById(R.id.listEmail);
        ImageButton bt_confirm = (ImageButton) convertView.findViewById(R.id.confirm_button);
        ImageButton bt_delete = (ImageButton) convertView.findViewById(R.id.delete_button);

        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shelterId = shelterIdPending.get(position);
                pendingMarkersList.get(position).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.green_shelter2));
                Log.d(TAG, "onClick: shelter id: "+shelterId);
                approvePoint(shelterId ,pendingMarkersList.get(position), getItem(position).getuId());
                Toast.makeText(mContext,"מחסה אושר",Toast.LENGTH_LONG).show();
                pendingSheltersList.remove(position);
                notifyDataSetChanged();
            }
        });
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shelterId = shelterIdPending.get(position);
                Log.d(TAG, "onClick: shelter Id "+shelterId);
                DeclinePoint(shelterId ,pendingMarkersList.get(position), getItem(position).getuId());
                Toast.makeText(mContext,"מחסה לא אושר",Toast.LENGTH_LONG).show();
                pendingSheltersList.remove(position);
                notifyDataSetChanged();
            }
        });
        tx_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = pendingSheltersList.get(position).getLalatitudet();
                double lang = pendingSheltersList.get(position).getLongitude();
                CameraUpdate location =CameraUpdateFactory.newLatLng(new LatLng(lat,lang));
                mMap.animateCamera(location);
            }
        });

        tx_address.setText(address);
        tx_email.setText(email);
        return convertView;


    }

    private void approvePoint(final String shelterId, Marker marker, final String uID) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.green_shelter2));
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pointsApproved = dataSnapshot.child("users/"+uID).getValue(User.class).getPointsApproved();
                database.child("users/"+uID).child("pointsApproved").setValue(pointsApproved+1);
                database.child("shelters/"+shelterId).child("approved").setValue(1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void DeclinePoint(final String shelterId, Marker marker, final String uID) {
        marker.remove();
        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pointsDeclined = dataSnapshot.child("users/"+uID).getValue(User.class).getPointsDeclined();
                database.child("users/"+uID).child("pointsDeclined").setValue(pointsDeclined+1);
                database.child("shelters/"+shelterId).removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    public void setMap(GoogleMap map) {
        this.mMap = map;
    }
}
