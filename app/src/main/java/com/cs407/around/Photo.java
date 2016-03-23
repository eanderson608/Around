package com.cs407.around;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class Photo {

    private String userId;
    private String userName;
    private String fileName;
    private long upvotes;
    private long downvotes;
    private long timeStamp;
    private double[] location;

    // Public constructor, uses parameter userId and current time
    // to determine fileName
    public Photo(String userId) {
        this.userId = userId;
        this.upvotes = 0;
        this.downvotes = 0;
        this.timeStamp = System.currentTimeMillis();
        this.fileName = userId + "-" + Long.toString(this.timeStamp) + ".jpg";
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(long upvotes) {
        this.upvotes = upvotes;
    }

    public long getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(long downvotes) {
        this.downvotes = downvotes;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double[] getLocation() {
        return location;
    }

    public void setLocation(double[] location) {
        this.location = location;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                ", timeStamp=" + timeStamp +
                ", location=" + Arrays.toString(location) +
                '}';
    }
}
