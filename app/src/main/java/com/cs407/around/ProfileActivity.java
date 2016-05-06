package com.cs407.around;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    ImageView userPhoto;
    TextView userName;
    LoginButton fbLoginButton;
    AccessTokenTracker accessTokenTracker;
    PreferencesHelper prefs;
    User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userPhoto = (ImageView) findViewById(R.id.user_photo);
        userName = (TextView) findViewById(R.id.user_name);

        // load me from preferences
        prefs = new PreferencesHelper(this);
        Gson gson = new Gson();
        String json = prefs.getPreferences("me");
        me = gson.fromJson(json, User.class);

        userName.setText(me.getName());

        // Download image with picasso
        Picasso.with(this).load(me.getUserProfilePic())
                .error(R.drawable.error)
                .placeholder(R.drawable.grey_placeholder)
                .into(userPhoto);

        fbLoginButton = (LoginButton) findViewById(R.id.fb_login_button);

        //returns to main activity on fb lougout
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    finish();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

    }

    /**
     * for exiting Profile Activity
     * @param view
     */
    public void backClicked(View view) {
        finish();
    }
}
