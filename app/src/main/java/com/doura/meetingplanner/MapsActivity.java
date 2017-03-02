package com.doura.meetingplanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int PICK_FROM_GALLERY = 1 ;
    private GoogleMap mMap;
    private Marker mMarker;
    private Marker oMarker;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private StorageReference mStorage;
    private ProgressDialog mProgressdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mStorage = FirebaseStorage.getInstance().getReference();
        mProgressdialog = new ProgressDialog(this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    }


    public boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        } else
            return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);

            if (mMap != null) {
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        AddMarker(latLng);
                    }
                });
            }
        }
    }

    public void AddMarker(LatLng latLng){

        if(mMarker!=null)
            mMarker.remove();


        EditMarker();

        MarkerOptions options = new MarkerOptions()
                .title("Cliquer pour éditer")
                .position(new LatLng(latLng.latitude, latLng.longitude));

        mMarker = mMap.addMarker(options);
        mMarker.showInfoWindow();

    }


    /**
     * Méthode pour ajouter nom et photo a la position du marqueur
     */
    public void EditMarker(){

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
                final View mView = getLayoutInflater().inflate(R.layout.infowindow,null);
                final View dView = getLayoutInflater().inflate(R.layout.windowlayout,null);

                final TextView mName  = (TextView) mView.findViewById(R.id.mName);
                final TextView mLat  = (TextView) mView.findViewById(R.id.mLat);
                final TextView mLong  = (TextView) mView.findViewById(R.id.mLong);

                Button dAnnuler = (Button)dView.findViewById(R.id.dAnnuler);
                Button dValider = (Button) dView.findViewById(R.id.dValider);
                final EditText dName  = (EditText) dView.findViewById(R.id.dName);
                ImageView dImage = (ImageView)dView.findViewById(R.id.dImage);

                mBuilder.setView(dView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();

                dImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FROM_GALLERY);
                    }

                });

                dAnnuler.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { dialog.dismiss(); }
                });

                dValider.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MapsActivity.this, "Bouton valider on click!", Toast.LENGTH_LONG).show();
                        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker marker) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                Toast.makeText(MapsActivity.this, "getInfoContents!", Toast.LENGTH_LONG).show();
                                LatLng ll = marker.getPosition();
                                mName.setText(dName.getText().toString());
                                mLat.setText("Latitude:" + ll.latitude);
                                mLong.setText("Longitude" + ll.longitude);
                                return mView;
                            }
                        });
                        dialog.dismiss();
                    }
                });

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final View mView = getLayoutInflater().inflate(R.layout.infowindow,null);
        final ImageView mImage = (ImageView) mView.findViewById(R.id.mIcon);
        View dView = getLayoutInflater().inflate(R.layout.windowlayout,null);
        ImageView dImage = (ImageView) dView.findViewById(R.id.dImage);

        if (requestCode== PICK_FROM_GALLERY && resultCode== Activity.RESULT_OK ) {
            Bitmap mbitmap = (Bitmap) data.getExtras().get("data");
            mImage.setImageBitmap(mbitmap);

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            dImage.setImageBitmap(bitmap);
        }
        else
            Toast.makeText(MapsActivity.this, "data.getExtras(): " + data, Toast.LENGTH_LONG).show();
    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    public void getlocation() {

 /*      if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }*/
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


    public void onLocationChanged(Location location) {

        mLastLocation = location;
        //placer le marqueur de la position courante
        LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        MarkerOptions markeroptions = new MarkerOptions();
        markeroptions.position(latLng);
        markeroptions.title("Position actuelle!");
        markeroptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mMap.addMarker(markeroptions);
        //Bouger la camera de la carte
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }





    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


}


