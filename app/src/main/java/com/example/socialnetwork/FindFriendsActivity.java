package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar toolbarFindFriends;

    private ImageButton imageButtonSearchFriends;
    private EditText editTextSearchInput;
    private RecyclerView recyclerViewSearchResultList;

    private DatabaseReference databaseReferenceUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        databaseReferenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbarFindFriends = (Toolbar) findViewById(R.id.toolbarFindFriends);
        setSupportActionBar(toolbarFindFriends);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        recyclerViewSearchResultList = (RecyclerView) findViewById(R.id.recyclerViewSearchResultList);
        recyclerViewSearchResultList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewSearchResultList.setLayoutManager(linearLayoutManager);

        imageButtonSearchFriends = (ImageButton) findViewById(R.id.imageButtonSearchFriends);
        editTextSearchInput = (EditText) findViewById(R.id.editTextSearchInput);

        imageButtonSearchFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = editTextSearchInput.getText().toString();
                searchPeopleAndFriends(searchBoxInput);
            }
        });
    }

    private void searchPeopleAndFriends(String searchBoxInput) {

        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();

        //query to filter/search app users in the Firebase Database by their fullname
        Query querySearchFriends = databaseReferenceUsers.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        //in the .setQuery() method, the first parameter will be the Query object with which we search for the users
        //the query objects has a DatabaseReference object assigned
        FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(querySearchFriends, FindFriends.class)
                        .build();

        FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder> firebaseRecyclerAdapterFriends =
                new FirebaseRecyclerAdapter<FindFriends, FindFriendsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, int position, @NonNull FindFriends model) {

                        holder.textViewAllUsersUsername.setText(model.getFullname());
                        holder.textViewAllUsersStatus.setText(model.getStatus());
                        Picasso.get().load(model.getProfileimage()).into(holder.circleImageViewAllUsersProfileImage);
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                        FindFriendsViewHolder findFriendsViewHolder = new FindFriendsViewHolder(view);
                        return findFriendsViewHolder;
                    }
                };
        recyclerViewSearchResultList.setAdapter(firebaseRecyclerAdapterFriends);
        firebaseRecyclerAdapterFriends.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        CircleImageView circleImageViewAllUsersProfileImage;
        TextView textViewAllUsersUsername, textViewAllUsersStatus;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageViewAllUsersProfileImage = (CircleImageView) itemView.findViewById(R.id.circleImageViewAllUsersProfileImage);
            textViewAllUsersUsername = (TextView) itemView.findViewById(R.id.textViewAllUsersProfileFullName);
            textViewAllUsersStatus = (TextView) itemView.findViewById(R.id.textViewAllUsersProfileStatus);
        }
    }
}
