package com.parayada.creampen.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.parayada.creampen.Activity.ExamActivity;
import com.parayada.creampen.Model.QuestionPaper;
import com.parayada.creampen.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PyqListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int NO_PY_QUESIONS = 100;
    private static final int PYQ_HOLDER = 101;

    ArrayList<QuestionPaper> pyqList;
    private Context mContext;

    private QpClickHandler clickHandler;

    public interface QpClickHandler{
        void onQpClick(QuestionPaper qp);
    }

    public PyqListAdapter(ArrayList<QuestionPaper> pyqList, Context mContext) {
        this.pyqList = pyqList;
        this.clickHandler = (QpClickHandler) mContext;
        this.mContext = mContext;
    }

    public void setViewBy(ArrayList<QuestionPaper> pyqList){
        this.pyqList = pyqList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (pyqList == null || pyqList.size() < 1) {
            return NO_PY_QUESIONS;
        }
        return PYQ_HOLDER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == NO_PY_QUESIONS) {
            return new EmptyViewHolder(
                    LayoutInflater.from(mContext).inflate(
                            R.layout.empty_item_view,
                            parent,false));
        } else {
            return new ViewHolder(
                    LayoutInflater.from(mContext).inflate(
                            R.layout.pyq_item_view,
                            parent,
                            false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == PYQ_HOLDER) {
            QuestionPaper qp = pyqList.get(position);

            ((ViewHolder) holder).tv1.setText(qp.getName());
            ((ViewHolder) holder).tv2.setText(new SimpleDateFormat("dd-MMM-yyyy").format(qp.getDate()));

            ((ViewHolder) holder).itemView.setOnClickListener(v -> {
                clickHandler.onQpClick(qp);
            });
        }
    }

    @Override
    public int getItemCount() {
        if (pyqList == null || pyqList.isEmpty())
            return 1;
        return pyqList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tv1,tv2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv1 = itemView.findViewById(R.id.tv1);
            tv2 = itemView.findViewById(R.id.tv2);
        }
    }

    public class EmptyViewHolder extends RecyclerView.ViewHolder{

        public EmptyViewHolder(@NonNull View itemView){
            super(itemView);

            TextView tv = itemView.findViewById(R.id.tv_empty);

            tv.setText(Html.fromHtml("Seems like <b>no question paper</b><br>for the search filters.<br>Have a question paper?,<br>Just inform us"));
        }
    }

}
