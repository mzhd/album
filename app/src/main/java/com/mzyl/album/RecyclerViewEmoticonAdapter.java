package com.mzyl.album;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RecyclerViewEmoticonAdapter extends RecyclerView.Adapter{
    Context context;
    LayoutInflater inflater;
    int[] icons;
    public RecyclerViewEmoticonAdapter(Context context,int[] icons) {
        this.context=context;
        inflater=LayoutInflater.from(context);
        this.icons=icons;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_emoticon_item, parent, false);
        MyHolder holder = new MyHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyHolder myHolder = (MyHolder) holder;
        myHolder.iv_emoticon.setImageResource(icons[position]);
        myHolder.tv_description.setText(position+"");
    }

    @Override
    public int getItemCount() {
        return icons.length;
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView iv_emoticon;
        TextView tv_description;
        public MyHolder(View itemView) {
            super(itemView);
            iv_emoticon=itemView.findViewById(R.id.iv_emoticon);
            tv_description=itemView.findViewById(R.id.tv_description);

        }
    }
}
