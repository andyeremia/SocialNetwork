package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class PostActivity extends AppCompatActivity {

    private Toolbar toolbarPost;
    private ImageButton imageButtonSelectPostImage;
    private Button buttonUpdatePost;
    private EditText editTextPostDescription;

    private static final int galleryPic = 1;
    private Uri postImageUri;
    private String getPostDescription;

    private StorageReference storageReferencePostImages;
    private DatabaseReference databaseReferenceUser, databaseReferencePost;
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialogLoadingBar;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        storageReferencePostImages = FirebaseStorage.getInstance().getReference();
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferencePost = FirebaseDatabase.getInstance().getReference().child("Posts");

        imageButtonSelectPostImage = (ImageButton) findViewById(R.id.imageButtonPostPostImage);
        buttonUpdatePost = (Button) findViewById(R.id.buttonPostUpdatePost);
        editTextPostDescription = (EditText) findViewById(R.id.editTextPostPostDescription);

        toolbarPost = (Toolbar) findViewById(R.id.toolbarPostUpdatePost);
        setSupportActionBar(toolbarPost);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        progressDialogLoadingBar = new ProgressDialog(this);

        imageButtonSelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        buttonUpdatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInformation();
            }
        });
    }

    private void validatePostInformation() {
        getPostDescription = editTextPostDescription.getText().toString();
        if(postImageUri == null){
            Toast.makeText(this, "Please select post image", Toast.LENGTH_SHORT).show();
        } else if(TextUtils.isEmpty(getPostDescription)) {
            Toast.makeText(this, "Please say something about your image", Toast.LENGTH_SHORT).show();
        } else{

            progressDialogLoadingBar.setTitle("Add new post");
            progressDialogLoadingBar.setMessage("Updating post. Please wait...");
            progressDialogLoadingBar.show();
            progressDialogLoadingBar.setCanceledOnTouchOutside(true);   //does't disappear when user touches background

            storingPostImageToFirebaseStorage();
        }
    }

    //storing the post image in the Firebase Storage and giving the post image a unique name made of it's posting date & time
    private void storingPostImageToFirebaseStorage() {
        Calendar calendarPostDate = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatCurrentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = simpleDateFormatCurrentDate.format(calendarPostDate.getTime());

        Calendar calendarPostTime = Calendar.getInstance();
        SimpleDateFormat simpleDateFormatCurrentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = simpleDateFormatCurrentTime.format(calendarPostTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference storageReferenceFilePath = storageReferencePostImages.child("Post Images").child(postImageUri.getLastPathSegment() + postRandomName + ".jpg");

        storageReferenceFilePath.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(PostActivity.this, "Image uploaded to Firebase Storage", Toast.LENGTH_SHORT).show();

                //getting the link of the post image from the Firebase Storage
                storageReferenceFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        downloadUrl = uri.toString();
                        savingPostInformationToDatabase();
                    }
                });
            }
        });
    }

    private void savingPostInformationToDatabase() {
        databaseReferenceUser.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String getUserFullName = dataSnapshot.child("fullname").getValue().toString();
                    String getUserProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap hashMapPosts = new HashMap();
                        hashMapPosts.put("userid", currentUserId);
                        hashMapPosts.put("date", saveCurrentDate);
                        hashMapPosts.put("time", saveCurrentTime);
                        hashMapPosts.put("description", getPostDescription);
                        hashMapPosts.put("postimage", downloadUrl);
                        hashMapPosts.put("profileimage", getUserProfileImage);
                        hashMapPosts.put("fullname", getUserFullName);
                    databaseReferencePost.child(currentUserId + postRandomName).updateChildren(hashMapPosts).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New post is updated successfully", Toast.LENGTH_SHORT).show();
                                progressDialogLoadingBar.dismiss();
                            } else{
                                Toast.makeText(PostActivity.this, "Error occurred while updating post", Toast.LENGTH_SHORT).show();
                                progressDialogLoadingBar.dismiss();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, galleryPic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == galleryPic && resultCode == RESULT_OK && data != null){
            postImageUri = data.getData();
            imageButtonSelectPostImage.setImageURI(postImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
