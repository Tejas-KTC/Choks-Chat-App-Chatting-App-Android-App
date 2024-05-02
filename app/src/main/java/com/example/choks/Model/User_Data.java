package com.example.choks.Model;

public class User_Data {
    private String imageURL;
    private String username;
    private String lastMsgDate;
    private int unseenMsgCount;


    public int getUnseenMsgCount() {
        return unseenMsgCount;
    }

    public void setUnseenMsgCount(int unseenMsgCount) {
        this.unseenMsgCount = unseenMsgCount;
    }

    public User_Data(String imageURL, String username, String lastMsgDate, int unseenMsgCount) {
        this.imageURL = imageURL;
        this.username = username;
        this.lastMsgDate = lastMsgDate;
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
}
