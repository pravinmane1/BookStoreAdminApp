package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;

public class UserCardViewHolder extends RecyclerView.ViewHolder {

    View mView;
    public CardView userCard;


    public UserCardViewHolder(View itemView){
        super(itemView);
        mView = itemView;
        userCard = mView.findViewById(R.id.user_card);
    }

    public void setDetails(String photo, Context ctx,String name,String email,String uId){

        ImageView mImg = mView.findViewById(R.id.img);

        if (photo!=null && !photo.equals("")){

            Picasso.get()
                    .load(photo)
                    .into(mImg);
        }
        else {
            mImg.setImageResource(R.drawable.userdisplay);
        }

        TextView mName = mView.findViewById(R.id.name);
        TextView mEmail = mView.findViewById(R.id.email);
        TextView mUid = mView.findViewById(R.id.uid);

        mName.setText(name);
        mEmail.setText(email);
        mUid.setText(uId);

    }
}
