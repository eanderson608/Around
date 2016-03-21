package com.cs407.around;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView fbInfo;
    private LoginButton fbLoginButton;
    private User me = new User();
    SharedPreferences mPrefs;
    private CallbackManager callbackManager;
    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getPreferences(MODE_PRIVATE);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        fbInfo = (TextView) findViewById(R.id.fb_info_textview);
        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);

        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                profile = Profile.getCurrentProfile();
                getUserRetro(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken());

            }

            @Override
            public void onCancel() {
                fbInfo.setText("login canceled");
            }

            @Override
            public void onError(FacebookException e) {
                fbInfo.setText("login error");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void getUserRetro(final String userId, final String authToken) {

        UserClient client = ServiceGenerator.createService(UserClient.class);
        Call<User> call = client.getUser(userId);
        call.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (response.isSuccess()) {
                    Log.d("HTTP_GET_RESPONSE", response.body().toString());

                    Gson gson = new GsonBuilder().create();
                    me = gson.fromJson(response.body().toString(), User.class);

                    // update fb info
                    me.setFirstName(profile.getFirstName());
                    me.setLastName(profile.getLastName());
                    me.setName(profile.getName());
                    me.setUserProfilePic(profile.getProfilePictureUri(300, 300).toString());
                    me.setAuthToken(authToken);

                    Log.d("getUserRetro", me.toString());
                    updateUserRetro(me);

                    // Save current user (me) to preferences
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    String json = gson.toJson(me);
                    prefsEditor.putString("me", json);
                    prefsEditor.commit();


                } else {
                    // error response, no access to resource?
                    Log.d("HTTP_GET_RESPONSE", response.raw().toString());

                    // if userId cannot be found, create new user
                    if (response.message().equals("Not Found")) {
                        User user = new User();
                        user.setUserId(userId);
                        createUserRetro(user);
                        getUserRetro(userId, authToken);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
            }
        });
    }

    private void createUserRetro(final User user) {

        UserClient client = ServiceGenerator.createService(UserClient.class);
        Call<ResponseBody> call = client.createUser(user);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccess()) {
                    Log.d("SUCCESS", response.raw().toString());

                } else {
                    // error response, no access to resource?
                    Log.d("ERROR", response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
            }
        });
    }

    private void updateUserRetro(User user) {

        UserClient client = ServiceGenerator.createService(UserClient.class);
        Log.d("START UPDATE USER", user.toString());
        Call<User> call = client.updateUser(user.getUserId(), user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccess()) {
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

    public void cameraButtonPressed(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
