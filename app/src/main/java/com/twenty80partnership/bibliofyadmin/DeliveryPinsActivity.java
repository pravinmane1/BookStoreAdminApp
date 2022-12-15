package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.opengl.ETC1;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofyadmin.models.Delivery;
import com.twenty80partnership.bibliofyadmin.viewHolders.DeliveryViewHolder;


public class DeliveryPinsActivity extends AppCompatActivity {

    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private MaterialSearchBar materialSearchBar;
    private FirebaseDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_pins);

        db = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Pins");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        floatingActionButton = findViewById(R.id.add_new_pin);
        recyclerView = findViewById(R.id.recycler_view);
        materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    Query query = db.getReference("Delivery").child("pin").orderByChild("pin");
                    firebaseSearch(query);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

                Query query = db.getReference("Delivery").child("pin").orderByChild("pin").equalTo(text.toString());//.orderByChild("pin").startAt(text.toString()).endAt(text.toString()+"\uf8ff");
                firebaseSearch(query);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(DeliveryPinsActivity.this);
                LayoutInflater layoutInflater = DeliveryPinsActivity.this.getLayoutInflater();
                final View customDialog = layoutInflater.inflate(R.layout.add_delivery_pin_dialog,null);

                alert.setView(customDialog);

                final EditText etPincode = customDialog.findViewById(R.id.pincode);
                final EditText etDaysForDelivery = customDialog.findViewById(R.id.days_for_delivery);
                final EditText etDeliveryCharges = customDialog.findViewById(R.id.delivery_charges);
                Button btnSave = customDialog.findViewById(R.id.btn_save);

                final AlertDialog alertDialog = alert.create();

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String pincode = etPincode.getText().toString();

                        
                        if(pincode.length()==6 &&  etDaysForDelivery.getText()!=null && etDeliveryCharges.getText()!=null
                         && !etDaysForDelivery.getText().toString().isEmpty()
                                && !etDeliveryCharges.getText().toString().isEmpty()){

                            int daysForDelivery = Integer.parseInt(etDaysForDelivery.getText().toString());
                            int deliveryCharges = Integer.parseInt(etDeliveryCharges.getText().toString());
                            Delivery delivery = new Delivery(pincode,daysForDelivery,deliveryCharges);

                            final ProgressDialog pd = new ProgressDialog(DeliveryPinsActivity.this);
                            pd.setMessage("Saving...");
                            pd.setCancelable(false);
                            pd.show();

                            db.getReference("Delivery").child("pin").child(pincode).setValue(delivery).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        pd.dismiss();
                                        Toast.makeText(DeliveryPinsActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                        alertDialog.dismiss();
                                    }
                                    else{
                                        pd.dismiss();
                                        Toast.makeText(DeliveryPinsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                        else{
                            Toast.makeText(DeliveryPinsActivity.this, "data is empty or incomplete", Toast.LENGTH_SHORT).show();
                        }
                      

                    }
                });


                alertDialog.show();


            }
        });

        //show pinlist

        recyclerView = findViewById(R.id.recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        Query query = db.getReference("Delivery").child("pin");

        firebaseSearch(query);

    }

    private void firebaseSearch(Query query) {


        FirebaseRecyclerAdapter<Delivery, DeliveryViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Delivery, DeliveryViewHolder>(
                        Delivery.class,R.layout.delivery_row,DeliveryViewHolder.class,query) {
            @Override
            protected void populateViewHolder(DeliveryViewHolder deliveryViewHolder, final Delivery delivery, int i) {

                deliveryViewHolder.setDetails(delivery.getPin(),delivery.getDaysForDelivery(),delivery.getDeliveryCharges());

            deliveryViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryPinsActivity.this);
                    LayoutInflater layoutInflater = DeliveryPinsActivity.this.getLayoutInflater();
                    View customDialog = layoutInflater.inflate(R.layout.add_delivery_pin_dialog,null);
                    builder.setView(customDialog);

                    builder.setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            db.getReference("Delivery").child("pin").child(String.valueOf(delivery.getPin())).removeValue();
                        }
                    });

                    final EditText etPincode = customDialog.findViewById(R.id.pincode);
                    etPincode.setEnabled(false);
                    final EditText etDaysForDelivery = customDialog.findViewById(R.id.days_for_delivery);
                    final EditText etDeliveryCharges = customDialog.findViewById(R.id.delivery_charges);
                    Button btnSave = customDialog.findViewById(R.id.btn_save);

                    etPincode.setText(delivery.getPin());
                    etDaysForDelivery.setText(delivery.getDaysForDelivery().toString());
                    etDeliveryCharges.setText(delivery.getDeliveryCharges().toString());


                    final AlertDialog alertDialog = builder.create();

                    btnSave.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String pincode = etPincode.getText().toString();
                            int daysForDelivery = Integer.parseInt(etDaysForDelivery.getText().toString());
                            int deliveryCharges = Integer.parseInt(etDeliveryCharges.getText().toString());

                            Delivery delivery = new Delivery(pincode,daysForDelivery,deliveryCharges);
                            db.getReference("Delivery").child("pin").child(pincode).setValue(delivery);
                            alertDialog.dismiss();

                        }
                    });


                    alertDialog.show();



                }
            });
            }
        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}