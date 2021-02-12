package com.parayada.creampen.Activity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.parayada.creampen.Adapter.AnalyseAdapter;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {

    TextView tv;
    RecyclerView rv;
    AnalyseAdapter analyseAdapter;

    ArrayList<String> questions,
            selectedAnswers,
            cropedAnswers = new ArrayList<>(),
            cats = new ArrayList<>(),
            catRightList = new ArrayList<>(),
            catWrongList = new ArrayList<>(),
            catSkippedList = new ArrayList<>(),
            mainRightList = new ArrayList<>(),
            mainWrongList = new ArrayList<>(),
            mainSkippedList = new ArrayList<>(),
            subRightList = new ArrayList<>(),
            subWrongList = new ArrayList<>(),
            subSkippedList = new ArrayList<>();

    ArrayList<McqSet>
            wrongMcq = new ArrayList<>(),
            rightMcq = new ArrayList<>(),
            skippedMcq = new ArrayList<>(),
            cropedMcqs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        //loadAds();

        questions = getIntent().getStringArrayListExtra("questions");
        selectedAnswers = getIntent().getStringArrayListExtra("answers");

        for (int i = 0; i<questions.size(); i++){
            McqSet mcqSet = new McqSet(questions.get(i));

            if (selectedAnswers.get(i).equals(getResources().getString(R.string.skipped))){
                skippedMcq.add(mcqSet);
                analyseMcq(mcqSet.getTopics(),catSkippedList,mainSkippedList,subSkippedList);
            }
            else if (selectedAnswers.get(i).equals(mcqSet.getAnswer())){
                rightMcq.add(mcqSet);
                analyseMcq(mcqSet.getTopics(),catRightList,mainRightList,subRightList);
            }
            else {
                wrongMcq.add(mcqSet);
                analyseMcq(mcqSet.getTopics(),catWrongList,mainWrongList,subWrongList);
            }

        }

        loadPieChart(findViewById(R.id.scorePieChart));
        loadBarChart(findViewById(R.id.barChart));

        // Load all wrong question and selected answers as croped on create

        tv = findViewById(R.id.tv_analysis);
        rv = findViewById(R.id.rv_analysis);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(this));
        analyseAdapter = new AnalyseAdapter(cropedMcqs,cropedAnswers);
        rv.setAdapter(analyseAdapter);
    }

    private void loadAds() {
        MobileAds.initialize(this, initializationStatus -> { });

        AdView adView = findViewById(R.id.adView);
        //Load Ad in adView
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("E56246F9159612F353BE9D2DECF13389").build();

        adView.loadAd(adRequest);
    }

    private BarData getTopicWiseAnalysedData(
            ArrayList<String> skippedList,
            ArrayList<String> wrongList,
            ArrayList<String> rightList) {

        ArrayList<BarEntry> valueSet = new ArrayList<>();

        for (int i = 0; i<cats.size(); i++){
            String cat = cats.get(i);

            valueSet.add(new BarEntry(i,
                    new float[]{
                            Collections.frequency(skippedList,cat),
                            Collections.frequency(wrongList,cat),
                            Collections.frequency(rightList,cat),
            }));
        }

        BarDataSet barDataSet = new BarDataSet(valueSet, "");
        barDataSet.setColors(new int[]{getResources().getColor(R.color.skipped), getResources().getColor(R.color.wrong), getResources().getColor(R.color.right)});
        barDataSet.setStackLabels(new String[]{"Skipped","Wrong","Right"});

        ArrayList dataSets = new ArrayList();
        dataSets.add(barDataSet);

        return new BarData(dataSets);
    }

    private void analyseMcq(
            ArrayList<String> topics,
            ArrayList<String> catList,
            ArrayList<String> mainList,
            ArrayList<String> subList) {

        ArrayList<String> mcqCats = new ArrayList<>();
        ArrayList<String> mcqMains = new ArrayList<>();

        for (String topic:topics){
            String subject = topic.substring(0,topic.indexOf("->")).trim();
            String mainTopic = topic.substring(0,topic.lastIndexOf("->")).trim();

            if (!mcqCats.contains(subject)) mcqCats.add(subject);
            if (!mcqMains.contains(mainTopic)) mcqMains.add(mainTopic);
            subList.add(topic);

            if (!cats.contains(subject)) {cats.add(subject);}

        }
        catList.addAll(mcqCats);              mcqCats.clear();
        mainList.addAll(mcqMains);            mcqMains.clear();
    }

    private SpannableString generateCenterSpannableText() {

        String  mark ="Scored\n";
        switch (wrongMcq.size() % 3){
            case 0:
                mark += String.valueOf(rightMcq.size()- wrongMcq.size()/3);
                break;
            case 1:
                mark += (rightMcq.size() - 1 - wrongMcq.size() / 3) + ".67";
                break;
            case 2:
                mark += (rightMcq.size() - 1 - wrongMcq.size() / 3) + ".33";
                break;
        }
        mark += "\nMarks";

        SpannableString s = new SpannableString(mark+"\nApp developed by\nAfeef K K");
        s.setSpan(new RelativeSizeSpan(1.9f), 0, mark.length(), 0);
        s.setSpan(new StyleSpan(Typeface.NORMAL), mark.length(), s.length() - 10, 0);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), mark.length(), s.length() - 9, 0);
        s.setSpan(new RelativeSizeSpan(.8f), mark.length(), s.length() - 9, 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 11, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 9, s.length(), 0);
        return s;
    }

    private void loadPieChart(PieChart pieChart){

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(skippedMcq.size(),"Skipped"));
        entries.add(new PieEntry(wrongMcq.size(),"Wrong"));
        entries.add(new PieEntry(rightMcq.size(),"Right"));

        PieDataSet set = new PieDataSet(entries,"");
        set.setSliceSpace(0f);
        List<Integer> colors = new ArrayList<>();
        colors.add(getResources().getColor(R.color.skipped));
        colors.add(getResources().getColor(R.color.wrong));
        colors.add(getResources().getColor(R.color.right));
        set.setColors(colors);

        PieData data = new PieData(set);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(14f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });

        pieChart.setData(data);
        pieChart.animateXY(1400,1400);
        pieChart.setDescription(null);
        pieChart.invalidate();

        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(60f);

        pieChart.setCenterText(generateCenterSpannableText());
        pieChart.setDrawCenterText(true);

        // entry label styling
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(14f);
    }

    private void loadBarChart(BarChart mChart){

        BarData data = getTopicWiseAnalysedData(catSkippedList,catWrongList,catRightList);

        mChart.setDescription(null);
        mChart.animateXY(1400, 1400);

        mChart.setDoubleTapToZoomEnabled(false);

        mChart.setDrawValueAboveBar(false);
        data.setDrawValues(true);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) Math.floor(value));
            }
        });
        data.setValueTextColor(Color.LTGRAY);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelRotationAngle(65);
        xAxis.setLabelCount(cats.size());
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return cats.get((int) value);
            }
        });

        mChart.getAxisRight().setDrawGridLines(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getAxisLeft().setGridColor(Color.LTGRAY);

        mChart.setData(data);

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                getCropedMcq(h.getX(),h.getStackIndex());
                analyseAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected() {
                tv.setText("Select a bar to view your answers");
                cropedMcqs.clear();
                cropedAnswers.clear();
                analyseAdapter.notifyDataSetChanged();
            }

        });

        mChart.setFitBars(true);
        mChart.invalidate();
    }

    private void getCropedMcq(float x, int stackIndex) {

        String subject = cats.get((int) x);
        cropedMcqs.clear();
        cropedAnswers.clear();

        if (stackIndex == 0){
            for (McqSet mcqSet:skippedMcq){
                if (mcqSet.getTopics().toString().contains(subject)){
                    cropedMcqs.add(mcqSet);
                    int index = questions.indexOf(mcqSet.getQuestionAsString());
                    cropedAnswers.add(selectedAnswers.get(index));
                }
            }
            tv.setText("Skipped questions from ");
        }
        else if (stackIndex == 1){
            for (McqSet mcqSet:wrongMcq){
                if (mcqSet.getTopics().toString().contains(subject)){
                    cropedMcqs.add(mcqSet);
                    int index = questions.indexOf(mcqSet.getQuestionAsString());
                    cropedAnswers.add(selectedAnswers.get(index));
                }
            }
            tv.setText("Wrong answers from ");
        }
        else {
            for (McqSet mcqSet:rightMcq){
                if (mcqSet.getTopics().toString().contains(subject)){
                    cropedMcqs.add(mcqSet);
                    int index = questions.indexOf(mcqSet.getQuestionAsString());
                    cropedAnswers.add(selectedAnswers.get(index));
                }
            }
            tv.setText("Right from ");
        }

        tv.append(subject);
    }
}

