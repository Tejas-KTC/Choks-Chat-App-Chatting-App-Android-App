package com.example.choks;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Bottom_dialog_name extends BottomSheetDialogFragment {
    EditText ed;
    public Bottom_dialog_name() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_name,container,false);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btn_add=view.findViewById(R.id.btn_save);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btn_cancel=view.findViewById(R.id.btn_cancel);
        ed= view.findViewById(R.id.update_username);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateUsername();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return view;
    }
    private void updateUsername() {
        String newUsername = ed.getText().toString().trim();

        if (newUsername.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("username").equalTo(newUsername.toLowerCase()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        DatabaseReference userRef = usersRef.child(currentUser.getUid());

                        userRef.child("username").setValue(newUsername);
                        userRef.child("search").setValue(newUsername.toLowerCase())
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
                                    dismiss();
                                })
                                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update search name: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(getContext(), "Username already exists. Please choose a different one.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to check for duplicate username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
