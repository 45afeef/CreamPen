package com.parayada.creampen.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.parayada.creampen.Activity.PyqListActivity;
import com.parayada.creampen.R;

import java.util.ArrayList;

public class StreamContentAdapter extends RecyclerView.Adapter<StreamContentAdapter.ViewHolder>{

    private Context mContext;
    private  ArrayList<String > contentList;
    private String streamPath;

    public StreamContentAdapter(ArrayList<String> contentList, String streamPath) {
        this.contentList = contentList;
        this.streamPath = streamPath;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new ViewHolder(
                LayoutInflater.from(mContext).inflate(
                        R.layout.item_common,
                        parent,
                        false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvContent.setText(contentList.get(position));
        holder.tvContent.setOnClickListener(v -> {
            switch (contentList.get(position)){
                case "Previous Questions":
                    Intent toPyqActivity = new Intent(mContext, PyqListActivity.class);
                    toPyqActivity.putExtra("pyqPath",streamPath+"/PYQ");
                    toPyqActivity.putExtra("from","main");

                    mContext.startActivity(toPyqActivity);
                    break;
                case "Notes":
                    // fall through
                case "Capsules":
                    // fall through
                case "Mnemonics" :
                    // fall through
                default:
                    Toast t = Toast.makeText(mContext, contentList.get(position) + " are under Construction", Toast.LENGTH_LONG);
                    t.setGravity(1,0,0);
                    t.show();

            }
        });
    }

    @Override
    public int getItemCount() {
        if (contentList == null || contentList.isEmpty())
            return 0;
        return contentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvContent;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvContent = itemView.findViewById(R.id.tv_common);
        }
    }
}
