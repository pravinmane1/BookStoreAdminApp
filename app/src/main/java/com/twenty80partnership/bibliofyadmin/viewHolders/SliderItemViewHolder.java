package com.twenty80partnership.bibliofyadmin.viewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;

public class SliderItemViewHolder  extends RecyclerView.ViewHolder {

    public View mView;
    public ImageView more;

    public SliderItemViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setDetails(String img, Context ctx,String title,Float priority) {

        TextView itemTop=mView.findViewById(R.id.top);
        ImageView itemImg = mView.findViewById(R.id.img);
        TextView itemTitle=mView.findViewById(R.id.title);


        itemTop.setText("Priority: "+priority.toString());

        if(img!=null&&!img.equals(""))
            Picasso.get().load(img).into(itemImg);
        else
            itemImg.setImageDrawable(null);

        if(title!=null)
        itemTitle.setText(title);

    }

}

