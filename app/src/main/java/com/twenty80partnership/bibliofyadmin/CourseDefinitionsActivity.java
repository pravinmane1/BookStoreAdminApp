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
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;
import com.twenty80partnership.bibliofyadmin.models.Course;
import com.twenty80partnership.bibliofyadmin.viewHolders.CourseViewHolder;

import java.util.HashMap;

public class CourseDefinitionsActivity extends AppCompatActivity {
    CardView addCourse;
    RecyclerView itemList;
    Uri saveUri;

    Button upload,select;
    private  final  int PICK_IMAGE_REQUEST = 71;
    private StorageReference imageFolder;
    EditText id;
    private ProgressDialog progressDialog;
    private AlertDialog dialog;
    private EditText name,priority;
    String course;
    String courseName,mode;
    DatabaseReference SPPUtermsRef;
    private DatabaseReference CoursesRef;
    private TextView years;
    private TextView bookCourseRefernce;
    private EditText currentSem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_definitions);

        if (! Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(CourseDefinitionsActivity.this);
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

            add.setText("Add new User Course Category");
            CoursesRef = FirebaseDatabase.getInstance().getReference("Courses").child("SPPU");
            Query query = CoursesRef.orderByChild("priority");
            firebaseCategorySearch(query);


        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder alert = new AlertDialog.Builder(CourseDefinitionsActivity.this);
                LayoutInflater inflater = CourseDefinitionsActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_user_course_dialog,null);
                alert.setView(customDialog);
                name = (EditText)customDialog.findViewById(R.id.name);
                id = (EditText)customDialog.findViewById(R.id.id);
                priority =(EditText) customDialog.findViewById(R.id.topic);
                years = customDialog.findViewById(R.id.years);
                upload = customDialog.findViewById(R.id.upload);
                Button clearRef = customDialog.findViewById(R.id.clear_ref);
                currentSem = customDialog.findViewById(R.id.current_sem);

                bookCourseRefernce = customDialog.findViewById(R.id.book_course_reference);
                bookCourseRefernce.setFocusable(false);

                alert.setCancelable(false);

                alert.setIcon(R.drawable.plus_green);

                alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });

                dialog = alert.create();
                dialog.show();

                clearRef.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bookCourseRefernce.setText("");
                    }
                });

                bookCourseRefernce.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent courseIntent = new Intent(CourseDefinitionsActivity.this, CoursesListingActivity.class);
                        courseIntent.putExtra("mode","selectReference");
                        startActivityForResult(courseIntent,1);
                    }
                });

                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        uploadData();
                    }
                });

            }
        });

    }


    public void firebaseCategorySearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<Course, CourseViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Course, CourseViewHolder>(
                Course.class, R.layout.course_container, CourseViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final CourseViewHolder viewHolder, final Course model, final int position) {


                //viewHolder.setDetails(model.getName(),model.getYears(),model.getPriority(),model.getCurrentSem());

                viewHolder.itemCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent branchIntent = new Intent(CourseDefinitionsActivity.this,BranchesActivity.class);
                        branchIntent.putExtra("userCourseId",model.getId());

//                        if (model.getBookCourseReference()!=null && !model.getBookCourseReference().equals("")){
//                            branchIntent.putExtra("ref",model.getBookCourseReference());
//                        }

                        startActivity(branchIntent);
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
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses").child("SPPU").child(model.getId());
                                            ref.removeValue();
                                            return true;

                                        case R.id.edit:
                                            updateCourse(model);
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

    private void updateCourse(final Course course) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(CourseDefinitionsActivity.this);
        LayoutInflater inflater = CourseDefinitionsActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_user_course_dialog,null);
        alert.setView(customDialog);
        name = (EditText)customDialog.findViewById(R.id.name);
        id = (EditText)customDialog.findViewById(R.id.id);
        priority =(EditText) customDialog.findViewById(R.id.topic);
        years = customDialog.findViewById(R.id.years);
        currentSem = customDialog.findViewById(R.id.current_sem);
        upload = customDialog.findViewById(R.id.upload);
        Button clearRef = customDialog.findViewById(R.id.clear_ref);

        bookCourseRefernce = customDialog.findViewById(R.id.book_course_reference);
        bookCourseRefernce.setFocusable(false);



        name.setText(course.getName());
        id.setText(course.getId());
        id.setEnabled(false);
        priority.setText(course.getPriority().toString());
        years.setText(course.getYear().toString());
        //currentSem.setText(course.getCurrentSem());
      //  bookCourseRefernce.setText(course.getBookCourseReference());
        bookCourseRefernce.setFocusable(false);

        alert.setCancelable(false);

        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        dialog = alert.create();
        dialog.show();

        clearRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookCourseRefernce.setText("");
            }
        });

        bookCourseRefernce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent courseIntent = new Intent(CourseDefinitionsActivity.this, CoursesListingActivity.class);
                courseIntent.putExtra("mode","selectReference");
                startActivityForResult(courseIntent,1);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadData();
            }
        });



    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void uploadData() {

        if (!name.getText().toString().isEmpty() && !priority.getText().toString().isEmpty()
                && !years.getText().toString().isEmpty() && !currentSem.getText().toString().isEmpty()){

            if (id.getText().toString().equals("")){
                id.setText(name.getText().toString());
            }
            progressDialog = new ProgressDialog(CourseDefinitionsActivity.this);
            progressDialog.setCancelable(false);

                progressDialog.setTitle("Saving Data.....");
                progressDialog.show();


                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses").child("SPPU").child(id.getText().toString());


                HashMap<String,Object> result = new HashMap<>();

                result.put("name",name.getText().toString());
                result.put("priority",Float.parseFloat(priority.getText().toString()));
                result.put("bookCourseReference",bookCourseRefernce.getText().toString());
                result.put("years",Integer.parseInt(years.getText().toString()));
                result.put("currentSem",currentSem.getText().toString());
                result.put("id",id.getText().toString());

                ref.updateChildren(result).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(CourseDefinitionsActivity.this,"Data Uploaded successfully",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        dialog.dismiss();
                    }
                });




        }
        else{
            Toast.makeText(CourseDefinitionsActivity.this,"Data is not asigned",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            saveUri = data.getData();
            select.setText("Image Selected");

        }
        else if (requestCode == 1 && resultCode == RESULT_OK){
            String ref = data.getStringExtra("ref");
            bookCourseRefernce.setText(ref);
        }
    }
}
