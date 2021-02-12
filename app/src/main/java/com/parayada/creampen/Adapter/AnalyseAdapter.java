package com.parayada.creampen.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.R;

import java.util.ArrayList;

public class AnalyseAdapter extends RecyclerView.Adapter {

    Context mContext;
    ArrayList<McqSet> mcqSetList;
    ArrayList<String> selection;

    public AnalyseAdapter(ArrayList<McqSet> selectedMcqs,ArrayList<String> selection) {
        this.mcqSetList = selectedMcqs;
        this.selection = selection;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        return new McqViewHolder(
                LayoutInflater.from(mContext).inflate(
                        R.layout.analyse_mcq_item_view,
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        McqSet mcqSet = mcqSetList.get(position);
        McqViewHolder mcqHolder = (McqViewHolder) holder;

        mcqHolder.tvQuestion.setText(mcqSet.getQuestion());
        mcqHolder.tvAnswer.setText("Right answer : "+mcqSet.getAnswer());
        mcqHolder.tvSelection.setText("Your selection : "+selection.get(position));

    }

    @Override
    public int getItemCount() {
        if (mcqSetList == null)
            return 0;
        return mcqSetList.size();
    }

    private class McqViewHolder extends RecyclerView.ViewHolder {

        TextView tvQuestion,
            tvAnswer,
            tvSelection;
        public McqViewHolder(View itemView) {

            super(itemView);

            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            tvSelection = itemView.findViewById(R.id.tv_selection);
        }
    }
}
