package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.tv.TvContentRating;
import android.os.strictmode.SqliteObjectLeakedViolation;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofyadmin.R;
import com.twenty80partnership.bibliofyadmin.models.Date;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public CardView orderCard;
    public LinearLayout orderCard2;
    public TextView statusT;

    public OrderViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        orderCard = mView.findViewById(R.id.order_card);
        orderCard2 = mView.findViewById(R.id.order_card2);
        statusT = mView.findViewById(R.id.status);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(Long timeAdded, String name, String phone, String status, String value,
                           String address, String method, Context ctx,String orderId,int daysForDelivery) {

        TextView timeAddedT =mView.findViewById(R.id.time_added);
        TextView nameT = mView.findViewById(R.id.name);
        TextView phoneT = mView.findViewById(R.id.phone);
        statusT = mView.findViewById(R.id.status);
        TextView valueT = mView.findViewById(R.id.value);
        TextView addressT = mView.findViewById(R.id.address);
        TextView methodT = mView.findViewById(R.id.method);
        TextView daysLeftT = mView.findViewById(R.id.days_left);
        TextView oIdT = mView.findViewById(R.id.o_id);

        oIdT.setText("order id: "+orderId);

        Date dateDelivery = new Date();

        dateDelivery.incrementDate(timeAdded,daysForDelivery);

        Long dateDeliveryLong = dateDelivery.createAbsoluteDate((long)dateDelivery.getNewYear(),(long)dateDelivery.getNewMonth()
                ,(long)dateDelivery.getNewDay());

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        java.util.Date currentDate = Calendar.getInstance().getTime();

        String todayDate = dateFormat.format(currentDate);


        Log.d("difflog",todayDate+" "+dateDeliveryLong);
        int diff = dateDelivery.differnceBetweenDates(Long.valueOf(todayDate),dateDeliveryLong);

        daysLeftT.setText("Days Left: "+diff);

        String date = new Date().convertLongDateIntoSplit(timeAdded);
        timeAddedT.setText(date);
        nameT.setText(name);
        phoneT.setText("+91 "+phone);
        statusT.setText(status);
        switch (status){
            case "Placed":
            statusT.setBackgroundColor(ContextCompat.getColor(ctx, R.color.yellow));
                break;
            case "Confirmed":
                statusT.setBackgroundColor(ContextCompat.getColor(ctx, R.color.aqua));
                break;
            case "Dispatched":
                statusT.setBackgroundColor(ContextCompat.getColor(ctx, R.color.neongreen));
                break;
            case "Delivered":
                statusT.setBackgroundColor(ContextCompat.getColor(ctx, R.color.light_green));
                break;
            case "Cancelled":
                statusT.setBackgroundColor(ContextCompat.getColor(ctx, R.color.red));
                break;
        }
        valueT.setText("Value : ₹"+value);
        addressT.setText("Address : "+address);
        methodT.setText("Method : "+method);



    }

}
