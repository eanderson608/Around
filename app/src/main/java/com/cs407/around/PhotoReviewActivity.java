package com.cs407.around;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoReviewActivity extends AppCompatActivity {

    // file where preferences are stored
    public static final String PREFS_NAME = "AROUND_PREFS";

    ImageView imageView;
    Button button;
    Photo photo;
    User me;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review);

        button = (Button) findViewById(R.id.save_photo_button);

        // retrieve photo and place in imageView
        final Uri uri = Uri.fromFile(new File(this.getFilesDir() + "/temp_photo"));
        imageView = (ImageView) findViewById(R.id.photo_review_imageview);
        imageView.setImageURI(uri);

        // retrieve photo location from intent
        double[] location = getIntent().getDoubleArrayExtra("location");

        //Retrieve current user
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("me", "");
        me = gson.fromJson(json, User.class);

        //Create Photo object
        photo = new Photo(me.getUserId());
        photo.setLocation(location);
        Log.d("PHOTO CREATED", photo.toString());

        // set behavior for save button
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                uploadPhoto(photo);
            }
        });

    }

    // Upload photo object and file to remote server
    private void uploadPhoto(final Photo photo) {

        // Assemble request to upload photo FILE
        File file = new File(this.getFilesDir() + "/temp_photo");
        PhotoClient service = ServiceGenerator.createService(PhotoClient.class);
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        RequestBody fileName = RequestBody.create(MediaType.parse("multipart/form-data"), photo.getFileName());
        Log.v("PHOTO FILENAME", photo.getFileName());

        // finally, execute the request
        Call<ResponseBody> call = service.upload(requestFile, fileName);

        // Upload photo FILE
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.v("Upload", response.raw().toString());

                // FILE upload was successful, upload photo OBJECT
                if (response.code() == 200) {

                    createPhotoRetro(photo);

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

    private void createPhotoRetro(final Photo photo) {

        PhotoClient client = ServiceGenerator.createService(PhotoClient.class);
        Call<ResponseBody> call = client.createPhoto(photo);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccess()) {
                    Log.d("SUCCESS", response.raw().toString());
                    Log.d("PHOTO UPLOADED", photo.toString());

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
