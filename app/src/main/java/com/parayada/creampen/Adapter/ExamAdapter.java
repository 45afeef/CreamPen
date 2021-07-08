package com.parayada.creampen.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.R;

import java.util.ArrayList;
import java.util.function.UnaryOperator;

import static android.graphics.Typeface.NORMAL;

public class ExamAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    ArrayList<String> questions;
    ArrayList<String> answers = new ArrayList<>();
    private boolean isFinished = false;
    private boolean lockAtFirst = true;

    public ExamAdapter(){}

    public ExamAdapter(ArrayList<String> questions, boolean lockAtFirst) {
        this.questions = questions;
        this.lockAtFirst = lockAtFirst;
        if (questions != null){
            answers.clear();
            for (String q:questions){
                answers.add(0, "UnAttempted");
            }
        }
    }

    public void addNewMcq(McqSet mcqSet){
        if (questions == null) questions = new ArrayList<>();

        questions.add(mcqSet.getQuestionAsString());
        answers.add(mcqSet.getAnswer());
        notifyDataSetChanged();

    }

    public ArrayList<String> getQuestions(){
        return this.questions;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new McqViewHolder(
                LayoutInflater.from(mContext).inflate(
                        R.layout.mcq_view,
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        McqViewHolder mcqHolder = (McqViewHolder) holder;
        McqSet mcqSet = new McqSet(questions.get(position));

        // Set Question and four options
        mcqHolder.questionView.setText((position + 1) + " " + mcqSet.getQuestion());
        mcqHolder.option1View.setText(mcqSet.getOption1());
        mcqHolder.option2View.setText(mcqSet.getOption2());
        mcqHolder.option3View.setText(mcqSet.getOption3());
        mcqHolder.option4View.setText(mcqSet.getOption4());

        // set Text color for question and options
        mcqHolder.questionView.setTextColor(Color.BLACK);
        mcqHolder.option1View.setTextColor(Color.BLACK);
        mcqHolder.option2View.setTextColor(Color.BLACK);
        mcqHolder.option3View.setTextColor(Color.BLACK);
        mcqHolder.option4View.setTextColor(Color.BLACK);

        if(this.lockAtFirst) {
            // Educator restrict second selection of answer and lock the first attempt
            if (answers.get(position).equals("UnAttempted")) {
                // Allow user to select a option as answer and lock it
                mcqHolder.option1View.setOnClickListener(v -> {
                    answers.set(position, mcqHolder.option1View.getText().toString());
                    disableTouches(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position);
                });
                mcqHolder.option2View.setOnClickListener(v -> {
                    answers.set(position, mcqHolder.option2View.getText().toString());
                    disableTouches(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position);
                });
                mcqHolder.option3View.setOnClickListener(v -> {
                    answers.set(position, mcqHolder.option3View.getText().toString());
                    disableTouches(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position);
                });
                mcqHolder.option4View.setOnClickListener(v -> {
                    answers.set(position, mcqHolder.option4View.getText().toString());
                    disableTouches(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position);
                });
            } else {
                // User already selected a option as answer
                // As educator restricts changing the answer all other options are disabled
                disableTouches(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position);
            }

        }else {
            if(!isFinished) {
                // Educator allowed to change the choice
                mcqHolder.option1View.setOnClickListener(v -> {
                    // override the current selection for this question
                    if (answers.get(position).equals(mcqHolder.option1View.getText().toString())) {
                        // Remove this option as answer
                        answers.set(position, "UnAttempted");
                    } else {
                        // Add this option as answer
                        answers.set(position, mcqHolder.option1View.getText().toString());
                    }
                    highlightSelectedOption(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position, Color.GRAY);

                });
                mcqHolder.option2View.setOnClickListener(v -> {
                    // override the current selection for this question
                    if (answers.get(position).equals(mcqHolder.option2View.getText().toString())) {
                        // Remove this option as answer
                        answers.set(position, "UnAttempted");
                    } else {
                        // Add this option as answer
                        answers.set(position, mcqHolder.option2View.getText().toString());
                    }
                    highlightSelectedOption(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position, Color.GRAY);

                });
                mcqHolder.option3View.setOnClickListener(v -> {
                    // override the current selection for this question
                    if (answers.get(position).equals(mcqHolder.option3View.getText().toString())) {
                        // Remove this option as answer
                        answers.set(position, "UnAttempted");
                    } else {
                        // Add this option as answer
                        answers.set(position, mcqHolder.option3View.getText().toString());
                    }
                    highlightSelectedOption(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position, Color.GRAY);

                });
                mcqHolder.option4View.setOnClickListener(v -> {
                    // override the current selection for this question
                    if (answers.get(position).equals(mcqHolder.option4View.getText().toString())) {
                        // Remove this option as answer
                        answers.set(position, "UnAttempted");
                    } else {
                        // Add this option as answer
                        answers.set(position, mcqHolder.option4View.getText().toString());
                    }
                    highlightSelectedOption(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position, Color.GRAY);

                });
            }else{
                mcqHolder.option1View.setOnClickListener(null);
                mcqHolder.option2View.setOnClickListener(null);
                mcqHolder.option3View.setOnClickListener(null);
                mcqHolder.option4View.setOnClickListener(null);
            }

            highlightSelectedOption(mcqHolder.option1View, mcqHolder.option2View, mcqHolder.option3View, mcqHolder.option4View, position,Color.GRAY);

        }

        if (isFinished) {
            //mcqHolder.questionView.setTextColor(Color.BLACK);

            int anColor = mContext.getResources().getColor(R.color.right);

            if (answers.get(position).equals(mContext.getResources().getString(R.string.skipped)))
                anColor = mContext.getResources().getColor(R.color.skipped);

            if (mcqHolder.option1View.getText().toString().equals(mcqSet.getAnswer())){
                mcqHolder.option1View.setTextColor(anColor);
            }else if (mcqHolder.option2View.getText().toString().equals(mcqSet.getAnswer())){
                mcqHolder.option2View.setTextColor(anColor);
            }else if (mcqHolder.option3View.getText().toString().equals(mcqSet.getAnswer())){
                mcqHolder.option3View.setTextColor(anColor);
            }else if (mcqHolder.option4View.getText().toString().equals(mcqSet.getAnswer())){
                mcqHolder.option4View.setTextColor(anColor);
            }
        }


    }

    private void disableTouches(TextView o1,TextView o2,TextView o3,TextView o4, int position) {
        o1.setOnClickListener(null);
        o2.setOnClickListener(null);
        o3.setOnClickListener(null);
        o4.setOnClickListener(null);

        // setTextColor
        int disabledColor = mContext.getResources().getColor(R.color.common_google_signin_btn_text_dark_disabled);

        highlightSelectedOption(o1,o2,o3,o4,position,disabledColor);
    }

    private void highlightSelectedOption(TextView o1,TextView o2,TextView o3,TextView o4, int position,int defaultColor){

        // Set default color for all options
        o1.setTextColor(defaultColor);
        o2.setTextColor(defaultColor);
        o3.setTextColor(defaultColor);
        o4.setTextColor(defaultColor);

        // Show the selected option with highlight color
        int selectionColor = mContext.getResources().getColor(R.color.selected);
        if (isFinished)
            selectionColor = mContext.getResources().getColor(R.color.wrong);

        if (answers.get(position).equals(o1.getText().toString())){
            o1.setTextColor(selectionColor);
        }else if (answers.get(position).equals(o2.getText().toString())){
            o2.setTextColor(selectionColor);
        }else if (answers.get(position).equals(o3.getText().toString())){
            o3.setTextColor(selectionColor);
        }else if (answers.get(position).equals(o4.getText().toString())){
            o4.setTextColor(selectionColor);
        }
    }

    @Override
    public int getItemCount() {
        if (questions == null || questions.isEmpty())
            return 0;
        return questions.size();
    }

    public ArrayList<String> onFinish() {
        // set skipped questions to 0
        isFinished = true;
        for (int i = 0; i<answers.size(); i++){
            if (answers.get(i).equals("UnAttempted"))
                answers.set(i,mContext.getResources().getString(R.string.skipped));
        }
        notifyDataSetChanged();

        return answers;
    }

    private class McqViewHolder extends RecyclerView.ViewHolder{


        private TextView questionView;
        private TextView option1View;
        private TextView option2View;
        private TextView option3View;
        private TextView option4View;

        McqViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
            ((CardView) itemView).setCardBackgroundColor(Color.rgb(255,255,255));
            questionView = itemView.findViewById(R.id.tv_question);
            questionView.setGravity(Gravity.START);
            questionView.setTextColor(Color.BLACK);
            questionView.setTextSize(16);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                questionView.setTextAppearance(NORMAL);
            }
            option1View = itemView.findViewById(R.id.tv_OptionA);
            option1View.setTextColor(Color.BLACK);
            option2View = itemView.findViewById(R.id.tv_OptionB);
            option2View.setTextColor(Color.BLACK);
            option3View = itemView.findViewById(R.id.tv_OptionC);
            option3View.setTextColor(Color.BLACK);
            option4View = itemView.findViewById(R.id.tv_OptionD);
            option4View.setTextColor(Color.BLACK);

        }
    }

}
