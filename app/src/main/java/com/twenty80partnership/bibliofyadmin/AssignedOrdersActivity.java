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
import com.twenty80partnership.bibliofyadmin.models.DeliveryPersonOrderEntry;
import com.twenty80partnership.bibliofyadmin.models.OrderThumb;
import com.twenty80partnership.bibliofyadmin.models.Status;
import com.twenty80partnership.bibliofyadmin.viewHolders.OrderViewHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class AssignedOrdersActivity extends AppCompatActivity {
    RecyclerView itemList;
    DatabaseReference orderAssignmentRef;
    FirebaseDatabase db;
    int count = 0;
    private ProgressDialog pd;
    private String uUid, oid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assigned_orders);

        db = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pd = new ProgressDialog(this);
        pd.setMessage("loading...");
        pd.setCancelable(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        itemList.setLayoutManager(mLayoutManager);

        //selects first tab (active)
        orderAssignmentRef = FirebaseDatabase.getInstance().getReference("OrderAssignment").child(getIntent().getStringExtra("dUid")).child("ordersAssigned");

        firebaseSearch(orderAssignmentRef);
    }

    public void firebaseSearch(DatabaseReference q) {

        final FirebaseRecyclerAdapter<DeliveryPersonOrderEntry, OrderViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<DeliveryPersonOrderEntry, OrderViewHolder>(
                DeliveryPersonOrderEntry.class, R.layout.order_row, OrderViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final OrderViewHolder viewHolder, final DeliveryPersonOrderEntry deliveryPersonOrderEntry, final int position) {

                final String place = "activeOrders";

                if (deliveryPersonOrderEntry.getOrderId()==null){
                    Toast.makeText(AssignedOrdersActivity.this, "null oid", Toast.LENGTH_SHORT).show();
                }
                else{
                    db.getReference("OrdersListThumb").child(place + "Thumb")
                            .child(deliveryPersonOrderEntry.getOrderId())

                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        final OrderThumb model = dataSnapshot.getValue(OrderThumb.class);

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

                                                Intent intent = new Intent(AssignedOrdersActivity.this, OrderDetailsActivity.class);
                                                intent.putExtra("orderId", model.getOrderId());
                                                intent.putExtra("source", "myOrders");

                                                startActivity(intent);
                                            }
                                        });

                                        viewHolder.statusT.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(AssignedOrdersActivity.this);
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

                                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                                    if (ds.child("statusName").exists() &&
                                                                            ds.child("statusName").getValue(String.class).equals("Cancelled")) {

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

                                                            Toast.makeText(AssignedOrdersActivity.this, "Placed", Toast.LENGTH_SHORT).show();

                                                        } else if ("Confirmed".equals(states1[which])) {
                                                            Toast.makeText(AssignedOrdersActivity.this, "Confirmed", Toast.LENGTH_SHORT).show();
                                                        } else if ("Dispatched".equals(states1[which]))
                                                        {

                                                            status.setStatusName("Dispatched");
                                                            status.setDescription("Your order has been dispatched.");

                                                            if (states.get(oldStatus) > states.get("Confirmed")) {
                                                                Toast.makeText(AssignedOrdersActivity.this, "Cant go back", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else{
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
                                                                                        Toast.makeText(AssignedOrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                            Toast.makeText(AssignedOrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }
                                                        else if ("Delivered".equals(states1[which])) {

                                                             if (states.get(oldStatus) >= states.get("Delivered")) {
                                                                Toast.makeText(AssignedOrdersActivity.this, "Cant go back", Toast.LENGTH_SHORT).show();
                                                            }

                                                             else{
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
                                                                                                                                                             Toast.makeText(AssignedOrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                                         } else {
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
                                                                                                                                                 Toast.makeText(AssignedOrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

                                                                                 } else {
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
                                                                                                                             Toast.makeText(AssignedOrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                                                                 Toast.makeText(AssignedOrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                             }
                                                                                                         }
                                                                                                     });
                                                                                                 }
                                                                                             });

                                                                                         }

                                                                                     });

                                                                                 }
                                                                             }
                                                                         });

                                                                     }
                                                                 });
                                                             }




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
                                                                                                            Toast.makeText(AssignedOrdersActivity.this, "User node failed to update status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                        } else {
                                                                                                            db.getReference("OrderMapping").child(model.getOrderId()).child("place").setValue("cancelledOrders");


                                                                                                            db.getReference("OrdersList").child("activeOrders").child(model.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                                @Override
                                                                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                                    db.getReference("OrdersList").child("cancelledOrders").child(model.getOrderId()).setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                        @Override
                                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                                            if (task.isSuccessful()) {
                                                                                                                                db.getReference("OrdersList").child("cancelledOrders").child(model.getOrderId()).child("orderData").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                                                    @Override
                                                                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                                                        //fetch order data and update count to book location


                                                                                                                                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                                                                                                            String itemId = ds.child("itemId").getValue(String.class);
                                                                                                                                            String itemLocation = ds.child("itemLocation").getValue(String.class);
                                                                                                                                            final Integer quantity = ds.child("quantity").getValue(Integer.class);

                                                                                                                                            db.getReference(itemLocation).child(itemId).child("count").runTransaction(new Transaction.Handler() {
                                                                                                                                                @NonNull
                                                                                                                                                @Override
                                                                                                                                                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {


                                                                                                                                                    Integer count = mutableData.getValue(Integer.class);

                                                                                                                                                    if (count != null) {
                                                                                                                                                        count = count + quantity;
                                                                                                                                                        mutableData.setValue(count);
                                                                                                                                                        // Toast.makeText(BookList2PSActivity.this, "Success", Toast.LENGTH_SHORT).show();

                                                                                                                                                        return Transaction.success(mutableData);
                                                                                                                                                    } else {
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
                                                                                                Toast.makeText(AssignedOrdersActivity.this, "Admin node failed to update status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

                                                        } else {
                                                            Log.d("abccd", "status is not matching " + states.get(states1[which]));
                                                        }
                                                    }
                                                });
                                                builder.show();
                                            }
                                        });

                                    }
                                    else {
                                        //todo search for the order in ordermapping

                                        db.getReference("OrderMapping").child(deliveryPersonOrderEntry.getOrderId()).child("place").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()){

                                                    final String placeActual = dataSnapshot.getValue(String.class);

                                                    db.getReference("OrdersListThumb").child(placeActual + "Thumb").child(deliveryPersonOrderEntry.getOrderId())

                                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.exists()) {
                                                                        final OrderThumb model = dataSnapshot.getValue(OrderThumb.class);

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

                                                                                Intent intent = new Intent(AssignedOrdersActivity.this, OrderDetailsActivity.class);
                                                                                intent.putExtra("orderId", model.getOrderId());
                                                                                intent.putExtra("source", "myOrders");

                                                                                startActivity(intent);
                                                                            }
                                                                        });

                                                                        viewHolder.statusT.setOnClickListener(new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                AlertDialog.Builder builder = new AlertDialog.Builder(AssignedOrdersActivity.this);
                                                                                builder.setTitle("Select order status");
                                                                                builder.setItems(states1, new DialogInterface.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(DialogInterface dialog, final int which) {

                                                                                        pd.show();

                                                                                        final String oldStatus = model.getOrderStatus();

                                                                                        //paymentstatus

                                                                                        final DatabaseReference paymentStatusRef = FirebaseDatabase.getInstance().getReference("OrdersList")
                                                                                                .child(placeActual)
                                                                                                .child(model.getOrderId()).child("paymentStatus");

                                                                                        final DatabaseReference paymentStatusRefThumb = FirebaseDatabase.getInstance().getReference("OrdersListThumb")
                                                                                                .child(placeActual + "Thumb")
                                                                                                .child(model.getOrderId()).child("paymentStatus");

                                                                                        final DatabaseReference userPaymentStatusRef = FirebaseDatabase.getInstance().getReference("UserOrders").child(model.getuId())
                                                                                                .child(model.getOrderId()).child("paymentStatus");

                                                                                        final DatabaseReference userPaymentStatusRefThumb = FirebaseDatabase.getInstance()
                                                                                                .getReference("UserOrdersThumb").child(model.getuId())
                                                                                                .child(model.getOrderId()).child("paymentStatus");

                                                                                        //orderstatus
                                                                                        final DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("OrdersList")
                                                                                                .child(placeActual)
                                                                                                .child(model.getOrderId()).child("orderStatus");

                                                                                        final DatabaseReference statusRefThumb = FirebaseDatabase.getInstance().getReference("OrdersListThumb")
                                                                                                .child(placeActual + "Thumb")
                                                                                                .child(model.getOrderId()).child("orderStatus");

                                                                                        final DatabaseReference statusStackAdminRef = FirebaseDatabase.getInstance().getReference("OrdersList")
                                                                                                .child(placeActual)
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

                                                                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                                                                    if (ds.child("statusName").exists() &&
                                                                                                            ds.child("statusName").getValue(String.class).equals("Cancelled")) {

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

                                                                                            Toast.makeText(AssignedOrdersActivity.this, "No Action on placed", Toast.LENGTH_SHORT).show();

                                                                                        } else if ("Confirmed".equals(states1[which])) {
                                                                                            Toast.makeText(AssignedOrdersActivity.this, "No Action on Confirmed", Toast.LENGTH_SHORT).show();
                                                                                        } else if ("Dispatched".equals(states1[which]))
                                                                                        {

                                                                                            status.setStatusName("Dispatched");
                                                                                            status.setDescription("Your order has been dispatched.");

                                                                                            if (states.get(oldStatus) > states.get("Confirmed")) {
                                                                                                Toast.makeText(AssignedOrdersActivity.this, "Cant reset dispatched", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                            else{
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
                                                                                                                        Toast.makeText(AssignedOrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                                                            Toast.makeText(AssignedOrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                            }
                                                                                        }
                                                                                        else if ("Delivered".equals(states1[which])) {

                                                                                            if (states.get(oldStatus) >= states.get("Delivered")) {
                                                                                                Toast.makeText(AssignedOrdersActivity.this, "can not reset delivered.Already set", Toast.LENGTH_SHORT).show();
                                                                                            }

                                                                                            else{
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
                                                                                                                                                                                            Toast.makeText(AssignedOrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                                                                        } else {
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
                                                                                                                                                                                Toast.makeText(AssignedOrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                                                                                                            Toast.makeText(AssignedOrdersActivity.this, "User node fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                                                                                                                                                Toast.makeText(AssignedOrdersActivity.this, "Admin fail" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                            }
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                                }
                                                                                                                            });

                                                                                                                        }

                                                                                                                    });

                                                                                                                }
                                                                                                            }
                                                                                                        });

                                                                                                    }
                                                                                                });
                                                                                            }




                                                                                        }
                                                                                        else if ("Cancelled".equals(states1[which])) {

                                                                                            
                                                                                            if(states.get(oldStatus)==states.get("Delivered")){
                                                                                                Toast.makeText(AssignedOrdersActivity.this, "Cant cancel order already delivered", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                            else if (states.get(oldStatus) == states.get("Cancelled")){
                                                                                                Toast.makeText(AssignedOrdersActivity.this, "Already cancelled order", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                            else{
                                                                                                //todo confirmation dialog
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
                                                                                                                                                Toast.makeText(AssignedOrdersActivity.this, "User node failed to update status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                                                                                            } else {
                                                                                                                                                db.getReference("OrderMapping").child(model.getOrderId()).child("place").setValue("cancelledOrders");


                                                                                                                                                db.getReference("OrdersList").child("activeOrders").child(model.getOrderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                                                                    @Override
                                                                                                                                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                                                                        db.getReference("OrdersList").child("cancelledOrders").child(model.getOrderId()).setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                                            @Override
                                                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                                                    db.getReference("OrdersList").child("cancelledOrders").child(model.getOrderId()).child("orderData").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                                                                                        @Override
                                                                                                                                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                                                                                            //fetch order data and update count to book location


                                                                                                                                                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                                                                                                                                                String itemId = ds.child("itemId").getValue(String.class);
                                                                                                                                                                                String itemLocation = ds.child("itemLocation").getValue(String.class);
                                                                                                                                                                                final Integer quantity = ds.child("quantity").getValue(Integer.class);

                                                                                                                                                                                db.getReference(itemLocation).child(itemId).child("count").runTransaction(new Transaction.Handler() {
                                                                                                                                                                                    @NonNull
                                                                                                                                                                                    @Override
                                                                                                                                                                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {


                                                                                                                                                                                        Integer count = mutableData.getValue(Integer.class);

                                                                                                                                                                                        if (count != null) {
                                                                                                                                                                                            count = count + quantity;
                                                                                                                                                                                            mutableData.setValue(count);
                                                                                                                                                                                            // Toast.makeText(BookList2PSActivity.this, "Success", Toast.LENGTH_SHORT).show();

                                                                                                                                                                                            return Transaction.success(mutableData);
                                                                                                                                                                                        } else {
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
                                                                                                                                    Toast.makeText(AssignedOrdersActivity.this, "Admin node failed to update status: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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


                                                                                        } else {
                                                                                            Log.d("abccd", "status is not matching " + states.get(states1[which]));
                                                                                        }
                                                                                    }
                                                                                });
                                                                                builder.show();
                                                                            }
                                                                        });

                                                                    }
                                                                    else {
                                                                        Toast.makeText(AssignedOrdersActivity.this, "Orderthumb Doesn't exist in database : " ,Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                }
                                                            });

                                                }
                                                else{
                                                    Toast.makeText(AssignedOrdersActivity.this, "OrderMapping Doesn't exist in database : " ,Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        };

        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
