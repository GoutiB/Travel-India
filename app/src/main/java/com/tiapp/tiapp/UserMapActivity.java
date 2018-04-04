package com.tiapp.tiapp;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;

import com.firebase.geofire.LocationCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;

import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class UserMapActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener{

    private GoogleMap mMap;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    SupportMapFragment mapFragment;
    Spinner dd_source, dd_destination;
    private static final String Tag = MainActivity.class.getSimpleName();


    private Button mLogout;
    private boolean isLoggingOut = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private Boolean source_set = false, destination_set = false;
    private String source_lat, source_lng, destination_lat, destination_lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        setContentView(R.layout.activity_user_map);

        dd_source = (Spinner) findViewById (R.id.dd_source);
        dd_destination = (Spinner) findViewById (R.id.dd_destination);
        List<String> elements = new ArrayList<String>();
        elements.add("Russal Choak");
        elements.add("Hall 4");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, elements
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dd_source.setAdapter(adapter);
        dd_source.setOnItemSelectedListener(this);



        List<String> elements_d = new ArrayList<String>();
        elements_d.add("Russal Choak");
        elements_d.add("Hall 4");
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, elements
        );
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dd_destination.setAdapter(adapter2);
        dd_destination.setOnItemSelectedListener(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment != null) {
            mapFragment.getMapAsync(UserMapActivity.this);

        }

        mLogout  = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingOut = true;

                disconnectUser();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserMapActivity.this , MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        Log.d(Tag, "App started ");

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
//        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setNumUpdates(2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){


            }else{

                checkLocationPermission();
            }
        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest , mLocationCallback ,Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }

    }

   com.google.android.gms.location.LocationCallback mLocationCallback = new com.google.android.gms.location.LocationCallback(){
       @Override
       public void onLocationResult(LocationResult locationResult) {

           for(Location location : locationResult.getLocations()){
               if (getApplicationContext() != null) {
                   Log.d(Tag, "Got Location");
                   mLastLocation = location;

                   LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                   mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                   mMap.animateCamera(CameraUpdateFactory.zoomTo(14));


                   String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                   DatabaseReference ref = FirebaseDatabase.getInstance().getReference("usersavailable");

                   GeoFire geoFire = new GeoFire(ref);
                   geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
               }

           }
       }
   };



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){

            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest , mLocationCallback ,Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }

                }else{

                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    private void checkLocationPermission() {

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION )){

                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                ActivityCompat.requestPermissions(UserMapActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                .create()
                .show();
            }

            else{

                ActivityCompat.requestPermissions(UserMapActivity.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }




    private void disconnectUser(){

        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isLoggingOut){

             disconnectUser();

        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(Tag, "Clicked location:" +i+" adapter: "+view.toString());
        Spinner spinner = (Spinner) adapterView;
        LatLng latlng = new LatLng(23.176311, 80.020137);
        if(spinner.getId() == R.id.dd_source){
            switch (i) {
                case 0:
                    source_lat = "23.163264";
                    source_lng = "79.936873";
                    latlng = new LatLng(23.163264, 79.936873);
                    break;
                case 1:
                    source_lat = "23.176311";
                    source_lng = "80.020137";
                    latlng = new LatLng(23.176311, 80.020137);
                    break;
            }
            addMarker("source", latlng);
        } else if (spinner.getId() == R.id.dd_destination) {
            switch (i){
                case 0 :
                    destination_lat = "23.163264";
                    destination_lng = "79.936873";
                    latlng = new LatLng(23.163264, 79.936873); break;
                case 1 :
                    destination_lat = "23.176311";
                    destination_lng = "80.020137";
                    latlng = new LatLng(23.176311, 80.020137); break;
            }
            addMarker("destination", latlng);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d(Tag, "Here ... ");
    }

    public void addMarker(String loc, LatLng latLan){
        mMap.addMarker(new MarkerOptions().position(latLan));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLan));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        setLoc_status(loc);
        if(check_path()){
//            LatLng origin = new LatLng(Double.parseDouble(source_lat), Double.parseDouble(source_lng));
//            LatLng destination = new LatLng(Double.parseDouble((destination_lat)), Double.parseDouble(destination_lng));


//            Point displaySize = new Point();
//            getWindowManager().getDefaultDisplay().getSize(displaySize);
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));
        }
    }

    public void setLoc_status(String loc){
        if (loc == "source"){
            source_set = true;
        } else {
            destination_set = true;
        }
    }

    public Boolean check_path(){
        return source_set && destination_set;
    }

}
