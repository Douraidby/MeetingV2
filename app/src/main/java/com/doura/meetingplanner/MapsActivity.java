package com.doura.meetingplanner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int PICK_IMAGE_REQUEST = 1 ;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private StorageReference mStorage;
    private ProgressDialog mProgressdialog;
    public View dView;
    private HashMap<Marker,MarkerHolder> markerHolderMap;
    Uri imageUri;
    //Define a request code to send to Google Play services This code is returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
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
                markerHolderMap = new HashMap<>();
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        EditAlertDialog(latLng);
                    }
                });
            }
        }
    }


    /**
     * Méthode pour ajouter nom et photo a la position du marqueur
     */
    public void EditAlertDialog(LatLng latLng){

            final LatLng ll = latLng;

            dView = getLayoutInflater().inflate(R.layout.windowlayout,null);
            final EditText dName  = (EditText) dView.findViewById(R.id.dName);
            final ImageView dImage = (ImageView)dView.findViewById(R.id.dImage);
            final Button dAnnuler = (Button)dView.findViewById(R.id.dAnnuler);
            final Button dValider = (Button) dView.findViewById(R.id.dValider);

            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
            mBuilder.setView(dView);
            final AlertDialog dialog = mBuilder.create();
            dialog.show();

            dImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(intent, PICK_IMAGE_REQUEST);
                }
            });
            dAnnuler.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { dialog.dismiss(); }
            });

            dValider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String MarkerName =  dName.getText().toString();
                    AddMarker(MarkerName,ll);
                    dialog.dismiss();
                }
            });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                EditAlertDialog(ll);
            }
        });
/*        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                EditAlertDialog(ll);
            }
        });*/
 //               marker.showInfoWindow();
    }

    public void AddMarker(String name,LatLng latLng){

        MarkerOptions options = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude));
        Marker mMarker;

        mMarker = mMap.addMarker(options);
        MarkerHolder mHolder = new MarkerHolder(name,latLng.latitude,latLng.longitude,imageUri);

        Iterator<HashMap.Entry<Marker, MarkerHolder>> iterator = markerHolderMap.entrySet().iterator();
        while(iterator.hasNext()){
            HashMap.Entry<Marker, MarkerHolder> entry = iterator.next();
            if (entry.getValue().getmLat() == latLng.latitude && entry.getValue().getmLong() == latLng.longitude) {
                iterator.remove();
                entry.getKey().remove();
            }
        }

        markerHolderMap.put(mMarker,mHolder);
        ShowInfoWindow(mMarker);
        for (HashMap.Entry<Marker,MarkerHolder> entry: markerHolderMap.entrySet() ) {
            Log.d("Id/MarkerHolder/LatLng",entry.getKey() + "/" + entry.getValue() + latLng);

        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ShowInfoWindow(marker);
                return false;
            }
        });
        mMarker.showInfoWindow();
    }


    public void ShowInfoWindow(final Marker mM){

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                MarkerHolder myHolder = markerHolderMap.get(mM);
                View mView = getLayoutInflater().inflate(R.layout.infowindow,null);
                TextView mName = (TextView) mView.findViewById(R.id.mName);
                TextView mLat = (TextView)mView.findViewById(R.id.mLat);
                TextView mLong = (TextView) mView.findViewById(R.id.mLong);
                ImageView mImage = (ImageView) mView.findViewById(R.id.mIcon);

                mName.setText(myHolder.getmName());
                mLat.setText(String.valueOf(myHolder.getmLat()));
                mLong.setText(String.valueOf(myHolder.getmLong()));
                try {
                    mImage.setImageBitmap(getBitmapFromUri(myHolder.getmUri()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return mView;
            }
        });
    }


 /** Methode pour creer un bitmap a partir d'un Uri **/
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        if (uri != null) {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        }
        return null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView dImage = (ImageView) dView.findViewById(R.id.dImage);
    //    ImageView mImage  = (ImageView) mView.findViewById(R.id.mIcon);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();

            try {
                Bitmap bitmap = getBitmapFromUri(imageUri);
                dImage.setImageBitmap(bitmap);
    //            mImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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

    @Override
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
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }
}


