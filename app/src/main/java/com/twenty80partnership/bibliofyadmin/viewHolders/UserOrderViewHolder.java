package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofyadmin.R;

public class UserOrderViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public CardView orderCard;

    public UserOrderViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        orderCard = mView.findViewById(R.id.order_card);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String orderId, Integer count) {

        TextView tOrderId =mView.findViewById(R.id.order_id);
        TextView tCount = mView.findViewById(R.id.count);

        tOrderId.setText(orderId);
        if (count!=null){
            if (count==1){
                tCount.setText(count.toString()+" Item");

            }
            else {
                tCount.setText(count.toString()+" Items");

            }

        }
        else {
            tCount.setText("loading...");

        }

    }

}
