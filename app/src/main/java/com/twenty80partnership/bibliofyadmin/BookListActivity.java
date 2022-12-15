package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.viewHolders.BookViewHolder;

import java.lang.reflect.Field;

public class BookListActivity extends AppCompatActivity {
    String course, category, typeName;
    RecyclerView itemList;
    EditText id;
    private FloatingActionButton mFloatingActionButton;
    MaterialSearchBar materialSearchBar;
    private Query query;
    private DatabaseReference SPPUbooksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);


        if (!Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(BookListActivity.this);
            alert.setTitle("No internet Connection");
            alert.setCancelable(false);
            alert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (!Common.isConnectedToInternet(getApplicationContext())) {
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


        course = getIntent().getStringExtra("course");
        category = getIntent().getStringExtra("category");
        typeName = getIntent().getStringExtra("typeName");

        SPPUbooksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks")
                .child(course).child(category);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(typeName);

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);
        materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    query = SPPUbooksRef.orderByChild("searchName");
                    firebaseSearch(query);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                query = SPPUbooksRef.orderByChild("searchName").startAt(text.toString()).endAt(text.toString() + "\uf8ff");
                firebaseSearch(query);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        itemList.setLayoutManager(mLayoutManager);


        Query query = SPPUbooksRef.orderByChild("searchName");

        firebaseSearch(query);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(BookListActivity.this, BookDetailsActivity.class);
                intent.putExtra("mode", "new");
                intent.putExtra("course", course);
                intent.putExtra("category", category);
                startActivity(intent);


            }
        });


    }


    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<Book, BookViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Book, BookViewHolder>(
                Book.class, R.layout.book_row, BookViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final BookViewHolder viewHolder, final Book model, final int position) {


                viewHolder.setDetails(model.getName(), model.getAuthor(), model.getPublication(), model.getImg(), model.getOriginalPrice()
                        , model.getDiscountedPrice(), model.getDiscount(), model.getAvailability(), getApplicationContext(), model.getCount());

                if (model.getCount() == null) {
                    Toast.makeText(BookListActivity.this, "Count does not exist setting it to 0", Toast.LENGTH_SHORT).show();
                    SPPUbooksRef.child(model.getId()).child("count").setValue(0);
                }


                if (model.getVisibility()!=null && model.getVisibility()){
                    viewHolder.bookCard.setBackgroundColor(getResources().getColor(R.color.white));
                }
                else{
                    viewHolder.bookCard.setBackgroundColor(getResources().getColor(R.color.light_gray));
                }
                viewHolder.bookCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        AlertDialog.Builder builder = new AlertDialog.Builder(BookListActivity.this);
                        LayoutInflater inflater =  BookListActivity.this.getLayoutInflater();

                        View customDialog = inflater.inflate(R.layout.add_number_count_dialog,null);
                        builder.setView(customDialog);

                        final EditText number = customDialog.findViewById(R.id.number);


                        builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SPPUbooksRef.child(model.getId()).child("count").runTransaction(new Transaction.Handler() {
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(BookListActivity.this);
                        LayoutInflater inflater =  BookListActivity.this.getLayoutInflater();

                        View customDialog = inflater.inflate(R.layout.add_number_count_dialog,null);
                        builder.setView(customDialog);

                        final EditText number = customDialog.findViewById(R.id.number);
                        TextView textView = customDialog.findViewById(R.id.tv_text);

                        textView.setText("-");

                        builder.setPositiveButton("REMOVE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                SPPUbooksRef.child(model.getId()).child("count").runTransaction(new Transaction.Handler() {
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

                viewHolder.bookCard.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.mName);
                        //inflating menu from xml resource
                        popup.inflate(R.menu.book_options_menu);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete:

                                        deleteBook(model.getSearchName(), model.getId(), model.getImg());

                                        return true;
                                    case R.id.edit:
                                        Intent intent = new Intent(BookListActivity.this, BookDetailsActivity.class);
                                        intent.putExtra("mode", "edit");
                                        intent.putExtra("book", model);
                                        intent.putExtra("course", course);
                                        intent.putExtra("category", category);
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


    private void deleteBook(final String searchName, final String id, String pic) {

        DatabaseReference SPPUbooksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks");

        SPPUbooksRef.child(course).child(category).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    DatabaseReference BooksLocationsRef = FirebaseDatabase.getInstance().getReference("BooksLocations").child("SPPUbooks");
                    final DatabaseReference SearchIndexRef = FirebaseDatabase.getInstance().getReference("SearchIndex").child("SPPU");


                    //remove from users cart using cartrequest
                    DatabaseReference cartIndexRef = FirebaseDatabase.getInstance().getReference("CartIndex").child("books").child(id);

                    cartIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String uid = ds.getKey();

                                    DatabaseReference cartReqRef = FirebaseDatabase.getInstance().getReference("CartReq")
                                            .child(uid).child("books").child(id);

                                    cartReqRef.removeValue();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    //remove searchIndex ref
                    SearchIndexRef.child(searchName + id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            SearchIndexRef.child(searchName + id).removeValue();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    BooksLocationsRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(BookListActivity.this, "book location removed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BookListActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Toast.makeText(BookListActivity.this, "book deleted from database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookListActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (pic != null && !pic.isEmpty()) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Toast.makeText(BookListActivity.this, "img deleted from storage", Toast.LENGTH_SHORT).show();

                    Log.d("deletecourse", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(BookListActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

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