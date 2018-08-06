package com.final_project_afeka.collectingpointsfinal;

import android.support.annotation.NonNull;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private Button confirm;
    private FirebaseAuth mAuth;
    private TextView forgotPassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.emailLoginActivity);
        password = (EditText) findViewById(R.id.passwordLoginActivity);
        confirm = (Button) findViewById(R.id.confirmLoginActivity);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        mAuth = FirebaseAuth.getInstance();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateLogin();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, PasswordActivity.class));
            }
        });



    }

    public void authenticateLogin(){
        String emailInserted = email.getText().toString();
        String passwordInserted = password.getText().toString();
        if(emailInserted.isEmpty() || passwordInserted.isEmpty())
            Toast.makeText(getApplicationContext(),"YOU BAD!",Toast.LENGTH_LONG).show();
        else{
            mAuth.signInWithEmailAndPassword(emailInserted,passwordInserted).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(LoginActivity.this,MainMenuActivity.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        //     Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(),"Wrong password inserted",Toast.LENGTH_LONG).show();
                    }

                }
            });
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }
}
