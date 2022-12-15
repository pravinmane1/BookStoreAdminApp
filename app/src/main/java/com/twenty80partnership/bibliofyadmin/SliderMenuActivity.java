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
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofyadmin.models.MenuItem;
import com.twenty80partnership.bibliofyadmin.models.SliderItem;
import com.twenty80partnership.bibliofyadmin.viewHolders.SliderItemViewHolder;

import java.util.UUID;

public class SliderMenuActivity extends AppCompatActivity {
    RecyclerView itemList;
    MaterialSearchBar materialSearchBar;
    private FloatingActionButton mFloatingActionButton;
    private Query query;
    private DatabaseReference SliderMenuRef;
    private int PICK_IMAGE_REQUEST = 2;
    private Uri saveUri;
    private ProgressDialog progressDialog;
    private FirebaseDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        progressDialog = new ProgressDialog(SliderMenuActivity.this);
        progressDialog.setTitle("Saving Data...");
        progressDialog.setCancelable(false);

        db = FirebaseDatabase.getInstance();

        if (!Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(SliderMenuActivity.this);
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

        SliderMenuRef = FirebaseDatabase.getInstance().getReference("Slider")
                .child("SPPU");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Slider-Menu");

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);
        materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    query = SliderMenuRef.orderByChild("searchName");
                    firebaseSearch(query);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                query = SliderMenuRef.orderByChild("searchName").startAt(text.toString()).endAt(text.toString() + "\uf8ff");
                firebaseSearch(query);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        itemList.setLayoutManager(mLayoutManager);
        itemList.setHasFixedSize(true);


        Query query = SliderMenuRef.orderByChild("priority");

        firebaseSearch(query);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(SliderMenuActivity.this);
                alert.setTitle("Add New Slider Item");
                alert.setCancelable(false);

                LayoutInflater inflater = SliderMenuActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_course_dialog, null);
                alert.setView(customDialog);

                LinearLayout layout = customDialog.findViewById(R.id.edits);


                layout.setVisibility(View.GONE);

                Button select = customDialog.findViewById(R.id.select);
                Button upload = customDialog.findViewById(R.id.upload);

                select.setText("Books");
                upload.setText("Stationary");


                alert.setIcon(R.drawable.plus_green);

                alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });

                final AlertDialog dialog = alert.create();
                dialog.show();

                select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent selectIntent = new Intent(SliderMenuActivity.this, CoursesListingActivity.class);
                        selectIntent.putExtra("mode", "menuItem");
                        startActivityForResult(selectIntent, 1);
                    }
                });

                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent selectIntent = new Intent(SliderMenuActivity.this, StationaryActivity.class);
                        selectIntent.putExtra("mode", "menuItem");
                        startActivityForResult(selectIntent, 1);
                    }
                });

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra("menuItem")) {
                        MenuItem menuItem = (MenuItem) data.getSerializableExtra("menuItem");

                        SliderItem sliderItem = new SliderItem();

                        sliderItem.setCourse(menuItem.getCourse());
                        sliderItem.setCategory(menuItem.getCategory());
                        sliderItem.setType(menuItem.getType());
                        String key = db.getReference("Slider").child("SPPU").push().getKey();
                        sliderItem.setId(key);
                        addAndUpdate(sliderItem);
                    }
                }
                break;

            case 2:
                if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                    saveUri = data.getData();
                }
                break;
        }
    }

    private void addAndUpdate(final SliderItem sliderItem) {

        AlertDialog.Builder alert = new AlertDialog.Builder(SliderMenuActivity.this);
        alert.setTitle("Add New sliderItem");
        alert.setCancelable(false);

        LayoutInflater inflater = SliderMenuActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_slider_layout, null);
        alert.setView(customDialog);

        final TextView name = customDialog.findViewById(R.id.name);
        final TextView priority = customDialog.findViewById(R.id.topic);
        final TextView id = customDialog.findViewById(R.id.id);
        final TextView courseCategory = customDialog.findViewById(R.id.course_category);

        courseCategory.setText(sliderItem.getCourse() + "/" + sliderItem.getCategory());
        courseCategory.setEnabled(false);

        if (sliderItem.getId()!=null)
        id.setText(sliderItem.getId());
        id.setEnabled(false);

        priority.setText(String.valueOf(sliderItem.getPriority()));

        if (sliderItem.getName()!=null)
        name.setText(sliderItem.getName());

        Button save = customDialog.findViewById(R.id.upload);
        Button select = customDialog.findViewById(R.id.select);

        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!priority.getText().toString().isEmpty()  && !courseCategory.getText().toString().isEmpty()) {

                    if (((sliderItem.getImg()!=null && !sliderItem.getImg().isEmpty() )|| saveUri!=null)){
                        //after uploading image to storage



                        if (!name.getText().toString().isEmpty())
                            sliderItem.setName(name.getText().toString());

                        sliderItem.setPriority(Float.parseFloat(priority.getText().toString()));

                            uploadData(sliderItem, sliderItem.getImg());



                        dialog.dismiss();

                    }
                    else{
                        Toast.makeText(SliderMenuActivity.this, "Data is empty", Toast.LENGTH_SHORT).show();

                    }



                } else {
                    Toast.makeText(SliderMenuActivity.this, "Data is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<SliderItem, SliderItemViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<SliderItem, SliderItemViewHolder>(
                SliderItem.class, R.layout.slider_menu_row, SliderItemViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final SliderItemViewHolder viewHolder, final SliderItem model, final int position) {


                viewHolder.setDetails(model.getImg(), getApplicationContext(), model.getName(), model.getPriority());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addAndUpdate(model);
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
                            public boolean onMenuItemClick(android.view.MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete:

                                        deleteSlider(model.getId(), model.getImg());

                                        return true;
                                    case R.id.edit:
                                        addAndUpdate(model);
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

    private void deleteSlider(final String id, final String img) {

        final DatabaseReference SliderMenuRef = FirebaseDatabase.getInstance().getReference("Slider").child("SPPU");

        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(img);

        photoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                SliderMenuRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(SliderMenuActivity.this, "menu item deleted from database", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SliderMenuActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST);
    }


    private void uploadData(final SliderItem sliderItem, final String oldImg) {

        progressDialog.setTitle("Uploading Image....");
        progressDialog.show();
        String imageName = UUID.randomUUID().toString();
        final StorageReference imageFolder = FirebaseStorage.getInstance().getReference().child("sliders/" + imageName);

        if (saveUri!=null){
            //upload new image
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            saveUri = null;
                            progressDialog.setTitle("Saving Data..");
                            Toast.makeText(SliderMenuActivity.this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {


                                    if (oldImg != null && !oldImg.equals("")) {
                                        StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldImg);

                                        photoRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                sliderItem.setImg(uri.toString());
                                                saveFinal(sliderItem);
                                            }
                                        });

                                    }
                                    else{
                                        sliderItem.setImg(uri.toString());
                                        saveFinal(sliderItem);
                                    }

                                }
                            });
                        }
                    });
        }
        else{
            progressDialog.setTitle("Saving Data..");

            sliderItem.setImg(oldImg);
            saveFinal(sliderItem);

        }

        //true indicates that new image is uploaded.
    }

    private void saveFinal(SliderItem sliderItem) {
            progressDialog.dismiss();
            SliderMenuRef.child(sliderItem.getId()).setValue(sliderItem);
    }
}