package com.cs407.around;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoReviewActivity extends AppCompatActivity {

    ImageView imageView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review);

        button = (Button) findViewById(R.id.save_photo_button);

        // retrieve photo and place in imageView
        File file = new File(this.getFilesDir() + "/temp_photo");
        final Uri uri = Uri.fromFile(file);
        imageView = (ImageView) findViewById(R.id.photo_review_imageview);
        imageView.setImageURI(uri);


        // set behavior for save button
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                uploadPhoto(uri);
            }
        });

    }

    // Upload file to remote server
    private void uploadPhoto(Uri uri) {

        File file = new File(uri.getPath());
        PhotoClient service = ServiceGenerator.createService(PhotoClient.class);

        // create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // finally, execute the request
        Call<ResponseBody> call = service.upload(requestFile);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.v("Upload", response.raw().toString());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Upload error:", t.toString());
            }
        });
    }
}
