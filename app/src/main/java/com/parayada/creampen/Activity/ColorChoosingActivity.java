package com.parayada.creampen.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.parayada.creampen.R;

public class ColorChoosingActivity extends AppCompatActivity {


    String[] colorArray;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_choosing);

        colorArray = getResources().getStringArray(R.array.colorsArray);

        RecyclerView rvColor = findViewById(R.id.rv_choose_color);
        rvColor.setHasFixedSize(true);
        rvColor.setLayoutManager(new GridLayoutManager(this,4));
        rvColor.setAdapter(new ColorAdapter());
    }

    private class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ViewHolder> {
        @NonNull
        @Override
        public ColorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.color_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ColorAdapter.ViewHolder holder, int position) {

            holder.tv_color.setBackgroundColor(Color.parseColor(colorArray[position]));
            holder.tv_color.setText(colorArray[position]);

            holder.tv_color.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent data = new Intent();
                    data.putExtra("color",colorArray[position]);
                    setResult(RESULT_OK,data);

                    finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return colorArray.length;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView tv_color;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                tv_color = itemView.findViewById(R.id.tv_color);
            }
        }
    }
}
