package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

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

public class HomeCoursesActivity extends AppCompatActivity {
    String course,type,typeName;
    RecyclerView itemList;
    EditText id;
    private FloatingActionButton mFloatingActionButton;
    MaterialSearchBar materialSearchBar;
    private Query query;
    private DatabaseReference HomeMenuRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_courses);



        if (! Common.isConnectedToInternet(getApplicationContext())) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(HomeCoursesActivity.this);
            alert.setTitle("No internet Connection");
            alert.setCancelable(false);
            alert.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (! Common.isConnectedToInternet(getApplicationContext())){
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

        HomeMenuRef = FirebaseDatabase.getInstance().getReference("HomeCoursesList")
                .child("SPPU");



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitle("Home-Menu");

        itemList = findViewById(R.id.recycler_view);
        mFloatingActionButton = findViewById(R.id.add_new_course);
        materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled){
                    query = HomeMenuRef.orderByChild("searchName");
                    firebaseSearch(query);
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                query = HomeMenuRef.orderByChild("searchName").startAt(text.toString()).endAt(text.toString()+"\uf8ff");
                firebaseSearch(query);
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        itemList.setLayoutManager(new GridLayoutManager(this,4));
        itemList.setHasFixedSize(true);



        Query query = HomeMenuRef.orderByChild("priority");

        firebaseSearch(query);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                AlertDialog.Builder alert = new AlertDialog.Builder(HomeCoursesActivity.this);
                alert.setTitle("Add New Course");
                alert.setCancelable(false);

                LayoutInflater inflater = HomeCoursesActivity.this.getLayoutInflater();

                View customDialog = inflater.inflate(R.layout.add_course_dialog,null);
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
                        Intent selectIntent = new Intent(HomeCoursesActivity.this, CoursesListingActivity.class);
                        selectIntent.putExtra("mode","menuItem");
                        startActivityForResult(selectIntent,1);
                    }
                });

                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        Intent selectIntent = new Intent(HomeCoursesActivity.this,StationaryActivity.class);
                        selectIntent.putExtra("mode","menuItem");
                        startActivityForResult(selectIntent,1);
                    }
                });

            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK){
                    if (data.hasExtra("menuItem")){
                        MenuItem menuItem = (MenuItem) data.getSerializableExtra("menuItem");
                        addAndUpdate(menuItem);
                    }
                }
                break;
        }
    }

    private void addAndUpdate(final MenuItem menuItem) {

        AlertDialog.Builder alert = new AlertDialog.Builder(HomeCoursesActivity.this);
        alert.setTitle("Add New menuItem");
        alert.setCancelable(false);

        LayoutInflater inflater = HomeCoursesActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.menu_item_dialog,null);
        alert.setView(customDialog);

        final TextView title = customDialog.findViewById(R.id.title);
        final TextView priority = customDialog.findViewById(R.id.topic);
        final TextView top = customDialog.findViewById(R.id.top);
        final TextView currentSem = customDialog.findViewById(R.id.current_sem);

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

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!title.getText().toString().isEmpty() && !priority.getText().toString().isEmpty()){
                    menuItem.setTitle(title.getText().toString());
                    menuItem.setPriority(Float.parseFloat(priority.getText().toString()));

                    if (!top.getText().toString().isEmpty())
                        menuItem.setTop(top.getText().toString());

                    String key = HomeMenuRef.push().getKey();
                    menuItem.setId(key);

                    HomeMenuRef.child(key).setValue(menuItem);

                    dialog.dismiss();

                }
                else{
                    Toast.makeText(HomeCoursesActivity.this,"Data is empty",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void firebaseSearch(Query q) {

        Log.d("recycle debug", "firebasesearch");

        FirebaseRecyclerAdapter<MenuItem, MenuItemViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<MenuItem, MenuItemViewHolder>(
                MenuItem.class, R.layout.menu_item_row, MenuItemViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final MenuItemViewHolder viewHolder, final MenuItem model, final int position) {


                viewHolder.setDetails(model.getTop(),model.getImg(),getApplicationContext(),model.getTitle(),model.getPriority());

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
                                        //todo
//                                        Intent intent = new Intent(HomeCoursesActivity.this,BookDetailsActivity.class);
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
        };


        itemList.setAdapter(firebaseRecyclerAdapter);
    }

    private void editMenuItem(final MenuItem menuItem) {

        AlertDialog.Builder alert = new AlertDialog.Builder(HomeCoursesActivity.this);
        alert.setTitle("Edit menuItem");
        alert.setCancelable(false);

        LayoutInflater inflater = HomeCoursesActivity.this.getLayoutInflater();

        View customDialog = inflater.inflate(R.layout.menu_item_dialog,null);
        alert.setView(customDialog);

        final TextView title = customDialog.findViewById(R.id.title);
        final TextView priority = customDialog.findViewById(R.id.topic);
        final TextView top = customDialog.findViewById(R.id.top);
        final TextView currentSem = customDialog.findViewById(R.id.current_sem);

        title.setText(menuItem.getTitle());
        priority.setText(menuItem.getPriority().toString());
        top.setText(menuItem.getTop());

        Button save = customDialog.findViewById(R.id.save);
        save.setText("update");

        alert.setIcon(R.drawable.ic_action_edit_black);

        alert.setPositiveButton("DISMISS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });

        final AlertDialog dialog = alert.create();
        dialog.show();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!title.getText().toString().isEmpty() && !priority.getText().toString().isEmpty()){
                    menuItem.setTitle(title.getText().toString());
                    menuItem.setPriority(Float.parseFloat(priority.getText().toString()));

                    menuItem.setTop(top.getText().toString());

                    HomeMenuRef.child(menuItem.getId()).setValue(menuItem);

                    dialog.dismiss();

                }
                else{
                    Toast.makeText(HomeCoursesActivity.this,"Data is empty",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deleteMenuItem(final String id) {

        DatabaseReference HomeMenuRef = FirebaseDatabase.getInstance().getReference("HomeCoursesList").child("SPPU");

        HomeMenuRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    Toast.makeText(HomeCoursesActivity.this,"menu item deleted from database",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(HomeCoursesActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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