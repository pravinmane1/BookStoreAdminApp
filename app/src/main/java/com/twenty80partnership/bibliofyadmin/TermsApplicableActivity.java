package com.twenty80partnership.bibliofyadmin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofyadmin.models.ApplicableTerm;
import com.twenty80partnership.bibliofyadmin.viewHolders.CodesApplicableViewHolder;

public class TermsApplicableActivity extends AppCompatActivity {
    RecyclerView itemList;
    DatabaseReference SPPUcodesListingRef;
    String course, category;
    FloatingActionButton floatingActionButton;
    boolean editable;
    private EditText etPriority;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_applicable);

        db = FirebaseDatabase.getInstance();


        course = getIntent().getStringExtra("course");
        category = getIntent().getStringExtra("category");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        itemList = findViewById(R.id.recycler_view);
        itemList.setHasFixedSize(false);
        //LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        //itemList.setLayoutManager(mLayoutManager);
        itemList.setLayoutManager(new GridLayoutManager(this, 2));

        floatingActionButton = findViewById(R.id.add_new_code);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(TermsApplicableActivity.this, "Please wait a moment and try again...", Toast.LENGTH_SHORT).show();
            }
        });

        SPPUcodesListingRef = db.getReference("SPPUbooksListing").child("category").child(course)
                .child(category).child("applicableTerms");


        db.getReference("SPPUbooksTemplates").child(course).child(category).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Query query = SPPUcodesListingRef.orderByChild("priority");
                    firebaseSearch(query, false);

                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(TermsApplicableActivity.this, "Can't add more terms as templetes are present inside", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Query query = SPPUcodesListingRef.orderByChild("priority");
                    firebaseSearch(query, true);

                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(TermsApplicableActivity.this, AllTermsActivity.class);
                            intent.putExtra("course", course);
                            intent.putExtra("category", category);
                            intent.putExtra("categoryName", category);
                            intent.putExtra("mode", "select");
                            startActivityForResult(intent, 1);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void firebaseSearch(Query q, final Boolean editable) {

        FirebaseRecyclerAdapter<ApplicableTerm, CodesApplicableViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<ApplicableTerm, CodesApplicableViewHolder>(
                ApplicableTerm.class, R.layout.item_row, CodesApplicableViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final CodesApplicableViewHolder viewHolder, final ApplicableTerm model, final int position) {


                viewHolder.setDetails(model.getTermId(), model.getPriority());

                viewHolder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (editable) {

                            //creating a popup menu
                            PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.more);
                            //inflating menu from xml resource
                            popup.inflate(R.menu.codes_applicable_menu);
                            //adding click listener
                            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    switch (item.getItemId()) {
                                        case R.id.delete:
                                            SPPUcodesListingRef.child(model.getTermId()).removeValue();
                                            return true;

                                        case R.id.edit:
                                            update(model.getTermId(), model.getPriority(), "");
                                            return true;

                                        default:
                                            return false;
                                    }
                                }
                            });
                            //displaying the popup
                            popup.show();
                        } else {
                            Toast.makeText(TermsApplicableActivity.this, "Books templetes present inside.. not editable...", Toast.LENGTH_SHORT).show();
                        }


                    }
                });
            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    private void update(final String termId, int p, final String mode) {

        final AlertDialog.Builder alert = new AlertDialog.Builder(TermsApplicableActivity.this);
        alert.setTitle("Edit Priority");
        alert.setCancelable(false);
        //alert.setMessage("sample message");

        LayoutInflater inflater = TermsApplicableActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_applicable_term_dialog, null);
        alert.setView(customDialog);

        final EditText etId = (EditText) customDialog.findViewById(R.id.et_id);
        etPriority = (EditText) customDialog.findViewById(R.id.et_priority);


        Button btnSave = customDialog.findViewById(R.id.btn_save);
        btnSave.setText("update");

        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();

        etPriority.setText(p + "");
        etId.setText(termId);
        etId.setEnabled(false);

        btnSave.setOnClickListener(v -> {
            if (mode.equals("both")) {
                ApplicableTerm applicableTerm = new ApplicableTerm();
                applicableTerm.setTermId(termId);
                applicableTerm.setPriority(Integer.parseInt(etPriority.getText().toString()));
                SPPUcodesListingRef.child(termId).setValue(applicableTerm);
                dialog.dismiss();
            } else {
                SPPUcodesListingRef.child(termId).child("priority").setValue(Integer.parseInt(etPriority.getText().toString()));
                dialog.dismiss();
            }

        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {


                update(data.getStringExtra("id"), 0, "both");
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
