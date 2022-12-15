package com.twenty80partnership.bibliofyadmin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofyadmin.models.MenuItem;
import com.twenty80partnership.bibliofyadmin.viewHolders.MenuItemViewHolder;

public class YMALMenuActivity extends AppCompatActivity {
    RecyclerView itemList;
    MaterialSearchBar materialSearchBar;
    private FloatingActionButton mFloatingActionButton;
    private Query query;
    private DatabaseReference youMayAlsoLikeMenuRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ymal_menu);


        if (!Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(YMALMenuActivity.this);
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

        youMayAlsoLikeMenuRef = FirebaseDatabase.getInstance().getReference("YouMayAlsoLikeMenu")
                .child("SPPU");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("You May Also Like");

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);
        materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    query = youMayAlsoLikeMenuRef.orderByChild("searchName");
                    firebaseSearch(query);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                query = youMayAlsoLikeMenuRef.orderByChild("searchName").startAt(text.toString()).endAt(text.toString() + "\uf8ff");
                firebaseSearch(query);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        itemList.setLayoutManager(new GridLayoutManager(this, 4));
        itemList.setHasFixedSize(true);


        Query query = youMayAlsoLikeMenuRef.orderByChild("priority");

        firebaseSearch(query);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                AlertDialog.Builder alert = new AlertDialog.Builder(YMALMenuActivity.this);
                alert.setTitle("Add New Menu Item");
                alert.setCancelable(false);

                LayoutInflater inflater = YMALMenuActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_new_ymal_menu_item, null);
                alert.setView(customDialog);

                Button btnBooks = customDialog.findViewById(R.id.btn_books);
                Button btnStationary = customDialog.findViewById(R.id.btn_stationary);
                Button btnMore = customDialog.findViewById(R.id.btn_more);


                alert.setIcon(R.drawable.plus_green);

                alert.setPositiveButton("DISMISS", (dialog, which) -> {
                    dialog.dismiss();
                });

                final AlertDialog dialog = alert.create();
                dialog.show();

                btnBooks.setOnClickListener(v1 -> {
                    dialog.dismiss();
                    Intent selectIntent = new Intent(YMALMenuActivity.this, CoursesListingActivity.class);
                    selectIntent.putExtra("mode", "menuItem");
                    startActivityForResult(selectIntent, 1);
                });

                btnStationary.setOnClickListener(v12 -> {
                    dialog.dismiss();
                    Intent selectIntent = new Intent(YMALMenuActivity.this, StationaryActivity.class);
                    selectIntent.putExtra("mode", "menuItem");
                    startActivityForResult(selectIntent, 1);
                });

                btnMore.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        MenuItem menuItem = new MenuItem();
                        menuItem.setType("more");
                        menuItem.setPriority(100f);
                        String key = youMayAlsoLikeMenuRef.push().getKey();
                        menuItem.setId(key);

                        youMayAlsoLikeMenuRef.child(key).setValue(menuItem);
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
                        addAndUpdate(menuItem);
                    }
                }
                break;
        }
    }

    private void addAndUpdate(final MenuItem menuItem) {

        AlertDialog.Builder alert = new AlertDialog.Builder(YMALMenuActivity.this);
        alert.setTitle("Additional Info");
        alert.setCancelable(false);

        LayoutInflater inflater = YMALMenuActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.menu_item_additional_info, null);
        alert.setView(customDialog);

        final TextView title = customDialog.findViewById(R.id.title);
        final TextView priority = customDialog.findViewById(R.id.topic);
        final TextView top = customDialog.findViewById(R.id.top);

        Button save = customDialog.findViewById(R.id.save);


        alert.setIcon(R.drawable.plus_green);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();


        save.setOnClickListener(v -> {
            if (!priority.getText().toString().isEmpty()) {
                menuItem.setTitle(title.getText().toString());
                menuItem.setPriority(Float.parseFloat(priority.getText().toString()));

                if (!top.getText().toString().isEmpty())
                    menuItem.setTop(top.getText().toString());

                String key = youMayAlsoLikeMenuRef.push().getKey();
                menuItem.setId(key);

                youMayAlsoLikeMenuRef.child(key).setValue(menuItem);

                dialog.dismiss();

            } else {
                Toast.makeText(YMALMenuActivity.this, "Data is empty", Toast.LENGTH_SHORT).show();
            }
        });

        priority.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(priority, InputMethodManager.SHOW_IMPLICIT);

    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<MenuItem, MenuItemViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<MenuItem, MenuItemViewHolder>(
                MenuItem.class, R.layout.ymal_menu_item_row, MenuItemViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final MenuItemViewHolder viewHolder, final MenuItem model, final int position) {

                if (model.getType().equals("more")) {
                    viewHolder.setDetails("More");
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
                                            deleteMenuItem(model.getId());
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
                else {


                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            editMenuItem(model);
                        }
                    });

                    viewHolder.setDetails(model.getTop(), model.getImg(), getApplicationContext(), model.getTitle(), model.getPriority());

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
                                            deleteMenuItem(model.getId());

                                            return true;
                                        case R.id.edit:
                                            editMenuItem(model);
//                                        Intent intent = new Intent(YMALMenuActivity.this,BookDetailsActivity.class);
//                                        intent.putExtra("mode","edit");
//                                        intent.putExtra("book",model);
//                                        intent.putExtra("course",course);
//                                        intent.putExtra("type",type);
//                                        startActivity(intent);
                                            //updateCourse(model);

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


            }
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    private void editMenuItem(final MenuItem menuItem) {

        AlertDialog.Builder alert = new AlertDialog.Builder(YMALMenuActivity.this);
        alert.setTitle("Edit menu Item");
        alert.setCancelable(false);

        LayoutInflater inflater = YMALMenuActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.menu_item_dialog, null);
        alert.setView(customDialog);

        final TextView title = customDialog.findViewById(R.id.title);
        final TextView priority = customDialog.findViewById(R.id.topic);
        final TextView top = customDialog.findViewById(R.id.top);

        if (menuItem.getTitle() != null)
            title.setText(menuItem.getTitle());

        if (menuItem.getPriority() != null)
            priority.setText(menuItem.getPriority().toString());

        if (menuItem.getTop() != null)
            top.setText(menuItem.getTop());

        Button save = customDialog.findViewById(R.id.save);
        save.setText("update");

        alert.setIcon(R.drawable.ic_action_edit_black);

        alert.setPositiveButton("DISMISS", (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = alert.create();
        dialog.show();

        save.setOnClickListener(v -> {
            if (!priority.getText().toString().isEmpty()) {
                menuItem.setTitle(title.getText().toString());
                menuItem.setPriority(Float.parseFloat(priority.getText().toString()));
                menuItem.setTop(top.getText().toString());

                youMayAlsoLikeMenuRef.child(menuItem.getId()).setValue(menuItem);

                dialog.dismiss();

            } else {
                Toast.makeText(YMALMenuActivity.this, "Data is empty", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteMenuItem(final String id) {

        DatabaseReference HomeMenuRef = FirebaseDatabase.getInstance().getReference("YouMayAlsoLikeMenu").child("SPPU");

        HomeMenuRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    Toast.makeText(YMALMenuActivity.this, "menu item deleted from database", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(YMALMenuActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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