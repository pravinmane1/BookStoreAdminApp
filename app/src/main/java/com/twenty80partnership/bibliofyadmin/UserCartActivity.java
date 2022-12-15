package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.models.CartItem;
import com.twenty80partnership.bibliofyadmin.models.PriceDetails;
import com.twenty80partnership.bibliofyadmin.models.StationaryItem;
import com.twenty80partnership.bibliofyadmin.viewHolders.CartItemViewHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UserCartActivity extends AppCompatActivity {
    String uid;

    private ShimmerFrameLayout mShimmerViewContainer;
    private RecyclerView bookList,stationaryList;
    private TextView totalItems, amountItems, amountDelivery, amountTotal, amountSavings, payableAmount,continueBtn;
    private CardView priceDetails, bottom,addressCard,suggestionCard;
    private ProgressDialog pricePd,pd;
    private ImageView noItemInCart,edit;

    private DatabaseReference cartRef,cartRequestRef, rootRef, wishlistRef, priceDetailsRef, deliveryRef,addressesRef;
    private ValueEventListener priceDetailsUpdateListener,countListener;

    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
    private Integer delivery, amountItemsInt, amountTotalInt, savings, totalItemsInt;
    private String bookPath;
    private Date currentTime;
    Long cartChildrenCount=0L;
    private ValueEventListener showAndClearListener;
    private DatabaseReference cartItemCount;
    private Toolbar toolbar;
    private String id;
    private String addressId;
    private boolean changeAddress = false;
    private PriceDetails details;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_cart);

        uid = getIntent().getStringExtra("uId");
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        //start shimmer as soon as activity starts
        mShimmerViewContainer.startShimmerAnimation();

        //set up pricePd
        pricePd = new ProgressDialog(UserCartActivity.this);
        pricePd.setMessage("Loading Price Details");
        pricePd.setCancelable(false);

        //set toolbar as actionBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        noItemInCart = findViewById(R.id.no_item_in_cart);
        bottom = findViewById(R.id.bottom);
        priceDetails = findViewById(R.id.price_details);
        bookList = findViewById(R.id.recycler_view);
        stationaryList = findViewById(R.id.recycler_view_stationary);
        addressCard = findViewById(R.id.address_card);
        suggestionCard = findViewById(R.id.suggestion_card);
        edit = findViewById(R.id.edit);
        Button useAnotherAddress = findViewById(R.id.use_another_address);

        bookList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        bookList.setLayoutManager(mLayoutManager);

        stationaryList.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        stationaryList.setLayoutManager(layoutManager);

        //by default pd bottom and suggestion card are invisible.
        priceDetails.setVisibility(View.GONE);
        bottom.setVisibility(View.GONE);
        suggestionCard.setVisibility(View.GONE);

        //set up references
        cartRef = FirebaseDatabase.getInstance().getReference("Cart").child(uid);
        cartRequestRef = FirebaseDatabase.getInstance().getReference("CartReq").child(uid);
        rootRef = FirebaseDatabase.getInstance().getReference();
        wishlistRef = FirebaseDatabase.getInstance().getReference("Wishlist").child(uid);
        priceDetailsRef = FirebaseDatabase.getInstance().getReference("PriceDetails").child(uid);
        deliveryRef = FirebaseDatabase.getInstance().getReference("Delivery");
        cartItemCount = FirebaseDatabase.getInstance().getReference("PriceDetails").child(uid).child("count");
        addressesRef = FirebaseDatabase.getInstance().getReference("Addresses").child(uid);


        //show cart items added according to time added
        firebaseSearch(cartRef);


        //when function count (priceDetails) and user defined (cart requests) count are equal then it shows the data
        //(After checking if there is item in cart)
        countListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("priced",cartChildrenCount+" "+dataSnapshot.getValue());

                //display data when pricedetails count and cartreq becomes equals
                if (cartChildrenCount == dataSnapshot.getValue() && cartChildrenCount!=0){

                    noItemInCart.setVisibility(View.GONE);
                    priceDetails.setVisibility(View.VISIBLE);
                    bottom.setVisibility(View.VISIBLE);
                    bookList.setVisibility(View.VISIBLE);
                    stationaryList.setVisibility(View.VISIBLE);
                    suggestionCard.setVisibility(View.VISIBLE);

                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmerAnimation();
                    pricePd.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserCartActivity.this,"countlistener"+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();

            }
        };

        //to check if there is no item in cart and if there then set listener to show data.
        showAndClearListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                cartChildrenCount = 0L;

                if (dataSnapshot.child("stationary").exists()){

                    for(DataSnapshot stationarySnapshot: dataSnapshot.child("stationary").getChildren()){
                        cartChildrenCount =cartChildrenCount + stationarySnapshot.child("quantity").getValue(Long.class);
                    }
                    //  cartChildrenCount = dataSnapshot.child("books").getChildrenCount() + dataSnapshot.child("stationary").getChildrenCount();
                }

                if(dataSnapshot.child("books").exists()) {

                    for(DataSnapshot booksSnapshot: dataSnapshot.child("books").getChildren()){
                        //Log.d("data",booksSnapshot.toString()+"\n");
                        cartChildrenCount =cartChildrenCount + booksSnapshot.child("quantity").getValue(Long.class);
                    }
                }

                //if there is no item in cart request
                if (cartChildrenCount == 0) {

                    bookList.setVisibility(View.GONE);
                    stationaryList.setVisibility(View.GONE);
                    priceDetails.setVisibility(View.GONE);
                    suggestionCard.setVisibility(View.GONE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmerAnimation();
                    noItemInCart.setVisibility(View.VISIBLE);
                    pricePd.dismiss();

                }

                //if there are item in cart
                else {
                    cartItemCount.addValueEventListener(countListener);

                    //initially this makes empty card invisible
                    noItemInCart.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserCartActivity.this,"show and clear"+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        };

        cartRequestRef.addValueEventListener(showAndClearListener);

        showPriceDetails();

    }


    private void showPriceDetails() {

        totalItems = findViewById(R.id.total_items);
        amountTotal = findViewById(R.id.amount_total);
        amountDelivery = findViewById(R.id.amount_delivery);
        amountItems = findViewById(R.id.amount_items);
        amountSavings = findViewById(R.id.amount_savings);
        payableAmount = findViewById(R.id.payable_amount);

        priceDetailsUpdateListener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("showing", "update price detail event listener called");

                //show progress dialogue as soon as there is change in price
//                if (!pricePd.isShowing() && pricePd != null) {
////                    mShimmerViewContainer.startShimmerAnimation();
////                    mShimmerViewContainer.setVisibility(View.VISIBLE);
//                    //pricePd.show();
//                }
                //1 count for total items in cart
                details = dataSnapshot.getValue(PriceDetails.class);

                //totalItemsInt = dataSnapshot.child("count").getValue(Integer.class);

                // if count is zero then hide pricedetails bottom and show noItemInCart Picture
                if (details.getCount() == 0) {
                    priceDetails.setVisibility(View.GONE);
                    bottom.setVisibility(View.GONE);
                    noItemInCart.setVisibility(View.VISIBLE);

                }

                //set total items
                totalItems.setText("Price (" + details.getCount()+ " items)");

                //2
                //set total amount
                //amountItemsInt = dataSnapshot.child("amountDiscounted").getValue(Integer.class);
                amountItems.setText("₹ " + details.getAmountDiscounted());

                //3
                //set total savings
                //savings = dataSnapshot.child("amountOriginal").getValue(Integer.class) - amountItemsInt;
                savings = details.getAmountOriginal() - details.getAmountDiscounted();
                amountSavings.setText("You will save ₹ " + savings + " on this order");

                //check for delivery rates
                deliveryRef.child("basic").child("rate").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //4
                        //set delivery amount
                        delivery = dataSnapshot.getValue(Integer.class);
                        amountDelivery.setText(delivery.toString());

                        //5
                        //set total with added delivery
                        amountTotalInt = delivery + details.getAmountDiscounted();
                        amountTotal.setText("₹ " + amountTotalInt.toString());

                        //6
                        //set amount to bottom
                        payableAmount.setText("₹ " + amountTotalInt.toString());


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(UserCartActivity.this,"delivery ref"+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UserCartActivity.this,"priceDetailsUpdateListener"+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
            }
        };


        priceDetailsRef.addValueEventListener(priceDetailsUpdateListener);


    }

    private void firebaseSearch(DatabaseReference q) {


        FirebaseRecyclerAdapter<CartItem, CartItemViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<CartItem, CartItemViewHolder>(
                CartItem.class, R.layout.cart_item_row, CartItemViewHolder.class, q.child("books").orderByChild("timeAdded")
        ) {

            @Override
            protected CartItem parseSnapshot(DataSnapshot snapshot) {
                return super.parseSnapshot(snapshot);
            }

            @Override
            protected void populateViewHolder(final CartItemViewHolder viewHolder, final CartItem model, final int position) {

                final String bookId = model.getItemId();
                final String bookLocation = model.getItemLocation();



                //add listener to obtained path of book ,to set the data of book
                rootRef.child(bookLocation).child(bookId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        Book book;
                        book = dataSnapshot.getValue(Book.class);
                        model.setItemName(book.getName());
                        model.setItemAuthor(book.getAuthor());
                        model.setItemPublication(book.getPublication());
                        model.setItemImg(book.getImg());
                        model.setItemOriginalPrice(book.getOriginalPrice());
                        model.setItemDiscountedPrice(book.getDiscountedPrice());
                        model.setItemDiscount(book.getDiscount());

                        Log.d("showing", "capture " + dataSnapshot.toString());

                        viewHolder.setDetails(model.getItemName(), "Author: "+model.getItemAuthor(), model.getItemPublication(), model.getItemImg(),
                                model.getItemOriginalPrice(), model.getItemDiscountedPrice(), model.getItemDiscount(), getApplicationContext(), model.getQuantity(),"book");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(UserCartActivity.this,"rootRef"+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
                    }
                });



                    //remove button listener
                    viewHolder.remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pricePd.show();
                            //pricePd is dismissed when there is update to price of data by cloud function
                            String id = model.getItemId();

                            bookList.setVisibility(View.GONE);
                            stationaryList.setVisibility(View.GONE);
                            priceDetails.setVisibility(View.GONE);
                            bottom.setVisibility(View.GONE);
                            mShimmerViewContainer.startShimmerAnimation();
                            mShimmerViewContainer.setVisibility(View.VISIBLE);

                            cartRequestRef.child("books").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()){
                                        Toast.makeText(UserCartActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }
                                }
                            });
                        }
                    });

                    //wishlist button listener
                    viewHolder.wishlist.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pricePd.show();
                            //pricePd is dismissed when there is update to price of data by cloud function
                            currentTime = Calendar.getInstance().getTime();
                            String date = dateFormat.format(currentTime);

                            final String id = model.getItemId();
                            cartRequestRef.child("books").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()){
                                        Toast.makeText(UserCartActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }
                                }
                            });
                            wishlistRef.child(id).setValue(Long.parseLong(date));
                        }
                    });





            }
        };


        bookList.setAdapter(firebaseRecyclerAdapter);


        FirebaseRecyclerAdapter<CartItem, CartItemViewHolder> firebaseRecyclerAdapterS = new FirebaseRecyclerAdapter<CartItem, CartItemViewHolder>(
                CartItem.class, R.layout.cart_item_row, CartItemViewHolder.class, q.child("stationary").orderByChild("timeAdded")
        ) {

            @Override
            protected CartItem parseSnapshot(DataSnapshot snapshot) {
                return super.parseSnapshot(snapshot);
            }

            @Override
            protected void populateViewHolder(final CartItemViewHolder viewHolder, final CartItem model, final int position) {

                final String stationaryId = model.getItemId();
                final String stationaryLocation = model.getItemLocation();

                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                uid = mAuth.getCurrentUser().getUid();

                ValueEventListener quantityListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        viewHolder.quantity.setText(dataSnapshot.child("quantity").getValue(Integer.class).toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                rootRef.child("Cart").child(uid).child("stationary").child(stationaryId).addListenerForSingleValueEvent(quantityListener);

                //add listener to obtained path of book ,to set the data of book
                rootRef.child(stationaryLocation).child(stationaryId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        StationaryItem stationaryItem;
                        stationaryItem = dataSnapshot.getValue(StationaryItem.class);
                        model.setItemName(stationaryItem.getName());
                        model.setItemPublication(stationaryItem.getAtr1());

                        model.setItemImg(stationaryItem.getImg());
                        model.setItemOriginalPrice(stationaryItem.getOriginalPrice());
                        model.setItemDiscountedPrice(stationaryItem.getDiscountedPrice());
                        model.setItemDiscount(stationaryItem.getDiscount());

                        Log.d("showing", "capture " + dataSnapshot.toString());

                        viewHolder.setDetails(model.getItemName(), model.getItemAuthor(), model.getItemPublication(), model.getItemImg(),
                                model.getItemOriginalPrice(), model.getItemDiscountedPrice(), model.getItemDiscount(), getApplicationContext(), model.getQuantity(),"stationary");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(UserCartActivity.this,"rootRef"+databaseError.toException().toString(),Toast.LENGTH_SHORT).show();
                    }
                });


                viewHolder.plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(model.getQuantity()<101){
                            pricePd.show();
                            final Integer qUpdate = model.getQuantity()+1;
                            viewHolder.quantity.setText(qUpdate.toString());

                            cartRequestRef.child("stationary").child(model.getItemId()).child("quantity").setValue(qUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()){
                                        Toast.makeText(UserCartActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }

                                }
                            });
                        }
                        else {
                            Toast.makeText(UserCartActivity.this,"Quantity can't be greater than 100",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                viewHolder.minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (model.getQuantity()>1){
                            pricePd.show();
                            final Integer qUpdate = model.getQuantity()-1;
                            viewHolder.quantity.setText(qUpdate.toString());

                            cartRequestRef.child("stationary").child(model.getItemId()).child("quantity").setValue(qUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()){
                                        Toast.makeText(UserCartActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        pricePd.dismiss();
                                    }

                                }
                            });
                        }
                        else {
                            Toast.makeText(UserCartActivity.this,"Quantity can't be less than 1",Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        };


        stationaryList.setAdapter(firebaseRecyclerAdapterS);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }



    @Override
    protected void onDestroy() {
        priceDetailsRef.removeEventListener(priceDetailsUpdateListener);
        cartRequestRef.removeEventListener(showAndClearListener);
        cartItemCount.removeEventListener(countListener);
        super.onDestroy();
    }


}
