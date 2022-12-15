package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elyeproj.loaderviewlibrary.LoaderImageView;
import com.elyeproj.loaderviewlibrary.LoaderTextView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;

import androidx.recyclerview.widget.RecyclerView;

public class CartShortViewHolder extends RecyclerView.ViewHolder {

    private View mView;

    public CartShortViewHolder(View itemView) {
        super(itemView);
        mView = itemView;

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String img, Integer discountedPrice) {

        TextView itemName = mView.findViewById(R.id.name);
        TextView itemPrice = mView.findViewById(R.id.price);
        ImageView itemPic = mView.findViewById(R.id.pic);

        if (img != null && !img.equals(""))
            Picasso.get().load(img).into(itemPic);

        Log.d("img: ", img);
        itemName.setText(name);
        itemPrice.setText("₹ " + discountedPrice);
    }

}
