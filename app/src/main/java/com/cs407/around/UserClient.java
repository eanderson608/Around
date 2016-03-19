package com.cs407.around;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;


public interface UserClient {

    @GET("/api/users/{userId}")
    Call<User> getEvents(@Path("userId") String userId);
}
