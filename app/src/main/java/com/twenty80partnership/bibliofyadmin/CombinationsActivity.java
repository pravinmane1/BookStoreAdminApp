package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofyadmin.models.ApplicableTerm;
import com.twenty80partnership.bibliofyadmin.models.Book;
import com.twenty80partnership.bibliofyadmin.models.Item;
import com.twenty80partnership.bibliofyadmin.models.ReturnableInfo;
import com.twenty80partnership.bibliofyadmin.viewHolders.CombinationViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CombinationsActivity extends AppCompatActivity {

    private Spinner spinner1, spinner2, spinner3;
    private DatabaseReference codeRef;
    private Button btnGo;

    Boolean year1common = false;

    private ArrayList<Item> OneList;
    private ArrayList<Item> TwoList;
    private ArrayList<Item> ThreeList;
    DataSnapshot oneData;
    DataSnapshot twoData;// = dataSnapshot.child("EnggYear");
    DataSnapshot threeData;// = dataSnapshot.child("EnggSem");
    private ArrayAdapter<String> oneAdapter, twoAdapter, threeAdapter;
    private ArrayList<String> first, second, third;
    private String searchCode = "______", oneCode = "__", twoCode = "__", threeCode = "__";

    String typeName, course, category;

    private DatabaseReference applicableTermsRef, SPPUtermsListingRef;
    private ArrayList<ApplicableTerm> applicableTermList;
    private String oldOneCode;
    private RecyclerView itemList;
    private FirebaseDatabase db;
    private ProgressDialog pdFetching;
    private Switch swCommon;
    private LinearLayout llCommon;
    private DatabaseReference combinationRef;
    private int minimumReturnBookCount;
    private boolean publicationSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_combinations);

        db = FirebaseDatabase.getInstance();

        pdFetching = new ProgressDialog(CombinationsActivity.this);
        pdFetching.setMessage("Fetching...");
        pdFetching.setCancelable(false);

        Intent intent = getIntent();
        course = intent.getStringExtra("course");
        category = intent.getStringExtra("category");
        typeName = intent.getStringExtra("typeName");
        publicationSystem = intent.getBooleanExtra("publicationSystem",false);

        Toast.makeText(this, course + category, Toast.LENGTH_SHORT).show();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle(typeName + " " + "Combinations");

        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        spinner3 = findViewById(R.id.spinner3);
        btnGo = findViewById(R.id.btn_go);
        swCommon = findViewById(R.id.sw_return_all_enable);
        llCommon = findViewById(R.id.ll_common);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(CombinationsActivity.this, searchCode, Toast.LENGTH_SHORT).show();

                if (publicationSystem){
                    Intent intent = new Intent(CombinationsActivity.this, BookList2PSActivity.class);
                    intent.putExtra("category", category);
                    intent.putExtra("typeName", typeName);
                    intent.putExtra("course", course);
                    intent.putExtra("combination", searchCode);
                    startActivity(intent);
                }
                else{
                    Intent intent = new Intent(CombinationsActivity.this, BookList3NPSActivity.class);
                    intent.putExtra("category", category);
                    intent.putExtra("typeName", typeName);
                    intent.putExtra("course", course);
                    intent.putExtra("combination", searchCode);
                    startActivity(intent);
                }


            }
        });


        if (category.equals("return"))
            llCommon.setVisibility(View.VISIBLE);
        else
            llCommon.setVisibility(View.GONE);

        pdFetching.show();

        db.getReference("ReturnEnablement").child("SPPU").child(course).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                pdFetching.dismiss();

                if (dataSnapshot.child("common").child("returnable").exists()) {
                    swCommon.setChecked(dataSnapshot.child("common").child("returnable").getValue(Boolean.class));
                } else {
                    swCommon.setChecked(false);
                }


                swCommon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            db.getReference("ReturnEnablement").child("SPPU").child(course).child(ds.getKey()).child("returnable").setValue(isChecked);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                pdFetching.dismiss();
            }
        });


        //these will store direct data from database as a object containing codes to search
        OneList = new ArrayList<Item>();
        TwoList = new ArrayList<Item>();
        ThreeList = new ArrayList<Item>();

        //these will be used to show the data in spinners which is obtained by above lists
        first = new ArrayList<String>();
        second = new ArrayList<String>();
        third = new ArrayList<String>();

        db.getReference("SPPUbooksListing")
                .child("details").child(course).child("year1common").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(Boolean.class)){
                    year1common = true;
                }
                else{
                    year1common = false;
                }

                applicableTermsRef = db
                        .getReference("SPPUbooksListing").child("category").child(course).child(category).child("applicableTerms");

                SPPUtermsListingRef = db.getReference("SPPUtermsListing").child(course);

                codeRef = db.getReference("SPPUbooksListing").child("codes").child(course);

                applicableTermsRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //ensuring that no data present in arraylists
                        OneList.clear();
                        TwoList.clear();
                        ThreeList.clear();

                        first.clear();
                        second.clear();
                        third.clear();

                        applicableTermList = new ArrayList<ApplicableTerm>();

                        if (dataSnapshot.getChildrenCount() == 0) {
                            Toast.makeText(CombinationsActivity.this, "No applicable code added", Toast.LENGTH_SHORT).show();
                            spinner1.setVisibility(View.GONE);
                            spinner2.setVisibility(View.GONE);
                            spinner3.setVisibility(View.GONE);
                        } else {

                            if(dataSnapshot.getChildrenCount()==2){
                                spinner3.setVisibility(View.INVISIBLE);
                            }
                            else if (dataSnapshot.getChildrenCount()==1){
                                spinner2.setVisibility(View.INVISIBLE);
                                spinner3.setVisibility(View.INVISIBLE);
                            }
                            for (DataSnapshot currentApplicable : dataSnapshot.getChildren()) {
                                ApplicableTerm applicableTerm = currentApplicable.getValue(ApplicableTerm.class);
                                applicableTermList.add(applicableTerm);
                            }
                        }

                        SPPUtermsListingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String termId = "";


                                for (int i = 0; i < applicableTermList.size(); i++) {
                                    termId = applicableTermList.get(i).getTermId();

                                    ApplicableTerm temp;
                                    temp = applicableTermList.get(i);

                                   if (!dataSnapshot.child(termId).child("topic").exists()){
                                        Toast.makeText(CombinationsActivity.this, "This combination contains 'CORRUPT APPLICABLE TERMS' Please Remove them first.", Toast.LENGTH_LONG).show();
                                        if (pdFetching.isShowing()){
                                            pdFetching.dismiss();
                                        }
                                        finish();
                                        break;
                                    }

                                    temp.setTopic(dataSnapshot.child(termId).child("topic").getValue(String.class));
                                    Log.d("CombinationsActivity11", temp.getTopic());
                                    applicableTermList.set(i, temp);

                                }


                                for (int i = 0; i < applicableTermList.size(); i++) {
                                    switch (i) {
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

                                        default:
                                            Toast.makeText(CombinationsActivity.this, "more than limited 3 terms", Toast.LENGTH_SHORT).show();
                                    }
                                }


                                codeRef.orderByChild("priority").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        Item currentOne;
                                        Item currentTwo;
                                        Item currentThree;
                                        //children of term which contain further codes
                                        for (int i = 0; i < applicableTermList.size(); i++) {
                                            switch (i) {
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
                                            }
                                        }


                                        for (int i = 0; i < applicableTermList.size(); i++) {


                                            switch (i) {
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

                                                                    if (spinner2.getVisibility()==View.VISIBLE)
                                                                    spinner2.performClick();

                                                                    break;
                                                                }

                                                            }

                                                            if (!codeFoundForSelectedString) {
                                                                oneCode = "__";
                                                            }

                                                            if (twoCode.equals("01") && codeFoundForSelectedString && year1common) {
                                                                oldOneCode = oneCode;
                                                                oneCode = "XX";
                                                            }
                                                            searchCode = oneCode + twoCode + threeCode;
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

                                                                    if (spinner3.getVisibility()==View.VISIBLE)
                                                                        spinner3.performClick();

                                                                    break;
                                                                }
                                                            }

                                                            if (!codeFoundForSelectedString) {
                                                                twoCode = "__";
                                                            }

                                                            if (twoCode.equals("01")&& year1common) {
                                                                if (!oneCode.equals("XX")){
                                                                    oldOneCode = oneCode;
                                                                }
                                                                oneCode = "XX";
                                                            } else if (oneCode.equals("XX")) {
                                                                oneCode = oldOneCode;
                                                            }
                                                            searchCode = oneCode + twoCode + threeCode;
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
                                                            boolean codeFoundForSelectedString = false;

                                                            if (semSelected.equals(applicableTermList.get(2).getTermId())) {
                                                                threeCode = "__";
                                                                searchCode = oneCode + twoCode + threeCode;
                                                                return;
                                                            }

                                                            for (int i = 0; i < ThreeList.size(); i++) {
                                                                if (ThreeList.get(i).getName().equals(semSelected)) {
                                                                    threeCode = ThreeList.get(i).getCode();
                                                                    codeFoundForSelectedString = true;
                                                                    break;
                                                                }
                                                            }

                                                            if (!codeFoundForSelectedString) {
                                                                threeCode = "__";
                                                            }
                                                            searchCode = oneCode + twoCode + threeCode;
                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> parent) {

                                                        }
                                                    });
                                                    break;
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
                                Toast.makeText(CombinationsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(CombinationsActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        //LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        //itemList.setLayoutManager(mLayoutManager);
        itemList.setLayoutManager(new GridLayoutManager(this, 2));

        if(publicationSystem)
        combinationRef = db.getReference("SPPUbooksTemplates").child(course).child(category);

        else
            combinationRef = db.getReference("SPPUbooks").child(course).child(category);

        Query query = combinationRef.orderByChild("priority");

        firebaseSearch(query);
    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<Item, CombinationViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Item, CombinationViewHolder>(
                Item.class, R.layout.item_row, CombinationViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final CombinationViewHolder viewHolder, final Item model, final int position) {

                final String keyId = this.getRef(position).getKey();


                viewHolder.setDetails(keyId);

                viewHolder.itemCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (publicationSystem){
                            Intent intent = new Intent(CombinationsActivity.this, BookList2PSActivity.class);
                            intent.putExtra("category", category);
                            intent.putExtra("typeName", typeName);
                            intent.putExtra("course", course);
                            intent.putExtra("combination", keyId);
                            startActivity(intent);
                        }
                        else{
                            Intent intent = new Intent(CombinationsActivity.this, BookList3NPSActivity.class);
                            intent.putExtra("category", category);
                            intent.putExtra("typeName", typeName);
                            intent.putExtra("course", course);
                            intent.putExtra("combination", keyId);
                            startActivity(intent);
                        }

                    }
                });

                if (category.equals("return")) {
                    viewHolder.more.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            pdFetching.show();

                            db.getReference("ReturnEnablement").child("SPPU").child(course).child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    pdFetching.dismiss();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(CombinationsActivity.this);
                                    LayoutInflater layoutInflater = CombinationsActivity.this.getLayoutInflater();
                                    View customDialog = layoutInflater.inflate(R.layout.return_enablement, null);
                                    builder.setView(customDialog);

                                    builder.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                                    final EditText etDesc = customDialog.findViewById(R.id.et_desc);
                                    final Switch swReturnable = customDialog.findViewById(R.id.sw_returnable);
                                    final EditText etRequired = customDialog.findViewById(R.id.et_required);

                                    Button btnSave = customDialog.findViewById(R.id.btn_save);
                                    Button btnAutocalculate = customDialog.findViewById(R.id.btn_autocalculate);
                                    final EditText etCartRequired = customDialog.findViewById(R.id.et_cart_required);


                                    final AlertDialog alertDialog = builder.create();

                                    btnAutocalculate.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            pdFetching.show();

                                            combinationRef.child(keyId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()) {


                                                        if (minimumReturnBookCount != 0) {

                                                            Map<String, Integer> codesMap = new HashMap<>();

                                                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                                                Log.d("Combinationsdebug", "bookdata " + ds.getValue());

                                                                Book book = ds.getValue(Book.class);

                                                                if (book.getCode() != null) {
                                                                    if (codesMap.containsKey(book.getCode())) {

                                                                        Log.d("Combinationsdebug", "code exists in mp " + book.getCode());
                                                                        Log.d("Combinationsdebug", "discounted price in mp " + codesMap.get(book.getCode()));


                                                                        if (codesMap.get(book.getCode()) < book.getDiscountedPrice()) {
                                                                            Log.d("Combinationsdebug", "updating entry in mp code: " + book.getCode() + " price " + book.getDiscountedPrice());

                                                                            codesMap.put(book.getCode(), book.getDiscountedPrice());
                                                                        }
                                                                    } else {
                                                                        Log.d("Combinationsdebug", "new entry in mp code: " + book.getCode() + " price " + book.getDiscountedPrice());

                                                                        codesMap.put(book.getCode(), book.getDiscountedPrice());
                                                                    }
                                                                }


                                                            }


                                                            List<Integer> list = new ArrayList<Integer>(codesMap.values());
                                                            Collections.sort(list, Collections.reverseOrder());

                                                            int sum = 0;
                                                            for (int i = 0; i < minimumReturnBookCount; i++) {
                                                                sum = sum + list.get(i);
                                                            }

                                                            etDesc.setText(String.valueOf(sum));

                                                            pdFetching.dismiss();


                                                        } else {
                                                            pdFetching.dismiss();
                                                            Toast.makeText(CombinationsActivity.this, "Please enter minimum return books", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    pdFetching.dismiss();
                                                }
                                            });
                                        }
                                    });

                                    btnSave.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            String desc = etDesc.getText().toString();
                                            Boolean returnable = swReturnable.isChecked();

                                            Integer required = null;
                                            Integer cartRequired = null;
                                            if (etRequired.getText() != null && !etRequired.getText().toString().isEmpty()) {
                                                required = Integer.parseInt(etRequired.getText().toString());
                                            }

                                            if (etCartRequired.getText() != null && !etCartRequired.getText().toString().isEmpty()) {
                                                cartRequired = Integer.parseInt(etCartRequired.getText().toString());
                                            }

                                            if (required != null && cartRequired != null) {

                                                ReturnableInfo returnableInfo = new ReturnableInfo();
                                                returnableInfo.setDescription(desc);
                                                returnableInfo.setReturnable(returnable);
                                                returnableInfo.setRequired(required);
                                                returnableInfo.setCartNewBooksRequiredCount(cartRequired);
                                                db.getReference("ReturnEnablement").child("SPPU").child(course).child(keyId).setValue(returnableInfo);

                                                alertDialog.dismiss();
                                            } else {
                                                Toast.makeText(CombinationsActivity.this, "Please enter required counts", Toast.LENGTH_SHORT).show();
                                            }


                                        }
                                    });

                                    if (dataSnapshot.exists()) {
                                        ReturnableInfo returnableInfo = dataSnapshot.getValue(ReturnableInfo.class);

                                        if (returnableInfo.getDescription() != null)
                                            etDesc.setText(returnableInfo.getDescription());

                                        if (returnableInfo.getReturnable()) {
                                            swReturnable.setChecked(true);
                                        } else {
                                            swReturnable.setChecked(false);
                                        }

                                        if (returnableInfo.getRequired() != null) {
                                            etRequired.setText(returnableInfo.getRequired().toString());
                                            minimumReturnBookCount = returnableInfo.getRequired();
                                        }
                                        else {
                                            minimumReturnBookCount = 0;
                                        }

                                        if (returnableInfo.getCartNewBooksRequiredCount() != null) {
                                            etCartRequired.setText(returnableInfo.getCartNewBooksRequiredCount().toString());
                                        }

                                    }
                                    else{
                                        minimumReturnBookCount = 0;
                                    }
                                    alertDialog.show();

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    pdFetching.dismiss();
                                }
                            });


                        }
                    });
                }
            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    public static HashMap<String, Integer> sortByValue(HashMap<String, Integer> hm) {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}