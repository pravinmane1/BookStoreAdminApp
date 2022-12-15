package com.twenty80partnership.bibliofyadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;


import com.twenty80partnership.bibliofyadmin.adapters.BookRecyclerAdapter;
import com.twenty80partnership.bibliofyadmin.adapters.StationaryItemRecyclerAdapter;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.models.StationaryItem;

import java.util.ArrayList;

public class OrderDataActivity extends AppCompatActivity {

    RecyclerView recyclerView,recyclerView2;
    RecyclerView.LayoutManager layoutManager,layoutManager2;
    BookRecyclerAdapter adapter;
    StationaryItemRecyclerAdapter stationaryItemRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_data);

        //books
        recyclerView = findViewById(R.id.recycler_view);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new BookRecyclerAdapter();
        adapter.loadData((ArrayList<Book>) getIntent().getSerializableExtra("bookArrayList"));
        recyclerView.setAdapter(adapter);

        //stationary
        recyclerView2 = findViewById(R.id.recycler_view2);

        layoutManager2 = new LinearLayoutManager(this);
        recyclerView2.setLayoutManager(layoutManager2);

        stationaryItemRecyclerAdapter = new StationaryItemRecyclerAdapter();
        stationaryItemRecyclerAdapter.loadData((ArrayList<StationaryItem>) getIntent().getSerializableExtra("stationaryItemArrayList"));
        recyclerView2.setAdapter(stationaryItemRecyclerAdapter);
    }
}
