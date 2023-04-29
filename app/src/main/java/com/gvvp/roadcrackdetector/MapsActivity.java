package com.gvvp.roadcrackdetector;

import static androidx.constraintlayout.widget.Constraints.TAG;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.gvvp.roadcrackdetector.databinding.ActivityMapsBinding;

import com.gvvp.roadcrackdetector.env.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private List<Marker> markerList;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final int DEFAULT_ZOOM = 15;
    private static final Logger LOGGER = new Logger();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        markerList = new ArrayList<>();
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        final String uid = currentUser.getUid();

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                lastKnownLocation = location;
                LatLng latLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference locationsRef = db.collection("Users").document(uid).collection("locations");

        locationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String label = document.getString("Label");
                        double latitude = document.getDouble("Latitude");
                        double longitude = document.getDouble("Longitude");

                        String addressLine = document.getString("Address Line");
                        double confidence = document.getDouble("Confidence");
                        String image = document.getString("Image");
                        String locality = document.getString("Locality");
                        String postalCode = document.getString("Postal Code");
                        String timeStamp = document.getString("TimeStamp");

                        Bitmap markerimage = decodeBase64ToBitmap(image);

                        LatLng location = new LatLng(latitude, longitude);

                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .title(label)
                                .snippet(addressLine);

                        Marker newmarker = mMap.addMarker(markerOptions);
                        newmarker.setTag(image);
                        markerList.add(newmarker);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (Marker marker : markerList) {
                            builder.include(marker.getPosition());
                        }
                        LatLngBounds bounds = builder.build();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        /*
        // Create a database reference to the "locations" node in your Firebase Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("locations");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    // Get the key of the child location node
                    String locationKey = childSnapshot.getKey();
                    // Get the values of the child location node
                    Map<String, Object> locationValues = (Map<String, Object>) childSnapshot.getValue();
                    // Access individual values using the keys

                    String label = (String) locationValues.get("Label");
                    double latitude = (double) locationValues.get("Latitude");
                    double longitude = (double) locationValues.get("Longitude");

                    String addressLine = (String) locationValues.get("Address Line");
                    double confidence = (double) locationValues.get("Confidence");
                    String image = (String) locationValues.get("Image");
                    String locality = (String) locationValues.get("Locality");
                    String postalCode = (String) locationValues.get("Postal Code");
                    String timeStamp = (String) locationValues.get("TimeStamp");

                    Bitmap markerimage = decodeBase64ToBitmap(image);

                    LatLng location = new LatLng(latitude, longitude);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(location)
                            .title(label)
                            .snippet(addressLine);
                            //.icon(BitmapDescriptorFactory.fromBitmap(markerimage));

                    Marker newmarker = mMap.addMarker(markerOptions);
                    newmarker.setTag(image);
                    markerList.add(newmarker);
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markerList) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });*/
    }
    private Bitmap decodeBase64ToBitmap(String base64) {
        byte[] imageBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
}

