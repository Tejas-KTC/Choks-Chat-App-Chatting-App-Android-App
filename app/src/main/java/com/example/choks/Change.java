package com.example.choks;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

public class Change extends AppCompatActivity {

    EditText old_pass,new_pass,new_confirm;
    TextView back;
    Button btn_continue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //bind with xml
        old_pass = findViewById(R.id.change_old);
        new_confirm = findViewById(R.id.change_new_confirm);
        new_pass = findViewById(R.id.change_new);
        back = findViewById(R.id.backbtn);
        btn_continue = findViewById(R.id.change_continue);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Change.this,Setting.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentPassword = old_pass.getText().toString().trim();
                String newPassword = new_pass.getText().toString().trim();
                String newConfirm = new_confirm.getText().toString().trim();

                if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(newConfirm)) {
                    Toast.makeText(Change.this, "Oops!! Looks like some fields are missing!!", Toast.LENGTH_SHORT).show();
                } else if (newPassword.equals(currentPassword)) {
                    Toast.makeText(Change.this, "Password is the same as the old one", Toast.LENGTH_SHORT).show();
                } else if (!newPassword.equals(newConfirm)) {
                    Toast.makeText(Change.this, "New password and confirm password do not match!!", Toast.LENGTH_SHORT).show();
                } else {
                    changePassword(currentPassword, newPassword);
                }
            }
        });

    }

    private void changePassword(String currentPassword, String newPassword) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Change.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance().signOut();
                                        clearSharedPreferences();
                                        Intent intent = new Intent(Change.this, Signin.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle password update failure
                                        Toast.makeText(Change.this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Change.this, "Reauthentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void clearSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences("profile_setup", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Setting.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}