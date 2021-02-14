package com.maps_mohitsinghrawat_c0800401;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public final class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnMarkerDragListener {
    private GoogleMap mMap;
    private final HashMap<String, LatLng> markerMap = new HashMap<>();

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;

    private static final float INITIAL_ZOOM = 5.0F;
    private static final int PERMISSIONS_REQUEST_LOCATION = 99;


    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission((Context) this, Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
                getLocation();
            } else {
                checkLocationPermission();
            }
        } else {
            getLocation();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync((OnMapReadyCallback) this);
    }

    @SuppressLint({"MissingPermission"})
    private void getLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastKnownLocation = location;
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission((Context) this, Manifest.permission.ACCESS_FINE_LOCATION) != 0) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this).
                        setTitle("Location Permission Needed").
                        setMessage("This app needs the Location permission, please accept to show distance between the city and your location").
                        setPositiveButton("Ok", (dialogInterface, i) ->
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION))
                        .create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
            }
        }

    }

    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length != 0 && grantResults[0] == 0) {
                if (ContextCompat.checkSelfPermission((Context) this, Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
                    getLocation();
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint({"PotentialBehaviorOverride"})
    public void onMapReady(@NotNull GoogleMap googleMap) {
        mMap = googleMap;
        LatLng canada = new LatLng(56.1304D, -106.3468D);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(canada, INITIAL_ZOOM));
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnPolygonClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);
    }

    public void onMapClick(@Nullable LatLng point) {
        if (markerMap.size() < 4 && point != null) {
            String nameString = "ABCD";
            String name = String.valueOf(nameString.charAt(markerMap.size()));
            Marker marker = mMap.addMarker((new MarkerOptions()).position(point).draggable(true).icon(BitmapDescriptorFactory.fromBitmap(generateCustomMarker(name))));
            marker.setTag(name);
            if (lastKnownLocation != null) {
                double computeDistanceBetween = SphericalUtil.computeDistanceBetween(point, new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                marker.setTitle("Distance in KM: "+ roundDecimal(computeDistanceBetween/1000));
            }

            markerMap.put(name, point);
            if (markerMap.size() == 4) {
                drawOnMap();
            }
        }

    }

    private void drawOnMap() {

        mMap.addPolyline((new PolylineOptions()).clickable(true).color(Color.RED).
                add((LatLng) markerMap.get("A"), (LatLng) markerMap.get("B")));
        mMap.addPolyline((new PolylineOptions()).clickable(true).color(Color.RED).
                add((LatLng) markerMap.get("B"), (LatLng) markerMap.get("C")));
        mMap.addPolyline((new PolylineOptions()).clickable(true).color(Color.RED).
                add((LatLng) markerMap.get("C"), (LatLng) markerMap.get("D")));
        mMap.addPolyline((new PolylineOptions()).clickable(true).color(Color.RED).
                add((LatLng) markerMap.get("D"), (LatLng) markerMap.get("A")));


        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.add((LatLng) markerMap.get("A"),
                (LatLng) markerMap.get("B"),
                (LatLng) markerMap.get("C"),
                (LatLng) markerMap.get("D"));
        polygonOptions.clickable(true);
        polygonOptions.fillColor(Color.parseColor("#5900FF00"));
        mMap.addPolygon(polygonOptions);
    }

    public void onPolylineClick(@NotNull Polyline polyline) {
        double distanceBetweenCitiesInKm = SphericalUtil.computeLength(polyline.getPoints()) / (double) 1000;
        new AlertDialog.Builder(this)
                .setMessage("Distance between cities: " + roundDecimal(distanceBetweenCitiesInKm))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public boolean onMarkerClick(@NotNull Marker marker) {
        Geocoder geocode = new Geocoder((Context) this);
        try {
            List<Address> addressesList = geocode.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
            if (!addressesList.isEmpty()) {
                Toast.makeText(this, addressesList.get(0).getAddressLine(0), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        marker.showInfoWindow();
        return true;
    }

    public void onMapLongClick(@NotNull LatLng position) {
        if (markerMap.size() >= 4) {
            List<LatLng> positions = new ArrayList<>();
            positions.add((LatLng) markerMap.get("A"));
            positions.add((LatLng) markerMap.get("B"));
            positions.add((LatLng) markerMap.get("C"));
            positions.add((LatLng) markerMap.get("D"));
            if (PolyUtil.containsLocation(position, positions, true)) {
                mMap.clear();
                markerMap.clear();
            }
        }

    }

    public void onPolygonClick(@Nullable Polygon polygon) {
        double aToB = SphericalUtil.computeDistanceBetween((LatLng) markerMap.get("A"), (LatLng) markerMap.get("B"));
        double bToC = SphericalUtil.computeDistanceBetween((LatLng) markerMap.get("B"), (LatLng) markerMap.get("C"));
        double cToD = SphericalUtil.computeDistanceBetween((LatLng) markerMap.get("C"), (LatLng) markerMap.get("D"));
        double totalInKm = (aToB + bToC + cToD) / (double) 1000L;

        new AlertDialog.Builder(this)
                .setMessage("Total Distance in Km: " + roundDecimal(totalInKm))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public void onMarkerDragStart(@Nullable Marker marker) {
    }

    public void onMarkerDrag(@Nullable Marker marker) {
    }

    public void onMarkerDragEnd(@NotNull Marker marker) {
        String name = marker.getTag().toString();
        LatLng position = marker.getPosition();
        markerMap.put(name, position);
        if (markerMap.size() == 4) {
            mMap.clear();

            for (String key : markerMap.keySet()) {
                markerMap.get(key);
                Marker mkr = mMap.addMarker((new MarkerOptions()).position(markerMap.get(key)).
                        draggable(true).icon(BitmapDescriptorFactory.fromBitmap(generateCustomMarker(key))));
                mkr.setTag(key);
                if (lastKnownLocation != null) {
                    double computeDistanceBetween = SphericalUtil.computeDistanceBetween(markerMap.get(key), new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()));
                    mkr.setTitle("Distance in KM: "+ roundDecimal(computeDistanceBetween/1000));
                }
            }
            drawOnMap();
        }

    }

    private Bitmap generateCustomMarker(String text) {
        IconGenerator icg = new IconGenerator((Context) this);
        icg.setColor(Color.BLUE);
        icg.setTextAppearance(R.style.WhiteText);
        return icg.makeIcon(text);
    }

    private double roundDecimal(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

}
