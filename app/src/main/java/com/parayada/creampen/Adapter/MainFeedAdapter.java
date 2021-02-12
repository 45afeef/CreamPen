package com.parayada.creampen.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parayada.creampen.Activity.LessonActivity;
import com.parayada.creampen.Model.Lesson;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.R;

import java.util.ArrayList;

public class MainFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Lesson> lessons;
    Context mContext;

    public MainFeedAdapter(ArrayList<Lesson> lessonArrayList) {
        this.lessons = lessonArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_lesson, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        Lesson lesson = lessons.get(pos);

        ((ImageViewHolder) holder).tvEducatorName.setText(lesson.getUserName());
        Glide.with(mContext)
                .load(lesson.getUserPic())
                .into(((ImageViewHolder) holder).ivProfile);
        ((ImageViewHolder) holder).titleView.setText(lesson.getTitle());

        try {
            String topics = lesson.getTopics().toString();
            ((ImageViewHolder) holder).tvTopics.setText(topics.substring(1,topics.length()-1));
        }catch (Exception e){
            e.printStackTrace();
        }

        ((ImageViewHolder) holder).itemView.setOnClickListener(v -> {
            Intent toLessonsIntent = new Intent(mContext, LessonActivity.class);

            // Bundle args = new Bundle();
            //args.putParcelable("lesson", lessons.get(position));

            //toLessonsIntent.putExtra("bundle", args);
            toLessonsIntent.putExtra("lesson", lesson);

            mContext.startActivity(toLessonsIntent);

        });

        String firstSlide = lesson.getSlideArrayList().get(0);
        switch (firstSlide.substring(0,3)){
            //Slide Code 100 is for imaes
            case "100":
                ((ImageViewHolder) holder).slideImageView.setVisibility(View.VISIBLE);
                ((ImageViewHolder) holder).textView.setVisibility(View.GONE);
                //((ImageViewHolder) holder).plainText.setVisibility(View.GONE);

                Glide.with(mContext)
                        .load(firstSlide.substring(3))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        //.placeholde(new ColorDrawable(Color.WHITE))
                        .fallback(new ColorDrawable(Color.GRAY))
                        .into(((ImageViewHolder) holder).slideImageView);
                break;
            //Slide Code 101 is for questionSet
            case "101":
                ((ImageViewHolder) holder).textView.setVisibility(View.VISIBLE);
                ((ImageViewHolder) holder).slideImageView.setVisibility(View.INVISIBLE);
                //((ImageViewHolder) holder).plainText.setVisibility(View.GONE;

                McqSet mcqSet = new McqSet(firstSlide.substring(3));

                ((ImageViewHolder) holder).textView.setText("? "+mcqSet.getQuestion());
                ((ImageViewHolder) holder).textView.setBackgroundColor(mContext.getResources().getColor(R.color.black));

                break;
            // Slide Code 102 is for Html Text
            case "102":
                ((ImageViewHolder) holder).textView.setVisibility(View.VISIBLE);
                ((ImageViewHolder) holder).slideImageView.setVisibility(View.INVISIBLE);

                ((ImageViewHolder) holder).textView.setText(Html.fromHtml(firstSlide.substring(10)));
                ((ImageViewHolder) holder).textView.setBackgroundColor(Color.parseColor(firstSlide.substring(3,10)));

                break;
        }

    }

    @Override
    public int getItemCount() {
        if (lessons != null) return lessons.size();
        return 0;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder{


        private TextView titleView,textView;
        private ImageView slideImageView;

        private ImageView ivProfile;
        private TextView tvEducatorName;
        private TextView tvTopics;


        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            titleView = itemView.findViewById(R.id.tv_title);
            textView = itemView.findViewById(R.id.tv_lesson_card);
            slideImageView = itemView.findViewById(R.id.iv_lesson);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvEducatorName = itemView.findViewById(R.id.tv_name);
            tvTopics = itemView.findViewById(R.id.tv_topics);
            tvTopics.setSelected(true);



        }
    }}