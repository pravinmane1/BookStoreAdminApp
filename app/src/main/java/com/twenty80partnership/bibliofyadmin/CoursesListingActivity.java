package com.twenty80partnership.bibliofyadmin;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.twenty80partnership.bibliofyadmin.models.ApplicableTerm;
import com.twenty80partnership.bibliofyadmin.models.Branch;
import com.twenty80partnership.bibliofyadmin.models.Code;
import com.twenty80partnership.bibliofyadmin.models.Course;
import com.twenty80partnership.bibliofyadmin.models.CourseDetails;
import com.twenty80partnership.bibliofyadmin.models.Item;
import com.twenty80partnership.bibliofyadmin.models.MenuItem;
import com.twenty80partnership.bibliofyadmin.viewHolders.CourseDetailsViewHolder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoursesListingActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 71;
    CardView addCourse;
    RecyclerView itemList;
    Uri saveUri;
    Button upload, select;
    EditText etId;
    String mode;
    private StorageReference imageFolder;
    private ProgressDialog progressDialog;
    private AlertDialog dialog;
    private EditText name, priority;
    private EditText currentSem;
    private FirebaseDatabase db;
    private SwitchCompat swYear1common, swPublicationSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses_listing);

        db = FirebaseDatabase.getInstance();

        mode = getIntent().getStringExtra("mode");

        internetCheck();

        DatabaseReference SPPUbooksListingDetailsRef = db.getReference("SPPUbooksListing").child("details");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addCourse = findViewById(R.id.add_new_course);
        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(this, 2));
        Query query = SPPUbooksListingDetailsRef.orderByChild("priority");

        firebaseSearch(query);


        if (mode.equals("codes") || mode.equals("selectReference")) {
            addCourse.setVisibility(View.GONE);
        }
        addCourse.setOnClickListener(v -> {


            final AlertDialog.Builder alert = new AlertDialog.Builder(CoursesListingActivity.this);
            alert.setTitle("Add New Course");
            alert.setCancelable(false);
            //alert.setMessage("sample message");

            LayoutInflater inflater = CoursesListingActivity.this.getLayoutInflater();

            View customDialog = inflater.inflate(R.layout.add_course_dialog, null);
            alert.setView(customDialog);
            name = (EditText) customDialog.findViewById(R.id.course);
            priority = (EditText) customDialog.findViewById(R.id.topic);
            select = customDialog.findViewById(R.id.select);
            etId = customDialog.findViewById(R.id.id);
            upload = customDialog.findViewById(R.id.upload);
            currentSem = customDialog.findViewById(R.id.code);
            swYear1common = customDialog.findViewById(R.id.sw_year1common);
            swPublicationSystem = customDialog.findViewById(R.id.sw_publication_system);
            currentSem.setHint("Add sem");
            currentSem.setVisibility(View.VISIBLE);
            etId.setVisibility(View.GONE);


            alert.setIcon(R.drawable.plus_green);

            alert.setPositiveButton("DISMISS", (dialog, which) -> dialog.dismiss());

            dialog = alert.create();
            dialog.show();

            select.setOnClickListener(v12 -> chooseImage());

            upload.setOnClickListener(v1 -> {

                String id = SPPUbooksListingDetailsRef.push().getKey();

                uploadData(null, true, id);
            });


        });

    }

    private void internetCheck() {
        if (!Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(CoursesListingActivity.this);
            alert.setTitle("No internet Connection");
            alert.setCancelable(false);
            alert.setPositiveButton("Retry", (dialog, which) -> {
                dialog.dismiss();
                if (!Common.isConnectedToInternet(getApplicationContext())) {
                    alert.show();
                }
            });

            alert.setNegativeButton("Exit", (dialog, which) -> dialog.dismiss());

            AlertDialog dialog = alert.create();
            dialog.show();
        }

    }

    private boolean uploadData(String oldPic, final boolean addDefaultTerm, String id) {

        if (!name.getText().toString().isEmpty() && !priority.getText().toString().isEmpty() ) {

            progressDialog = new ProgressDialog(CoursesListingActivity.this);
            progressDialog.setCancelable(false);


            if (saveUri != null) {
                progressDialog.setTitle("Uploading Image....");
                progressDialog.show();
                String imageName = UUID.randomUUID().toString();
                imageFolder = FirebaseStorage.getInstance().getReference().child("courses/" + imageName);

                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            saveUri = null;
                            progressDialog.setTitle("Saving Data..");
                            Toast.makeText(CoursesListingActivity.this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {

                                finalSave(uri.toString(), addDefaultTerm, id);

                            });
                        });
                return true;
            } else {
                progressDialog.setTitle("Saving data....");
                progressDialog.show();


                Item item;
                //if oldpic exists
                if (oldPic != null) {
                    finalSave(oldPic, addDefaultTerm, id);
                }
                // to proceed without any pic
                else {
                    finalSave(null, addDefaultTerm, id);
                }
                return false;
            }


        } else {
            Toast.makeText(CoursesListingActivity.this, "Data is not asigned", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void finalSave(String uriString, boolean addDefaultTerm, String id) {

        CourseDetails courseDetails = new CourseDetails();

        courseDetails.setName(name.getText().toString());
        courseDetails.setPic(uriString);
        courseDetails.setId(id);
        courseDetails.setPriority(Float.valueOf(priority.getText().toString()));

        if (!currentSem.getText().toString().isEmpty())
        courseDetails.setCurrentSem(currentSem.getText().toString());
        
        courseDetails.setYear1common(swYear1common.isChecked());
        courseDetails.setPublicationSystem(swPublicationSystem.isChecked());


        DatabaseReference bookListingRef = db.getReference("SPPUbooksListing")
                .child("details").child(id);

        bookListingRef.setValue(courseDetails).addOnCompleteListener(task -> {
            Toast.makeText(CoursesListingActivity.this, "Data Uploaded successfully", Toast.LENGTH_SHORT).show();

            Course course = new Course();
            course.setName(courseDetails.getName());
            course.setId(courseDetails.getId());
            course.setPriority(courseDetails.getPriority());
            course.setPic(courseDetails.getPic());
            course.setCurrentSem(courseDetails.getCurrentSem());

            DatabaseReference courseRef = db.getReference("Courses").child("SPPU").child(id);

            courseRef.updateChildren(course.toMap());

            if (addDefaultTerm) {

                if (!id.isEmpty()) {
                    addDefaultTerms(id);
                    addSemCode(id);
                    createRegularBookCategoryAndAddApplicableTerms(id);

                    enterTermCodes(id, name.getText().toString());
                }

            }

            progressDialog.dismiss();
            dialog.dismiss();
        });
    }

    private void enterTermCodes(String id, String name) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(CoursesListingActivity.this);
        alert.setTitle("Enter Branches and Year Information");
        alert.setCancelable(false);
        alert.setPositiveButton("Enter", (dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent(CoursesListingActivity.this, AllTermsActivity.class);
            intent.putExtra("mode", "codes");
            intent.putExtra("course", id);
            intent.putExtra("courseName", name);
            startActivity(intent);
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void addSemCode(String id) {
        DatabaseReference ref = db.getReference("SPPUbooksListing").child("codes").child(id).child("sem");
        Map<String, Object> semsMap = new HashMap<>();
        //
        Code code1 = new Code();
        code1.setName("sem-JUNE");
        code1.setCode("S1");
        code1.setPriority(1f);
        semsMap.put(code1.getCode(), code1);

        Code code2 = new Code();
        code2.setName("sem-DEC");
        code2.setCode("S2");
        code2.setPriority(2f);
        semsMap.put(code2.getCode(), code2);

        ref.updateChildren(semsMap);
    }

    private void addDefaultTerms(String id) {

        DatabaseReference SPPUtermsListing = db.getReference("SPPUtermsListing");


        final Map<String, Object> finalMap = new HashMap<>();

        Map<String, Object> allTermsMap = new HashMap<>();
        //branch
        Item item1 = new Item();
        item1.setId("branch");
        item1.setName("Branches");
        item1.setTopic("--BRANCH--");
        allTermsMap.put(item1.getId(), item1);

        //year
        Item item2 = new Item();
        item2.setId("year");
        item2.setName("Years");
        item2.setTopic("--YEAR--");
        allTermsMap.put(item2.getId(), item2);


        //sem
        Item item3 = new Item();
        item3.setId("sem");
        item3.setName("Semesters");
        item3.setTopic("--SEM--");
        allTermsMap.put(item3.getId(), item3);

        //PUBLICATIONS
        Item item4 = new Item();
        item4.setId("publication");
        item4.setName("Publications");
        item4.setTopic("--PUBLICATIONS--");
        allTermsMap.put(item4.getId(), item4);


        finalMap.put(id, allTermsMap);
        SPPUtermsListing.updateChildren(finalMap);

    }

    private boolean createRegularBookCategoryAndAddApplicableTerms(String courseId) {

        //taking system time
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date currentTime = Calendar.getInstance().getTime();
        Long date = Long.parseLong(dateFormat.format(currentTime));


        final DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
                .child("category").child(courseId).child("regular");


        Map<String, Object> map = new HashMap<>();

        map.put("name", "regular");
        map.put("priority", 1);
        map.put("timeAdded", date);
        map.put("id", "regular");

        final Map<String, Object> finalMap = new HashMap<>();


        Map<String, Object> applicableTermsMap = new HashMap<>();
        ApplicableTerm applicableTerm = new ApplicableTerm();

        //branch
        applicableTerm.setPriority(1);
        applicableTerm.setTermId("branch");

        applicableTermsMap.put("branch", applicableTerm);

        //year
        ApplicableTerm applicableTerm2 = new ApplicableTerm();
        applicableTerm2.setPriority(2);
        applicableTerm2.setTermId("year");

        applicableTermsMap.put("year", applicableTerm2);

        //sem
        ApplicableTerm applicableTerm3 = new ApplicableTerm();
        applicableTerm3.setPriority(3);
        applicableTerm3.setTermId("sem");

        applicableTermsMap.put("sem", applicableTerm3);

        finalMap.put("applicableTerms", applicableTermsMap);


        courseRef.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                courseRef.updateChildren(finalMap);
            }
        });


        return false;

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveUri = data.getData();
            select.setText("Image Selected");

        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                MenuItem menuItem = (MenuItem) data.getSerializableExtra("menuItem");
                Intent rIntent = new Intent();
                rIntent.putExtra("menuItem", menuItem);
                setResult(RESULT_OK, rIntent);
                finish();
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST);
    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<CourseDetails, CourseDetailsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<CourseDetails, CourseDetailsViewHolder>(
                CourseDetails.class, R.layout.course_details_row, CourseDetailsViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final CourseDetailsViewHolder viewHolder, final CourseDetails model, final int position) {

                viewHolder.swVisibility.setEnabled(false);

                db.getReference("Courses").child("SPPU").child(model.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        viewHolder.swVisibility.setEnabled(true);
                        viewHolder.swVisibility.setChecked(snapshot.exists());

                        viewHolder.swVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (!isChecked) {
                                db.getReference("Courses").child("SPPU").child(model.getId()).removeValue();
                            } else {
                                makeCourseVisible(model);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


                if (!mode.equals("books")) {
                    viewHolder.ivMore.setVisibility(View.GONE);
                    viewHolder.swVisibility.setVisibility(View.GONE);
                }

                viewHolder.setDetails(model.getName(), model.getPic(), model.getPriority(), mode);

                viewHolder.cvItemCard.setOnClickListener(v -> {

                    Intent intent = new Intent(CoursesListingActivity.this, BooksCategoryActivity.class);
                    intent.putExtra("mode", mode);

                    if (mode.equals("books")) {
                        intent.putExtra("course", model.getId());
                        intent.putExtra("courseName", model.getName());

                        if (model.getPublicationSystem() != null) {
                            intent.putExtra("publicationSystem", model.getPublicationSystem());
                        } else {
                            intent.putExtra("publicationSystem", false);
                        }

                        startActivity(intent);
                    } else if (mode.equals("menuItem")) {
                        intent.putExtra("course", model.getId());
                        intent.putExtra("courseName", model.getName());
                        startActivityForResult(intent, 1);
                    }
                });

                viewHolder.ivMore.setOnClickListener(v -> {

                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.ivMore);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.options_menu);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(android.view.MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.delete:
                                    deleteCourse(model.getId(), model.getPic());
                                    return true;
                                case R.id.edit:
                                    updateCourse(model);
                                    return true;
                                case R.id.remove_pic:
                                    removePic(model.getPic(), model.getId());
                                    return true;
                                case R.id.terms_available:
                                    Intent intent = new Intent(CoursesListingActivity.this, AllTermsActivity.class);
                                    intent.putExtra("mode", "codes");
                                    intent.putExtra("course", model.getId());
                                    intent.putExtra("courseName", model.getName());
                                    startActivity(intent);
                                default:
                                    return false;
                            }
                        }

                    });
                    //displaying the popup
                    popup.show();
                });
            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    private void makeCourseVisible(CourseDetails model) {
        Course course = new Course();
        course.setId(model.getId());
        course.setPic(model.getPic());
        course.setPriority(model.getPriority());
        course.setName(model.getName());

        db.getReference("SPPUbooksListing").child("codes").child(model.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                if (snapshot1.child("year").exists()) {
                    course.setYear((int) snapshot1.child("year").getChildrenCount());
                }

                if (snapshot1.child("branch").exists()) {

                    HashMap<String, Object> branchMap = new HashMap<>();

                    for (DataSnapshot dataSnapshot : snapshot1.child("branch").getChildren()) {
                        Branch branch = dataSnapshot.getValue(Branch.class);
                        branchMap.put(branch.getCode(), branch);
                    }
                    course.setBranch(branchMap);
                }

                db.getReference("Courses").child("SPPU").child(model.getId()).setValue(course);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void removePic(String pic, final String id) {

        if (pic != null) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    DatabaseReference picRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing")
                            .child("details").child(id).child("pic");
                    picRef.removeValue();
                    // File deleted successfully
                    Toast.makeText(CoursesListingActivity.this, "delete successful", Toast.LENGTH_SHORT).show();
                    Log.d("update", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(CoursesListingActivity.this, "delete failed", Toast.LENGTH_SHORT).show();
                    Log.d("update", "onFailure: did not delete file");
                }
            });
        } else {
            Toast.makeText(CoursesListingActivity.this, "No pic set", Toast.LENGTH_SHORT).show();

        }

    }

    private void updateCourse(final CourseDetails courseDetails) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(CoursesListingActivity.this);
        alert.setTitle("Edit Course");
        alert.setCancelable(false);
        //alert.setMessage("sample message");

        LayoutInflater inflater = CoursesListingActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_course_dialog, null);
        alert.setView(customDialog);
        name = (EditText) customDialog.findViewById(R.id.course);
        priority = (EditText) customDialog.findViewById(R.id.topic);
        select = customDialog.findViewById(R.id.select);
        etId = customDialog.findViewById(R.id.id);
        upload = customDialog.findViewById(R.id.upload);
        currentSem = customDialog.findViewById(R.id.code);
        swYear1common = customDialog.findViewById(R.id.sw_year1common);
        swPublicationSystem = customDialog.findViewById(R.id.sw_publication_system);
        currentSem.setHint("Current Sem");
        currentSem.setVisibility(View.VISIBLE);


        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        dialog = alert.create();
        dialog.show();

        if (courseDetails.getName()!=null)
        name.setText(courseDetails.getName());

        etId.setText(courseDetails.getId());
        etId.setFocusable(false);
        etId.setEnabled(false);
        etId.setCursorVisible(false);
        priority.setText(courseDetails.getPriority().toString());

        if (courseDetails.getCurrentSem()!=null)
        currentSem.setText(courseDetails.getCurrentSem());

        currentSem.setHint("current sem");
        currentSem.setVisibility(View.VISIBLE);
        upload.setText("UPDATE");

        if (courseDetails.getYear1common() != null)
            swYear1common.setChecked(courseDetails.getYear1common());

        if (courseDetails.getPublicationSystem() != null)
            swPublicationSystem.setChecked(courseDetails.getPublicationSystem());


        select.setOnClickListener(v -> chooseImage());

        upload.setOnClickListener(v -> {

            String oldPic = courseDetails.getPic();
            if (uploadData(courseDetails.getPic(), false, courseDetails.getId())) {

                //if oldpic exists and user uploaded new image delete old image
                if (oldPic != null) {
                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldPic);

                    photoRef.delete().addOnSuccessListener(aVoid -> {
                        // File deleted successfully
                        Log.d("update", "onSuccess: deleted file");
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Uh-oh, an error occurred!
                            Log.d("update", "onFailure: did not delete file");
                        }
                    });
                }

            } else {
                Toast.makeText(CoursesListingActivity.this, "Old image is kept", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void deleteCourse(String id, String pic) {
        //delete listing
        DatabaseReference SPPUbooksListingRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing");
        DatabaseReference SPPUtermsListingRef = FirebaseDatabase.getInstance().getReference("SPPUtermsListing");
        DatabaseReference SPPUbooksRef = FirebaseDatabase.getInstance().getReference("SPPUbooks");
        DatabaseReference SPPUbooksTemplatesRef = FirebaseDatabase.getInstance().getReference("SPPUbooksTemplates");
        DatabaseReference courseRef = db.getReference("Courses").child("SPPU").child(id);

        SPPUtermsListingRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CoursesListingActivity.this, "terms listing removed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        SPPUbooksListingRef.child("details").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CoursesListingActivity.this, "details removed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CoursesListingActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        SPPUbooksListingRef.child("codes").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CoursesListingActivity.this, "codes removed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CoursesListingActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        SPPUbooksListingRef.child("category").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CoursesListingActivity.this, "course category removed", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CoursesListingActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        SPPUbooksRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(CoursesListingActivity.this, "Books deleted if present", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CoursesListingActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        courseRef.removeValue();
        SPPUbooksTemplatesRef.child(id).removeValue();


        if (pic != null) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Toast.makeText(CoursesListingActivity.this, "deleted from storage", Toast.LENGTH_SHORT).show();

                    Log.d("deletecourse", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(CoursesListingActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(CoursesListingActivity.this, "pic is null", Toast.LENGTH_SHORT).show();
        }


        //todo remove search index, books location,

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
