package com.example.choks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.example.choks.Model.User_Data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Search extends AppCompatActivity {

    TextView txt_back;
    CardView cardView;
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private SearchAdapter searchAdapter;
    private List<User_Data> searchResults;
    private DatabaseReference usersRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //bind with xml
        txt_back = findViewById(R.id.backbtn);
        cardView = findViewById(R.id.main_search);
        searchEditText = findViewById(R.id.search_user);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        searchResults = new ArrayList<>();
        searchAdapter = new SearchAdapter(searchResults,this);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchAdapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        txt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Search.this, MainActivity.class);
                intent.putExtra("Count",1);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        Search.this,
                        cardView,
                        "search"
                );
                startActivity(intent, options.toBundle());
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            cardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    cardView.getViewTreeObserver().removeOnPreDrawListener(this);
                    startPostponedEnterTransition();
                    return true;
                }
            });
        }
    }

    private void search(String query) {
        searchResults.clear();

        Query queryRef = usersRef.orderByChild("search")
                .startAt(query)
                .endAt(query + "\uf8ff");

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String imageURL = snapshot.child("imageURL").getValue(String.class);
                    String username = snapshot.child("username").getValue(String.class);
                    String lastMsgDate = snapshot.child("lastMsgDate").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String fcmToken = snapshot.child("token").getValue(String.class);

                    searchResults.add(new User_Data(imageURL, username, lastMsgDate,0,fcmToken,status));
                }

                searchAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
