package com.parayada.creampen.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.FirebaseFirestore;
import com.parayada.creampen.Activity.CourseActivity;
import com.parayada.creampen.Model.Course;
import com.parayada.creampen.R;
import com.parayada.creampen.Utils.SharingLink;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int COURSE_ITEM_VIEW_TYPE = 100;
    private static final int EMPTY_ITEM_VIEW_TYPE = 101;

    private Context mContext;
    private ArrayList<Course> courses;
    private String separator;

    public CourseAdapter(ArrayList<Course> courses) {
        this.courses = courses;
    }

    @Override
    public int getItemViewType(int position) {

        if (courses == null || courses.size() == 0) {
            return EMPTY_ITEM_VIEW_TYPE;
        }else {
            return COURSE_ITEM_VIEW_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        separator = mContext.getResources().getString(R.string.mySeparator);
        switch (viewType) {
            case EMPTY_ITEM_VIEW_TYPE:
                return new RecyclerView.ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.empty_item_view,
                                parent,
                                false
                        )
                ) {
                };
            case COURSE_ITEM_VIEW_TYPE:
                // fall through
            default:
                return new CourseViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.card_view_course,
                                parent,
                                false
                        )
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == COURSE_ITEM_VIEW_TYPE){
            final Course c = courses.get(position);
            ((CourseViewHolder) holder).titleView.setText(c.getTitle());

            ((CourseViewHolder) holder).titleView.setOnClickListener(v -> {
                Intent courseActivity = new Intent(mContext, CourseActivity.class);

                courseActivity.putExtra("course",c);
                mContext.startActivity(courseActivity);
            });

            ((CourseViewHolder)  holder).shareBtn.setOnClickListener(v -> {

                    Toast.makeText(mContext, "Fetching Link for the course \"" + c.getTitle() + "\"", Toast.LENGTH_LONG).show();

                    SharingLink.Course(c,mContext);

            });

        }
    }

    @Override
    public int getItemCount() {
        if (courses == null || courses.size() == 0)
            return 1;
        return courses.size();
    }

    private class CourseViewHolder extends RecyclerView.ViewHolder {

        TextView titleView;
        ImageButton shareBtn;
        private CourseViewHolder(View itemView) {
            super(itemView);

            titleView = itemView.findViewById(R.id.tv_course_title);
            shareBtn = itemView.findViewById(R.id.ib_share_course);
        }
    }
}