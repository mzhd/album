package com.mzyl.album;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class EmoticonFragment extends Fragment {
    public static final String TAG = "EmoticonFragment";

    private ViewPager vp_emoticon_item;
    private List icons;
    private RecyclerView rv_emoticon;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_emoticon, container, false);
        Bundle bundle = getArguments();
        icons = bundle.getIntegerArrayList("icons");

        rv_emoticon = contentView.findViewById(R.id.rv_emoticon);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4, LinearLayoutManager.VERTICAL, false);
        rv_emoticon.setLayoutManager(layoutManager);
        rv_emoticon.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.recyclerview_emoticon_item, parent, false);
                return new MyHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                MyHolder myHolder = (MyHolder) holder;
                myHolder.iv_emoticon.setImageResource((Integer) icons.get(position));
                myHolder.iv_emoticon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomImageView2 iv_image = ((Activity) getContext()).findViewById(R.id.iv_image);
                        iv_image.addEmotion((Integer) icons.get(position));
                        ((Activity) getContext()).findViewById(R.id.lly_emoticon_main).setVisibility(View.INVISIBLE);
                        ((CheckBox) ((Activity) getContext()).findViewById(R.id.cb_emoticon)).setChecked(false);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return icons.size();
            }

            class MyHolder extends RecyclerView.ViewHolder {
                ImageView iv_emoticon;
                TextView tv_description;

                public MyHolder(View itemView) {
                    super(itemView);
                    iv_emoticon = itemView.findViewById(R.id.iv_emoticon);
                    tv_description = itemView.findViewById(R.id.tv_description);
                }
            }
        });

        return contentView;
    }


}
