package com.twenty80partnership.bibliofyadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;
import com.twenty80partnership.bibliofyadmin.models.StationaryItem;

import java.util.ArrayList;

public class StationaryItemRecyclerAdapter extends RecyclerView.Adapter<StationaryItemRecyclerAdapter.ViewHolder> {


    ArrayList<StationaryItem> stationaryItemArrayList = new ArrayList<>();

    public void loadData(ArrayList<StationaryItem> stationaryItemArrayList){

        this.stationaryItemArrayList = stationaryItemArrayList;

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView itemImage;
        public TextView itemTitle;
        public TextView itemPublication;
        public TextView itemPrice;


        public ViewHolder(View itemView) {
            super(itemView);
            itemImage =
                    (ImageView) itemView.findViewById(R.id.img);
            itemTitle =
                    (TextView) itemView.findViewById(R.id.title);
            itemPublication =
                    (TextView) itemView.findViewById(R.id.publication);
            itemPrice =
                    itemView.findViewById(R.id.price);

        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.order_data_book_row, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.itemTitle.setText(stationaryItemArrayList.get(i).getName());


        String location = stationaryItemArrayList.get(i).getItemLocation();
        String[] split = location.split("/");

        viewHolder.itemPublication.setText(split[1]);
        viewHolder.itemPrice.setText(stationaryItemArrayList.get(i).getDiscountedPrice().toString());

        if (stationaryItemArrayList.get(i).getImg()!=null && !stationaryItemArrayList.get(i).getImg().equals("")){
            Picasso.get().load(stationaryItemArrayList.get(i).getImg()).into(viewHolder.itemImage);
        }
        else{
            Picasso.get().load(R.drawable.sample_book).into(viewHolder.itemImage);
        }

    }

    @Override
    public int getItemCount() {
        return stationaryItemArrayList.size();
    }

}