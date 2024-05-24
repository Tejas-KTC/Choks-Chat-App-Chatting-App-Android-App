package com.example.choks;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.choks.Adapter.Message_Adapter;
import com.example.choks.Model.Message_model;
import com.example.choks.Model.User_Data;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class Message extends AppCompatActivity {

    TextView user_name, back_btn;
    CircleImageView image_user;
    EditText send_txt;
    ImageButton btn_send;
    private static final String STATUS_ONLINE = "online";
    private static final String STATUS_OFFLINE = "offline";
    String username, recieveruserId, imageURL;
    RecyclerView recyclerView;
    private FirebaseUser currentUser;
    DatabaseReference reference;
    private Message_Adapter message_adapter;
    private List<Message_model> messages;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // Bind views with XML
        back_btn = findViewById(R.id.backbtn);
        user_name = findViewById(R.id.msg_username);
        image_user = findViewById(R.id.msg_profile_image);
        btn_send = findViewById(R.id.btn_send);
        send_txt = findViewById(R.id.send_text);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        username = intent.getStringExtra("userID");
        user_name.setText(username);

        fetchUserData(username);
        readMessages(currentUser.getUid(), recieveruserId);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(username);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                fetchUserData(username);
                readMessages(currentUser.getUid(), recieveruserId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        btn_send.setOnClickListener(v -> {
            String text = send_txt.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
            }
        });

        back_btn.setOnClickListener(view -> {
            Intent intent1 = new Intent(Message.this, MainActivity.class);
            startActivity(intent1);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserStatus(STATUS_ONLINE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateUserStatus(STATUS_OFFLINE);
    }

    private void updateUserStatus(String status) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid()).child("status");
            userStatusRef.setValue(status)
                    .addOnSuccessListener(aVoid -> Log.d("UpdateStatus", "User status updated successfully"))
                    .addOnFailureListener(e -> {
                        Log.e("UpdateStatus", "Failed to update user status: " + e.getMessage());
                        Toast.makeText(Message.this, "Failed to update user status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e("UpdateStatus", "Current user is null");
        }
    }

    private void sendMessage(String message) {
        reference = FirebaseDatabase.getInstance().getReference().child("Chats");

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender", currentUser.getUid());
        messageMap.put("receiver", recieveruserId);
        messageMap.put("message", message);
        messageMap.put("isseen", false);

        reference.push().setValue(messageMap)
                .addOnSuccessListener(aVoid -> {
                    send_txt.setText("");
                    readMessages(currentUser.getUid(), recieveruserId);
                })
                .addOnFailureListener(e -> Toast.makeText(Message.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void readMessages(String myId, String userId) {
        messages = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String sender = snapshot.child("sender").getValue(String.class);
                    String receiver = snapshot.child("receiver").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);
                    Boolean isseen = snapshot.child("isseen").getValue(Boolean.class);

                    if ((receiver != null && receiver.equals(myId) && sender != null && sender.equals(userId)) ||
                            (receiver != null && receiver.equals(userId) && sender != null && sender.equals(myId))) {
                        Message_model chat = new Message_model(message, sender, receiver, isseen != null ? isseen : false);
                        messages.add(chat);

                        if (receiver.equals(myId) && sender.equals(userId)) {
                            snapshot.getRef().child("isseen").setValue(true);
                        }
                    }
                }

                message_adapter = new Message_Adapter(messages, Message.this, imageURL);
                recyclerView.setAdapter(message_adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Message.this, "Failed to read messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserData(String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        imageURL = snapshot.child("imageURL").getValue(String.class);
                        recieveruserId = snapshot.getKey();
                        loadImage(imageURL);

                        readMessages(currentUser.getUid(), recieveruserId);
                        return;
                    }
                } else {
                    Toast.makeText(Message.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(Message.this, "Error fetching user data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImage(String imageURL) {
        RequestOptions requestOptions = new RequestOptions().placeholder(R.drawable.person_button_svgrepo_com).error(R.mipmap.ic_launcher_round);
        Glide.with(this)
                .load(imageURL)
                .apply(requestOptions)
                .into(image_user);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}
