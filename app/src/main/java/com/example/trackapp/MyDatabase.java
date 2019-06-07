package com.example.trackapp;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database (entities = {Coordinate.class}, version = 1)
public abstract class MyDatabase extends RoomDatabase {

    public abstract MyDao myDao();

    private static MyDatabase instance;


   public static MyDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MyDatabase.class, "my_database")
                    .build();

        }
        return instance;
    }
}
