package com.parayada.creampen.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.parayada.creampen.Adapter.TopicAdapter;
import com.parayada.creampen.Model.Course;
import com.parayada.creampen.Model.Topic;
import com.parayada.creampen.R;

import java.util.ArrayList;
import java.util.Arrays;


public class CreateCourseActivity extends AppCompatActivity implements TopicAdapter.clickHandler{

    Context mContext;
    ArrayList<Topic> syllabus = new ArrayList<>();
    private String syllabusString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_course);
        mContext = this;

        syllabusString = getIntent().getStringExtra("syllabus");
        if (syllabusString != null){
            syllabus = new Topic().stringToList(syllabusString);
        }

        final EditText etName = findViewById(R.id.et_course_name);
        final EditText etDescription = findViewById(R.id.et_course_description);
        Button buttonSave = findViewById(R.id.btn_save);
        RecyclerView recyclerView = findViewById(R.id.rv_syllabus);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setAdapter(new TopicAdapter(syllabus,mContext));

        buttonSave.setOnClickListener(v -> {
            recyclerView.requestFocus();

            String title = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();

            if (title.isEmpty() || title.length() < 2) {
                Toast.makeText(mContext,"Please enter a valid course name", Toast.LENGTH_LONG).show();
                etName.requestFocus();
            }
            else if (description.isEmpty() || description.length() < 20) {
                Toast.makeText(mContext, "Description should have 20 character", Toast.LENGTH_LONG).show();
                etDescription.requestFocus();
            }
            else if (syllabusString.isEmpty()) {
                Toast.makeText(mContext, "Please add one Subject", Toast.LENGTH_SHORT).show();
            }
            else if (!syllabusString.contains("}}")) {
                Toast.makeText(mContext, "Please add one Main Topic under subject", Toast.LENGTH_SHORT).show();
            }
            else if (new Topic().getTopicLevel(syllabusString) < 3) {
                Toast.makeText(mContext, "Please add one Sub topic under Main topic", Toast.LENGTH_SHORT).show();
            }
            else {

                FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                Course course = new Course();
                course.setTitle(title);
                course.setDescription(description);
                course.addEducatorId(mUser.getUid());
                course.addEducatorName(mUser.getDisplayName());
                course.setSyllabus(syllabusString);

                Intent data = new Intent();
                data.putExtra("course", course);
                setResult(Activity.RESULT_OK, data);
                finish();

            }

        });
    }


    /*@Override
    public void updatedSyllabus(ArrayList<Topic> syllabus, ArrayList<String> chosenTopics) {
        this.syllabusString = new Topic().topicListToString(syllabus);

    }*/

    @Override
    public void onSyllabusUpdate(ArrayList<Topic> updatedSyllabus) {
        this.syllabusString = new Topic().topicListToString(updatedSyllabus);
    }

    @Override
    public void onChoosingTopics(ArrayList<String> chosenTopics) {

    }
}
