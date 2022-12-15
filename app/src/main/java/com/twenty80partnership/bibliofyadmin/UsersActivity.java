package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;
import com.twenty80partnership.bibliofyadmin.models.QueryData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class UsersActivity extends AppCompatActivity {

    ToggleSwitch toggleSwitch;
    EditText from,to;
    TextView userCount,todayCount,monthCount,weekCount,customCount;
    Button calculate;
    private int mYear,mMonth,mDay;
    DatabaseReference usersRef;
    ValueEventListener userCountListener, todayUserCountListener,customCountListener;
    Query qToday,qMonth,qWeek,qCustom;
    private ValueEventListener weekUserCountListener;
    private Long fromDate=0L,toDate=0L;
    private String type="registerDate";
    private long date,weekDate,iDate,monthDate;
    private boolean case0=true,case1=false,case2=false,set0,set1,set2;
    private ValueEventListener monthUserCountListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        toggleSwitch = findViewById(R.id.toggle_switch);
        from = findViewById(R.id.from);
        to = findViewById(R.id.to);
        userCount = findViewById(R.id.user_count);
        todayCount = findViewById(R.id.today_count);
        monthCount = findViewById(R.id.month_count);
        weekCount = findViewById(R.id.week_count);
        customCount = findViewById(R.id.custom_count);
        calculate = findViewById(R.id.calculate);
        final CardView allUsers = findViewById(R.id.all_users);
        CardView today = findViewById(R.id.today);
        CardView thisMonth = findViewById(R.id.this_month);
        final CardView thisWeek = findViewById(R.id.this_week);


        ArrayList<Long> months = new ArrayList<>();


        months.add(31L);
        months.add(29L);
        months.add(31L);
        months.add(30L);
        months.add(31L);
        months.add(30L);
        months.add(31L);
        months.add(31L);
        months.add(30L);
        months.add(31L);
        months.add(30L);
        months.add(31L);


        userCountListener= new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            Toast.makeText(UsersActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
            }
        };

        usersRef.addValueEventListener(userCountListener);

        //taking system time
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date currentTime= Calendar.getInstance().getTime();
        date=Long.parseLong(dateFormat.format(currentTime));


        //absolute date
        date = (date/1000000000);


        //next day date
        iDate = date + 1;
        Log.d("dateabs",date+" "+iDate);





        //for 1 week before date
        Long tDate = date;
        Long day =  (tDate)%100;
        tDate = tDate/100;

        Long month = (tDate)%100;
        tDate = tDate/100;

        Long year = tDate;

        Log.d("day",day+" " +month+" "+year);

        if (day>7){
            day = day-7;
        }
        else if (day == 7){
            day =1L;
        }
        else {
            if (month>1){
                month = month - 1;
                day = months.get(month.intValue()-1) - (6 - day);
            }
            else {
                year = year - 1;
                month = 12L;
                day = 31 - (6- day);
            }

        }
        Log.d("day",day+" " +month+" "+year);

        weekDate = createAbsoluteDate(year,month,day);



        //for 1 month before date
        tDate = date;
        tDate = tDate/100;

        month = (tDate)%100;
        tDate = tDate/100;

        year = tDate;
        day = 1L;
        monthDate = createAbsoluteDate(year,month,day);

        todayUserCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                todayCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                Log.d("todayusers",dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        weekUserCountListener =  new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                weekCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                Log.d("weekusers",dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        monthUserCountListener =  new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                monthCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                Log.d("weekusers",dataSnapshot.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        customCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                customCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        Log.d("day",weekDate+" ");

        //default
        toggleSwitch.setCheckedPosition(0);
        case0 = true;
        type = "registerDate";
        toggleSwitch.setCheckedPosition(0);
        qToday = usersRef.orderByChild(type).startAt(date*1000000000).endAt(iDate*1000000000);
        qWeek = usersRef.orderByChild(type).startAt(weekDate*1000000000).endAt(iDate*1000000000);
        qMonth = usersRef.orderByChild(type).startAt(monthDate*1000000000).endAt(iDate*1000000000);

        qToday.addValueEventListener(todayUserCountListener);
        qWeek.addValueEventListener(weekUserCountListener);
        qMonth.addValueEventListener(monthUserCountListener);

        toggleSwitch.setOnChangeListener(new ToggleSwitch.OnChangeListener() {

            @Override
            public void onToggleSwitchChanged(int i) {
                switch (i){
                    case 0:
                        case0 = true;
                        type = "registerDate";
                        toggleSwitch.setCheckedPosition(0);
                        qToday = usersRef.orderByChild(type).startAt(date*1000000000).endAt(iDate*1000000000);
                        qWeek = usersRef.orderByChild(type).startAt(weekDate*1000000000).endAt(iDate*1000000000);
                        qMonth = usersRef.orderByChild(type).startAt(monthDate*1000000000).endAt(iDate*1000000000);

                        qToday.addValueEventListener(todayUserCountListener);
                        qWeek.addValueEventListener(weekUserCountListener);
                        qMonth.addValueEventListener(monthUserCountListener);
                        break;
                    case 1:
                        case1 = true;
                        type = "lastLogin";
                        toggleSwitch.setCheckedPosition(1);
                        qToday = usersRef.orderByChild(type).startAt(date*1000000000).endAt(iDate*1000000000);
                        qWeek = usersRef.orderByChild(type).startAt(weekDate*1000000000).endAt(iDate*1000000000);
                        qMonth = usersRef.orderByChild(type).startAt(monthDate*1000000000).endAt(iDate*1000000000);

                        qToday.addValueEventListener(todayUserCountListener);
                        qWeek.addValueEventListener(weekUserCountListener);
                        qMonth.addValueEventListener(monthUserCountListener);
                        break;
                    case 2:
                        case2 = true;
                        type = "lastOpened";
                        toggleSwitch.setCheckedPosition(2);
                        qToday = usersRef.orderByChild(type).startAt(date*1000000000).endAt(iDate*1000000000);
                        qWeek = usersRef.orderByChild(type).startAt(weekDate*1000000000).endAt(iDate*1000000000);
                        qMonth = usersRef.orderByChild(type).startAt(monthDate*1000000000).endAt(iDate*1000000000);

                        qToday.addValueEventListener(todayUserCountListener);
                        qWeek.addValueEventListener(weekUserCountListener);
                        qMonth.addValueEventListener(monthUserCountListener);
                        break;

                }

                calculate.callOnClick();
            }
        });


//
//        qToday = usersRef.orderByChild(type).startAt(date*1000000000).endAt(iDate*1000000000);
//        qWeek = usersRef.orderByChild(type).startAt(weekDate*1000000000).endAt(iDate*1000000000);
//        qMonth = usersRef.orderByChild(type).startAt(date-7000000000L).endAt(iDate);









        from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate(from); }
        });

        to.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate(to);

            }
        });

        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("custom","from "+fromDate+" to "+toDate);

                if (type.equals("registerDate"))
                set0 = true;
                if (type.equals("lastLogin"))
                    set1 = true;
                if (type.equals("lastOpened"))
                    set2 = true;

                qCustom = usersRef.orderByChild(type).startAt(fromDate*1000000000).endAt(toDate*1000000000);
                qCustom.addValueEventListener(customCountListener);
            }
        });




        allUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userlistIntent =  new Intent(UsersActivity.this,UserListActivity.class);
                userlistIntent.putExtra("count",userCount.getText().toString());
                startActivity(userlistIntent);

            }
        });

        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userlistIntent =  new Intent(UsersActivity.this,UserListActivity.class);

                QueryData queryData= new QueryData(type,date*1000000000,iDate*1000000000);
                userlistIntent.putExtra("count",todayCount.getText().toString());
                userlistIntent.putExtra("queryData",queryData);
                startActivity(userlistIntent);
            }
        });

        thisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userlistIntent =  new Intent(UsersActivity.this,UserListActivity.class);

                QueryData queryData= new QueryData(type,weekDate*1000000000,iDate*1000000000);
                userlistIntent.putExtra("count",weekCount.getText().toString());
                userlistIntent.putExtra("queryData",queryData);
                Log.d("userDebug","querydata is"+queryData.getType());

                startActivity(userlistIntent);
            }
        });

        thisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userlistIntent =  new Intent(UsersActivity.this,UserListActivity.class);

                QueryData queryData= new QueryData(type,monthDate*1000000000,iDate*1000000000);
                userlistIntent.putExtra("count",monthCount.getText().toString());
                userlistIntent.putExtra("queryData",queryData);
                startActivity(userlistIntent);
            }
        });

       customCount.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Intent userlistIntent =  new Intent(UsersActivity.this,UserListActivity.class);

               QueryData queryData= new QueryData(type,fromDate*1000000000,toDate*1000000000);
               userlistIntent.putExtra("count",customCount.getText().toString());
               userlistIntent.putExtra("queryData",queryData);
               startActivity(userlistIntent);
           }
       });
    }




    private Long createAbsoluteDate(Long year, Long month, Long day) {
        year = year*10000;
        month = month*100;

        return year+month+day;
    }

    public void pickDate(final EditText editText){
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        editText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);


                        if (editText==findViewById(R.id.to)) {
                            toDate = createAbsoluteDate(Long.valueOf(year), Long.valueOf(monthOfYear + 1), Long.valueOf(dayOfMonth));
                        }
                        else if (editText==findViewById(R.id.from)){
                            fromDate = createAbsoluteDate(Long.valueOf(year), Long.valueOf(monthOfYear + 1), Long.valueOf(dayOfMonth));
                        }
                       // Log.d("custom","result "+result);

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
      //  Log.d("custom","result before return"+result);


    }

    @Override
    public void onBackPressed() {

        usersRef.removeEventListener(userCountListener);

        if (case0){
            type = "registerDate";
            qToday = usersRef.orderByChild(type).startAt(date*1000000000).endAt(iDate*1000000000);
            qWeek = usersRef.orderByChild(type).startAt(weekDate*1000000000).endAt(iDate*1000000000);
            qCustom = usersRef.orderByChild(type).startAt(fromDate*1000000000).endAt(toDate*1000000000);
            qMonth = usersRef.orderByChild(type).startAt(monthDate*1000000000).endAt(iDate*1000000000);

            qToday.removeEventListener(todayUserCountListener);
            qWeek.removeEventListener(weekUserCountListener);
            qMonth.removeEventListener(monthUserCountListener);

            if (set0) qCustom.removeEventListener(customCountListener);
        }

        if (case1){
            type = "lastLogin";
            qToday = usersRef.orderByChild(type).startAt(date*1000000000).endAt(iDate*1000000000);
            qWeek = usersRef.orderByChild(type).startAt(weekDate*1000000000).endAt(iDate*1000000000);
            qCustom = usersRef.orderByChild(type).startAt(fromDate*1000000000).endAt(toDate*1000000000);
            qMonth = usersRef.orderByChild(type).startAt(monthDate*1000000000).endAt(iDate*1000000000);

            qToday.removeEventListener(todayUserCountListener);
            qWeek.removeEventListener(weekUserCountListener);
            qMonth.removeEventListener(monthUserCountListener);

            if (set1) qCustom.removeEventListener(customCountListener);
        }

        if (case2){
            type = "lastOpened";
            qToday = usersRef.orderByChild(type).startAt(date*1000000000).endAt(iDate*1000000000);
            qWeek = usersRef.orderByChild(type).startAt(weekDate*1000000000).endAt(iDate*1000000000);
            qCustom = usersRef.orderByChild(type).startAt(fromDate*1000000000).endAt(toDate*1000000000);
            qMonth = usersRef.orderByChild(type).startAt(monthDate*1000000000).endAt(iDate*1000000000);

            qToday.removeEventListener(todayUserCountListener);
            qWeek.removeEventListener(weekUserCountListener);
            qMonth.removeEventListener(monthUserCountListener);

            if (set2) qCustom.removeEventListener(customCountListener);
        }

//        usersRef.removeEventListener(userCountListener);
//        qToday.removeEventListener(todayUserCountListener);
//        qWeek.removeEventListener(weekUserCountListener);
//        if (set)
//        qCustom.removeEventListener(customCountListener);
        super.onBackPressed();
    }
}
