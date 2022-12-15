package com.twenty80partnership.bibliofyadmin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.twenty80partnership.bibliofyadmin.models.Order;
import com.twenty80partnership.bibliofyadmin.viewHolders.UserOrderViewHolder;

public class UserOrdersActivity extends AppCompatActivity {

    RecyclerView itemList;
    DatabaseReference userOrdersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_orders);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        itemList.setLayoutManager(mLayoutManager);


        userOrdersRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(getIntent().getStringExtra("uId"));
        Query query = userOrdersRef.orderByChild("timeAdded");

        firebaseSearch(query);

    }


    public void firebaseSearch(Query q){

        Log.d("recycle debug","firebasesearch");

        final FirebaseRecyclerAdapter<Order, UserOrderViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Order, UserOrderViewHolder>(
                Order.class, R.layout.user_order_row, UserOrderViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final UserOrderViewHolder viewHolder, final Order model, final int position) {

                String key = getRef(position).getKey();

                viewHolder.setDetails(key,model.getPriceDetails().getCount());

                //remove button listener
                viewHolder.orderCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(UserOrdersActivity.this,UserOrderDetailsActivity.class);
                        intent.putExtra("order",model);
                        intent.putExtra("uId",getIntent().getStringExtra("uId"));
                        startActivity(intent);

                    }
                });


            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
