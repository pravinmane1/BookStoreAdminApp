package com.twenty80partnership.bibliofyadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twenty80partnership.bibliofyadmin.models.ApplicableTerm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataAcessTestActivity extends AppCompatActivity {

    FirebaseDatabase db;
    TextView tvTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_acess_test);

        db = FirebaseDatabase.getInstance();
        tvTest = findViewById(R.id.tv_test);

       DatabaseReference ref = db.getReference();
        Map<String,Object> map = new HashMap<>();

        Map<String,Object> applicableTermsMap = new HashMap<>();
        ApplicableTerm applicableTerm = new ApplicableTerm();

        //branch
        applicableTerm.setPriority(1);
        applicableTerm.setTermId("branch");

        applicableTermsMap.put("branch",applicableTerm);

        //year
        applicableTerm.setPriority(2);
        applicableTerm.setTermId("year");

        applicableTermsMap.put("year",applicableTerm);

        //sem
        applicableTerm.setPriority(3);
        applicableTerm.setTermId("sem");

        applicableTermsMap.put("sem",applicableTerm);

        map.put("applicableTerms",applicableTermsMap);

       ref.child("trial").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {
               if (task.isSuccessful()){
                   Toast.makeText(DataAcessTestActivity.this, "success", Toast.LENGTH_SHORT).show();
               }
               else {
                   Toast.makeText(DataAcessTestActivity.this, "fail", Toast.LENGTH_SHORT).show();

               }
           }
       });
    }
}