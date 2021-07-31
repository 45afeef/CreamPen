package com.parayada.creampen.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parayada.creampen.Activity.ExamActivity;
import com.parayada.creampen.Activity.LessonActivity;
import com.parayada.creampen.Model.TypeIdName;
import com.parayada.creampen.R;
import com.parayada.creampen.Utils.SharingLink;

import java.util.ArrayList;

public class LessonsAdapter extends RecyclerView.Adapter {

    Context mContext;
    private ArrayList<TypeIdName> items;
    private boolean isEducator;
    private lessonClickHandler mClickHandler;

    public interface lessonClickHandler{
        void editQuiz(int index,String quizId);
    }

    public LessonsAdapter(ArrayList<TypeIdName> items, boolean isEducator,Context context) {
        this.mContext = context;
        this.items = items;
        this.isEducator = isEducator;
        this.mClickHandler = (lessonClickHandler) context;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Integer.parseInt(mContext.getString(R.string.CodeLesson))) {
            return new LessonViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_course, parent, false));
        }
        else{
            return new QuizViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.quiz_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, final int position) {
        final TypeIdName item = items.get(position);
        switch (item.getType()){
            case 100:
                // Code 100 is for Lesson
                LessonViewHolder lessonHolder = (LessonViewHolder) h;

                lessonHolder.titleView.setText((position+1)+"        "+item.getName());
                lessonHolder.itemView.setOnClickListener(v -> {
                    Intent toLearnIntent = new Intent(mContext, LessonActivity.class);

                    toLearnIntent.putExtra("lessonId", item.getId());
                    mContext.startActivity(toLearnIntent);

                });
                lessonHolder.shareBtn.setOnClickListener(v -> {
                    Toast.makeText(mContext,"Fetching link for the lesson \""+ item.getName() + "\"",Toast.LENGTH_LONG).show();
                    SharingLink.TypeIdName(item,mContext);
                });
                break;
            case 101:
                // Code 101 is for Quiz
                // fall through
            default:
                QuizViewHolder quizHolder = (QuizViewHolder) h;

                quizHolder.position.setText(String.valueOf(position+1));
                quizHolder.name.setText(item.getName());
                quizHolder.itemView.setOnClickListener(v -> {
                    Intent toQuizIntent = new Intent(mContext, ExamActivity.class);
                    toQuizIntent.putExtra("quizId", item.getId());
                    mContext.startActivity(toQuizIntent);
                });
                quizHolder.shareBtn.setOnClickListener(v -> {
                    Toast.makeText(mContext,"Fetching link for this quiz named \""+ item.getName() + "\"",Toast.LENGTH_LONG).show();
                    SharingLink.TypeIdName(item,mContext);
                });

                // Enable editButton for quizzes only if the user id includes in educator list
                if(isEducator) {
                    quizHolder.editBtn.setVisibility(View.VISIBLE);
                    quizHolder.editBtn.setOnClickListener(v->mClickHandler.editQuiz(position,item.getId()));
                }

                break;
        }

    }

    @Override
    public int getItemCount() {
        if (items != null) return items.size();
        return 0;
    }

    private class LessonViewHolder extends RecyclerView.ViewHolder {

        View itemView;
        TextView titleView;
        ImageButton shareBtn;

        private LessonViewHolder(View view) {
            super(view);
            itemView = view;
            titleView = view.findViewById(R.id.tv_course_title);
            shareBtn = itemView.findViewById(R.id.ib_share_course);


        }
    }

    private class QuizViewHolder extends RecyclerView.ViewHolder{

        TextView name ;
        TextView position ;
        ImageButton shareBtn;
        ImageButton editBtn;

        private QuizViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.tv_quiz_name);
            position = itemView.findViewById(R.id.tv_sl_no);
            shareBtn = itemView.findViewById(R.id.ib_share_quiz);
            editBtn = itemView.findViewById((R.id.ib_edit_quiz));

        }
    }
}