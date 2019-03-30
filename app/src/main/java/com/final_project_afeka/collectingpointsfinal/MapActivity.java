package com.final_project_afeka.collectingpointsfinal;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.graphics.Color;
import android.os.AsyncTask;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {


    GoogleMap mMap;
    Marker myMarker = null;
    private FirebaseAuth mAuth;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 16;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private LatLng mDefaultLocation = new LatLng(32.113819, 34.817794);
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private ImageButton uploadBtn;
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private HashMap<Integer, Marker> pendingMarkersList = new HashMap<>();
    private HashMap<Integer,Marker> approvedMarkersList = new HashMap<>();
    private CheckBox pendingCheckBox, approvedCheckBox;
    private TextView currentPoint;
    private static final String TAG = "Activity";
    private ConnectionServer connectionServer;
    private ArrayList<SafePoint> shelters;
    private HashMap<LatLng, Integer> pendingMarkersIds = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points_insert);
        mAuth = FirebaseAuth.getInstance();
        mGeoDataClient = Places.getGeoDataClient(this, null);
        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        uploadBtn = (ImageButton) findViewById(R.id.upload__btn_img);
        currentPoint = (TextView) findViewById(R.id.currentPointText);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        this.connectionServer = new ConnectionServer(this);
        connectionServer.getAllShelters();
        mapFragment.getMapAsync(this);
        addFilters();
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPoint();
            }
        });

    }

    private void addFilters() {
        pendingCheckBox = (CheckBox) findViewById(R.id.pendingPointsCheck);
        approvedCheckBox = (CheckBox) findViewById(R.id.approvedPointsCheck);
        pendingCheckBox.setChecked(true);
        approvedCheckBox.setChecked(true);
        pendingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (Marker pendingMarker : pendingMarkersList.values()) {
                    if (pendingCheckBox.isChecked())
                        pendingMarker.setVisible(true);
                    else
                        pendingMarker.setVisible(false);
                }
            }
        });
        approvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (Marker approvedMarker : approvedMarkersList.values()) {
                    if (approvedCheckBox.isChecked())
                        approvedMarker.setVisible(true);
                    else
                        approvedMarker.setVisible(false);
                }
            }
        });

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        // prompt the user for permission
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // First check if myMarker is null
                if (myMarker == null) {
                    // Marker was not set yet. Add marker:
                    myMarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("הכנס נקודה חדשה")
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_shelter2))
                            .snippet(getCompleteAddressString(latLng.latitude, latLng.longitude)));
                    myMarker.showInfoWindow();

                } else {
                    // Marker already exists, just update it's position
                    myMarker.setPosition(latLng);
                    myMarker.setSnippet(getCompleteAddressString(latLng.latitude, latLng.longitude));
                    myMarker.showInfoWindow();
                    myMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.red_shelter2));

                }
                currentPoint.setText(getCompleteAddressString(myMarker.getPosition().latitude, myMarker.getPosition().longitude));
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(myMarker != null) {
                    //Red Marker option
                    if (myMarker.getId().equals(marker.getId())) {
                        uploadPoint();
                        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.orange_shelter2));
                        connectionServer.getAllShelters();
                    }
                }
                    //Orange Marker options
                if(pendingMarkersList.values().contains(marker)){
                    connectionServer.deleteSafePoint(pendingMarkersIds.get(marker.getPosition())); // send delete request
                    int pointId = pendingMarkersIds.get(marker.getPosition());
                    pendingMarkersList.get(pointId).remove();
                }


            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currentPoint.setText(getCompleteAddressString(marker.getPosition().latitude, marker.getPosition().longitude));
                if(pendingMarkersList.values().contains(marker)){
                    marker.setSnippet("לחץ על החלון למחיקת הנקודה ");

                }
                return false;
            }
        });




        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker arg0) {
                // TODO Auto-generated method stub
            }

            @SuppressWarnings("unchecked")
            @Override
            public void onMarkerDragEnd(Marker arg0) {
                //setStreetViewImage();
                currentPoint.setText(getCompleteAddressString(myMarker.getPosition().latitude, myMarker.getPosition().longitude));
            }

            @Override
            public void onMarkerDrag(Marker arg0) {
                //TODO Auto-generated method stub
            }
        });
    }





    public void addSheltersMarkers(ArrayList<SafePoint> shelters) {
        for (int i = 0; i < shelters.size(); i++) {
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(shelters.get(i).getLatitude(), shelters.get(i).getLongitude()))
                    .title(shelters.get(i).getAddress());
            if (shelters.get(i).getApproved() == 1) {
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_shelter2));
                approvedMarkersList.put(shelters.get(i).getId(), mMap.addMarker(marker));
            } else {
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_shelter2));
                pendingMarkersIds.put(marker.getPosition(),shelters.get(i).getId());
                pendingMarkersList.put(shelters.get(i).getId(), mMap.addMarker(marker));
            }
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
        } catch (SecurityException e) {
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
                            } catch (NullPointerException e) {
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
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    public void uploadPoint() {
        if (myMarker != null) {
            double lng = myMarker.getPosition().longitude;
            double lat = myMarker.getPosition().latitude;
            final String email = mAuth.getCurrentUser().getEmail();
            SafePoint point = new SafePoint(0, email, lat, lng, 0, getCompleteAddressString(lat, lng));
            connectionServer.uploadSafePoint(point);
            connectionServer.updatePointsCollected(email);
            Toast.makeText(getApplicationContext(), "מחסה הועלה למאגר הנתונים", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "אנא בחר נקודה!", Toast.LENGTH_LONG).show();
        }
    }


}