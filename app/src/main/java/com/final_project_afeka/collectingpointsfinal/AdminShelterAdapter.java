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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONObject;

import java.util.ArrayList;

public class AdminShelterAdapter extends ArrayAdapter<SafePoint> {
    private static final String TAG = AdminShelterAdapter.class.getSimpleName();
    private Context mContext;
    private int mResource;
    private ArrayList<SafePoint> pendingSheltersList;
    private ArrayList<Marker> pendingMarkersList;
    private ArrayList<Integer> shelterIdPending;
    private GoogleMap mMap;
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private RequestQueue mQueue;



    public AdminShelterAdapter(Context context, int resource , ArrayList<SafePoint> pendingSheltersList, ArrayList<Marker> pendingMarkersList, ArrayList<Integer> shelterIdPending , GoogleMap mMap){
        super(context, resource, pendingSheltersList);
        mContext = context;
        mQueue = Volley.newRequestQueue(context);
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
        final int shelterId = getItem(position).getId();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource,parent,false);
        TextView tx_address = (TextView) convertView.findViewById(R.id.listAddress);
        TextView tx_email = (TextView) convertView.findViewById(R.id.listEmail);
        ImageButton bt_confirm = (ImageButton) convertView.findViewById(R.id.confirm_button);
        ImageButton bt_delete = (ImageButton) convertView.findViewById(R.id.delete_button);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pendingMarkersList.get(position).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.green_shelter2));
                Log.d(TAG, "onClick: shelter id: "+shelterId);
                approvePoint(shelterId ,pendingMarkersList.get(position), getItem(position).getEmail());
                Toast.makeText(mContext,"מחסה אושר",Toast.LENGTH_LONG).show();
                pendingSheltersList.remove(position);
                notifyDataSetChanged();

            }
        });
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: shelter Id "+shelterId);
                DeclinePoint(shelterId ,pendingMarkersList.get(position), getItem(position).getEmail());
                Toast.makeText(mContext,"מחסה לא אושר",Toast.LENGTH_LONG).show();
                pendingSheltersList.remove(position);
                notifyDataSetChanged();
            }
        });
        tx_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = pendingSheltersList.get(position).getLatitude();
                double lang = pendingSheltersList.get(position).getLongitude();
                CameraUpdate location =CameraUpdateFactory.newLatLng(new LatLng(lat,lang));
                mMap.animateCamera(location);
            }
        });

        tx_address.setText(address);
        tx_email.setText(email);
        return convertView;


    }

    private void approvePoint(int shelterId, Marker marker, final String email) {
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.green_shelter2));

        // Approve point
        String url = mContext.getString(R.string.server_ip)+"/management/shelters?id="+shelterId; // update shelter
        JsonObjectRequest requestUpdateSafePoint = new JsonObjectRequest(Request.Method.PUT, url,null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: respone"+ response );
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        String userUrl = mContext.getString(R.string.server_ip)+"/management/users/points_approved?email="+email; // add point to user
        JsonObjectRequest requestAddPointUser = new JsonObjectRequest(Request.Method.PUT, userUrl,null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: respone"+ response );
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(requestUpdateSafePoint);
        mQueue.add(requestAddPointUser);
    }

    private void DeclinePoint(int shelterId, Marker marker, final String email) {
        marker.remove();
        String url =  mContext.getString(R.string.server_ip)+"/management/shelters?id="+shelterId;
        JsonObjectRequest requestUpdateSafePoint = new JsonObjectRequest(Request.Method.DELETE, url,null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: respone"+ response );
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        String userUrl = mContext.getString(R.string.server_ip)+"/management/users/points_declined?email="+email;
        JsonObjectRequest requestDecreasePointUser = new JsonObjectRequest(Request.Method.PUT, userUrl,null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: respone"+ response );
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(requestUpdateSafePoint);
        mQueue.add(requestDecreasePointUser);
    }



    public void setMap(GoogleMap map) {
        this.mMap = map;
    }
}
