package com.doura.meetingplanner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

public class PostActivity extends AppCompatActivity {

    private ImageView mImage;
    private EditText mTitle;
    private EditText mDesc;
    private String mImageEncoded;
    private String mGroup;
    private String mUser;

    private static final int CAMERA_REQUEST = 2001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Bundle extras = getIntent().getExtras();
        mGroup = extras.getString("group");
        mUser = extras.getString("user");
        mImage = (ImageView) findViewById(R.id.blog_img);
        mTitle = (EditText) findViewById(R.id.blog_name);
        mDesc = (EditText) findViewById(R.id.blog_desc);
        Button mSubmitbtn = (Button) findViewById(R.id.blog_postbtn);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,CAMERA_REQUEST);
            }
        });

        mSubmitbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostBlog();
            }
        });

    }

    private void PostBlog() {

        final String titletext = mTitle.getText().toString().trim();
        final String desctext = mDesc.getText().toString().trim();

        if (!TextUtils.isEmpty(titletext) && !TextUtils.isEmpty(desctext) && mImageEncoded!=null){
            DatabaseReference mFirebase = FirebaseDatabase.getInstance().getReference().child(mGroup).child("Blogs");

            Blog mBlog = new Blog(titletext,desctext,mImageEncoded,mUser);
            String mId = mFirebase.push().getKey();
            mFirebase.child(mId).setValue(mBlog).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(PostActivity.this, "Blog publié...", Toast.LENGTH_SHORT).show();
                    goBackToBlogs();
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, "Blog n'est pas publié! Svp essayez plus tard...", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
            Toast.makeText(this, "Veuillez remplir tous les champs!", Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode==CAMERA_REQUEST && resultCode==RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            mImage.setImageBitmap(bitmap);
            ByteArrayOutputStream CompressedImg = new ByteArrayOutputStream();
            if (bitmap != null)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, CompressedImg);
            mImageEncoded = Base64.encodeToString(CompressedImg.toByteArray(),Base64.DEFAULT);
        }
    }

    private void goBackToBlogs() {
        Intent intent = new Intent(this, BlogListActivity.class);
        Bundle extras = new Bundle();
        extras.putString("group",mGroup);
        extras.putString("user",mUser);
        intent.putExtras(extras);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
