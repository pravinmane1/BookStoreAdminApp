package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.twenty80partnership.bibliofyadmin.models.SearchIndex;
import com.twenty80partnership.bibliofyadmin.models.Item;


import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class BookDetailsActivity extends AppCompatActivity {
    private Spinner spinner1, spinner2, spinner3, spinner4;
    private DatabaseReference codeRef;

    private ArrayList<Item> OneList;
    private ArrayList<Item> TwoList;
    private ArrayList<Item> ThreeList;
    private ArrayList<Item> FourList;
    DataSnapshot oneData;
    DataSnapshot twoData;// = dataSnapshot.child("EnggYear");
    DataSnapshot threeData;// = dataSnapshot.child("EnggSem");
    DataSnapshot fourData ;
    private ArrayAdapter<String> oneAdapter,twoAdapter,threeAdapter,fourAdapter;
    private ArrayList<String> first, second, third, fourth;
    private String searchCode = " ",oneCode = "",twoCode = "",threeCode = "",fourCode = "";
    EditText nameView,idView,originalPriceView,discountView,discountedPriceView,authorView;
    ImageView imgView;
    TextView removeView;
    EditText publicationView;
    Switch availabilityView,visibilityView;
    String mode,course,category;
    Book book;
    private  final  int PICK_IMAGE_REQUEST = 71;
    Button update;
    ProgressDialog progressDialog;
    private Uri saveUri;
    private StorageReference imageFolder;
    private DatabaseReference BooksLocationsRef,applicableTermsRef,SPPUtermsListingRef;
    private ArrayList<ApplicableTerm> applicableTermList;
    private String debugCode;
    private Book uBook;
    private DatabaseReference bookRef;
    private String childVal;
    private DatabaseReference IndexForSearchRef,cartIndexRef;
    private int publicationNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);


        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);



        progressDialog = new ProgressDialog(BookDetailsActivity.this);
        progressDialog.setTitle("Saving Data...");
        progressDialog.setCancelable(false);

        nameView = findViewById(R.id.name);
        publicationView = findViewById(R.id.publication);
        availabilityView = findViewById(R.id.switch1);
        visibilityView = findViewById(R.id.switch2);
        idView = findViewById(R.id.id);
        originalPriceView = findViewById(R.id.original_price);
        discountView = findViewById(R.id.discount);
        discountedPriceView = findViewById(R.id.discounted_price);
        imgView = findViewById(R.id.img);
        update = findViewById(R.id.update);
        authorView = findViewById(R.id.author);
        removeView = findViewById(R.id.remove);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        course = intent.getStringExtra("course");
        category = intent.getStringExtra("category");


        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        spinner4 = findViewById(R.id.spinner4);


        if (mode.equals("edit")){


            spinner1.setEnabled(false);
            spinner2.setEnabled(false);
            spinner3.setEnabled(false);
            spinner4.setEnabled(false);

            removeView.setVisibility(View.VISIBLE);
            update.setText("update");
            book = (Book)intent.getSerializableExtra("book");
            searchCode = book.getCode();
            debugCode = searchCode;
            Log.d("debuglength","searchCode is initialised to "+searchCode);

            nameView.setText(book.getName());
            publicationView.setText(book.getPublication());
            idView.setText(book.getId());
            originalPriceView.setText(""+book.getOriginalPrice());
            discountView.setText(""+book.getDiscount());
            discountedPriceView.setText(""+book.getDiscountedPrice());
            authorView.setText(book.getAuthor());

            if (book.getImg()!=null && !book.getImg().isEmpty())
                Picasso.get().load(book.getImg()).into(imgView);

            if (book.getAvailability()!=null)
                availabilityView.setChecked(book.getAvailability());

            if (book.getVisibility()!=null)
                visibilityView.setChecked(book.getVisibility());



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

                if(!discountView.getText().toString().isEmpty() && !s.toString().isEmpty()){
                    Float discountedPrice = Float.parseFloat(s.toString())*((100-Float.parseFloat(discountView.getText().toString()))/100);

                    discountedPriceView.setText(String.valueOf(Math.round(discountedPrice)));
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

                if(!originalPriceView.getText().toString().isEmpty() && !s.toString().isEmpty()){
                    Float discountedPrice = Float.parseFloat(originalPriceView.getText().toString())*((100-Float.parseFloat(s.toString()))/100);

                    discountedPriceView.setText(String.valueOf(Math.round(discountedPrice)));
                }

            }
        });




        //these will store direct data from database as a object containing codes to search
        OneList = new ArrayList<Item>();
        TwoList = new ArrayList<Item>();
        ThreeList = new ArrayList<Item>();
        FourList = new ArrayList<Item>();

        //these will be used to show the data in spinners which is obtained by above lists
        first = new ArrayList<String>();
        second = new ArrayList<String>();
        third = new ArrayList<String>();
        fourth = new ArrayList<String>();

        applicableTermsRef = FirebaseDatabase.getInstance()
                .getReference("SPPUbooksListing").child("category").child(course).child(category).child("applicableTerms");

        SPPUtermsListingRef = FirebaseDatabase.getInstance().getReference("SPPUtermsListing").child(course);

        codeRef = FirebaseDatabase.getInstance().getReference("SPPUbooksListing").child("codes").child(course);

        applicableTermsRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //ensuring that no data present in arraylists
                OneList.clear();
                TwoList.clear();
                ThreeList.clear();
                FourList.clear();

                first.clear();
                second.clear();
                third.clear();
                fourth.clear();

                applicableTermList = new ArrayList<ApplicableTerm>();

                if (dataSnapshot.getChildrenCount()==0){
                    Toast.makeText(BookDetailsActivity.this,"No applicable code added",Toast.LENGTH_SHORT).show();
                }
                else {
                    publicationNumber = -1;
                    int i = 0;
                    for (DataSnapshot currentApplicable:dataSnapshot.getChildren()){
                        ApplicableTerm applicableTerm = currentApplicable.getValue(ApplicableTerm.class);
                        applicableTermList.add(applicableTerm);

                        if(applicableTerm.getTermId().equals("publication")){
                            publicationNumber = i;
                        }

                        i++;
                    }
                }

                SPPUtermsListingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String termId="";


                        for (int i=0;i<applicableTermList.size();i++){
                           termId  = applicableTermList.get(i).getTermId();

                           ApplicableTerm temp;
                           temp = applicableTermList.get(i);
                           temp.setTopic(dataSnapshot.child(termId).child("topic").getValue(String.class));
                           applicableTermList.set(i,temp);
                        }


                        for (int i=0;i<applicableTermList.size();i++){
                            switch (i){
                                case 0:
                                    first.add(applicableTermList.get(i).getTopic());
                                    oneAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, first);
                                    spinner1.setAdapter(oneAdapter);
                                    break;
                                case 1:
                                    second.add(applicableTermList.get(i).getTopic());
                                    twoAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, second);
                                    spinner2.setAdapter(twoAdapter);
                                    break;
                                case 2:
                                    third.add(applicableTermList.get(i).getTopic());
                                    threeAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, third);
                                    spinner3.setAdapter(threeAdapter);
                                    break;
                                case 3:
                                    fourth.add(applicableTermList.get(i).getTopic());
                                    fourAdapter = new ArrayAdapter(getApplicationContext(), R.layout.spinner_item, fourth);
                                    spinner4.setAdapter(fourAdapter);
                                    break;

                                default:
                                    Toast.makeText(BookDetailsActivity.this,"more than limited terms",Toast.LENGTH_SHORT).show();
                            }
                        }


                        codeRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Item currentOne;
                                Item currentTwo;
                                Item currentThree;
                                Item currentFour;

                                //children of term which contain further codes
                                for (int i=0;i<applicableTermList.size();i++){
                                    switch (i){
                                        case 0:
                                            oneData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                            for (DataSnapshot branch : oneData.getChildren()) {

                                                currentOne = branch.getValue(Item.class);
                                                OneList.add(currentOne);
                                                first.add(currentOne.getName());

                                            }
                                            oneAdapter.notifyDataSetChanged();
                                            break;
                                        case 1:
                                            twoData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                            for (DataSnapshot year : twoData.getChildren()) {

                                                currentTwo = year.getValue(Item.class);
                                                TwoList.add(currentTwo);
                                                second.add(currentTwo.getName());

                                            }
                                            twoAdapter.notifyDataSetChanged();
                                            break;
                                        case 2:
                                            threeData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                            for (DataSnapshot sem : threeData.getChildren()) {

                                                currentThree = sem.getValue(Item.class);
                                                ThreeList.add(currentThree);
                                                third.add(currentThree.getName());

                                            }
                                            threeAdapter.notifyDataSetChanged();
                                            break;
                                        case 3:
                                            fourData = dataSnapshot.child(applicableTermList.get(i).getTermId());
                                            for (DataSnapshot publication : fourData.getChildren()) {

                                                currentFour = publication.getValue(Item.class);
                                                FourList.add(currentFour);
                                                fourth.add(currentFour.getName());

                                            }
                                            fourAdapter.notifyDataSetChanged();
                                            break;
                                    }
                                }


                                for(int i=0;i<applicableTermList.size();i++){


                                    switch (i){
                                        case 0:
                                            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                                    //obtain the  selected branch from spinner
                                                    String branchSelected = spinner1.getSelectedItem().toString();

                                                    boolean codeFoundForSelectedString = false;

                                                    //loop applied on arrayList to find the code for selected branch from objects arrayList
                                                    for (int i = 0; i < OneList.size(); i++) {

                                                        if (OneList.get(i).getName().equals(branchSelected)) {

                                                            oneCode = OneList.get(i).getCode();
                                                            codeFoundForSelectedString = true;
                                                            break;
                                                        }

                                                    }

                                                    if (!codeFoundForSelectedString) {
                                                        oneCode = " ";
                                                    }

                                                    if (twoCode.equals("01")) {
                                                        oneCode = "XX";
                                                    }

                                                    //check for publication to add value to publication key
                                                    if(publicationNumber==0){
                                                        publicationView.setText(branchSelected);
                                                    }


                                                    searchCode = oneCode + twoCode + threeCode + fourCode;

                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> parent) {

                                                }
                                            });
                                            break;
                                        case 1:
                                            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                                    String yearSelected = spinner2.getSelectedItem().toString();
                                                    boolean codeFoundForSelectedString = false;

                                                    for (int i = 0; i < TwoList.size(); i++) {
                                                        if (TwoList.get(i).getName().equals(yearSelected)) {
                                                            twoCode = TwoList.get(i).getCode();
                                                            codeFoundForSelectedString = true;
                                                            break;
                                                        }
                                                    }

                                                    if (!codeFoundForSelectedString ) {
                                                        twoCode = " ";
                                                    }

                                                    if (twoCode.equals("01")) {
                                                        oneCode = "XX";
                                                    }

                                                    //check for publication to add value to publication key
                                                    if(publicationNumber==1){
                                                        publicationView.setText(yearSelected);
                                                    }

                                                    searchCode = oneCode + twoCode + threeCode + fourCode;


                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> parent) {

                                                }
                                            });
                                            break;
                                        case 2:
                                            spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                                    String semSelected = spinner3.getSelectedItem().toString();

                                                    if (semSelected.equals(applicableTermList.get(2).getTermId()) ){
                                                        threeCode = "";
                                                        searchCode = oneCode + twoCode + threeCode + fourCode;
                                                        return;
                                                    }

                                                    for (int i = 0; i < ThreeList.size(); i++) {
                                                        if (ThreeList.get(i).getName().equals(semSelected)) {
                                                            threeCode = ThreeList.get(i).getCode();
                                                            break;
                                                        }
                                                    }

                                                    //check for publication to add value to publication key
                                                    if(publicationNumber==2){
                                                        publicationView.setText(semSelected);
                                                    }


                                                    searchCode = oneCode + twoCode + threeCode + fourCode;
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> parent) {

                                                }
                                            });
                                            break;
                                        case 3:
                                            spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                                    String publicationSelected = spinner4.getSelectedItem().toString();

                                                    if (publicationSelected.equals(applicableTermList.get(3).getTermId())){
                                                        fourCode = "";
                                                        searchCode = oneCode + twoCode + threeCode + fourCode;
                                                        return;
                                                    }

                                                    for (int i = 0; i < FourList.size(); i++) {
                                                        if (FourList.get(i).getName().equals(publicationSelected)) {
                                                            fourCode = FourList.get(i).getCode();
                                                            break;
                                                        }
                                                    }

                                                    //check for publication to add value to publication key
                                                    if(publicationNumber==3){
                                                        publicationView.setText(publicationSelected);
                                                    }

                                                    searchCode = oneCode + twoCode + threeCode + fourCode;
                                                }

                                                @Override
                                                public void onNothingSelected(AdapterView<?> parent) {
                                                    Toast.makeText(BookDetailsActivity.this,"nothing selected",Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            break;
                                    }
                                }

                                //adding data from code child to arrayLists
                                //after adding data to the arrayLists the adapter need to update
                                if (mode.equals("edit")){
                                    //for branch
                                    //obtain the  selected branch from spinner


                                    for (int j=0;j<applicableTermList.size();j++) {
                                        switch (j) {

                                            case 0:

                                                if (debugCode.length()>=2){

                                                    String oneCodeObtained = debugCode.substring(0, 2);

                                                    Log.d("debuglength","debugCode in case 0 after subs is :  "+oneCodeObtained);

                                                //loop applied on arrayList to find the code for selected branch from objects arrayList
                                                    for (int i = 0; i < OneList.size(); i++) {

                                                        Log.d("branchdebug", "branch code from list:" + OneList.get(i).getCode() + " code obtained:" + oneCodeObtained);
                                                        Log.d("compare1","codeExtract: "+oneCodeObtained+" codeDatabase: "+OneList.get(i).getCode());
                                                        if (OneList.get(i).getCode().equals(oneCodeObtained)) {

                                                            spinner1.setSelection(i + 1);
                                                            Log.d("branchdebug", spinner1.getSelectedItem().toString());
                                                            break;
                                                        }

                                                    }
                                                }
                                            break;

                                            case 1:

                                                if (debugCode.length()>=4) {


                                                    //for year
                                                    //obtain the  selected year from spinner
                                                    Log.d("debuglength", "debugCode in case 1 is :  " + debugCode);
                                                    String twoCodeObtained = debugCode.substring(2, 4);
                                                    Log.d("debuglength", "debugCode in case 1 after subs is :  " + twoCodeObtained);


                                                    //loop applied on arrayList to find the code for selected branch from objects arrayList
                                                    for (int i = 0; i < TwoList.size(); i++) {
                                                        Log.d("compare2", "codeExtract: " + twoCodeObtained + " codeDatabase: " + TwoList.get(i).getCode());

                                                        Log.d("yeardebug", "branch code from list:" + TwoList.get(i).getCode() + " code obtained:" + twoCodeObtained);
                                                        if (TwoList.get(i).getCode().equals(twoCodeObtained)) {

                                                            spinner2.setSelection(i + 1);
                                                            Log.d("branchdebug", spinner2.getSelectedItem().toString());
                                                            break;
                                                        }

                                                    }

                                                }
                                            break;


                                            case 2:

                                                if (debugCode.length()>=6) {


                                                    //for sem
                                                    //obtain the  selected sem from spinner
                                                    String threeCodeObtained = debugCode.substring(4, 6);

                                                    //loop applied on arrayList to find the code for selected branch from objects arrayList
                                                    for (int i = 0; i < ThreeList.size(); i++) {
                                                        Log.d("compare3", "codeExtract: " + threeCodeObtained + " codeDatabase: " + ThreeList.get(i).getCode());

                                                        if (ThreeList.get(i).getCode().equals(threeCodeObtained)) {

                                                            spinner3.setSelection(i + 1);
                                                            break;
                                                        }

                                                    }
                                                }
                                            break;

                                            case 3:
                                                if (debugCode.length()>=8) {
                                                    //for Publication
                                                    //obtain the  selected publication from spinner
                                                    String fourCodeObtained = debugCode.substring(6, 8);

                                                    //loop applied on arrayList to find the code for selected branch from objects arrayList
                                                    for (int i = 0; i < FourList.size(); i++) {

                                                        if (FourList.get(i).getCode().equals(fourCodeObtained)) {

                                                            spinner4.setSelection(i + 1);
                                                            break;
                                                        }

                                                    }
                                                }
                                            break;


                                            default:

                                        }
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(BookDetailsActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(BookDetailsActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
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
                if (getIntent().getStringExtra("mode").equals("edit")){
                    oldPic = book.getImg();
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

                                DatabaseReference bookRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).child(category).child(book.getId());
                                bookRef.child("img").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        progressDialog.dismiss();
                                        if (task.isSuccessful()){
                                            Toast.makeText(BookDetailsActivity.this,"img is removed from database and storage",Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            Toast.makeText(BookDetailsActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(BookDetailsActivity.this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                                // Uh-oh, an error occurred!
                                Log.d("update", "onFailure: did not delete file");
                            }
                        });
                    }
                    else {
                        Toast.makeText(BookDetailsActivity.this,"no pic set",Toast.LENGTH_SHORT).show();
                    }

                }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("notSetDebug",oneCode+" "+twoCode+" "+threeCode+" "+fourCode);

                boolean codeSelected = true;

                for (int i=0;i<applicableTermList.size();i++){

                    switch (i){
                        case 0:
                            if (oneCode.isEmpty()){ codeSelected = false;}
                            break;
                        case 1:
                            if (twoCode.isEmpty()){ codeSelected = false;}
                            break;
                        case 2:
                            if (threeCode.isEmpty()){ codeSelected = false;}
                            break;
                        case 3:
                            if (fourCode.isEmpty()){ codeSelected = false;}
                            break;
                    }

                    if (!codeSelected){
                        break;
                    }
                }

                if (!nameView.getText().toString().isEmpty() && !authorView.getText().toString().isEmpty()
                        && !originalPriceView.getText().toString().isEmpty() &&
                        !discountedPriceView.getText().toString().isEmpty() && !discountView.getText().toString().isEmpty()
                && codeSelected) {
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
                        Toast.makeText(BookDetailsActivity.this, "Old image is kept", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(BookDetailsActivity.this, "data is not set", Toast.LENGTH_SHORT).show();
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


    private boolean uploadData(final String oldImg) {


        bookRef = FirebaseDatabase.getInstance().getReference("SPPUbooks").child(course).child(category);
        BooksLocationsRef = FirebaseDatabase.getInstance().getReference("BooksLocations").child("SPPUbooks");
        IndexForSearchRef = FirebaseDatabase.getInstance().getReference("SearchIndex").child("SPPU");


        //for existing book
        if (getIntent().getStringExtra("mode").equals("edit")) {

            cartIndexRef = FirebaseDatabase.getInstance().getReference("CartIndex").child("books").child(book.getId());

            //for cartIndex update
            cartIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue()!=null){
                        Log.d("presentDebug",dataSnapshot.getValue().toString());

                        for (DataSnapshot ds:dataSnapshot.getChildren()){

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
                    }
                    else {
                        Log.d("presentDebug","data is nuull");

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            //if updated code and old code are equal
            if (book.getCode().equals(searchCode)){

                childVal = book.getId();

                //code is same but searchname is changed. remove searchindex.
                if (!book.getSearchName().equals(nameView.getText().toString().toLowerCase())){
                    IndexForSearchRef.child(book.getSearchName()+book.getId()).removeValue();
                }
            }
            //if code doesn't match old and new , need to change id.
            else {

                //before changing id remove item from all users cart by cartReq.

                cartIndexRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot ds:dataSnapshot.getChildren()){

                            String uid = ds.getKey();

                            final DatabaseReference uCartReq = FirebaseDatabase.getInstance().getReference("CartReq").child(uid).child(book.getId());

                            uCartReq.removeValue();
                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


                String key = BooksLocationsRef.push().getKey();

                String shortSearch = book.getSearchName().substring(0, Math.min(book.getSearchName().length(), 8));
                childVal = searchCode+shortSearch+key;


                bookRef.child(book.getId()).removeValue();
                BooksLocationsRef.child(book.getId()).removeValue();
                IndexForSearchRef.child(book.getSearchName()+book.getId()).removeValue();
            }



        }
        //for new book
        else {
            String key = BooksLocationsRef.push().getKey();

            String shortSearch = nameView.getText().toString().toLowerCase().substring(0, Math.min(nameView.getText().toString().toLowerCase().length(), 8));

            childVal = searchCode+shortSearch+key;

        }

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
                                Toast.makeText(BookDetailsActivity.this, "Image Uploaded successfully", Toast.LENGTH_SHORT).show();
                                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {


                                        //after uploading image to storage

                                        uBook = createBook(childVal,uri.toString());

                                        Map<String, Object> bookValues = uBook.toMap();

                                        //add book data to specific location
                                        bookRef.child(childVal).updateChildren(bookValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    String searchId = uBook.getSearchName()+uBook.getId();

                                                    SearchIndex indexBook = new SearchIndex(uBook.getName(),uBook.getSearchName(),uBook.getAuthor(),uBook.getPublication(),
                                                            uBook.getImg(),uBook.getId(),uBook.getCode(),course,category,"books",uBook.getOriginalPrice(),
                                                            uBook.getDiscountedPrice(),uBook.getDiscount(),uBook.getAvailability(),uBook.getVisibility(),"oldVersionNoCombination");

                                                    BooksLocationsRef.child(childVal).setValue(course+"/"+category);
                                                    IndexForSearchRef.child(searchId).setValue(indexBook);

                                                    Toast.makeText(BookDetailsActivity.this, "book data added", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(BookDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

                uBook = createBook(childVal,oldImg);
                Map<String, Object> bookValues = uBook.toMap();
                //add book data to specific location
                bookRef.child(childVal).updateChildren(bookValues).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            String searchId = uBook.getSearchName()+uBook.getId();

                            SearchIndex indexBook = new SearchIndex(uBook.getName(),uBook.getSearchName(),uBook.getAuthor(),uBook.getPublication(),
                                    uBook.getImg(),uBook.getId(),uBook.getCode(),course,category,"books",uBook.getOriginalPrice(),
                                    uBook.getDiscountedPrice(),uBook.getDiscount(),uBook.getAvailability(),uBook.getVisibility(),"oldVersionNoCombination");

                            BooksLocationsRef.child(childVal).setValue(course+"/"+category);
                            IndexForSearchRef.child(searchId).setValue(indexBook);

                            Toast.makeText(BookDetailsActivity.this, "book data added", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(BookDetailsActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                //returning false indicates that no image is uploaded
                return false;


            }


        }

        private Book createBook(String id,String picStr){
           return new Book(nameView.getText().toString(),
                   nameView.getText().toString().toLowerCase(),
                   authorView.getText().toString(),
                   publicationView.getText().toString(),
                   picStr,
                   id,
                   searchCode,
                   Integer.parseInt(originalPriceView.getText().toString()),
                   Integer.parseInt(discountedPriceView.getText().toString()),
                   Integer.parseInt(discountView.getText().toString()),
                   availabilityView.isChecked(),
                   visibilityView.isChecked(),"books");


        }

    @Override
    public void onBackPressed() {
        saveUri =null;
        super.onBackPressed();
    }
}
