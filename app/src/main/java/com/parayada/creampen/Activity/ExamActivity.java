package com.parayada.creampen.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parayada.creampen.Adapter.ExamAdapter;
import com.parayada.creampen.Model.AnswerPaper;
import com.parayada.creampen.Model.QuestionPaper;
import com.parayada.creampen.R;
import com.parayada.creampen.Utils.GoogleAdGarage;

import java.util.ArrayList;

public class ExamActivity extends AppCompatActivity {

    private ExamAdapter examAdapter;
    private ArrayList<String> answers;
    private QuestionPaper qp;
    private String quizId;
    private MenuItem finishExam;

    private CountDownTimer timer;
    private long timeLeft;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        // Load Ads from adGarage
        GoogleAdGarage.loadBannerFromXml(this,findViewById(R.id.adView));

        Log.d("ExamActivity","justStarted");

        if (getIntent().hasExtra("qp")){
            qp = getIntent().getParcelableExtra("qp");
            showInstruction();
        }
        else if (getIntent().hasExtra("quizId")){
            quizId = getIntent().getStringExtra("quizId");
            FirebaseFirestore.getInstance().document("Quizzes/"+quizId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult()!= null){
                            Log.d("FirebaseResult","success");

                            qp = task.getResult().toObject(QuestionPaper.class);

                            if (qp != null){
                                showInstruction();
                            }else {
                                Toast.makeText(this,"Can't find the Quiz",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this,MainActivity.class));
                                finish();
                            }
                        }else {
                            AlertDialog.Builder alert = new AlertDialog.Builder(this);
                            alert.setTitle("Oh Sorry")
                                    .setMessage("This quiz is not live yet or not available \n\nGet in touch with your educator so that you can come back here when this quiz is live")
                                        .setPositiveButton("OK", (d,w) -> finish())
                                        .setCancelable(false)
                                    .show();
                        }
                    });
        }
        else {
            Toast.makeText(this,"No Quiz Found",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    private void showInstruction(){
//        if (qp.getInstruction() != null && !qp.getInstruction().isEmpty()){

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Instruction")
                    .setMessage(qp.getInstruction() + "\n\nMaximum time allowed is " + qp.getMaxTime() + " minutes")
                    .setNegativeButton("Go Back",(d,w) -> finish())
                    .setPositiveButton("Start Exam", (dialog, which) -> loadQuestionPaper())
                    .setCancelable(false)
                    .show();
  //      }else {
   //         loadQuestionPaper();
     //   }
    }

    private void loadQuestionPaper() {
        TextView tvCounter = findViewById(R.id.tv_counter);

        timer = new CountDownTimer(qp.getMaxTime()*60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;

                int secs = (int) (millisUntilFinished / 1000);
                int mins = secs / 60;
                secs = secs % 60;
                if (tvCounter != null)
                    tvCounter.setText("Time remains : " + String.format("%02d", mins) + ":" + String.format("%02d", secs));

            }

            @Override
            public void onFinish() {
                Toast.makeText(ExamActivity.this,"Checking your answers Please wait...", Toast.LENGTH_LONG).show();
                finishExam.setTitle("Show Analysis");
                uploadAnswerPaper();
            }

        }.start();

        setTitle(qp.getName());

        examAdapter = new ExamAdapter(qp.getQuestions(),qp.isLockAtFirst());


        RecyclerView rvExam = findViewById(R.id.rv_exam);
        rvExam.setHasFixedSize(true);
        rvExam.setLayoutManager(new LinearLayoutManager(this));
        rvExam.setAdapter(examAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_exam, menu);

        finishExam = menu.findItem(R.id.finish_exam);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.finish_exam) {
            if (item.getTitle().equals(this.getResources().getString(R.string.finish_exam))) {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Finish Exam ?")
                        .setMessage("Click Submit to finish this exam and to submit your answers")
                        .setNegativeButton("Check again", null)
                        .setPositiveButton("Submit", (dialog, which) -> {
                            Toast.makeText(this,"Checking your answers Please wait...", Toast.LENGTH_LONG).show();
                            item.setTitle("Show Analysis");
                            timer.cancel();
                            uploadAnswerPaper();
                        })
                        .show();
            }
            else {
                showAnalysis();
            }
        }
        return(super.onOptionsItemSelected(item));
    }

    private void uploadAnswerPaper(){
        answers = examAdapter.onFinish();

        // Now check whether the quiz attempt is made in between the statAt and endAt field
        if(qp.getEndAt() != null && qp.getEndAt().getSeconds() > (System.currentTimeMillis()/1000)){

            FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();

            AnswerPaper answerPaper = new AnswerPaper();

            answerPaper.setStudentId(mUser.getUid());
            answerPaper.setStudentName(mUser.getDisplayName());
            answerPaper.setQuestionPaperId(quizId);
            answerPaper.setQuestionPaperName(qp.getName());
            answerPaper.setQuestions(qp.getQuestions());
            answerPaper.setAnswers(answers);
            answerPaper.setTimeLeft(timeLeft);
            answerPaper.setMaxTime(qp.getMaxTime() * 60 * 1000);

            FirebaseFirestore.getInstance()
                    .document("Quizzes/" + quizId + "/AnswerPapers/"+mUser.getUid())
                    .set(answerPaper);
            Toast.makeText(this, "Your answers are submitted for educator review", Toast.LENGTH_SHORT).show();
            finish();
        }else{
            showAnalysis();
        }
    }

    private void showAnalysis() {
        Toast.makeText(this, "Showing Analysis", Toast.LENGTH_SHORT).show();

        Intent toAnalysisActivity = new Intent(this,AnalysisActivity.class);

        toAnalysisActivity.putStringArrayListExtra("answers",answers);
        toAnalysisActivity.putStringArrayListExtra("questions",qp.getQuestions());

        startActivity(toAnalysisActivity);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Wanna to go back?")
                .setMessage("Do you wanna to go back and cancel this exam or stay here?")
                .setNegativeButton("Stay here",null)
                .setPositiveButton("Yes Cancel Exam",(d,w)->finish())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (timer != null) timer.cancel();
    }
}
