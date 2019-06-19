package com.example.trackapp.Database;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.trackapp.DaoInterface.MyDao;
import com.example.trackapp.Model.Coordinate;

@Database (entities = {Coordinate.class}, version = 1, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {

    public abstract MyDao myDao();

    //private static MyDatabase instance;


  /* public static MyDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MyDatabase.class, "my_database")
                    .build();

        }
        return instance;
    }*/
}
