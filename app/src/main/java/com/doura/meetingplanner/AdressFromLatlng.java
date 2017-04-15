package com.doura.meetingplanner;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by doura on 4/10/2017.
 */

public class AdressFromLatlng {

    private double aLat;
    private double aLng;
    private String Adress;
    private Context aContext;

    public AdressFromLatlng(Context context, double aLat, double aLng) {
        this.aLat = aLat;
        this.aLng = aLng;
        this.aContext = context;
        requestAdress();
    }

    public void requestAdress() {

        RequestQueue requestQueue = Volley.newRequestQueue(aContext.getApplicationContext());
        String mkey = "AIzaSyDJD2tptCBtRxCmpKao5FZ5L7rdLgBIcyw";
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + aLat + "," + aLng + "&key=" + mkey;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject jsonObject = response.getJSONArray("results").getJSONObject(0);
                    String mAdress = jsonObject.getString("formatted_address").split(",")[2];
                    setAdress(mAdress);
                    Log.d("Adress ",mAdress);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Error.Response", String.valueOf(error));
                }
            }
        );
        requestQueue.add(request);
    }

    public String getAdress() {
        return Adress;
    }

    public void setAdress(String adress) {
        Adress = adress;
    }
}
