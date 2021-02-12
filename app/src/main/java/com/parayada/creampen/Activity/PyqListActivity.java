package com.parayada.creampen.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.parayada.creampen.Adapter.PyqListAdapter;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.Model.QuestionPaper;
import com.parayada.creampen.R;

import java.time.Year;
import java.util.*;

public class PyqListActivity extends AppCompatActivity implements PyqListAdapter.QpClickHandler {

    private ArrayList<QuestionPaper> pyqList = new ArrayList<QuestionPaper>();

    private String selectedName = "", selectedYear = "";
    private PyqListAdapter pyqListAdapter;

    SharedPreferences sharedPref;
    long lastSaved;

    String intentFrom = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pyq_list);

        String pyqPath = getIntent().getStringExtra("pyqPath");
        intentFrom = getIntent().getStringExtra("from");

        FirebaseFirestore mDb = FirebaseFirestore.getInstance();

        //Load from cache
        mDb.collection(pyqPath)
                .get(Source.CACHE)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot snapshot : (task.getResult()).getDocuments()) {
                            QuestionPaper qp = snapshot.toObject(QuestionPaper.class);
                            pyqList.add(qp);
                        }
                        loadSpinners();
                    }
                });


        if (intentFrom.equals("main")) {
            sharedPref = this.getSharedPreferences("pyqList", Context.MODE_PRIVATE);
            lastSaved = sharedPref.getLong(pyqPath, 1000);
            //Load from Cloud
            mDb.collection(pyqPath)
                    .whereGreaterThan("published", lastSaved)
                    .orderBy("published").get(Source.SERVER)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot snapshot : (task.getResult()).getDocuments()) {

                                QuestionPaper qp = snapshot.toObject(QuestionPaper.class);
                                pyqList.add(qp);

                                if (lastSaved < snapshot.getLong("published")) {
                                    lastSaved = snapshot.getLong("published");
                                }
                            }
                            // Save the publish time of last published qp
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putLong(pyqPath, lastSaved);
                            editor.apply();

                            loadSpinners();
                        }
                    });
        }


        //Declare and assign recycler view
        RecyclerView rvPyqList = findViewById(R.id.rv_pyq_list);
        pyqListAdapter = new PyqListAdapter(pyqList, this);

        rvPyqList.setHasFixedSize(true);
        rvPyqList.setLayoutManager(new LinearLayoutManager(this));
        rvPyqList.setAdapter(pyqListAdapter);

    }

    private void loadSpinners() {


        //notify data set changed
        pyqListAdapter.notifyDataSetChanged();

        //Spinner for choosing the year
        Spinner yearSpinner = findViewById(R.id.spinner_year);
        // Spinner for choosing the exam
        Spinner nameSpinner = findViewById(R.id.spinner_name);


        //Load nameList and yearList
        ArrayList<String> names = new ArrayList<>();
        String[] yearList = new String[]{"All Previous Years", "2020", "2019", "2018", "2017", "2016"};
        for (QuestionPaper q : pyqList) {
            if (!names.contains(q.getName())) {
                names.add(q.getName());
            }
        }

        //Make year Adapter active
        ArrayAdapter yearAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, yearList);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = yearList[position];
                showBySelection();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Make name Adapter active
        Collections.sort(names);
        names.add(0, "All Exams");

        ArrayAdapter nameAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, names);
        nameSpinner.setAdapter(nameAdapter);
        nameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedName = names.get(position);
                showBySelection();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void showBySelection() {
        ArrayList<QuestionPaper> newList = new ArrayList<>();

        //When all question papers are shown
        if (selectedYear.equals("All Previous Years") && selectedName.equals("All Exams")) {
            newList = pyqList;
        }
        //When all question papers of a specific name is shown
        else if (selectedYear.equals("All Previous Years")) {
            for (QuestionPaper q : pyqList) {
                if (q.getName().equals(selectedName))
                    newList.add(q);
            }
        }
        //When all question papers of a specific year is shown
        else if (selectedName.equals("All Exams")) {
            for (QuestionPaper q : pyqList) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(q.getDate());
                if (c.get(Calendar.YEAR) == Integer.parseInt(selectedYear))
                    newList.add(q);
            }
        }
        //When all question papers of a specific name in a given year is shown
        else {
            for (QuestionPaper q : pyqList) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(q.getDate());
                if (c.get(Calendar.YEAR) == Integer.parseInt(selectedYear) && q.getName().equals(selectedName))
                    newList.add(q);
            }
        }

        pyqListAdapter.setViewBy(newList);

    }

    @Override
    public void onQpClick(QuestionPaper qp) {
        if (intentFrom.equals("main")) {
            Intent toExamActivity = new Intent(this, ExamActivity.class);
            toExamActivity.putExtra("qp", qp);

            startActivity(toExamActivity);
        } else if (intentFrom.equals("studio")) {

            ArrayList<String> selectedQuestions = new ArrayList<>();

            ArrayList<String> questions = new ArrayList<>();
            boolean[] bools = new boolean[qp.getQuestions().size()];

            for (int i = 0; i<qp.getQuestions().size(); i++){
                McqSet mcqSet = new McqSet(qp.getQuestions().get(i));
                questions.add(mcqSet.getQuestion() + " \n" +
                        "   A - " +mcqSet.getOption1() + "\n" +
                        "   B - " +mcqSet.getOption2() + "\n" +
                        "   C - " +mcqSet.getOption3() + "\n" +
                        "   D - " +mcqSet.getOption4() + "\n"
                );
                bools[i] = false;
            }

            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setTitle("Select question");
            b.setMultiChoiceItems(questions.toArray(new String[0]), bools, (dialog, which, isChecked) -> {
                if (selectedQuestions.contains(qp.getQuestions().get(which)))
                    selectedQuestions.remove(qp.getQuestions().get(which));
                else
                    selectedQuestions.add(qp.getQuestions().get(which));

                Log.d("selectedQuestions",selectedQuestions.toString());
            });
            //b.setItems(questions.toArray(new String[0]), (dialog, which) -> {//Show selection});
            b.setPositiveButton("OK",(d,w) -> {

                Intent data = new Intent();
                data.putStringArrayListExtra("selectedQuestions", selectedQuestions);

                setResult(Activity.RESULT_OK, data);
                d.dismiss();
                finish();
            });
            b.show();



        }
    }

}
