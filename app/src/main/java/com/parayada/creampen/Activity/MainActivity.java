package com.parayada.creampen.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.parayada.creampen.Adapter.CourseAdapter;
import com.parayada.creampen.Adapter.MainFeedAdapter;
import com.parayada.creampen.Adapter.SavedItemAdpater;
import com.parayada.creampen.Adapter.StreamContentAdapter;
import com.parayada.creampen.Model.Course;
import com.parayada.creampen.Model.Lesson;
import com.parayada.creampen.Model.SavedItem;
import com.parayada.creampen.R;
import com.parayada.creampen.Room.SavedItemViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements SavedItemAdpater.clickHandler{

    private static final int RC_CREATE_COURSE = 100;
    private static final int RC_SIGN_IN = 101;
    private static final int RC_EDIT_PROFILE = 102;

    private FirebaseFirestore db;
    private FirebaseUser mUser;

    // Declare views
    private ViewPager2 viewPager;
    private ProgressBar loadingView;

    // Adapter and helpers
    private ArrayList<Lesson> lessonArrayList = new ArrayList<>();
    private ArrayList<Course> teachingsCourses = new ArrayList<>();
    private ArrayList<String> streamList = new ArrayList<>();

    private CourseAdapter teachAdapter = new CourseAdapter(teachingsCourses);
    private MainFeedAdapter feedAdapter = new MainFeedAdapter(lessonArrayList);
    private SavedItemAdpater learnAdapter = new SavedItemAdpater(this);

    private SavedItemViewModel savedItemViewModel;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_saves:
                    viewPager.setCurrentItem(1);
                    return true;

                case R.id.navigation_contribution:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }
    };
    private String streamName="";
    private Context context;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        // Todo load ads
//        loadAds();

        loadingView = findViewById(R.id.loadingBar);
        loadingView.setVisibility(View.VISIBLE);

        sharedPref = context.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        //hide ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.hide(); }

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser != null) {
            checkProfile();
        } else {
            FirebaseAuth.getInstance().signOut();

            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    //new AuthUI.IdpConfig.FacebookBuilder().build(),
                    //new AuthUI.IdpConfig.TwitterBuilder().build(),
                    //new AuthUI.IdpConfig.EmailBuilder().build(),
                    //new AuthUI.IdpConfig.GoogleBuilder().build(),
                    new AuthUI.IdpConfig.PhoneBuilder().build()
                    );
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false,true)
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }

    }

    private void loadAds() {
        MobileAds.initialize(this, initializationStatus -> { });

        AdView adView = findViewById(R.id.adView);
        //Load Ad in adView
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("E56246F9159612F353BE9D2DECF13389")
                .build();
        adView.loadAd(adRequest);
    }

    private void checkProfile() {

        if (mUser.getDisplayName() == null || mUser.getDisplayName().trim().isEmpty()){

            Intent toEditProfile = new Intent(this,EditProfileActivity.class);
            startActivityForResult(toEditProfile,RC_EDIT_PROFILE);

        }
        else{
            // Initialize firestore
            db = FirebaseFirestore.getInstance();

            // update user last login details

            Map<String, Object> user = new HashMap<>();
            user.put("name", mUser.getDisplayName());
            user.put("id", mUser.getUid());
            user.put("email", mUser.getEmail());
            user.put("phone", mUser.getPhoneNumber());
            user.put("lastLogin",FieldValue.serverTimestamp());

            db.document("Users/"+mUser.getUid()).set(user);

            //Load UI
            loadUi();
        }
    }

    private void loadUi() {
        // BottomNavigationView
        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // viewPager
        viewPager = findViewById(R.id.main_viewpager);
        viewPager.setAdapter(new MainViewAdapter());
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setUserInputEnabled(false);

        loadData();
 }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CREATE_COURSE && resultCode == RESULT_OK){

            Course course = (Course) data.getParcelableExtra("course");

            db.collection("Courses").add(course)
                    .addOnSuccessListener(documentReference -> {
                        course.setId(documentReference.getId());
                        teachingsCourses.add(course);
                        teachAdapter.notifyDataSetChanged();
                    });
        }
        else if (requestCode == RC_SIGN_IN || requestCode == RC_EDIT_PROFILE) {
            if (resultCode == RESULT_OK) {
                mUser = FirebaseAuth.getInstance().getCurrentUser();
                checkProfile();
            } else {
                Toast.makeText(this, "SignIn canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void deleteSavedItem(SavedItem item) {

        new AlertDialog.Builder(this)
                .setTitle("Sure to delete?")
                .setMessage("Do you want to delete the " + item.getItemType() + " \""+item.getItemTitle() + "\" from your Device \nIt will be tougher to find again")
                .setPositiveButton("Confirm Delete", (dialog, whichButton) -> savedItemViewModel.delete(item))
                .setNegativeButton("Cancel",null)
                .create().show();

    }

    // ViewPager Adapter
    private class MainViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


            if (viewType == 0) {
                return new mainViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.tab_feed_view,
                                parent,
                                false
                        )
                );
            } else   if (viewType == 1){
                return new mainViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.tab_mylearnings_view,
                                parent,
                                false
                        )
                );
            }else {
                return new mainViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.tab_mycontribution_view,
                                parent,
                                false
                        )
                );
            }
        }

        @Override
        public void onBindViewHolder (@NonNull RecyclerView.ViewHolder holder,int position){

            if (position == 0){
                loadFeed(((mainViewHolder) holder).itemView);
            }else if (position == 1){
                loadMyLearning(((mainViewHolder) holder).itemView);
            }else {
                loadMyTeaching(((mainViewHolder) holder).itemView);
            }
        }

        @Override
        public int getItemCount () {
            return 3;
        }

        class mainViewHolder extends RecyclerView.ViewHolder {

            private mainViewHolder(@NonNull View itemView) {
                super(itemView);
            }

        }

    }

    private void loadMyTeaching(View v) {

        RecyclerView rvTeach = v.findViewById(R.id.recyclerView);
        // RecyclerView for teaching courses
        rvTeach.setHasFixedSize(true);
        rvTeach.setLayoutManager(new LinearLayoutManager(this));
        rvTeach.setAdapter(teachAdapter);

        FloatingActionButton fabCreate = v.findViewById(R.id.fab_contribution);
        fabCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent createCourseIntent = new Intent(MainActivity.this, CreateCourseActivity.class);
                createCourseIntent.putExtra("syllabus",streamSyllabus);

                startActivityForResult(createCourseIntent,RC_CREATE_COURSE);
            }
        });

    }

    // Load and display My Course Tab Details
    private void loadMyLearning(View v) {

        RecyclerView rvLearn = v.findViewById(R.id.recyclerView);

        // RecyclerView for learning courses
        rvLearn.setHasFixedSize(true);
        rvLearn.setLayoutManager(new LinearLayoutManager(this));
        rvLearn.setAdapter(learnAdapter);


    }// END myCourse

    // Load and display Feed Tab
    private void loadFeed(View v) {
        // RecyclerView vpFeatured = v.findViewById(R.id.rv_featured);
        RecyclerView rvFeed = v.findViewById(R.id.rv_feed);

        //vpFeatured.setAdapter(feedAdapter);
        //vpFeatured.setClipToPadding(true);
        //vpFeatured.setClipChildren(true);
        //vpFeatured.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));

        rvFeed.setHasFixedSize(true);
        rvFeed.setLayoutManager(new LinearLayoutManager(this));
        rvFeed.setAdapter(feedAdapter);

        db.document("Message/welcome").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        String message = document.getString("mainActivity");
                        message = message.replace("<user>",mUser.getDisplayName());
                        TextView textView =  v.findViewById(R.id.tv_hai);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            textView.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            textView.setText(Html.fromHtml(message));
                        }

                        //Checking app version
                        // and controlling usage
                        if (5L < document.getLong("minVersion")){
                            new AlertDialog.Builder(this)
                                    .setTitle("Update Cream Pen")
                                    .setMessage("You are using a version no longer support \n\nPlease update to latest version")
                                    .setNegativeButton("Cancel",(d,w) -> finish())
                                    .setPositiveButton("Update now", (dialog, which) ->{
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                                        finish();
                                    })
                                    .setCancelable(false)
                                    .show();
                        }
                        else if (5L < document.getLong("latestVersion")){

                            Snackbar.make(v, "New Update is available with more new features", Snackbar.LENGTH_LONG)
                                    .setAction("UPDATE NOW", view -> {
                                        try {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                                        } catch (android.content.ActivityNotFoundException e) {
                                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                                        }

                                    }).show();
                        }

                    }
                });

        // stream selector
        TextView tvStream = v.findViewById(R.id.tvStream);
        RecyclerView rvStream = v.findViewById(R.id.rv_stream);
        tvStream.setText(streamName);
        loadStream(rvStream);
        TextView streamChooser = v.findViewById(R.id.tvChangeStream);
        streamChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (streamList == null || streamList.isEmpty()){
                    Toast.makeText(MainActivity.this, "Loading Streams from Cloud Please Wait....", Toast.LENGTH_LONG).show();
                    loadingView.setVisibility(View.VISIBLE);
                    db.document("Streams/list").get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    streamList = (ArrayList<String>) task.getResult().get("names");
                                    Collections.sort(streamList);
                                    streamList.add("Others");
                                    chooseStream();
                                    loadingView.setVisibility(View.GONE);
                                }
                            });
                }else {
                    chooseStream();
                }
            }

            private void chooseStream() {
                AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setTitle("Select a stream");
                b.setItems(streamList.toArray(new String[0]), (dialog, which) -> {
                    dialog.dismiss();
                    if (!streamName.equals(streamList.get(which))) {
                        streamName = streamList.get(which);
                        Toast.makeText(MainActivity.this, "Showing Content for "+streamName, Toast.LENGTH_LONG).show();
                        tvStream.setText(streamName);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(getString(R.string.saved_stream_key), streamName);
                        editor.apply();
                        loadStream(rvStream);
                    }

                });
                b.show();
            }

        });

    }// END feed

    ArrayList<String> contentList = new ArrayList<>();
    String streamSyllabus ;
    private void loadStream(RecyclerView rvStream) {
        String streamPath = "Streams/"+streamName;

        loadingView.setVisibility(View.VISIBLE);
        db.document("Streams/"+streamName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        contentList = (ArrayList<String>) task.getResult().get("publicContent");

                        streamSyllabus = task.getResult().getString("syllabus");
                        if (streamSyllabus == null)
                            streamSyllabus = "Demo Subject 1{Main topic 1{sub topic1{}}}Demo Subject 2{Main topic 2{sub topic2{}}}";
                    }

                    rvStream.setHasFixedSize(true);
                    rvStream.setLayoutManager(new GridLayoutManager(this,1));
                    rvStream.setAdapter(new StreamContentAdapter(contentList,streamPath));

                    loadingView.setVisibility(View.GONE);
                });
    }// END stream

    private void loadData() {
        // Get lessons from cloud
        db.collection("Lessons")
                //.whereEqualTo("featured",true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                         for (DocumentSnapshot doc:task.getResult().getDocuments()) {

                            Lesson lesson =doc.toObject(Lesson.class);
                            lesson.setId(doc.getId());
                            lessonArrayList.add(lesson);

                        }
                        feedAdapter.notifyDataSetChanged();
                    }
                    loadingView.setVisibility(View.GONE);
                });

        // Get my courses from cloud
        db.collection("Courses").whereArrayContains("educatorIds", mUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for (DocumentSnapshot document:task.getResult().getDocuments()){
                            Course course = document.toObject(Course.class);//.withId(document.getId());
                            course.setId(document.getId());
                            teachingsCourses.add(course);
                        }
                        teachAdapter.notifyDataSetChanged();
                    }
                });

        // get courses details that the user learn from local phone memory
        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @SuppressWarnings("unchecked") @Override public <T extends ViewModel> T create(final Class<T> modelClass) {
                if (modelClass.equals(SavedItemViewModel.class)) {
                    return (T) new SavedItemViewModel(getApplication());
                } else {
                    return null;
                }
            }
        };
        savedItemViewModel = new ViewModelProvider(this,factory).get(SavedItemViewModel.class);
        savedItemViewModel.getAllItems().observe(this, items -> learnAdapter.setItems(items));

        streamName = sharedPref.getString(getString(R.string.saved_stream_key),"No Stream Selected");

    }
}