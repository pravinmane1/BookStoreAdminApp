package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofyadmin.R;

public class DeliveryViewHolder extends RecyclerView.ViewHolder {

    public View view;
    public DeliveryViewHolder(@NonNull View itemView) {
        super(itemView);
        view = itemView;
    }

    public void setDetails(String pin,int daysForDelivery,int deliveryCharges){

        TextView tvPincode,tvDaysForDelivery,tvDeliveryCharges;

        tvPincode = view.findViewById(R.id.tv_pincode);
        tvDaysForDelivery = view.findViewById(R.id.tv_days_for_delivery);
        tvDeliveryCharges = view.findViewById(R.id.tv_delivery_charges);

        tvPincode.setText(pin);
        tvDaysForDelivery.setText(String.valueOf(daysForDelivery));
        tvDeliveryCharges.setText("₹ "+deliveryCharges);
    }
}
