package com.mzyl.album;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class VPItemFragment extends Fragment  {
    public static final String TAG = "VPItemFragment";
    private ImageView iv_vp_item;

    PreviewActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        activity = (PreviewActivity) getContext();
        View contentView = inflater.inflate(R.layout.viewpager_item, container, false);
        iv_vp_item = contentView.findViewById(R.id.iv_vp_item);

        if (activity.isPreview) {
            ImageEntry entry= (ImageEntry) activity.datas.get((Integer) (activity).checkedDatas.get(getArguments().getInt("position")));
            Picasso.get().load(new File(entry.getData())).config(Bitmap.Config.RGB_565).resize(((MyApplication) activity.getApplication()).screenWidth,((MyApplication) activity.getApplication()).screenHeight).centerInside().into(iv_vp_item);
        } else {
            ImageEntry entry= (ImageEntry) activity.datas.get(getArguments().getInt("position"));
            Picasso.get().load(new File(entry.getData())).config(Bitmap.Config.RGB_565).resize(((MyApplication) activity.getApplication()).screenWidth,((MyApplication) activity.getApplication()).screenHeight).centerInside().into(iv_vp_item);
        }


        iv_vp_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test","onClick");
                //执行动画
                if (!activity.isFullScreen) {
                    activity.fullScreen();
                }else{
                    activity.exitFullScreen();
                }
            }
        });
        return contentView;
    }





}
