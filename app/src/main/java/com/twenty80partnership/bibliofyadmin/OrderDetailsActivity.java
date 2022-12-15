package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofyadmin.models.Address;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.models.Date;
import com.twenty80partnership.bibliofyadmin.models.Order;
import com.twenty80partnership.bibliofyadmin.models.PriceDetails;
import com.twenty80partnership.bibliofyadmin.models.StationaryItem;
import com.twenty80partnership.bibliofyadmin.models.Status;
import com.twenty80partnership.bibliofyadmin.models.StatusStack;
import com.twenty80partnership.bibliofyadmin.viewHolders.CartShortViewHolder;

import java.util.ArrayList;


public class OrderDetailsActivity extends AppCompatActivity {

    DatabaseReference bookDataRef;
    String mapping="";
    FirebaseAuth mAuth;
    private RecyclerView itemList;
    private Order order = new Order();
    private LottieAnimationView img1, img2, img3, img4;
    private Handler handler;
    private int i, j = -1;
    private Intent intent;
    private ProgressDialog progressDialog;
    private TextView orderIdView, deliveryDateView;
    private TextView placedLabel, desc, statusTime, confirmedLabel, desc2, statusTime2, dispatchedLabel, desc3, statusTime3, deliveredLabel, desc4, statusTime4;
    private View bar1, bar2, bar3;
    private Toolbar toolbar;
    private Date date;
    private RelativeLayout orderData;
    ArrayList<Book> bookArrayList;
    ArrayList<StationaryItem> stationaryItemArrayList;
    LinearLayout lin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        mAuth = FirebaseAuth.getInstance();
        intent = getIntent();

        date = new Date();

        orderIdView = findViewById(R.id.o_id);
        deliveryDateView = findViewById(R.id.delivery_date);
        orderData = findViewById(R.id.order_data);

        lin = findViewById(R.id.lin);




        if (Build.VERSION.SDK_INT >= 21) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.lightGreen)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lightGreen)); //status bar or the time bar at the top
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        if (getIntent().getStringExtra("orderId") != null) {
            order.setOrderId(getIntent().getStringExtra("orderId"));
        } else {
            finish();
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemList = findViewById(R.id.recycler_view);
        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        itemList.setLayoutManager(mLayoutManager);





        fetchDetails();




    }

    private void fetchDetails() {
        progressDialog.show();

        Log.d("abcc", "inside fetch details");

        DatabaseReference orderMappingRef = FirebaseDatabase.getInstance().getReference("OrderMapping").child(order.getOrderId()).child("place");

        orderMappingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mapping = dataSnapshot.getValue(String.class);


                DatabaseReference userOrdersRef = FirebaseDatabase.getInstance().getReference("OrdersList")
                        .child(mapping).child(order.getOrderId());

                userOrdersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            if (dataSnapshot.child("orderData").exists()){

                                final Intent intent2 = new Intent(OrderDetailsActivity.this,OrderDataActivity.class);

                                bookArrayList = new ArrayList<>();
                                stationaryItemArrayList = new ArrayList<>();

                                for (DataSnapshot dsItem:dataSnapshot.child("orderData").getChildren()){

                                    if (dsItem.child("type").getValue(String.class).equals("books")){

                                        bookArrayList.add(dsItem.getValue(Book.class));
                                    }
                                    else{
                                        stationaryItemArrayList.add(dsItem.getValue(StationaryItem.class));
                                    }


                                }

                                intent2.putExtra("bookArrayList",bookArrayList);
                                intent2.putExtra("stationaryItemArrayList",stationaryItemArrayList);

                                orderData.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(intent2);
                                    }
                                });


                                lin.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        startActivity(intent2);
                                    }
                                });
                            }


                            order = dataSnapshot.getValue(Order.class);

                            orderIdView.setText("Order Id : " + order.getOrderId());
                            deliveryDateView.setText("Delivery expected by " + date.incrementDate(order.getUserTimeAdded(), order.getDaysForDelivery()));

                            setPaymentStatus(order.getPaymentStatus(),
                                    order.getPriceDetails().getAmountDiscounted() + order.getPriceDetails().getDeliveryCharges(),
                                    order.getOrderStatus());

                            setStatus(order.getStatusStack(), order.getPaymentStatus());


                            if (intent.getStringExtra("source").equals("notification")) {
                                Log.d("listenOrder", order.getOrderId());
                                DatabaseReference userViewedRef = FirebaseDatabase.getInstance().getReference("UserOrders")
                                        .child(mAuth.getCurrentUser().getUid())
                                        .child(order.getOrderId()).child("userViewed");
                                userViewedRef.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(OrderDetailsActivity.this, "success", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(OrderDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });

                            }



                            bookDataRef = FirebaseDatabase.getInstance().getReference("OrdersList")
                                    .child(mapping).child(order.getOrderId()).child("orderData");

                            Query query = bookDataRef.orderByChild("timeAdded").limitToFirst(4);

                            showShortList(query);
                            setAddress(order.getAddress());
                            setPriceDetails(dataSnapshot.child("priceDetails"));

                            progressDialog.dismiss();
                        } else {
                            Log.d("abcc", "inside fetch details");
                            Toast.makeText(OrderDetailsActivity.this, "order data does not exist", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("abcc", databaseError.getMessage());

                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void setPaymentStatus(String paymentStatus, int amount, String orderStatus) {

        LottieAnimationView warning = findViewById(R.id.warning_anim);
        TextView payment = findViewById(R.id.payment);
        TextView resolve = findViewById(R.id.resolve);
        ImageView arrow = findViewById(R.id.arrow);

        if (orderStatus.equals("Cancelled")) {

            deliveryDateView.setVisibility(View.GONE);

            warning.setAnimation("warning_anim.json");
            warning.playAnimation();
            payment.setText("₹ " + amount);

            if (paymentStatus.equals("pendingCOD") || paymentStatus.equals("successCOD")) {
                resolve.setText("(Cash On Delivery)");

            } else {
                resolve.setText("(Online Payment)");
            }
            resolve.setTextColor(getResources().getColor(R.color.black));
            arrow.setVisibility(View.GONE);
        } else {

            if (orderStatus.equals("Delivered")) {
                deliveryDateView.setText("Order has been delivered successfully.");
            }
            deliveryDateView.setVisibility(View.VISIBLE);

            switch (paymentStatus) {
                case "pendingCOD":
                case "successCOD":
                    warning.setAnimation("simple_tick.json");
                    warning.playAnimation();
                    payment.setText("₹ " + amount);
                    resolve.setText("(Cash On Delivery)");
                    resolve.setTextColor(getResources().getColor(R.color.black));
                    arrow.setVisibility(View.GONE);
                    break;
                case "successOnline":
                    warning.setAnimation("simple_tick.json");
                    warning.playAnimation();
                    payment.setText("₹ " + amount);
                    resolve.setText("(Online Payment)");
                    resolve.setTextColor(getResources().getColor(R.color.black));
                    arrow.setVisibility(View.GONE);
                    break;
                case "pendingOnline":
                    warning.setAnimation("warning_anim.json");
                    warning.setRepeatCount(LottieDrawable.INFINITE);
                    warning.playAnimation();
                    payment.setText("₹ " + amount);
                    resolve.setText("Resolve Payment");
                    resolve.setTextColor(getResources().getColor(R.color.red));
                    arrow.setVisibility(View.VISIBLE);
                    break;
            }

        }


    }

    private void setStatus(final StatusStack statusStack, String paymentStatus) {

        RelativeLayout background = findViewById(R.id.delivery);

        if (statusStack.getA() != null && statusStack.getA().getStatusName().equals("Cancelled")) {

            background.setBackgroundColor(getResources().getColor(R.color.lightRed));
            toolbar.setBackgroundColor(getResources().getColor(R.color.lightRed));

            if (Build.VERSION.SDK_INT >= 21) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.lightRed)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lightRed)); //status bar or the time bar at the top
            }
        } else if (statusStack.getB() != null && statusStack.getB().getStatusName().equals("Cancelled")) {

            background.setBackgroundColor(getResources().getColor(R.color.lightRed));
            toolbar.setBackgroundColor(getResources().getColor(R.color.lightRed));

            if (Build.VERSION.SDK_INT >= 21) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.lightRed)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lightRed)); //status bar or the time bar at the top
            }

        } else if (statusStack.getC() != null && statusStack.getC().getStatusName().equals("Cancelled")) {

            background.setBackgroundColor(getResources().getColor(R.color.lightRed));
            toolbar.setBackgroundColor(getResources().getColor(R.color.lightRed));

            if (Build.VERSION.SDK_INT >= 21) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.lightRed)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lightRed)); //status bar or the time bar at the top
            }

        } else if (statusStack.getD() != null && statusStack.getD().getStatusName().equals("Cancelled")) {

            background.setBackgroundColor(getResources().getColor(R.color.lightRed));
            toolbar.setBackgroundColor(getResources().getColor(R.color.lightRed));

            if (Build.VERSION.SDK_INT >= 21) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.lightRed)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lightRed)); //status bar or the time bar at the top
            }

        } else if (paymentStatus.equals("pendingOnline")) {
            background.setBackgroundColor(getResources().getColor(R.color.lightYellow));
            toolbar.setBackgroundColor(getResources().getColor(R.color.lightYellow));

            if (Build.VERSION.SDK_INT >= 21) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
                getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.lightYellow)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
                getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.lightYellow)); //status bar or the time bar at the top
            }
        }


        img1 = findViewById(R.id.img1);
        placedLabel = findViewById(R.id.placed_label);
        desc = findViewById(R.id.desc);
        statusTime = findViewById(R.id.status_time);
        bar1 = findViewById(R.id.bar1);

        img2 = findViewById(R.id.img2);
        confirmedLabel = findViewById(R.id.confirmed_label);
        desc2 = findViewById(R.id.desc2);
        statusTime2 = findViewById(R.id.status_time2);
        bar2 = findViewById(R.id.bar2);

        img3 = findViewById(R.id.img3);
        dispatchedLabel = findViewById(R.id.dispatched_label);
        desc3 = findViewById(R.id.desc3);
        statusTime3 = findViewById(R.id.status_time3);
        bar3 = findViewById(R.id.bar3);


        img4 = findViewById(R.id.img4);
        deliveredLabel = findViewById(R.id.delivered_label);
        desc4 = findViewById(R.id.desc4);
        statusTime4 = findViewById(R.id.status_time4);


        img1.setProgress(0.2f);
        img2.setProgress(0.2f);
        img3.setProgress(0.2f);
        img4.setProgress(0.2f);


        int k = 0;
        int l = -1;


        while (k < 5) {
            switch (k) {
                case 0:
                    Log.d("abcc", "inside case 0 ");

                    if (statusStack.getA() != null) {
                        Status a = statusStack.getA();
                        if (a.getStatusName().equals("Cancelled")) {
                            l = 0;
                            img1.setAnimation("error.json");

                            img2.setVisibility(View.GONE);
                            confirmedLabel.setVisibility(View.GONE);
                            desc2.setVisibility(View.GONE);
                            statusTime2.setVisibility(View.GONE);
                            bar1.setVisibility(View.GONE);

                            img3.setVisibility(View.GONE);
                            dispatchedLabel.setVisibility(View.GONE);
                            desc3.setVisibility(View.GONE);
                            statusTime3.setVisibility(View.GONE);
                            bar2.setVisibility(View.GONE);

                            img4.setVisibility(View.GONE);
                            deliveredLabel.setVisibility(View.GONE);
                            desc4.setVisibility(View.GONE);
                            statusTime4.setVisibility(View.GONE);
                            bar3.setVisibility(View.GONE);
                        }


                        placedLabel.setText(a.getStatusName());
                        desc.setText(a.getDescription());
                        statusTime.setText(date.convertLongDateIntoSplit(a.getDate()));

                        placedLabel.setVisibility(View.INVISIBLE);
                        placedLabel.setTextColor(getResources().getColor(R.color.black));
                        desc.setVisibility(View.INVISIBLE);
                        statusTime.setVisibility(View.INVISIBLE);
                    }

                    break;
                case 1:
                    Log.d("abcc", "inside case 1 ");

                    if (statusStack.getB() != null) {
                        Status b = statusStack.getB();
                        if (b.getStatusName().equals("Cancelled")) {
                            l = 1;
                            img2.setAnimation("error.json");

                            img3.setVisibility(View.GONE);
                            dispatchedLabel.setVisibility(View.GONE);
                            desc3.setVisibility(View.GONE);
                            statusTime3.setVisibility(View.GONE);
                            bar2.setVisibility(View.GONE);

                            img4.setVisibility(View.GONE);
                            deliveredLabel.setVisibility(View.GONE);
                            desc4.setVisibility(View.GONE);
                            statusTime4.setVisibility(View.GONE);
                            bar3.setVisibility(View.GONE);
                        }
                        confirmedLabel.setText(b.getStatusName());
                        desc2.setText(b.getDescription());
                        statusTime2.setText(date.convertLongDateIntoSplit(b.getDate()));

                        confirmedLabel.setVisibility(View.INVISIBLE);
                        confirmedLabel.setTextColor(getResources().getColor(R.color.black));
                        desc2.setVisibility(View.INVISIBLE);
                        statusTime2.setVisibility(View.INVISIBLE);
                    }

                    break;
                case 2:
                    Log.d("abcc", "inside case 2 ");

                    if (statusStack.getC() != null) {
                        Status c = statusStack.getC();
                        if (c.getStatusName().equals("Cancelled")) {
                            l = 2;
                            img3.setAnimation("error.json");

                            img4.setVisibility(View.GONE);
                            deliveredLabel.setVisibility(View.GONE);
                            desc4.setVisibility(View.GONE);
                            statusTime4.setVisibility(View.GONE);
                            bar3.setVisibility(View.GONE);
                        }
                        dispatchedLabel.setText(c.getStatusName());
                        desc3.setText(c.getDescription());
                        statusTime3.setText(date.convertLongDateIntoSplit(c.getDate()));

                        dispatchedLabel.setVisibility(View.INVISIBLE);
                        dispatchedLabel.setTextColor(getResources().getColor(R.color.black));
                        desc3.setVisibility(View.INVISIBLE);
                        statusTime3.setVisibility(View.INVISIBLE);
                    }

                    break;
                case 3:
                    Log.d("abcc", "inside case 3 ");

                    if (statusStack.getD() != null) {
                        Status d = statusStack.getD();
                        if (d.getStatusName().equals("Cancelled")) {
                            l = 3;
                            img4.setAnimation("error.json");
                        }
                        deliveredLabel.setText(d.getStatusName());
                        desc4.setText(d.getDescription());
                        statusTime4.setText(date.convertLongDateIntoSplit(d.getDate()));

                        deliveredLabel.setVisibility(View.INVISIBLE);
                        deliveredLabel.setTextColor(getResources().getColor(R.color.black));
                        desc4.setVisibility(View.INVISIBLE);
                        statusTime4.setVisibility(View.INVISIBLE);

                    }

                    break;

            }

            if (l == -1) {
                k++;
            } else {
                break;
            }

        }


        i = 0;

        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                if (i < 4) {

                    Log.d("abcc", "outside switch j: " + j);

                    switch (i) {
                        case 0:
                            Log.d("abcc", "inside case 0 ");

                            if (statusStack.getA() != null) {
                                Status a = statusStack.getA();
                                if (a.getStatusName().equals("Cancelled")) {
                                    j = 0;
                                    img1.setAnimation("error.json");

                                    img2.setVisibility(View.GONE);
                                    confirmedLabel.setVisibility(View.GONE);
                                    desc2.setVisibility(View.GONE);
                                    statusTime2.setVisibility(View.GONE);
                                    bar1.setVisibility(View.GONE);

                                    img3.setVisibility(View.GONE);
                                    dispatchedLabel.setVisibility(View.GONE);
                                    desc3.setVisibility(View.GONE);
                                    statusTime3.setVisibility(View.GONE);
                                    bar2.setVisibility(View.GONE);

                                    img4.setVisibility(View.GONE);
                                    deliveredLabel.setVisibility(View.GONE);
                                    desc4.setVisibility(View.GONE);
                                    statusTime4.setVisibility(View.GONE);
                                    bar3.setVisibility(View.GONE);
                                }

                                placedLabel.setText(a.getStatusName());
                                desc.setText(a.getDescription());
                                statusTime.setText(date.convertLongDateIntoSplit(a.getDate()));

                                img1.setVisibility(View.VISIBLE);
                                placedLabel.setVisibility(View.VISIBLE);
                                placedLabel.setTextColor(getResources().getColor(R.color.black));
                                desc.setVisibility(View.VISIBLE);
                                statusTime.setVisibility(View.VISIBLE);

                                img1.playAnimation();
                                img1.setProgress(0.2f);
                            }

                            break;
                        case 1:
                            Log.d("abcc", "inside case 1 ");

                            if (statusStack.getB() != null) {
                                Status b = statusStack.getB();
                                if (b.getStatusName().equals("Cancelled")) {
                                    j = 1;
                                    img2.setAnimation("error.json");

                                    img3.setVisibility(View.GONE);
                                    dispatchedLabel.setVisibility(View.GONE);
                                    desc3.setVisibility(View.GONE);
                                    statusTime3.setVisibility(View.GONE);
                                    bar2.setVisibility(View.GONE);

                                    img4.setVisibility(View.GONE);
                                    deliveredLabel.setVisibility(View.GONE);
                                    desc4.setVisibility(View.GONE);
                                    statusTime4.setVisibility(View.GONE);
                                    bar3.setVisibility(View.GONE);
                                }
                                confirmedLabel.setText(b.getStatusName());
                                desc2.setText(b.getDescription());
                                statusTime2.setText(date.convertLongDateIntoSplit(b.getDate()));

                                img2.setVisibility(View.VISIBLE);
                                confirmedLabel.setVisibility(View.VISIBLE);
                                confirmedLabel.setTextColor(getResources().getColor(R.color.black));
                                desc2.setVisibility(View.VISIBLE);
                                statusTime2.setVisibility(View.VISIBLE);
                                bar1.setVisibility(View.VISIBLE);

                                img2.playAnimation();
                                img2.setProgress(0.2f);
                            }

                            break;
                        case 2:
                            Log.d("abccd", "inside case 2 ");

                            if (statusStack.getC() != null) {
                                Status c = statusStack.getC();
                                if (c.getStatusName().equals("Cancelled")) {
                                    j = 2;
                                    img3.setAnimation("error.json");

                                    img4.setVisibility(View.GONE);
                                    deliveredLabel.setVisibility(View.GONE);
                                    desc4.setVisibility(View.GONE);
                                    statusTime4.setVisibility(View.GONE);
                                    bar3.setVisibility(View.GONE);
                                }
                                dispatchedLabel.setText(c.getStatusName());
                                desc3.setText(c.getDescription());
                                statusTime3.setText(date.convertLongDateIntoSplit(c.getDate()));

                                img3.setVisibility(View.VISIBLE);
                                dispatchedLabel.setVisibility(View.VISIBLE);
                                dispatchedLabel.setTextColor(getResources().getColor(R.color.black));
                                desc3.setVisibility(View.VISIBLE);
                                statusTime3.setVisibility(View.VISIBLE);
                                bar2.setVisibility(View.VISIBLE);

                                img3.playAnimation();
                                img3.setProgress(0.2f);
                            }

                            break;
                        case 3:
                            Log.d("abcc", "inside case 3 ");

                            if (statusStack.getD() != null) {
                                Status d = statusStack.getD();
                                if (d.getStatusName().equals("Cancelled")) {
                                    j = 3;
                                    img4.setAnimation("error.json");
                                }
                                deliveredLabel.setText(d.getStatusName());
                                desc4.setText(d.getDescription());
                                statusTime4.setText(date.convertLongDateIntoSplit(d.getDate()));

                                img4.setVisibility(View.VISIBLE);
                                deliveredLabel.setVisibility(View.VISIBLE);
                                deliveredLabel.setTextColor(getResources().getColor(R.color.black));
                                desc4.setVisibility(View.VISIBLE);
                                statusTime4.setVisibility(View.VISIBLE);
                                bar3.setVisibility(View.VISIBLE);

                                img4.playAnimation();
                                img4.setProgress(0.2f);
                            }

                            break;

                    }

                    if (j == -1) {
                        Log.d("abcc", "j is not eqaul to -1 ");

                        i++;
                        handler.postDelayed(this, 500);
                    }

                }
            }
        };

        handler.postDelayed(r, 500);
    }


    private void setAddress(Address address) {
        ImageView remove, edit;
        TextView name, number, type, completeAddress;

        edit = findViewById(R.id.edit);
        name = findViewById(R.id.name);
        number = findViewById(R.id.number);
        completeAddress = findViewById(R.id.address);
        TextView pinView = findViewById(R.id.pin_view);

        edit.setVisibility(View.GONE);
        name.setText(address.getName());
        number.setText(address.getNumber());

        String combinedAddress = address.getBuildingNameNumber() + " " +
                address.getAreaRoad() + " " +
                address.getCity() + " " +
                address.getState() + "-" +
                address.getPincode();

        completeAddress.setText(combinedAddress);
        pinView.setText(address.getPincode().toString());
    }


    private void setPriceDetails(DataSnapshot dataSnapshot) {


        final TextView totalItems, amountTotal, amountDelivery, amountItems, amountSavings;
        totalItems = findViewById(R.id.total_items);
        amountTotal = findViewById(R.id.amount_total);
        amountDelivery = findViewById(R.id.amount_delivery);
        amountItems = findViewById(R.id.amount_items);
        amountSavings = findViewById(R.id.amount_savings);

        Log.d("showing", "update price detail event listener called");

        PriceDetails details = dataSnapshot.getValue(PriceDetails.class);

        //set total items
        totalItems.setText("Price (" + details.getCount() + " items)");

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
        int deliveryCharges = dataSnapshot.child("deliveryCharges").getValue(Integer.class);
        amountDelivery.setText(deliveryCharges + "");

        //5
        //set total with added delivery
        int amountTotalInt = deliveryCharges + details.getAmountDiscounted();
        amountTotal.setText("₹ " + amountTotalInt);

        //visibility
//                        if (totalItemsInt > 0) {
//                            noItemInCart.setVisibility(View.GONE);
//                            priceDetails.setVisibility(View.VISIBLE);
//                            bottom.setVisibility(View.VISIBLE);
//                            itemList.setVisibility(View.VISIBLE);
//                        }
        //this should be done when count are equal
//                        mShimmerViewContainer.setVisibility(View.GONE);
//                        mShimmerViewContainer.stopShimmerAnimation();
//                        pricePd.dismiss();


    }

    private void showShortList(Query q) {
        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(this, 4));
        Log.d("abcc", "inside show shortlist");
        FirebaseRecyclerAdapter<Book, CartShortViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Book, CartShortViewHolder>(
                Book.class, R.layout.cart_short_row, CartShortViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final CartShortViewHolder viewHolder, final Book model, final int position) {
                Log.d("abcc", "show short list populate");

                viewHolder.setDetails(model.getName(), model.getImg(), model.getDiscountedPrice());

//                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        final Intent intent1 = new Intent(OrderDetailsActivity.this,OrderDataActivity.class);
//
//                        intent1.putExtra("bookArrayList",bookArrayList);
//                        intent1.putExtra("stationaryItemArrayList",stationaryItemArrayList);
//                        startActivity(intent1);
//
//                    }
//                });
            }
        };

        Log.d("abcc", "after adapter");
        itemList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
