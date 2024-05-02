package com.example.choks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Signin extends AppCompatActivity {

    TextView signuppage;
    EditText edt_email,edt_password;
    Button btn_signin;
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //bind with xml
        signuppage = findViewById(R.id.btn_signuppage);
        edt_email = findViewById(R.id.edt_email1);
        edt_password = findViewById(R.id.edt_password1);
        btn_signin = findViewById(R.id.buttonsignin);

        boolean isProfileSetupComplete = isProfileSetupComplete();

        if (isProfileSetupComplete) {
            startActivity(new Intent(Signin.this, MainActivity.class));
            finish();
        }

        signuppage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Signin.this,Signup.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edt_email.getText().toString();
                String password = edt_password.getText().toString();

                //here, we validate the fields
                if(email.isEmpty() || password.isEmpty())
                {
                    Toast.makeText(Signin.this, "Oops!!Looks like some fields are missing!!", Toast.LENGTH_SHORT).show();
                }
                else {
                    signInWithEmailAndPassword(email,password);
                }
            }
        });



        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.signingoogle).setOnClickListener(new View.OnClickListener() {
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
                                                    Toast.makeText(Signin.this, "Welcome, " + str_username + "!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(Signin.this, Profile.class);
                                                    setProfileSetupComplete(true);
                                                    startActivity(intent);
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.w(TAG, "Error creating user data", e);
                                                    Toast.makeText(Signin.this, "Failed to create user data", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(Signin.this, "Welcome back, " + dataSnapshot.child("username").getValue(String.class) + "!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(Signin.this, Profile.class);
                                        setProfileSetupComplete(true);
                                        startActivity(intent);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.e(TAG, "Error accessing user data", databaseError.toException());
                                    Toast.makeText(Signin.this, "Failed to access user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(Signin.this, "Login failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void setProfileSetupComplete(boolean isComplete) {
        SharedPreferences sharedPreferences = getSharedPreferences("profile_setup", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("is_profile_setup_complete", isComplete);
        editor.apply();
    }
    public void signInWithEmailAndPassword(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Signin.this, "Sign in successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Signin.this,Profile.class);
                            setProfileSetupComplete(true);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Signin.this, "Sign in failed: ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}