package com.cs407.around;

import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by patron on 3/20/16.
 */
public interface PhotoClient {

    @GET("/api/photos")
    Call<ArrayList<Photo>> getPhotos();

    @POST("/api/photos/")
    Call<ResponseBody> createPhoto(@Body Photo photo);

    @Multipart
    @POST("/api/photos/uploads")
    Call<ResponseBody> upload(@Part("file\"; filename=\"temp_photo\" ") RequestBody file,
                              @Part("fileName") RequestBody fileName);
}
