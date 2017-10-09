package be.vdab.locationprovider;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by vdabcursist on 09/10/2017.
 */

public class DataParse {
    public List <HashMap<String, String>> parse (String jsonData){
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject((String) jsonData);
            jsonArray = jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }


    private List<HashMap<String,String>> getPlaces(JSONArray jsonArray) {
        int placesCount = jsonArray.length();
        List<HashMap<String,String>> placesList = new ArrayList<>();
        HashMap<String,String> placeMap = null;

        for (int i =0; i<placesCount;i++){
            try {
                placeMap = getPlaces((JSONObject)jsonArray.get(i));
                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private HashMap<String,String> getPlaces(JSONObject googlePlacejson) {
        HashMap<String,String> googlePlaceMap = new HashMap<String, String>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";
        Log.d ("getPlaces", "Entered");
        try{
            if(!googlePlacejson.isNull("name")){
                placeName = googlePlacejson.getString("name");
            }
            if (!googlePlacejson.isNull("vicinity")){
                vicinity = googlePlacejson.getString("vicinity");
            }
            latitude = googlePlacejson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlacejson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlacejson.getString("reference");
            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);
            Log.d("getPlaces", "Putting Places");

        } catch (JSONException e) {
            Log.d("getplaces", "error");
            e.printStackTrace();
        }
        return googlePlaceMap;

    }
}
