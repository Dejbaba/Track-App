package com.example.trackapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MyDao {

    @Insert
    public void addCoordinate(Coordinate coordinate);

    @Query("select * from coordinates")
    public List<Coordinate> getCoordinates();
}
