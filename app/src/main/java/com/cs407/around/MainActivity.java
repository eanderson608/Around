package com.cs407.around;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private TextView fbInfo;
    private LoginButton fbLoginButton;
    private User me;
    PreferencesHelper prefs;
    private CallbackManager callbackManager;
    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d("HASH", printKeyHash(this));
        prefs = new PreferencesHelper(getApplicationContext());

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
                Log.d("FACEBOOK ONSUCCESS", "WHAT");

                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                startActivity(intent);
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

        // attempt to retrieve current user
        String json = prefs.getPreferences("me");
        Log.d("JSON ME MAIN", json);
        Gson gson = new Gson();
        me = gson.fromJson(json, User.class);

        if (isLoggedIn()) {
            Intent intent = new Intent(this, FeedActivity.class);
            startActivity(intent);
        }

        /*

        // uncomment  below to load camera immediately if user is already logged in

        try { // attempt to load user profile from prefs
            if (me.getUserId() != null) {
                Intent intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            // continue, wait for user to press log in button
        }
        */
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
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
                    profile = Profile.getCurrentProfile();

                    // update fb info
                    me.setFirstName(profile.getFirstName());
                    me.setLastName(profile.getLastName());
                    me.setName(profile.getName());
                    me.setUserProfilePic(profile.getProfilePictureUri(350, 350).toString());
                    me.setAuthToken(authToken);

                    // upload profile picture to remote server @ /uploads/{userId}.jpg
                    uploadProfilePhoto(profile.getProfilePictureUri(350, 350), me.getUserId());

                    Log.d("me.updateRetro()", me.toString());
                    me.updateRetro(getApplicationContext());

                    // Save current user (me) to preferences
                    gson = new Gson();
                    String json = gson.toJson(me);
                    Log.d("SAVE PREFS", json);
                    prefs.savePreferences("me", json);
                    prefs.savePreferences("camera", "0");



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

                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                startActivity(intent);


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
                    Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                    startActivity(intent);

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

    public void cameraButtonPressed(View view) {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    public void feedButtonPressed(View view) {
        Intent intent = new Intent(this, FeedActivity.class);
        startActivity(intent);
    }

    public void exploreButtonPressed(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    // Upload users profile to remote server to location /uploads/{userId}.jpg
    private void uploadProfilePhoto(Uri uri, final String userId) {

        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ImageLoaderConfiguration.createDefault(this));

        // Load image, decode it to Bitmap and return Bitmap to callback
        imageLoader.loadImage(uri.toString(), new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                // Save bitmap to temp file
                File file = new File(getFilesDir(), "/temp_photo");
                FileOutputStream os;
                try {
                    os = new FileOutputStream(file);
                    loadedImage.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Assemble request to upload photo
                PhotoClient service = ServiceGenerator.createService(PhotoClient.class);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                RequestBody fileName = RequestBody.create(MediaType.parse("multipart/form-data"), userId + ".jpg");

                // finally, execute the request
                Call<ResponseBody> call = service.upload(requestFile, fileName);

                // Upload photo
                call.enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.v("Upload", response.raw().toString());

                        // profile photo upload successful
                        if (response.code() == 200) {

                        } else {
                            Log.e("file upload ERROR", response.raw().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("Upload error:", t.toString());
                    }
                });
            }
        });
    }

    // get key hash
    // copied from: http://javatechig.com/android/how-to-get-key-hashes-for-android-facebook-app
    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        }
        catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

}
