package com.parayada.creampen.Activity;

import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;

import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.parayada.creampen.Adapter.SliderAdapter;
import com.parayada.creampen.Animation.ZoomOutPageTransformer;
import com.parayada.creampen.Model.Lesson;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.R;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RecordActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    boolean permissionToRecordAccepted = false;

    private static int sTime =0, eTime =0;
    private static String fileName = null;

    int nextChange = 0;
    long timeInMilliseconds = 0L, timeSwapBuff = 0L, updatedTime = 0L;
    private ArrayList<String> slideList;
    private Context mContext;
    private ViewPager2 viewPager;
    private ImageButton mPreviousBtn, mNextBtn, playbtn, recordbtn, uploadbtn;
    private MediaPlayer mPlayer;

    private TextView timeView;
    private SeekBar songPrgs;
    private Handler hdlr = new Handler();
    private ArrayList<String > slideChangeList = new ArrayList<>();
    private MediaRecorder recorder = null;

    // Requesting permission to RECORD_AUDIO
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private boolean isRecording = false;
    private boolean isPlaying = false;
    private boolean isUploading = false;
    private long startHTime = 0L;
    private String title;
    private int slideCount;
    private int uploads = 0;

    private ArrayList<String> slideListForFirebase = new ArrayList<>();
    private View pauseView;
    private Runnable UpdateSongTime = new Runnable() {
        @Override
        public void run() {

            sTime = mPlayer.getCurrentPosition();
            songPrgs.setProgress(sTime);

            if( nextChange < slideChangeList.size()){
                String[] nextC = slideChangeList.get(nextChange).split(",");
                if ( Integer.parseInt(nextC[0]) <= (sTime+500) ){

                    viewPager.setCurrentItem(Integer.parseInt(nextC[1]));
                    nextChange ++;
                }
            }

            hdlr.postDelayed(this, 500);
            if (sTime >= eTime){
                uploadbtn.setEnabled(true);
                uploadbtn.setVisibility(View.VISIBLE);
                hdlr.removeCallbacks(UpdateSongTime);
            }
        }
    };
    private Runnable recordingTime = new Runnable() {
        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;


            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            if (timeView != null)
                timeView.setText("" + String.format("%02d", mins) + ":" + String.format("%02d", secs));
            hdlr.postDelayed(this, 100);
        }
    };
    private FirebaseUser mUser;

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recorder.start();

        startHTime = SystemClock.uptimeMillis();
        hdlr.postDelayed(recordingTime, 0);
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        timeSwapBuff += timeInMilliseconds;
        hdlr.removeCallbacks(recordingTime);


        // Prepare mPlayer for play
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
            eTime = mPlayer.getDuration();

            sTime = mPlayer.getCurrentPosition();
            songPrgs.setMax(eTime);

            songPrgs.setProgress(sTime);
        } catch (IOException e) {
            e.printStackTrace();
        }


        //ide view
        pauseView.setVisibility(View.GONE);
        recordbtn.setVisibility(View.GONE);
        mPreviousBtn.setVisibility(View.GONE);
        mNextBtn.setVisibility(View.GONE);

        //sow view
        playbtn.setVisibility(View.VISIBLE);
        playbtn.setEnabled(true);
        songPrgs.setVisibility(View.VISIBLE);
        songPrgs.setEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        mContext = this;
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        //Getting a ArrayList of object from one Activity (Studio Activity)
        //Bundle args = intent.getBundleExtra("slideBundle");
        //slideList = (ArrayList<Object>) args.getSerializable("ARRAYLIST");
        //title = args.getString("title");
        slideList = intent.getStringArrayListExtra("slideList");
        title = intent.getStringExtra("title");

        Toast.makeText(mContext,title,Toast.LENGTH_LONG).show();


        viewPager = findViewById(R.id.recordpager);
        mPreviousBtn = findViewById(R.id.pre_button);
        mNextBtn = findViewById(R.id.next_button);
        uploadbtn = findViewById(R.id.upload_button);
        recordbtn = (ImageButton) findViewById(R.id.btnrecord);
        playbtn = (ImageButton)findViewById(R.id.btnPlay);
        timeView = (TextView) findViewById(R.id.timeView);
        songPrgs = (SeekBar)findViewById(R.id.sBar);
        pauseView = findViewById(R.id.pauseView);

        songPrgs.setClickable(false);
        playbtn.setEnabled(false);


        playbtn.setVisibility(View.GONE);
        songPrgs.setVisibility(View.GONE);
        timeView.setText(slideList.size() + " Slide");




        // Load slides in ViewPager2
        viewPager.setAdapter(new SliderAdapter(slideList));

        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(1);
        viewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        viewPager.setUserInputEnabled(false);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
  /*    CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(10));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });*/



        // Set up the user interaction to manually show or hide the system UI.
   /*     mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              toggle();
            }
        });*/

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //      mControlsView.setOnTouchListener(mDelayHideTouchListener);

        //Previous Slide
        mPreviousBtn.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);

                if (isRecording){
                    slideChangeList.add(updatedTime + "," + viewPager.getCurrentItem());
                }
            }
        });
        //Next Slide
        mNextBtn.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() != (slideList.size()-1)) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                if (isRecording) {
                    slideChangeList.add(updatedTime + "," + viewPager.getCurrentItem());
                }
            }
        });

        // Record to the external storage for visibility


//        File audioFile = new File(Environment.getExternalStorageDirectory().getPath(), "CreamPen/Audio/" + lesson.getId() + ".mp3");



        // Get access to audio file
        File audioFileDir = new File(Environment.getExternalStorageDirectory().getPath(), "CreamPen/Uploads/Audio");
        if (!audioFileDir.exists())
            audioFileDir.mkdirs();
        fileName = audioFileDir.getAbsolutePath() +"/"+ title+".mp3";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        recordbtn.setOnClickListener(v -> {
            if (isRecording) {
                // code to pause
                isRecording = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    recorder.pause();
                    recordbtn.setEnabled(false);
                    hdlr.removeCallbacks(recordingTime);
                    pauseView.setVisibility(View.VISIBLE);
                }else {
                    stopRecording();
                }
            }else {
                isRecording = true;
                startRecording();
                slideChangeList.add("0," + viewPager.getCurrentItem());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    recordbtn.setImageResource(R.drawable.pause);
                }else{
                    recordbtn.setImageResource(R.drawable.stop);
                }
            }
        });

        findViewById(R.id.stop).setOnClickListener(v -> stopRecording());
        findViewById(R.id.resume).setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                pauseView.setVisibility(View.GONE);
                recorder.resume();
                recordbtn.setEnabled(true);
                startHTime = SystemClock.uptimeMillis() - updatedTime;
                hdlr.post(recordingTime);
                isRecording = true;
            }
        });

        playbtn.setOnClickListener(v -> {
            if (isPlaying){
                mPlayer.pause();
                hdlr.removeCallbacks(UpdateSongTime);
                isPlaying = false;
                playbtn.setImageResource(R.drawable.play);

            }else{
                mPlayer.start();
                hdlr.postDelayed(UpdateSongTime, 0);
                isPlaying = true;
                playbtn.setImageResource(R.drawable.pause);
            }
        });
        uploadbtn.setOnClickListener(v -> {

            isUploading = true;

            uploadbtn.setEnabled(false);
            mPreviousBtn.setEnabled(false);
            mNextBtn.setEnabled(false);
            recordbtn.setEnabled(false);
            playbtn.setEnabled(false);
            songPrgs.setEnabled(false);

            final StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            // Structure slidelist
            slideCount = slideList.size();

            //sow uploadin proress in existin bar
            songPrgs.setMax(slideCount+2);
            songPrgs.setProgress(0);

            uploads = 0;
            for (int i= 0; i<slideList.size(); i++){
                if (String.valueOf(slideList.get(i)).substring(0,3).equals("100")){
                    // Create a child reference imagesRef now points to "images"
                    final StorageReference imagesRef = storageRef.child("Slide/" + mUser.getUid() +"/"+ System.currentTimeMillis());
                    // Prefix code 100 is for pic slides path
                    slideListForFirebase.add("100" + imagesRef.getPath());

                    //UploadTask task = imagesRef.putFile(Uri.parse(String.valueOf(slideList.get(i)).substring(3)));
                    UploadTask task = imagesRef.putFile(CompressImage(Uri.parse(String.valueOf(slideList.get(i)).substring(3))));
                    final int finalI = i;

                    task.continueWithTask(task1 -> {
                        if (!task1.isSuccessful()) {
                            throw task1.getException();
                        }
                        // Continue with the task to get the download URL
                        return imagesRef.getDownloadUrl();
                    }).addOnCompleteListener(task12 -> {
                        if (task12.isSuccessful()) {
                            Uri downloadUri = task12.getResult();
                            slideListForFirebase.set(finalI,"100" + downloadUri);
                            slideList.set(finalI,"100" + downloadUri);
                            uploads++;
                            Toast.makeText(mContext,"Uploaded " + uploads + " Slides",Toast.LENGTH_LONG).show();
                            songPrgs.setProgress(uploads);
                            if (uploads == slideCount)
                                uploadAudio(storageRef);
                        }
                    });
                }
                else if(String.valueOf(slideList.get(i)).substring(0,3).equals("101")) {
                    slideListForFirebase.add(slideList.get(i));
                    uploads++;
                    Toast.makeText(mContext, "Uploaded " + uploads + " Slides", Toast.LENGTH_LONG).show();
                    songPrgs.setProgress(uploads);
                    if (uploads == slideCount) {
                        uploadAudio(storageRef);
                    }
                }
                else if (String.valueOf(slideList.get(i)).substring(0,3).equals("102")){
                    slideListForFirebase.add((String) slideList.get(i));
                    uploads ++;
                    Toast.makeText(mContext,"Uploaded " + uploads+ " Slides",Toast.LENGTH_LONG).show();
                    songPrgs.setProgress(uploads);
                    if (uploads == slideCount ) {
                        uploadAudio(storageRef);
                    }
                }
            }
        });

        songPrgs.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                timeView.setText(String.format("%d:%d ", TimeUnit.MILLISECONDS.toMinutes(progress),
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

                        int currentChangeIndex = slideChangeList.indexOf(c);

                        currentItem = Integer.parseInt(change[1]);
                        nextChange  = currentChangeIndex + 1;
                    }
                }
                viewPager.setCurrentItem(currentItem);



            }
        });

    }

    private Uri CompressImage(Uri mFileUri) {

        FileDescriptor fileDescriptor = getFileDiscripterFromURI(mFileUri);

        Bitmap scaledBitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 540.0f;
        float maxWidth = 960.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitma
            bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor,null,options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        Matrix matrix = new Matrix();
        try {
            exif = new ExifInterface(String.valueOf(mFileUri));//(fileDescriptor);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodError e){
            e.printStackTrace();
        }

        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                true);

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return Uri.fromFile(new File(filename));
    }

    private FileDescriptor getFileDiscripterFromURI(Uri mFileUri){
        ParcelFileDescriptor mInputPFD = null;
        try {
            mInputPFD = getContentResolver().openFileDescriptor(mFileUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileDescriptor fileDescriptor = mInputPFD.getFileDescriptor();

        return fileDescriptor;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public String getFilename() {

        File file = new File(Environment.getExternalStorageDirectory().getPath(), "CreamPen/Uploads/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private void uploadAudio(StorageReference storageRef){//,ArrayList<String> slideListForFirebase) {

        //Upload audiofile
        final StorageReference audioRef =
                storageRef.child("Audio/"+ mUser.getUid() +"/"+ System.currentTimeMillis()+".3gp");
        UploadTask  test = audioRef.putFile(Uri.fromFile(new File(fileName)));
        test.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            // Continue with the task to get the download URL
            return audioRef.getDownloadUrl();
        })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(mContext,"Uploaded Audio",Toast.LENGTH_LONG).show();
                        songPrgs.setProgress(++uploads);
                        uploadLesson(task.getResult());
                    }
                });

    }

    private void uploadLesson(Uri audioLink) {

        // Access a Cloud Firestore instance from your Activity
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        String mysep = getResources().getString(R.string.mySeparator);
        final String courseId = getIntent().getStringExtra("courseId");
        String courseTitle = getIntent().getStringExtra("courseTitle");

        final Lesson lesson = new Lesson();
        lesson.setAudioLink(audioLink.toString());
        lesson.setSlideArrayList(slideList);
        lesson.setSlideChangeList(slideChangeList);
        lesson.setTitle(title);
        lesson.setUserPic(mUser.getPhotoUrl().toString());
        lesson.setUserName(mUser.getDisplayName());
        lesson.setUserId(mUser.getUid());
        lesson.setCourseId(courseId);
        lesson.setCourseTitle(courseTitle);
        lesson.setTopics(getIntent().getStringArrayListExtra("chosenTopics"));

        db.collection("Lessons").add(lesson)
                .addOnFailureListener(e -> {
                    Toast.makeText(mContext,"Uploadin lesson is Failure",Toast.LENGTH_LONG).show();
                    uploadbtn.setEnabled(true);
                    playbtn.setEnabled(true);
                })
                .addOnSuccessListener(documentReference -> {
                    String lessonTypeIdName = getResources().getString(R.string.CodeLesson) +
                            getResources().getString(R.string.mySeparator)+
                            documentReference.getId()+
                            getResources().getString(R.string.mySeparator) +
                            lesson.getTitle();
                    db.document("Courses/"+courseId)
                            .update("lessons", FieldValue.arrayUnion(lessonTypeIdName));

                    Toast.makeText(mContext,"Uploading lesson is Success",Toast.LENGTH_LONG).show();
                    songPrgs.setProgress(++uploads);

                    /////////////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////
                    // delete the savedPreference lesson draft
                    SharedPreferences sharedPref = this.getSharedPreferences(courseId, Context.MODE_PRIVATE);
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
                        for (int i = 0; i < lesson.getTopics().size(); i++)
                            editor.putString(getString(R.string.topic) + i, "Cat->Main->sub");

                        //Apply edits
                        editor.putBoolean(getString(R.string.hasSaved), false);
                        editor.apply();
                    }
                    ////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////

                    setResult(RESULT_OK);
                    finish();
                    Toast.makeText(this, "Lesson Uploading Completed", Toast.LENGTH_SHORT).show();

                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    @Override
    public void onBackPressed() {

        if (isRecording){
            Toast.makeText(mContext, "Please stop recording first", Toast.LENGTH_SHORT).show();
        }
        else if (isUploading){
            setResult(RESULT_OK);
            super.onBackPressed();
        }
        else {
            new AlertDialog.Builder(this)
                    .setTitle("Go Back?")
                    .setMessage("All your recording wil be lost unless you upload it")
                    .setPositiveButton("Record and Upload ", null)
                    .setNegativeButton("Edit Slide", (dialog, which) -> {
                        super.onBackPressed();
                        if (isPlaying)
                            mPlayer.stop();
                    })
                    .create().show();
        }
    }
}