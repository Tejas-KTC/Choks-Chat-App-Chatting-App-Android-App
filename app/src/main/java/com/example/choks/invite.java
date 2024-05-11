package com.example.choks;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

public class invite extends AppCompatActivity {

    TextView backbtn;
    Button btn_invite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        //bind with xml
        backbtn = findViewById(R.id.backbtn);
        btn_invite = findViewById(R.id.btn_invite);


        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(invite.this,Setting.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

        btn_invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "🎉 Welcome to Choks! 🎉\n\n" +
                        "Hey there! Welcome aboard to Choks, our friendly chat app where you can connect and chat with friends and family seamlessly. Whether it's catching up with old friends or making new ones, Choks has got you covered!\n\n" +
                        "Feel free to explore all the features Choks has to offer, including chats, and more. Don't forget to personalize your profile and make it your own!\n\n" +
                        "Happy chatting! 🚀\n\n"+
                        "-By KTC_Tej";
                String link = "https://github.com/Tejas-KTC/Choks-Chat-App-Chatting-App-Android-App";

                BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.drawable.logo);
                Bitmap bitmap = drawable.getBitmap();
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Choks Logo", null);
                Uri logoUri = Uri.parse(path);

                shareMessageWithLinkAndImage(message, link, logoUri);
            }
        });
    }

    private void shareMessageWithLinkAndImage(String message, String link, Uri imageUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message + "\n\n" + link);

        Intent chooserIntent = Intent.createChooser(shareIntent, "Share Welcome Message");

        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooserIntent);
        }
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