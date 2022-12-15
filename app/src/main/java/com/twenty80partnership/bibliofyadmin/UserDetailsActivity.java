package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.twenty80partnership.bibliofyadmin.models.User;

public class UserDetailsActivity extends AppCompatActivity {

    EditText name,uId,phone,registerDate,lastLogin,lastOpened,totalOrders,college,course;
    ImageView nameEdit,phoneEdit,registerDateEdit,lastLoginEdit,lastOpenedEdit,totalOrdersEdit,collegeEdit,courseEdit;
    TextView phoneStatus;
    ImageView img;
    Switch permissionSwitch;
    ProgressDialog pd;
    TextView orders,cart,wishlist;
    boolean nameB,phoneB,registerDateB,lastLoginB,lastOpenedB,totalOrdersB,collegeB,courseB;
    private String sUId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);



        name = findViewById(R.id.name);
        uId = findViewById(R.id.uid);
        phone = findViewById(R.id.phone);
        registerDate = findViewById(R.id.register_date);
        lastLogin = findViewById(R.id.last_login);
        lastOpened = findViewById(R.id.last_opened);
        totalOrders = findViewById(R.id.total_orders);
        college = findViewById(R.id.college);
        course = findViewById(R.id.course);
        phoneStatus = findViewById(R.id.phone_status);
        img = findViewById(R.id.img);
        permissionSwitch = findViewById(R.id.switch1);

        nameEdit = findViewById(R.id.name_edit);
        phoneEdit = findViewById(R.id.number_edit);
        registerDateEdit = findViewById(R.id.register_date_edit);
        lastLoginEdit = findViewById(R.id.last_login_edit);
        lastOpenedEdit = findViewById(R.id.last_opened_edit);
        totalOrdersEdit = findViewById(R.id.total_orders_edit);
        collegeEdit = findViewById(R.id.college_edit);
        courseEdit = findViewById(R.id.course_edit);

        orders = findViewById(R.id.orders);
        cart = findViewById(R.id.cart);
        wishlist = findViewById(R.id.wishlist);


        pd = new ProgressDialog(UserDetailsActivity.this);
        pd.setTitle("Saving");
        pd.setCancelable(false);


        orders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailsActivity.this,UserOrdersActivity.class);
                intent.putExtra("uId",sUId);
                startActivity(intent);
            }
        });

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailsActivity.this,UserCartActivity.class);
                intent.putExtra("uId",sUId);
                startActivity(intent);
            }
        });


        permissionSwitch.setClickable(false);



        Intent intent = getIntent();
        sUId = intent.getStringExtra("uId");


        nameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!nameB){
                    nameEdit.setImageDrawable(ContextCompat.getDrawable(UserDetailsActivity.this, R.drawable.save_action));
                    name.setFocusableInTouchMode(true);
                    name.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);
                    nameB = true;
                }
                else {
                    // Check if no view has focus:


                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(name.getWindowToken(), 0);

                    pd.show();
                    DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference("Users").child(sUId).child("name");
                    nameRef.setValue(name.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            nameEdit.setImageDrawable(ContextCompat.getDrawable(UserDetailsActivity.this, R.drawable.edit_action));
                            name.setFocusable(false);
                            nameB = false;
                        }
                    });

                }


            }
        });

        phoneEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!phoneB){
                    phoneEdit.setImageDrawable(ContextCompat.getDrawable(UserDetailsActivity.this, R.drawable.save_action));
                    phone.setFocusableInTouchMode(true);
                    phone.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(phone, InputMethodManager.SHOW_IMPLICIT);
                    phoneB = true;
                }
                else {
                    // Check if no view has focus:


                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(phone.getWindowToken(), 0);

                    pd.show();
                    DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference("Users").child(sUId).child("phone");
                    nameRef.setValue(phone.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            phoneEdit.setImageDrawable(ContextCompat.getDrawable(UserDetailsActivity.this, R.drawable.edit_action));
                            phone.setFocusable(false);
                            phoneB = false;
                        }
                    });

                }


            }
        });

        totalOrdersEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!totalOrdersB){
                    totalOrdersEdit.setImageDrawable(ContextCompat.getDrawable(UserDetailsActivity.this, R.drawable.save_action));
                    totalOrders.setFocusableInTouchMode(true);
                    totalOrders.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(totalOrders, InputMethodManager.SHOW_IMPLICIT);
                    totalOrdersB = true;
                }
                else {
                    // Check if no view has focus:


                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(totalOrders.getWindowToken(), 0);

                    pd.show();
                    DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference("Users").child(sUId).child("orders");
                    nameRef.setValue(Integer.parseInt(totalOrders.getText().toString())).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            totalOrdersEdit.setImageDrawable(ContextCompat.getDrawable(UserDetailsActivity.this, R.drawable.edit_action));
                            totalOrders.setFocusable(false);
                            totalOrdersB = false;
                        }
                    });

                }


            }
        });

        collegeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!collegeB){
                    collegeEdit.setImageDrawable(ContextCompat.getDrawable(UserDetailsActivity.this, R.drawable.save_action));
                    college.setFocusableInTouchMode(true);
                    college.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(college, InputMethodManager.SHOW_IMPLICIT);
                    collegeB = true;
                }
                else {
                    // Check if no view has focus:


                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(college.getWindowToken(), 0);

                    pd.show();
                    DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference("Users").child(sUId).child("college");
                    nameRef.setValue(college.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            pd.dismiss();
                            collegeEdit.setImageDrawable(ContextCompat.getDrawable(UserDetailsActivity.this, R.drawable.edit_action));
                            college.setFocusable(false);
                            collegeB = false;
                        }
                    });

                }


            }
        });

        courseEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserDetailsActivity.this, CourseDefinitionsActivity.class);
                intent.putExtra("uId",sUId);
             startActivityForResult(intent,0);

            }
        });

        if (sUId!=null){
            final DatabaseReference permissionRef = FirebaseDatabase.getInstance().getReference("Permissions").child(sUId);

            permissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    permissionSwitch.setClickable(true);

                    if (dataSnapshot.exists())
                        permissionSwitch.setChecked(dataSnapshot.getValue(Boolean.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });



            permissionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    permissionRef.setValue(isChecked);
                }
            });
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(sUId);

            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);

                    if (user!=null){
                        Log.d("userdetails","done");


                        if (user.getName()!=null && !user.getName().equals("")){
                            name.setText(user.getName());
                        }

                        if (user.getPhoto()!=null && !user.getPhoto().equals("")){
                            Picasso.get().load(user.getPhoto()).into(img);
                        }

                        if (user.getuId()!=null && !user.getuId().equals("")){
                            uId.setText(user.getuId());
                        }

                        if (user.getPhone()!=null && !user.getPhone().equals("")){
                            phone.setText(user.getPhone());
                        }

                        if (user.getRegisterDate()!=null && user.getRegisterDate()!=0L){
                            Long tDate = user.getRegisterDate();
                            tDate = tDate/1000000000;
                            Long day =  (tDate)%100;
                            tDate = tDate/100;

                            Long month = (tDate)%100;
                            tDate = tDate/100;

                            Long year = tDate;
                            registerDate.setText(day+"-"+month+"-"+year);
                        }

                        if (user.getLastLogin()!=null && user.getLastLogin()!=0L){
                            Long tDate = user.getLastLogin();
                            tDate = tDate/1000000000;
                            Long day =  (tDate)%100;
                            tDate = tDate/100;

                            Long month = (tDate)%100;
                            tDate = tDate/100;

                            Long year = tDate;
                            lastLogin.setText(day+"-"+month+"-"+year);
                        }

                        if (user.getLastOpened()!=null && user.getLastOpened()!=0L){
                            Long tDate = user.getLastOpened();
                            tDate = tDate/1000000000;
                            Long day =  (tDate)%100;
                            tDate = tDate/100;

                            Long month = (tDate)%100;
                            tDate = tDate/100;

                            Long year = tDate;
                            lastOpened.setText(day+"-"+month+"-"+year);
                        }

                        if (user.getOrders()!=null){
                            totalOrders.setText(user.getOrders()+"");
                        }

                        if (user.getCollege()!=null && !user.getCollege().equals("")){
                            college.setText(user.getCollege());
                        }


                        if (user.getCourse()!=null && !user.getCourse().equals("")){
                            course.setText(user.getCourse());
                        }

                        if(user.getIsPhoneVerified()){
                            phoneStatus.setText("verified");
                        }
                    }



                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==0){
            if (resultCode==RESULT_OK){
                if (data.getStringExtra("course")!=null)
                course.setText(data.getStringExtra("course"));
            }
        }
    }
}
