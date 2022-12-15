package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofyadmin.models.Item;
import com.twenty80partnership.bibliofyadmin.viewHolders.CodeViewHolder;

public class BranchesActivity extends AppCompatActivity {
    String course;
    RecyclerView itemList;
    private FloatingActionButton mFloatingActionButton;

    private EditText name,id;
    private EditText priority;
    private EditText code;
    String mode="";
    private DatabaseReference branchRef;

    String ref="";
    String userCourseId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branches);


       userCourseId = getIntent().getStringExtra("userCourseId");
        if (getIntent().getStringExtra("ref")!=null){
            ref = getIntent().getStringExtra("ref");
        }

        if (! Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(BranchesActivity.this);
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


        TextView copyView = (TextView) findViewById(R.id.copy);

        copyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            if (ref!=null && !ref.equals("")){

                DatabaseReference SPPUbranchRef = FirebaseDatabase.getInstance()
                        .getReference("SPPUbooksListing").child("codes").child(ref).child("branch");

                SPPUbranchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue()!=null){
                            branchRef = FirebaseDatabase.getInstance().getReference("Courses").child("SPPU").child(userCourseId).child("branches");

                            branchRef.setValue(dataSnapshot.getValue());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            else {
                Toast.makeText(BranchesActivity.this,"ref is not set ",Toast.LENGTH_SHORT).show();
            }
            }
        });


        branchRef = FirebaseDatabase.getInstance().getReference("Courses").child("SPPU").child(userCourseId).child("branches");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        itemList.setLayoutManager(mLayoutManager);

        firebaseSearch(branchRef);


        ////

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(BranchesActivity.this);
                alert.setTitle("Add Code");
                alert.setCancelable(false);
                //alert.setMessage("sample message");

                LayoutInflater inflater = BranchesActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_course_dialog,null);
                alert.setView(customDialog);
                name = (EditText) customDialog.findViewById(R.id.course);
                name.setHint("Display name for code");

                code = customDialog.findViewById(R.id.code);
                code.setVisibility(View.VISIBLE);

                id = customDialog.findViewById(R.id.id);
                id.setVisibility(View.GONE);

                priority = (EditText) customDialog.findViewById(R.id.topic);
                priority.setVisibility(View.VISIBLE);
                Button select = customDialog.findViewById(R.id.select);
                Button upload = customDialog.findViewById(R.id.upload);
                select.setVisibility(View.GONE);
                upload.setText("save");


                alert.setIcon(R.drawable.plus_green);

                alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });

                final AlertDialog dialog = alert.create();
                dialog.show();

                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Item item= new Item();
                        if (!name.getText().toString().isEmpty()
                                && !priority.getText().toString().isEmpty() && !code.getText().toString().isEmpty()){

                            item.setName(name.getText().toString());
                            item.setCode(code.getText().toString());
                            item.setPriority(Float.parseFloat(priority.getText().toString()));

                            branchRef.child(code.getText().toString()).setValue(item);
                            dialog.dismiss();
                        }
                        else {
                            Toast.makeText(BranchesActivity.this,"Data is empty",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });



    }

//    private boolean uploadData(String oldPic) {
//
//        if (!name.getText().toString().isEmpty() && !priority.getText().toString().isEmpty()){
//
//            if (id.getText().toString().equals("")){
//                id.setText(name.getText().toString());
//            }
//            progressDialog = new ProgressDialog(BookListActivity.this);
//            progressDialog.setCancelable(false);
//
//
//            if (saveUri != null){
//                progressDialog.setTitle("Uploading Image....");
//                progressDialog.show();
//                String imageName = UUID.randomUUID().toString();
//                imageFolder = FirebaseStorage.getInstance().getReference().child("Books/"+course+"/"+category+"/"+imageName);
//
//                imageFolder.putFile(saveUri)
//                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                saveUri = null;
//                                progressDialog.setTitle("Saving Data..");
//                                Toast.makeText(BookListActivity.this,"Image Uploaded successfully",Toast.LENGTH_SHORT).show();
//                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                                    @Override
//                                    public void onSuccess(Uri uri) {
//
//                                        //taking system time
//                                        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//                                        Date currentTime= Calendar.getInstance().getTime();
//                                        Long date=Long.parseLong(dateFormat.format(currentTime));
//
//                                        Item item = new Item(name.getText().toString(),uri.toString(),id.getText().toString(),date,Float.parseFloat(priority.getText().toString()));
//                                        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
//                                                .child(getIntent().getStringExtra("course")).child("category").child(item.getId());
//
//                                        courseRef.setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
//                                            @Override
//                                            public void onComplete(@NonNull Task<Void> task) {
//                                                Toast.makeText(BookListActivity.this,"Data Uploaded successfully",Toast.LENGTH_SHORT).show();
//                                                progressDialog.dismiss();
//                                                dialog.dismiss();
//                                            }
//                                        });
//                                    }
//                                });
//                            }
//                        });
//                return true;
//            }
//            else {
//                progressDialog.setTitle("Saving data....");
//                progressDialog.show();
//                //taking system time
//                DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//                Date currentTime= Calendar.getInstance().getTime();
//                Long date=Long.parseLong(dateFormat.format(currentTime));
//
//                Item item;
//                //if oldpic exists
//                if (oldPic != null) {
//                    item = new Item(name.getText().toString(), oldPic, id.getText().toString(), date, Float.parseFloat(priority.getText().toString()));
//                }
//                // to proceed without any pic
//                else {
//                    item = new Item(name.getText().toString(), id.getText().toString(), date, Float.parseFloat(priority.getText().toString()));
//                }
//                DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
//                        .child(getIntent().getStringExtra("course")).child("category").child(item.getId());
//
//                courseRef.setValue(item).addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(BookListActivity.this, "Data Uploaded successfully", Toast.LENGTH_SHORT).show();
//                        progressDialog.dismiss();
//                        dialog.dismiss();
//                    }
//                });
//
//
//                return false;
//
//
//            }
//
//
//        }
//        else{
//            Toast.makeText(BookListActivity.this,"Data is not asigned",Toast.LENGTH_SHORT).show();
//            return false;
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
//            saveUri = data.getData();
//            select.setText("Image Selected");
//
//        }
//    }
//
//    private void chooseImage() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent,"Select image"),PICK_IMAGE_REQUEST);
//    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<Item, CodeViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Item, CodeViewHolder>(
                Item.class, R.layout.code_row, CodeViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final CodeViewHolder viewHolder, final Item model, final int position) {


                //Toast.makeText(BranchesActivity.this,model.getCode()+model.getName(),Toast.LENGTH_SHORT).show();
                viewHolder.setDetails(model.getName(),model.getCode(),model.getPriority());

                viewHolder.codeCard.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.codeCard);
                        //inflating menu from xml resource
                        popup.inflate(R.menu.book_options_menu);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete:
                                        branchRef.child(model.getCode()).removeValue();
                                        return true;

                                    case R.id.edit:
                                        updateCode(model);
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

    private void updateCode(final Item item) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(BranchesActivity.this);
        alert.setTitle("Edit Code");
        alert.setCancelable(false);
        //alert.setMessage("sample message");

        LayoutInflater inflater = BranchesActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_course_dialog,null);
        alert.setView(customDialog);
        name = (EditText) customDialog.findViewById(R.id.course);
        name.setHint("Name Of Code");
        id = (EditText)customDialog.findViewById(R.id.id);
        id.setVisibility(View.GONE);


        code = (EditText) customDialog.findViewById(R.id.code);
        code.setInputType(InputType.TYPE_CLASS_TEXT);
        code.setVisibility(View.VISIBLE);
        code.setEnabled(false);
        code.setFocusable(false);
        code.setCursorVisible(false);

        priority = (EditText) customDialog.findViewById(R.id.topic);

        Button select = customDialog.findViewById(R.id.select);
        Button upload = customDialog.findViewById(R.id.upload);
        select.setVisibility(View.GONE);
        upload.setText("update");


        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();

        name.setText(item.getName());
        //   id.setText(item.getId());
        code.setText(item.getCode());

        if (item.getPriority()!=null)
            priority.setText(item.getPriority().toString());

        upload.setText("UPDATE");



        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!name.getText().toString().isEmpty()  && !priority.getText().toString().isEmpty() && !code.getText().toString().isEmpty()){
                    // item.setId(id.getText().toString());
                    item.setName(name.getText().toString());
                    item.setPriority(Float.parseFloat(priority.getText().toString()));


                    branchRef.child(item.getCode()).setValue(item);
                    dialog.dismiss();
                }
                else {
                    Toast.makeText(BranchesActivity.this,"data is null",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

//    private void removePic(final String pic, final String id) {
//
//        if (pic!=null){
//            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);
//
//            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    DatabaseReference picRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
//                            .child(getIntent().getStringExtra("course")).child("category").child(id).child("pic");
//                    picRef.removeValue();
//                    // File deleted successfully
//                    Toast.makeText(BookListActivity.this,"delete successful",Toast.LENGTH_SHORT).show();
//                    Log.d("update", "onSuccess: deleted file");
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    // Uh-oh, an error occurred!
//                    Toast.makeText(BookListActivity.this,"delete failed",Toast.LENGTH_SHORT).show();
//                    Log.d("update", "onFailure: did not delete file");
//                }
//            });
//        }
//        else {
//            Toast.makeText(BookListActivity.this,"No pic set",Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    private void updateCourse(final Book book) {
//
//        final AlertDialog.Builder alert = new AlertDialog.Builder(BookListActivity.this);
//        alert.setTitle("Edit Book category");
//        alert.setCancelable(false);
//        //alert.setMessage("sample message");
//
//        LayoutInflater inflater =BookListActivity.this.getLayoutInflater();
//
//        View customDialog = inflater.inflate(R.layout.add_course_dialog,null);
//        alert.setView(customDialog);
//        name = (EditText)customDialog.findViewById(R.id.course);
//        name.setHint("category of Book");
//        id = (EditText)customDialog.findViewById(R.id.id);
//        id.setHint("Id for Book category");
//        priority =(EditText) customDialog.findViewById(R.id.priority);
//        select = customDialog.findViewById(R.id.select);
//        upload = customDialog.findViewById(R.id.upload);
//
//
//
//        alert.setIcon(R.drawable.plus_green);
//
//        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//
//            }
//        });
//
//        dialog = alert.create();
//        dialog.show();
//
//        name.setText(book.getName());
//        id.setText(book.getId());
//        //priority.setText(item.getPriority().toString());
//        upload.setText("UPDATE");
//
//
//        select.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                chooseImage();
//            }
//        });
//
//        upload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                String oldPic = book.getImg();
//                if (uploadData(book.getImg())){
//
//                    //if oldpic exists and user uploaded new image delete old image
//                    if (oldPic!=null){
//                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldPic);
//
//                        photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                // File deleted successfully
//                                Log.d("update", "onSuccess: deleted file");
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception exception) {
//                                // Uh-oh, an error occurred!
//                                Log.d("update", "onFailure: did not delete file");
//                            }
//                        });
//                    }
//
//                }
//
//                else {
//                    Toast.makeText(BookListActivity.this,"Old image is kept",Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//
//
//    }



    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
