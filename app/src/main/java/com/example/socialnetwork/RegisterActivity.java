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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonCreateAccount;
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialogLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = (EditText) findViewById(R.id.editTextRegisterEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextRegisterPass);
        editTextConfirmPassword = (EditText) findViewById(R.id.editTextRegisterConfirmPass);
        buttonCreateAccount = (Button) findViewById(R.id.buttonRegisterCreateAccount);
        progressDialogLoadingBar = new ProgressDialog(this);

        buttonCreateAccount.setOnClickListener(new View.OnClickListener() { //clicking the button
            @Override                                                       //redirects to the method
            public void onClick(View v) {
                createNewAccount();
            }


        });
    }

    private void createNewAccount() {
        String getEmail = editTextEmail.getText().toString();
        String getPassword = editTextPassword.getText().toString();
        String getConfirmedPassword = editTextConfirmPassword.getText().toString();

        //verifying if the email and passwords are completed (correctly)

        if(TextUtils.isEmpty(getEmail)){
            Toast.makeText(this, "Please write your email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(getPassword)){
            Toast.makeText(this, "Please write your password", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(getConfirmedPassword)){
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_SHORT).show();
        }
        else if(!getPassword.equals(getConfirmedPassword)){
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
        }

        //creating account using email and password provided

        else{

            //progress dialog to tell user account is creating...

            progressDialogLoadingBar.setTitle("Creating new account...");
            progressDialogLoadingBar.setMessage("Please wait while we are creating your new account...");
            progressDialogLoadingBar.show();
            progressDialogLoadingBar.setCanceledOnTouchOutside(true);   //does't disappear when user touches background

            mAuth.createUserWithEmailAndPassword(getEmail, getPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        sendUserToSetupActivity();

                        Toast.makeText(RegisterActivity.this, "Your are authenticated successfully!", Toast.LENGTH_SHORT).show();
                        progressDialogLoadingBar.dismiss();
                    } else{
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
                        progressDialogLoadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
