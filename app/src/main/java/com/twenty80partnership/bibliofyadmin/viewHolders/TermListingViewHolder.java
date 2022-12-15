package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofyadmin.R;

public class TermListingViewHolder extends RecyclerView.ViewHolder {

    public CardView itemCard;
    public ImageView more;
    private View mView;

    public TermListingViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        itemCard = mView.findViewById(R.id.item_card);
        more = mView.findViewById(R.id.more);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String mode) {

        TextView itemName = mView.findViewById(R.id.name);

        itemName.setText(name);

        if (mode.equals("select")) {
            more.setVisibility(View.GONE);
            //more.setVisibility(View.GONE);
        }

    }
}

