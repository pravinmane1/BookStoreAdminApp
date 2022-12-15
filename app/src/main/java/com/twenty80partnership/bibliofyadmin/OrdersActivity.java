package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;
import com.twenty80partnership.bibliofyadmin.models.DeliveryPerson;
import com.twenty80partnership.bibliofyadmin.models.OrderThumb;
import com.twenty80partnership.bibliofyadmin.models.Status;
import com.twenty80partnership.bibliofyadmin.viewHolders.OrderViewHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class OrdersActivity extends AppCompatActivity {

    private static final int DELIVERYBOYSELECTMANDATORY = 124;
    ToggleSwitch toggleSwitch;
    RecyclerView itemList;
    DatabaseReference ordersListThumbRef;
    FirebaseDatabase db;
    int count = 0;
    private ProgressDialog pd;
    private final int DELIVERYBOYSELECT = 123;
    private String uUid,oid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        db = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toggleSwitch = findViewById(R.id.toggle_switch);


        pd = new ProgressDialog(this);
        pd.setMessage("loading...");
        pd.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        itemList.setLayoutManager(mLayoutManager);

        final TextView more = findViewById(R.id.more);

        //selects first tab (active)
        ordersListThumbRef = FirebaseDatabase.getInstance().getReference("OrdersListThumb").child("activeOrdersThumb");
        Query query = ordersListThumbRef.orderByChild("timeAdded").limitToLast(20);
        count = 20;

        firebaseSearch(query, "activeOrders");

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = count + 20;
                Query query = ordersListThumbRef.orderByChild("timeAdded").limitToLast(count);
                firebaseSearch(query, "activeOrders");
            }
        });


        toggleSwitch.setCheckedPosition(0);


        toggleSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {
            @Override
            public void onToggleSwitchChanged(int i) {
            switch (i) {

                case 0:
                    ordersListThumbRef = FirebaseDatabase.getInstance().getReference("OrdersListThumb").child("activeOrdersThumb");
                    Query query = ordersListThumbRef.orderByChild("timeAdded").limitToLast(20);
                    count = 100;

                    firebaseSearch(query, "activeOrders");

                    more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            count = count + 100;
                            Query query = ordersListThumbRef.orderByChild("timeAdded").limitToLast(count);
                            firebaseSearch(query, "activeOrders");
                        }
                    });
                    break;

                case 1:
                    ordersListThumbRef = FirebaseDatabase.getInstance().getReference("OrdersListThumb").child("completedOrdersThumb");
                    Query query1 = ordersListThumbRef.orderByChild("timeAdded").limitToLast(20);
                    count = 100;

                    firebaseSearch(query1, "completedOrders");

                    more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            count = count + 100;
                            Query query = ordersListThumbRef.orderByChild("timeAdded").limitToLast(count);
                            firebaseSearch(query, "completedOrders");
                        }
                    });
                    break;

                case 2:
                    ordersListThumbRef = FirebaseDatabase.getInstance().getReference("OrdersListThumb").child("cancelledOrdersThumb");
                    Query query2 = ordersListThumbRef.orderByChild("timeAdded").limitToLast(20);
                    count = 100;

                    firebaseSearch(query2, "cancelledOrders");

                    more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            count = count + 100;
                            Query query = ordersListThumbRef.orderByChild("timeAdded").limitToLast(count);
                            firebaseSearch(query, "cancelledOrders");
                        }
                    });
                    break;
            }
            }
        });


    }


    public void firebaseSearch(Query q, final String place) {

        Log.d("recycle debug", "firebasesearch");

        final FirebaseRecyclerAdapter<OrderThumb, OrderViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<OrderThumb, OrderViewHolder>(
                OrderThumb.class, R.layout.order_row, OrderViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final OrderViewHolder viewHolder, final OrderThumb model, final int position) {

                // String key = getRef(position).getKey();
                //todo
                viewHolder.setDetails(model.getUserTimeAdded(), model.getUserName(), model.getNumber(), model.getOrderStatus(),
                        model.getTotalPrice().toString(), model.getPincode().toString(),
                        model.getPaymentStatus(), getApplicationContext(), model.getOrderId(), model.getDaysForDelivery());

                final String[] states1 = {"Placed", "Confirmed", "Dispatched", "Delivered", "Cancelled"};

                final HashMap<String, Integer> states = new HashMap<>();
                states.put("Placed", 1);
                states.put("Confirmed", 2);
                states.put("Dispatched", 3);
                states.put("Delivered", 4);
                states.put("Cancelled", 5);


                viewHolder.orderCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    Intent intent = new Intent(OrdersActivity.this, OrderDetailsActivity.class);
                    intent.putExtra("orderId", model.getOrderId());
                    intent.putExtra("source", "myOrders");

                    startActivity(intent);
                    }
                });


                if (place.equals("activeOrders")) {
                    viewHolder.statusT.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OrdersActivity.this);
                        builder.setTitle("Select order status");
                        builder.setItems(states1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {

                            pd.show();

                            final String oldStatus = model.getOrderStatus();

                            //paymentstatus

                            final DatabaseReference paymentStatusRef = FirebaseDatabase.getInstance().getReference("OrdersList")
                                .child(place)
                                .child(model.getOrderId()).child("paymentStatus");

                            final DatabaseReference paymentStatusRefThumb = FirebaseDatabase.getInstance().getReference("OrdersListThumb")
                                .child(place + "Thumb")
                                .child(model.getOrderId()).child("paymentStatus");


                            final DatabaseReference userPaymentStatusRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(model.getuId())
                                    .child(model.getOrderId()).child("paymentStatus");

                            final DatabaseReference userPaymentStatusRefThumb = FirebaseDatabase.getInstance()
                                    .getReference("UserOrdersThumb").child(model.getuId())
                                    .child(model.getOrderId()).child("paymentStatus");


                            //orderstatus
                            final DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("OrdersList")
                                    .child(place)
                                    .child(model.getOrderId()).child("orderStatus");

                            final DatabaseReference statusRefThumb = FirebaseDatabase.getInstance().getReference("OrdersListThumb")
                                    .child(place + "Thumb")
                                    .child(model.getOrderId()).child("orderStatus");


                            final DatabaseReference statusStackAdminRef = FirebaseDatabase.getInstance().getReference("OrdersList")
                                    .child(place)
                                    .child(model.getOrderId()).child("statusStack");

                            final DatabaseReference statusStackUserRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(model.getuId())
                                    .child(model.getOrderId()).child("statusStack");

                            final DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(model.getuId())
                                    .child(model.getOrderId()).child("orderStatus");

                            final DatabaseReference userStatusRefThumb = FirebaseDatabase.getInstance()
                                    .getReference("UserOrdersThumb").child(model.getuId())
                                    .child(model.getOrderId()).child("orderStatus");

                            statusStackAdminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Log.d("tagedebug", "datasnapshot is " + dataSnapshot);
                                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                        Log.d("tagedebug", "ds is " + ds);
                                        if (ds.child("statusName").exists() &&
                                                ds.child("statusName").getValue(String.class).equals("Cancelled")) {
                                            Log.d("tagedebug", "equal");

                                            if (!ds.getKey().equals("d")) {
                                                statusStackAdminRef.child(ds.getKey()).removeValue();
                                                statusStackUserRef.child(ds.getKey()).removeValue();
                                            }

                                        }
                                    }

                                    pd.dismiss();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Log.d("tagedebug", "no access ");
                                    pd.dismiss();
                                }
                            });

                            DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

                            Date currentDate = Calendar.getInstance().getTime();

                            String date = dateFormat.format(currentDate);

                            final Status status = new Status();
                            status.setDate(Long.parseLong(date));

                            if ("Placed".equals(states1[which])) {

                                Log.d("abccd", "status is equal to placed");

                                status.setStatusName("Placed");
                                status.setDescription("Your order has been placed.");

                                if (states.get(oldStatus) > states.get("Placed")) {
                                    statusStackAdminRef.child("b").removeValue();
                                    statusStackAdminRef.child("c").removeValue();
                                    statusStackAdminRef.child("d").removeValue();

                                    statusStackUserRef.child("b").removeValue();
                                    statusStackUserRef.child("c").removeValue();
                                    statusStackUserRef.child("d").removeValue();
                                }

                                statusStackAdminRef.child("a").setValue(status);
                                statusStackUserRef.child("a").setValue(status);

                                statusRefThumb.setValue("Placed");
                                userStatusRefThumb.setValue("Placed");

                                statusRef.setValue("Placed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        userStatusRef.setValue("Placed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                Toast.makeText(OrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (!states1[which].equals(oldStatus)) {
                                                    DatabaseReference userViewedRef = FirebaseDatabase.getInstance().getReference("UserOrders")
                                                            .child(model.getuId()).child(model.getOrderId()).child("userViewed");
                                                    userViewedRef.setValue(false);

                                                }
                                            }
                                            }
                                        });
                                        viewHolder.statusT.setText("Placed");
                                        viewHolder.statusT.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.yellow));
                                    } else {
                                        Toast.makeText(OrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    }
                                });

                            }
                            else if ("Confirmed".equals(states1[which])) {

                                Intent i = new Intent(OrdersActivity.this, DeliveryPersonsActivity.class);
                                i.putExtra("mode","select");
                                i.putExtra("oid",model.getOrderId());
                                i.putExtra("uUid",model.getuId());
                                startActivityForResult(i, DELIVERYBOYSELECT);

                                // onactivity result set deliveryboy

                                Log.d("abccd", "status is equal to confirmed");

                                status.setStatusName("Confirmed");
                                status.setDescription("Your order is confirmed.");

                                if (states.get(oldStatus) > states.get("Confirmed")) {
                                    statusStackAdminRef.child("c").removeValue();
                                    statusStackAdminRef.child("d").removeValue();

                                    statusStackUserRef.child("c").removeValue();
                                    statusStackUserRef.child("d").removeValue();
                                }


                                statusStackAdminRef.child("b").setValue(status);
                                statusStackUserRef.child("b").setValue(status);

                                statusRefThumb.setValue("Confirmed");
                                userStatusRefThumb.setValue("Confirmed");


                                statusRef.setValue("Confirmed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            userStatusRef.setValue("Confirmed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (!task.isSuccessful()) {
                                                        Toast.makeText(OrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        if (!states1[which].equals(oldStatus)) {
                                                            DatabaseReference userViewedRef = FirebaseDatabase.getInstance().getReference("UserOrders")
                                                                    .child(model.getuId()).child(model.getOrderId()).child("userViewed");
                                                            userViewedRef.setValue(false);

                                                        }
                                                    }
                                                }
                                            });
                                            viewHolder.statusT.setText("Confirmed");
                                            viewHolder.statusT.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.aqua));
                                        } else {
                                            Toast.makeText(OrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }
                            else if ("Dispatched".equals(states1[which])) {



                                Intent i = new Intent(OrdersActivity.this, DeliveryPersonsActivity.class);
                                i.putExtra("mode","select");
                                i.putExtra("oid",model.getOrderId());
                                i.putExtra("uUid",model.getuId());

                                uUid = model.getuId();
                                oid = model.getOrderId();
                                startActivityForResult(i, DELIVERYBOYSELECTMANDATORY);
                                Log.d("abccd", "status is equal to dispatched");

                                status.setStatusName("Dispatched");
                                status.setDescription("Your order has been dispatched.");

                                if (states.get(oldStatus) > states.get("Confirmed")) {
                                    statusStackAdminRef.child("d").removeValue();

                                    statusStackUserRef.child("d").removeValue();
                                }

                                statusStackAdminRef.child("c").setValue(status);
                                statusStackUserRef.child("c").setValue(status);

                                statusRefThumb.setValue("Dispatched");
                                userStatusRefThumb.setValue("Dispatched");


                                statusRef.setValue("Dispatched").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            userStatusRef.setValue("Dispatched").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (!task.isSuccessful()) {
                                                        Toast.makeText(OrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        if (!states1[which].equals(oldStatus)) {
                                                            DatabaseReference userViewedRef = FirebaseDatabase.getInstance().getReference("UserOrders")
                                                                    .child(model.getuId()).child(model.getOrderId()).child("userViewed");
                                                            userViewedRef.setValue(false);

                                                        }
                                                    }
                                                }
                                            });
                                            viewHolder.statusT.setText("Dispatched");
                                            viewHolder.statusT.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.neongreen));
                                        } else {
                                            Toast.makeText(OrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else if ("Delivered".equals(states1[which])) {
                                Log.d("abccd", "status is equal to delivered");

                                status.setStatusName("Delivered");
                                status.setDescription("Your order has been delivered.");

                                statusStackAdminRef.child("d").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                    statusStackUserRef.child("d").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                        if (model.getPaymentStatus().equals("pendingCOD")) {

                                            paymentStatusRefThumb.setValue("successCOD").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                userPaymentStatusRefThumb.setValue("successCOD").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                    paymentStatusRef.setValue("successCOD").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                        userPaymentStatusRef.setValue("successCOD").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                            ///old updates
                                                            statusRefThumb.setValue("Delivered").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                userStatusRefThumb.setValue("Delivered").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {


                                                                    statusRef.setValue("Delivered").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            userStatusRef.setValue("Delivered").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                if (!task.isSuccessful()) {
                                                                                    Toast.makeText(OrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                }
                                                                                else {
                                                                                    db.getReference("OrderMapping").child(model.getOrderId()).child("place").setValue("completedOrders");


//                                                                                          todo update nodes...

                                                                                    db.getReference("OrdersList").child("activeOrders").child(model.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            db.getReference("OrdersList").child("completedOrders").child(model.getOrderId()).setValue(dataSnapshot.getValue());
                                                                                            db.getReference("OrdersList").child("activeOrders").child(model.getOrderId()).removeValue();
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });

                                                                                    db.getReference("OrdersListThumb").child("activeOrdersThumb").child(model.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                            db.getReference("OrdersListThumb").child("completedOrdersThumb").child(model.getOrderId()).setValue(dataSnapshot.getValue());
                                                                                            db.getReference("OrdersListThumb").child("activeOrdersThumb").child(model.getOrderId()).removeValue();
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                        }
                                                                                    });


                                                                                    if (!states1[which].equals(oldStatus)) {
                                                                                        DatabaseReference userViewedRef = FirebaseDatabase.getInstance().getReference("UserOrders")
                                                                                                .child(model.getuId()).child(model.getOrderId()).child("userViewed");
                                                                                        userViewedRef.setValue(false);

                                                                                    }
                                                                                }
                                                                                }
                                                                            });
                                                                            viewHolder.statusT.setText("Delivered");
                                                                            viewHolder.statusT.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_green));
                                                                        } else {
                                                                            Toast.makeText(OrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        }
                                                                    });
                                                                    }
                                                                });

                                                                }

                                                            });


                                                            }
                                                        });
                                                        }
                                                    });
                                                    }
                                                });
                                                }
                                            });

                                        }
                                        else {
                                            statusRefThumb.setValue("Delivered").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                userStatusRefThumb.setValue("Delivered").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {


                                                        statusRef.setValue("Delivered").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    userStatusRef.setValue("Delivered").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (!task.isSuccessful()) {
                                                                                Toast.makeText(OrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                            } else {
                                                                                db.getReference("OrderMapping").child(model.getOrderId()).child("place").setValue("completedOrders");

                                                                                //todo update nodes...
                                                                                db.getReference("OrdersList").child("activeOrders").child(model.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                        db.getReference("OrdersList").child("completedOrders").child(model.getOrderId()).setValue(dataSnapshot.getValue());
                                                                                        db.getReference("OrdersList").child("activeOrders").child(model.getOrderId()).removeValue();
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                                db.getReference("OrdersListThumb").child("activeOrdersThumb").child(model.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                        db.getReference("OrdersListThumb").child("completedOrdersThumb").child(model.getOrderId()).setValue(dataSnapshot.getValue());
                                                                                        db.getReference("OrdersListThumb").child("activeOrdersThumb").child(model.getOrderId()).removeValue();
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                    }
                                                                                });


                                                                                if (!states1[which].equals(oldStatus)) {
                                                                                    DatabaseReference userViewedRef = FirebaseDatabase.getInstance().getReference("UserOrders")
                                                                                            .child(model.getuId()).child(model.getOrderId()).child("userViewed");
                                                                                    userViewedRef.setValue(false);

                                                                                }
                                                                            }
                                                                        }
                                                                    });






                                                                    viewHolder.statusT.setText("Delivered");
                                                                    viewHolder.statusT.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.light_green));
                                                                } else {
                                                                    Toast.makeText(OrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });

                                                }

                                            });

                                        }

                                            //
                                        }
                                    });

                                    }
                                });


                            }
                            else if ("Cancelled".equals(states1[which])) {
                                Log.d("abccd", "status is equal to cancelled");

                                status.setStatusName("Cancelled");
                                status.setDescription("Your order has been cancelled.");

                                statusStackAdminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            if (!dataSnapshot.child("a").exists()) {
                                                statusStackAdminRef.child("a").setValue(status);
                                                statusStackUserRef.child("a").setValue(status);
                                            } else if (!dataSnapshot.child("b").exists()) {
                                                statusStackAdminRef.child("b").setValue(status);
                                                statusStackUserRef.child("b").setValue(status);
                                            } else if (!dataSnapshot.child("c").exists()) {
                                                statusStackAdminRef.child("c").setValue(status);
                                                statusStackUserRef.child("c").setValue(status);
                                            } else {
                                                statusStackAdminRef.child("d").setValue(status);
                                                statusStackUserRef.child("d").setValue(status);
                                            }
                                        }

                                        statusRefThumb.setValue("Cancelled").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                            userStatusRefThumb.setValue("Cancelled").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                statusRef.setValue("Cancelled").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        userStatusRef.setValue("Cancelled").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                            if (!task.isSuccessful()) {
                                                                Toast.makeText(OrdersActivity.this, "User node failed to update status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {




                                                                db.getReference("OrderMapping").child(model.getOrderId()).child("place").setValue("cancelledOrders");


                                                                db.getReference("OrdersList").child("activeOrders").child(model.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        db.getReference("OrdersList").child("cancelledOrders").child(model.getOrderId()).setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){
                                                                                  db.getReference("OrdersList").child("cancelledOrders").child(model.getOrderId()).child("orderData").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                      @Override
                                                                                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                          //fetch order data and update count to book location


                                                                                          for (DataSnapshot ds:dataSnapshot.getChildren()){

                                                                                              String itemId = ds.child("itemId").getValue(String.class);
                                                                                              String itemLocation = ds.child("itemLocation").getValue(String.class);
                                                                                              final Integer quantity = ds.child("quantity").getValue(Integer.class);

                                                                                              db.getReference(itemLocation).child(itemId).child("count").runTransaction(new Transaction.Handler() {
                                                                                                  @NonNull
                                                                                                  @Override
                                                                                                  public Transaction.Result doTransaction(@NonNull MutableData mutableData) {



                                                                                                      Integer count = mutableData.getValue(Integer.class);

                                                                                                      Log.d("OrdersActivityDebug","count "+count+" quant. "+quantity);

                                                                                                      if (count != null) {
                                                                                                          count = count + quantity;
                                                                                                          mutableData.setValue(count);
                                                                                                          // Toast.makeText(BookList2PSActivity.this, "Success", Toast.LENGTH_SHORT).show();

                                                                                                          return Transaction.success(mutableData);
                                                                                                      }else{
                                                                                                          // Toast.makeText(BookList2PSActivity.this, "Invalid action", Toast.LENGTH_SHORT).show();
                                                                                                      }
                                                                                                      return Transaction.abort();
                                                                                                  }

                                                                                                  @Override
                                                                                                  public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                                                                                                  }
                                                                                              });
                                                                                          }
                                                                                      }

                                                                                      @Override
                                                                                      public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                      }
                                                                                  });
                                                                                }
                                                                            }
                                                                        });
                                                                        db.getReference("OrdersList").child("activeOrders").child(model.getOrderId()).removeValue();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });

                                                                db.getReference("OrdersListThumb").child("activeOrdersThumb").child(model.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                        db.getReference("OrdersListThumb").child("cancelledOrdersThumb").child(model.getOrderId()).setValue(dataSnapshot.getValue());
                                                                        db.getReference("OrdersListThumb").child("activeOrdersThumb").child(model.getOrderId()).removeValue();
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                    }
                                                                });


                                                                if (!states1[which].equals(oldStatus)) {
                                                                    DatabaseReference userViewedRef = FirebaseDatabase.getInstance().getReference("UserOrders")
                                                                            .child(model.getuId()).child(model.getOrderId()).child("userViewed");
                                                                    userViewedRef.setValue(false);

                                                                }
                                                            }
                                                            }
                                                        });
                                                        viewHolder.statusT.setText("Cancelled");
                                                        viewHolder.statusT.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
                                                    } else {
                                                        Toast.makeText(OrdersActivity.this, "Admin node failed to update status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                    }
                                                });
                                                }
                                            });

                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                            else {
                                Log.d("abccd", "status is not matching " + states.get(states1[which]));
                            }
                            }
                        });
                        builder.show();
                        }
                    });
                }

            }
        };

        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode)
        {
            case DELIVERYBOYSELECT:
                if (resultCode==RESULT_OK){
                    String oid = data.getStringExtra("oid");
                    String uUid = data.getStringExtra("uUid");
                    String dUid = data.getStringExtra("dUid");

                    db.getReference("UserOrders").child(uUid).child(oid).child("deliveryPerson").setValue(dUid);
                    db.getReference("OrdersList").child("activeOrders").child(oid).child("deliveryPerson").setValue(dUid);
                    db.getReference("OrderAssignment").child(dUid).child("ordersAssigned").child(oid).child("orderId").setValue(oid);
                }
                break;

            case DELIVERYBOYSELECTMANDATORY:
                if (resultCode==RESULT_OK){
                    String oid = data.getStringExtra("oid");
                    String uUid = data.getStringExtra("uUid");
                    String dUid = data.getStringExtra("dUid");
                    String number = data.getStringExtra("number");

                    db.getReference("UserOrders").child(uUid).child(oid).child("deliveryPersonNumber").setValue(number);
                    db.getReference("OrdersList").child("activeOrders").child(oid).child("deliveryPerson").setValue(dUid);
                    db.getReference("OrderAssignment").child(dUid).child("ordersAssigned").child(oid).child("orderId").setValue(oid);
                }
                else{
                    Intent i = new Intent(OrdersActivity.this, DeliveryPersonsActivity.class);
                    i.putExtra("mode","select");
                    i.putExtra("oid",oid);
                    i.putExtra("uUid",uUid);
                    startActivityForResult(i, DELIVERYBOYSELECTMANDATORY);
                }

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
