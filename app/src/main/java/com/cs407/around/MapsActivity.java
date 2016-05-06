package com.cs407.around;

import android.Manifest;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Hashtable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    LatLngBounds.Builder builder;
    DisplayMetrics metrics;
    int screenHeight;
    int screenWidth;

    GoogleApiClient googleApiClient;
    Location lastLocation;

    private String path = "http://eanderson608.ddns.net/uploads/";
    ImageView imageView;

    Marker marker = null;
    private Hashtable<String, String> markers;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        builder = new LatLngBounds.Builder();
        imageView = new ImageView(MapsActivity.this);

        metrics = Resources.getSystem().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        lastLocation = new Location("location");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initImageLoader();
        markers = new Hashtable<String, String>();
        imageLoader = ImageLoader.getInstance();

        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.ic_photo)        //    Display Stub Image
                .showImageForEmptyUri(R.drawable.ic_photo)    //    If Empty image found
                .cacheInMemory()
                .cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        //check permission to see current location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        } else {
            mMap.setMyLocationEnabled(true);
        }

        //CURRENTLY HARD-CODED. SHOULD GET DEVICE'S LOCATION.

        lastLocation.setLongitude(-89.384497);
        lastLocation.setLatitude(43.074651);

//        try {
//            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//            Log.d("lastLocation", lastLocation.toString());
//        } catch (SecurityException e) {
//            Log.e("SecurityException", e.toString());
//        }

        loadPhotosOnMap(lastLocation);

    }

    private void loadPhotosOnMap(final Location lastLocation) {

        long maxDistance = 20037942; // no max distance, loads all photos
        final long time = System.currentTimeMillis() / 1000;  // current time
        final long offset = 7889238;  //3 months in millisec

        PhotoClient client = ServiceGenerator.createService(PhotoClient.class);
        Call<ArrayList<Photo>> call = client.getPhotos(lastLocation.getLongitude(), lastLocation.getLatitude(), "time", maxDistance);

        call.enqueue(new Callback<ArrayList<Photo>>() {
            @Override
            public void onResponse(Call<ArrayList<Photo>> call, Response<ArrayList<Photo>> response) {
                if (response.isSuccess()) {
                    Log.d("SUCCESS", response.raw().toString());

                    for (Photo e : response.body()) {
                        Log.d("PHOTOS", e.toString());
                        LatLng loc = new LatLng(e.getLocation()[1], e.getLocation()[0]);

                        //only include image in marker if it is within 3 months
                        if ( (e.getTime() / 1000) > (time - offset) ) {

                            Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(loc)
                                    .title(e.getUserName())
                                    .snippet("Photo score: " + e.getScore()));
                            markers.put(marker.getId(), path + e.getFileName());

                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                                @Override
                                public void onInfoWindowClick(Marker marker) {
                                    loadImage(marker);
                                }
                            });
                            //builder.include(loc);

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 13));  //adjust camera based on zoom and location

                            //mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 50));  //adjust camera based on markers
                        }
                    }

                } else {
                    // error response
                    Log.d("ERROR", response.raw().toString());
                    alertError();
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Photo>> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
                alertError();
            }
        });
    }

    /**
     * alert error when image can't load
     */
    private void alertError() {
        new AlertDialog.Builder(MapsActivity.this)
                .setMessage("Unable to load photos.")
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /**
     * load image into alert dialog to display it larger.
     * @param marker
     */
    public void loadImage(Marker marker) {
        String url = markers.get(marker.getId());

        ImageView img = new ImageView(this);

        AlertDialog.Builder builder =
                new AlertDialog.Builder(this).
                        setMessage("").
                        setPositiveButton("DONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).
                        setView(img);
        builder.create().show();

        Picasso.with(MapsActivity.this).load(url)
                //.rotate(90)
                .resize(screenWidth, screenHeight)
                .centerInside()
                .error(R.drawable.error)
                .placeholder(R.drawable.grey_placeholder)
                .into(img);

        Log.d("loadImage", url);

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d("", marker.getTitle());
    }

    /**
     * for exiting Explore Fragment Activity
     * @param view
     */
    public void backClicked(View view) {
        finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /**
     * Custom class for showing info window.
     */
    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private View view;

        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.custom_info_window,
                    null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            MapsActivity.this.marker = marker;

            String url = null;

            if (marker.getId() != null && markers != null && markers.size() > 0) {
                if ( markers.get(marker.getId()) != null &&
                        markers.get(marker.getId()) != null) {
                    url = markers.get(marker.getId());
                }
            }
            final ImageView image = ((ImageView) view.findViewById(R.id.badge));

            if (url != null && !url.equalsIgnoreCase("null")
                    && !url.equalsIgnoreCase("")) {
                imageLoader.displayImage(url, image, options,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri,
                                                          View view, Bitmap loadedImage) {
                                super.onLoadingComplete(imageUri, view, loadedImage);
                                //view.setRotation(90);
                                getInfoContents(MapsActivity.this.marker);
                            }
                        });
            } else {
                image.setImageResource(R.drawable.ic_photo);
            }

            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }

            final String snippet = marker.getSnippet();
            final TextView snippetUi = ((TextView) view
                    .findViewById(R.id.snippet));
            if (snippet != null) {
                snippetUi.setText(snippet);
            } else {
                snippetUi.setText("");
            }

            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {
            if (MapsActivity.this.marker != null
                    && MapsActivity.this.marker.isInfoWindowShown()) {
                MapsActivity.this.marker.hideInfoWindow();
                MapsActivity.this.marker.showInfoWindow();
            }
            return null;
        }
    }

    /**
     * init image loader
     */
    private void initImageLoader() {
        int memoryCacheSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            int memClass = ((ActivityManager)
                    getSystemService(Context.ACTIVITY_SERVICE))
                    .getMemoryClass();
            memoryCacheSize = (memClass / 8) * 1024 * 1024;
        } else {
            memoryCacheSize = 2 * 1024 * 1024;
        }

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(memoryCacheSize)
                .memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize-1000000))
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();

        ImageLoader.getInstance().init(config);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_explore_activity, menu);
        return true;
    }

}
