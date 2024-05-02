package com.example.choks;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class help extends AppCompatActivity {

    private EditText descriptionEditText;
    private Button continueButton;
    private TextView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //bind with xml
        descriptionEditText = findViewById(R.id.help_des);
        continueButton = findViewById(R.id.help_continue);
        backButton = findViewById(R.id.backbtn);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddress = "boxcode07@gmail.com";
                String message = descriptionEditText.getText().toString();

                if (!emailAddress.isEmpty() && !message.isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_SEND);

                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                    intent.putExtra(Intent.EXTRA_TEXT, message);

                    intent.setType("message/rfc822");

                    startActivity(Intent.createChooser(intent, "Choose an Email client :"));
                } else {
                    Toast.makeText(help.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                }


            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(help.this,Setting.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });
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
