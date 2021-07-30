package com.parayada.creampen.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.StreamEncoder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.parayada.creampen.R;

import java.io.InputStream;

public class EditProfileActivity extends AppCompatActivity {

    ImageView ivProfile;
    EditText etProfileName;
    Button btnSaveProfile;

    FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        mUser = FirebaseAuth.getInstance().getCurrentUser();

        ivProfile  = findViewById(R.id.iv_profile_image);
        etProfileName  = findViewById(R.id.et_profile_name);
        btnSaveProfile = findViewById(R.id.btn_save);

        etProfileName.setText(mUser.getDisplayName());
        if(mUser.getPhotoUrl() == null){
            Glide.with(this)
                    .load("https://api.adorable.io/avatars/300/"+mUser.getUid())
                    //.load("https://api.adorable.io/avatars/300/"+mUser.getUid())
                    //.load("https://avatars.dicebear.com/api/avataaars/"+ mUser.getUid() + ".svg")
                    .into(ivProfile);
        }else {
            Glide.with(this)
                    .load(mUser.getPhotoUrl())
                    .into(ivProfile);
        }
        btnSaveProfile.setOnClickListener(v -> {

            String name = etProfileName.getText().toString().trim();
            if (name.isEmpty()){
                Toast.makeText(this,"Please Enter Your Name",Toast.LENGTH_LONG).show();
            }else{
                v.setEnabled(false);

                UserProfileChangeRequest.Builder profileBuilder = new UserProfileChangeRequest.Builder();

                profileBuilder.setDisplayName(name);
                if (mUser.getPhotoUrl() == null)
                    profileBuilder.setPhotoUri(Uri.parse("https://api.adorable.io/avatars/300/" + mUser.getUid()));

                mUser.updateProfile(profileBuilder.build())
                        .addOnCompleteListener(task -> {
                            setResult(RESULT_OK);
                            finish();
                        });

            }

        });
    }
}
