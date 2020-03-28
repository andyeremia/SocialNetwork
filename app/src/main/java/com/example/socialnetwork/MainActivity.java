package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar toolbarMain;

    private CircleImageView circleImageViewNavProfileImage;
    private TextView textViewNavProfileUsername;
    private ImageButton imageButtonAddNewPost;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReferenceUser;    //reference to the Firebase Database used to check the user existence
    String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        try{
            currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        } catch (Exception e){
            sendUserToLoginActivity();
        }

        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users");   //

        toolbarMain = (Toolbar) findViewById(R.id.main_page_toolbar);   //adding the Toolbar to
        setSupportActionBar(toolbarMain);                          //the MainActivity
        getSupportActionBar().setTitle("Home");

        imageButtonAddNewPost = (ImageButton) findViewById(R.id.imageButtonMainPost);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);  // adding the "burger" to Home Toolbar
        actionBarDrawerToggle.syncState();                      //
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //
        navigationView =(NavigationView) findViewById(R.id.navigation_view);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);    //adding the navigation header to navigation menu
        circleImageViewNavProfileImage = (CircleImageView) navView.findViewById(R.id.circleImageViewNavHeaderProfileImage);
        textViewNavProfileUsername = (TextView) navView.findViewById(R.id.textViewNavHeaderUsername);

        //displaying the username and the profile picture of the user in the NavigationHeader
        databaseReferenceUser.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("full name")){
                        String usernameLink = dataSnapshot.child("full name").getValue().toString();
                        textViewNavProfileUsername.setText(usernameLink);
                    }
                    if(dataSnapshot.hasChild("profile image")){
                        String imageLink = dataSnapshot.child("profile image").getValue().toString();
                        Picasso.get().load(imageLink).placeholder(R.drawable.profile).into(circleImageViewNavProfileImage);
                    } else {
                        Toast.makeText(MainActivity.this, "Profile name doesn't exist!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userMenuSelector(item);         //method to select items from the menu located in the NavigationView
                return false;
            }
        });

        imageButtonAddNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToPostActivity();
            }
        });
    }

    private void sendUserToPostActivity() {
        Intent postIntent = new Intent (MainActivity.this, PostActivity.class);
        startActivity(postIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();  //the firebase user
        if(currentUser == null){        //check if user is/isn't authenticated
            sendUserToLoginActivity();  //if not -> method to send the user to LoginActivity
        } else{
            checkUserExistence();
        }
    }

    private void checkUserExistence() {
        final String currentUserId = mAuth.getCurrentUser().getUid();  //the id of the user who will be online
        databaseReferenceUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(currentUserId)){      //if the user is authenticated but has no information in the
                    sendUserToSetupActivity();                  // Firebase Realtime Database, redirect the user to SetupActivity
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent (MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }


    private void sendUserToLoginActivity() {    //method to send user to the LoginActivity
        Intent loginIntent = new Intent (MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void userMenuSelector(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post:
                sendUserToPostActivity();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                mAuth.signOut();    //signing out the user from the Firebase Authentication
                sendUserToLoginActivity();
                break;
        }
    }
}
