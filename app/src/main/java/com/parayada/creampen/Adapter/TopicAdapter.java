package com.parayada.creampen.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.parayada.creampen.Model.Topic;
import com.parayada.creampen.R;

import java.util.ArrayList;

public class TopicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int MainTopic = 2000;
    private static final int SubTopic = 3000;
    private static final int NEW_ENTRY_VIEW_TYPE = 100;
    private static final int TOPIC_VIEW_TYPE = 101;

    private Context mContext;
    private ArrayList<Topic> syllabus;

    private ArrayList<String> chosenTopics = new ArrayList<>();

    private clickHandler mClickHandler;

    public interface clickHandler{
        //void updatedSyllabus(ArrayList<Topic> syllabus,ArrayList<String> chosenTopics);


        void onSyllabusUpdate(ArrayList<Topic> updatedSyllabus);

        void onChoosingTopics(ArrayList<String> chosenTopics);
    }

    public TopicAdapter(ArrayList<Topic> syllabus, Context mContext) {
        this.syllabus = syllabus;
        this.mClickHandler = (clickHandler) mContext;
    }

    @Override
    public int getItemViewType(int position) {
        if (syllabus == null || position == syllabus.size() || syllabus.size() == 0) {
            return NEW_ENTRY_VIEW_TYPE;
        }else {
            return TOPIC_VIEW_TYPE;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view;
        switch (viewType){
            case TOPIC_VIEW_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expandable,parent,false);
                return new ExpandableViewHolder(view);
            case NEW_ENTRY_VIEW_TYPE:
                // fallthrough to default
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_entry,parent,false);
                return new NewEntryViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int position) {
        int itemView = getItemViewType(position);
        if (itemView == TOPIC_VIEW_TYPE){
            ExpandableViewHolder holder = (ExpandableViewHolder) h;
            Topic catTopic =syllabus.get(position);

            holder.tvItem.setText(catTopic.getName());

            // remove all views from linearLayout
            holder.childViews.removeAllViews();

            if (catTopic.getSubTopics() != null){
                holder.tvItem.append(" ("+catTopic.getSubTopics().size()+")");
                for (int mainTopicId = 0; mainTopicId < catTopic.getSubTopics().size(); mainTopicId++) {
                    Topic mainTopic = catTopic.getSubTopics().get(mainTopicId);

                    View mainTopicView = LayoutInflater.from(mContext).inflate(R.layout.item_expandable, holder.childViews, false);
                    mainTopicView.setId(MainTopic + mainTopicId);

                    TextView textView = mainTopicView.findViewById(R.id.tv_expandable_item);
                    textView.setText(mainTopic.getName());
                    textView.setTextSize(15.5f);
                    textView.setTextColor(mContext.getResources().getColor(R.color.white_light));
                    textView.setPadding(25, 8, 0, 8);
                    textView.setBackgroundColor(mContext.getResources().getColor(R.color.black2));

                    LinearLayout subTopicViewGroup = mainTopicView.findViewById(R.id.child_item_view);
                    //subTopicViewGroup.setVisibility(View.GONE);

                    // Show subTopic if and only if subtopic is available
                    if (mainTopic.getSubTopics() != null) {
                        textView.append(" ("+mainTopic.getSubTopics().size()+")");
                        for (int subTopicId = 0; subTopicId < mainTopic.getSubTopics().size(); subTopicId++) {

                            Topic subTopic = mainTopic.getSubTopics().get(subTopicId);

                            TextView tv = new TextView(mContext);
                            tv.setId(SubTopic + subTopicId);
                            tv.setPadding(50, 8, 0, 8);
                            tv.setText(subTopic.getName());
                            tv.setGravity(Gravity.LEFT);
                            tv.setTextColor(Color.GRAY);
                            tv.setBackgroundColor(mContext.getResources().getColor(R.color.black3));

                            if (tv.getParent() != null) {
                                ((ViewGroup) tv.getParent()).removeView(tv); // <- fix
                            }
                            //tv.setVisibility(View.GONE);
                            subTopicViewGroup.addView(tv, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                            String str =catTopic.getName()+" -> "+
                                    mainTopic.getName()+" -> "+
                                    subTopic.getName();
                            if (chosenTopics.contains(str)){
                                tv.setTextColor(Color.WHITE);
                                tv.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
                            }
                            // select sub topic for lesson creation
                            tv.setOnClickListener(v -> {
                                if (chosenTopics.contains(str)){
                                    chosenTopics.remove(str);
                                    tv.setTextColor(Color.GRAY);
                                    tv.setBackgroundColor(mContext.getResources().getColor(R.color.black3));
                                }else {
                                    chosenTopics.add(str);
                                    tv.setTextColor(Color.WHITE);
                                    tv.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
                                }
                                mClickHandler.onChoosingTopics(chosenTopics);
                            });
                            // onLong Click listener
                            tv.setOnLongClickListener(v -> {
                                deleteConfirmDialog(mainTopic.getSubTopics(),subTopic);
                                return false;
                            });

                        }
                    }
                    if (mainTopicView.getParent() != null) {
                        ((ViewGroup) mainTopicView.getParent()).removeView(mainTopicView); // <- fix
                    }


                    ImageButton arrowBtn = mainTopicView.findViewById(R.id.btn_expand);
                    ImageButton addBtn = mainTopicView.findViewById(R.id.btn_add);
                    //ImageButton deleteBtn = mainTopicView.findViewById(R.id.btn_delete);

                    textView.setOnClickListener(v -> toggleExpandableView(subTopicViewGroup, arrowBtn));
                    arrowBtn.setOnClickListener(v -> toggleExpandableView(subTopicViewGroup, arrowBtn));
                    addBtn.setOnClickListener(v -> newEntryDialog(mainTopic,subTopicViewGroup,arrowBtn));
                    //deleteBtn.setOnClickListener(v -> {
                    textView.setOnLongClickListener(v -> {
                        deleteConfirmDialog(catTopic.getSubTopics(),mainTopic);
                        return true;
                    });

                    //  mainTopicView.setVisibility(View.GONE);
                    holder.childViews.addView(mainTopicView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                }
            }

            //Toggle child items visibility
            holder.tvItem.setOnClickListener(v -> {toggleExpandableView(holder.childViews,holder.arrowBtn); });
            holder.arrowBtn.setOnClickListener(v -> {toggleExpandableView(holder.childViews,holder.arrowBtn);
            });
            holder.addBtn.setOnClickListener(v -> {
                newEntryDialog(catTopic,holder.childViews,holder.arrowBtn);
            });
            //holder.deleteBtn.setOnClickListener(v -> {
            holder.tvItem.setOnLongClickListener(v -> {
                deleteConfirmDialog(syllabus,catTopic);
                return true;
            });

        }
        else if (itemView == NEW_ENTRY_VIEW_TYPE){
            NewEntryViewHolder holder = (NewEntryViewHolder) h;

            holder.btnNewCategory.setOnClickListener(v -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

                alert.setTitle("New Category/Subject");
                alert.setMessage("You can create new category, subject or any other 1st level syllabus head here \nEvery category can further divided into Main Topic and Sub Topic");

                // Set an EditText view to get user input
                final EditText input = new EditText(mContext);
                alert.setView(input);

                alert.setPositiveButton("Ok", (dialog, whichButton) -> {
                    String value = input.getText().toString().trim();
                    // add new category to list
                    if (value.contains("{") || value.contains("}") || value.contains(",")){
                        input.setTextColor(mContext.getResources().getColor(R.color.red));
                        Toast.makeText(mContext, "Only alphanumeric characters are allowed", Toast.LENGTH_SHORT).show();
                    }else if (!value.isEmpty()) {
                        syllabus.add(new Topic(value));
                        mClickHandler.onSyllabusUpdate(syllabus);
                        //mClickHandler.updatedSyllabus(syllabus,chosenTopics);
                        this.notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton("Cancel",null);

                alert.show();


            });

        }
    }

    private void deleteConfirmDialog(ArrayList<Topic> topicList,Topic topic) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("Confirm Delete?")
                .setMessage("Are you sure to delete \"" + topic.getName()+ "\"")
                .setNegativeButton("Cancel",null)
                .setPositiveButton("Confirm", (dialog1, which) -> {
                    topicList.remove(topic);
                    mClickHandler.onSyllabusUpdate(syllabus);
                    //mClickHandler.updatedSyllabus(syllabus,chosenTopics);
                    this.notifyDataSetChanged();
                });
        dialog.create().show();
    }

    private void toggleExpandableView(View childViews, ImageButton arrowBtn) {
        if (childViews.getVisibility() == View.VISIBLE) {
            childViews.setVisibility(View.GONE);
            arrowBtn.setRotation(90);
        } else{
            childViews.setVisibility(View.VISIBLE);
            arrowBtn.setRotation(270);
        }
    }

    private void newEntryDialog(Topic topic, LinearLayout subTopicViewGroup, ImageButton arrowBtn){

        subTopicViewGroup.setVisibility(View.VISIBLE);
        arrowBtn.setRotation(270);
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

        alert.setTitle("New Topic");
        alert.setMessage("Create a new topic under \""+ topic.getName()+"\"");

        // Set an EditText view to get user input
        final EditText input = new EditText(mContext);
        alert.setView(input);


        alert.setPositiveButton("Ok", (dialog, whichButton) -> {
            String value = input.getText().toString().trim();
            // Add new topics
            if (value.contains("{") || value.contains("}") || value.contains(",")){
                input.setTextColor(mContext.getResources().getColor(R.color.red));
                Toast.makeText(mContext, "Only alphanumeric characters are allowed", Toast.LENGTH_SHORT).show();
            }else if (!value.isEmpty()) {
                topic.addNewSubTopic(new Topic(value));
                mClickHandler.onSyllabusUpdate(syllabus);
                //mClickHandler.updatedSyllabus(syllabus,chosenTopics);
                this.notifyDataSetChanged();
            }
        });

        alert.setNegativeButton("Cancel", null);

        alert.show();

    }

    @Override
    public int getItemCount() {

        if (syllabus == null)
            return 1;
        return syllabus.size()+1;
    }

    private class ExpandableViewHolder extends RecyclerView.ViewHolder {

        TextView tvItem;
        LinearLayout childViews;
        ImageButton arrowBtn;
        ImageButton addBtn;
        //ImageButton deleteBtn;
        ExpandableViewHolder(View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tv_expandable_item);
            childViews = itemView.findViewById(R.id.child_item_view);
            //childViews.setVisibility(View.GONE);
            arrowBtn = itemView.findViewById(R.id.btn_expand);
            addBtn = itemView.findViewById(R.id.btn_add);
            //deleteBtn = itemView.findViewById(R.id.btn_delete);

            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) arrowBtn.getLayoutParams();

            params.height = 40;
            params.width = 40;
            params.setMarginEnd(20);
        }
    }

    private class NewEntryViewHolder extends RecyclerView.ViewHolder {

        Button btnNewCategory;
        NewEntryViewHolder(View view) {
            super(view);
            btnNewCategory = view.findViewById(R.id.btn_new_category);
        }
    }
}

