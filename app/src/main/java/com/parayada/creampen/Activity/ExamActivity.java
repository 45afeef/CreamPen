package com.parayada.creampen.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parayada.creampen.Adapter.ExamAdapter;
import com.parayada.creampen.Model.Course;
import com.parayada.creampen.Model.QuestionPaper;
import com.parayada.creampen.R;

import java.util.ArrayList;
import java.util.Arrays;

public class ExamActivity extends AppCompatActivity {

    private ExamAdapter examAdapter;
    private ArrayList<String> answers;
    private QuestionPaper qp;
    private String quizId;
    private MenuItem finishExam;
    private CountDownTimer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam);

        // Todo don't forgot to add ads in release build
        //loadAds();

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

                            qp = task.getResult().toObject(QuestionPaper.class);
                            if (qp != null){
                                showInstruction();
                            }else {
                                Toast.makeText(this,"Can't find the Quiz",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this,MainActivity.class));
                                finish();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(this,"No Quiz Found",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    private void loadAds() {
        MobileAds.initialize(this, initializationStatus -> { });

        AdView adView = findViewById(R.id.adView);
        //Load Ad in adView
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("E56246F9159612F353BE9D2DECF13389").build();

        adView.loadAd(adRequest);
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
                answers = examAdapter.onFinish();
                showAnalysis();
            }

        }.start();

        setTitle(qp.getName());

        examAdapter = new ExamAdapter(qp.getQuestions());

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
                        .setMessage("Click OK to finish this exam and to submit your answers")
                        .setNegativeButton("Check again", null)
                        .setPositiveButton("OK", (dialog, which) -> {
                            Toast.makeText(this,"Checking your answers Please wait...", Toast.LENGTH_LONG).show();
                            answers = examAdapter.onFinish();
                            item.setTitle("Show Analysis");
                            timer.cancel();
                            showAnalysis();
                        })
                        .show();
            }
            else {
                showAnalysis();
            }
        }
        return(super.onOptionsItemSelected(item));
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
