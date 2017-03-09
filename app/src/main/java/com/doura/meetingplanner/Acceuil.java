package com.doura.meetingplanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class Acceuil extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;
    private ImageView mImage;
    private Uri mImageUri;
    private EditText Name_T;
    private EditText Group_T;
    byte[] img_Blob;
    DatabaseHelper myDB;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceuil);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Profiles");
        myDB = DatabaseHelper.getInstance(this);

        mProgress = new ProgressDialog(this);
        mImage = (ImageView)findViewById(R.id.imageView);
        Name_T = (EditText)findViewById(R.id.usaername);
        Group_T = (EditText) findViewById(R.id.usergroup);
        ImageButton btn_photo = (ImageButton)findViewById(R.id.userphoto);
        Button btn_valider = (Button)findViewById(R.id.btn_valider);


        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_REQUEST);
            }
        });


        btn_valider.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Validerprofile()) {

                    //Insertion du profile dans SQLite
                    boolean isInserted = myDB.insertData(Name_T.getText().toString(), Group_T.getText().toString(), img_Blob);
                    if (isInserted) {
                        Toast.makeText(Acceuil.this, "Données inserrées dans SQLite!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Acceuil.this,MapsActivity.class);
                        startActivity(intent);
                    }
                    else
                        Toast.makeText(Acceuil.this, "Erreur d'insertion dans SQLite!", Toast.LENGTH_LONG).show();

                    //Insertion du profile dans Firebase
                    startPostingProfile();


                }
            }
        });

    }


    private void startPostingProfile() {
        mProgress.setMessage("Uploading to Firebase...");
        mProgress.show();
        final String name_val = Name_T.getText().toString().trim();
        final String group_val = Group_T.getText().toString().trim();

        final DatabaseReference newPost = mDatabase.push();                           //.push() create empty node with unique ID
        newPost.child("Nom").setValue(name_val);
        newPost.child("Groupe").setValue(group_val);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot data: dataSnapshot.getChildren()){
 //                   Toast.makeText(Acceuil.this, "data.getChildrenCount():  " + String.valueOf(data.getChildrenCount()) , Toast.LENGTH_SHORT).show();

                    if (data.child("Groupe").getValue().equals(group_val)) {
                        Toast.makeText(Acceuil.this, "Le groupe existe deja!" , Toast.LENGTH_SHORT).show();
                        newPost.child("Organisateur").setValue("false");

                    } else {
                        Toast.makeText(Acceuil.this, "Vous etes oragnisateur du groupe:  " + group_val, Toast.LENGTH_SHORT).show();
                        newPost.child("Organisateur").setValue("true");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Creation dans la base Storage du dossier Images_Profiles et ajout des noms des images
    //    StorageReference filepath = mStorage.child("Images_Profiles").child(mImageUri.getLastPathSegment());
/*        StorageReference filepath = mStorage.child("Images_Profiles").child(Name_T.getText().toString());
        filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

            }
        });*/

        mProgress.dismiss();
    }

    //Affichage de la photo et compression
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            Bundle bundle = data.getExtras();
            mImageUri = (Uri)bundle.get(Intent.EXTRA_STREAM);

    //        mImageUri = data.getData();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            mImage.setImageBitmap(bitmap);
            ByteArrayOutputStream CompressedImg = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, CompressedImg);
            img_Blob = CompressedImg.toByteArray();

        }
    }

    //Valider si une partie du profile (nom+groupe+photo) est saisie
    public boolean Validerprofile(){

            EditText n_text = (EditText)findViewById(R.id.usaername);
            EditText g_text = (EditText)findViewById(R.id.usergroup);

            String name = n_text.getText().toString();
            String group = g_text.getText().toString();

   //         if (!isNameEmpty() && !isGroupEmpty() && !isImageEmpty()){
                if (!isNameEmpty() && !isGroupEmpty()){
                User user = new User(name,group,img_Blob);
                return true;
            }
            else
                return false;

    }

    //Verifier si le nom est saisi
    public boolean isNameEmpty(){
        EditText un_text = (EditText) findViewById(R.id.usaername);
        if (un_text.getText().toString().matches("")) {
            Toast.makeText(getApplicationContext(), "Vous devez saisir votre nom!", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
            return false;
    }

    //Verifier si le groupe est saisi
    public boolean isGroupEmpty(){
        EditText un_group = (EditText) findViewById(R.id.usergroup);
        if (un_group.getText().toString().matches("")) {
            Toast.makeText(getApplicationContext(), "Vous devez saisir le groupe!", Toast.LENGTH_SHORT).show();
            return true;
        }
        else
            return false;
    }

    //Verifier si l'image est prise
    public  boolean isImageEmpty(){
        if (img_Blob == null ||img_Blob.length == 0) {
            Toast.makeText(this, "Vous devez prendre une photo!", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }


}
