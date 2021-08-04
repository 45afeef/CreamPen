package com.parayada.creampen.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.parayada.creampen.Model.Lesson;
import com.parayada.creampen.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddEducatorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_educator);

        EditText etPhoneNumber = findViewById(R.id.et_search_phone_number);
        ImageButton searchBtn = findViewById(R.id.ib_search_phone_number);

        searchBtn.setOnClickListener(view -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();

            if (phoneNumber == null || phoneNumber.length() < 12){
                Toast toast = Toast.makeText(this, "Oh! Sorry, Please enter the phone number with country code", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }else{
                if('+' == phoneNumber.charAt(0)) {
                    FirebaseFirestore mDb = FirebaseFirestore.getInstance();

                    mDb.collection("Users")
                            .whereEqualTo("phone", phoneNumber)
                            .get()
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful() && !task.getResult().getDocuments().isEmpty()){
                                    String id = task.getResult().getDocuments().get(0).getId();
                                    String name = task.getResult().getDocuments().get(0).getString("name");

                                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                                    b.setTitle("Confirm new Educator?");
                                    b.setMessage("Are you sure to add " + name + " as a new educator for this course. " +
                                            name +" will be able to create lessons and quizzes in this course. "+
                                            name +" will also be able to edit existing quizzes in this course.");
                                    b.setNegativeButton("No",null);
                                    b.setPositiveButton("Add " + name, (dialogInterface, i) -> {

                                        Map<String,Object> educatorInfo = new HashMap<>();
                                        educatorInfo.put("educatorIds", FieldValue.arrayUnion(id));
                                        educatorInfo.put("educatorNames", FieldValue.arrayUnion(name));

                                        mDb.collection("Courses").document(getIntent().getStringExtra("courseId"))
                                                .update(educatorInfo)
                                                .addOnSuccessListener(ts -> {
                                                   Toast.makeText(this, "Added " + name + " as an educator", Toast.LENGTH_SHORT).show();
                                                   finish();
                                                })
                                                .addOnFailureListener(tf->finish());
                                    });
                                    b.show();
                                }else{
                                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                                    b.setTitle("Sorry No record found!");
                                    b.setMessage("Please double check the phone number. Any how we can't find a user registered with the phone number "+phoneNumber);
                                    b.setPositiveButton("OK",null);
                                    b.show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.d("onCompleteFailure", String.valueOf(e));
                            });
                }else {
                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                    b.setTitle("No \"+country code\"");
                    b.setMessage("Make sure you have entered proper country code preceded by \"+\" and succeeds with 10 digit phone number \nExample : +919876543210");
                    b.show();
                }
            }
        });
    }
}