package com.example.trackapp.Model;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "coordinates")
public class Coordinate {

    @PrimaryKey
    private int id;

    @ColumnInfo (name = "coordinate_position")
    private String position;

    @ColumnInfo (name = "coordinate_longitude")
    private double longitude;

    @ColumnInfo (name = "coordinate_latitude")
    private double latitude;

    public Coordinate(int id, String position, double longitude, double latitude) {
        this.id = id;
        this.position = position;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
