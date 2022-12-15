package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.elyeproj.loaderviewlibrary.LoaderImageView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;

public class StationaryItemViewHolder extends RecyclerView.ViewHolder {
    public View mView;
    public Button add;
    public ImageView plus,minus;
    public TextView mQuantity;
    public TextView removeItem;
    public TextView tvAddBooks,tvRemoveBooks;

    public StationaryItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        add = mView.findViewById(R.id.add);
        plus=mView.findViewById(R.id.plus);
        minus=mView.findViewById(R.id.minus);

      //  mQuantity=mView.findViewById(R.id.s_quantity);
        //removeItem=mView.findViewById(R.id.remove_item);
    }

    public  void setDetails(String brand,String pic,Context ctx,String atr1,String atr2,String atr3,String atr4,
                            Integer discount,Integer discountedPrice,Integer originalPrice,Boolean availability,Integer count){

        LoaderImageView tImg = mView.findViewById(R.id.pic);
        TextView tBrand = mView.findViewById(R.id.brand);
        TextView tAtr1 = mView.findViewById(R.id.atr1);
        TextView tAtr2 = mView.findViewById(R.id.atr2);
        TextView tAtr3 = mView.findViewById(R.id.atr3);
        TextView tAtr4 = mView.findViewById(R.id.atr4);

        LinearLayout atr1L = mView.findViewById(R.id.atr1_layout);
        LinearLayout atr3L = mView.findViewById(R.id.atr3_layout);


        LinearLayout atr2L = mView.findViewById(R.id.atr2_layout);
        View line12 = mView.findViewById(R.id.SplitLine_ver1);

        LinearLayout atr4L = mView.findViewById(R.id.atr4_layout);
        View middle = mView.findViewById(R.id.splitline);
        View line34 = mView.findViewById(R.id.SplitLine_ver2);

        LinearLayout atr12L = mView.findViewById(R.id.atr12);
        LinearLayout atr34L = mView.findViewById(R.id.atr34);

        tvAddBooks = mView.findViewById(R.id.add_books);
        tvRemoveBooks = mView.findViewById(R.id.remove_books);
        TextView tvBookCount = mView.findViewById(R.id.book_count);

        tvBookCount.setText("Count: "+count);

        tBrand.setText(brand);

        LinearLayout.LayoutParams expandParam = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                4
        );


        LinearLayout.LayoutParams reduceParam = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                2
        );


        if (atr1!=null && !atr1.equals("")){
            tAtr1.setText(atr1);
            atr12L.setVisibility(View.VISIBLE);
        }
        else {
            atr12L.setVisibility(View.GONE);
        }

        if (atr2!=null && !atr2.equals("")){
            tAtr2.setText(atr2);
            atr12L.setVisibility(View.VISIBLE);
            line12.setVisibility(View.VISIBLE);
            atr2L.setVisibility(View.VISIBLE);
            atr1L.setLayoutParams(reduceParam);

        }
        else {
            atr1L.setLayoutParams(expandParam);
            line12.setVisibility(View.GONE);
            atr2L.setVisibility(View.GONE);
        }

        if (atr3!=null && !atr3.equals("")){
            tAtr3.setText(atr3);
            middle.setVisibility(View.VISIBLE);
            atr34L.setVisibility(View.VISIBLE);

        }
        else {
            atr34L.setVisibility(View.GONE);
            middle.setVisibility(View.GONE);
        }

        if (atr4!=null && !atr4.equals("")){
            tAtr4.setText(atr4);
            line34.setVisibility(View.VISIBLE);
            atr4L.setVisibility(View.VISIBLE);
            atr3L.setLayoutParams(reduceParam);
        }
        else {
            line34.setVisibility(View.GONE);
            atr4L.setVisibility(View.GONE);
            atr3L.setLayoutParams(expandParam);
        }

        TextView tDiscountedPrice = mView.findViewById(R.id.s_discounted_price);
        TextView tDiscount = mView.findViewById(R.id.s_discount);
        TextView tOriginalPrice = mView.findViewById(R.id.s_original_price);
        tOriginalPrice.setPaintFlags(tOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        if(pic!=null && !pic.equals(""))
        Picasso.get().load(pic).into(tImg);

        tDiscountedPrice.setText("₹ "+String.valueOf(discountedPrice));
        tDiscount.setText(discount+"% off");
        tOriginalPrice.setText("₹ "+(originalPrice));
    }
}

