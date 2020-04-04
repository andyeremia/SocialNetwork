package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private ImageButton imageButtonCommentsPost;
    private EditText editTextCommentsInput;
    private RecyclerView recyclerViewCommentsList;

    private DatabaseReference databaseReferenceUser, databaseReferencePost;
    private FirebaseAuth mAuth;

    private String postKey, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        postKey = getIntent().getExtras().get("postKey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        databaseReferenceUser = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReferencePost = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("Comments");

        recyclerViewCommentsList = (RecyclerView) findViewById(R.id.recyclerViewComments);
        recyclerViewCommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerViewCommentsList.setLayoutManager(linearLayoutManager);

        editTextCommentsInput = (EditText) findViewById(R.id.editTextCommentsInput);
        imageButtonCommentsPost = (ImageButton) findViewById(R.id.imageButtonCommentsPostComment);

        imageButtonCommentsPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReferenceUser.child(currentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String userName = dataSnapshot.child("username").getValue().toString();

                            validateComment(userName);
                            editTextCommentsInput.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options =
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(databaseReferencePost, Comments.class)
                        .build();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {
                        holder.textViewCommentUsername.setText("@" + model.getUsername() + " ");
                        holder.textViewCommentDate.setText(model.getCommentDate());
                        holder.textViewCommentTime.setText(model.getCommentTime());
                        holder.textViewCommentInput.setText(model.getComment());
                    }

                    @NonNull
                    @Override
                    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);
                        CommentsViewHolder commentsViewHolder = new CommentsViewHolder(view);
                        return commentsViewHolder;
                    }
                };

        recyclerViewCommentsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCommentUsername, textViewCommentDate, textViewCommentTime, textViewCommentInput;
        public CommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewCommentUsername = (TextView) itemView.findViewById(R.id.textViewAllCommentsUsername);
            textViewCommentDate = (TextView) itemView.findViewById(R.id.textViewAllCommentsDate);
            textViewCommentTime = (TextView) itemView.findViewById(R.id.textViewAllCommentsTime);
            textViewCommentInput = (TextView) itemView.findViewById(R.id.textViewAllCommentsCommentText);
        }
    }

    private void validateComment(String userName) {
        String commentText = editTextCommentsInput.getText().toString();

        if(TextUtils.isEmpty(commentText)){
            Toast.makeText(this, "Please write text", Toast.LENGTH_SHORT).show();
        } else{
            //get the date and time from when the comment was written so we can create a random key for the comment
            //in the Firebase Database and store those as well
            Calendar calendarCommentDate = Calendar.getInstance();
            SimpleDateFormat simpleDateFormatCurrentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = simpleDateFormatCurrentDate.format(calendarCommentDate.getTime());

            Calendar calendarCommentTime = Calendar.getInstance();
            SimpleDateFormat simpleDateFormatCurrentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = simpleDateFormatCurrentTime.format(calendarCommentTime.getTime());

            final String commentRandomKey = currentUserId + saveCurrentDate + saveCurrentTime;

            HashMap hashMapComments = new HashMap();
                hashMapComments.put("userid", currentUserId);
                hashMapComments.put("comment", commentText);
                hashMapComments.put("commentDate", saveCurrentDate);
                hashMapComments.put("commentTime", saveCurrentTime);
                hashMapComments.put("username", userName);
            databaseReferencePost.child(commentRandomKey).updateChildren(hashMapComments)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CommentsActivity.this, "You have commented successfully!", Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(CommentsActivity.this, "Error occurred! Try again!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

}
