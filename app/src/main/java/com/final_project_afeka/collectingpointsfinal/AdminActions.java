package com.final_project_afeka.collectingpointsfinal;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class AdminActions extends AppCompatActivity implements OnMapReadyCallback,AdapterView.OnItemClickListener {
    GoogleMap mMap;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 16;
    private LatLng mDefaultLocation = new LatLng(32.113819, 34.817794);
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private ArrayList<Marker> pendingMarkersList = new ArrayList<>();
    private ArrayList<SafePoint> pendingShelterList = new ArrayList<>();
    private ArrayList<Integer> shelterIdPending = new ArrayList<>();
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    AdminShelterAdapter adapter;
    ListView listView;
    private RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_actions);
        mQueue = Volley.newRequestQueue(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        listView = (ListView)findViewById(R.id.listView_admin);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragmentAdmin);
        mapFragment.getMapAsync(this);
        getAllShelters();
        adapter = new AdminShelterAdapter(this,R.layout.adapter_view_layout,pendingShelterList,pendingMarkersList,shelterIdPending,mMap);
      //  adapter = new AdminShelterAdapter(this,R.layout.adapter_view_layout, pendingShelterList, pendingMarkersList, shelterIdPending, mMap);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        adapter.setMap(mMap);
        // prompt the user for permission
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            try {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }catch (NullPointerException e){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            }
                        } else {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }



    private void getAllShelters(){
        String url = getString(R.string.server_ip)+"/management/shelters"; // get shelters URL
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("result");
                    for (int i=0; i< jsonArray.length(); i++) {
                        JSONObject she = jsonArray.getJSONObject(i);
                        SafePoint shelter = new SafePoint(she.getInt("id"),she.getString("user_email"), she.getDouble("latitude") , she.getDouble("longitude"), she.getInt("approved"), she.getString("address"));
                        MarkerOptions marker = new MarkerOptions()
                                .position(new LatLng(shelter.getLatitude(), shelter.getLongitude()))
                                .title(shelter.getAddress());
                        if(shelter.getApproved() == 1)
                        {
                            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_shelter));
                            mMap.addMarker(marker);
                        }else{
                            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_shelter));
                            pendingMarkersList.add(mMap.addMarker(marker));
                            pendingShelterList.add(shelter);
                            shelterIdPending.add(shelter.getId());
                            moveCamera(shelter.getLatitude(),shelter.getLongitude());
                        }
                        adapter.notifyDataSetChanged();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mQueue.add(request);
    }

    public void moveCamera(double latitude, double longitude)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), DEFAULT_ZOOM));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(AdminActions.this, "You Clicked at "+position, Toast.LENGTH_SHORT).show();
    }
}
