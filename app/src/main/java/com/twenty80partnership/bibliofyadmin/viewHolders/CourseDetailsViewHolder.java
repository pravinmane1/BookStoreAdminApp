package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;

public class CourseDetailsViewHolder extends RecyclerView.ViewHolder {

    public CardView cvItemCard;
    public ImageView ivMore;
    public SwitchCompat swVisibility;
    private View mView;

    public CourseDetailsViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        cvItemCard = mView.findViewById(R.id.item_card);
        ivMore = mView.findViewById(R.id.more);
        swVisibility = mView.findViewById(R.id.sw_visibility);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String pic, Float priority, String mode) {

        TextView itemName = mView.findViewById(R.id.name);
        if (priority != null)
            name = priority + " : " + name;

        itemName.setText(name);

        if (mode.equals("books") || mode.equals("menuItem")) {
            ImageView itemPic = mView.findViewById(R.id.pic);
            Picasso.get().load(pic).into(itemPic);
        } else if (mode.equals("select")) {
            ivMore.setVisibility(View.GONE);
            mView.findViewById(R.id.views_container).getLayoutParams().height = 200;
        }
    }
}

