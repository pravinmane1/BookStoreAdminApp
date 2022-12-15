package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofyadmin.R;

public class CourseViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public CardView itemCard;
    public ImageView more;

    public CourseViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        itemCard = mView.findViewById(R.id.item_card);
        more = mView.findViewById(R.id.more);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name,Integer years,Float priority,String currentSem) {

        TextView nameView = mView.findViewById(R.id.name);
        TextView yearView = mView.findViewById(R.id.years);
        TextView priorityView = mView.findViewById(R.id.topic);
        TextView currentSemView = mView.findViewById(R.id.current_sem);

        nameView.setText(name);
        yearView.setText("Years: "+years.toString());
        currentSemView.setText("Current sem: "+currentSem);

        if (priority!=null)
        priorityView.setText("Priority: "+priority.toString());


    }

}

