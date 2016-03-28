package com.cs407.around;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class Photo {

    private String _id;
    private String userId;
    private String userName;
    private String fileName;
    private long upvotes;
    private long downvotes;
    private long time;
    private long score;
    private double[] location;

    // Public constructor, uses parameter userId and current time
    // to determine fileName
    public Photo(String userId) {
        this.userId = userId;
        this.upvotes = 0;
        this.downvotes = 0;
        this.time = System.currentTimeMillis();
        this.fileName = userId + "-" + Long.toString(this.time) + ".jpg";
    }

    public String get_id() {
        return _id;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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

    public long getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "{" +
                "_id='" + _id + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                ", time=" + time +
                ", score=" + score +
                ", location=" + Arrays.toString(location) +
                '}';
    }
}
