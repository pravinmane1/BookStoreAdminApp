package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.elyeproj.loaderviewlibrary.LoaderImageView;
import com.elyeproj.loaderviewlibrary.LoaderTextView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;

public class CartItemViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public LinearLayout wishlist, remove,wishlistRemoveLayout;
    public View splitline;
    public TextView quantity;
    public ImageView plus,minus;

    public CartItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        wishlist = mView.findViewById(R.id.add_to_wishlist);
        remove = mView.findViewById(R.id.remove);
        wishlistRemoveLayout = mView.findViewById(R.id.wishlist_remove_layout);
        splitline = mView.findViewById(R.id.splitline);
        quantity = mView.findViewById(R.id.item_quantity);
        plus = mView.findViewById(R.id.plus);
        minus = mView.findViewById(R.id.minus);
    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String author, String publication, String img,
                           Integer originalPrice, Integer discountedPrice, Integer discount,
                           Context ctx, Integer quantity,String type) {

        LoaderTextView itemName=mView.findViewById(R.id.item_name);
        LoaderTextView itemAuthor=mView.findViewById(R.id.item_author);
        LoaderTextView itemPublication=mView.findViewById(R.id.item_publication);
        LoaderImageView itemImg=mView.findViewById(R.id.item_img);
        LoaderTextView itemOriginalPrice=mView.findViewById(R.id.item_original_price);
        LoaderTextView itemDiscountedPrice=mView.findViewById(R.id.item_discounted_price);
        LoaderTextView itemDiscount=mView.findViewById(R.id.item_discount);
        //LoaderTextView itemQuantity=mView.findViewById(R.id.item_quantity);
        itemOriginalPrice.setPaintFlags(itemOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        itemName.setText(name);
        itemAuthor.setText(author);
        itemPublication.setText(publication);
        itemOriginalPrice.setText("₹ "+originalPrice.toString());
        itemDiscountedPrice.setText("₹ "+discountedPrice.toString());
        itemDiscount.setText(discount.toString()+"% off");
        Picasso.get().load(img).into(itemImg);

        if (type.equals("stationary")){
            mView.findViewById(R.id.quantity_layout).setVisibility(View.VISIBLE);
        }
        else {
            mView.findViewById(R.id.quantity_layout).setVisibility(View.GONE);

        }

    }

}
