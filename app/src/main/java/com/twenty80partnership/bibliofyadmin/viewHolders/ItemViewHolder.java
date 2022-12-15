package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;

public class ItemViewHolder  extends RecyclerView.ViewHolder {

    private View mView;
    public CardView itemCard;
    public ImageView more;

    public ItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        itemCard = mView.findViewById(R.id.item_card);
        more = mView.findViewById(R.id.more);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String pic, Context ctx,Float priority,String mode) {

        TextView itemName=mView.findViewById(R.id.name);

        if (priority!=null)
            name = priority+" : "+name;

        itemName.setText(name);

        if (mode.equals("books") || mode.equals("menuItem") ){
            ImageView itemPic=mView.findViewById(R.id.pic);
            Picasso.get().load(pic).into(itemPic);

        }
        else if (mode.equals("codes")){
            mView.findViewById(R.id.views_container).getLayoutParams().height=150;
        }
        else if (mode.equals("select")){
            more.setVisibility(View.GONE);
            mView.findViewById(R.id.views_container).getLayoutParams().height=200;
            //more.setVisibility(View.GONE);
            }

    }

    public void setDetails(String name, String pic, Context ctx,Float priority) {

        TextView itemName=mView.findViewById(R.id.name);

        if (priority!=null)
            name = priority+" : "+name;

        itemName.setText(name);

            ImageView itemPic=mView.findViewById(R.id.pic);

            if (pic!=null && !pic.equals(""))
            Picasso.get().load(pic).into(itemPic);

    }

}

