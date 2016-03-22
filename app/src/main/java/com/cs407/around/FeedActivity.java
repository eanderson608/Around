package com.cs407.around;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    private ArrayList<Photo> photoArrayList;
    private CustomPhotoFeedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        photoArrayList = new ArrayList<Photo>();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CustomPhotoFeedAdapter(this, photoArrayList);
        recyclerView.setAdapter(adapter);

        getPhotosRetro();

    }

    private void getPhotosRetro() {

        PhotoClient client = ServiceGenerator.createService(PhotoClient.class);
        Call<ArrayList<Photo>> call = client.getPhotos();

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
}
