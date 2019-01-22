package com.final_project_afeka.collectingpointsfinal;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainMenuActivity extends AppCompatActivity {
    private static final String TAG = MainMenuActivity.class.getSimpleName();
    private User user;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText nameET;
    private EditText collectEt;
    private EditText approveEt;
    private EditText declineEt;
    private FloatingActionButton floatMenu;
    boolean finishReadFromDB = false;
    private static RequestQueue mQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mAuth = FirebaseAuth.getInstance();
        mQueue = Volley.newRequestQueue(this);
        nameET = (EditText) findViewById(R.id.nameMainActivity);
        collectEt = (EditText) findViewById(R.id.collectedMainActivity);
        approveEt = (EditText) findViewById(R.id.approvedMainActivity);
        declineEt = (EditText) findViewById(R.id.declinedPointsActivity);
        floatMenu = (FloatingActionButton)findViewById(R.id.floatingPopupMenu);
        pullUserData(mAuth.getCurrentUser().getEmail());
        floatMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(MainMenuActivity.this,floatMenu);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getOrder()){
                            case 0:
                                startActivity(new Intent(MainMenuActivity.this,MapActivity.class));
                                break;
                            case 1:
                                if (user.getIsAdmin() == 0 )
                                    Toast.makeText(getApplicationContext(),R.string.admin_failed,Toast.LENGTH_LONG).show();
                                else
                                    startActivity(new Intent(MainMenuActivity.this,AdminActions.class));
                                break;
                        }
                        return true;
                    }
                });

                popupMenu.show();

            }
        });
    }

    void pullUserData(String email) {
        String url = "http://3.121.116.91:3000/users/?email="+email; // GET users

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e(TAG, "onResponse: respone" + response);
                try{
                    user = new User();
                    user.setEmail(response.getString("email"));
                    user.setIsAdmin(response.getInt("admin"));
                    user.setPointsApproved(response.getInt("points_approved"));
                    user.setPointsCollected(response.getInt("points_collected"));
                    user.setPointsDeclined(response.getInt("points_declined"));
                    nameET.setText(user.getEmail());
                    approveEt.setText((getResources().getString(R.string.points_approved)+" "+user.getPointsApproved()));
                    declineEt.setText(getResources().getString(R.string.points_declined)+" "+user.getPointsDeclined());
                    collectEt.setText(getResources().getString(R.string.points_collected)+" "+user.getPointsCollected());
                    nameET.setKeyListener(null);
                    approveEt.setKeyListener(null);
                    declineEt.setKeyListener(null);
                    collectEt.setKeyListener(null);
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
    @Override
    protected void onResume() {
        super.onResume();
    }
}
