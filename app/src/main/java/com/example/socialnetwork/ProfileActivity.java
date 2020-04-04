package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


// user can see his own information retrieved from the Firebase Realtime Database
// this class shares the same code as SettingsActivity.class in terms of retrieving the user information from the database
public class ProfileActivity extends AppCompatActivity {

    private TextView textViewStatus, textViewUsername, textViewFullName, textViewCountry, textViewDOB, textViewGender, textViewRelationshipStatus;
    private CircleImageView circleImageViewProfileProfileImage;

    private DatabaseReference databaseReferenceUserProfile;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        databaseReferenceUserProfile = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        textViewStatus = (TextView) findViewById(R.id.textViewProfileProfileStatus);
        textViewUsername = (TextView) findViewById(R.id.textViewProfileProfileUsername);
        textViewFullName = (TextView) findViewById(R.id.textViewProfileProfileName);
        textViewCountry = (TextView) findViewById(R.id.textViewProfileProfileCountry);
        textViewDOB = (TextView) findViewById(R.id.textViewProfileProfileDOB);
        textViewGender = (TextView) findViewById(R.id.textViewProfileProfileGender);
        textViewRelationshipStatus = (TextView) findViewById(R.id.textViewProfileProfileRelationshipStatus);
        circleImageViewProfileProfileImage = (CircleImageView) findViewById(R.id.circleImageViewProfileProfileImage);

        databaseReferenceUserProfile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String profileProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String profileUsername = dataSnapshot.child("username").getValue().toString();
                    String profileFullName = dataSnapshot.child("fullname").getValue().toString();
                    String profileCountry = dataSnapshot.child("country").getValue().toString();
                    String profileDOB = dataSnapshot.child("dob").getValue().toString();
                    String profileGender = dataSnapshot.child("gender").getValue().toString();
                    String profileRelationshipStatus = dataSnapshot.child("relationshipstatus").getValue().toString();
                    String profileProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    Picasso.get().load(profileProfileImage).placeholder(R.drawable.profile).into(circleImageViewProfileProfileImage);
                    textViewStatus.setText(profileProfileStatus);
                    textViewUsername.setText("@" + profileUsername);
                    textViewFullName.setText(profileFullName);
                    textViewCountry.setText("Country: " + profileCountry);
                    textViewDOB.setText("DOB: " + profileDOB);
                    textViewGender.setText("Gender: " + profileGender);
                    textViewRelationshipStatus.setText("Relationship status: " + profileRelationshipStatus);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
