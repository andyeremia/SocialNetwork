package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;


// activity for the user to modify (delete/edit) his posts
public class ClickPostActivity extends AppCompatActivity {

    private ImageView imageViewPostImage;
    private TextView textViewPostDescription;
    private Button buttonDeletePost, buttonEditPost;

    private DatabaseReference databaseReferenceClickPost;
    private FirebaseAuth mAuth;

    private String postKey, currentUserId, databaseUserId, postDescription, postImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        postKey = getIntent().getExtras().get("postKey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();    //the user who will be online
        databaseReferenceClickPost = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey);

        imageViewPostImage = (ImageView) findViewById(R.id.imageViewClickPostImage);
        textViewPostDescription = (TextView) findViewById(R.id.textViewClickPostDescription);
        buttonDeletePost = (Button) findViewById(R.id.buttonClickPostDelete);
        buttonEditPost = (Button) findViewById(R.id.buttonClickPostEdit);

        //at start, buttons will be invisible so only the author of the post can edit/delete it
        buttonDeletePost.setVisibility(View.INVISIBLE);
        buttonEditPost.setVisibility(View.INVISIBLE);

        //getting the post description and post image that have to be deleted/edited
        databaseReferenceClickPost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    postDescription = dataSnapshot.child("description").getValue().toString();
                    postImage = dataSnapshot.child("postimage").getValue().toString();

                    databaseUserId = dataSnapshot.child("userid").getValue().toString();

                    textViewPostDescription.setText(postDescription);
                    Picasso.get().load(postImage).into(imageViewPostImage);

                    if(currentUserId.equals(databaseUserId)){
                        buttonDeletePost.setVisibility(View.VISIBLE);
                        buttonEditPost.setVisibility(View.VISIBLE);
                    }

                    //to the edit post method when we can edit only the post description
                    buttonEditPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editCurrentPost(postDescription);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        buttonDeletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteCurrentPost();
            }
        });
    }

    private void editCurrentPost(String postDescription) {
        //making a dialog to update the post description
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        //creating the input field to edit the post description
        final EditText editTextInputField = new EditText(ClickPostActivity.this);
        editTextInputField.setText(postDescription);
        builder.setView(editTextInputField);

        //buttons for the update and cancel the post description in the dialog
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseReferenceClickPost.child("description").setValue(editTextInputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "Post updated successfully...", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);
    }

    //delete only from Firebase Database and not the image from the Firebase Storage
    private void deleteCurrentPost() {
        databaseReferenceClickPost.removeValue();
        sendUserToMainActivity();
        Toast.makeText(this, "Post has been deleted!", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
