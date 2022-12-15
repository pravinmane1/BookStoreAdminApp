package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.twenty80partnership.bibliofyadmin.models.ApplicableTerm;
import com.twenty80partnership.bibliofyadmin.models.Item;
import com.twenty80partnership.bibliofyadmin.models.MenuItem;
import com.twenty80partnership.bibliofyadmin.models.TermListing;
import com.twenty80partnership.bibliofyadmin.viewHolders.ItemViewHolder;
import com.twenty80partnership.bibliofyadmin.viewHolders.TermListingViewHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AllTermsActivity extends AppCompatActivity {
    CardView cvAddNewTerm;
    RecyclerView itemList;
    DatabaseReference SPPUbooksOrCodesListingRef;

    Button save;
    EditText id;
    private ProgressDialog progressDialog;
    private AlertDialog dialog;
    private EditText name, topic;
    String course;
    String courseName,mode;
    DatabaseReference SPPUtermsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_terms);


        mode = getIntent().getStringExtra("mode");
        course = getIntent().getStringExtra("course");
        courseName = getIntent().getStringExtra("courseName");




        if (! Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(AllTermsActivity.this);
            alert.setTitle("No internet Connection");
            alert.setCancelable(false);
            alert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (! Common.isConnectedToInternet(getApplicationContext())){
                        alert.show();
                    }
                }
            });

            alert.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = alert.create();
            dialog.show();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        cvAddNewTerm = findViewById(R.id.cv_add_new_term);
        TextView tvAddNewTerm = findViewById(R.id.tv_add_new_term);
        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(this,2));


         if (mode.equals("codes")){
            toolbar.setTitle("All Terms");
            tvAddNewTerm.setText("Add new Term");
            SPPUbooksOrCodesListingRef = FirebaseDatabase.getInstance().getReference("SPPUtermsListing")
                    .child(getIntent().getStringExtra("course"));

            SPPUtermsRef =  FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
                    .child("codes").child(getIntent().getStringExtra("course"));

            Query query = SPPUbooksOrCodesListingRef.orderByChild("priority");
            firebaseCategorySearch(query);
        }
        else if (mode.equals("select")){
            toolbar.setTitle("Select a Term");
            cvAddNewTerm.setVisibility(View.GONE);
            SPPUbooksOrCodesListingRef = FirebaseDatabase.getInstance().getReference("SPPUtermsListing")
                    .child(getIntent().getStringExtra("course"));

            SPPUtermsRef =  FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
                    .child("codes").child(getIntent().getStringExtra("course"));

            Query query = SPPUbooksOrCodesListingRef.orderByChild("priority");
            firebaseCategorySearch(query);
        }


        cvAddNewTerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder alert = new AlertDialog.Builder(AllTermsActivity.this);

                alert.setCancelable(false);
                //alert.setMessage("sample message");

                LayoutInflater inflater = AllTermsActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_term_dialog,null);
                alert.setView(customDialog);
                name = (EditText)customDialog.findViewById(R.id.name);

                id = (EditText)customDialog.findViewById(R.id.id);

                topic =(EditText) customDialog.findViewById(R.id.topic);
                save = customDialog.findViewById(R.id.btn_save);


                    alert.setTitle("Add New Term");
                    name.setHint("Name of Term");
                    id.setHint("Id for Term Name");
                    save.setText("save");
                    topic.setHint("Topic Name for DropDown");

                alert.setIcon(R.drawable.plus_green);

                alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });

                dialog = alert.create();
                dialog.show();

                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       if (!name.getText().toString().isEmpty() && !id.getText().toString().isEmpty() && !topic.getText().toString().isEmpty()){
                            Item item= new Item();
                            item.setName(name.getText().toString());
                            item.setId(id.getText().toString());
                            item.setTopic(topic.getText().toString());
                            DatabaseReference codeListingRef = FirebaseDatabase.getInstance().getReference("SPPUtermsListing").child(course);
                            codeListingRef.child(item.getId()).setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(AllTermsActivity.this,"term added successfully",Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(AllTermsActivity.this,"Data is empty",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }


    public void firebaseCategorySearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<TermListing, TermListingViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<TermListing, TermListingViewHolder>(
                TermListing.class, R.layout.all_term_row, TermListingViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final TermListingViewHolder viewHolder, final TermListing model, final int position) {


                viewHolder.setDetails(model.getName(),mode);

                viewHolder.itemCard.setOnClickListener(v -> {

                   if (mode.equals("codes")){
                        Intent intent = new Intent(AllTermsActivity.this,CodeListActivity.class);
                        intent.putExtra("termId",model.getId());
                        intent.putExtra("termName",model.getName());
                        intent.putExtra("course",getIntent().getStringExtra("course"));
                        startActivity(intent);
                    }else if (mode.equals("select")){
                        Intent intent = new Intent();
                        intent.putExtra("id",model.getId());
                        setResult(RESULT_OK, intent);
                        finish();
                    }


                });



                viewHolder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.more);
                        //inflating menu from xml resource
                            popup.inflate(R.menu.book_options_menu);
                            //adding click listener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(android.view.MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.delete:
                                            SPPUbooksOrCodesListingRef.child(model.getId()).removeValue();
                                            SPPUtermsRef.child(model.getId()).removeValue();
                                            return true;

                                        case R.id.edit:
                                            updateCategory(model);
                                            return true;

                                        default:
                                            return false;
                                    }
                                }

                            });
                        //displaying the popup
                        popup.show();
                    }
                });
            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    private void updateCategory(final TermListing item) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(AllTermsActivity.this);
        LayoutInflater inflater = AllTermsActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_term_dialog,null);
        alert.setView(customDialog);
        name = (EditText)customDialog.findViewById(R.id.name);
        id = (EditText)customDialog.findViewById(R.id.id);
        topic =(EditText) customDialog.findViewById(R.id.topic);
        save = customDialog.findViewById(R.id.btn_save);
        alert.setCancelable(false);

        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

            alert.setTitle("Edit Term Type");
            name.setHint("Name of Term");
            id.setHint("Id for Term name");
            topic.setHint("Topic Name");

            name.setText(item.getName());
            id.setText(item.getId());
            id.setFocusable(false);
            id.setEnabled(false);
            id.setCursorVisible(false);

            topic.setText(item.getTopic());

            save.setText("update");


        dialog = alert.create();
        dialog.show();




        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (!name.getText().toString().isEmpty() && !id.getText().toString().isEmpty() && !topic.getText().toString().isEmpty()){
                        Item item= new Item();
                        item.setName(name.getText().toString());
                        item.setId(id.getText().toString());
                        item.setTopic(topic.getText().toString());
                        DatabaseReference codeListingRef = FirebaseDatabase.getInstance().getReference("SPPUtermsListing").child(course);
                        codeListingRef.child(item.getId()).setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(AllTermsActivity.this,"term updated successfully",Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            }
                        });
                    }
                    else {
                        Toast.makeText(AllTermsActivity.this,"Data is empty",Toast.LENGTH_SHORT).show();
                    }


            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
