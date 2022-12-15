package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.twenty80partnership.bibliofyadmin.R;

public class CodeViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    public CardView codeCard;

    public CodeViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        codeCard = mView.findViewById(R.id.code_card);

    }

    @SuppressLint("SetTextI18n")
    public void setDetails(String name, String code,float priority) {

        TextView itemName=mView.findViewById(R.id.name);
        itemName.setText(name);

       TextView itemCode= mView.findViewById(R.id.code);
       itemCode.setText(code);

        TextView itemPriority = mView.findViewById(R.id.topic);
        itemPriority.setText("Priority: "+priority);



    }

}
