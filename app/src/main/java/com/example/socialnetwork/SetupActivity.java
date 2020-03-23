package com.example.socialnetwork;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextFullName, editTextCountry;
    private Button buttonSaveInfo;
    private CircleImageView circleImageViewProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        editTextUsername = (EditText) findViewById(R.id.editTextSetupUsername);
        editTextFullName = (EditText) findViewById(R.id.editTextSetupFullName);
        editTextCountry = (EditText) findViewById(R.id.editTextSetupCountry);
        buttonSaveInfo = (Button) findViewById(R.id.buttonSetupSaveInfo);
        circleImageViewProfileImage = (CircleImageView) findViewById(R.id.circleImageViewSetupProfileImage);
    }
}
