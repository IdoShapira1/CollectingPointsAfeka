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
import java.util.HashMap;
import java.util.Map;

public class ConnectionServer {

    private static final String TAG = "Connection Server";
    private static RequestQueue mQueue;
    private MapActivity mapActivity;
    private ArrayList<SafePoint> shelters = new ArrayList<SafePoint>();


    public ConnectionServer(Context context){
        mQueue = Volley.newRequestQueue(context);
        this.mapActivity = (MapActivity) context;
    }

    public ArrayList<SafePoint> getAllShelters(){
        String url = "https://api.myjson.com/bins/zm5ws"; // get shelters URL
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("locations");
                    for (int i=0; i< jsonArray.length(); i++) {
                        JSONObject she = jsonArray.getJSONObject(i);
                        SafePoint point = new SafePoint(she.getString("user_email"), she.getDouble("latitude") , she.getDouble("longitude"), she.getInt("approved"), she.getString("address"));
                        Log.e(TAG, "shelter long:" +point.getLongitude()+" lati: "+point.getLatitude());
                        shelters.add(point);
                    }
                    mapActivity.addSheltersMarkers(shelters);

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
        return  shelters;
    }

    public void uploadSafePoint(SafePoint point){
        String url = "https://webhook.site/b358b9ce-9950-4409-93ed-74e6618637ac"; // post new shelter
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("email",point.getEmail());
        params.put("latitude",point.getLatitude());
        params.put("longitude",point.getLongitude());
        params.put("address",point.getAddress());
        params.put("city",point.getAddress().split(",")[1]);

        JSONObject jsonObj = new JSONObject(params);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonObj, new com.android.volley.Response.Listener<JSONObject>() {
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
        mQueue.add(request);
    }




    public void updateUserData(String uId){

    }

}
