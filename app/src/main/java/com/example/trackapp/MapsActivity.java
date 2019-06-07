package com.example.trackapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.room.Room;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private Button startBtn, stopBtn, calcBtn;
    private TextView distTV;
    double longitude, latitude;


    private GPSTracker gps;
    private GoogleDirection gd;
    private String lati, longi;
    private String TotalDistance, TotalDuration;

    public static CoordinateRepository coordinateRepository;
    //public static MyDatabase myDatabase;

    private Double[] PositionA = new Double[2];
    private Double[] PositionB = new Double[2];


    private LatLng startPoint,endPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        coordinateRepository = new CoordinateRepository(getApplication());
        //myDatabase = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, "my_database").allowMainThreadQueries().build();

        startBtn = findViewById(R.id.start);
        stopBtn = findViewById(R.id.stop);
        calcBtn = findViewById(R.id.calcDisc);
        distTV = findViewById(R.id.distanceTV);

        stopBtn.setEnabled(false);
        calcBtn.setEnabled(false);
        distTV.setVisibility(View.GONE);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: start button clicked... attempting to save to DB");
                stopBtn.setEnabled(true);
                startBtn.setEnabled(false);
                distTV.setVisibility(View.GONE);
                getLocation();
                setMarker(latitude, longitude, "Point A");
                saveCoordinateToDb(1, "Point A", longitude, latitude);
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: start button clicked... attempting to save to DB");
                getLocation();
                setMarker(latitude, longitude, "Point B");
                saveCoordinateToDb(2, "Point B", longitude, latitude);
                startBtn.setEnabled(true);
                stopBtn.setEnabled(false);
                calcBtn.setEnabled(true);
            }
        });

        calcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                measureAndDisplayDistance();
                distTV.setText(TotalDistance);
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady: Map is Ready...");

        mMap = googleMap;

        //Retrieve Coordinates from RoomDb
        retrieveCoordinatesFromDb();

        if (PositionA != null && PositionB != null){

            /**if coordinates are found in db, calculate distance between the two coordinates
             * and display distance on the maps view
             */
            measureAndDisplayDistance();

        }else{

            /**
             * if no cordinates in the db, get the current location of the device
             * and display on the map
             */
            getLocation();

            // Add a marker to the device's current location and move the camera
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

            // Check if GPS enabled
            if (gps.canGetLocation()) {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                Toast.makeText(getApplicationContext(),
                        "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                lati = Double.toString(latitude);
                longi = Double.toString(longitude);
                Log.d(TAG, "getLocation: your location is lati:" + lati + "and longi:" + longi);
            } else {
                // Can't get location.
                // GPS or network is not enabled.
                // Ask user to enable GPS/network in settings.
                gps.showSettingsAlert();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted,
                    gps = new GPSTracker(this, MapsActivity.this);

                    // Check if GPS enabled
                    if (gps.canGetLocation()) {

                        latitude = gps.getLatitude();
                        longitude = gps.getLongitude();

                        Toast.makeText(getApplicationContext(),
                                "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG)
                                .show();
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
    }

    /**
     * method used to save coordinates to the roomdb using an asyntask class
     * @param id
     * @param position
     * @param longitude
     * @param latitude
     */
    private void saveCoordinateToDb(int id, String position, double longitude, double latitude){

        Coordinate coordinate = new Coordinate(id, position, longitude, latitude);
        coordinateRepository.addCoordinate(coordinate);
        //myDatabase.myDao().addCoordinate(coordinate);
        Toast.makeText(MapsActivity.this, "Co-Ordinates saved to ROOM DB", Toast.LENGTH_LONG).show();
        Log.d(TAG, "saveCoordinateToDb: id: "+id
            +"position: "+position
            +"longitude: "+longitude
            +"latitude: "+latitude);

    }

    /**
     * Method used to retrieve coordinates from RoomDb
     */
    private void retrieveCoordinatesFromDb(){

        List<Coordinate> coordinates = coordinateRepository.getAllCoordinates();

        for (Coordinate cood : coordinates){

            int id = cood.getId();
            String position = cood.getPosition();
            double longitude = cood.getLongitude();
            double latitude = cood.getLatitude();

            if (id == 1){
                PositionA[0] = latitude;
                PositionA[1] = longitude;
                Log.d(TAG, "retrieveCoordinatesFromDb:, lat: "+latitude+" long: "+longitude+" for position "+position);
            }else if (id == 2){
                PositionB[0] = latitude;
                PositionB[1] = longitude;
                Log.d(TAG, "retrieveCoordinatesFromDb: PositionB, lat: "+latitude+" long: "+longitude+" for position "+position);
            }
        }
    }



    /**
     * Method used to set marker on the map to show device location
     * @param latitude
     * @param longitude
     * @param title
     */
    private void setMarker(double latitude, double longitude, String title){

        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10.2f));
    }

    /**
     * Method for measuring distance between two points/coordinates
     */
    private void measureAndDisplayDistance(){

        startPoint = new LatLng(PositionA[0], PositionA[1]);
        endPoint = new LatLng(PositionB[0], PositionB[1]);

        gd = new GoogleDirection(this);
        gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {
            public void onResponse(String status, Document doc, GoogleDirection gd) {
                Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();

                gd.animateDirection(mMap, gd.getDirection(doc), GoogleDirection.SPEED_FAST
                        , true, true, true, false, null, false, true, new PolylineOptions().width(8).color(Color.RED));

                mMap.addMarker(new MarkerOptions().position(startPoint)
                        .icon(BitmapDescriptorFactory.defaultMarker()));

                mMap.addMarker(new MarkerOptions().position(endPoint)
                        .icon(BitmapDescriptorFactory.defaultMarker()));

                TotalDistance = gd.getTotalDistanceText(doc);
                TotalDuration = gd.getTotalDurationText(doc);
            }
        });

        gd.request(startPoint, endPoint, GoogleDirection.MODE_DRIVING);


    }



}


