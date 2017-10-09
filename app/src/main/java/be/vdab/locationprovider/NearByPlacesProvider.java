package be.vdab.locationprovider;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


/**
 * Created by vdabcursist on 09/10/2017.
 */

public class NearByPlacesProvider extends AsyncTask <Object,String,String> {

    String googlePlacesData;
    GoogleMap googleMap;
    String url;



    @Override
    protected String doInBackground(Object[] params) {
        try {
            googleMap = (GoogleMap) params[0];
            url = (String) params[1];
            HttpHandler downloadUrl = new HttpHandler();
            googlePlacesData = downloadUrl.getDataFormUrl(url);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return googlePlacesData;
    }


    @Override
    protected void onPostExecute(String result) {
        List<HashMap<String,String>> nearByPlacesList = null;
        DataParse dataParse = new DataParse();
        nearByPlacesList = dataParse.parse(result);
        ShowNearByPlaces(nearByPlacesList);

    }

    private void ShowNearByPlaces (List<HashMap<String,String>> nearByPlacesList){
        Log.d ("places", nearByPlacesList.toString());
        for (int i = 0; i<nearByPlacesList.size();i++){
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String,String> googlePlace = nearByPlacesList.get(i);
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));
            String placeName = googlePlace.get ("place_name");
            String vicinity = googlePlace.get("vicinity");
            LatLng latlng = new LatLng(lat,lng);
            markerOptions.position(latlng);
            markerOptions.title(placeName + " : " + vicinity);
            googleMap.addMarker(markerOptions);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }
    }


}
