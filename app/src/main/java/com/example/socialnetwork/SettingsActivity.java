package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar toolbarSettings;

    private EditText editTextStatus, editTextUsername, editTextFullName, editTextCountry, editTextDOB, editTextGender, editTextRelationshipStatus;
    private Button buttonUpdateAccount;
    private CircleImageView circleImageViewProfileImage;

    private DatabaseReference databaseReferenceSettings;
    private StorageReference storageReferenceUserProfileImage;
    private FirebaseAuth mAuth;
    private String currentUserId;

    final static int galleryPic = 1;

    private ProgressDialog progressDialogLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        progressDialogLoadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        databaseReferenceSettings = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        storageReferenceUserProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Images");

        //toolbar with back arrow to go back to MainActivity (Manifest -> MainActivity: parent activity)
        toolbarSettings = (Toolbar) findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbarSettings);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTextStatus = (EditText) findViewById(R.id.editTextSettingsStatus);
        editTextUsername = (EditText) findViewById(R.id.editTextSettingsUsername);
        editTextFullName = (EditText) findViewById(R.id.editTextSettingsFullName);
        editTextCountry = (EditText) findViewById(R.id.editTextSettingsCountry);
        editTextDOB = (EditText) findViewById(R.id.editTextSettingsDOB);
        editTextGender = (EditText) findViewById(R.id.editTextSettingsGender);
        editTextRelationshipStatus = (EditText) findViewById(R.id.editTextSettingsRelationshipStatus);
        buttonUpdateAccount = (Button) findViewById(R.id.buttonSettingsUpdateAccount);
        circleImageViewProfileImage = (CircleImageView) findViewById(R.id.circleImageViewSettingsProfileImage);

        databaseReferenceSettings.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String settingsProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String settingsUsername = dataSnapshot.child("username").getValue().toString();
                    String settingsFullName = dataSnapshot.child("fullname").getValue().toString();
                    String settingsCountry = dataSnapshot.child("country").getValue().toString();
                    String settingsDOB = dataSnapshot.child("dob").getValue().toString();
                    String settingsGender = dataSnapshot.child("gender").getValue().toString();
                    String settingsRelationshipStatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                    String settingsProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.get().load(settingsProfileImage).placeholder(R.drawable.profile).into(circleImageViewProfileImage);
                    editTextStatus.setText(settingsProfileStatus);
                    editTextUsername.setText(settingsUsername);
                    editTextFullName.setText(settingsFullName);
                    editTextCountry.setText(settingsCountry);
                    editTextDOB.setText(settingsDOB);
                    editTextGender.setText(settingsGender);
                    editTextRelationshipStatus.setText(settingsRelationshipStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonUpdateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAccountInfo();
            }
        });

        circleImageViewProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == galleryPic && resultCode==RESULT_OK && data != null){
            Uri imageUri = data.getData();

            //displaying the cropping activity in which the user is going to crop his profile pic

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult activityResult = CropImage.getActivityResult(data);

            //save cropped image to the Firebase Storage
            if(resultCode == RESULT_OK){

                progressDialogLoadingBar.setTitle("Profile image");
                progressDialogLoadingBar.setMessage("Please wait...");
                progressDialogLoadingBar.show();
                progressDialogLoadingBar.setCanceledOnTouchOutside(true);   //does't disappear when user touches background


                Uri resultUri = activityResult.getUri();
                final StorageReference storageReferenceFilePath = storageReferenceUserProfileImage.child(currentUserId + ".jpg");

                storageReferenceFilePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReferenceFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Toast.makeText(SettingsActivity.this, "Profile image successfully stored to Firebase Storage", Toast.LENGTH_SHORT).show();

                                //saving the link of the image from the Firebase Storage to the Firebase Realtime Database
                                //to the user profile information

                                final String downloadUrl = uri.toString();
                                databaseReferenceSettings.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Intent selfSetupIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                            startActivity(selfSetupIntent);

                                            Toast.makeText(SettingsActivity.this, "Profile image successfully stored to Firebase Database!", Toast.LENGTH_SHORT).show();
                                            progressDialogLoadingBar.dismiss();
                                        } else{
                                            String errorMessage = task.getException().getMessage();
                                            Toast.makeText(SettingsActivity.this, "Error occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
                                            progressDialogLoadingBar.dismiss();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            } else{
                Toast.makeText(this, "Error occurred: Image can't be cropped. Try again!", Toast.LENGTH_SHORT).show();
                progressDialogLoadingBar.dismiss();
            }
        }
    }

    private void validateAccountInfo() {
        String status = editTextStatus.getText().toString();
        String username = editTextUsername.getText().toString();
        String fullname = editTextFullName.getText().toString();
        String country = editTextCountry.getText().toString();
        String dob = editTextDOB.getText().toString();
        String gender = editTextGender.getText().toString();
        String relationshipStatus = editTextRelationshipStatus.getText().toString();

        if(TextUtils.isEmpty(status)){
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(fullname)){
            Toast.makeText(this, "Please write your fullname", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(country)){
            Toast.makeText(this, "Please write your country", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(dob)){
            Toast.makeText(this, "Please write your dob", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Please write your gender", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(relationshipStatus)){
            Toast.makeText(this, "Please write your relationshipStatus", Toast.LENGTH_SHORT).show();
        } else {

            progressDialogLoadingBar.setTitle("Update Account");
            progressDialogLoadingBar.setMessage("Please wait...");
            progressDialogLoadingBar.show();
            progressDialogLoadingBar.setCanceledOnTouchOutside(true);   //does't disappear when user touches background

            updateAccountInfo(status, username, fullname, country, dob, gender, relationshipStatus);
        }
    }

    private void updateAccountInfo(String status, String username, String fullname, String country, String dob, String gender, String relationshipStatus) {
        HashMap hashMapUser = new HashMap();
            hashMapUser.put("status", status);
            hashMapUser.put("username", username);
            hashMapUser.put("fullname", fullname);
            hashMapUser.put("country", country);
            hashMapUser.put("dob", dob);
            hashMapUser.put("gender", gender);
            hashMapUser.put("relationshipstatus", relationshipStatus);
        databaseReferenceSettings.updateChildren(hashMapUser).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account settings updated successfully", Toast.LENGTH_SHORT).show();
                    progressDialogLoadingBar.dismiss();
                } else{
                    Toast.makeText(SettingsActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                    progressDialogLoadingBar.dismiss();
                }
            }
        });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
