package com.mzyl.album;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RecyclerViewClassifyAdapter extends RecyclerView.Adapter {
    private Context context;
    private LayoutInflater inflater;
    private Map mapDatas;
    private List keys;
    private int checkedPosition;

    public RecyclerViewClassifyAdapter(Context context, Map mapDatas) {
        this.context = context;
        this.mapDatas = mapDatas;
        inflater = LayoutInflater.from(context);
        keys=new ArrayList();
        if (mapDatas==null)
            return;
        Iterator iterator = mapDatas.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (key.equals("图片和视频")) {
                keys.add(0,key);
            }else
            keys.add(key);
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.recyclerview_classify_item, parent, false);
        return new MyHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyHolder) {
            MyHolder myHolder = (MyHolder) holder;
            List list = (List) mapDatas.get(keys.get(position));

            if (list!=null&&list.size()!=0)
            {
                ImageEntry   entry = (ImageEntry) (list).get(0);
                Picasso.get().load(new File(entry.getData())).fit().into(myHolder.iv_thumbnail);
            }
            myHolder.tv_classify_title.setText((CharSequence) keys.get(position));
            if (position == 0) {
                myHolder.tv_classify_count.setVisibility(View.INVISIBLE);
            }else{
                myHolder.tv_classify_count.setVisibility(View.VISIBLE);
            }
            myHolder.tv_classify_count.setText("" + (list).size()+"张");
            myHolder.itemView.setOnClickListener(new MyOnClickListener(position));
            if (position==checkedPosition){
                myHolder.rb_classify.setVisibility(View.VISIBLE);
            }else{
                myHolder.rb_classify.setVisibility(View.INVISIBLE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView iv_thumbnail;
        ImageView iv_icon;
        TextView tv_classify_title;
        TextView tv_classify_count;
        RadioButton rb_classify;

        public MyHolder(View itemView) {
            super(itemView);
            iv_thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            tv_classify_title = itemView.findViewById(R.id.tv_classify_title);
            tv_classify_count = itemView.findViewById(R.id.tv_classify_count);
            rb_classify = itemView.findViewById(R.id.rb_classify);
        }

    }
    public final void notifyDataSetChanged(Map mapDatas) {
        this.mapDatas = mapDatas;
        keys=new ArrayList();
        if (mapDatas==null)
            return;
        Iterator iterator = mapDatas.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            if (key.equals("图片和视频")) {
                keys.add(0,key);
            }else
            keys.add(key);
        }
        notifyDataSetChanged();
    }
    class MyOnClickListener implements View.OnClickListener {
        int position;

        public MyOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            notifyItemChanged(checkedPosition);
            notifyItemChanged(position);
            checkedPosition = position;
            ((RecyclerViewContentAdapter)((MainActivity)context).adapter).notifyDataSetChanged((List) mapDatas.get(keys.get(position)));
            ((MainActivity)context).dismissBottomSheet();

        }
    }
}
