package com.mzyl.album;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class EmoticonItemFragment extends Fragment {
    private GridView gv_emoticon;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final int[] icons = getArguments().getIntArray("icons");
        View view = inflater.inflate(R.layout.fragment_emoticon_item, container, false);
        gv_emoticon = view.findViewById(R.id.gv_emoticon);
        gv_emoticon.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return icons.length;
            }

            @Override
            public Object getItem(int position) {
                return icons[position];
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder = null;
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.recyclerview_emoticon_item, parent, false);
                    holder = new ViewHolder();
                    holder.iv_emoticon = convertView.findViewById(R.id.iv_emoticon);
                    holder.tv_description = convertView.findViewById(R.id.tv_description);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.iv_emoticon.setImageResource((Integer) getItem(position));
                return convertView;

            }

            class ViewHolder {
                ImageView iv_emoticon;
                TextView tv_description;
            }
        });
        return view;
    }
}
