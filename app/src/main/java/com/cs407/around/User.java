package com.cs407.around;


import java.util.ArrayList;

public class User {

    private String _id;
    private String userId;
    private String name;
    private String firstName;
    private String lastName;
    private String authToken;
    private String userImageUrl;


    public String getUserProfilePic() {
        return userImageUrl;
    }

    public void setUserProfilePic(String userImageUrl) {
        this.userImageUrl = userImageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public String toString() {
        return "{" +
                "_id='" + _id + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", authToken='" + authToken + '\'' +
                ", userImageUrl='" + userImageUrl + '\'' +
                '}';
    }
}
