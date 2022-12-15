package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.models.StationaryItem;
import com.twenty80partnership.bibliofyadmin.viewHolders.BookViewHolder;
import com.twenty80partnership.bibliofyadmin.viewHolders.StationaryItemViewHolder;

public class StationaryListActivity extends AppCompatActivity {
    String category,categoryName;
    RecyclerView itemList;
    EditText id;
    private FloatingActionButton mFloatingActionButton;
    MaterialSearchBar materialSearchBar;
    private Query query;
    private String searchName;
    private DatabaseReference SPPUstationaryRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary_list);



        if (! Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(StationaryListActivity.this);
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


        category = getIntent().getStringExtra("category");
        categoryName = getIntent().getStringExtra("categoryName");
        searchName = getIntent().getStringExtra("searchName");

        SPPUstationaryRef = FirebaseDatabase.getInstance().getReference("SPPUstationary").child(category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(categoryName);

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);
        materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled){
                    query = SPPUstationaryRef.orderByChild("priority");
                    firebaseSearch(query);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                query = SPPUstationaryRef.orderByChild("priority").startAt(text.toString()).endAt(text.toString()+"\uf8ff");
                firebaseSearch(query);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(this,2));



        Query query = SPPUstationaryRef.orderByChild("searchName");

        firebaseSearch(query);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(StationaryListActivity.this,StationaryDetailsActivity.class);
                intent.putExtra("mode","new");
                intent.putExtra("categoryName",categoryName);
                intent.putExtra("category",category);
                intent.putExtra("searchName",searchName);
                startActivity(intent);
            }
        });



    }


    public void firebaseSearch(Query q) {


        FirebaseRecyclerAdapter<StationaryItem, StationaryItemViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<StationaryItem, StationaryItemViewHolder>(
                StationaryItem.class, R.layout.stationary_item_row, StationaryItemViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final StationaryItemViewHolder viewHolder, final StationaryItem model, final int position) {


                viewHolder.setDetails(model.getName(),model.getImg(),getApplicationContext(),model.getAtr1(),model.getAtr2(),model.getAtr3(),
                        model.getAtr4(),model.getDiscount(),model.getDiscountedPrice()
                        ,model.getOriginalPrice(),model.getAvailability(),model.getCount());

                if (model.getCount() == null) {
                    Toast.makeText(StationaryListActivity.this, "Count does not exist setting it to 0", Toast.LENGTH_SHORT).show();
                    SPPUstationaryRef.child(model.getId()).child("count").setValue(0);
                }

                viewHolder.tvAddBooks.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(StationaryListActivity.this);
                        LayoutInflater inflater =  StationaryListActivity.this.getLayoutInflater();

                        View customDialog = inflater.inflate(R.layout.add_number_count_dialog,null);
                        builder.setView(customDialog);

                        final EditText number = customDialog.findViewById(R.id.number);


                        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SPPUstationaryRef.child(model.getId()).child("count").runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                                        Integer count = mutableData.getValue(Integer.class);
                                        if (count != null) {
                                            count = count + Integer.valueOf(number.getText().toString());
                                            mutableData.setValue(count);
                                            // Toast.makeText(BookListActivity.this, "Success", Toast.LENGTH_SHORT).show();

                                            return Transaction.success(mutableData);
                                        }else{
                                            // Toast.makeText(BookListActivity.this, "Invalid action", Toast.LENGTH_SHORT).show();
                                        }
                                        return Transaction.abort();
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                                    }
                                });
                            }
                        });

                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        AlertDialog alertDialog = builder.create();

                        alertDialog.show();

                    }
                });


                viewHolder.tvRemoveBooks.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(StationaryListActivity.this);
                        LayoutInflater inflater =  StationaryListActivity.this.getLayoutInflater();

                        View customDialog = inflater.inflate(R.layout.add_number_count_dialog,null);
                        builder.setView(customDialog);

                        final EditText number = customDialog.findViewById(R.id.number);
                        TextView textView = customDialog.findViewById(R.id.tv_text);

                        textView.setText("-");

                        builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SPPUstationaryRef.child(model.getId()).child("count").runTransaction(new Transaction.Handler() {
                                    @NonNull
                                    @Override
                                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                                        Integer count = mutableData.getValue(Integer.class);
                                        count = count - Integer.valueOf(number.getText().toString());

                                        if (count != null && count>=0){
                                            mutableData.setValue(count);
                                            // Toast.makeText(BookListActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                            return Transaction.success(mutableData);
                                        }
                                        else{
                                            //  Toast.makeText(BookListActivity.this, "Invalid action", Toast.LENGTH_SHORT).show();
                                        }
                                        return Transaction.abort();
                                    }

                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

                                    }
                                });
                            }
                        });

                        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        AlertDialog alertDialog = builder.create();

                        alertDialog.show();

                    }
                });

                viewHolder.mView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.mView);
                        //inflating menu from xml resource
                        popup.inflate(R.menu.book_options_menu);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete:

                                        deleteStationary(model.getId(),model.getImg());

                                        return true;
                                    case R.id.edit:
                                        Intent intent = new Intent(StationaryListActivity.this,StationaryDetailsActivity.class);
                                        intent.putExtra("mode","edit");
                                        intent.putExtra("categoryName",categoryName);
                                        intent.putExtra("category",category);
                                        intent.putExtra("stationaryItem",model);
                                        intent.putExtra("searchName",searchName);
                                        startActivity(intent);
                                        //updateCourse(model);
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        });
                        //displaying the popup
                        popup.show();
                        return false;
                    }
                });


            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }


    private void deleteStationary(final String id, String pic) {

        DatabaseReference SPPUstationaryRef = FirebaseDatabase.getInstance().getReference("SPPUstationary");

        SPPUstationaryRef.child(category).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    DatabaseReference BooksLocationsRef = FirebaseDatabase.getInstance().getReference("StationaryLocations").child("SPPUstationary");


                    BooksLocationsRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(StationaryListActivity.this, "book location removed", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(StationaryListActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Toast.makeText(StationaryListActivity.this,"book deleted from database",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(StationaryListActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (pic!=null && !pic.isEmpty()){
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Toast.makeText(StationaryListActivity.this,"img deleted from storage",Toast.LENGTH_SHORT).show();

                    Log.d("deletecourse", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(StationaryListActivity.this,exception.getMessage(),Toast.LENGTH_SHORT).show();

                }
            });
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}