package com.example.choks.Model;

import com.google.firebase.Timestamp;

public class Message_model {
    private String text;
    private String senderId;
    private String receiverId;
    private boolean isseen;
    private Timestamp timestamp;

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }


    public Message_model(String text, String senderId, String receiverId, boolean isseen) {
        this.text = text;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.isseen = isseen;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public Message_model() {
    }



    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSeen() {
        return isseen;
    }

    public void setSeen(boolean isseen) {
        this.isseen = isseen;
    }
}
