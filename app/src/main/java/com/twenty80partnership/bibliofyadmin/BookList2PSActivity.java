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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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

public class BookList2PSActivity extends AppCompatActivity {
    String course, category, typeName, combination;
    RecyclerView itemList;
    EditText id;
    private FloatingActionButton mFloatingActionButton;
    MaterialSearchBar materialSearchBar;
    private Query query;
    private DatabaseReference SPPUbooksRef;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list2);

        db = FirebaseDatabase.getInstance();


        if (!Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(BookList2PSActivity.this);
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
        combination = getIntent().getStringExtra("combination");

        SPPUbooksRef = db.getReference("SPPUbooks")
                .child(course).child(category).child(combination);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(typeName+" "+combination+" Booklist");

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);
        materialSearchBar = findViewById(R.id.search_bar);
        Button btnTemplates = findViewById(R.id.btn_templates);

        btnTemplates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(BookList2PSActivity.this,BookTemplatesActivity.class);
                intent.putExtra("category",category);
                intent.putExtra("typeName",typeName);
                intent.putExtra("course",course);
                intent.putExtra("combination",combination);
                startActivity(intent);
            }
        });

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


                Intent intent = new Intent(BookList2PSActivity.this, BookDetails2PSActivity.class);
                intent.putExtra("mode", "new");
                intent.putExtra("course", course);
                intent.putExtra("category", category);
                intent.putExtra("combination",combination);
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
                    Toast.makeText(BookList2PSActivity.this, "Count does not exist setting it to 10000", Toast.LENGTH_SHORT).show();
                    SPPUbooksRef.child(model.getId()).child("count").setValue(10000);
                }


                if (model.getVisibility()!=null && model.getVisibility()){
                    viewHolder.bookCard.setBackgroundColor(getResources().getColor(R.color.white));
                }
                else{
                    viewHolder.bookCard.setBackgroundColor(getResources().getColor(R.color.light_gray));
                }
                viewHolder.bookCard.setOnClickListener(v -> {
                    Intent intent = new Intent(BookList2PSActivity.this, BookDetails2PSActivity.class);
                    intent.putExtra("mode", "edit");
                    intent.putExtra("book", model);
                    intent.putExtra("course", course);
                    intent.putExtra("category", category);
                    intent.putExtra("combination",combination);
                    startActivity(intent);
                });

                viewHolder.tvAddBooks.setOnClickListener(v -> {


                    AlertDialog.Builder builder = new AlertDialog.Builder(BookList2PSActivity.this);
                    LayoutInflater inflater =  BookList2PSActivity.this.getLayoutInflater();

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
                    });

                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    AlertDialog alertDialog = builder.create();

                    alertDialog.show();

                });

                viewHolder.tvRemoveBooks.setOnClickListener(v -> {

                    AlertDialog.Builder builder = new AlertDialog.Builder(BookList2PSActivity.this);
                    LayoutInflater inflater =  BookList2PSActivity.this.getLayoutInflater();

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
                                        // Toast.makeText(BookList2PSActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                        return Transaction.success(mutableData);
                                    }
                                    else{
                                        //  Toast.makeText(BookList2PSActivity.this, "Invalid action", Toast.LENGTH_SHORT).show();
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

                                        DatabaseReference templatesRef = db.getReference("SPPUbooksTemplates").child(course).child(category).child(combination);

                                        templatesRef.orderByChild("defaultBookId").equalTo(model.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                //no template has this book as default
                                                if (!snapshot.exists()){
                                                    deleteBook(model.getId(), model.getImg());
                                                }
                                                //there is a template pointing to this book delete it first
                                                else{
                                                    final AlertDialog.Builder alert = new AlertDialog.Builder(BookList2PSActivity.this);
                                                    alert.setTitle("Error");
                                                    alert.setMessage("This book is set as default for a template first change default book for template.");
                                                    alert.setCancelable(false);
                                                    alert.setPositiveButton("Go to templates", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            Intent intent = new Intent(BookList2PSActivity.this,BookTemplatesActivity.class);
                                                            intent.putExtra("category",category);
                                                            intent.putExtra("typeName",typeName);
                                                            intent.putExtra("course",course);
                                                            intent.putExtra("combination",combination);
                                                            intent.putExtra("openTemplate",model.getCode());
                                                            startActivity(intent);
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
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                                        return true;
                                    case R.id.edit:
                                        Intent intent = new Intent(BookList2PSActivity.this, BookDetails2PSActivity.class);
                                        intent.putExtra("mode", "edit");
                                        intent.putExtra("book", model);
                                        intent.putExtra("course", course);
                                        intent.putExtra("category", category);
                                        intent.putExtra("combination",combination);
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


    private void deleteBook(final String id, String pic) {



        DatabaseReference SPPUbooksRef = db.getReference("SPPUbooks");

        SPPUbooksRef.child(course).child(category).child(combination).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    DatabaseReference BooksLocationsRef = db.getReference("BooksLocations").child("SPPUbooks");
                    final DatabaseReference SearchIndexRef = db.getReference("SearchIndex").child("SPPU");

                    DatabaseReference pricingRef = db.getReference("Pricing").child("SPPUbooks").child(course).child(category).child(combination).child(id);

                    pricingRef.removeValue();

                    //remove from users cart using cartrequest
                    DatabaseReference cartIndexRef = db.getReference("CartIndex").child("books").child(id);

                    cartIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() != null) {

                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String uid = ds.getKey();

                                    DatabaseReference cartReqRef = db.getReference("CartReq")
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
                    SearchIndexRef.child(id).removeValue();



                    BooksLocationsRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(BookList2PSActivity.this, "book location removed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BookList2PSActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Toast.makeText(BookList2PSActivity.this, "book deleted from database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookList2PSActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (pic != null && !pic.isEmpty()) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Toast.makeText(BookList2PSActivity.this, "img deleted from storage", Toast.LENGTH_SHORT).show();

                    Log.d("deletecourse", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(BookList2PSActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

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