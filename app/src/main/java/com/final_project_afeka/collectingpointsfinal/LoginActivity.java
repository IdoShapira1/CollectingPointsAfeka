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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private FirebaseAuth mAuth;
    private static final String TAG = "Login";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button confirm;
        TextView forgotPassword;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        email = (EditText) findViewById(R.id.emailLoginActivity);
        password = (EditText) findViewById(R.id.passwordLoginActivity);
        confirm = (Button) findViewById(R.id.confirmLoginActivity);
        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            Intent intent = new Intent(LoginActivity.this,MainMenuActivity.class);
            startActivity(intent);
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authenticateLogin();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String useremail = email.getText().toString().trim();

                if (useremail.equals(""))
                    Toast.makeText(LoginActivity.this, R.string.reset_pass_no_mail, Toast.LENGTH_SHORT).show();
                else if (!isEmailValid(useremail))
                    Toast.makeText(getApplicationContext(),R.string.invalid_mail,Toast.LENGTH_LONG).show();
                else {
                    mAuth.sendPasswordResetEmail(useremail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, R.string.reset_pass_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.reset_pass_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void authenticateLogin(){
        String emailInserted = email.getText().toString();
        String passwordInserted = password.getText().toString();
        if(emailInserted.isEmpty() || passwordInserted.isEmpty())
            Toast.makeText(getApplicationContext(),R.string.must_enter_user_and_password,Toast.LENGTH_LONG).show();
        else if (!isEmailValid(emailInserted))
            Toast.makeText(getApplicationContext(),R.string.invalid_mail,Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(),R.string.login_failed,Toast.LENGTH_LONG).show();
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
    }
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
