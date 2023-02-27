package com.example.exampleserver.server;

import com.example.exampleserver.database.LogItem;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("send.php")
    Single<ServerResponse> sendLogs(@Body List<LogItem> items);
}

