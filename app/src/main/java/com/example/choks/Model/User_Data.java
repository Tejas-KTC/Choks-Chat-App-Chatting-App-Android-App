package com.example.choks.Model;

public class User_Data {
    private String imageURL;
    private String username;
    private String lastMsgDate;
    private int unseenMsgCount;
    private String token;
    private String status;

    public User_Data() {
    }

    public User_Data(String imageURL, String username, String lastMsgDate, int unseenMsgCount, String token, String status) {
        this.imageURL = imageURL;
        this.username = username;
        this.lastMsgDate = lastMsgDate;
        this.unseenMsgCount = unseenMsgCount;
        this.token = token;
        this.status = status;
    }

    public int getUnseenMsgCount() {
        return unseenMsgCount;
    }

    public void setUnseenMsgCount(int unseenMsgCount) {
        this.unseenMsgCount = unseenMsgCount;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getUsername() {
        return username;
    }

    public String getLastMsgDate() {
        return lastMsgDate;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
