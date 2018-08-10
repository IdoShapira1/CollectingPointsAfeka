package com.final_project_afeka.collectingpointsfinal;

import android.content.Intent;
import android.content.res.Resources;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        mAuth = FirebaseAuth.getInstance();
        nameET = (EditText) findViewById(R.id.nameMainActivity);
        collectEt = (EditText) findViewById(R.id.collectedMainActivity);
        approveEt = (EditText) findViewById(R.id.approvedMainActivity);
        declineEt = (EditText) findViewById(R.id.declinedPointsActivity);
        floatMenu = (FloatingActionButton)findViewById(R.id.floatingPopupMenu);
        pullUserData();
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
                                if (finishReadFromDB)
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

    void pullUserData(){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
                finishReadFromDB = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showData(DataSnapshot dataSnapshot) {
        DataSnapshot data_user;

        String uID = mAuth.getCurrentUser().getUid();
        data_user=dataSnapshot.child("users").child(uID);
        Log.d(TAG, "showData: data_user : "+data_user);
        user = new User();
        if(data_user.getValue() == null){ // create user if doesn't exist yet
            mDatabase.child("users").child(uID).child("email").setValue(mAuth.getCurrentUser().getEmail());
            mDatabase.child("users").child(uID).child("isAdmin").setValue(0);
            mDatabase.child("users").child(uID).child("pointsApproved").setValue(0);
            mDatabase.child("users").child(uID).child("pointsCollected").setValue(0);
            mDatabase.child("users").child(uID).child("pointsDeclined").setValue(0);
            user.setEmail(mAuth.getCurrentUser().getEmail());
            user.setIsAdmin(0);
            user.setPointsApproved(0);
            user.setPointsCollected(0);
            user.setPointsDeclined(0);

        }else { // fetch user
            user.setEmail(data_user.getValue(User.class).getEmail());
            user.setIsAdmin(data_user.getValue(User.class).getIsAdmin());
            user.setPointsApproved(data_user.getValue(User.class).getPointsApproved());
            user.setPointsCollected(data_user.getValue(User.class).getPointsCollected());
            user.setPointsDeclined(data_user.getValue(User.class).getPointsDeclined());
        }
        nameET.setText(user.getEmail());
        approveEt.setText((getResources().getString(R.string.points_approved)+" "+user.getPointsApproved()));
        declineEt.setText(getResources().getString(R.string.points_declined)+" "+user.getPointsDeclined());
        collectEt.setText(getResources().getString(R.string.points_collected)+" "+user.getPointsCollected());
        nameET.setKeyListener(null);
        approveEt.setKeyListener(null);
        declineEt.setKeyListener(null);
        collectEt.setKeyListener(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                showData(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
