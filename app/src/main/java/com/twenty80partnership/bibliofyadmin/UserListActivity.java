package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.twenty80partnership.bibliofyadmin.models.QueryData;
import com.twenty80partnership.bibliofyadmin.models.UserCardData;
import com.twenty80partnership.bibliofyadmin.viewHolders.UserCardViewHolder;

import java.util.ArrayList;

public class UserListActivity extends AppCompatActivity {

    RecyclerView itemList;
    private ArrayAdapter adapter;
    private DatabaseReference usersRef;
    private Query q,usersQuery;
    private String type,searchType="searchName";
    private Long startDate,endDate;
    private ValueEventListener createCustomDataListener;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        itemList = findViewById(R.id.recycler_view);

        itemList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        itemList.setLayoutManager(mLayoutManager);

        ArrayList<String> options = new ArrayList<>();

        options.add("SEARCH BY");
        options.add("User ID");
        options.add("Name");
        options.add("Email");
        options.add("Phone no.");

        final Spinner optionSpinner = findViewById(R.id.option_spinner);

        adapter = new ArrayAdapter(this, R.layout.spinner_item, options);

        optionSpinner.setAdapter(adapter);

        optionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String optionSelected = optionSpinner.getSelectedItem().toString();

                if (optionSelected.equals("User ID")){

                    searchType = "uId";
                }
                else if (optionSelected.equals("Email")){

                    searchType = "email";
                }
                else if (optionSelected.equals("Phone no.")){

                    searchType = "phone";
                }
                else if (optionSelected.equals("Name")){

                    searchType = "searchName";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Intent intent = getIntent();

        Log.d("userDebug","inside activity");

        usersRef =  FirebaseDatabase.getInstance().getReference("Users");

        final QueryData queryData = (QueryData)intent.getSerializableExtra("queryData");
        final int count = Integer.parseInt(intent.getStringExtra("count"));

        TextView countView = findViewById(R.id.count);
        countView.setText(count+" Users");

        final DatabaseReference customRef = FirebaseDatabase.getInstance().getReference("Custom");

        createCustomDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Log.d("userDebug","createcustomdata");

                customRef.setValue(dataSnapshot.getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            usersQuery = customRef.orderByChild(searchType);
                            Log.d("userDebug","after setting data firesearch query fired");

                            firebaseSearch(usersQuery);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("userDebug","access denied");

            }
        };

        if (queryData!=null){
            Log.d("userDebug","data is set");

            type = queryData.getType();
            startDate = queryData.getStartDate();
            endDate = queryData.getEndDate();

            q = usersRef.orderByChild(type).startAt(startDate).endAt(endDate);

            q.addListenerForSingleValueEvent(createCustomDataListener);
        }
        else {
            Log.d("userDebug","data is null");
            usersQuery = usersRef.orderByChild(searchType);

            firebaseSearch(usersQuery);

            //q = usersRef
        }

        MaterialSearchBar materialSearchBar = findViewById(R.id.search_bar);

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                Log.d("datasearch",enabled+" is enabled");
                if (!enabled){
                    if (queryData!=null){
                        usersQuery = customRef.orderByChild("name");
                    }
                    else {
                        usersQuery = usersRef.orderByChild("name");
                    }
                    firebaseSearch(usersQuery);

                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                Log.d("datasearch",text.toString());
                if (queryData!=null){
                    usersQuery = customRef.orderByChild(searchType).startAt(text.toString()).endAt(text.toString()+ "\uf8ff");
                }
                else {
                    usersQuery = usersRef.orderByChild(searchType).startAt(text.toString()).endAt(text.toString()+ "\uf8ff");
                }
                firebaseSearch(usersQuery);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                Log.d("datasearch",buttonCode+" is buttonCode");
            }
        });
    }

    public void firebaseSearch(Query q){

        Log.d("recycle debug","firebasesearch");

        final FirebaseRecyclerAdapter<UserCardData, UserCardViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<UserCardData, UserCardViewHolder>(
                UserCardData.class, R.layout.user_row, UserCardViewHolder.class, q
        ) {

            @Override
            protected void populateViewHolder(final UserCardViewHolder viewHolder, final UserCardData model, final int position) {

                viewHolder.setDetails(model.getPhoto(),getApplication(),model.getName(),model.getEmail(),model.getuId());
                viewHolder.userCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent detailIntent = new Intent(UserListActivity.this,UserDetailsActivity.class);
                        detailIntent.putExtra("uId",model.getuId());
                        startActivity(detailIntent);
                    }
                });

                viewHolder.userCard.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        //creating a popup menu
                        PopupMenu popup = new PopupMenu(getApplicationContext(), viewHolder.userCard);
                        //inflating menu from xml resource
                        popup.inflate(R.menu.user_menu);
                        //adding click listener
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.delete:

                                        AlertDialog.Builder builder1 = new AlertDialog.Builder(UserListActivity.this);
                                        builder1.setMessage("Do you really want to kick out this user?");
                                        builder1.setCancelable(true);

                                        builder1.setPositiveButton(
                                                "Yes Seriously!",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        deleteUser(model.getuId());
                                                    }
                                                });

                                        builder1.setNegativeButton(
                                                "No way",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.cancel();
                                                    }
                                                });

                                        AlertDialog alert = builder1.create();
                                        alert.show();

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


    private void deleteUser(final String uId){

        if (uId!=null &&  uId.equals("")){
            return;
        }
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("Addresses").child(uId).removeValue();
        ref.child("Cart").child(uId).removeValue();
        ref.child("CartReq").child(uId).removeValue();
        ref.child("CheckId").child(uId).removeValue();
        ref.child("CountData").child(uId).removeValue();
        ref.child("LoginInfo").child(uId).removeValue();
        ref.child("OrderPlaceFailed").child(uId).removeValue();
        ref.child("OrderReq").child(uId).removeValue();
        ref.child("Permissions").child(uId).removeValue();
        ref.child("PriceDetails").child(uId).removeValue();
        ref.child("RequestedBooks").child(uId).removeValue();
        ref.child("UpiConfirmation").child(uId).removeValue();
        ref.child("UserOrders").child(uId).removeValue();
        ref.child("Users").child(uId).removeValue();
        ref.child("Wishlist").child(uId).removeValue();
    }
}
