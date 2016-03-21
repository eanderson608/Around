package com.cs407.around;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        surfaceView = (SurfaceView) findViewById(R.id.camera_surface_view);
        surfaceHolder = surfaceView.getHolder();

        surfaceHolder.addCallback((SurfaceHolder.Callback) this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        jpegCallback = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                double[] location = {longitude, latitude};

                // create file on phone where photo is stored temporarily
                File file = new File(getFilesDir(), "/temp_photo");

                try { // save the photo file
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
            camera = Camera.open();
        }

        catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        // set image format and rotate camera
        Camera.Parameters param;
        param = camera.getParameters();
        param.setPictureFormat(ImageFormat.JPEG);
        param.setRotation(90);
        camera.setDisplayOrientation(90);
        camera.setParameters(param);

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }

        catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
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
