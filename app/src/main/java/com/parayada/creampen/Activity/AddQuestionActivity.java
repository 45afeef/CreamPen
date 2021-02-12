package com.parayada.creampen.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.R;

public class AddQuestionActivity extends AppCompatActivity {


    private static final int RC_CHOOSE_TOPIC = 100;

    private Context mContext;

    private TextView tvTopics;
    private RadioButton checkedButton;

    private String topics = "";

    private String syllabus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();
        mContext = this;

        syllabus = getIntent().getStringExtra("syllabus");


        final EditText questionView = findViewById(R.id.tv_question);
        final EditText option1View = findViewById(R.id.et_A);
        final EditText option2View = findViewById(R.id.et_B);
        final EditText option3View = findViewById(R.id.et_C);
        final EditText option4View = findViewById(R.id.et_D);
        tvTopics = findViewById(R.id.tv_topics);


        RadioGroup rgOptions = (RadioGroup) findViewById(R.id.rg_options);
        Button btnSave = findViewById(R.id.btn_saveQuestion);

        rgOptions.setOnCheckedChangeListener((group, checkedId) -> {
            checkedButton = group.findViewById(checkedId);
        });

        btnSave.setOnClickListener(v -> {

            String question = questionView.getText().toString().trim();
            String optA = option1View.getText().toString().trim();
            String optB = option2View.getText().toString().trim();
            String optC = option3View.getText().toString().trim();
            String optD = option4View.getText().toString().trim();

            if (question.isEmpty()){
                Toast.makeText(mContext,"Please Type your question",Toast.LENGTH_SHORT).show();
                questionView.requestFocus();
            }else if (optA.isEmpty()){
                Toast.makeText(mContext,"Option A is Missing",Toast.LENGTH_SHORT).show();
                option1View.requestFocus();
            }else if (optB.isEmpty()){
                Toast.makeText(mContext,"Option B is Missing",Toast.LENGTH_SHORT).show();
                option2View.requestFocus();
            }else if (optC.isEmpty()){
                Toast.makeText(mContext,"Option C is Missing",Toast.LENGTH_SHORT).show();
                option3View.requestFocus();
            }else if (optD.isEmpty()){
                Toast.makeText(mContext,"Option D is Missing",Toast.LENGTH_SHORT).show();
                option4View.requestFocus();
            }else if (checkedButton == null){
                Toast.makeText(mContext,"Choose a option as answer",Toast.LENGTH_LONG).show();
            }else if (topics.isEmpty()){
                Toast.makeText(mContext,"Please select suitable topics for this question",Toast.LENGTH_LONG).show();
                selectTopics();
            }else {
                String  answer = "";
                switch (checkedButton.getText().toString().trim()) {
                    case "A":
                        answer = optA;
                        break;
                    case "B":
                        answer = optB;
                        break;
                    case "C":
                        answer = optC;
                        break;
                    case "D":
                        answer = optD;
                        break;
                }
                McqSet mcqSet = new McqSet();

                mcqSet.setQuestion(question);
                mcqSet.setAnswer(answer);
                mcqSet.setOption1(optA);
                mcqSet.setOption2(optB);
                mcqSet.setOption3(optC);
                mcqSet.setOption4(optD);
                mcqSet.setTopics(topics);

                Intent data = new Intent();
                data.putExtra("newQuestionSet", mcqSet);
                data.putExtra("syllabus",syllabus);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });

        tvTopics.setOnClickListener(v -> selectTopics());
    }

    public void selectTopics() {
        Intent chooseTopicIntent = new Intent(this,SyllabusActivity.class);
        chooseTopicIntent.putExtra("syllabus",syllabus);

        startActivityForResult(chooseTopicIntent,RC_CHOOSE_TOPIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CHOOSE_TOPIC) {

            // Update syllabus if changed
            if (data != null && data.hasExtra("syllabus")) {
                String newSyllabus = data.getStringExtra("syllabus");
                if (!newSyllabus.equals(syllabus)) {
                    FirebaseFirestore.getInstance()
                            .document("Courses/" + getIntent().getStringExtra("courseId"))
                            .update("syllabus", newSyllabus);
                    syllabus = newSyllabus;
                }
            }
            if (resultCode == RESULT_OK) {

                topics = data.getStringArrayListExtra("chosenTopics").toString();
                topics = topics.substring(1,topics.length()-1);
                tvTopics.setText("");
                for (String s:topics.split(","))
                    tvTopics.append(s+"\n");
            }
        }
    }
}