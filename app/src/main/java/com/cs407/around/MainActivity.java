package com.cs407.around;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
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
    private static User me;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_main);
        fbInfo = (TextView) findViewById(R.id.fb_info_textview);
        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);

        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                fbInfo.setText("User ID: " + loginResult.getAccessToken().getUserId() + "\n" +
                                "Auth Token: " + loginResult.getAccessToken().getToken() + "\n" +
                                "loginResult.toString(): " + loginResult.toString());

                getUserRetro(loginResult.getAccessToken().getUserId());

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

    static public void getUserRetro(final String userId) {

        UserClient client = ServiceGenerator.createService(UserClient.class);
        Call<User> call = client.getUser(userId);

        call.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {

                if (response.isSuccess()) {
                    Log.d("HTTP_GET_RESPONSE", response.body().toString());

                    Gson gson = new GsonBuilder().create();
                    me = gson.fromJson(response.body().toString(), User.class);


                } else {
                    // error response, no access to resource?
                    Log.d("HTTP_GET_RESPONSE", response.raw().toString());

                    if (response.message().equals("Not Found")) {
                        User user = new User();
                        user.setUserId(userId);
                        createUserRetro(user);

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

    private static void createUserRetro(final User user) {

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
}
