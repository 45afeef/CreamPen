package com.parayada.creampen.Activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parayada.creampen.Adapter.ExamAdapter;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.Model.QuestionPaper;
import com.parayada.creampen.R;

import java.util.ArrayList;

public class CreateQpActivity extends AppCompatActivity {

    private static final int RC_NEW_MCQ = 100;
    private ExamAdapter questionsAdapter = new ExamAdapter();

    private String syllabusString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qp);

        String courseId = getIntent().getStringExtra("courseId");
        syllabusString = getIntent().getStringExtra("syllabus");

        // Initialize views
        TextView saveQuiz = findViewById(R.id.save_quiz);
        EditText etName = findViewById(R.id.et_qp_name);
        EditText etInstruction = findViewById(R.id.et_qp_instruction);
        EditText etMaxTime = findViewById(R.id.et_qp_maxtime);
        RecyclerView rvQuestions = findViewById(R.id.rv_questions);
        Button btnNewQuestion = findViewById(R.id.btn_new_question);

        // Set question recycler view
        rvQuestions.setHasFixedSize(true);
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(questionsAdapter);

        // add new question
        btnNewQuestion.setOnClickListener(v -> {
            Intent toAddNewQuestion = new Intent(this, AddQuestionActivity.class);
            toAddNewQuestion.putExtra("syllabus",syllabusString);
            toAddNewQuestion.putExtra("courseId",courseId);
            startActivityForResult(toAddNewQuestion,RC_NEW_MCQ);
        });

        // confirm and save the quiz
        saveQuiz.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String maxTime = etMaxTime.getText().toString().trim();

            if (name.isEmpty()){
                Toast.makeText(this,"Please add a name for the quiz",Toast.LENGTH_LONG).show();
                etName.requestFocus();
            }
            else if (maxTime.isEmpty()){
                Toast.makeText(this,"Please add a maximum Time for the quiz",Toast.LENGTH_LONG).show();
                etMaxTime.requestFocus();
            }
            else if (questionsAdapter.getQuestions() == null || questionsAdapter.getQuestions().size() < 10){
                Toast.makeText(this,"Please add at least 10 questions",Toast.LENGTH_LONG).show();
                btnNewQuestion.requestFocus();
            }
            else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Upload this Quiz?")
                        .setMessage("Its great, You just created a quiz with "+ questionsAdapter.getQuestions().size()+ " MCQ questions\n\nNow just check again and upload it")
                        .setNegativeButton("Check again", null)
                        .setPositiveButton("Confirm to Upload", (d, w) -> {

                            QuestionPaper qp = new QuestionPaper();

                            qp.setName(name);
                            qp.setMaxTime(Integer.parseInt(maxTime));
                            qp.setInstruction(etInstruction.getText().toString().trim());
                            qp.setDate(System.currentTimeMillis());
                            qp.setQuestions(questionsAdapter.getQuestions());

                            Toast.makeText(this,"Uploading your new created Quiz with " + qp.getQuestions().size()+ " questions",Toast.LENGTH_LONG).show();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            db.collection("Quizzes")
                                    .add(qp)
                                    .addOnSuccessListener(documentReference -> {

                                        Toast.makeText(this,"Uploading Quiz is completed",Toast.LENGTH_LONG).show();

                                        finish();
                                        String quizTypeIdName = getResources().getString(R.string.CodeQuiz) +
                                                getResources().getString(R.string.mySeparator) +
                                                documentReference.getId() +
                                                getResources().getString(R.string.mySeparator) +
                                                qp.getName()+"\n"+qp.getQuestions().size()+" Questions";
                                        db.document("Courses/"+courseId)
                                                .update("lessons", FieldValue.arrayUnion(quizTypeIdName));
                                    });



                            etName.setEnabled(false);
                            etMaxTime.setEnabled(false);
                            etInstruction.setEnabled(false);
                            btnNewQuestion.setEnabled(false);
                            saveQuiz.setVisibility(View.GONE);

                        });
                dialog.create().show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_NEW_MCQ && resultCode == RESULT_OK){

            questionsAdapter.addNewMcq((McqSet) data.getSerializableExtra("newQuestionSet"));
            syllabusString = data.getStringExtra("syllabus");
        }
    }
}
