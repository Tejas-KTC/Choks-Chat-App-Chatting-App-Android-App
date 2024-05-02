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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class Bottom_dialog_des extends BottomSheetDialogFragment {
    EditText ed;
    public Bottom_dialog_des() {
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_des,container,false);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btn_add=view.findViewById(R.id.btn_save);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button btn_cancel=view.findViewById(R.id.btn_cancel);
        ed= view.findViewById(R.id.update_des);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDes();
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
    private void updateDes() {
        String newDes = ed.getText().toString().trim();

        if (newDes.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a Description", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

            userRef.child("description").setValue(newDes)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Description updated successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update Description: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

}
