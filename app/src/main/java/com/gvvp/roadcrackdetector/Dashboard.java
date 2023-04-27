package com.gvvp.roadcrackdetector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.gvvp.roadcrackdetector.env.Logger;

public class Dashboard extends AppCompatActivity {
    public static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.15f;
    public static final int PRIORITY_HIGH_ACCURACY = 100;
    private Button logout_btn, detect_btn,map_btn;
    private FirebaseAuth mAuth;
    public static double latitude;
    public static double longitude;
    private static final Logger LOGGER = new Logger();
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        logout_btn = findViewById(R.id.logout_button);
        detect_btn = findViewById(R.id.detect_button);
        map_btn = findViewById(R.id.map_button);

        mAuth = FirebaseAuth.getInstance();

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Dashboard.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();
                startActivity(new Intent(Dashboard.this, MainActivity.class));
                finish();
                Toast.makeText(Dashboard.this, "Successfully logged out!", Toast.LENGTH_LONG).show();
            }
        });

        detect_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),DetectorActivity.class);
                startActivity(intent);
            }
        });

        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                startActivity(intent);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                Dashboard.this
        );
        this.getLoc();
    }

    public void getLoc() {
        if(ActivityCompat.checkSelfPermission(Dashboard.this
                , android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Dashboard.this
                , android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(Dashboard.this
                    , new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION
                            , android.Manifest.permission.ACCESS_COARSE_LOCATION}
                    , 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            getCurrentLocation();
        } else {
            Toast.makeText(getApplicationContext(), "Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // Request location permission
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            fusedLocationProviderClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();

                    if (location != null) {
                        LOGGER.i("*************" + location.getLatitude());
                        LOGGER.i("*************" + location.getLongitude());
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                    } else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                                .setInterval(100)
                                .setFastestInterval(10)
                                .setNumUpdates(3);

                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                Location location1 = locationResult.getLastLocation();
                                LOGGER.i("*************" + location1.getLatitude());
                                LOGGER.i("*************" + location1.getLongitude());
                                longitude = location1.getLongitude();
                                latitude = location1.getLatitude();
                            }
                        };

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }
                }
            });
        } else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

}