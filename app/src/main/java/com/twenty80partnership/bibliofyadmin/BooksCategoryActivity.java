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
import com.twenty80partnership.bibliofyadmin.viewHolders.ItemViewHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BooksCategoryActivity extends AppCompatActivity {
    CardView addCourse;
    RecyclerView itemList;
    Uri saveUri;
    DatabaseReference SPPUbooksOrCodesListingRef;

    Button upload,select;
    private  final  int PICK_IMAGE_REQUEST = 71;
    private StorageReference imageFolder;
    EditText id;
    private ProgressDialog progressDialog;
    private AlertDialog dialog;
    private EditText type,priority;
    String course;
    String courseName,mode;
    boolean publicationSystem;
    DatabaseReference SPPUtermsRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_definitions);


        mode = getIntent().getStringExtra("mode");
        course = getIntent().getStringExtra("course");
        courseName = getIntent().getStringExtra("courseName");
        publicationSystem = getIntent().getBooleanExtra("publicationSystem",false);



        if (! Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(BooksCategoryActivity.this);
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


        addCourse = findViewById(R.id.add_new_course);
        TextView add = findViewById(R.id.add);
        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);

        itemList.setLayoutManager(new GridLayoutManager(this,2));

        if (mode.equals("books") || mode.equals("menuItem")){
            toolbar.setTitle(courseName);
            add.setText("Add new Course Category");
            SPPUbooksOrCodesListingRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
                    .child("category").child(getIntent().getStringExtra("course"));
            Query query = SPPUbooksOrCodesListingRef.orderByChild("priority");
            firebaseCategorySearch(query);

        }

        
        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder alert = new AlertDialog.Builder(BooksCategoryActivity.this);

                alert.setCancelable(false);
                //alert.setMessage("sample message");

                LayoutInflater inflater = BooksCategoryActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_book_category_dialog,null);
                alert.setView(customDialog);
                type = (EditText)customDialog.findViewById(R.id.et_type);

                id = (EditText)customDialog.findViewById(R.id.et_id);

                priority =(EditText) customDialog.findViewById(R.id.et_priority);
                select = customDialog.findViewById(R.id.select);
                upload = customDialog.findViewById(R.id.upload);


                    alert.setTitle("Add New Book Type of Selected Course");
                    type.setHint("Type of Book to Display");
                    id.setHint("Id for Book Type to Store ");

                alert.setIcon(R.drawable.plus_green);

                alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });

                dialog = alert.create();
                dialog.show();

                select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            chooseImage();

                    }
                });

                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        uploadData(null,true);
                    }
                });



            }
        });

    }


    private boolean uploadData(String oldPic, final Boolean addDefaultTerm) {

        if (!type.getText().toString().isEmpty() && !priority.getText().toString().isEmpty()){

            if (id.getText().toString().equals("")){
                id.setText(type.getText().toString());
            }
            progressDialog = new ProgressDialog(BooksCategoryActivity.this);
            progressDialog.setCancelable(false);


            if (saveUri != null){
                progressDialog.setTitle("Uploading Image....");
                progressDialog.show();
                String imageName = UUID.randomUUID().toString();
                imageFolder = FirebaseStorage.getInstance().getReference().child("courseType/"+imageName);

                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                saveUri = null;
                                progressDialog.setTitle("Saving Data..");
                                Toast.makeText(BooksCategoryActivity.this,"Image Uploaded successfully",Toast.LENGTH_SHORT).show();
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        //taking system time
                                        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                                        Date currentTime= Calendar.getInstance().getTime();
                                        Long date=Long.parseLong(dateFormat.format(currentTime));

                                        //Item item = new Item(name.getText().toString(),uri.toString(),id.getText().toString(),date,Float.parseFloat(priority.getText().toString()));

                                        final DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
                                                .child("category").child(getIntent().getStringExtra("course")).child(id.getText().toString());


                                        Map<String,Object> map = new HashMap<>();

                                        map.put("name", type.getText().toString());
                                        map.put("priority",Float.parseFloat(priority.getText().toString()));
                                        map.put("timeAdded",date);
                                        map.put("pic",uri.toString());
                                        map.put("id",id.getText().toString());

                                        final Map<String,Object> finalMap = new HashMap<>();

                                        if(addDefaultTerm){

                                            Map<String,Object> applicableTermsMap = new HashMap<>();
                                            ApplicableTerm applicableTerm = new ApplicableTerm();

                                            //branch
                                            applicableTerm.setPriority(1);
                                            applicableTerm.setTermId("branch");

                                            applicableTermsMap.put("branch",applicableTerm);

                                            //year
                                            ApplicableTerm applicableTerm2 = new ApplicableTerm();
                                            applicableTerm2.setPriority(2);
                                            applicableTerm2.setTermId("year");

                                            applicableTermsMap.put("year",applicableTerm2);

                                            //sem
                                            ApplicableTerm applicableTerm3 = new ApplicableTerm();
                                            applicableTerm3.setPriority(3);
                                            applicableTerm3.setTermId("sem");

                                            applicableTermsMap.put("sem",applicableTerm3);

                                            finalMap.put("applicableTerms",applicableTermsMap);

                                        }

                                        courseRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {


                                                if (addDefaultTerm)
                                                    courseRef.updateChildren(finalMap);

                                                Toast.makeText(BooksCategoryActivity.this,"Data Uploaded successfully",Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                return true;
            }
            else {
                progressDialog.setTitle("Saving data....");
                progressDialog.show();
                //taking system time
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                Date currentTime= Calendar.getInstance().getTime();
                Long date=Long.parseLong(dateFormat.format(currentTime));

                Item item;

                final DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
                        .child("category").child(getIntent().getStringExtra("course")).child(id.getText().toString());


                //if oldpic exists
                if (oldPic != null) {
                    courseRef.child("pic").setValue(oldPic);
                    //item = new Item(name.getText().toString(), oldPic, id.getText().toString(), date, Float.parseFloat(priority.getText().toString()));
                }
                // to proceed without any pic
//                else {
//                    item = new Item(name.getText().toString(), id.getText().toString(), date, Float.parseFloat(priority.getText().toString()));
//                }
                Map<String,Object> map = new HashMap<>();

                map.put("name", type.getText().toString());
                map.put("priority",Float.parseFloat(priority.getText().toString()));
                map.put("timeAdded",date);
                map.put("id",id.getText().toString());

                final Map<String,Object> finalMap = new HashMap<>();

                if(addDefaultTerm){

                    Map<String,Object> applicableTermsMap = new HashMap<>();
                    ApplicableTerm applicableTerm = new ApplicableTerm();

                    //branch
                    applicableTerm.setPriority(1);
                    applicableTerm.setTermId("branch");

                    applicableTermsMap.put("branch",applicableTerm);

                    //year
                    ApplicableTerm applicableTerm2 = new ApplicableTerm();
                    applicableTerm2.setPriority(2);
                    applicableTerm2.setTermId("year");

                    applicableTermsMap.put("year",applicableTerm2);

                    //sem
                    ApplicableTerm applicableTerm3 = new ApplicableTerm();
                    applicableTerm3.setPriority(3);
                    applicableTerm3.setTermId("sem");

                    applicableTermsMap.put("sem",applicableTerm3);

                    finalMap.put("applicableTerms",applicableTermsMap);

                }


                courseRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (addDefaultTerm)
                        courseRef.updateChildren(finalMap);

                        Toast.makeText(BooksCategoryActivity.this,"Data Uploaded successfully",Toast.LENGTH_SHORT).show();


                        progressDialog.dismiss();
                        dialog.dismiss();
                    }
                });


                return false;


            }


        }
        else{
            Toast.makeText(BooksCategoryActivity.this,"Data is not asigned",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            saveUri = data.getData();
            select.setText("Image Selected");

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select image"),PICK_IMAGE_REQUEST);
    }

    public void firebaseCategorySearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<Item, ItemViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Item, ItemViewHolder>(
                Item.class, R.layout.item_row, ItemViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final ItemViewHolder viewHolder, final Item model, final int position) {


                viewHolder.setDetails(model.getName(), model.getPic(), getApplicationContext(),model.getPriority(),mode);

                viewHolder.itemCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mode.equals("books")){

                            //add direct books
//                            Intent intent = new Intent(BooksCategoryActivity.this,BookListActivity.class);
//                            intent.putExtra("category",model.getId());
//                            intent.putExtra("typeName",model.getName());
//                            intent.putExtra("course",getIntent().getStringExtra("course"));
//                            startActivity(intent);

                            //layer of branch+year+sem under the layer books are added
                            Intent intent = new Intent(BooksCategoryActivity.this,CombinationsActivity.class);
                            intent.putExtra("category",model.getId());
                            intent.putExtra("typeName",model.getName());
                            intent.putExtra("course",getIntent().getStringExtra("course"));
                            intent.putExtra("publicationSystem",publicationSystem);
                            startActivity(intent);
                        }
                        else if (mode.equals("menuItem")){

                            MenuItem menuItem = new MenuItem();
                            menuItem.setType("books");
                            menuItem.setCourse(course);
                            menuItem.setCategory(model.getId());
                            menuItem.setImg(model.getPic());

                            Intent intent = new Intent();
                            intent.putExtra("menuItem",menuItem);
                            setResult(RESULT_OK, intent);
                            finish();
                        }

                    }
                });



                viewHolder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.more);
                        //inflating menu from xml resource
                            popup.inflate(R.menu.option_type_menu);
                            //adding click listener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(android.view.MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.delete:
                                            deleteCategory(model.getId(),model.getPic());

                                            return true;

                                        case R.id.edit:
                                            updateCategory(model);
                                            return true;

                                        case R.id.remove_pic:
                                            removePic(model.getPic(),model.getId());
                                            return true;

                                        case R.id.terms:
                                            Intent intent = new Intent(BooksCategoryActivity.this, TermsApplicableActivity.class);
                                            intent.putExtra("course",course);
                                            intent.putExtra("category",model.getId());
                                            startActivity(intent);
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

    private void removePic(final String pic, final String id) {

        if (pic!=null){
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    DatabaseReference picRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
                            .child("category").child(getIntent().getStringExtra("course")).child(id).child("pic");
                    picRef.removeValue();
                    // File deleted successfully
                    Toast.makeText(BooksCategoryActivity.this,"delete successful",Toast.LENGTH_SHORT).show();
                    Log.d("update", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(BooksCategoryActivity.this,"delete failed",Toast.LENGTH_SHORT).show();
                    Log.d("update", "onFailure: did not delete file");
                }
            });
        }
        else {
            Toast.makeText(BooksCategoryActivity.this,"No pic set",Toast.LENGTH_SHORT).show();
        }

    }

    private void updateCategory(final Item item) {



        final AlertDialog.Builder alert = new AlertDialog.Builder(BooksCategoryActivity.this);
        LayoutInflater inflater = BooksCategoryActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_book_category_dialog,null);
        alert.setView(customDialog);
        type = (EditText)customDialog.findViewById(R.id.et_type);
        id = (EditText)customDialog.findViewById(R.id.et_id);
        priority =(EditText) customDialog.findViewById(R.id.et_priority);
        select = customDialog.findViewById(R.id.select);
        upload = customDialog.findViewById(R.id.upload);
        alert.setCancelable(false);

        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

         alert.setTitle("Edit Book Type");
        type.setHint("Type of Book");
        id.setHint("Id for Book category");
        type.setText(item.getName());
        id.setText(item.getId());
        id.setFocusable(false);
        id.setEnabled(false);
        id.setCursorVisible(false);

        priority.setText(item.getPriority().toString());
        upload.setText("UPDATE");



        dialog = alert.create();
        dialog.show();




        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   chooseImage();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String oldPic = item.getPic();
                if (uploadData(item.getPic(),false)){

                    //if oldpic exists and user uploaded new image delete old image
                    if (oldPic!=null){
                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldPic);

                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // File deleted successfully
                                Log.d("update", "onSuccess: deleted file");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Uh-oh, an error occurred!
                                Log.d("update", "onFailure: did not delete file");
                            }
                        });
                    }

                }

                else {
                    Toast.makeText(BooksCategoryActivity.this,"Old image is kept",Toast.LENGTH_SHORT).show();
                }
            }
        });



    }

    private void deleteCategory(String id,String pic) {

        DatabaseReference SPPUbooksListingRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing");
        DatabaseReference SPPUbooksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(getIntent().getStringExtra("course")).child(id);

        SPPUbooksRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(BooksCategoryActivity.this,"books for catagory deleted from database",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(BooksCategoryActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });


        SPPUbooksListingRef.child("category").child(getIntent().getStringExtra("course")).child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(BooksCategoryActivity.this,"catagory deleted from database",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(BooksCategoryActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(pic!=null){
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Toast.makeText(BooksCategoryActivity.this,"deleted from storage",Toast.LENGTH_SHORT).show();

                    Log.d("deleteCategory", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(BooksCategoryActivity.this,exception.getMessage(),Toast.LENGTH_SHORT).show();

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
