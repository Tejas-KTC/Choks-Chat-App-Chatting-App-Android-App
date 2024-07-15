package com.example.choks;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.choks.Adapter.User_Adapter;
import com.example.choks.Model.User_Data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ImageView setting;
    private CardView card_search;
    private RecyclerView recyclerView;
    private User_Adapter userAdapter;
    private List<User_Data> userList;
    private FirebaseUser currentUser;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Log.d("MainActivity", "onCreate: started");

        // Bind with XML
        setting = findViewById(R.id.menu_btn);
        card_search = findViewById(R.id.main_search);
        recyclerView = findViewById(R.id.user_recylerview);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new User_Adapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            Log.d("MainActivity", "onCreate: User is authenticated");
            fetchUsers();
        } else {
            Log.d("MainActivity", "onCreate: User is not authenticated, redirecting to login");
            redirectToLogin();
        }

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Setting.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });

        card_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Search.class);
                intent.putExtra("Count", 1);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        MainActivity.this,
                        findViewById(R.id.main_search),
                        "search"
                );
                startActivity(intent, options.toBundle());
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            card_search.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    card_search.getViewTreeObserver().removeOnPreDrawListener(this);
                    startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    private void fetchUsers() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Chats");
        String currentUserId = currentUser.getUid();

        ValueEventListener messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Set<String> userSet = new HashSet<>();
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        String receiverId = messageSnapshot.child("receiver").getValue(String.class);
                        String senderId = messageSnapshot.child("sender").getValue(String.class);

                        if (receiverId != null && senderId != null) {
                            if (receiverId.equals(currentUserId)) {
                                userSet.add(senderId);
                            } else if (senderId.equals(currentUserId)) {
                                userSet.add(receiverId);
                            }
                        }
                    }
                    Log.d("MainActivity", "Users involved in messages: " + userSet);
                    fetchUserData(userSet);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error fetching users: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        messagesRef.addListenerForSingleValueEvent(messageListener);
    }

    private void fetchUserData(Set<String> userSet) {
        if (userSet.isEmpty()) {
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userList.clear();
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        if (userSet.contains(userId)) {
                            String profileImageUrl = userSnapshot.child("imageURL").getValue(String.class);
                            String username = userSnapshot.child("username").getValue(String.class);
                            String lastMessage = userSnapshot.child("lastMessage").getValue(String.class);
                            String status = userSnapshot.child("status").getValue(String.class);
                            String fcmToken = userSnapshot.child("token").getValue(String.class);

                            countUnseenMessages(currentUser.getUid(), username, new CountCallback() {
                                @Override
                                public void onCountReceived(int count) {
                                    User_Data user = new User_Data(profileImageUrl, username, lastMessage, count, fcmToken, status);
                                    userList.add(user);

                                    userAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error fetching users: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        usersRef.addListenerForSingleValueEvent(userListener);
    }

    private void countUnseenMessages(String currentUserId, String username, CountCallback callback) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Chats");

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userId = dataSnapshot.getChildren().iterator().next().getKey();
                    messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int unseenMessageCount = 0;
                            for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                                String receiverId = messageSnapshot.child("receiver").getValue(String.class);
                                String senderId = messageSnapshot.child("sender").getValue(String.class);
                                boolean isSeen = messageSnapshot.child("isseen").getValue(Boolean.class);

                                if (receiverId != null && senderId != null) {
                                    if ((receiverId.equals(currentUserId) && senderId.equals(userId)) && !isSeen) {
                                        unseenMessageCount++;
                                    }
                                }
                            }
                            callback.onCountReceived(unseenMessageCount);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("MainActivity", "Error counting unseen messages: " + databaseError.getMessage());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainActivity", "Error fetching user data for unseen messages: " + databaseError.getMessage());
            }
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, Signin.class);
        startActivity(intent);
        finish();
    }

    interface CountCallback {
        void onCountReceived(int count);
    }
}
