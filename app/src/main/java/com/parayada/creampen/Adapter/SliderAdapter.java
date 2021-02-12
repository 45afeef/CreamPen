package com.parayada.creampen.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parayada.creampen.Model.McqSet;
import com.parayada.creampen.R;

import java.util.ArrayList;

public class SliderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int PICTURE_ITEM_VIEW_TYPE = 100;
    private static final int MCQ_ITEM_VIEW_TYPE = 101;
    private static final int TEXT_ITEM_VIEW_TYPE  = 102;

    private ArrayList<String> slideList;
    private Context mContext;

    public  SliderAdapter(ArrayList<String> slideList){
        this.slideList = slideList;
    }

    /**
     * Determines the view type for the given position.
     */
    @Override
    public int getItemViewType(int position) {
        return Integer.parseInt(slideList.get(position).substring(0,3));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        switch (viewType) {
            case MCQ_ITEM_VIEW_TYPE:
                return new McqSetViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.mcq_view,
                                parent,
                                false
                        )
                );
            case PICTURE_ITEM_VIEW_TYPE:
                return new PicViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.image_view,
                                parent,
                                false
                        )
                );
            case TEXT_ITEM_VIEW_TYPE:
            // fall through
            default:
                return new TextViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(
                                R.layout.textview,
                                parent,
                                false
                        )
                );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = Integer.parseInt(slideList.get(position).substring(0,3));

        switch (viewType) {
            case MCQ_ITEM_VIEW_TYPE:

                McqSet mcqSet = new McqSet(slideList.get(position));
                McqSetViewHolder mcqHolder = (McqSetViewHolder) holder;

                mcqHolder.option1View.setBackgroundColor(Color.parseColor("#00ffffff"));
                mcqHolder.option2View.setBackgroundColor(Color.parseColor("#00ffffff"));
                mcqHolder.option3View.setBackgroundColor(Color.parseColor("#00ffffff"));
                mcqHolder.option4View.setBackgroundColor(Color.parseColor("#00ffffff"));

                mcqHolder.questionView.setText(mcqSet.getQuestion());
                mcqHolder.option1View.setText(mcqSet.getOption1());
                mcqHolder.option2View.setText(mcqSet.getOption2());
                mcqHolder.option3View.setText(mcqSet.getOption3());
                mcqHolder.option4View.setText(mcqSet.getOption4());

                final boolean[] firstClick = {true};
                View.OnClickListener onOptionClick = v -> {
                    if (((TextView) v).getText().equals(mcqSet.getAnswer())){
                        if (firstClick[0]){
                            Toast.makeText(mContext, "Hoooraayyyy..... You make it with first Attempt", Toast.LENGTH_SHORT).show();
                        }
                        v.setBackgroundColor(mContext.getResources().getColor(R.color.right));
                    }else {
                        v.setBackgroundColor(mContext.getResources().getColor(R.color.wrong));
                    }
                    firstClick[0] = false;
                };


                mcqHolder.option1View.setOnClickListener(onOptionClick);
                mcqHolder.option2View.setOnClickListener(onOptionClick);
                mcqHolder.option3View.setOnClickListener(onOptionClick);
                mcqHolder.option4View.setOnClickListener(onOptionClick);

                break;

            case PICTURE_ITEM_VIEW_TYPE:
                PicViewHolder picHolder = (PicViewHolder) holder;
                Glide.with(mContext)
                        .load(String.valueOf(slideList.get(position)).substring(3))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        //.placeholder(new ColorDrawable(Color.WHITE))
                        .fallback(new ColorDrawable(Color.GRAY))
                        .into((picHolder).imageView);
                break;
            case TEXT_ITEM_VIEW_TYPE :
                String text = (String) slideList.get(position);
                TextViewHolder textHolder = (TextViewHolder) holder;

                // Set background color
                ((View) textHolder.textView.getParent()).setBackgroundColor(Color.parseColor(text.substring(3,10)));

                // Set text
                textHolder.textView.setText(Html.fromHtml(text.substring(10)));

                break;

            default:

                // load the following code if the slide is not supported
                // loaded textViewHolder to show not supported message;
                ((TextViewHolder) holder).textView.setTextColor(Color.WHITE);
                ((TextViewHolder) holder).textView.setText("This slide is not supported in this version \n\nPlease update the CreamPen app to latest version");

        }
    }

    @Override
    public int getItemCount() {
        return slideList.size();
    }

    private class PicViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;

        public PicViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageSlide);
        }

    }

    private class McqSetViewHolder extends RecyclerView.ViewHolder{


        private TextView questionView;
        private TextView option1View;
        private TextView option2View;
        private TextView option3View;
        private TextView option4View;

        public McqSetViewHolder(@NonNull View itemView) {
            super(itemView);

            questionView = itemView.findViewById(R.id.tv_question);
            option1View = itemView.findViewById(R.id.tv_OptionA);
            option2View = itemView.findViewById(R.id.tv_OptionB);
            option3View = itemView.findViewById(R.id.tv_OptionC);
            option4View = itemView.findViewById(R.id.tv_OptionD);
        }
    }

    private class TextViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;

        public TextViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textview);
        }
    }
}