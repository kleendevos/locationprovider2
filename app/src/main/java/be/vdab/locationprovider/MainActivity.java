package be.vdab.locationprovider;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 777 ;
    TextView tvCapturedGeolocation;
    private GoogleApiClient googleApiClient;
    ImageView ivRadar;
    private double longitude;
    private double latitude;
    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivRadar = (ImageView) findViewById(R.id.ivRadar);
        tvCapturedGeolocation = (TextView) findViewById(R.id.tvCapturedGeolocation);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this,this)
                .build();

    }
    public void btnCaptureLocationOnClick (View view){
        if (checkPermission()) {
        } else {
            requestPermission();
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationlistener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if(grantResults.length>0 && grantResults [0] == PackageManager.PERMISSION_GRANTED){
                    Snackbar.make(tvCapturedGeolocation, "Permission Granted, Now you can acces location data", Snackbar.LENGTH_LONG).show();
                } else {
                    Snackbar.make(tvCapturedGeolocation, "Permission Denied, Yout Cannot acces location data.", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }


    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Toast.makeText(this,"GPS permission needed in order to capture your location", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ACCESS_COARSE_LOCATION);
        }
    }



    LocationListener locationlistener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            String msg = "Location captured succesfully: longitud %s latitude %s";

            longitude = location.getLongitude();
            latitude = location.getLatitude();

            mapFragment.getMapAsync(MainActivity.this);

            msg = String.format(msg, location.getLongitude(),location.getLatitude());
            try {
                msg += "\nStreet: " + getAddressFromLocation (location.getLongitude(), location.getLatitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            tvCapturedGeolocation.setText(msg);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(MainActivity.this, "Provider Enabled", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(MainActivity.this,"Provider Disabled", Toast.LENGTH_LONG).show();

        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {

        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(longitude,latitude));

        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);

        googleMap.moveCamera(center);
        googleMap.moveCamera(zoom);

        getNearbyRestaurant (longitude, latitude, googleMap);
    }

    private void getNearbyRestaurant(double lat, double longu, GoogleMap googleMap) {
        googleMap.clear();
        String url = getUrl (lat,longu);
        Object[] DataTransfer = new Object[2];
        DataTransfer[0] = googleMap;
        DataTransfer[1] = url;

        NearByPlacesProvider nearByPlacesProvider = new NearByPlacesProvider();
        nearByPlacesProvider.execute(DataTransfer);
        Toast.makeText(this,"nearby Restaurants ", Toast.LENGTH_LONG).show();

    }

    private String getUrl(double latitude, double longitude) {
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=" + latitude + "," + longitude);
        googlePlacesUrl.append("&radius=" + 15000);
        googlePlacesUrl.append("&type=restaurant");
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + "AIzaSyDXWDSTVEY1RhR-wAkAnjVmBoWq_750Mvo");
        Log.d("locationProvider", googlePlacesUrl.toString());
        return (googlePlacesUrl.toString());
    }

    private String getAddressFromLocation(double longitude, double latitude) throws IOException {
        Geocoder geocoder = new Geocoder(this);
        List<Address> matches = geocoder.getFromLocation(latitude, longitude, 1);
        if(matches.size()<1) return "";
        return (matches.isEmpty() ? null:matches.get(0)).getAddressLine(0).toString();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"unable to find nearby restaurants, check your internet connection", Toast.LENGTH_LONG).show();
    }
}
