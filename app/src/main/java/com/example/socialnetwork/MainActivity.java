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
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);   //adding the Toolbar to
        setSupportActionBar(mToolbar);                          //the MainActivity
        getSupportActionBar().setTitle("Home");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);  // adding the "burger" to Home Toolbar
        actionBarDrawerToggle.syncState();                      //
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //
        navigationView =(NavigationView) findViewById(R.id.navigation_view);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);    //adding the navigation header to navigation menu



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                userMenuSelector(item);         //method to select items from the menu located in the NavigationView
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){        //check if user is/isn't authenticated
            sendUserToLoginActivity();  //if not -> method to send the user to LoginActivity
        }
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
