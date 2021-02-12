package com.parayada.creampen.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parayada.creampen.R;

import java.io.File;
import java.util.ArrayList;

public class

ChooseImageActivity extends AppCompatActivity {

    private static final int REQUEST_STORAGE_PERMISSION = 200;
    Context mContext;
    ImageAdapter imageAdapter = new ImageAdapter();
    ArrayList<Uri> images;
    ArrayList<Uri> selectedImages = new ArrayList<>();
    private String [] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_image);
        mContext = this;


        int permissionWriteExternal = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // Read/Write Permission
        if (permissionWriteExternal != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_STORAGE_PERMISSION);
        }else {
            loadUiandImages();
        }
    }

    private void loadUiandImages() {
        images = getAllShownImagesPath(this);

        RecyclerView rv = findViewById(R.id.rv_choose_image);

        rv.setLayoutManager(new GridLayoutManager(this,4));
        rv.setHasFixedSize(true);
        rv.setAdapter(imageAdapter);

        Button btnSelect = findViewById(R.id.btnSelect);

        btnSelect.setOnClickListener(v -> {
            if (selectedImages.size() ==  0){
                Toast.makeText(mContext,"You din't choose any image",Toast.LENGTH_LONG).show();
            }else {
                Intent data = new Intent();
                data.putExtra("images",selectedImages);
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        });
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

        @NonNull
        @Override
        public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(mContext).inflate(
                    R.layout.image_view,
                    parent,
                    false
            ));

        }

        @Override
        public void onBindViewHolder(@NonNull final ImageAdapter.ViewHolder holder, int position) {
            final Uri uri = images.get(position);
            Glide.with(mContext)
                    .load(uri)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(v -> {
                if (selectedImages.contains(uri)){
                    selectedImages.remove(uri);
                    holder.countView.setVisibility(View.GONE);
                    imageAdapter.notifyDataSetChanged();

                }else {
                    selectedImages.add(uri);
                    holder.countView.setText(String.valueOf(1+selectedImages.indexOf(uri)));
                    holder.countView.setVisibility(View.VISIBLE);

                }

            });

            if (selectedImages.contains(uri)){
                holder.countView.setVisibility(View.VISIBLE);
                holder.countView.setText(String.valueOf(1+selectedImages.indexOf(uri)));

            }else {
                holder.countView.setVisibility(View.GONE);

            }
        }

        @Override
        public int getItemCount() {
            if (images != null) return images.size();
            return 0;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView countView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.imageSlide);
                countView = itemView.findViewById(R.id.tvImaCount);

                ViewGroup.LayoutParams params = imageView.getLayoutParams();
                params.height = 200;
                itemView.setLayoutParams(params);
            }
        }
    }

    private ArrayList<Uri> getAllShownImagesPath(Activity activity) {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<Uri> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = activity.getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);

            Uri imageUri = Uri.fromFile(new File(absolutePathOfImage));
            listOfAllImages.add(0,imageUri);
        }
        return listOfAllImages;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_STORAGE_PERMISSION){
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED){
                loadUiandImages();
            }else {
               finish();
            }
        }

    }

}