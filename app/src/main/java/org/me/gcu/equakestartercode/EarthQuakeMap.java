package org.me.gcu.equakestartercode;
// Carlos Leal, Matric Number 20/21 - S1828057

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class EarthQuakeMap extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMap = null;
    List<EarthquakeClass> earthquakes;
    EarthquakeClass selectedEarthquake = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earth_quake_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        Bundle b = getIntent().getExtras() == null ? savedInstanceState : getIntent().getExtras();
        earthquakes = b.getParcelableArrayList("List of Earthquakes");

        Object obj = b.getParcelable("Selected Earthquake");
        if (obj instanceof EarthquakeClass) {
            EarthquakeClass eq = (EarthquakeClass) obj;
            this.selectedEarthquake = eq;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("List of Earthquakes", new ArrayList<>(earthquakes));

        if (selectedEarthquake != null)
            outState.putParcelable("Selected Earthquake", selectedEarthquake);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera and location services!", Toast.LENGTH_SHORT).show();
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    private boolean onMarkerClick(Marker marker) {
        if (marker.getTag() != null && marker.getTag() instanceof EarthquakeClass) {
            EarthquakeClass earthquake = (EarthquakeClass)marker.getTag();
            selectedEarthquake = earthquake;

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(marker.getPosition()).build();
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            Toast.makeText(getBaseContext(), "HTML link: " + earthquake.getLink() + ",\n "
                    + "Publication Date: " + earthquake.getPubDate() + ",\n " + "Earthquake category: "
                    + earthquake.getCategory() + ",\n " + "Latitude distance: " + earthquake.getGeoLatitude() + ",\n "
                    + "Longitude distance: " + earthquake.getGeoLongitude(), Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        this.mMap.setOnMarkerClickListener(this::onMarkerClick);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            this.requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 0);
        }

        this.mMap.setOnCameraMoveListener(() -> {
            final LatLng target = mMap.getCameraPosition().target;
            TextView tv = (TextView) this.findViewById(R.id.mapTopText);
            tv.setText(String.format(" Your Lat/Long:   %f  ,  %f", target.latitude, target.longitude));
        });

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(54.63, -4.49)).zoom(5.4f).build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        loadMarkers();
    }

    void loadMarkers() {
        // Clear the map markers
        mMap.clear();

        for (EarthquakeClass earthquake : earthquakes) {
            @SuppressLint("DefaultLocale")
            final String markerTitle = earthquake.getMetadata().getMagnitude() + " M // " + earthquake.getMetadata().getLocation();

            //If Magnitude is between
            //  0-1 (blue),
            //  1-2 (green),
            //  2-3 (yellow),
            //  3-4 (red)

            float mapColour = BitmapDescriptorFactory.HUE_MAGENTA;

            // Adding Markers to the map, with colours based on the earthquake's magnitude.
            if (earthquake.getMetadata().getMagnitudeBetween(0, 1)) {
                mapColour = BitmapDescriptorFactory.HUE_BLUE;
            } else if (earthquake.getMetadata().getMagnitudeBetween(1, 2)) {
                mapColour = BitmapDescriptorFactory.HUE_GREEN;
            } else if (earthquake.getMetadata().getMagnitudeBetween(2, 3)) {
                mapColour = BitmapDescriptorFactory.HUE_YELLOW;
            } else if (earthquake.getMetadata().getMagnitude() >= 3) {
                mapColour = BitmapDescriptorFactory.HUE_RED;
            }

            double latitude  = Double.parseDouble(earthquake.getGeoLatitude());
            double longitude = Double.parseDouble(earthquake.getGeoLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(markerTitle)
                    .icon(BitmapDescriptorFactory.defaultMarker(mapColour)));

            marker.setTag(earthquake);

            // IF MARKER CLICKED
            if (this.selectedEarthquake != null && earthquake.Guid.equals(this.selectedEarthquake.Guid)) {
                marker.showInfoWindow();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(marker.getPosition()).zoom(6f).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

        }
    }
}