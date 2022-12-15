package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.models.ApplicableTerm;
import com.twenty80partnership.bibliofyadmin.models.Item;
import com.twenty80partnership.bibliofyadmin.models.StationaryItem;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class StationaryDetailsActivity extends AppCompatActivity {


    EditText brandView,atr1,atr2,atr3,atr4,originalPriceView,discountView,discountedPriceView;
    ImageView imgView;
    TextView removeView;
    Switch availabilityView;
    String mode,categoryName,category;
    StationaryItem stationaryItem;
    private  final  int PICK_IMAGE_REQUEST = 71;
    Button update;
    ProgressDialog progressDialog;
    private Uri saveUri;
    private StorageReference imageFolder;
    private DatabaseReference StationaryLocationsRef;
    private DatabaseReference StationaryRef;
    private String child;
    private DatabaseReference cartIndexRef;
    private String searchName;
    private DatabaseReference searchIndexRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stationary_details);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);



        progressDialog = new ProgressDialog(StationaryDetailsActivity.this);
        progressDialog.setTitle("Saving Data...");
        progressDialog.setCancelable(false);

        brandView = findViewById(R.id.brand);
        atr1 = findViewById(R.id.atr1);
        availabilityView = findViewById(R.id.switch1);
        atr2 = findViewById(R.id.atr2);
        atr3 = findViewById(R.id.atr3);
        atr4 = findViewById(R.id.atr4);

        originalPriceView = findViewById(R.id.original_price);
        discountView = findViewById(R.id.discount);
        discountedPriceView = findViewById(R.id.discounted_price);
        imgView = findViewById(R.id.img);
        update = findViewById(R.id.update);
        removeView = findViewById(R.id.remove);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        categoryName = intent.getStringExtra("categoryName");
        category = intent.getStringExtra("category");
        searchName = intent.getStringExtra("searchName");


        if (mode.equals("edit")){
            removeView.setVisibility(View.VISIBLE);
            update.setText("update");
            stationaryItem = (StationaryItem) intent.getSerializableExtra("stationaryItem");

            brandView.setText(stationaryItem.getName());
            atr1.setText(stationaryItem.getAtr1());
            atr2.setText(stationaryItem.getAtr2());
            atr3.setText(stationaryItem.getAtr3());
            atr4.setText(stationaryItem.getAtr4());

            originalPriceView.setText(""+stationaryItem.getOriginalPrice());
            discountView.setText(""+stationaryItem.getDiscount());
            discountedPriceView.setText(""+stationaryItem.getDiscountedPrice());

            if (stationaryItem.getImg()!=null && !stationaryItem.getImg().isEmpty())
                Picasso.get().load(stationaryItem.getImg()).into(imgView);

            availabilityView.setChecked(stationaryItem.getAvailability());

        }

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        removeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldPic;
                if (getIntent().getStringExtra("mode").equals("edit")){
                    oldPic = stationaryItem.getImg();
                }
                else {
                    oldPic = null;
                }

                if (oldPic!=null && !oldPic.isEmpty()){

                    progressDialog.setTitle("removing pic...");
                    progressDialog.show();

                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldPic);

                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            DatabaseReference stationaryRef = FirebaseDatabase.getInstance().getReference("SPPUstationary").child(category).child(stationaryItem.getId());
                            stationaryRef.child("img").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    progressDialog.dismiss();
                                    if (task.isSuccessful()){
                                        Toast.makeText(StationaryDetailsActivity.this,"img is removed from database and storage",Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(StationaryDetailsActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            // File deleted successfully
                            Log.d("update", "onSuccess: deleted file");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(StationaryDetailsActivity.this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                            // Uh-oh, an error occurred!
                            Log.d("update", "onFailure: did not delete file");
                        }
                    });
                }
                else {
                    Toast.makeText(StationaryDetailsActivity.this,"no pic set",Toast.LENGTH_SHORT).show();
                }

            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!brandView.getText().toString().isEmpty()  && !originalPriceView.getText().toString().isEmpty() &&
                        !discountedPriceView.getText().toString().isEmpty() && !discountView.getText().toString().isEmpty()) {
                    progressDialog.show();

                    String oldPic = "";
                    if (getIntent().getStringExtra("mode").equals("edit")) {
                        oldPic = stationaryItem.getImg();
                    }
                    if (uploadData(oldPic)) {

                        //if oldpic exists and user uploaded new image delete old image
                        if (oldPic != null && !oldPic.isEmpty()) {
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
                        Toast.makeText(StationaryDetailsActivity.this, "Old image is kept", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(StationaryDetailsActivity.this, "data is not set", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select image"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            removeView.setVisibility(View.VISIBLE);
            saveUri = data.getData();
            Picasso.get().load(saveUri).into(imgView);
        }
    }

    private boolean uploadData(String oldImg) {

        //references
        StationaryRef = FirebaseDatabase.getInstance().getReference("SPPUstationary").child(category);
        StationaryLocationsRef = FirebaseDatabase.getInstance().getReference("StationaryLocations").child("SPPUstationary");
        searchIndexRef = FirebaseDatabase.getInstance().getReference("SearchIndex");

        // for existing stationary
        if (getIntent().getStringExtra("mode").equals("edit")) {

            Log.d("StationaryD","inside edit mode");

            searchName = brandView.getText().toString().toLowerCase();

            cartIndexRef = FirebaseDatabase.getInstance().getReference("CartIndex").child("stationary").child(stationaryItem.getId());

            // if searchname of category is changed need to update searchindex
            if ( !  (searchName) .equals(stationaryItem.getSearchName())){
                Log.d("StationaryD","name doesnt match");
                searchIndexRef.child(stationaryItem.getSearchName()).removeValue();
                StationaryRef.child(stationaryItem.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("StationaryD","s removed");

                        }
                        else {
                            Log.d("StationaryD","s is not removed");

                        }
                    }
                });


                //delete existing user stationary added to cart
                cartIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()!=null){
                            Log.d("presentDebug",dataSnapshot.getValue().toString());

                            for (DataSnapshot ds:dataSnapshot.getChildren()){

                                String uid = ds.getKey();

                                final DatabaseReference uCart = FirebaseDatabase.getInstance().getReference("CartReq")
                                        .child(uid).child("stationary");

                                uCart.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        uCart.child(stationaryItem.getId()).removeValue();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                        else {
                            Log.d("presentDebug","data is nuull");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                String newId = StationaryRef.push().getKey();
                child = searchName+newId;
            }
            else{
                child = stationaryItem.getId();

                //for cartIndex update
                cartIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue()!=null){
                            Log.d("presentDebug",dataSnapshot.getValue().toString());

                            for (DataSnapshot ds:dataSnapshot.getChildren()){

                                String uid = ds.getKey();

                                final DatabaseReference uCart = FirebaseDatabase.getInstance().getReference("Cart").child(uid).child("stationary").child(stationaryItem.getId());

                                uCart.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        uCart.child("discount").setValue(discountView.getText().toString());
                                        uCart.child("discountedPrice").setValue(discountedPriceView.getText().toString());
                                        uCart.child("originalPrice").setValue(originalPriceView.getText().toString());
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        else {
                            Log.d("presentDebug","data is nuull");

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }





        }
        //for new stationary
        else {
            Log.d("helloworld","else");
               TextView searchV = findViewById(R.id.brand);
               searchName = searchV.getText().toString().toLowerCase();

            String key = StationaryLocationsRef.push().getKey();
            child = searchName+key;

        }

        //if image is selected to upload
        if (saveUri != null) {

            progressDialog.setTitle("Uploading Image....");
            progressDialog.show();
            String imageName = UUID.randomUUID().toString();
            imageFolder = FirebaseStorage.getInstance().getReference().child("Stationary/" + imageName);

            //upload img
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            saveUri = null;
                            progressDialog.setTitle("Saving Data..");
                            Toast.makeText(StationaryDetailsActivity.this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    StationaryItem item = new StationaryItem(brandView.getText().toString(),
                                            uri.toString(),
                                            atr1.getText().toString(),
                                            atr2.getText().toString(),
                                            atr3.getText().toString(),
                                            atr4.getText().toString(),
                                            child,
                                            brandView.getText().toString().toLowerCase(),
                                            Integer.parseInt(discountView.getText().toString()),
                                            Integer.parseInt(discountedPriceView.getText().toString()),
                                            Integer.parseInt(originalPriceView.getText().toString()),
                                            availabilityView.isChecked());

                                    Map<String,Object> itemValues = item.toMap();


                                    StationaryRef.child(child).updateChildren(itemValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                if (!getIntent().getStringExtra("mode").equals("edit")){
                                                    StationaryLocationsRef.child(child).setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            progressDialog.dismiss();
                                                            if (task.isSuccessful()){
                                                                Toast.makeText(StationaryDetailsActivity.this, "stationary location added", Toast.LENGTH_SHORT).show();
                                                            }
                                                            else {
                                                                Toast.makeText(StationaryDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                }
                                                else {
                                                    progressDialog.dismiss();
                                                }

                                                Toast.makeText(StationaryDetailsActivity.this, "stationary data added", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(StationaryDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
            return true;

        }
        //if save uri is null
        else {
            progressDialog.setTitle("Saving data....");
            progressDialog.show();

            StationaryItem item = new StationaryItem(
                    brandView.getText().toString(),
                    oldImg,
                    atr1.getText().toString(),
                    atr2.getText().toString(),
                    atr3.getText().toString(),
                    atr4.getText().toString(),
                    child,
                    brandView.getText().toString().toLowerCase(),
                    Integer.parseInt(discountView.getText().toString()),
                    Integer.parseInt(discountedPriceView.getText().toString()),
                    Integer.parseInt(originalPriceView.getText().toString()),
                    availabilityView.isChecked());

            Map<String,Object> itemValues = item.toMap();


            StationaryRef.child(child).updateChildren(itemValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        if (!getIntent().getStringExtra("mode").equals("edit")){
                            StationaryLocationsRef.child(child).setValue(category).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()){
                                        Toast.makeText(StationaryDetailsActivity.this, "stationary location added", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(StationaryDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else {
                            progressDialog.dismiss();
                        }

                        Toast.makeText(StationaryDetailsActivity.this, "stationary data added", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(StationaryDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


            return false;


        }


    }

    @Override
    public void onBackPressed() {
        saveUri =null;
        super.onBackPressed();
    }
}
