package com.mzyl.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.List;

import javax.crypto.spec.IvParameterSpec;

import static android.widget.ImageView.ScaleType.CENTER_INSIDE;

public class RecyclerViewContentAdapter extends RecyclerView.Adapter {
    private Context context;
    private LayoutInflater inflater;
    private List datas;

    public RecyclerViewContentAdapter(Context context, List datas) {
        this.context = context;
        this.datas = datas;
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override

    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recyclerview_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MyHolder) {
            final ImageView item = ((MyHolder) holder).iv_item;
            ImageEntry entry = (ImageEntry) datas.get(position);
            Picasso.get().load(new File(entry.getData())).tag("content").fit().into(item);

            if (((MainActivity) context).checkedDatas.contains(datas.get(position))) {
                ((MyHolder) holder).cBox.setChecked(true);
                holder.itemView.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
            } else {
                ((MyHolder) holder).cBox.setChecked(false);
                holder.itemView.findViewById(R.id.overlay).setVisibility(View.GONE);
            }
            ((MyHolder) holder).cBox.setOnClickListener(new MyOnclickListener(position));
        }
    }

    @Override
    public int getItemCount() {
        return datas != null ? datas.size() : 0;
    }

    public final void notifyDataSetChanged(List datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        AppCompatImageView iv_item;
        CheckBox cBox;


        public MyHolder(final View itemView) {
            super(itemView);
            iv_item = itemView.findViewById(R.id.iv_item);
            cBox = itemView.findViewById(R.id.cBox);

        }
    }

    class MyOnclickListener implements View.OnClickListener {
        int position;

        public MyOnclickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cBox:
                    boolean checked = ((CheckBox) v).isChecked();
                    if (checked) {
                        if (!((MainActivity) context).checkedDatas.contains(datas.get(position)))
                            ((MainActivity) context).checkedDatas.add(datas.get(position));
                        ((View)v.getParent()).findViewById(R.id.overlay).setVisibility(View.VISIBLE);

                    } else {
                        if (((MainActivity) context).checkedDatas.contains(datas.get(position)))
                            ((MainActivity) context).checkedDatas.remove(datas.get(position));
                        ((View)v.getParent()).findViewById(R.id.overlay).setVisibility(View.GONE);

                    }
                    ((Button) ((MainActivity) context).findViewById(R.id.btn_send)).setText("发送(" + ((MainActivity) context).checkedDatas.size() + "/9)");
                    if (((MainActivity) context).checkedDatas.size() == 0) {
                        ((Button) ((MainActivity) context).findViewById(R.id.btn_send)).setText("发送");
                        ( ((MainActivity) context).findViewById(R.id.btn_send)).setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_dark));

                    } else {
                        (((MainActivity) context).findViewById(R.id.btn_send)).setBackgroundColor(Color.GREEN);

                    }
                    break;
            }
        }
    }
}
