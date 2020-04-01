package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button buttonLogin;
    private EditText editTextLoginEmail, editTextLoginPassword;
    private TextView textViewLoginCreateAccountLink;
    private SignInButton signInButtonGoogle;

    private FirebaseAuth mAuth;

    private static final int RC_SIGN_IN = 1001;
    GoogleSignInClient googleSignInClient;
    private static final String TAG = "LoginActivity";

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
        signInButtonGoogle = (SignInButton) findViewById(R.id.signInButtonLoginGoogle);
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

        signInButtonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch SignIn
                signInToGoogle();
            }
        });

        //configure Google client
        GoogleSignInOptions gso = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //building a GoogleSignInClient with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    private void signInToGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            progressDialogLoadingBar.setTitle("Google Sign In...");
            progressDialogLoadingBar.setMessage("Please wait...");
            progressDialogLoadingBar.setCanceledOnTouchOutside(true);   //does't disappear when user touches background
            progressDialogLoadingBar.show();

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(this, "Google Sign in Succeeded", Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google Sign in Failed" + e, Toast.LENGTH_SHORT).show();
                progressDialogLoadingBar.dismiss();
            }
        }

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d(TAG, "signInWithCredential:success: currentUser: " + user.getEmail());
                            Toast.makeText(LoginActivity.this, "Firebase Authentication Succeeded", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                            progressDialogLoadingBar.dismiss();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Firebase Authentication failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                            sendUserToLoginActivity();
                            progressDialogLoadingBar.dismiss();
                        }
                    }
                });

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
            progressDialogLoadingBar.setCanceledOnTouchOutside(true);   //does't disappear when user touches background
            progressDialogLoadingBar.show();


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

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(LoginActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
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
