package com.cs407.around;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.facebook.internal.CollectionMapper;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class User {

    private String _id;
    private String userId;
    private String name;
    private String firstName;
    private String lastName;
    private ArrayList<String> upvotes;
    private ArrayList<String> downvotes;
    private long score;
    private String authToken;
    private String userImageUrl;
    private PreferencesHelper prefs;

    public User() {
        this.upvotes = new ArrayList<String>();
        this.downvotes = new ArrayList<String>();
        this.score = 0;
    }

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

    public ArrayList getDownvotes() {
        return downvotes;
    }

    public void addDownvote(String _id) {
        if (_id != null) {
            this.downvotes.add(_id);
        }
    }

    public void removeDownvote(String _id) {
        if (_id != null) {
            this.downvotes.removeAll(Collections.singleton(_id));
        }
    }

    public boolean hasDownvoted(String _id) {
        if (!this.downvotes.isEmpty() && this.downvotes.contains(_id)) return true;
        else return false;
    }

    public long getScore() {
        return score;
    }

    public ArrayList getUpvotes() {
        return upvotes;
    }

    public void addUpvote(String _id) {
        if (_id != null) {
            this.upvotes.add(_id);
        }
    }

    public void removeUpvote(String _id) {
        if (_id != null) {
            this.upvotes.removeAll(Collections.singleton(_id));
        }
    }

    public boolean hasUpvoted(String _id) {
        if (!this.upvotes.isEmpty() && this.upvotes.contains(_id)) return true;
        else return false;
    }

    @Override
    public String toString() {
        return "{" +
                "_id='" + _id + '\'' +
                ", userId='" + userId + '\'' +
                ", name='" + name + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                ", score=" + score +
                ", authToken='" + authToken + '\'' +
                ", userImageUrl='" + userImageUrl + '\'' +
                '}';
    }

    public void updateRetro(final Context context) {

        UserClient client = ServiceGenerator.createService(UserClient.class);
        Log.d("UPDATE USER", this.toString());
        Call<User> call = client.updateUser(this.userId, this);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccess()) {


                    /*

                    Gson gson = new Gson();
                    prefs = new PreferencesHelper(context);
                    String json = gson.toJson(this);
                    prefs.savePreferences("me", json);

                    */

                    Log.d("UPDATE SUCCESS", response.raw().toString());

                } else {
                    // error response, no access to resource?
                    Log.d("UPDATE ERROR", response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.toString());
            }
        });
    }
}
