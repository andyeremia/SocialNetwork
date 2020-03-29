package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextFullName, editTextCountry;
    private Button buttonSaveInfo;
    private CircleImageView circleImageViewProfileImage;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReferenceUser;
    private StorageReference storageReferenceUserProfileImage;

    private ProgressDialog progressDialogLoadingBar;

    String currentUserId;

    final static int galleryPic = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        storageReferenceUserProfileImage = FirebaseStorage.getInstance().getReference().child("Profile Images");

        editTextUsername = (EditText) findViewById(R.id.editTextSetupUsername);
        editTextFullName = (EditText) findViewById(R.id.editTextSetupFullName);
        editTextCountry = (EditText) findViewById(R.id.editTextSetupCountry);
        buttonSaveInfo = (Button) findViewById(R.id.buttonSetupSaveInfo);
        circleImageViewProfileImage = (CircleImageView) findViewById(R.id.circleImageViewSetupProfileImage);

        progressDialogLoadingBar = new ProgressDialog(this);



        buttonSaveInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        circleImageViewProfileImage.setOnClickListener(new View.OnClickListener() {     //when clicking on the image icon
            @Override                                                                   ///redirects the user to the phone gallery
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPic);
            }
        });

        //loading the cropped profile image in the CircleImageView from the SetupActivity
        databaseReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("profileimage")){
                        String imageLink = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(imageLink).placeholder(R.drawable.profile).into(circleImageViewProfileImage);
                    } else{
                        Toast.makeText(SetupActivity.this, "Please select profile image first", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                                Toast.makeText(SetupActivity.this, "Profile image successfully stored to Firebase Storage", Toast.LENGTH_SHORT).show();

                                //saving the link of the image from the Firebase Storage to the Firebase Realtime Database
                                //to the user profile information

                                final String downloadUrl = uri.toString();
                                databaseReferenceUser.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){

                                            Intent selfSetupIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                            startActivity(selfSetupIntent);

                                            Toast.makeText(SetupActivity.this, "Profile image successfully stored to Firebase Database!", Toast.LENGTH_SHORT).show();
                                            progressDialogLoadingBar.dismiss();
                                        } else{
                                            String errorMessage = task.getException().getMessage();
                                            Toast.makeText(SetupActivity.this, "Error occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
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

    //method to save the user information into the Firebase Realtime Database
    private void saveAccountSetupInformation() {
        String getUsername = editTextUsername.getText().toString();
        String getFullName = editTextFullName.getText().toString();
        String getCountry = editTextCountry.getText().toString();

        if(TextUtils.isEmpty(getUsername)){
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(getFullName)){
            Toast.makeText(this, "Please write your full name", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(getCountry)){
            Toast.makeText(this, "Please write the name of your country", Toast.LENGTH_SHORT).show();
        } else{

            progressDialogLoadingBar.setTitle("Saving information...");
            progressDialogLoadingBar.setMessage("Please wait...");
            progressDialogLoadingBar.show();
            progressDialogLoadingBar.setCanceledOnTouchOutside(true);   //does't disappear when user touches background


            HashMap hashMapUser = new HashMap();
            hashMapUser.put("username", getUsername);
            hashMapUser.put("fullname", getFullName);
            hashMapUser.put("country", getCountry);
            hashMapUser.put("status", "Hey there!");
            hashMapUser.put("gender", "none");
            hashMapUser.put("dob", "none");
            hashMapUser.put("relationshipstatus", "none");
            databaseReferenceUser.updateChildren(hashMapUser).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your account is created successfully!", Toast.LENGTH_LONG).show();
                        progressDialogLoadingBar.dismiss();
                    } else{
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occurred: " + errorMessage, Toast.LENGTH_SHORT).show();
                        progressDialogLoadingBar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
