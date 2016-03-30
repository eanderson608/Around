package com.cs407.around;

import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by patron on 3/20/16.
 */
public interface PhotoClient {

    @GET("/api/photos")
    Call<ArrayList<Photo>> getPhotos(@Query("longitude") double longitude, @Query("latitude") double latitude,
                                     @Query("sortOn") String sortOn, @Query("maxDistance") long maxDistance);

    @POST("/api/photos/")
    Call<ResponseBody> createPhoto(@Body Photo photo);

    @PUT("/api/photos/{id}/increment")
    Call<ResponseBody> incrementPhotoVote(@Path("id") String id, @Query("field") String field, @Query("amount") int amount);

    @DELETE("/api/photos/{id}")
    Call<ResponseBody> deletePhoto(@Path("id") String id);

    @Multipart
    @POST("/api/photos/uploads")
    Call<ResponseBody> upload(@Part("file\"; filename=\"temp_photo\" ") RequestBody file,
                              @Part("fileName") RequestBody fileName);
}
