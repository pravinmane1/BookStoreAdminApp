package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofyadmin.models.Address;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.models.Order;
import com.twenty80partnership.bibliofyadmin.models.PriceDetails;
import com.twenty80partnership.bibliofyadmin.viewHolders.CartShortViewHolder;

public class UserOrderDetailsActivity extends AppCompatActivity {
    DatabaseReference bookDataRef;
    private RecyclerView itemList;
    TextView oId;
    private PriceDetails details;
    private DatabaseReference priceDetailsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_order_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        oId = findViewById(R.id.o_id);

        itemList = findViewById(R.id.recycler_view);
        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        itemList.setLayoutManager(mLayoutManager);

        Intent intent = getIntent();
        Order order =(Order) intent.getSerializableExtra("order");
        Log.d("debugOrder","in act: "+order.getOrderId());


        bookDataRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(getIntent().getStringExtra("uId"))
                .child(order.getOrderId()).child("orderData").child("books");
        priceDetailsRef= FirebaseDatabase.getInstance().getReference("UserOrders").child(getIntent().getStringExtra("uId"))
                .child(order.getOrderId()).child("priceDetails");


        oId.setText("Order ID : "+order.getOrderId());

        Query query = bookDataRef.orderByChild("timeAdded");

        showShortList(query);
        setAddress(order.getAddress());
        setPriceDetails();
    }

    private void setAddress(Address address) {
        ImageView remove,edit;
        TextView name,number,type,completeAddress;

        remove = findViewById(R.id.remove);
        edit = findViewById(R.id.edit);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        type = findViewById(R.id.type);
        completeAddress = findViewById(R.id.address);

        remove.setVisibility(View.GONE);
        edit.setVisibility(View.GONE);
        name.setText(address.getName());
        number.setText(address.getNumber());
        type.setText(address.getType());

        String combinedAddress = address.getBuildingNameNumber() +" "+
                address.getAreaRoad()  +" "+
                address.getCity()  +" "+
                address.getState() + "-"+
                address.getPincode();

        completeAddress.setText(combinedAddress);
    }


    private void setPriceDetails() {


        final TextView totalItems,amountTotal,amountDelivery,amountItems,amountSavings,payableAmount;
        totalItems = findViewById(R.id.total_items);
        amountTotal = findViewById(R.id.amount_total);
        amountDelivery = findViewById(R.id.amount_delivery);
        amountItems = findViewById(R.id.amount_items);
        amountSavings = findViewById(R.id.amount_savings);

        priceDetailsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("showing", "update price detail event listener called");

                details = dataSnapshot.getValue(PriceDetails.class);

                //set total items
                totalItems.setText("Price (" + details.getCount()+ " items)");

                //2
                //set total amount
                //amountItemsInt = dataSnapshot.child("amountDiscounted").getValue(Integer.class);
                amountItems.setText("₹ " + details.getAmountDiscounted());

                //3
                //set total savings
                //savings = dataSnapshot.child("amountOriginal").getValue(Integer.class) - amountItemsInt;
                int savings = details.getAmountOriginal() - details.getAmountDiscounted();
                amountSavings.setText("You have saved ₹ " + savings + " on this order");

                //check for delivery rates

                amountDelivery.setText("50");

                //5
                //set total with added delivery
                Integer amountTotalInt = 50 + details.getAmountDiscounted();
                amountTotal.setText("₹ " + amountTotalInt.toString());



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserOrderDetailsActivity.this,databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showShortList(Query q) {
        Log.d("abcc","inside show shortlist");
        FirebaseRecyclerAdapter<Book, CartShortViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, CartShortViewHolder>(
                Book.class, R.layout.cart_short_row, CartShortViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final CartShortViewHolder viewHolder, final Book model, final int position) {

                viewHolder.setDetails(model.getName(),model.getPublication(),model.getDiscountedPrice());
            }
        };

        Log.d("abcc","after adapter");
        itemList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
