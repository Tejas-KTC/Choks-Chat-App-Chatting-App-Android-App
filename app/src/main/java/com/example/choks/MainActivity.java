package com.example.choks;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationService;

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

        //bind with xml
        setting = findViewById(R.id.menu_btn);
        card_search = findViewById(R.id.main_search);
        recyclerView = findViewById(R.id.user_recylerview);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        userAdapter = new User_Adapter(this, userList);
        recyclerView.setAdapter(userAdapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_userid = currentUser.getUid();
        start_service(current_userid);
        fetchUsers();

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
        obtainAndStoreFCMToken();
    }

    private void fetchUsers() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("Chats");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ValueEventListener messageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Set<String> userSet = new HashSet<>();
                    for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                        String receiverId = messageSnapshot.child("receiver").getValue(String.class);
                        String senderId = messageSnapshot.child("sender").getValue(String.class);

                        if (receiverId.equals(currentUserId)) {
                            userSet.add(senderId);
                        } else if (senderId.equals(currentUserId)) {
                            userSet.add(receiverId);
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

                            countUnseenMessages(FirebaseAuth.getInstance().getCurrentUser().getUid(), username, new CountCallback() {
                                @Override
                                public void onCountReceived(int count) {
                                    User_Data user = new User_Data(profileImageUrl, username, lastMessage, count,fcmToken,status);
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

                                if ((receiverId.equals(currentUserId) && senderId.equals(userId))) {
                                    if (!isSeen) {
                                        unseenMessageCount++;
                                    }
                                }
                            }
                            callback.onCountReceived(unseenMessageCount);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void obtainAndStoreFCMToken() {
        // Get the FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult();
                        Log.d("FCM Token", "Token: " + fcmToken);

                        // Store the FCM token in the user's data in the database
                        storeFCMToken(fcmToken);
                    } else {
                        Log.e("FCM Token", "Failed to obtain token: " + task.getException());
                    }
                });
    }

    private void storeFCMToken(String fcmToken) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("token");

        userRef.setValue(fcmToken)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM Token", "Token stored successfully");
                    } else {
                        Log.e("FCM Token", "Failed to store token: " + task.getException());
                    }
                });
    }

    interface CountCallback {
        void onCountReceived(int count);
    }
    public void start_service(String userid)
    {
        Application application = getApplication();
        long appID = 949773437;
        String appSign = "8e14d70fc5b647ddbd788650325d8d6fc1f4a77d714b88318d7ffba2b078d2c7";
        String userName = userid;

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallService.init(getApplication(), appID, appSign, userid, userName,callInvitationConfig);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZegoUIKitPrebuiltCallInvitationService.unInit();
    }
}
