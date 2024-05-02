package com.example.choks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Signup extends AppCompatActivity {

    TextView signinpage;
    EditText username,email,password;
    Button btnsign;
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //bind with xml
        signinpage = findViewById(R.id.btn_signinpage);
        username = findViewById(R.id.edt_username);
        email = findViewById(R.id.edt_email);
        password = findViewById(R.id.edt_password);
        btnsign = findViewById(R.id.signinbutton);

        boolean isProfileSetupComplete = isProfileSetupComplete();

        mAuth = FirebaseAuth.getInstance();

        btnsign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_username = username.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                //here, we validate the fields
                if (str_username.isEmpty() || str_email.isEmpty() || str_password.isEmpty()) {
                    Toast.makeText(Signup.this, "Oops!! Looks like some fields are missing!!", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                    usersRef.orderByChild("username").equalTo(str_username.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Toast.makeText(Signup.this, "Username already exists. Please choose a different one.", Toast.LENGTH_SHORT).show();
                            } else {
                                mAuth.createUserWithEmailAndPassword(str_email, str_password)
                                        .addOnCompleteListener(taskSignUp -> {
                                            if (taskSignUp.isSuccessful()) {
                                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                                if (currentUser != null) {
                                                    String userId = currentUser.getUid();
                                                    DatabaseReference newUserRef = usersRef.child(userId);
                                                    Map<String, Object> user = new HashMap<>();
                                                    user.put("id", userId);
                                                    user.put("username", str_username);
                                                    user.put("email", str_email);
                                                    user.put("imageURL", "default");
                                                    user.put("search", str_username.toLowerCase());
                                                    user.put("description", "default");
                                                    user.put("status", "default");

                                                    newUserRef.setValue(user)
                                                            .addOnSuccessListener(aVoid -> {
                                                                Toast.makeText(Signup.this, "Sign up successful", Toast.LENGTH_SHORT).show();
                                                                Intent intent = new Intent(Signup.this, MainActivity.class);
                                                                setProfileSetupComplete(true);
                                                                startActivity(intent);
                                                                finish();
                                                            })
                                                            .addOnFailureListener(e -> {
                                                                Toast.makeText(Signup.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            });
                                                }
                                            } else {
                                                Toast.makeText(Signup.this, "Sign up failed: " + taskSignUp.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(Signup.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });



        signinpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signup.this,Signin.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.signingoogle1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private boolean isProfileSetupComplete() {
        SharedPreferences sharedPreferences = getSharedPreferences("profile_setup", MODE_PRIVATE);
        return sharedPreferences.getBoolean("is_profile_setup_complete", false);
    }
    private void setProfileSetupComplete(boolean isComplete) {
        SharedPreferences sharedPreferences = getSharedPreferences("profile_setup", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_profile_setup_complete", isComplete);
        editor.apply();
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

                        if (user != null) {
                            String userId = user.getUid();
                            DatabaseReference userRef = usersRef.child(userId);

                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (!dataSnapshot.exists()) {
                                        String str_username = user.getDisplayName();
                                        String str_email = user.getEmail();

                                        Map<String, Object> userMap = new HashMap<>();
                                        userMap.put("id", userId);
                                        userMap.put("username", str_username);
                                        userMap.put("email", str_email);
                                        userMap.put("imageURL", "default");
                                        userMap.put("search", str_username.toLowerCase());
                                        userMap.put("description", "default");
                                        userMap.put("status", "default");

                                        userRef.setValue(userMap)
                                                .addOnSuccessListener(aVoid -> {
                                                    Log.d(TAG, "User data created successfully");
                                                    Toast.makeText(Signup.this, "Welcome, " + str_username + "!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(Signup.this, Profile.class);
                                                    setProfileSetupComplete(true);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w(TAG, "Error creating user data", e);
                                                    Toast.makeText(Signup.this, "Failed to create user data", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(Signup.this, "Welcome back, " + dataSnapshot.child("username").getValue(String.class) + "!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Signup.this, Profile.class);
                                        setProfileSetupComplete(true);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "Error accessing user data", databaseError.toException());
                                    Toast.makeText(Signup.this, "Failed to access user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(Signup.this, "Login failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Signin.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}