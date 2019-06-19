package com.example.trackapp.Activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import com.example.trackapp.Database.MyDatabase;
import com.example.trackapp.Model.Coordinate;
import com.example.trackapp.R;
import com.example.trackapp.Utils.GPSTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private Button startBtn, stopBtn, calcBtn;
    private TextView distTV;

    //current location
    double longitude, latitude;
    //Location of point A
    double longitudeA, latitudeA;
    //Location of point B
    double longitudeB, latitudeB;


    private GPSTracker gps;
    private String lati, longi, latiA, longiA, latiB, longiB;

    public static MyDatabase myDatabase;

    private boolean retrievedA = false;
    private boolean retrievedB = false;
    private boolean retrieved = false;


    private ArrayList<LatLng> listPoints;
    private Location ptA, ptB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        myDatabase = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, "my_database").allowMainThreadQueries().build();

        startBtn = findViewById(R.id.start);
        stopBtn = findViewById(R.id.stop);
        calcBtn = findViewById(R.id.calcDisc);
        distTV = findViewById(R.id.distanceTV);

        stopBtn.setEnabled(false);
        calcBtn.setEnabled(false);
        distTV.setVisibility(View.INVISIBLE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listPoints = new ArrayList<>();
        ptA = new Location("");
        ptB = new Location("");

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: start button clicked... attempting to save to DB");
                stopBtn.setEnabled(true);
                startBtn.setEnabled(false);
                calcBtn.setEnabled(false);
                distTV.setVisibility(View.VISIBLE);
                distTV.setText("Tracking Started...");
                getLocation();
                checkMap();
                setMarker(listPoints.get(0), "Point A");
                saveCoordinateToDb(1, "Point A", longitudeA, latitudeA);
                ptA.setLatitude(latitudeA);
                ptA.setLongitude(longitudeA);
                //retrieveCoordinatesFromDb();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: stop button clicked... attempting to save to DB");
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                calcBtn.setEnabled(true);
                distTV.setText("Tracking Stopped...");
                getLocation();
                listPoints.add(new LatLng(latitudeB, longitudeB));
                setMarker(listPoints.get(1), "Point B");
                saveCoordinateToDb(2, "Point B", longitudeB, latitudeB);
                ptB.setLatitude(latitudeB);
                ptB.setLongitude(longitudeB);
                //retrieveCoordinatesFromDb();
            }
        });

        calcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distTV.setVisibility(View.VISIBLE);
                distTV.setText("THE DISTANCE BETWEEN POINT A and POINT B is "+ new DecimalFormat("#.#")
                        .format((ptA.distanceTo(ptB)) / 1000)+" km");
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
            Log.d(TAG, "onMapReady: Map is Ready...");

            mMap = googleMap;

            if (retrieveCoordinatesFromDb()){

                setMarker(listPoints.get(0), "POINT A");
                setMarker(listPoints.get(1), "POINT B");
                distTV.setVisibility(View.VISIBLE);
                distTV.setText("THE DISTANCE BETWEEN POINT A and POINT B is "+ new DecimalFormat("#.#")
                        .format((ptA.distanceTo(ptB)) / 1000)+" km");
                calcBtn.setEnabled(true);

            }else {

                getLocation();

                // Add a marker to the device's current location and move the camera to that location
                LatLng myLocation = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(myLocation).title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10.2f));
            }




    }

    //method to get current location either through network or GPS
    public void getLocation(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)

                != PackageManager.PERMISSION_GRANTED

                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)

                != PackageManager.PERMISSION_GRANTED) {

                         ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        } else {

            Toast.makeText(this, "Location Permission granted", Toast.LENGTH_SHORT).show();
            gps = new GPSTracker(this, MapsActivity.this);

            // Check if network or gps providers are available
            if (gps.canGetLocation()) {

                if (startBtn.isEnabled() && !calcBtn.isEnabled()) {

                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();

                    Toast.makeText(getApplicationContext(),
                            "Your Current Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                    lati = Double.toString(latitude);
                    longi = Double.toString(longitude);
                    Log.d(TAG, "getLocation: your Current location is lati:" + lati + "and longi:" + longi);

                }else if (!startBtn.isEnabled()) {

                    latitudeA = gps.getLatitude();
                    longitudeA = gps.getLongitude();

                    Toast.makeText(getApplicationContext(),
                            "Your Start Location is - \nLat: " + latitudeA + "\nLong: " + longitudeA, Toast.LENGTH_LONG).show();

                    latiA = Double.toString(latitudeA);
                    longiA = Double.toString(longitudeA);
                    Log.d(TAG, "getLocation: your Start location is lati:" + latiA + "and longi:" + longiA);

                }
                else if (!stopBtn.isEnabled()){

                    latitudeB = gps.getLatitude();
                    longitudeB = gps.getLongitude();

                    Toast.makeText(getApplicationContext(),
                            "Your Stop Location is - \nLat: " + latitudeB + "\nLong: " + longitudeB, Toast.LENGTH_LONG).show();

                    latiB = Double.toString(latitudeB);
                    longiB = Double.toString(longitudeB);
                    Log.d(TAG, "getLocation: your Stop location is lati:" + latiB + "and longi:" + longiB);
                }
            } else {
                // Can't get location.
                // GPS or network is not enabled.
                // Ask user to enable GPS/network in settings.
                gps.showSettingsAlert();
            }
        }
    }

    /**
     * clear map if more than one marker exists and save new LatLng to the arraylist
     */
    private void checkMap(){


        Log.d(TAG, "checkMap: LISTPOINTS CLEARED");
        listPoints.clear();

        Log.d(TAG, "checkMap: MAP CLEARED");
        mMap.clear();

        Log.d(TAG, "checkMap: NEW COORD ADDED TO LISTPOINTS");
        listPoints.add(new LatLng(latitudeA, longitudeA));


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If request is cancelled, the result arrays are empty.
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted,
                gps = new GPSTracker(this, MapsActivity.this);

                // Check if any of the providers(gps and network) are available
                if (gps.canGetLocation()) {

                    if (startBtn.isEnabled() && !calcBtn.isEnabled()) {

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();

                        Toast.makeText(getApplicationContext(),
                                "Your Current Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

                        lati = Double.toString(latitude);
                        longi = Double.toString(longitude);
                        Log.d(TAG, "getLocation: your Current location is lati:" + lati + "and longi:" + longi);

                    }
                } else {

                    // Can't get location.
                    // GPS or network is not enabled.
                    // Ask user to enable GPS/network in settings.
                    gps.showSettingsAlert();
                }

            } else {
                // permission denied
                Toast.makeText(this, "You need to grant permission",
                        Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    /**
     * method used to save coordinates to the roomdb
     * @param id
     * @param position
     * @param longitude
     * @param latitude
     */
    private void saveCoordinateToDb(int id, String position, double longitude, double latitude){
        Log.d(TAG, "saveCoordinateToDb: Trying to save cood to DB");

        Coordinate coordinate = new Coordinate(id, position, longitude, latitude);
        if (retrieveCoordinatesFromDb()){
            Log.d(TAG, "saveCoordinateToDb: coord found in DB");
            myDatabase.myDao().updateCoordinate(coordinate);
        }else {
            Log.d(TAG, "saveCoordinateToDb: no cood in DB...");
            myDatabase.myDao().addCoordinate(coordinate);
        }
        Toast.makeText(MapsActivity.this, "Co-Ordinates saved to ROOM DB", Toast.LENGTH_LONG).show();
        Log.d(TAG, "saveCoordinateToDb: id: "+ id
            +"position: " + position
            +"longitude: " + longitude
            +"latitude: " + latitude);

    }

    /**
     * Method used to retrieve coordinates from RoomDb
     */
    private boolean retrieveCoordinatesFromDb(){

        List<Coordinate> coordinates = myDatabase.myDao().getCoordinates();

        for (Coordinate cood : coordinates){

            int id = cood.getId();
            String position = cood.getPosition();
            double longitude = cood.getLongitude();
            double latitude = cood.getLatitude();

            if (id == 1){
                listPoints.clear();
                listPoints.add(0, new LatLng(latitude, longitude));
                ptA.setLatitude(latitude);
                ptA.setLongitude(longitude);
                retrievedA = true;
                Log.d(TAG, "retrieveCoordinatesFromDb:, lat: "+latitude+" long: "+longitude+" for position "+position);
            }else if (id == 2){
                listPoints.add(1, new LatLng(latitude, longitude));
                ptB.setLatitude(latitude);
                ptB.setLongitude(longitude);
                retrievedB = true;
                Log.d(TAG, "retrieveCoordinatesFromDb:, lat: "+latitude+" long: "+longitude+" for position "+position);
            }
        }

        if (retrievedA && retrievedB) {
            retrieved = true;
        }

        return retrieved;
    }


    /**
     * Method used to display location mark on the map
     * @param location
     * @param title
     */
    private void setMarker(LatLng location, String title){

        if (title.equalsIgnoreCase("POINT A")) {
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10.2f));
        }else{
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10.2f));
        }

    }




}


