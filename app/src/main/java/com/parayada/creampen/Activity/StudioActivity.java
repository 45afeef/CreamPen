package com.parayada.creampen.Activity;

import android.content.SharedPreferences;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.parayada.creampen.Adapter.SliderAdapter;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.R;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class StudioActivity extends AppCompatActivity {


    private static final int RC_NEW_IMAGE = 100;
    private static final int RC_NEW_QUESTION = 101;
    private static final int RC_NEW_TEXT = 102;
    private static final int RC_CHOOSE_PYQ = 103;

    private static final int RC_UPLOAD_LESSON = 201;
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private Context mContext;

    private ViewPager2 viewPager;
    private Button btnDelete;
    private TextView imageCounter;
    private TextView questionCounter;
    private TextView textCounter;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */

    private SliderAdapter sliderAdapter;

    private ArrayList<String> slideList = new ArrayList<>();
    private TextView titleView;
    private int imageCount=0,questionCount=0,textCount = 0;

    private String syllabusString;

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.

            if (slideList.size() == 0)
                deleteLessonDraft();
            super.onBackPressed();
        }
        else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_studio);
        hide();
        mContext = this;

        syllabusString = getIntent().getStringExtra("syllabus");


        // Instantiate Image buttons and counter views
        ImageButton moreQuestion = (ImageButton) findViewById(R.id.moreQuestionButton);
        questionCounter = (TextView) findViewById(R.id.tv_question_counter);
        ImageButton morePlainText = (ImageButton) findViewById(R.id.moreTextButton);
        textCounter = (TextView) findViewById(R.id.tv_text_counter);
        ImageButton moreSlides = (ImageButton) findViewById(R.id.moreSlidesButton);
        imageCounter = (TextView) findViewById(R.id.tv_slide_counter);
        Button btnRecord = (Button) findViewById(R.id.startRecordButton);
        btnDelete = (Button) findViewById(R.id.btnDeleteSlide);
        titleView = (TextView) findViewById(R.id.tvTitle);
        viewPager = findViewById(R.id.pager);

        // Load the slideList from draft
        if (getIntent().hasExtra("slideList")) {
            slideList = getIntent().getStringArrayListExtra("slideList");
            btnDelete.setVisibility(View.VISIBLE);
        }

        // update ViewPager2 and a PagerAdapter.
        sliderAdapter  = new SliderAdapter(slideList);
        viewPager.setAdapter(sliderAdapter);

        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(1);
        viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(10));
        compositePageTransformer.addTransformer((page, position) -> {
            float r = 1 - Math.abs(position);
            page.setScaleY(0.85f + r * 0.15f);
        });
        viewPager.setPageTransformer(compositePageTransformer);

        // load from saved
        if (getIntent().hasExtra("lessonName"))
            titleView.setText(getIntent().getStringExtra("lessonName"));

        moreSlides.setOnClickListener(v -> {

            Intent intent = new Intent(mContext,ChooseImageActivity.class);
            startActivityForResult(intent,RC_NEW_IMAGE);

            /*
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            intent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent,"select images"),RC_NEW_IMAGE);*/

        });

        moreQuestion.setOnClickListener(v -> {
            Intent toAddNewQuestion = new Intent(mContext, AddQuestionActivity.class);
            toAddNewQuestion.putExtra("syllabus",syllabusString);
            toAddNewQuestion.putExtra("courseId",getIntent().getStringExtra("courseId"));
            startActivityForResult(toAddNewQuestion,RC_NEW_QUESTION);
        });

        //Activate long click on moreQuestion button only when selected stream have previous question paper option enable
        //Only in Kerala psc for the time being
        SharedPreferences sharedPref = mContext.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (sharedPref.getString(getString(R.string.saved_stream_key),"").equals("Kerala PSC")){
            moreQuestion.setOnLongClickListener(v -> {
                Intent toPyqActivity = new Intent(mContext,PyqListActivity.class);
                toPyqActivity.putExtra("pyqPath","Streams/Kerala PSC/PYQ");
                toPyqActivity.putExtra("from","studio");

                startActivityForResult(toPyqActivity,RC_CHOOSE_PYQ);
                return false;
            });
        }

        morePlainText.setOnClickListener(v -> {
            Intent toAddNewText = new Intent(mContext,AddTextActivity.class);
            startActivityForResult(toAddNewText,RC_NEW_TEXT);
        });

        btnDelete.setOnClickListener(v -> {

            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Confirm this deletion")
                    .setMessage("You are about to delete current slide. Are you sure to delete?")
                    .setNegativeButton("Don't delete",null)
                    .setPositiveButton("Yes", (d,w) -> {
                        // delete only after conformation
                        if (slideList != null && slideList.size() > 0) {

                            switch (slideList.get(viewPager.getCurrentItem()).substring(0,3)){
                                case "100":
                                    //Image slide
                                    imageCount--;
                                    imageCounter.setText(String.valueOf(imageCount));

                                    break;
                                case "101":
                                    // Mcq slide
                                    questionCount--;
                                    questionCounter.setText(String.valueOf(questionCount));

                                    break;
                                case "102":
                                    // html slide
                                    textCount--;
                                    textCounter.setText(String.valueOf(textCount));

                                    break;
                            }

                            if (slideList.size() == 1){
                                v.setVisibility(View.GONE);
                            }

                            slideList.remove(viewPager.getCurrentItem());
                            sliderAdapter.notifyDataSetChanged();
                        }
                    });
            dialog.create().show();
        });

        btnRecord.setOnClickListener(v -> {
            if (titleView.getText().toString().trim().isEmpty()){
                titleView.requestFocus();
                Toast.makeText(mContext,"Please add title for lesson",Toast.LENGTH_LONG).show();
            }else if (slideList.size() > 2) {
                Intent toRecordActivity = new Intent(mContext, RecordActivity.class);

                //Sending a ArrayList of objects to another activity using bundle (to RecordActivity)

                //Bundle args = new Bundle();
                //args.putSerializable("ARRAYLIST", (Serializable) slideList);
                //args.putString("title", titleView.getText().toString().trim());

                toRecordActivity.putExtra("title", titleView.getText().toString().trim());
                toRecordActivity.putExtra("slideList",slideList);
                //toRecordActivity.putExtra("slideBundle", args);
                toRecordActivity.putExtra("courseId",getIntent().getStringExtra("courseId"));
                toRecordActivity.putExtra("courseTitle",getIntent().getStringExtra("courseTitle"));
                toRecordActivity.putExtra("chosenTopics",getIntent().getStringArrayListExtra("chosenTopics"));

                startActivityForResult(toRecordActivity, RC_UPLOAD_LESSON);
            }else {
                Toast.makeText(mContext,"Please add at least 3 slides",Toast.LENGTH_LONG).show();
            }
        });

    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        btnDelete.setVisibility(View.VISIBLE);
        titleView.clearFocus();
        if (requestCode == RC_NEW_IMAGE && resultCode == RESULT_OK){
            ArrayList<Uri> result = data.getParcelableArrayListExtra("images");
            if (result!= null){
                for (Uri uri:result){
                    imageCount ++;
                    if (slideList.isEmpty()) slideList.add(String.valueOf(RC_NEW_IMAGE)+uri);
                    else slideList.add(viewPager.getCurrentItem()+1,String.valueOf(RC_NEW_IMAGE)+uri);
                }
            }
            sliderAdapter.notifyDataSetChanged();
            imageCounter.setText(String.valueOf(imageCount));

            Toast.makeText(this, "New Image added", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == RC_NEW_QUESTION && resultCode == RESULT_OK){

            McqSet newQuestionSet = (McqSet) data.getSerializableExtra("newQuestionSet");
            syllabusString = data.getStringExtra("syllabus");

            questionCount ++;
            if (slideList.isEmpty()) slideList.add(newQuestionSet.getQuestionAsString());
            else slideList.add(viewPager.getCurrentItem()+1,newQuestionSet.getQuestionAsString());
            sliderAdapter.notifyDataSetChanged();
            questionCounter.setText(String.valueOf(questionCount));

            Toast.makeText(this, "New Question added", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == RC_CHOOSE_PYQ && resultCode == RESULT_OK){

            ArrayList<String> result = data.getStringArrayListExtra("selectedQuestions");

            questionCount += result.size();
            if (slideList.isEmpty()) slideList.addAll(result);
            else slideList.addAll(viewPager.getCurrentItem()+1,result);
            sliderAdapter.notifyDataSetChanged();
            questionCounter.setText(String.valueOf(questionCount));

            Toast.makeText(this, "New "+result.size()+" Question added", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == RC_NEW_TEXT && resultCode == RESULT_OK){
            String newTextData = data.getStringExtra("newText");

            textCount++;
            if (slideList.isEmpty()) slideList.add(RC_NEW_TEXT+newTextData);
            else slideList.add(viewPager.getCurrentItem()+1,RC_NEW_TEXT+newTextData);
            sliderAdapter.notifyDataSetChanged();
            textCounter.setText(String.valueOf(textCount));

            Toast.makeText(this, "New Text added", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == RC_UPLOAD_LESSON && resultCode == RESULT_OK){

            deleteLessonDraft();
            finish();
        }
    }

    private void deleteLessonDraft() {
        SharedPreferences sharedPref = this.getSharedPreferences(getIntent().getStringExtra("courseId"), Context.MODE_PRIVATE);
        if (sharedPref.getBoolean(getString(R.string.hasSaved),false)) {
            SharedPreferences.Editor editor = sharedPref.edit();

            // delete the lessonName
            editor.putString(getString(R.string.lessonName), "");

            // delete all slides and set slide count to zero = 0
            editor.putInt(getString(R.string.slideCount), 0);
            for (int i = 0; i < slideList.size(); i++)
                editor.putString(getString(R.string.slide) + i, "103#343434Sooooo sorry \n\n No Slide found ! :( ");

            // delete all topics and set topic count to zero = 0
            editor.putInt(getString(R.string.topicCount), 0);
            for (int i = 0; i < sharedPref.getInt(getString(R.string.topicCount), 0); i++)
                editor.putString(getString(R.string.topic) + i, "Cat->Main->sub");

            //Apply edits
            editor.putBoolean(getString(R.string.hasSaved), false);
            editor.apply();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the slideList
        outState.putStringArrayList("slideList",slideList);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Retrieve the slideList
        slideList = savedInstanceState.getStringArrayList("slideList");
        sliderAdapter  = new SliderAdapter(slideList);
        viewPager.setAdapter(sliderAdapter);

        if (slideList.size() != 0)
            btnDelete.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (slideList.size() !=0 ) {
            SharedPreferences sharedPref = this.getSharedPreferences(getIntent().getStringExtra("courseId"), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            // save the lessonName
            editor.putString(getString(R.string.lessonName), titleView.getText().toString());

            // save all slides
            editor.putInt(getString(R.string.slideCount), slideList.size());
            for (int i = 0; i < slideList.size(); i++)
                editor.putString(getString(R.string.slide) + i, slideList.get(i));

            // save all topics
            ArrayList<String> topics = getIntent().getStringArrayListExtra("chosenTopics");
            editor.putInt(getString(R.string.topicCount), topics.size());
            for (int i = 0; i < topics.size(); i++)
                editor.putString(getString(R.string.topic) + i, topics.get(i));

            //Apply edits
            editor.putBoolean(getString(R.string.hasSaved), true);
            editor.apply();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        imageCount = 0;
        questionCount = 0;
        textCount = 0;
        for (String slide:slideList) {
            switch (slide.substring(0, 3)) {
                case "100":
                    //Image slide
                    imageCount++;
                    break;
                case "101":
                    // Mcq slide
                    questionCount++;
                    break;
                case "102":
                    // html slide
                    textCount++;
                    break;
            }
        }
        imageCounter.setText(String.valueOf(imageCount));
        questionCounter.setText(String.valueOf(questionCount));
        textCounter.setText(String.valueOf(textCount));

    }
}
