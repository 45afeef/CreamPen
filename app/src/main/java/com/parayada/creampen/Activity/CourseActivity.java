package com.parayada.creampen.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parayada.creampen.Adapter.LessonsAdapter;
import com.parayada.creampen.Model.Course;
import com.parayada.creampen.Model.SavedItem;
import com.parayada.creampen.Model.TypeIdName;
import com.parayada.creampen.R;
import com.parayada.creampen.Room.SavedItemViewModel;
import com.parayada.creampen.Utils.SharingLink;

import java.util.ArrayList;

public class CourseActivity extends AppCompatActivity implements LessonsAdapter.lessonClickHandler{

    private static final int RC_CHOOSE_TOPIC = 100;
    private static final int RC_UPDATE_LESSON = 101;
    private static final int RC_ADD_EDUCATOR = 102;

    boolean isEducator = false;

    private String courseId;

    private SavedItemViewModel mViewModel;

    private FirebaseUser mUser;
    private Course course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (getIntent().hasExtra("course")){
            course = (Course) getIntent().getParcelableExtra("course");
            courseId = course.getId();
            loadCourse();
        }
        else if (getIntent().hasExtra("courseId")) {
            courseId =getIntent().getStringExtra("courseId");
            FirebaseFirestore.getInstance().document("Courses/"+courseId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult()!= null){

                            course = task.getResult().toObject(Course.class);
                            if (course !=null) {
                                course.setId(courseId);
                                loadCourse();
                            }else {
                                Toast.makeText(this,"Can't find the Course",Toast.LENGTH_LONG).show();

                                startActivity(new Intent(this,MainActivity.class));

                                finish();
                            }
                        }
                    });
        }
        else {
            Toast.makeText(this,"No Course Found",Toast.LENGTH_LONG).show();
            // finish();
        }

    }

    private void loadCourse() {
        isEducator = course.getEducatorIds().contains(mUser.getUid());
        if (isEducator){
            //Course Activity is opened by an Educator

            // Show NewLessonFab and label
            FloatingActionButton fabLesson = findViewById(R.id.fab_new_lesson);
            findViewById(R.id.new_lesson_label).setVisibility(View.VISIBLE);
            fabLesson.setVisibility(View.VISIBLE);
            fabLesson.setOnClickListener(v -> {

                SharedPreferences sharedPref = this.getSharedPreferences(course.getId(), Context.MODE_PRIVATE);

                //Check whether any lessons draft
                boolean hasSaved = sharedPref.getBoolean(getString(R.string.hasSaved),false);
                if (hasSaved){

                    //Get the lesson Name
                    String lessonName = sharedPref.getString(getString(R.string.lessonName),"");

                    //Get the saved slides
                    int slideCount = sharedPref.getInt(getString(R.string.slideCount),0);
                    ArrayList<String> slideList = new ArrayList<>();
                    for (int i= 0; i<slideCount; i++)
                        slideList.add(sharedPref.getString(getString(R.string.slide)+i,"103#343434Sooooo sorry \n\n No Slide found ! :( "));

                    //Get the saved topics
                    int topicCount = sharedPref.getInt(getString(R.string.topicCount),0);
                    ArrayList<String> topics  = new ArrayList<>();
                    for (int i = 0; i<topicCount; i++)
                        topics.add(sharedPref.getString(getString(R.string.topic)+i,"Subject->Main Topic-> sub Topic"));



                    //Start studioActivity with the above details too
                    // start activity to add lesson
                    Intent addLessonActivity = new Intent(CourseActivity.this, StudioActivity.class);

                    addLessonActivity.putExtra("courseId", course.getId());
                    addLessonActivity.putExtra("courseTitle", course.getTitle());
                    addLessonActivity.putExtra("syllabus",course.getSyllabus());

                    // chosen Topic is loaded from sharedPref
                    addLessonActivity.putExtra("chosenTopics",topics);

                    // other sharedPref values
                    addLessonActivity.putExtra("lessonName",lessonName);
                    addLessonActivity.putExtra("slideList",slideList);

                    startActivity(addLessonActivity);

                }else {
                    Intent chooseTopicIntent = new Intent(this, SyllabusActivity.class);
                    chooseTopicIntent.putExtra("syllabus", course.getSyllabus());

                    startActivityForResult(chooseTopicIntent, RC_CHOOSE_TOPIC);
                }
            });

            // Show NewQuizFab and label
            FloatingActionButton fabQp = findViewById(R.id.fab_new_qp);
            findViewById(R.id.new_quiz_label).setVisibility(View.VISIBLE);
            fabQp.setVisibility(View.VISIBLE);
            fabQp.setOnClickListener(v->{
                Intent newQPIntent = new Intent(this,CreateQpActivity.class);

                newQPIntent.putExtra("courseId", course.getId());
                newQPIntent.putExtra("syllabus",course.getSyllabus());

                startActivity(newQPIntent);
            });

        } else {/*Course Activity is opened by a Learner*/}
        setTitle(course.getTitle());

        ((TextView) findViewById(R.id.tv_description)).setText(course.getDescription());

        RecyclerView rvLessons = findViewById(R.id.rv_lessons);
        rvLessons.setHasFixedSize(true);
        rvLessons.setLayoutManager(new LinearLayoutManager(this));
        rvLessons.setAdapter(new LessonsAdapter(new TypeIdName().toTypeIdNameArrayList(course.getLessons()),isEducator,this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_course, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (isEducator){
            //Removing save button for educators
            menu.getItem(0).setVisible(false);

        }else {
            ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
                @SuppressWarnings("unchecked") @Override public <T extends ViewModel> T create(final Class<T> modelClass) {
                    if (modelClass.equals(SavedItemViewModel.class)) {
                        return (T) new SavedItemViewModel(getApplication());
                    } else {
                        return null;
                    }
                }
            };

            mViewModel = new ViewModelProvider(this,factory).get(SavedItemViewModel.class);

            SavedItem savedCourse = new SavedItem();
            savedCourse.setItemType("Course");
            savedCourse.setItemId(courseId);

            // Show save button if and only if it is not saved
            mViewModel.getItemByIdAndType(savedCourse).observe(this, item -> {
                if (item ==null ){
                    menu.getItem(0).setVisible(true);
                }else {
                    menu.getItem(0).setVisible(false);
                }
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.save_course:
                //add the function to perform here
                Toast.makeText(this,"Course saved for learning",Toast.LENGTH_LONG).show();
                SavedItem savedItem = new SavedItem();
                savedItem.setItemType("Course");
                savedItem.setItemTitle(course.getTitle());
                savedItem.setItemId(courseId);

                mViewModel.insert(savedItem);

                return(true);
            case R.id.share_course:
                if (course!=null)
                    SharingLink.Course(course,this);
                break;
            case R.id.action_educators:
                if( course!= null) {
                    AlertDialog.Builder b = new AlertDialog.Builder(this);
                    b.setTitle("Educators");
                    b.setItems(course.getEducatorNames().toArray(new String[0]), null);
                    if(isEducator){
                        b.setPositiveButton("Add New Educator", (dialogInterface, i) -> {
                            Intent newEducatorIntent = new Intent(this,AddEducatorActivity.class);
                            newEducatorIntent.putExtra("courseId", course.getId());
                            startActivityForResult(newEducatorIntent,RC_ADD_EDUCATOR);
                        });
                    }
                    b.show();
                }
        }
        return(super.onOptionsItemSelected(item));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CHOOSE_TOPIC) {
            // Update syllabus if changed
            if (data != null && data.hasExtra("syllabus")) {
                String newSyllabus = data.getStringExtra("syllabus");
                if (!newSyllabus.equals(course.getSyllabus())) {
                    course.setSyllabus(newSyllabus);
                    FirebaseFirestore.getInstance()
                            .document("Courses/" + courseId)
                            .update("syllabus", newSyllabus);
                }
            }
            if (resultCode == RESULT_OK) {
                // start activity to add lesson
                Intent addLessonActivity = new Intent(CourseActivity.this, StudioActivity.class);

                addLessonActivity.putExtra("courseId", course.getId());
                addLessonActivity.putExtra("courseTitle", course.getTitle());
                addLessonActivity.putExtra("syllabus",course.getSyllabus());
                addLessonActivity.putExtra("chosenTopics",data.getStringArrayListExtra("chosenTopics"));
                startActivity(addLessonActivity);

            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("No Topic is selected")
                        .setMessage("Please select at least one topic to create a lesson")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Let's select Topic", (d, w) -> {
                            Intent chooseTopicIntent = new Intent(this, SyllabusActivity.class);
                            chooseTopicIntent.putExtra("syllabus",course.getSyllabus());

                            startActivityForResult(chooseTopicIntent, RC_CHOOSE_TOPIC);
                        });
                dialog.create().show();
            }
        }else if(requestCode == RC_UPDATE_LESSON) {
            if (resultCode == RESULT_OK) {

                int index = data.getIntExtra("lessonIndex",-1);
                String quizTypeIdName = data.getStringExtra("quizTypeIdName");

                course.setLesson(index,quizTypeIdName);
                FirebaseFirestore.getInstance().document("Courses/" + courseId)
                        .update("lessons", course.getLessons());
            }
        }
    }

    @Override
    public void editQuiz(int index,String quizId) {
        Intent newQPIntent = new Intent(this,CreateQpActivity.class);

        newQPIntent.putExtra("lessonIndex",index);
        newQPIntent.putExtra("quizId", quizId);
        newQPIntent.putExtra("courseId", course.getId());
        newQPIntent.putExtra("syllabus",course.getSyllabus());

        startActivityForResult(newQPIntent,RC_UPDATE_LESSON);
    }
}
