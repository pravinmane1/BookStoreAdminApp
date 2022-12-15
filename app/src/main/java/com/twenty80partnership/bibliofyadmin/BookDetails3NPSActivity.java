package com.twenty80partnership.bibliofyadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.twenty80partnership.bibliofyadmin.models.Pricing;
import com.twenty80partnership.bibliofyadmin.models.SearchIndex;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BookDetails3NPSActivity extends AppCompatActivity {
    private final int PICK_IMAGE_REQUEST = 71;
    EditText idView, originalPriceView, discountView, discountedPriceView, authorView;
    ImageView imgView;
    TextView removeView, tvName;
    Switch availabilityView, visibilityView;
    String mode, course, category, combination;
    Book book;
    Button update;
    ProgressDialog progressDialog;
    private EditText etPublication, etName;
    private Uri saveUri;
    private StorageReference imageFolder;
    private DatabaseReference BooksLocationsRef;
    private Book uBook;
    private DatabaseReference bookRef;
    private String childVal;
    private DatabaseReference IndexForSearchRef, cartIndexRef;
    private FirebaseDatabase db;
    private boolean setThisBookAsTemplateDefault = false;
    private EditText profit, buyingDiscount, buyingPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details3);

        db = FirebaseDatabase.getInstance();


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);


        progressDialog = new ProgressDialog(BookDetails3NPSActivity.this);
        progressDialog.setTitle("Saving Data...");
        progressDialog.setCancelable(false);

        availabilityView = findViewById(R.id.switch1);
        visibilityView = findViewById(R.id.switch2);
        idView = findViewById(R.id.id);
        originalPriceView = findViewById(R.id.original_price);
        discountView = findViewById(R.id.discount);
        discountedPriceView = findViewById(R.id.discounted_price);
        profit = findViewById(R.id.profit);
        buyingPrice = findViewById(R.id.buying_price);
        buyingDiscount = findViewById(R.id.buying_discount);
        imgView = findViewById(R.id.img);
        update = findViewById(R.id.update);
        authorView = findViewById(R.id.author);
        removeView = findViewById(R.id.remove);
        tvName = findViewById(R.id.tv_name);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        course = intent.getStringExtra("course");
        category = intent.getStringExtra("category");
        combination = intent.getStringExtra("combination");

        etPublication = findViewById(R.id.et_publication);
        etName = findViewById(R.id.et_name);

        if (mode.equals("edit")) {

            removeView.setVisibility(View.VISIBLE);
            update.setText("update");
            book = (Book) intent.getSerializableExtra("book");

            if (book!=null){

                if (book.getName()!=null)
                    etName.setText(book.getName());

                if (book.getPublication() != null && !book.getPublication().equals(""))
                    etPublication.setText(book.getPublication());

                idView.setText(book.getId());
                originalPriceView.setText("" + book.getOriginalPrice());
                discountView.setText("" + book.getDiscount());
                discountedPriceView.setText("" + book.getDiscountedPrice());

                if (book.getAuthor() != null && !book.getAuthor().equals(""))
                    authorView.setText(book.getAuthor());

                if (book.getImg() != null && !book.getImg().isEmpty())
                    Picasso.get().load(book.getImg()).into(imgView);

                if (book.getAvailability() != null)
                    availabilityView.setChecked(book.getAvailability());

                if (book.getVisibility() != null)
                    visibilityView.setChecked(book.getVisibility());


                progressDialog.show();

                DatabaseReference pricingRef = db.getReference("Pricing").child("SPPUbooks").child(course).child(category).child(combination).child(book.getId());


                pricingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressDialog.dismiss();
                        if (snapshot.exists()) {
                            Pricing pricing = snapshot.getValue(Pricing.class);

                            profit.setText(String.valueOf(pricing.getProfit()));
                            buyingDiscount.setText(String.valueOf(pricing.getBuyingDiscount()));
                            buyingPrice.setText(String.valueOf(pricing.getBuyingPrice()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
            }
            else{
                finish();
            }


        }

        //set discounted price automatically depending on original and discount

        originalPriceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!discountView.getText().toString().isEmpty() && !s.toString().isEmpty()) {
                    Float discountedPrice = Float.parseFloat(s.toString()) * ((100 - Float.parseFloat(discountView.getText().toString())) / 100);

                    discountedPriceView.setText(String.valueOf(Math.round(discountedPrice)));
                }

                if (!buyingDiscount.getText().toString().isEmpty() && !s.toString().isEmpty()) {
                    Float discountedPrice = Float.parseFloat(s.toString()) * ((100 - Float.parseFloat(buyingDiscount.getText().toString())) / 100);

                    buyingPrice.setText(String.valueOf(Math.round(discountedPrice)));
                }

            }
        });


        discountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!originalPriceView.getText().toString().isEmpty() && !s.toString().isEmpty()) {
                    Float discountedPrice = Float.parseFloat(originalPriceView.getText().toString()) * ((100 - Float.parseFloat(s.toString())) / 100);

                    discountedPriceView.setText(String.valueOf(Math.round(discountedPrice)));
                }

            }
        });

        //set discounted price automatically depending on original and discount

        buyingDiscount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!originalPriceView.getText().toString().isEmpty() && !s.toString().isEmpty()) {
                    Float discountedPrice = Float.parseFloat(originalPriceView.getText().toString()) * ((100 - Float.parseFloat(s.toString())) / 100);

                    buyingPrice.setText(String.valueOf(Math.round(discountedPrice)));
                }

            }
        });


        buyingPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!discountedPriceView.getText().toString().isEmpty() && !s.toString().isEmpty()) {
                    int p = Integer.valueOf(discountedPriceView.getText().toString()) - Integer.valueOf(s.toString());
                    profit.setText(Integer.toString(p));
                }

            }
        });

        discountedPriceView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!buyingPrice.getText().toString().isEmpty() && !s.toString().isEmpty()) {
                    int p = Integer.valueOf(s.toString()) - Integer.valueOf(buyingPrice.getText().toString());
                    profit.setText(Integer.toString(p));
                }

            }
        });


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
                if (getIntent().getStringExtra("mode").equals("edit")) {
                    oldPic = book.getImg();
                } else {
                    oldPic = null;
                }

                if (oldPic != null && !oldPic.isEmpty()) {

                    progressDialog.setTitle("removing pic...");
                    progressDialog.show();

                    StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(oldPic);

                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).child(category).child(book.getId());
                            bookRef.child("img").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(BookDetails3NPSActivity.this, "img is removed from database and storage", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(BookDetails3NPSActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(BookDetails3NPSActivity.this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                            // Uh-oh, an error occurred!
                            Log.d("update", "onFailure: did not delete file");
                        }
                    });
                } else {
                    Toast.makeText(BookDetails3NPSActivity.this, "no pic set", Toast.LENGTH_SHORT).show();
                }

            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!etName.getText().toString().isEmpty() &&
                        !originalPriceView.getText().toString().isEmpty() &&
                        !discountedPriceView.getText().toString().isEmpty() &&
                        !discountView.getText().toString().isEmpty() &&
                        !buyingPrice.getText().toString().isEmpty() &&
                        !buyingDiscount.getText().toString().isEmpty() &&
                        !profit.getText().toString().isEmpty()) {
                    progressDialog.show();

                    String oldPic = "";
                    if (getIntent().getStringExtra("mode").equals("edit")) {
                        oldPic = book.getImg();
                    }

                    //if new pic is uploaded ,(delete oldpic)
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
                        Toast.makeText(BookDetails3NPSActivity.this, "Old image is kept", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(BookDetails3NPSActivity.this, "data is not set", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select image"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            removeView.setVisibility(View.VISIBLE);
            saveUri = data.getData();
            Picasso.get().load(saveUri).into(imgView);
        }
    }


    private boolean uploadData(final String oldImg) {


        bookRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).child(category).child(combination);
        BooksLocationsRef = FirebaseDatabase.getInstance().getReference("BooksLocations").child("SPPUbooks");
        IndexForSearchRef = FirebaseDatabase.getInstance().getReference("SearchIndex").child("SPPU");


        //for existing book
        if (getIntent().getStringExtra("mode").equals("edit")) {

            childVal = book.getId();

            cartIndexRef = FirebaseDatabase.getInstance().getReference("CartIndex").child("books").child(book.getId());

            //for cartIndex update
            cartIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Log.d("presentDebug", dataSnapshot.getValue().toString());

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {

                            String uid = ds.getKey();

                            final DatabaseReference uCart = FirebaseDatabase.getInstance().getReference("Cart").child(uid).child(book.getId());

                            uCart.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    uCart.child("discount").setValue(Integer.parseInt(discountView.getText().toString()));
                                    uCart.child("discountedPrice").setValue(Integer.parseInt(discountedPriceView.getText().toString()));
                                    uCart.child("originalPrice").setValue(Integer.parseInt(originalPriceView.getText().toString()));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    } else {
                        Log.d("presentDebug", "data is nuull");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        //for new book
        else {
            String key = BooksLocationsRef.push().getKey();

            //  String shortSearch = nameView.getText().toString().toLowerCase().substring(0, Math.min(nameView.getText().toString().toLowerCase().length(), 8));

            childVal = etName.getText().toString().toLowerCase().trim()  + category+ key;

        }

        //childval is set


        DatabaseReference ref = db.getReference("Pricing").child("SPPUbooks").child(course).child(category).child(combination).child(childVal);

        Map<String, Object> pricingMap = new HashMap<>();

        pricingMap.put("profit", Integer.valueOf(profit.getText().toString()));
        pricingMap.put("buyingPrice", Integer.valueOf(buyingPrice.getText().toString()));
        pricingMap.put("buyingDiscount", Integer.valueOf(buyingDiscount.getText().toString()));

        ref.updateChildren(pricingMap);


        //if image is selected by user
        if (saveUri != null) {
            progressDialog.setTitle("Uploading Image....");
            progressDialog.show();
            String imageName = UUID.randomUUID().toString();
            imageFolder = FirebaseStorage.getInstance().getReference().child("books/" + imageName);

            //upload new image
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            saveUri = null;
                            progressDialog.setTitle("Saving Data..");
                            Toast.makeText(BookDetails3NPSActivity.this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    //after uploading image to storage

                                    uBook = createBook(childVal, uri.toString());

                                    Map<String, Object> bookValues = uBook.toMap();

                                    //add book data to specific location
                                    bookRef.child(childVal).updateChildren(bookValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                if (!category.equals("return") && !category.equals("used")) {

                                                    String searchId = uBook.getId();

                                                    SearchIndex indexBook = new SearchIndex(uBook.getName(), uBook.getSearchName(), uBook.getAuthor(), uBook.getPublication(),
                                                            uBook.getImg(), uBook.getId(), uBook.getCode(), course, category, "books", uBook.getOriginalPrice(),
                                                            uBook.getDiscountedPrice(), uBook.getDiscount(), uBook.getAvailability(), uBook.getVisibility(), combination);

                                                    BooksLocationsRef.child(childVal).setValue(course + "/" + category + "/" + combination);
                                                    IndexForSearchRef.child(searchId).setValue(indexBook);

                                                    Toast.makeText(BookDetails3NPSActivity.this, "book data added", Toast.LENGTH_SHORT).show();
                                                }
                                                finish();


                                            } else {
                                                progressDialog.dismiss();
                                                Toast.makeText(BookDetails3NPSActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
            //true indicates that new image is uploaded.
            return true;
        }
        //if user hasn't selected image and wants to add the book data to database
        else {

            progressDialog.setTitle("Saving data....");
            progressDialog.show();

            uBook = createBook(childVal, oldImg);
            Map<String, Object> bookValues = uBook.toMap();
            //add book data to specific location
            bookRef.child(childVal).updateChildren(bookValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                        if (!category.equals("return") && !category.equals("used")) {
                            SearchIndex indexBook = new SearchIndex(uBook.getName(), uBook.getSearchName(), uBook.getAuthor(), uBook.getPublication(),
                                    uBook.getImg(), uBook.getId(), uBook.getCode(), course, category, "books", uBook.getOriginalPrice(),
                                    uBook.getDiscountedPrice(), uBook.getDiscount(), uBook.getAvailability(), uBook.getVisibility(), combination);

                            BooksLocationsRef.child(childVal).setValue(course + "/" + category + "/" + combination);
                            IndexForSearchRef.child(uBook.getId()).setValue(indexBook);

                            Toast.makeText(BookDetails3NPSActivity.this, "book data added", Toast.LENGTH_SHORT).show();
                        }
                        finish();


                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(BookDetails3NPSActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });


            //returning false indicates that no image is uploaded
            return false;


        }


    }

    private Book createBook(String id, String picStr) {
        return new Book(etName.getText().toString(),
                etName.getText().toString().toLowerCase(),
                authorView.getText().toString(),
                etPublication.getText().toString(),
                picStr,
                id,
                id,//todo review "code"
                Integer.parseInt(originalPriceView.getText().toString()),
                Integer.parseInt(discountedPriceView.getText().toString()),
                Integer.parseInt(discountView.getText().toString()),
                availabilityView.isChecked(),
                visibilityView.isChecked(), "books");


    }

    @Override
    public void onBackPressed() {
        saveUri = null;
        super.onBackPressed();
    }
}
