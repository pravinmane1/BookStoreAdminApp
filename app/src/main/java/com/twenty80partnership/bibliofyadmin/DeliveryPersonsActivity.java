package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.twenty80partnership.bibliofyadmin.models.DeliveryPerson;
import com.twenty80partnership.bibliofyadmin.viewHolders.DeliveryPersonViewHolder;
import com.twenty80partnership.bibliofyadmin.viewHolders.DeliveryViewHolder;


public class DeliveryPersonsActivity extends AppCompatActivity {

    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;
    private MaterialSearchBar materialSearchBar;
    private FirebaseDatabase db;
    private String mode,uUid,oid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_persons);

        Intent i = getIntent();

        if (i.getStringExtra("mode")!=null){
            mode = i.getStringExtra("mode");
            uUid = i.getStringExtra("uUid");
            oid = i.getStringExtra("oid");
        }
        else {
            mode = "regular";
        }

        db = FirebaseDatabase.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Delivery Persons");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        floatingActionButton = findViewById(R.id.fab_add_new_person);
        recyclerView = findViewById(R.id.recycler_view);
        materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                  //  Query query = db.getReference("Delivery").child("pin").orderByChild("pin");
                   // firebaseSearch(query);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {

               // Query query = db.getReference("Delivery").child("pin").orderByChild("pin").equalTo(text.toString());//.orderByChild("pin").startAt(text.toString()).endAt(text.toString()+"\uf8ff");
               // firebaseSearch(query);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(DeliveryPersonsActivity.this);
                LayoutInflater layoutInflater = DeliveryPersonsActivity.this.getLayoutInflater();
                final View customDialog = layoutInflater.inflate(R.layout.add_delivery_person,null);

                alert.setView(customDialog);

                final EditText etName = customDialog.findViewById(R.id.et_name);
                final EditText etContact = customDialog.findViewById(R.id.et_no);
                final EditText etEmail = customDialog.findViewById(R.id.et_email);
                Button btnSave = customDialog.findViewById(R.id.btn_save);

                final AlertDialog alertDialog = alert.create();

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        if( etContact.getText()!=null && etEmail.getText()!=null && etName.getText()!=null
                                && !etContact.getText().toString().isEmpty()
                                && !etEmail.getText().toString().isEmpty()
                                && !etName.getText().toString().isEmpty()){



                            final ProgressDialog pd = new ProgressDialog(DeliveryPersonsActivity.this);
                            pd.setMessage("Saving...");
                            pd.setCancelable(false);
                            pd.show();

                            String key = db.getReference("DeliveryPersons").push().getKey();

                            DeliveryPerson deliveryPerson = new DeliveryPerson();
                            deliveryPerson.setName(etName.getText().toString());
                            deliveryPerson.setContact(etContact.getText().toString());
                            deliveryPerson.setEmail(etEmail.getText().toString());
                            deliveryPerson.setUid(key);

                            db.getReference("DeliveryPersons").child(key).setValue(deliveryPerson).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        pd.dismiss();
                                        Toast.makeText(DeliveryPersonsActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                        alertDialog.dismiss();
                                    }
                                    else{
                                        pd.dismiss();
                                        Toast.makeText(DeliveryPersonsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });


                        }
                        else{
                            Toast.makeText(DeliveryPersonsActivity.this, "data is empty or incomplete", Toast.LENGTH_SHORT).show();
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

        Query query = db.getReference("DeliveryPersons");

        firebaseSearch(query);

    }

    private void firebaseSearch(Query query) {


        FirebaseRecyclerAdapter<DeliveryPerson, DeliveryPersonViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<DeliveryPerson, DeliveryPersonViewHolder>(
                        DeliveryPerson.class,R.layout.delivery_person_row,DeliveryPersonViewHolder.class,query) {
                    @Override
                    protected void populateViewHolder(DeliveryPersonViewHolder deliveryViewHolder, final DeliveryPerson deliveryPerson, int i) {

                        deliveryViewHolder.setDetails(deliveryPerson.getName(),deliveryPerson.getContact(),deliveryPerson.getEmail());

                        deliveryViewHolder.view.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                if (mode.equals("regular")){

                                    AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryPersonsActivity.this);
                                    LayoutInflater layoutInflater = DeliveryPersonsActivity.this.getLayoutInflater();
                                    View customDialog = layoutInflater.inflate(R.layout.add_delivery_person,null);
                                    builder.setView(customDialog);

                                    builder.setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            db.getReference("DeliveryPersons").child(deliveryPerson.getUid()).removeValue();
                                        }
                                    });

                                    final EditText etPincode = customDialog.findViewById(R.id.et_name);
                                    final EditText etDaysForDelivery = customDialog.findViewById(R.id.et_no);
                                    final EditText etDeliveryCharges = customDialog.findViewById(R.id.et_email);
                                    Button btnSave = customDialog.findViewById(R.id.btn_save);

                                    etPincode.setText(deliveryPerson.getName());
                                    etDaysForDelivery.setText(deliveryPerson.getContact());
                                    etDeliveryCharges.setText(deliveryPerson.getEmail());


                                    final AlertDialog alertDialog = builder.create();

                                    btnSave.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String pincode = etPincode.getText().toString();
                                            String daysForDelivery = etDaysForDelivery.getText().toString();
                                            String deliveryCharges = etDeliveryCharges.getText().toString();

                                            DeliveryPerson delivery = new DeliveryPerson(pincode,deliveryCharges,daysForDelivery,deliveryPerson.getUid());
                                            db.getReference("DeliveryPersons").child(deliveryPerson.getUid()).setValue(delivery);
                                            alertDialog.dismiss();

                                        }
                                    });


                                    alertDialog.show();

                                }
                                return false;
                            }
                        });

                        deliveryViewHolder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (mode.equals("regular")){
                                    Intent ordersAssignedIntent = new Intent(DeliveryPersonsActivity.this,AssignedOrdersActivity.class);
                                    ordersAssignedIntent.putExtra("dUid",deliveryPerson.getUid());
                                    startActivity(ordersAssignedIntent);
                                }
                                else{
                                    Intent rIntent = new Intent();
                                    rIntent.putExtra("dUid",deliveryPerson.getUid());
                                    rIntent.putExtra("uUid",uUid);
                                    rIntent.putExtra("oid",oid);
                                    rIntent.putExtra("number",deliveryPerson.getContact());
                                    setResult(RESULT_OK,rIntent);
                                    finish();
                                }



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