package com.doura.meetingplanner;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by doura on 3/29/2017.
 */

public class BlogViewHolder extends RecyclerView.ViewHolder{

    private View mView;

    public BlogViewHolder(View itemView) {
        super(itemView);

        this.mView = itemView;
    }

    public void setTitle(String title){

        TextView mtitle = (TextView) mView.findViewById(R.id.row_title);
        mtitle.setText(title);
    }

    public void setDesc(String desc){
        TextView mDesc = (TextView) mView.findViewById(R.id.row_desc);
        mDesc.setText(desc);
    }

    public void setImage(String EncodedImage){
        byte[] decodedString = Base64.decode(EncodedImage, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        ImageView mImage = (ImageView) mView.findViewById(R.id.row_image);
        mImage.setImageBitmap(decodedByte);
    }

    public void setUserName(String username){
        TextView mUser = (TextView) mView.findViewById(R.id.row_user);
        mUser.setText(username);
    }
}
