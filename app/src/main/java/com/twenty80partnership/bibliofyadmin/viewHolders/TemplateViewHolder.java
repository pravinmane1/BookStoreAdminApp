package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofyadmin.R;

public class TemplateViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public CardView templateCard;
    public TextView tvPublication;

    public TemplateViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        templateCard = mView.findViewById(R.id.template_card);
        tvPublication = mView.findViewById(R.id.tv_publication);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String publication,float priority) {

        TextView itemName=mView.findViewById(R.id.tv_name);
        itemName.setText(name);

       tvPublication= mView.findViewById(R.id.tv_publication);
        tvPublication.setText(publication);

        TextView itemPriority = mView.findViewById(R.id.tv_priority);
        itemPriority.setText("Priority: "+priority);



    }

}
