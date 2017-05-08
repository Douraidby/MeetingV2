package com.doura.meetingplanner;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import DirectionModules.DirectionFinder;
import DirectionModules.DirectionFinderListener;
import DirectionModules.Route;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,DatePickerFragment.FragmentCallbacks,
        TimePickerFragment.FragmentCallbacks,DirectionFinderListener, View.OnClickListener{

    //Define a request code to send to Google Play services This code is returned in Activity.onActivityResult
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int PICK_IMAGE_REQUEST = 1 ;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long FASTEST_INTERVAL =10000; /* 10 secs */
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationRequest mLocationRequest;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgressdialog;
    private HashMap<Marker,MarkerHolder> placeHolderMap;
    private HashMap<Marker,MarkerHolder> markerHolderMap;
    private HashMap<Marker,User> userHolderMap;
    private List<MarkerHolder> TempHolderList;
    private Uri imageUri;
    public View dView;
    public String cuName;
    public String cuGroup;
    public String cuImage;
    public Boolean cuOrganizer;
    public String cuRating;
    public List<String> Votes;
    private TextView datedebut;
    private TextView datefin;
    private TextView timedebut;
    private TextView timefin;
    private List<LatLng> mList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Polyline mPolyline;
    private MenuItem menuItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        buildGoogleApiClient();

        Bundle extras = getIntent().getExtras();
        cuName = extras.getString("user_name");
        cuGroup = extras.getString("user_group");
        cuImage = extras.getString("user_image");
        cuOrganizer = extras.getBoolean("user_organizer");
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Log.d("cuOrganizer ",String.valueOf(cuOrganizer));
        mProgressdialog = new ProgressDialog(this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            // This calls onMapReady(). (Asynchronously)
            mapFragment.getMapAsync(this);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        menuItem = menu.findItem(R.id.get_votes);
        writeBadge(0);
        return super.onCreateOptionsMenu(menu);
    }

    private void writeBadge(int count) {
        MenuItemCompat.setActionView(menuItem, R.layout.badge_layout);
        RelativeLayout layout = (RelativeLayout) MenuItemCompat.getActionView(menuItem);
        // A TextView with number.
        TextView tv = (TextView) layout.findViewById(R.id.badge_textView);
        if (count == 0) {
            tv.setVisibility(View.INVISIBLE);
        } else {
            tv.setVisibility(View.VISIBLE);
            tv.setText(String.valueOf(count));
        }
        // An icon, it also must be clicked.
        ImageView imageView = (ImageView) layout.findViewById(R.id.badge_icon_button);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        };
        menuItem.getActionView().setOnClickListener(onClickListener);
        imageView.setOnClickListener(onClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.open_blogholder:
                Intent intent = new Intent(this,BlogListActivity.class);
                Bundle extras = new Bundle();
                extras.putString("group",cuGroup);
                extras.putString("user",cuName);
                intent.putExtras(extras);
                startActivity(intent);
                return true;

            case R.id.start_chat:
                Intent mIntent = new Intent(this, ChatRoom.class);
                mIntent.putExtra("group",cuGroup);
                mIntent.putExtra("user", cuName);
                startActivity(mIntent);
                return true;
            case R.id.send_Positions:
                if(cuOrganizer)
                    UploadMeetingPlaces();
                else
                    Toast.makeText(this, "Seul l'oraganisateur a le droit de proposer des lieux de rencontre.", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.get_votes:
                if (cuOrganizer){
                    ShowVotes();
                    writeBadge(0);}
                else
                    Toast.makeText(this, "Accés restreint a l'organisateur!", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.get_event:
                    getEvent();
                return true;

            case R.id.quit_app:
                quitApplication();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getEvent() {
        DatabaseReference dbref = mDatabase.child(cuGroup).child("Events");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event myevent = dataSnapshot.getValue(Event.class);
                View edView = getLayoutInflater().inflate(R.layout.getevent_layout,null);
                TextView eName  = (TextView) edView.findViewById(R.id.eventname);
                TextView einfo  = (TextView) edView.findViewById(R.id.eventinfo);
                TextView eddebut  = (TextView) edView.findViewById(R.id.eventddebut);
                TextView ehdebut  = (TextView) edView.findViewById(R.id.eventhdebut);
                TextView edfin = (TextView) edView.findViewById(R.id.eventdfin);
                TextView ehfin = (TextView) edView.findViewById(R.id.eventhfin);

                eName.setText(myevent.geteName());
                einfo.setText(myevent.geteDescription());
                eddebut.setText(myevent.geteStartDate());
                ehdebut.setText(myevent.geteStartTime());
                edfin.setText(myevent.geteEndDate());
                ehfin.setText(myevent.geteEndTime());

                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
                mBuilder.setView(edView);
                AlertDialog dialog = mBuilder.create();
                dialog.show();    }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void quitApplication() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Voulez vous fermer l'application?");
        alertDialogBuilder.setPositiveButton("Oui",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        finishAffinity();
                    }
                });
        alertDialogBuilder.setNegativeButton("non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    /**
     * Methode pour sauvegarder les marqueurs avec leurs images dans Firebase
     */
    private void UploadMeetingPlaces() {
        DatabaseReference dbref = mDatabase.child(cuGroup).child("Meeting_Markers");
        dbref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    if (markerHolderMap.size() == 3) {
                        mProgressdialog.setMessage("Uploading to Firebase...");
                        mProgressdialog.show();
                        for (final HashMap.Entry<Marker, MarkerHolder> entry : markerHolderMap.entrySet()) {
                            String imgUri = entry.getValue().getmUri();
                            StorageReference Filepath = mStorage.child("Markers_Images").child(Uri.parse(imgUri).getLastPathSegment());
                            Filepath.putFile(Uri.parse(imgUri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    String ImgUrl = taskSnapshot.getDownloadUrl().toString();
                                    DatabaseReference dbref = mDatabase.child(cuGroup).child("Meeting_Markers");
                                    String id = dbref.push().getKey();
                                    entry.getValue().setmImgUrl(ImgUrl);
                                    entry.getValue().setmId(id);
                                    dbref.child(id).setValue(entry.getValue());
                                    dbref.child(entry.getValue().getmId()).child("mVotes").child(cuName + "@" + cuGroup).setValue(entry.getValue().getmVote());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        mProgressdialog.dismiss();
                    } else
                        Toast.makeText(MapsActivity.this, "Il faut ajouter trois lieux en appuyant longtemps sur la carte!", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(MapsActivity.this, "Vous avez déja ajouté trois lieux sur la carte!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
            if (mMap != null) {
                Log.d("onMapReady","Map not null");
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                markerHolderMap = new HashMap<>();
                userHolderMap = new HashMap<>();
                placeHolderMap = new HashMap<>();
                TempHolderList = new ArrayList<>();
                Votes = new ArrayList<>();

                Button btnTchemin = (Button) findViewById(R.id.btnFindPath);
                btnTchemin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        StartFindingDirection();
                    }
                });

                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        if (cuOrganizer)
                            EditAlertDialog(latLng);
                        else
                               Toast.makeText(MapsActivity.this, "Seul l'organisateur peut ajouter un lieu", Toast.LENGTH_SHORT).show();
                        }
                    });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        ShowInfoWindow(marker);
                        LatLng mLL = new LatLng(marker.getPosition().latitude,marker.getPosition().longitude);
                        if (mList.size()==2)
                            mList.remove(0);
                        mList.add(mLL);
                        return false;
                    }
                });

                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
/*                if (marker.getTag()=="place")
                    EditAlertDialog(ll);*/
                        if (!cuOrganizer && marker.getTag()=="placeOnServer"){
                            VoteDialog(marker);
                        }
                    }
                });

                if (cuOrganizer)
                    CountAllVotes();
            }
        }
    }

    private void StartFindingDirection() {
        if (mList.isEmpty()) {
            Toast.makeText(this, "Veuillez choisir 2 markeurs en cliquant dessus.", Toast.LENGTH_SHORT).show();
            return;}

        if (mList.size() ==1) {
            Toast.makeText(this, "Vueillez choisir une destination en cliquant sur le markeur correspondant.", Toast.LENGTH_SHORT).show();
            return;}

        try {
            new DirectionFinder(this, mList.get(0), mList.get(1)).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();}
    }


    private void CheckIfOrganizer(){
        final DatabaseReference  db = mDatabase.child(cuGroup).child("Users_Markers").child(cuName+"@"+cuGroup);
        db.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (dataSnapshot.exists()) {
 //                   db.child("organizer").setValue(true);
                    cuOrganizer = true;
                    Log.d("true",String.valueOf(dataSnapshot.getValue()));
                }
                else {

                    Log.d("false", String.valueOf(dataSnapshot.getValue()));
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getMeetingPlaces() {

        DatabaseReference dbref = mDatabase.child(cuGroup).child("Meeting_Markers");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        MarkerHolder mHolder = data.getValue(MarkerHolder.class);

                        MarkerOptions options = new MarkerOptions().position(new LatLng(mHolder.getmLat(), mHolder.getmLong()))
                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        Marker mMarker = mMap.addMarker(options);
                        mMarker.setTag("placeOnServer");
                        placeHolderMap.put(mMarker,mHolder);
//                        mMarker.showInfoWindow();
                    }
                     Log.d("getmeet Children Count ", String.valueOf(dataSnapshot.getChildrenCount()));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Méthode pour ajouter nom et photo a la position du marqueur
     */
    public void EditAlertDialog(LatLng latLng){

            final LatLng ll = latLng;

            dView = getLayoutInflater().inflate(R.layout.windowlayout,null);
            final EditText dName  = (EditText) dView.findViewById(R.id.dName);
            ImageView dImage = (ImageView)dView.findViewById(R.id.dImage);
            Button dAnnuler = (Button)dView.findViewById(R.id.dAnnuler);
            Button dValider = (Button) dView.findViewById(R.id.dValider);
            RatingBar dRating = (RatingBar) dView.findViewById(R.id.dRatingBar);

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

            dRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    cuRating = String.valueOf(rating);
                }
            });

            dAnnuler.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { dialog.dismiss(); }
            });

            dValider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((imageUri !=null && cuRating!=null)){
                        String placeName =  dName.getText().toString();
                        AddMarker(placeName,ll);
                        dialog.dismiss();}
                    else
                        Toast.makeText(MapsActivity.this, "Vous devez choisir nom, image et note!", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void VoteDialog(final Marker marker) {
        final DatabaseReference dbref = mDatabase.child(cuGroup).child("Meeting_Markers");
        View voteView = getLayoutInflater().inflate(R.layout.votewindow,null);
        final Button vBtn = (Button) voteView.findViewById(R.id.voteBtn);
        final RatingBar vBar = (RatingBar) voteView.findViewById(R.id.userratingBar);

         AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        mBuilder.setView(voteView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        vBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                cuRating = String.valueOf(rating);
            }
        });

        vBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference dbref = mDatabase.child(cuGroup).child("Meeting_Markers");

                for (final HashMap.Entry<Marker, MarkerHolder> entry : placeHolderMap.entrySet()) {
                    if (entry.getKey().equals(marker)){
                        dbref.child(entry.getValue().getmId()).child("mVotes").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.child(cuName+"@"+cuGroup).exists()){
                                    Toast.makeText(MapsActivity.this, "Vous avez déja noté ce lieu!", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();}
                                else {
                                    dbref.child(entry.getValue().getmId()).child("mVotes").child(cuName + "@" + cuGroup).setValue(cuRating);
                                    Toast.makeText(MapsActivity.this, "Votre note sera sauvegarder dans Firebase.", Toast.LENGTH_SHORT).show();}
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            }
        });
    }


    public void CheckIfAllRated() {
        DatabaseReference  dbRef = mDatabase.child(cuGroup).child("Users_Markers");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                long Nbusers = dataSnapshot.getChildrenCount();
                if (TempHolderList.get(0).getmVotes().size()== Nbusers && TempHolderList.get(1).getmVotes().size()== Nbusers && TempHolderList.get(2).getmVotes().size()== Nbusers)
                    writeBadge(1);
//                    ShowVotes();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void ShowVotes() {
        View vView = getLayoutInflater().inflate(R.layout.votes_layout,null);
        TextView vName1  = (TextView) vView.findViewById(R.id.lieu1);
        RatingBar vMoy1 = (RatingBar)vView.findViewById(R.id.RatingBar1);
        Button vbtn1 = (Button)vView.findViewById(R.id.btn1);
        TextView vName2  = (TextView) vView.findViewById(R.id.lieu2);
        RatingBar vMoy2 = (RatingBar)vView.findViewById(R.id.RatingBar2);
        Button vbtn2 = (Button)vView.findViewById(R.id.btn2);
        TextView vName3  = (TextView) vView.findViewById(R.id.lieu3);
        RatingBar vMoy3 = (RatingBar)vView.findViewById(R.id.RatingBar3);
        Button vbtn3 = (Button)vView.findViewById(R.id.btn3);

        vName1.setText(TempHolderList.get(0).getmName());
        vMoy1.setRating(TempHolderList.get(0).getVotesMoy());
        vName1.setText(TempHolderList.get(1).getmName());
        vMoy1.setRating(TempHolderList.get(1).getVotesMoy());
        vName1.setText(TempHolderList.get(2).getmName());
        vMoy1.setRating(TempHolderList.get(2).getVotesMoy());

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        mBuilder.setView(vView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        vbtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                EditEventDialog();
            }
        });
    }

    private void EditEventDialog() {

        final View edView = getLayoutInflater().inflate(R.layout.event_layout,null);
        Button btn_ddebut = (Button) edView.findViewById(R.id.btn_ddebut);
        Button btn_hdebut = (Button) edView.findViewById(R.id.btn_hdebut);
        Button btn_dfin = (Button) edView.findViewById(R.id.btn_dfin);
        Button btn_hfin = (Button) edView.findViewById(R.id.btn_hfin);
        Button btn_envoyer = (Button) edView.findViewById(R.id.btn_envoyer);

        datedebut = (TextView) edView.findViewById(R.id.ddebut);
        timedebut = (TextView) edView.findViewById(R.id.hdebut);
        datefin = (TextView) edView.findViewById(R.id.dfin);
        timefin = (TextView) edView.findViewById(R.id.hfin);

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MapsActivity.this);
        mBuilder.setView(edView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();

        btn_ddebut.setOnClickListener(this);
        btn_dfin.setOnClickListener(this);
        btn_hdebut.setOnClickListener(this);
        btn_hfin.setOnClickListener(this);

        btn_envoyer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadEvent(edView);
                dialog.dismiss();
            }

        });
        mBuilder.setView(edView);
        dialog.show();
    }

    private void UploadEvent(View eView) {

        DatabaseReference dbref = mDatabase.child(cuGroup).child("Events");
        EditText txt_name = (EditText) eView.findViewById(R.id.ename);
        EditText txt_info = (EditText) eView.findViewById(R.id.einfo);

        Event anEvent = new Event(txt_name.getText().toString(), txt_info.getText().toString(),datedebut.getText().toString(),timedebut.getText().toString(),
                datefin.getText().toString(),timefin.getText().toString());
        dbref.setValue(anEvent);
        Toast.makeText(this, "Event uploaded to Firebase!", Toast.LENGTH_SHORT).show();
    }

    public void CountAllVotes(){
        DatabaseReference dbref = mDatabase.child(cuGroup).child("Meeting_Markers");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    TempHolderList.clear();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        MarkerHolder tempHolder = data.getValue(MarkerHolder.class);
                        TempHolderList.add(tempHolder);
                        Log.d("tempHolder votes",String.valueOf(tempHolder.getmVotes().size()));
                    }
                    CheckIfAllRated();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void AddMarker(String name,LatLng latLng){

            MarkerOptions options = new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            Marker mMarker;
            mMarker = mMap.addMarker(options);
            mMarker.setTag("place");
            MarkerHolder mHolder = new MarkerHolder(name, cuGroup, latLng.latitude, latLng.longitude, imageUri.toString(),cuRating);

            Iterator<HashMap.Entry<Marker, MarkerHolder>> iterator = markerHolderMap.entrySet().iterator();
            while (iterator.hasNext()) {
                HashMap.Entry<Marker, MarkerHolder> entry = iterator.next();
                if (entry.getValue().getmLat() == latLng.latitude && entry.getValue().getmLong() == latLng.longitude) {
                    iterator.remove();
                    entry.getKey().remove();
                }
            }

            markerHolderMap.put(mMarker, mHolder);
            mMarker.showInfoWindow();

/*        for (HashMap.Entry<Marker,MarkerHolder> entry: markerHolderMap.entrySet() ) {
            Log.d("Id/MarkerHolder/LatLng",entry.getKey() + "/" + entry.getValue() + latLng);
        }*/
    }

    public void ShowInfoWindow(final Marker mM){

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View mView = getLayoutInflater().inflate(R.layout.infowindow, null);
                TextView mName = (TextView) mView.findViewById(R.id.mName);
                TextView mLat = (TextView) mView.findViewById(R.id.mLat);
                TextView mLong = (TextView) mView.findViewById(R.id.mLong);
                ImageView mImage = (ImageView) mView.findViewById(R.id.mIcon);
                RatingBar mRating = (RatingBar) mView.findViewById(R.id.mRating);

                if (marker.getTag()=="user") {
                    User myHolder = userHolderMap.get(marker);
/*                    AdressFromLatlng mAdress = new AdressFromLatlng(MapsActivity.this, myHolder.getuLat(), myHolder.getuLong());
                    String mAddress = mAdress.getAdress();
                    if (mAddress==null)
                        Log.d("madress", "nullll");
                    mLat.setText(mAddress);
                    mLong.setText("Long:" + String.valueOf(myHolder.getuLong()));   */
                    mName.setText(myHolder.getName());
                    mLat.setText("Lat:" + String.valueOf(myHolder.getuLat()));
                    mLong.setText("Long:" + String.valueOf(myHolder.getuLong()));
                    mRating.setVisibility(View.INVISIBLE);

                    byte[] decodedString = Base64.decode(myHolder.getmImgUrl(), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    mImage.setImageBitmap(decodedByte);

                    return mView;
                }
                if (marker.getTag()=="place") {
                    MarkerHolder myHolder = markerHolderMap.get(marker);
                    mName.setText(myHolder.getmName());
                    mLat.setText(String.valueOf(myHolder.getmLat()));
                    mLong.setText(String.valueOf(myHolder.getmLong()));
                    try {
                        mImage.setImageBitmap(getBitmapFromUri(Uri.parse(myHolder.getmUri())));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mRating.setRating(Float.parseFloat(myHolder.getmVote()));
                    return mView;
                }

                if (marker.getTag()=="placeOnServer") {
                    MarkerHolder pHolder = placeHolderMap.get(marker);
                    mName.setText(pHolder.getmName());
                    mLat.setText(String.valueOf(pHolder.getmLat()));
                    mLong.setText(String.valueOf(pHolder.getmLong()));

                    Picasso.with(MapsActivity.this)
                            .load(pHolder.getmImgUrl())
                            .into(mImage);

                    return mView;
                }
                else
                    return null;
            }
        });
    }

 /** Methode pour creer un bitmap a partir d'un Uri **/
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        if (uri != null) {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
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
        Log.d("Ici ","buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d("onConnected ","avant CheckIfOrganizer");
//        CheckIfOrganizer();

        if (mLocation!=null) {

            LatLng latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11);
            mMap.animateCamera(cameraUpdate);
            UploadProfileTwo(mLocation);
            getUsers();
            getMeetingPlaces();
        }
        else
            Log.d("onConnected mLocation ="," Null");

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        requestLocationUpdates();
    }

    private void UpdateProfile(Location loc) {

        DatabaseReference dbref = mDatabase.child(cuGroup).child("Users_Markers");
        String UserId = cuName+"@"+cuGroup;
        dbref.child(UserId).child("uLat").setValue(loc.getLatitude());
        dbref.child(UserId).child("uLong").setValue(loc.getLongitude());
        Toast.makeText(this, "Latitude and Longitude updated..", Toast.LENGTH_SHORT).show();
    }

    private void getUsers(){
        DatabaseReference dbref = mDatabase.child(cuGroup).child("Users_Markers");
        Log.d("getUsers()", "yes");
        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                float userIcon;
                String Tag;
                if (!userHolderMap.isEmpty()) {
                    for (HashMap.Entry<Marker,User> entry: userHolderMap.entrySet() ) {
                        entry.getKey().remove();}
                    userHolderMap.clear();}

                for (DataSnapshot data: dataSnapshot.getChildren()){
                    User user = data.getValue(User.class);
                    if (user.getName().equals(cuName))
                        userIcon = BitmapDescriptorFactory.HUE_BLUE;
                    else
                        userIcon = BitmapDescriptorFactory.HUE_RED;

                    MarkerOptions options = new MarkerOptions().position(new LatLng(user.getuLat(), user.getuLong()))
                                                               .icon(BitmapDescriptorFactory.defaultMarker(userIcon));

                    Marker mMarker = mMap.addMarker(options);
                    mMarker.setTag("user");
                    userHolderMap.put(mMarker,user);
//                    mMarker.showInfoWindow();
//                    Log.d("getUsers name: " + user.getName(),"Marker: " + mMarker);
                }
//                Log.d("getUser Children Count ", String.valueOf(dataSnapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,this);
        Log.d("requestLocationUpdates"," .");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("onLocationChanged"," .");
//        UpdateProfile(location);

        if (mLocation == null){
            UploadProfileTwo(location);

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 11);
            mMap.animateCamera(cameraUpdate);
            getUsers();
            getMeetingPlaces();
            mLocation = location;}
        else
            UpdateProfile(location);
    }

    /**
     * Méthode pour sauvegarder le profle de l'utilisateur courant dans Firebase
     */
    private void UploadProfileTwo(Location location) {

//        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location!=null) {
            User UserMarker = new User(cuName, cuGroup, cuOrganizer, location.getLatitude(), location.getLongitude(), cuImage);
            DatabaseReference dbref = mDatabase.child(cuGroup).child("Users_Markers");
            String UserId = cuName + "@" + cuGroup;
            dbref.child(UserId).setValue(UserMarker);
            Log.d("mLocation =","Not Null");
            Log.d("Profile ", "uploaded");
            Toast.makeText(this, "Profile uploaded to Firebase!", Toast.LENGTH_SHORT).show();
        }
        else
            Log.d("mLocation ="," Null");
/*        getUsers();
        getMeetingPlaces();*/
    }
    @Override
    protected void onStart() {
        Log.d("onStart"," .");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.d("onStop"," .");
        super.onStop();
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        Log.d("onResume"," .");
        super.onResume();
        if (mGoogleApiClient.isConnected())
            requestLocationUpdates();
    }

    @Override
    protected void onPause() {
        Log.d("onPause"," .");
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("onConnectionSuspended"," .");
        if (i == CAUSE_SERVICE_DISCONNECTED) {
            Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        } else if (i == CAUSE_NETWORK_LOST) {
            Toast.makeText(this, "Network lost. Please re-connect.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("onConnectionFailed"," .");
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
            Toast.makeText(getApplicationContext(),"Sorry. Location services not available to you", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void setTextDate(int year, int month, int day, String mdate) {
        if (mdate.equals("debut") )
            datedebut.setText(day+"/"+(month+1)+"/"+year);
        if (mdate.equals("fin") )
            datefin.setText(day+"/"+(month+1)+"/"+year);
    }

    @Override
    public void setTextTime(int hourOfDay, int minute, String mtime) {
        if (mtime.equals("debut")) {
            if (minute < 10)
                timedebut.setText(hourOfDay + ":0" + minute);
            else
                timedebut.setText(hourOfDay + ":" + minute);
        }
        if (mtime.equals("fin")) {
            if (minute<10)
                timefin.setText(hourOfDay + ":0" + minute);
            else
                timefin.setText(hourOfDay + ":" + minute);
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Recherche du chemin ","en cours...", true);

        if (mList != null)
            mList.clear();

        if (mPolyline != null)
                mPolyline.remove();
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> route) {
        progressDialog.dismiss();
        ((TextView) findViewById(R.id.tvDuration)).setText(route.get(0).getrDuration().text);
        ((TextView) findViewById(R.id.tvDistance)).setText(route.get(0).getrDistance().text);

        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(11);

        for (int i = 0; i < route.get(0).getPoints().size(); i++)
            polylineOptions.add(route.get(0).getPoints().get(i));

        mPolyline = mMap.addPolyline(polylineOptions);
    }

    @Override
    public void onClick(View v) {

        DialogFragment Datepicker = new DatePickerFragment();
        DialogFragment Timepicker = new TimePickerFragment();
        Bundle extras = new Bundle();

        switch(v.getId()){
            case R.id.btn_ddebut:
                extras.putString("date","debut");
                Datepicker.setArguments(extras);
                Datepicker.show(getFragmentManager(), "datePicker");
                break;
            case R.id.btn_dfin:
                extras.putString("date","fin");
                Datepicker.setArguments(extras);
                Datepicker.show(getFragmentManager(), "datePicker");
                break;
            case R.id.btn_hdebut:
                extras.putString("time","debut");
                Timepicker.setArguments(extras);
                Timepicker.show(getFragmentManager(), "timePicker");
                break;
            case R.id.btn_hfin:
                extras.putString("time","fin");
                Timepicker.setArguments(extras);
                Timepicker.show(getFragmentManager(),"timePicker");
                break;
        }
    }


}


