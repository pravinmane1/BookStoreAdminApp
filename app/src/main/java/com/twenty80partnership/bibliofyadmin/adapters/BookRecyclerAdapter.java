package com.twenty80partnership.bibliofyadmin.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.R;
import com.twenty80partnership.bibliofyadmin.models.Book;

import java.util.ArrayList;

public class BookRecyclerAdapter extends RecyclerView.Adapter<BookRecyclerAdapter.ViewHolder> {


    ArrayList<Book> bookArrayList = new ArrayList<>();

    public void loadData(ArrayList<Book> bookArrayList){

        this.bookArrayList = bookArrayList;

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
        viewHolder.itemTitle.setText(bookArrayList.get(i).getName());
        viewHolder.itemPublication.setText(bookArrayList.get(i).getPublication());
        viewHolder.itemPrice.setText(bookArrayList.get(i).getDiscountedPrice().toString());

        if (bookArrayList.get(i).getImg()!=null && !bookArrayList.get(i).getImg().equals("")){
            Picasso.get().load(bookArrayList.get(i).getImg()).into(viewHolder.itemImage);
        }
        else{
            Picasso.get().load(R.drawable.sample_book).into(viewHolder.itemImage);
        }

    }

    @Override
    public int getItemCount() {
        return bookArrayList.size();
    }

}