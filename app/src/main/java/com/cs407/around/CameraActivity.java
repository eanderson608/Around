package com.cs407.around;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.location.Location;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;


public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //TODO add camera2 logic so camera will work with APIs 22 plus

    // file where preferences are stored
    public static final String PREFS_NAME = "AROUND_PREFS";

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    GoogleApiClient googleApiClient;
    Camera.PictureCallback jpegCallback;
    Location lastLocation;
    double latitude;
    double longitude;
    boolean isPreview;
    private ImageButton closeButton;
    private ImageButton switchCameraButton;
    private int cameraToOpen;
    private PreferencesHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // load camera to open from prefs
        prefs = new PreferencesHelper(getApplicationContext());
        try {
            cameraToOpen = Integer.valueOf(prefs.getPreferences("camera"));
        } catch (Exception e) {
            cameraToOpen = 0;
        }
        Log.d("CAMERA TO OPEN", Integer.toString(cameraToOpen));

        isPreview = false;
        surfaceView = (SurfaceView) findViewById(R.id.camera_surface_view);
        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback((SurfaceHolder.Callback) this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);



        jpegCallback = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                // if front-facing-camera rotate image
                if (cameraToOpen == 1) {
                    Bitmap photo = BitmapFactory.decodeByteArray(data,0,data.length);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    photo = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] data2 = stream.toByteArray();
                    data = data2;
                }

                // Get last location
                try {
                    lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                } catch (SecurityException e) {
                    Log.e("SecurityException", e.toString());
                }
                if (lastLocation != null) {
                    longitude = lastLocation.getLongitude();
                    latitude = lastLocation.getLatitude();
                }

                double[] location = {longitude, latitude};
                Location l = new Location("here");
                l.setLatitude(latitude);
                l.setLongitude(longitude);
                Log.d("LOCATION l", l.toString());

                // create file on phone where photo is stored temporarily
                File file = new File(getFilesDir(), "/temp_photo");

                try { // save the photo to temp file
                    FileOutputStream outputStream = new FileOutputStream(file.getPath());
                    outputStream.write(data);
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }

                // Create intent to open PhotoReviewActivity with location as an extra
                Intent intent = new Intent(getApplicationContext(), PhotoReviewActivity.class);
                intent.putExtra("location", location);
                startActivity(intent);
            }
        };

        // handle behavior for the close capture button
        closeButton = (ImageButton) findViewById(R.id.close_capture_image_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Goto feed
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                startActivity(intent);
            }
        });

        // handle behavior for the switch camera button
        switchCameraButton = (ImageButton) findViewById(R.id.switch_camera_image_button);
        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // save opposite camera to preferences
                if (cameraToOpen == 0) {
                    prefs.savePreferences("camera", "1");
                } else {
                    prefs.savePreferences("camera", "0");
                }

                // Reopen camera
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
            }
        });

        // Register Google API Client to be used for Location Services
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }

    public void captureImage(View v) throws IOException {
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        }

        catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }
        catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open(cameraToOpen);
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.d("WIDTH", Integer.toString(width));
        Log.d("HEIGHT", Integer.toString(height));

        if (camera == null) {
            camera.open(cameraToOpen);
        }
        Camera.Parameters params = camera.getParameters();
        Camera.Size myBestSize = getBestPreviewSize(width, height, params);

        if(myBestSize != null){

            // set params if camera to open is rear-facing
            if (cameraToOpen == 0) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                params.setPreviewSize(myBestSize.width, myBestSize.height);
                params.setPictureSize(myBestSize.width, myBestSize.height);
            }

            camera.setParameters(params);
            camera.setDisplayOrientation(90);
            camera.startPreview();
            isPreview = true;

            Log.d("Best Size:",String.valueOf(myBestSize.width) + " : " + String.valueOf(myBestSize.height));
        }
        refreshCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        camera.stopPreview();
        camera.release();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    // get best supported camera previw size,
    // taken from http://android-er.blogspot.com/2012/08/determine-best-camera-preview-size.html
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters){
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for(int i = 1; i < sizeList.size(); i++){
            if((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)){
                bestSize = sizeList.get(i);
            }
        }
        return bestSize;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException e) {
            Log.e("SecurityException", e.toString());
        }
        if (lastLocation != null) {
            longitude = lastLocation.getLongitude();
            latitude = lastLocation.getLatitude();
        }
    }

    @Override // Needed for Google Location Services
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override // Needed for Google Location Services
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
