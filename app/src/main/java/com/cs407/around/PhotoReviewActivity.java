package com.cs407.around;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
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
    Button closeButton;
    Button textButton;
    Button paintButton;
    Photo photo;
    User me;
    PreferencesHelper prefs;
    Bitmap bitmap;
    Bitmap textBitmap;
    Bitmap combined;
    Canvas canvas;
    RelativeLayout layout;
    EditText textBox;
    boolean alreadyTextBox;

    //boolean paintMode;

    //DrawingView drawView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_review);

        button = (Button) findViewById(R.id.save_photo_button);
        closeButton = (Button) findViewById(R.id.close_button);
        textButton = (Button) findViewById(R.id.text_button);
        paintButton = (Button) findViewById(R.id.paint_button);
        layout = (RelativeLayout) findViewById(R.id.relativeLayout);
        alreadyTextBox = false;
        //paintMode = false;
        EditText editText = new EditText(this);
        prefs = new PreferencesHelper(this);

        // retrieve photo and place in imageView
        File file = new File(this.getFilesDir() + "/temp_photo");
        BitmapFactory.Options bitFacOptions = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), bitFacOptions);
        bitmap = bitmap.copy(bitmap.getConfig(), true);

        imageView = (ImageView) findViewById(R.id.photo_review_imageview);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

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
        me = gson.fromJson(json, User.class);

        //Create Photo object
        photo = new Photo(me.getUserId());
        photo.setUserName(me.getName());
        photo.setLocation(location);
        Log.d("PHOTO CREATED", photo.toString());

        imageView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (alreadyTextBox)
                    hideSoftKeyboard();
                else {
                    textBox = makeTextBox();
                    layout.addView(textBox);
                    layout.removeView(textButton);
                    alreadyTextBox = true;
                    textBox.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(textBox, InputMethodManager.SHOW_IMPLICIT);
                }

            }
        });

        /*imageView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (!paintMode)
                    return false;
                else {
                    float touchX = event.getX();
                    float touchY = event.getY();
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            drawPath.moveTo(touchX, touchY);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            drawPath.lineTo(touchX, touchY);
                            break;
                        case MotionEvent.ACTION_UP:
                            paintCanvas.drawPath(drawPath, paint);
                            drawPath.reset();
                            break;
                        default:
                            return false;
                    }
                    imageView.invalidate();
                    return true;
                }
            }
        });*/

        //set behavior for close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //go back to camera
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
        });

        // set behavior for save button
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // turn off button so you cant upload twice
                button.setClickable(false);
                if (alreadyTextBox) {
                    textBox.setCursorVisible(false);
                    //textBox.setRotation(270);
                    textBox.buildDrawingCache();
                    //Matrix matrix = new Matrix();
                    //matrix.postRotate(270);
                    textBitmap = Bitmap.createBitmap(textBox.getDrawingCache());
                    bitmap = addTextToPhoto(bitmap, textBitmap);
                }
                uploadPhoto(photo);
            }
        });

        textButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (alreadyTextBox)
                    return;
                else {
                    textBox = makeTextBox();
                    layout.addView(textBox);
                    layout.removeView(textButton);
                    alreadyTextBox = true;
                    textBox.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(textBox, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        paintButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //drawView = makeDrawingView();
                //drawView.makeBitmap(bitmap);
               /* if (!paintMode) {
                    drawPath = new Path();
                    paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setAntiAlias(true);
                    paint.setStrokeWidth(20);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeJoin(Paint.Join.ROUND);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    canvasPaint = new Paint(Paint.DITHER_FLAG);
                    paintBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    paintCanvas = new Canvas(paintBitmap);
                    paintCanvas.drawBitmap(paintBitmap, 0, 0, canvasPaint);
                    paintCanvas.drawPath(drawPath, paint);
                    paintMode = true;
                }*/
            }
        });

    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)  this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }

    private EditText makeTextBox() {
        EditText editText = new EditText(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, paintButton.getId());
        editText.setLayoutParams(params);
        editText.setTextColor(Color.WHITE);
        editText.setBackgroundColor(Color.TRANSPARENT);
        return editText;
    }

    // Upload photo object and file to remote server
    private void uploadPhoto(final Photo photo) {

        // Assemble request to upload photo FILE
        File file = new File(this.getFilesDir() + "/temp_photo");
        if (alreadyTextBox) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                combined.compress(Bitmap.CompressFormat.PNG, 85, fos);
                fos.flush();
                fos.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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

    public Bitmap addTextToPhoto(Bitmap bitmap1, Bitmap bitmap2) {
        //Matrix matrix = new Matrix();
        //matrix.setRotate(90, bitmap2.getWidth()/2, bitmap2.getHeight()/2);
        //Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, true);
        //Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap2.getWidth(), bitmap2.getHeight(), bitmap2.getConfig());
        combined = Bitmap.createBitmap(bitmap1.getWidth(), bitmap1.getHeight(), bitmap1.getConfig());
        canvas = new Canvas(combined);
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        canvas.drawBitmap(bitmap2, 0, 0, new Paint());

        /*Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, config);
        Canvas canvas = new Canvas(targetBitmap);
        Matrix matrix = new Matrix();
        matrix.setRotate(mRotation,source.getWidth()/2,source.getHeight()/2);
        canvas.drawBitmap(source, matrix, new Paint());*/


        return combined;
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
