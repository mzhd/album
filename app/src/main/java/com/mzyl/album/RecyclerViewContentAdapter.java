package com.mzyl.album;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            item.setOnClickListener(new MyOnclickListener(position));
            if (((MainActivity)context).checkedDatasPosition.contains(position)) {
                holder.itemView.findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                ((MyHolder) holder).cBox.setChecked(true);
            } else {
                holder.itemView.findViewById(R.id.overlay).setVisibility(View.GONE);
                ((MyHolder) holder).cBox.setChecked(false);
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
                case R.id.iv_item:
                    MainActivity activity = (MainActivity) RecyclerViewContentAdapter.this.context;
                    Intent intent = new Intent(activity, PreviewActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("isPreview", false);
                    intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) datas);
                    intent.putParcelableArrayListExtra("checked", (ArrayList<? extends Parcelable>) activity.checkedDatasPosition);
                    activity.startActivityForResult(intent, MainActivity.REQUEST_CODE_PREVIEW);
                    break;
                case R.id.cBox:
                    activity = (MainActivity) RecyclerViewContentAdapter.this.context;
                    boolean checked = ((CheckBox) v).isChecked();
                    if (checked) {
                        ((View) v.getParent()).findViewById(R.id.overlay).setVisibility(View.VISIBLE);
                        activity.checkedDatasPosition.add(position);

                    } else {
                        activity.checkedDatasPosition.remove(activity.checkedDatasPosition.indexOf(position));
                        ((View) v.getParent()).findViewById(R.id.overlay).setVisibility(View.GONE);
                    }
                    activity.updateView();
                    break;
            }
        }
    }



}
