package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;

public class BookViewHolder extends RecyclerView.ViewHolder {

    private View mView;
   public TextView add,mName;
   public CardView bookCard;
   public TextView tvAddBooks,tvRemoveBooks;


    public BookViewHolder(View itemView){
        super(itemView);
        mView=itemView;
        add=mView.findViewById(R.id.add);
        bookCard=mView.findViewById(R.id.book_card);
        mName=mView.findViewById(R.id.book_name);
    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String author, String publication, String img,
                           Integer originalPrice, Integer discountedPrice, Integer discount,
                           Boolean availability, Context ctx,Integer count){



        TextView mAuthor=mView.findViewById(R.id.book_author);
        TextView mPublication=mView.findViewById(R.id.book_publication);
        TextView mOriginalPrice=mView.findViewById(R.id.book_original_price);
        TextView mDiscountedPrice=mView.findViewById(R.id.book_discounted_price);
        TextView mDiscount=mView.findViewById(R.id.book_discount);
        TextView mAvailability=mView.findViewById(R.id.book_availability);
        tvAddBooks = mView.findViewById(R.id.add_books);
        tvRemoveBooks = mView.findViewById(R.id.remove_books);
        TextView tvBookCount = mView.findViewById(R.id.book_count);

        tvBookCount.setText("Count: "+count);

        ImageView mImg=mView.findViewById(R.id.book_img);
        mOriginalPrice.setPaintFlags(mOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        mName.setText(name);


        mAuthor.setText("Author: "+author);
        mPublication.setText(publication);
        mOriginalPrice.setText(originalPrice.toString());
        mDiscountedPrice.setText("₹ "+discountedPrice.toString());
        mDiscount.setText(discount.toString()+"% off");

        if (img!=null && !img.isEmpty())
        Picasso.get()
                .load(img)
                .into(mImg);

        if (availability) {
                mAvailability.setText("AVAILABLE");
            mAvailability.setTextColor(ContextCompat.getColor(ctx,R.color.green));

        }
        else {
            mAvailability.setText("UNAVAILABLE");
            mAvailability.setTextColor(ContextCompat.getColor(ctx,R.color.red));
        }




    }
}
