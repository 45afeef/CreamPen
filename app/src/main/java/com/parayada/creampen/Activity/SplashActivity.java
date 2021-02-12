package com.parayada.creampen.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().getAction() == Intent.ACTION_MAIN){
            openMainActivity();}
        else if (getIntent().getAction() == Intent.ACTION_VIEW) {

            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnFailureListener(this,e->openMainActivity())
                    .addOnSuccessListener(this, pendingDynamicLinkData -> {
                        // Get deep link from result (may be null if no link is found)
                        if (pendingDynamicLinkData != null) {
                            Uri deepLink = pendingDynamicLinkData.getLink();
                            List<String > segments = deepLink.getPathSegments();
                            Intent intent;
                            switch (segments.get(0)){
                                case "Courses":
                                    //https://creampen.com/Courses/g4fNIfpfionA3derpitQ
                                    intent = new Intent(SplashActivity.this,CourseActivity.class);
                                    intent.putExtra("courseId",segments.get(1));
                                    startActivity(intent);
                                    finish();
                                    break;
                                case "Lessons":
                                    //https://creampen.com/Lessons/g4fNIfpfionA3derpitQ
                                    intent = new Intent(SplashActivity.this,LessonActivity.class);
                                    intent.putExtra("lessonId",segments.get(1));
                                    startActivity(intent);
                                    finish();
                                    break;
                                case "Quizzes":
                                    //https://creampen.com/Quizzes/g4fNIfpfionA3derpitQ

                                    intent = new Intent(SplashActivity.this,ExamActivity.class);
                                    intent.putExtra("quizId",segments.get(1));
                                    startActivity(intent);
                                    finish();
                                    break;
                            }
                        }
                        else {openMainActivity();}
                    });
        }

    }

    private void openMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
