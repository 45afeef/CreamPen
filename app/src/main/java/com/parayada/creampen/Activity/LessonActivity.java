    package com.parayada.creampen.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

import android.os.PersistableBundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.parayada.creampen.Adapter.SliderAdapter;
import com.parayada.creampen.Animation.ZoomOutPageTransformer;
import com.parayada.creampen.Model.Lesson;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.Model.SavedItem;
import com.parayada.creampen.R;
import com.parayada.creampen.Room.SavedItemViewModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LessonActivity extends AppCompatActivity {


    private final int RC_STORAGE_PERMISSION = 200;
    private String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private final int AUTO_HIDE_DELAY_MILLIS = 3000;


    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private final int UI_ANIMATION_DELAY =  500;
    private final Handler mHandler = new Handler();
    private boolean isPlaying = false;
    private SeekBar mSeekBar;
    private ProgressBar mLoadingBar;
    private int sTime =0, eTime =0;
    private ArrayList<String> slideChangeList;
    private int nextChange = 0;
    private TextView mStartTime;
    private TextView mSongTime;

    private MediaPlayer mPlayer;
    private ViewPager2 viewPager;
    private ImageButton mControlButton;
    private Lesson lesson;

    private StorageReference audioRef;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            viewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    //     | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = () -> hide();
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = (view, motionEvent) -> {
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) { actionBar.hide(); }

        //Todo ads
//        loadAds();

        // Initialize views
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        viewPager = findViewById(R.id.fullscreen_content);
        mControlButton = findViewById(R.id.btnControl);
        mControlButton.setEnabled(false);
        mLoadingBar = findViewById(R.id.loadingControl);
        mSeekBar = findViewById(R.id.seekBar);
        mStartTime = findViewById(R.id.txtStartTime);
        mSongTime = findViewById(R.id.txtSongTime);
        mPlayer = new MediaPlayer();

        int permissionWriteExternal = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // Read/Write Permission
        if (permissionWriteExternal != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, RC_STORAGE_PERMISSION);
        }else {
            loadUi();
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

    private void loadUi() {

        // Load slides in ViewPager2
        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        // viewPager.setOffscreenPageLimit(1);
        viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_ALWAYS);
        viewPager.setUserInputEnabled(true);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        // Set up the user interaction to manually show or hide the system UI.
        viewPager.getChildAt(0).setOnTouchListener(new View.OnTouchListener() {
            float x,y;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        x = event.getX();
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float lx = event.getX();
                        float ly = event.getY();
                        if (Math.abs(lx-x) < 20 && Math.abs(ly-y)<20)
                            toggle();
                        break;
                }
                return false;
            }
        });


        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        mControlButton.setOnTouchListener(mDelayHideTouchListener);
        mSeekBar.setOnTouchListener(mDelayHideTouchListener);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                mStartTime.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(progress),
                        TimeUnit.MILLISECONDS.toSeconds(progress) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int pro = seekBar.getProgress();
                mPlayer.seekTo(pro);


                int currentItem=0;
                for (String c:slideChangeList){

                    String[] change = c.split(",");
                    if (pro >= Integer.parseInt(change[0])){

  //                      int currentChangeIndex = slideChangeList.indexOf(c);

                        currentItem = Integer.parseInt(change[1]);
//                        nextChange  = currentChangeIndex + 1;
                        nextChange  = slideChangeList.indexOf(c) + 1;
                    }
                }
                viewPager.setCurrentItem(currentItem);
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("lesson")) {
            lesson = intent.getParcelableExtra("lesson");
            if (lesson!= null)loadLessons();
        }
        else if(intent.hasExtra("lessonId")){
            String lessonId =intent.getStringExtra("lessonId");
            FirebaseFirestore.getInstance().document("Lessons/"+lessonId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            lesson = task.getResult().toObject(Lesson.class);
                            if (lesson != null) {
                                if (lesson.getId() == null)
                                    lesson.setId(task.getResult().getId());
                                loadLessons();
                            }
                            else {
                                Toast.makeText(this,"Can't find Resource",Toast.LENGTH_LONG).show();
                                startActivity(new Intent(this,MainActivity.class));
                                finish();
                            }

                        }
                    });
        }
        else {
            Toast.makeText(this,"Can't find Resource",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
    }

    private void loadLessons() {

        loadAudio();
        loadSaveButtonUi();

        viewPager.setAdapter(new SliderAdapter(lesson.getSlideArrayList()));

        slideChangeList = lesson.getSlideChangeList();
        mControlButton.setOnClickListener(v -> {
            if (isPlaying){
                mPlayer.pause();
                isPlaying = false;
                mControlButton.setImageResource(R.drawable.play);
            }else {
                isPlaying = true;
                mControlButton.setImageResource(R.drawable.pause);

                mPlayer.start();
                mHandler.postDelayed(UpdateSongTime, 100);
                mSeekBar.setProgress(sTime);
            }
        });

    }

    private void loadAudio(){
        File audioFile =
           //new File(Environment.getExternalStorageDirectory().getPath(), "CreamPen/Audio/" + lesson.getId() + ".mp3");
           new File(getCacheDir().getAbsolutePath() + lesson.getId() +".mp3");

        if (audioFile.exists()) {
            prepareAudio(audioFile);
        }else {
            downloadAudio(audioFile);
        }
    }

    private void loadSaveButtonUi() {

        // Save course or lesson or bot if t use need for future
        Button saveLessonBtn= findViewById(R.id.btnSaveLesson);
        Button saveCourseBtn = findViewById(R.id.btnSaveCourese);

        ViewModelProvider.Factory factory = new ViewModelProvider.Factory() {
            @SuppressWarnings("unchecked") @Override public <T extends ViewModel> T create(final Class<T> modelClass) {
                if (modelClass.equals(SavedItemViewModel.class)) {
                    return (T) new SavedItemViewModel(getApplication());
                } else {
                    return null;
                }
            }
        };

        SavedItemViewModel mViewModel = new ViewModelProvider(this,factory).get(SavedItemViewModel.class);

        SavedItem savedLesson = new SavedItem();
        savedLesson.setItemId(lesson.getId());
        savedLesson.setItemTitle(lesson.getTitle());
        savedLesson.setItemType("Lesson");

        mViewModel.getItemByIdAndType(savedLesson).observe(this, item -> {
            if (item ==null ){
                saveLessonBtn.setVisibility(View.VISIBLE);
                saveLessonBtn.setOnClickListener(v -> mViewModel.insert(savedLesson));
            }else {
                saveLessonBtn.setVisibility(View.GONE);
            }
        });

        SavedItem savedCourse = new SavedItem();
        savedCourse.setItemType("Course");
        savedCourse.setItemTitle(lesson.getCourseTitle());
        savedCourse.setItemId(lesson.getCourseId());

        mViewModel.getItemByIdAndType(savedCourse).observe(this, item -> {
            if (item ==null ){
                saveCourseBtn.setVisibility(View.VISIBLE);
                saveCourseBtn.setOnClickListener(v -> mViewModel.insert(savedCourse));
            }else {
                saveCourseBtn.setVisibility(View.GONE);
            }
        });

    }

    /*private void loadSlides(ArrayList<String> slideArrayList) {

        for (final String s:slideArrayList){
            String type = s.substring(0,3);
            switch (type){
                //Slide Code 100 is for imaes
                case "100":
                    slideList.add(s);//.substring(3));
                    break;
                //Slide Code 101 is for questionSet
                case "101":
                    slideList.add(s.substring(3));
                    break;
                //Slide Code 102 is for text
                case "102":
                    slideList.add(s);//.substring(3));
                    break;
            }
        }
        pagerAdapter.notifyDataSetChanged();
    }*/

    private void downloadAudio(File audioFile) {

        // Create a reference from an HTTPS URL
        // Note that in the URL, characters are URL escaped!
        audioRef = FirebaseStorage.getInstance().getReferenceFromUrl(lesson.getAudioLink());

        /*
        File audioFileDir = new File(Environment.getExternalStorageDirectory().getPath(), "CreamPen/Audio");

        if (!audioFileDir.exists())
            audioFileDir.mkdirs();*/

        audioRef.getFile(audioFile)
                .addOnSuccessListener(taskSnapshot -> {
                    // Local temp file has been created
                    prepareAudio(audioFile);
                });
    }

    private void prepareAudio(File audioFile) {
        Uri audio = Uri.fromFile(audioFile);

        try {
            mPlayer.setDataSource(this,audio);
            mPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "audio downloaded", Toast.LENGTH_SHORT).show();
        }
        mPlayer.setOnPreparedListener(mp -> {
            delayedHide(1500);

            eTime = mPlayer.getDuration();

            sTime = mPlayer.getCurrentPosition();
            mSeekBar.setMax(eTime);
            mSongTime.setText(String.format("/%d:%d", TimeUnit.MILLISECONDS.toMinutes(eTime),
                    TimeUnit.MILLISECONDS.toSeconds(eTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(eTime))));
            mStartTime.setText(String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(sTime),
                    TimeUnit.MILLISECONDS.toSeconds(sTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(sTime))));

            //Enable control button
            mControlButton.setEnabled(true);
            mControlButton.setVisibility(View.VISIBLE);
            mLoadingBar.setVisibility(View.GONE);

            //start playing
            isPlaying = true;
            mControlButton.setImageResource(R.drawable.pause);

            mPlayer.start();
            mHandler.postDelayed(UpdateSongTime, 100);
            mSeekBar.setProgress(sTime);
        });

    }

    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {
            sTime = mPlayer.getCurrentPosition();
            mSeekBar.setProgress(sTime);

            if( nextChange < slideChangeList.size()){
                String[] nextC = slideChangeList.get(nextChange).split(",");
                if ( Integer.parseInt(nextC[0]) <= (sTime+500) ){
                    viewPager.setCurrentItem(Integer.parseInt(nextC[1]));
                    nextChange ++;
                }
            }

            mHandler.postDelayed(this, 500);
            if (sTime >= eTime){
                nextChange = 0;
                show();
                mHandler.removeCallbacks(UpdateSongTime);

                isPlaying = false;
                mControlButton.setImageResource(R.drawable.play);
            }
        }
    };

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(UpdateSongTime);
        mPlayer.stop();
        mPlayer.release();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_STORAGE_PERMISSION){
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED){
                loadUi();
            }else {
                finish();
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // If there's a download in progress, save the reference so you can query it later
        if (audioRef != null) {
            outState.putString("reference", audioRef.toString());
        }

        outState.putInt("progress",mPlayer.getCurrentPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was a download in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef != null) {
            audioRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

            // Find all DownloadTasks under this StorageReference (in this example, there should be one)
            List<FileDownloadTask> tasks = audioRef.getActiveDownloadTasks();
            if (tasks.size() > 0) {
                // Get the task monitoring the download
                FileDownloadTask task = tasks.get(0);

                // Add new listeners to the task using an Activity scope
                task.addOnSuccessListener(this, state -> {
                    // Success!
                    // ...


                    prepareAudio(
                            //new File(Environment.getExternalStorageDirectory().getPath(), "CreamPen/Audio/" + lesson.getId() + ".mp3")
                            new File(getCacheDir().getAbsolutePath() + lesson.getId() + ".mp3"));
                });
            }
        }

        mPlayer.seekTo(savedInstanceState.getInt("progress"));
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHandler.removeCallbacks(mShowPart2Runnable);
        mHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    // @SuppressLint("InlinedApi")
    private void show() {

        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHandler.removeCallbacks(mHidePart2Runnable);
        mHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
        //delayedHide(5000);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHandler.removeCallbacks(mHideRunnable);
        mHandler.postDelayed(mHideRunnable, delayMillis);
    }
}