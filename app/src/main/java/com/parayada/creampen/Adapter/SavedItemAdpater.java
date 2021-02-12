package com.parayada.creampen.Adapter;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parayada.creampen.Activity.CourseActivity;
import com.parayada.creampen.Activity.LessonActivity;
import com.parayada.creampen.Model.SavedItem;
import com.parayada.creampen.R;

import java.util.ArrayList;
import java.util.List;

public class SavedItemAdpater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_VIEW_TYPE = 100;
    private static final int EMPTY_VIEW_TYPE = 101;
    List<SavedItem> savedItems;
    Context mContext;

    private clickHandler mClickHandler;

    public interface clickHandler{
        void deleteSavedItem(SavedItem item);
    }

    public SavedItemAdpater(Context context){
        this.mClickHandler = (clickHandler) context;
    }

    public SavedItemAdpater(ArrayList<SavedItem> savedItems) {
        this.savedItems = savedItems;
    }

    @Override
    public int getItemViewType(int position) {

        if (savedItems == null|| savedItems.size() == 0) {
            return EMPTY_VIEW_TYPE;
        }else {
            return ITEM_VIEW_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        mContext = parent.getContext();
        switch (viewType) {
            case EMPTY_VIEW_TYPE:
                return new RecyclerView.ViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.empty_item_view,
                                parent,
                                false
                        )
                ){};
            case ITEM_VIEW_TYPE:
                // fall through
            default:
                return new SavedViewHolder(
                        LayoutInflater.from(mContext).inflate(
                                R.layout.saved_item_view,
                                parent,
                                false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == ITEM_VIEW_TYPE) {
            SavedItem item = savedItems.get(position);

            if (item.getItemType().equals("Course")) {
                ((SavedViewHolder) holder).rootView.setBackgroundResource(R.drawable.text_back_2);
            } else {
                ((SavedViewHolder) holder).rootView.setBackgroundResource(R.drawable.text_back_1);
            }/**/

            ((SavedViewHolder) holder).titleView.setText(item.getItemTitle());
            ((SavedViewHolder) holder).typeView.setText(item.getItemType());

            // OnClickListener for savedItem Opening.
            ((SavedViewHolder) holder).itemView.setOnClickListener(v -> {
                Intent intent;
                if (item.getItemType().equals("Lesson")) {
                    intent = new Intent(mContext, LessonActivity.class);
                    intent.putExtra("lessonId", item.getItemId());
                } else {//if (item.getItemType().equals("Course")){
                    intent = new Intent(mContext, CourseActivity.class);
                    intent.putExtra("courseId", item.getItemId());
                }
                mContext.startActivity(intent);
            });

            // OnClickListener for deleting SavedItem
            ((SavedViewHolder) holder).btnDelete.setOnClickListener(v -> mClickHandler.deleteSavedItem(item));

        }
    }
    @Override
    public int getItemCount() {
        if (savedItems == null || savedItems.size() == 0)
            return 1;
        return savedItems.size();
    }

    public void setItems(List<SavedItem> items) {
        savedItems = items;
        this.notifyDataSetChanged();
    }

    private class SavedViewHolder extends RecyclerView.ViewHolder {

        View rootView;
        TextView titleView;
        TextView typeView;
        ImageButton btnDelete;

        private SavedViewHolder(View itemView) {
            super(itemView);

            rootView = itemView.findViewById(R.id.rootView);
            titleView = itemView.findViewById(R.id.tv_title);
            typeView = itemView.findViewById(R.id.tv_type);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

}