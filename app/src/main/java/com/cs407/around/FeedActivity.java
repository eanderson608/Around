package com.cs407.around;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


    RecyclerView recyclerView;
    private ArrayList<Photo> photoArrayList;
    private CustomPhotoFeedAdapter adapter;
    private Button hotButton;
    private Button newButton;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    User me;
    PreferencesHelper prefs;
    LinearLayoutManager layoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String sort;
    private long radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        radius = 16093;

        prefs = new PreferencesHelper(getApplicationContext());

        // load order feed was last sorted in from preferences
        try {
            sort = prefs.getPreferences("sort");
        } catch (Exception e) {
            sort = "time";
        }

        hotButton = (Button) findViewById(R.id.sort_by_hot_button);
        newButton = (Button) findViewById(R.id.sort_by_new_button);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        // set up Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        // Register Google API Client to be used for Location Services
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //Retrieve current user from preferences file
        Gson gson = new Gson();
        String json = prefs.getPreferences("me");
        Log.d("JSON ME", json);
        me = gson.fromJson(json, User.class);

        photoArrayList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(48));

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        /*
         *  Adapter is created and set in the OnConnected method for Google Location services
         *  below.  This is to make sure we find the current location before calculating distances
         *  to photos in the feed.
         *
         *  Uncomment the section below to use without Google Location Services.
         *  will probabably need to do this if the feed is not working since the emulators
         *  do not support location services.
         *
         */

        lastLocation = new Location("location");
        lastLocation.setLongitude(-89.3864085);
        lastLocation.setLatitude(43.0780441);
        adapter = new CustomPhotoFeedAdapter(this, photoArrayList, lastLocation);
        recyclerView.setAdapter(adapter);

        // get new photos
        getPhotosRetro(sort, radius);
        if (sort.equals("score")) {
            hotButton.setTextColor(getResources().getColor(R.color.colorBlack));
            hotButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            newButton.setTextColor(getResources().getColor(R.color.colorDeselected));
            newButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        } else {
            newButton.setTextColor(getResources().getColor(R.color.colorBlack));
            newButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            hotButton.setTextColor(getResources().getColor(R.color.colorDeselected));
            hotButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        }

        // sort photos by most upvotes
        hotButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                photoArrayList.clear();
                prefs.savePreferences("sort", "score");
                getPhotosRetro("score", radius);
                hotButton.setTextColor(getResources().getColor(R.color.colorBlack));
                hotButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                newButton.setTextColor(getResources().getColor(R.color.colorDeselected));
                newButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                layoutManager.scrollToPosition(0);
            }
        });

        // sort photos by newest
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                photoArrayList.clear();
                prefs.savePreferences("sort", "time");
                getPhotosRetro("time", radius);
                newButton.setTextColor(getResources().getColor(R.color.colorBlack));
                newButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                hotButton.setTextColor(getResources().getColor(R.color.colorDeselected));
                hotButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                layoutManager.scrollToPosition(0);
            }
        });

        // set behavior for swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                photoArrayList.clear();
                sort = prefs.getPreferences("sort");
                getPhotosRetro(sort, radius);
                if (sort.equals("score")) {
                    hotButton.setTextColor(getResources().getColor(R.color.colorBlack));
                    hotButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    newButton.setTextColor(getResources().getColor(R.color.colorDeselected));
                    newButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                } else {
                    newButton.setTextColor(getResources().getColor(R.color.colorBlack));
                    newButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    hotButton.setTextColor(getResources().getColor(R.color.colorDeselected));
                    hotButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                }
                layoutManager.scrollToPosition(0);
            }
        });

    }

    // get photos using Retrofit2 ORM, takes one parameter sortOn, which is either "time" or "score" depending
    // if you are sorting feed by 'new' or 'hot', results will be sorted in descending order for each case and
    // that code is handled in server side.
    private void getPhotosRetro(String sortOn, long maxDistance) {

        maxDistance = 16093; // 10 miles in meters, radius to search for photos in

        PhotoClient client = ServiceGenerator.createService(PhotoClient.class);
        Call<ArrayList<Photo>> call = client.getPhotos(lastLocation.getLongitude(), lastLocation.getLatitude(), sortOn, maxDistance);

        call.enqueue(new Callback<ArrayList<Photo>>() {
            @Override
            public void onResponse(Call<ArrayList<Photo>> call, Response<ArrayList<Photo>> response) {
                if (response.isSuccess()) {
                    Log.d("SUCCESS", response.raw().toString());

                    Gson gson = new GsonBuilder().create();

                    for (Photo e : response.body()) {
                        Log.d("PHOTOS", e.toString());
                        photoArrayList.add(gson.fromJson(e.toString(), Photo.class));
                    }

                    swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();


                } else {
                    // error response, no access to resource?
                    Log.d("ERROR", response.raw().toString());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<Photo>> call, Throwable t) {
                // something went completely south (like no internet connection)
                Log.d("Error", t.getMessage());
            }
        });
    }

    @Override // needed for Google Location Services
    public void onConnected(Bundle bundle) {

        lastLocation = new Location("location");
        lastLocation.setLongitude(-89.3864085);
        lastLocation.setLatitude(43.0780441);

        try {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            Log.d("lastLocation", lastLocation.toString());
        } catch (SecurityException e) {
            Log.e("SecurityException", e.toString());
        }

        // only set adapter and get photos after current location has been recorded
        adapter = new CustomPhotoFeedAdapter(this, photoArrayList, lastLocation);
        recyclerView.setAdapter(adapter);

        // get new photos
        getPhotosRetro(sort, radius);
        if (sort.equals("score")) {
            hotButton.setTextColor(getResources().getColor(R.color.colorBlack));
            hotButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            newButton.setTextColor(getResources().getColor(R.color.colorDeselected));
            newButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        } else {
            newButton.setTextColor(getResources().getColor(R.color.colorBlack));
            newButton.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            hotButton.setTextColor(getResources().getColor(R.color.colorDeselected));
            hotButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        }

    }

    @Override // needed for Google Location Services
    public void onConnectionSuspended(int i) {

    }

    @Override // needed for Google Location Services
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

    @Override
    protected void onResume() {
        googleApiClient.connect();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_feed_activity, menu);
        return true;
    }

    @Override // handle menu item button presses
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {

            // Open Camera
            case R.id.action_camera:
                intent = new Intent(this, CameraActivity.class);
                startActivity(intent);
                break;

            // Open Explore
            case R.id.action_map:
                intent = new Intent(this, MapsActivity.class);
                startActivity(intent);
                break;

            // Open Me
            case R.id.action_me:
                intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
        return true;
    }

}
