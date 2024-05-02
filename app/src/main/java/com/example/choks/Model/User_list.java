package com.example.choks.Model;
public class User_list {
    private String profileImageUrl;
    private String username;
    private String lastMessage;

    public User_list() {
        // Empty constructor needed for Firestore serialization
    }

    public User_list(String profileImageUrl, String username, String lastMessage) {
        this.profileImageUrl = profileImageUrl;
        this.username = username;
        this.lastMessage = lastMessage;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}

