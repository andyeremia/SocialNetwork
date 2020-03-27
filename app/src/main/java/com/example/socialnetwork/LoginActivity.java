package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

    private Button buttonLogin;
    private EditText editTextLoginEmail, editTextLoginPassword;
    private TextView textViewLoginCreateAccountLink;

    private FirebaseAuth mAuth;

    private ProgressDialog progressDialogLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        textViewLoginCreateAccountLink = (TextView) findViewById(R.id.textViewLoginCreateAccount);
        editTextLoginEmail = (EditText) findViewById(R.id.editTextLoginEmail);
        editTextLoginPassword = (EditText) findViewById(R.id.editTextLoginPass);
        buttonLogin = (Button) findViewById(R.id.buttonLoginAccount);
        progressDialogLoadingBar = new ProgressDialog(this);

        textViewLoginCreateAccountLink.setOnClickListener(new View.OnClickListener() {  //when clicking on the "Don't have an account?..."
            @Override
            public void onClick(View v) {   //send user to create new account
                sendUserToRegisterActivity();                       //when clicking on the "Don't have an account?..."
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() { //when clicking on the Login button
            @Override
            public void onClick(View v) {
                allowUserToLogin();
            }
        });

    }

    //if the user is already logged in, redirect the user to the MainActivity w/o
    //authenticating again (with email & password)
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendUserToMainActivity();
        }
    }

    //method for logging in the user, taking the email and password from the Login page
    //and verifying if they exist in the Firebase Authentication corresponding to an existing user (account)
    //if the user exists, the user is logged in the app

    private void allowUserToLogin() {
        String getEmail = editTextLoginEmail.getText().toString();
        String getPassword = editTextLoginPassword.getText().toString();
        if(TextUtils.isEmpty(getEmail)){
            Toast.makeText(this, "Please write your email!", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(getPassword)){
            Toast.makeText(this, "Please write your password!", Toast.LENGTH_SHORT).show();
        } else {

            //progress dialog to tell user is logging in...
            progressDialogLoadingBar.setTitle("Logging in...");
            progressDialogLoadingBar.setMessage("Please wait...");
            progressDialogLoadingBar.show();
            progressDialogLoadingBar.setCanceledOnTouchOutside(true);   //does't disappear when user touches background

            mAuth.signInWithEmailAndPassword(getEmail, getPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(LoginActivity.this, "You are logged in!", Toast.LENGTH_SHORT).show();
                        progressDialogLoadingBar.dismiss();
                    } else{
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(LoginActivity.this, "Error occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
                        progressDialogLoadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() { //method to redirect the user to the RegisterActivity
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
