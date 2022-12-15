package com.twenty80partnership.bibliofyadmin;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofyadmin.models.Item;
import com.twenty80partnership.bibliofyadmin.viewHolders.CodeViewHolder;

public class CodeListActivity extends AppCompatActivity {
    String course, termId, termName;
    RecyclerView itemList;
    MaterialSearchBar materialSearchBar;
    String mode = "";
    private FloatingActionButton mFloatingActionButton;
    private Query query;
    private DatabaseReference SPPUcodesRef;
    private FirebaseDatabase db;
    private EditText name, id;
    private EditText priority;
    private EditText code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_list);

        db = FirebaseDatabase.getInstance();
        if (!Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(CodeListActivity.this);
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


        course = getIntent().getStringExtra("course");
        termId = getIntent().getStringExtra("termId");
        termName = getIntent().getStringExtra("termName");
        if (getIntent().getStringExtra("mode") != null) {
            mode = getIntent().getStringExtra("mode");
        }
        SPPUcodesRef = db.getReference("SPPUbooksListing").child("codes")
                .child(course).child(termId);

        Toast.makeText(this, termId, Toast.LENGTH_SHORT).show();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Codes for " + "\"" + termName + "\"");

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);
        materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    query = SPPUcodesRef.orderByChild("name");
                    firebaseSearch(query);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                query = SPPUcodesRef.orderByChild("name").startAt(text.toString()).endAt(text.toString() + "\uf8ff");
                firebaseSearch(query);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        //mLayoutManager.setReverseLayout(true);
        //mLayoutManager.setStackFromEnd(true);
        itemList.setLayoutManager(mLayoutManager);


        Query query = SPPUcodesRef.orderByChild("priority");

        firebaseSearch(query);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(CodeListActivity.this);
                alert.setTitle("Add Code");
                alert.setCancelable(false);
                //alert.setMessage("sample message");

                LayoutInflater inflater = CodeListActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_code_dialog, null);
                alert.setView(customDialog);
                name = (EditText) customDialog.findViewById(R.id.et_name);
                name.setHint("Display name for code");

                code = customDialog.findViewById(R.id.et_code);


                priority = (EditText) customDialog.findViewById(R.id.et_priority);
                Button btnSave = customDialog.findViewById(R.id.btn_save);


                alert.setIcon(R.drawable.plus_green);

                alert.setPositiveButton("DISMISS", (dialog, which) -> dialog.dismiss());

                final AlertDialog dialog = alert.create();
                dialog.show();

                btnSave.setOnClickListener(v1 -> {
                    Item item = new Item();
                    if (!name.getText().toString().isEmpty()
                            && !priority.getText().toString().isEmpty() && !code.getText().toString().isEmpty()) {

                        item.setName(name.getText().toString());
                        item.setCode(code.getText().toString());
                        item.setPriority(Float.parseFloat(priority.getText().toString()));

                        SPPUcodesRef.child(item.getCode()).setValue(item).addOnCompleteListener(task -> {
                            Log.d("codesListDebug", "it is " + termId);

                            updateCourseNode();


                            dialog.dismiss();
                        });


                    } else {
                        Toast.makeText(CodeListActivity.this, "Data is empty", Toast.LENGTH_SHORT).show();
                    }

                });
            }
        });


    }

    private void updateCourseNode() {
        if (termId.equals("year")) {

            Log.d("codesListDebug", "it is years");


            SPPUcodesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot.getChildrenCount()!=0){
                        db.getReference("Courses")
                                .child("SPPU").child(course).child(termId)
                                .setValue(snapshot.getChildrenCount());
                    }
                    else{
                        db.getReference("Courses")
                                .child("SPPU").child(course).child(termId)
                                .removeValue();
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else if (termId.equals("branch")) {
            SPPUcodesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    db.getReference("Courses")
                            .child("SPPU").child(course).child(termId)
                            .setValue(snapshot.getValue());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<Item, CodeViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Item, CodeViewHolder>(
                Item.class, R.layout.code_row, CodeViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final CodeViewHolder viewHolder, final Item model, final int position) {


                //Toast.makeText(CodeListActivity.this,model.getCode()+model.getName(),Toast.LENGTH_SHORT).show();
                viewHolder.setDetails(model.getName(), model.getCode(), model.getPriority());

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateCode(model);
                    }
                });

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
                                        SPPUcodesRef.child(model.getCode()).removeValue().addOnCompleteListener(task -> {

                                            updateCourseNode();
                                        });
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
        final AlertDialog.Builder alert = new AlertDialog.Builder(CodeListActivity.this);
        alert.setTitle("Edit Code");
        alert.setCancelable(false);
        //alert.setMessage("sample message");

        LayoutInflater inflater = CodeListActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.add_code_dialog, null);
        alert.setView(customDialog);
        name = (EditText) customDialog.findViewById(R.id.et_name);
        name.setHint("Display name for code");

        code = customDialog.findViewById(R.id.et_code);


        priority = (EditText) customDialog.findViewById(R.id.et_priority);
        Button btnSave = customDialog.findViewById(R.id.btn_save);

        code = (EditText) customDialog.findViewById(R.id.et_code);
        code.setInputType(InputType.TYPE_CLASS_TEXT);
        code.setVisibility(View.VISIBLE);
        code.setEnabled(false);
        code.setFocusable(false);
        code.setCursorVisible(false);


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

        if (item.getPriority() != null)
            priority.setText(item.getPriority().toString());

        btnSave.setText("UPDATE");


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!name.getText().toString().isEmpty() && !priority.getText().toString().isEmpty() && !code.getText().toString().isEmpty()) {
                    // item.setId(id.getText().toString());
                    item.setName(name.getText().toString());
                    item.setPriority(Float.parseFloat(priority.getText().toString()));


                    SPPUcodesRef.child(item.getCode()).setValue(item);
                    dialog.dismiss();
                } else {
                    Toast.makeText(CodeListActivity.this, "data is null", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
