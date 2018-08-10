package com.final_project_afeka.collectingpointsfinal;

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
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback{


    GoogleMap mMap;
    Marker myMarker = null;
    private FirebaseAuth mAuth;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int DEFAULT_ZOOM = 16;
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient  mPlaceDetectionClient;
    private LatLng mDefaultLocation = new LatLng(32.113819, 34.817794);
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private boolean mLocationPermissionGranted;
    private ImageButton uploadBtn;
    private Spinner spinner;
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private List<Marker> pendingMarkersList = new ArrayList<>();
    private List<Marker> approvedMarkersList = new ArrayList<>();
    private CheckBox pendingCheckBox,approvedCheckBox;
    private Polyline polyline;
    private int spinnerCheck = 0;
    private TextView currentPoint;
    private ImageView streetView;


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
        uploadBtn = (ImageButton)findViewById(R.id.upload__btn_img);
        spinner = (Spinner) findViewById(R.id.listOfShelters);
        currentPoint = (TextView) findViewById(R.id.currentPointText);
        streetView = (ImageView) findViewById(R.id.streetView);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        addSheltersMarkers();
        addFilters();
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadPoint();
            }
        });


    }


    private void addSheltersNavigation(){
        List<Marker> allLists = new ArrayList(pendingMarkersList);
        allLists.addAll(approvedMarkersList);
        MarkersAdapter customAdapter = new MarkersAdapter(getApplicationContext(),android.R.layout.simple_spinner_item,allLists);
        spinner.setAdapter(customAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                // Here you get the current item (a User object) that is selected by its position

                if(++spinnerCheck > 1) {
                    try {
                        Marker mark = (Marker) spinner.getSelectedItem();
                        LatLng origin = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                        LatLng dest = new LatLng(mark.getPosition().latitude, mark.getPosition().longitude);
                        //getting URL to the Google direction API
                        String url = getDirectionsUrl(origin, dest);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        downloadTask.execute(url);
                    } catch(Exception e){
                        Toast.makeText(getApplicationContext(), "אנא הדלק רכיב GPS לפני בדיקת ניווט", Toast.LENGTH_LONG).show();
                    }
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {  }
        });


    }
    private void addFilters(){
        pendingCheckBox = (CheckBox) findViewById(R.id.pendingPointsCheck);
        approvedCheckBox = (CheckBox) findViewById(R.id.approvedPointsCheck);
        pendingCheckBox.setChecked(true);
        approvedCheckBox.setChecked(true);
        pendingCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(Marker pendingMarker : pendingMarkersList){
                    if(pendingCheckBox.isChecked())
                        pendingMarker.setVisible(true);
                    else
                        pendingMarker.setVisible(false);
                }
            }
        });
        approvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for(Marker approvedMarker : approvedMarkersList){
                    if(approvedCheckBox.isChecked())
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

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // First check if myMarker is null
                if (myMarker == null) {
                    // Marker was not set yet. Add marker:
                    myMarker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title("הכנס מקום בטוח")
                            .draggable(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_shelter2))
                            .snippet(""));
                } else {
                    // Marker already exists, just update it's position
                    myMarker.setPosition(latLng);

                }
                setStreetViewImage();
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
                setStreetViewImage();
            }
            @Override
            public void onMarkerDrag(Marker arg0) {
                 //TODO Auto-generated method stub
            }
        });




    }

    private void setStreetViewImage(){
        String imageURL;
        imageURL = "https://maps.googleapis.com/maps/api/streetview?size=400x400&location="+myMarker.getPosition().latitude+","+myMarker.getPosition().longitude+"&fov=90&heading=235&pitch=10";
        Picasso.get().load(imageURL).into(streetView);
        currentPoint.setText(getCompleteAddressString(myMarker.getPosition().latitude,myMarker.getPosition().longitude));
    }

    private void addSheltersMarkers(){
        // type = 0 pending shelter , type = 1 approved point
        database.child("shelters").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Shelter shelter = snapshot.getValue(Shelter.class);
                    MarkerOptions marker = new MarkerOptions()
                            .position(new LatLng(shelter.getLalatitudet(), shelter.getLongitude()))
                            .title(shelter.getAddress());
                    if(shelter.isApproved() == 1)
                    {
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.green_shelter2));
                        approvedMarkersList.add(mMap.addMarker(marker));
                    }else{
                        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.orange_shelter2));
                        pendingMarkersList.add(mMap.addMarker(marker));
                    }
                }
                addSheltersNavigation();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

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
                                //lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
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
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
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

    public void UploadPoint()
    {
        if (myMarker != null)
        {
            double lng = myMarker.getPosition().longitude;
            double lat = myMarker.getPosition().latitude;
            final String uID = mAuth.getCurrentUser().getUid();
            Shelter shelter = new Shelter(uID,lng,lat,getCompleteAddressString(lat,lng),0,mAuth.getCurrentUser().getEmail());

            database.child("shelters").push().setValue(shelter);
            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    updateUserInformation(dataSnapshot,uID);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Toast.makeText(getApplicationContext(), "מחסה הועלה למאגר הנתונים", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "אנא בחר נקודה!", Toast.LENGTH_LONG).show();
        }
    }

    private void updateUserInformation(DataSnapshot dataSnapshot,String uID) {
        int pointsCollected = dataSnapshot.child("users/"+uID).getValue(User.class).getPointsCollected();
        //   int pointsDeclined = dataSnapshot.child("users/"+uID).getValue(User.class).getPointsDeclined();
        database.child("users/"+uID).child("pointsCollected").setValue(pointsCollected+1);
        // database.child("users/"+uID).child("pointsDeclined").setValue(pointsDeclined+1);
    }





    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            try{
                if (polyline != null)
                        polyline.remove();
                polyline = mMap.addPolyline(lineOptions);
            } catch(Exception e){
                Toast.makeText(getApplicationContext(), "לא ניתן לייצר מסלול לנקודה זו", Toast.LENGTH_LONG).show();
            }

        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=true";
        String mode = "mode=walking";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}

















