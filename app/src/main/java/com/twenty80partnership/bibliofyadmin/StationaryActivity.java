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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.twenty80partnership.bibliofyadmin.models.Item;
import com.twenty80partnership.bibliofyadmin.models.MenuItem;
import com.twenty80partnership.bibliofyadmin.viewHolders.ItemViewHolder;

import java.util.UUID;

public class StationaryActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 71;
    CardView addStationary;
    RecyclerView itemList;
    Uri saveUri;
    DatabaseReference SPPUstationaryListingRef;
    Button upload, select;
    EditText id;
    private StorageReference imageFolder;
    private ProgressDialog progressDialog;
    private AlertDialog dialog;
    private EditText name, priority;
    private String mode = "";
    private FirebaseDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary);

        db = FirebaseDatabase.getInstance();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        addStationary = findViewById(R.id.add_new_stationary);
        TextView add = findViewById(R.id.add);
        itemList = findViewById(R.id.recycler_view);

        mode = getIntent().getStringExtra("mode");

        itemList.setHasFixedSize(false);
        itemList.setLayoutManager(new GridLayoutManager(this, 3));


        SPPUstationaryListingRef = FirebaseDatabase.getInstance().getReference("SPPUstationaryListing");
        Query query = SPPUstationaryListingRef.orderByChild("priority");
        firebaseCategorySearch(query);

        if (mode.equals("menuHome")) {
            addStationary.setVisibility(View.GONE);
        }


        addStationary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final AlertDialog.Builder alert = new AlertDialog.Builder(StationaryActivity.this);

                alert.setCancelable(false);
                //alert.setMessage("sample message");

                LayoutInflater inflater = StationaryActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_stationary_dialog, null);
                alert.setView(customDialog);
                name = customDialog.findViewById(R.id.course);

                id = customDialog.findViewById(R.id.id);
                id.setVisibility(View.GONE);
                priority = customDialog.findViewById(R.id.topic);
                select = customDialog.findViewById(R.id.select);
                upload = customDialog.findViewById(R.id.upload);


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

                        String key = db.getReference("SPPUstationaryListing").push().getKey();
                        uploadData(null, key);
                    }
                });


            }
        });

    }


    private boolean uploadData(String oldPic, String id) {

        if (!name.getText().toString().isEmpty() && !priority.getText().toString().isEmpty()) {

            progressDialog = new ProgressDialog(StationaryActivity.this);
            progressDialog.setCancelable(false);


            if (saveUri != null) {
                progressDialog.setTitle("Uploading Image....");
                progressDialog.show();
                String imageName = UUID.randomUUID().toString();
                imageFolder = FirebaseStorage.getInstance().getReference().child("StationaryType/" + imageName);

                imageFolder.putFile(saveUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                saveUri = null;
                                progressDialog.setTitle("Saving Data..");
                                Toast.makeText(StationaryActivity.this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        finalSave(uri.toString(), id);
                                    }
                                });
                            }
                        });
                return true;
            } else {
                progressDialog.setTitle("Saving data....");
                progressDialog.show();

                finalSave(oldPic, id);

                return false;
            }


        } else {
            Toast.makeText(StationaryActivity.this, "Data is not asigned", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void finalSave(String pic, String id) {
        DatabaseReference ref = db.getReference("SPPUstationaryListing").child(id);

        ref.child("name").setValue(name.getText().toString());
        ref.child("priority").setValue(Float.parseFloat(priority.getText().toString()));
        ref.child("pic").setValue(pic);
        ref.child("id").setValue(id).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(StationaryActivity.this, "Data Uploaded successfully", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveUri = data.getData();
            select.setText("Image Selected");

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST);
    }

    public void firebaseCategorySearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<Item, ItemViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Item, ItemViewHolder>(
                Item.class, R.layout.stationary_row, ItemViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final ItemViewHolder viewHolder, final Item model, final int position) {


                viewHolder.setDetails(model.getName(), model.getPic(), getApplicationContext(), model.getPriority());

                viewHolder.itemCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (mode.equals("menuItem")) {
                            MenuItem menuItem = new MenuItem();
                            menuItem.setType("stationary");
                            menuItem.setCategory(model.getId());
                            menuItem.setImg(model.getPic());

                            Intent intent = new Intent();
                            intent.putExtra("menuItem", menuItem);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            Intent intent = new Intent(StationaryActivity.this, StationaryListActivity.class);
                            intent.putExtra("category", model.getId());
                            intent.putExtra("categoryName", model.getName());
                            intent.putExtra("searchName", model.getSearchName());
                            startActivity(intent);
                        }


                    }
                });


                if (mode.equals("menuItem")) {
                    viewHolder.more.setVisibility(View.GONE);
                }

                viewHolder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.more);
                        //inflating menu from xml resource

                        popup.inflate(R.menu.option_menu_stationary);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(android.view.MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete:
                                        deleteStationary(model.getId(), model.getPic());
                                        return true;

                                    case R.id.edit:
                                        updateStationary(model);
                                        return true;

                                    case R.id.remove_pic:
                                        removePic(model.getPic(), model.getId());
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

        if (pic != null) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    DatabaseReference picRef = FirebaseDatabase.getInstance().getReference("SPPUstationaryListing").child(id).child("pic");
                    picRef.removeValue();
                    // File deleted successfully
                    Toast.makeText(StationaryActivity.this, "delete successful", Toast.LENGTH_SHORT).show();
                    Log.d("update", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(StationaryActivity.this, "delete failed", Toast.LENGTH_SHORT).show();
                    Log.d("update", "onFailure: did not delete file");
                }
            });
        } else {
            Toast.makeText(StationaryActivity.this, "No pic set", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateStationary(final Item item) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(StationaryActivity.this);
        LayoutInflater inflater = StationaryActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_stationary_dialog, null);
        alert.setView(customDialog);
        name = customDialog.findViewById(R.id.course);
        id = customDialog.findViewById(R.id.id);
        priority = customDialog.findViewById(R.id.topic);
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

        alert.setTitle("Edit Stationary Category");
        name.setText(item.getName());
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
                if (uploadData(item.getPic(), item.getId())) {

                    //if oldpic exists and user uploaded new image delete old image
                    if (oldPic != null) {
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

                } else {
                    Toast.makeText(StationaryActivity.this, "Old image is kept", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void deleteStationary(String id, String pic) {

        DatabaseReference SPPUstationaryListingRef = FirebaseDatabase.getInstance().getReference("SPPUstationaryListing").child(id);
        DatabaseReference SPPUstationaryRef = FirebaseDatabase.getInstance().getReference("SPPUstationary").child(id);

        SPPUstationaryRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(StationaryActivity.this, "stationary for catagory deleted from database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StationaryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });


        SPPUstationaryListingRef.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(StationaryActivity.this, "catagory deleted from database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StationaryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (pic != null && !pic.equals("")) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(pic);

            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Toast.makeText(StationaryActivity.this, "deleted from storage", Toast.LENGTH_SHORT).show();

                    Log.d("deleteStationary", "onSuccess: deleted file");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Toast.makeText(StationaryActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

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
