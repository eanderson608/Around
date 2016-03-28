package com.cs407.around;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface UserClient {

    @GET("/api/users/{userId}")
    Call<User> getUser(@Path("userId") String userId);

    @POST("/api/users/")
    Call<ResponseBody> createUser(@Body User user);

    @PUT("/api/users/{userId}")
    Call<User> updateUser(@Path("userId") String userId, @Body User user);

    @PUT("/api/users/{userId}/increment")
    Call<ResponseBody> incrementUserScore(@Path("userId") String userId, @Query("amount") int amount);
}
