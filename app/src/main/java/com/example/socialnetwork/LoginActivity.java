package com.example.socialnetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText userLoginEmail, userLoginPassword;
    private TextView userLoginCreateAccountLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userLoginCreateAccountLink = (TextView) findViewById(R.id.textViewLoginCreateAccount);
        userLoginEmail = (EditText) findViewById(R.id.editTextLoginEmail);
        userLoginPassword = (EditText) findViewById(R.id.editTextLoginPass);
        loginButton = (Button) findViewById(R.id.buttonLoginAccount);

        userLoginCreateAccountLink.setOnClickListener(new View.OnClickListener() {  //when clicking on the "Don't have an account?..."
            @Override
            public void onClick(View v) {
                sendUserToRegisterActivity();
            }
        });
    }

    private void sendUserToRegisterActivity() { //method to redirect the user to the RegisterActivity
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}
