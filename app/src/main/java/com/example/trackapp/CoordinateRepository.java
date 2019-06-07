package com.example.trackapp;

import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;


/**
 * This class helps to fetch from and send data to the data source(RoomDb)
 */
public class CoordinateRepository {

    private MyDao myDao;
    private Context context;
    public static MyDatabase myDatabase;
    private List<Coordinate> coordinates;


    public CoordinateRepository(Application application){
        MyDatabase database = MyDatabase.getInstance(application);
        myDao = database.myDao();
        coordinates = myDao.getCoordinates();
        //this.context = context;
        //myDatabase = Room.databaseBuilder(context, MyDatabase.class, "my_database").build();
       // myDao = myDatabase.myDao();
    }

    public void addCoordinate(Coordinate coordinate){
        new addCoordinateAsyncTask(myDao).execute(coordinate);
    }


    public List<Coordinate> getAllCoordinates(){
        return coordinates;
    }

    private static class addCoordinateAsyncTask extends AsyncTask<Coordinate, Void, Void>{

        private MyDao myDao;

        private addCoordinateAsyncTask(MyDao myDao){
            this.myDao = myDao;
        }

        @Override
        protected Void doInBackground(Coordinate... coordinates) {
            myDao.addCoordinate(coordinates[0]);
            return null;
        }
    }




}
