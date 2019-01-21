package com.final_project_afeka.collectingpointsfinal;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ConnectionServer {

    private static final String TAG = "Connection Server";
    private static RequestQueue mQueue;
    private MapActivity mapActivity;


    public ConnectionServer(Context context){
        mQueue = Volley.newRequestQueue(context);
     //   this.mapActivity = (MapActivity) context;
    }

    public void getAllShelters(){
        String url = "https://api.myjson.com/bins/aylr0"; // get shelters URL
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("locations");
                   // ArrayList<LatLng> rv;
                    for (int i=0; i< jsonArray.length(); i++) {
                        JSONObject shelter = jsonArray.getJSONObject(i);
                        Log.e(TAG, "shelter area_code:" +shelter.getInt("area_code")+" address: "+shelter.getString("address")+" approved:" +shelter.getInt("approved"));
                        Log.e(TAG, "lat is: "+shelter.getDouble("latitude") + " lan is: "+shelter.getDouble("longitude"));

                        //    mDatabaseHelper.addData(latlan.getDouble("lat"),latlan.getDouble("lan")); // adding points to local db
                    }
                 //   rv = mDatabaseHelper.getPointsNear(originPosition); // get points from db
                 //   mainActivity.addSafeMarkerOnMap(rv);
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
        mQueue.add(request);    }

}
