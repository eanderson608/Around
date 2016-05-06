package com.cs407.around;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    PreferencesHelper prefs;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review);

        button = (Button) findViewById(R.id.save_photo_button);
        prefs = new PreferencesHelper(this);

        // retrieve photo and place in imageView
        File file = new File(this.getFilesDir() + "/temp_photo");
        BitmapFactory.Options bitFacOptions = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bitFacOptions);
        bitmap = bitmap.copy(bitmap.getConfig(), true);

        imageView = (ImageView) findViewById(R.id.photo_review_imageview);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        String camera = prefs.getPreferences("camera");
        if (camera.equals("1")) {
            bitmap = rotateBitmap(bitmap, (float) -90);
        } else {
            bitmap = rotateBitmap(bitmap, (float) 90);
        }

        imageView.setImageBitmap(bitmap);







        // save image back to temp storage
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(this.getFilesDir() + "/temp_photo");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }



        // retrieve photo location from intent
        double[] location = getIntent().getDoubleArrayExtra("location");

        //Retrieve current user
        Gson gson = new Gson();
        String json = prefs.getPreferences("me");
        Log.d("JSON ME", json);
        me = gson.fromJson(json, User.class);

        //Create Photo object
        photo = new Photo(me.getUserId());
        photo.setUserName(me.getName());
        photo.setLocation(location);
        Log.d("PHOTO CREATED", photo.toString());

        // set behavior for save button
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // turn off button so you cant upload twice
                button.setClickable(false);
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

                    // Goto feed
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

    // rotate a bitmap in order to save image as proper orientation
    // copied from http://stackoverflow.com/questions/9015372/how-to-rotate-a-bitmap-90-degrees
    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
