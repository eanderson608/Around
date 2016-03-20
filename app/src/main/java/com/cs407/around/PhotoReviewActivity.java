package com.cs407.around;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class PhotoReviewActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review);

        // retrieve photo
        imageView = (ImageView) findViewById(R.id.photo_review_imageview);
        Bitmap bmp = BitmapFactory.decodeFile(this.getFilesDir() + "/photo.jpg");
        imageView.setImageBitmap(bmp);

    }
}
