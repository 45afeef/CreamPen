package com.parayada.creampen.Activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parayada.creampen.Adapter.ExamAdapter;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.Model.QuestionPaper;
import com.parayada.creampen.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class CreateQpActivity extends AppCompatActivity implements ExamAdapter.clickHandler{

    private static final int RC_NEW_MCQ = 100;
    private static final int RC_EDIT_MCQ = 101;

    private ExamAdapter questionsAdapter = new ExamAdapter(this);

    private String syllabusString;
    private String courseId;
    private String quizId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_qp);

        courseId = getIntent().getStringExtra("courseId");
        syllabusString = getIntent().getStringExtra("syllabus");

        // Initialize views
        TextView saveQuiz = findViewById(R.id.save_quiz);
        TextView tvStartAt = findViewById(R.id.et_start_time);
        TextView tvEndAt = findViewById(R.id.et_end_time);
        EditText etName = findViewById(R.id.et_qp_name);
        EditText etInstruction = findViewById(R.id.et_qp_instruction);
        EditText etMaxTime = findViewById(R.id.et_qp_maxtime);
        SwitchMaterial lockSwitch = findViewById(R.id.lock_at_first);
        RecyclerView rvQuestions = findViewById(R.id.rv_questions);
        Button btnNewQuestion = findViewById(R.id.btn_new_question);

        // Fetch and load quiz if quizId is provided
        if (getIntent().hasExtra("quizId")) {
            quizId = getIntent().getStringExtra("quizId");
            setTitle("Edit Quiz");
            // Fetch from server
            FirebaseFirestore.getInstance().document("Quizzes/" + quizId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            QuestionPaper qp = task.getResult().toObject(QuestionPaper.class);
                            if (qp == null) {
                                Toast.makeText(this, "Can't find the Quiz", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                etName.setText(qp.getName());
                                etInstruction.setText(qp.getInstruction());
                                etMaxTime.setText(String.valueOf(qp.getMaxTime()));
                                lockSwitch.setChecked(qp.isLockAtFirst());

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                                tvStartAt.setText(sdf.format(qp.getStartAt().toDate()));
                                tvEndAt.setText(sdf.format(qp.getEndAt().toDate()));

                                questionsAdapter.setQuestions(qp.getQuestions());

                            }
                        }else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(this);
                            alert.setTitle("Oh Sorry")
                                    .setMessage("This quiz is not live yet or not available \n\nGet in touch with your educator so that you can come back when this quiz is live")
                                    .setPositiveButton("OK", (d, w) -> finish())
                                    .setCancelable(false)
                                    .show();
                        }
                    });
        }

        // Set interactive with switch
        lockSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> questionsAdapter.setLockAtFirst(isChecked));

        // Set question recycler view
        rvQuestions.setHasFixedSize(true);
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(questionsAdapter);

        // add date and time for start and end quiz
        ((View) tvStartAt.getParent()).setOnClickListener(v -> getDateTime(tvStartAt));
        ((View) tvEndAt.getParent()).setOnClickListener(v -> getDateTime(tvEndAt));

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

            String startingTime = tvStartAt.getText().toString().trim();
            String endingTime = tvEndAt.getText().toString().trim();

            if (name.isEmpty()){
                Toast.makeText(this,"Please add a name for the quiz",Toast.LENGTH_LONG).show();
                etName.requestFocus();
            }
            else if (maxTime.isEmpty()){
                Toast.makeText(this,"Please add a maximum Time for the quiz",Toast.LENGTH_LONG).show();
                etMaxTime.requestFocus();
            }
            else if (questionsAdapter.getQuestions() == null || questionsAdapter.getQuestions().size() < 1){
                Toast.makeText(this,"Please add at least 10 questions",Toast.LENGTH_LONG).show();
                btnNewQuestion.requestFocus();
            }
            else if (startingTime.isEmpty()){
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("When to start?")
                        .setMessage("You haven't choose a starting date and time for the quiz. \nChoose one now")
                        .setPositiveButton("Choose a Date", (d,w) -> getDateTime(tvStartAt));
                dialog.create().show();
            }
            else if(endingTime.isEmpty()) {
                tvEndAt.setText(startingTime);
                Toast.makeText(this,"Expiry date set to starting date itself by default",Toast.LENGTH_LONG).show();
            }
            else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Upload this Quiz?")
                        .setMessage("Its great, You just created a quiz with "+ questionsAdapter.getQuestions().size()+ " MCQ questions\n\nNow just check again and upload it")
                        .setNegativeButton("Check again", null)
                        .setPositiveButton("Confirm to Upload", (d, w) -> {

                            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

                            QuestionPaper qp = new QuestionPaper();

                            qp.setName(name);
                            qp.setMaxTime(Integer.parseInt(maxTime));
                            qp.setInstruction(etInstruction.getText().toString().trim());
                            qp.setDate(System.currentTimeMillis());
                            qp.setQuestions(questionsAdapter.getQuestions());
                            qp.setLockAtFirst(lockSwitch.isChecked());
                            qp.setEducatorIds(getIntent().getStringArrayListExtra("educatorIds"));
                            qp.setEducatorNames(getIntent().getStringArrayListExtra("educatorNames"));

                            // Set the Dates
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                            try {
                                qp.setStartAt(new Timestamp(format.parse(startingTime)));
                                qp.setEndAt(new Timestamp(format.parse(endingTime)));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Toast.makeText(this,"Uploading your new created Quiz with " + qp.getQuestions().size()+ " questions",Toast.LENGTH_LONG).show();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            if(quizId == null) {
                                db.collection("Quizzes")
                                        .add(qp)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(this, "Uploading Quiz is completed", Toast.LENGTH_LONG).show();
                                            finish();

                                            String quizTypeIdName = getResources().getString(R.string.CodeQuiz) +
                                                    getResources().getString(R.string.mySeparator) +
                                                    documentReference.getId() +
                                                    getResources().getString(R.string.mySeparator) +
                                                    qp.getName() + "\n" + qp.getQuestions().size() + " Questions";

                                            db.document("Courses/" + courseId)
                                                    .update("lessons", FieldValue.arrayUnion(quizTypeIdName));
                                        });
                            }else {
                                db.collection("Quizzes").document(quizId)
                                        .set(qp)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Uploading Quiz is completed", Toast.LENGTH_LONG).show();

                                            String quizTypeIdName = getResources().getString(R.string.CodeQuiz) +
                                                    getResources().getString(R.string.mySeparator) +
                                                    quizId +
                                                    getResources().getString(R.string.mySeparator) +
                                                    qp.getName() + "\n" + qp.getQuestions().size() + " Questions";

                                            Intent data = new Intent();
                                            data.putExtra("quizTypeIdName", quizTypeIdName);

                                            int ind = getIntent().getIntExtra("lessonIndex",-1);

                                            data.putExtra("lessonIndex",ind);
                                            setResult(RESULT_OK, data);
                                            finish();
                                        });
                            }
                            // Disable the fields while uploading
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

    private void getDateTime(TextView textView) {
        // calender class's instance and get current date , month and year from calender
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR); // current year
        int mMonth = c.get(Calendar.MONTH); // current month
        int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        final String[] date = {""};
        final String[] time = {""};

        // date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateQpActivity.this,
                (dateView, year, monthOfYear, dayOfMonth) -> {
                    // set day of month , month and year value in the edit text
                    String dd;
                    String MM;

                    if (dayOfMonth<10) {dd = "0" + dayOfMonth;
                    }else {dd = String.valueOf(dayOfMonth);}

                    if (monthOfYear<9) {MM = "0" + (monthOfYear + 1);
                    }else {MM = String.valueOf(monthOfYear + 1);}

                    date[0] = dd + "/" + MM + "/" + year;

                    TimePickerDialog timePickerDialog = new TimePickerDialog(CreateQpActivity.this,
                            (timeView,hour,minute) -> {
                                // set hour and minute of the day
                                if (hour < 10 && minute < 10)
                                    time[0] = "0" + hour + ":0" + minute;
                                else if (hour < 10)
                                    time[0] = "0" + hour + ":" + minute;
                                else if (minute < 10)
                                    time[0] = hour + ":0" + minute;
                                else
                                    time[0] = hour + ":" + minute;

                                textView.setText(String.format("%s %s", date[0], time[0]));
                            },mHour,mMinute,false);

                    timePickerDialog.show();
                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Cancel Quiz Creation?")
                .setMessage("Are you sure to discard this quiz and close it now")
                .setNegativeButton("Continue working",null)
                .setPositiveButton("Discard and Exit", (dialog1, which) ->super.onBackPressed());
        dialog.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == RC_NEW_MCQ){
                questionsAdapter.addNewMcq((McqSet) data.getSerializableExtra("newQuestionSet"));
            }else if(requestCode == RC_EDIT_MCQ){
                questionsAdapter.setMcq(
                        data.getIntExtra("qIndex",0),
                        (McqSet) data.getSerializableExtra("newQuestionSet"));
            }
            syllabusString = data.getStringExtra("syllabus");
        }
    }

    @Override
    public void onLongClickMcq(int index,String qString) {
        Intent toAddNewQuestion = new Intent(this, AddQuestionActivity.class);

        toAddNewQuestion.putExtra("qString",qString);
        toAddNewQuestion.putExtra("qIndex",index);
        toAddNewQuestion.putExtra("syllabus",syllabusString);
        toAddNewQuestion.putExtra("courseId",courseId);
        startActivityForResult(toAddNewQuestion,RC_EDIT_MCQ);
    }
}
