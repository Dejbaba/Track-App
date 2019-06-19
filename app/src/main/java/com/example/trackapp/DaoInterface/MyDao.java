package com.example.trackapp.DaoInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.trackapp.Model.Coordinate;

import java.util.List;

@Dao
public interface MyDao {

    @Insert
    void addCoordinate(Coordinate coordinate);

    @Update
    void updateCoordinate(Coordinate coordinate);

    @Query("select * from coordinates")
    List<Coordinate> getCoordinates();
}
