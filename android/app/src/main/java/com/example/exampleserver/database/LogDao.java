package com.example.exampleserver.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface LogDao {


    @Query("SELECT * FROM logs")
    Single<List<LogItem>> getLogs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertLogs(List<LogItem> items);

    @Query("SELECT COUNT(*) FROM logs")
    Single<Integer> checkIfDataExists();
}
