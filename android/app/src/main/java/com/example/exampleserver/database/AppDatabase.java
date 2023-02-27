package com.example.exampleserver.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Create a Room database class that extends RoomDatabase and includes a singleton instance and methods to access the DAO:
@Database(entities = {LogItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract LogDao logDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "mydatabase.db")
                    .build();
        }
        return INSTANCE;
    }
}
