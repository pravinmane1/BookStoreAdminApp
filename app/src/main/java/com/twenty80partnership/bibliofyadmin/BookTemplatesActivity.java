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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.models.BookTemplate;
import com.twenty80partnership.bibliofyadmin.viewHolders.TemplateViewHolder;

import java.util.ArrayList;

public class BookTemplatesActivity extends AppCompatActivity {
    String course,category,typeName;
    RecyclerView itemList;
    private FloatingActionButton mFloatingActionButton;
    private Query query;
    private DatabaseReference SPPUbooksTemplatesRef;
    private EditText etName,id;
    private EditText etPriority;
    private EditText etCode;
    String mode="";
    private String combination;
    private FirebaseDatabase db;
    private String selectedBookId="__";
    private String publicationSelected;
    private ProgressDialog pd;
    private String openTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_templates);

        db = FirebaseDatabase.getInstance();

        if (! Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(BookTemplatesActivity.this);
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


        course = getIntent().getStringExtra("course");
        category = getIntent().getStringExtra("category");
        typeName = getIntent().getStringExtra("typeName");
        combination = getIntent().getStringExtra("combination");
        openTemplate = getIntent().getStringExtra("openTemplate");

        SPPUbooksTemplatesRef = db.getReference("SPPUbooksTemplates")
                .child(course).child(category).child(combination);

        pd = new ProgressDialog(BookTemplatesActivity.this);

        pd.setMessage("Copying...");
        pd.setCancelable(false);

        if (category.equals("used") || category.equals("return")){
            final TextView tvCopy = findViewById(R.id.tv_copy_templates);



            SPPUbooksTemplatesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()){
                        tvCopy.setVisibility(View.VISIBLE);

                        tvCopy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pd.setMessage("Copying...");

                                pd.show();

                                db.getReference("SPPUbooksTemplates")
                                        .child(course).child("regular").child(combination).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()){

                                            SPPUbooksTemplatesRef.setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                                                        SPPUbooksTemplatesRef.child(ds.child("templateId").getValue(String.class)).child("defaultBookId").removeValue();
                                                        SPPUbooksTemplatesRef.child(ds.child("templateId").getValue(String.class)).child("defaultPublication").removeValue();
                                                    }

                                                    pd.dismiss();
                                                }
                                            });
                                        }
                                        else{
                                            Toast.makeText(BookTemplatesActivity.this, "No template data for this combination in 'regular' category", Toast.LENGTH_SHORT).show();
                                            pd.dismiss();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        pd.dismiss();
                                    }
                                });
                            }
                        });
                    }
                    else{
                        Toast.makeText(BookTemplatesActivity.this, "Templates already present", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(BookTemplatesActivity.this, "Database access error", Toast.LENGTH_SHORT).show();

                }
            });


        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Templates for "+"\""+combination+"\"");

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);


        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        itemList.setLayoutManager(mLayoutManager);



        Query query = SPPUbooksTemplatesRef.orderByChild("priority");

        firebaseSearch(query);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(BookTemplatesActivity.this);
                alert.setTitle("Add Template");
                alert.setCancelable(false);

                LayoutInflater inflater = BookTemplatesActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_template_dialog,null);
                alert.setView(customDialog);
                etName = (EditText) customDialog.findViewById(R.id.et_name);


                etPriority = (EditText) customDialog.findViewById(R.id.et_priority);
                Button btnSave = customDialog.findViewById(R.id.btn_save);


                alert.setIcon(R.drawable.plus_green);

                alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });

                final AlertDialog dialog = alert.create();
                dialog.show();

                btnSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String name = etName.getText().toString();
                        String priority = etPriority.getText().toString();
                        String key = SPPUbooksTemplatesRef.push().getKey();

                        BookTemplate item= new BookTemplate();
                        if (!name.isEmpty() && !priority.isEmpty() && key!=null && !key.isEmpty()){

                            item.setName(name);
                            item.setTemplateId(key);
                            item.setPriority(Float.parseFloat(priority));

                            SPPUbooksTemplatesRef.child(key).setValue(item);
                            dialog.dismiss();
                        }
                        else {
                            Toast.makeText(BookTemplatesActivity.this,"Data is empty",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });



    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<BookTemplate, TemplateViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<BookTemplate, TemplateViewHolder>(
                BookTemplate.class, R.layout.template_row, TemplateViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final TemplateViewHolder viewHolder, final BookTemplate model, final int position) {


                String templateId = model.getTemplateId();
                if (openTemplate!=null && openTemplate.equals(templateId)){
                    openTemplate = null;
                    updateTemplate(model);
                }
                //Toast.makeText(BookTemplatesActivity.this,model.getCode()+model.getName(),Toast.LENGTH_SHORT).show();
                viewHolder.setDetails(model.getName(),"Loading....",model.getPriority());

                if (model.getDefaultBookId()!=null && !model.getDefaultBookId().equals("")){

                    db.getReference("SPPUbooks").child(course).child(category).child(combination)
                            .child(model.getDefaultBookId()).child("publication").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue()!=null)
                                viewHolder.tvPublication.setText(dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    viewHolder.tvPublication.setText("No book...");
                }

                viewHolder.templateCard.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.templateCard);
                        //inflating menu from xml resource
                        popup.inflate(R.menu.book_options_menu);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete:

                                        final AlertDialog.Builder alert = new AlertDialog.Builder(BookTemplatesActivity.this);
                                        alert.setTitle("Warning!!!");
                                        alert.setMessage("All books associated with this template will also get deleted along with this template.");
                                        alert.setCancelable(false);
                                        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                pd.setMessage("Deleting");
                                                pd.show();
                                                deleteAllLinks(model.getTemplateId());
                                            }
                                        });

                                        alert.setNegativeButton("DON'T DELETE", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });

                                        AlertDialog dialog = alert.create();
                                        dialog.show();

                                        return true;

                                    case R.id.edit:

                                        updateTemplate(model);
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

    private void deleteAllLinks(String templateId) {

        SPPUbooksTemplatesRef.child(templateId).removeValue();

        DatabaseReference booksRef = db.getReference("SPPUbooks").child(course).child(category).child(combination);

        booksRef.orderByChild("code").equalTo(templateId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot ds: snapshot.getChildren()){
                        String bookId = ds.child("id").getValue(String.class);
                        String bookImg = ds.child("img").getValue(String.class);

                        if (bookId!=null){
                            deleteBook(bookId,bookImg);
                        }
                    }
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pd.dismiss();
            }
        });

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


                    //1.
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

                                    //2.
                                    cartReqRef.removeValue();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


                    //3.
                    //remove searchIndex ref
                    SearchIndexRef.child(id).removeValue();


                    //4.
                    BooksLocationsRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(BookTemplatesActivity.this, "book location removed", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BookTemplatesActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Toast.makeText(BookTemplatesActivity.this, "book deleted from database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BookTemplatesActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (pic != null && !pic.isEmpty()) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            //5.
            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Toast.makeText(BookTemplatesActivity.this, "img deleted from storage", Toast.LENGTH_SHORT).show();

                    Log.d("deletecourse", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(BookTemplatesActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        }

    }


    private void updateTemplate(final BookTemplate item) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BookTemplatesActivity.this);
        alert.setTitle("Edit Template");
        alert.setCancelable(false);
        //alert.setMessage("sample message");

        LayoutInflater inflater = BookTemplatesActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_template_dialog,null);
        alert.setView(customDialog);
        etName = (EditText) customDialog.findViewById(R.id.et_name);
        etName.setHint("Name of the subject");

        final Spinner spDefaultPublication = customDialog.findViewById(R.id.sp_default_publication);


        //template spinner
        ////////////////////////////////////////////////////////////
        //these will store direct data from database as a object containing codes to search
        final ArrayList<Book> bookList = new ArrayList<Book>();

        //these will be used to show the data in spinners which is obtained by above lists
        final ArrayList<String> publicationDisplayList = new ArrayList<String>();

        DatabaseReference booksRef = db.getReference("SPPUbooks").child(course).child(category).child(combination);


        publicationDisplayList.add("select Publication");
        final ArrayAdapter publicationAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, publicationDisplayList);
        spDefaultPublication.setAdapter(publicationAdapter);



        booksRef.orderByChild("code").startAt(item.getTemplateId()).endAt(item.getTemplateId() + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Book currentBook;

                //children of term which contain further codes


                for (DataSnapshot book : dataSnapshot.getChildren()) {

                    currentBook = book.getValue(Book.class);
                    bookList.add(currentBook);
                    publicationDisplayList.add(currentBook.getPublication());

                }
                publicationAdapter.notifyDataSetChanged();

                Log.d("tagdebug1",bookList.size()+" before");


                spDefaultPublication.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        //obtain the  selected branch from spinner
                         publicationSelected = spDefaultPublication.getSelectedItem().toString();

                        boolean codeFoundForSelectedString = false;

                        //loop applied on arrayList to find the code for selected branch from objects arrayList
                        for (int i = 0; i < bookList.size(); i++) {

                            if (bookList.get(i).getPublication().equals(publicationSelected)) {

                                selectedBookId = bookList.get(i).getId();
                                codeFoundForSelectedString = true;

                                break;
                            }

                        }

                        if (!codeFoundForSelectedString) {
                            selectedBookId = "__";
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });




                Log.d("tagdebug1",bookList.size()+" aftr");

                /////////////////////////////////////////////////////
                //+bookList.get(i).getId()+" onecode "+oneCodeObtained);

                if (item.getDefaultBookId()!=null && !item.getDefaultBookId().equals("") ){
                    Log.d("tagdebug",bookList.size()+"");
                    String oneCodeObtained = item.getDefaultBookId();


                    //loop applied on arrayList to find the code for selected branch from objects arrayList
                    for (int i = 0; i < bookList.size(); i++) {

                        Log.d("tagdebug","BookList "+bookList.get(i).getId()+" onecode "+oneCodeObtained);

                        if (bookList.get(i).getId().equals(oneCodeObtained)) {

                            spDefaultPublication.setSelection(i + 1);
                            break;
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        etPriority = (EditText) customDialog.findViewById(R.id.et_priority);
        Button btnSave = customDialog.findViewById(R.id.btn_save);

        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();

        etName.setText(item.getName());

        if (item.getPriority()!=null)
            etPriority.setText(item.getPriority().toString());

        btnSave.setText("UPDATE");



        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String newBookName = etName.getText().toString();
                String priority = etPriority.getText().toString();

                if (!selectedBookId.equals("__") && !newBookName.isEmpty()  && !priority.isEmpty() ){

                    String oldName = item.getName();

                    if (!oldName.equals(newBookName)){
                        updateBooksAndSearchIndexNames(item.getTemplateId(), newBookName);
                    }

                    item.setName(newBookName);
                    item.setPriority(Float.parseFloat(priority));
                    item.setDefaultBookId(selectedBookId);
                    item.setDefaultPublication(publicationSelected);

                    SPPUbooksTemplatesRef.child(item.getTemplateId()).setValue(item);

                    dialog.dismiss();
                }
                else {
                    Toast.makeText(BookTemplatesActivity.this,"data is null",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void updateBooksAndSearchIndexNames(String templateId,String newBookName) {

        DatabaseReference booksRef = db.getReference("SPPUbooks").child(course).child(category).child(combination);
        DatabaseReference searchIndexRef = db.getReference("SearchIndex").child("SPPU");

        booksRef.orderByChild("code").equalTo(templateId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    for (DataSnapshot ds: snapshot.getChildren()){
                        String bookId = ds.child("id").getValue(String.class);

                        if (bookId!=null){
                            booksRef.child(bookId).child("name").setValue(newBookName);
                            booksRef.child(bookId).child("searchName").setValue(newBookName.toLowerCase());

                            searchIndexRef.child(bookId).child("name").setValue(newBookName);
                            searchIndexRef.child(bookId).child("searchName").setValue(newBookName.toLowerCase());

                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
