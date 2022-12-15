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

public class CombinationViewHolder  extends RecyclerView.ViewHolder {

    private View mView;
    public CardView itemCard;
    public ImageView more;

    public CombinationViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        itemCard = mView.findViewById(R.id.item_card);
        more = mView.findViewById(R.id.more);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name) {

        TextView itemName = mView.findViewById(R.id.name);
        itemName.setText(name);
    }

}

