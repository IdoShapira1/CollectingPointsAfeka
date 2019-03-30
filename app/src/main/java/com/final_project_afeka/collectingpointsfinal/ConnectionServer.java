package com.final_project_afeka.collectingpointsfinal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.NetworkResponse;
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
    private Context mContext;
    private ArrayList<SafePoint> shelters = new ArrayList<SafePoint>();


    public ConnectionServer(Context context){
        mQueue = Volley.newRequestQueue(context);
        mContext = context;
        this.mapActivity = (MapActivity) context;
    }

    public ArrayList<SafePoint> getAllShelters(){
        String url = mContext.getString(R.string.server_ip)+"/management/shelters"; // get shelters URL
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("result");
                    for (int i=0; i< jsonArray.length(); i++) {
                        JSONObject she = jsonArray.getJSONObject(i);
                        SafePoint point = new SafePoint(she.getInt("id"),she.getString("user_email"), she.getDouble("latitude") , she.getDouble("longitude"), she.getInt("approved"), she.getString("address"));
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
        String url = mContext.getString(R.string.server_ip)+"/management/shelters"; // post new shelter
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_email",point.getEmail());
        params.put("latitude",point.getLatitude()+"");
        params.put("longitude",point.getLongitude()+"");
        params.put("address",point.getAddress());
        params.put("city",point.getAddress().split(",")[1].replaceAll("\\s+","")); // remove spaces

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

    public void deleteSafePoint(int pointId){
        String url = mContext.getString(R.string.server_ip)+"/management/shelters?id="+pointId; // delete shelter
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url,null, new com.android.volley.Response.Listener<JSONObject>() {
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

    public void updatePointsCollected(String email){
        String url = mContext.getString(R.string.server_ip)+"/management/users/points_collected?email="+email; // post new shelter

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url,null, new com.android.volley.Response.Listener<JSONObject>() {
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



}
