package com.mzyl.album;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MyTask extends AsyncTask<String, String, List<ImageEntry>> {
    private Context context;

    public MyTask(Context context) {
        this.context = context;
    }

    private Map mapDatas;

    @Override
    protected List<ImageEntry> doInBackground(String... strings) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor query = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Images.ImageColumns.DATE_ADDED);

        if (context instanceof MainActivity) {
            mapDatas = ((MainActivity) context).mapDatas;
        }
        List<ImageEntry> images = ((MainActivity) context).datas;

        while (query != null && query.moveToNext()) {
            ImageEntry image = new ImageEntry();
            //文件名
            String display_name = query.getString(query.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
            image.setDisplay_name(display_name);
            //文件名不带后缀
            String title = query.getString(query.getColumnIndex(MediaStore.Images.ImageColumns.TITLE));
            image.setTitle(title);
            //类型
            String bucket_displaye_name = query.getString(query.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME));
            image.setBucket_display_name(bucket_displaye_name);
            //位置
            String data = query.getString(query.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            image.setData(data);
            //大小
            int size = query.getInt(query.getColumnIndex(MediaStore.Images.ImageColumns.SIZE));
            image.setSize(size);
            //图片类型
            String mime_type = query.getString(query.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE));
            image.setMime_type(mime_type);
            //日期
            long data_add = query.getLong(query.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
            image.setData_add(data_add);

            images.add(0,image);
            mapDatas.put(bucket_displaye_name, null);

        }
        if (query != null)
            query.close();
        Iterator iterator = mapDatas.keySet().iterator();
        while (iterator.hasNext()) {
            String bucket_name = (String) iterator.next();
            List list = new ArrayList();
            for (ImageEntry image :
                    images) {
                if (image.getBucket_display_name().equals(bucket_name)) {
                    list.add(image);
                }
            }
            mapDatas.put(bucket_name, list);
        }

        mapDatas.put("图片和视频", images);
        return images;
    }

    @Override
    protected void onPostExecute(List<ImageEntry> imageEntries) {
        super.onPostExecute(imageEntries);
        if (context instanceof MainActivity) {
            ((RecyclerViewContentAdapter) ((MainActivity) context).adapter).notifyDataSetChanged(imageEntries);
            ((RecyclerViewClassifyAdapter) ((MainActivity) context).classifyAdapter).notifyDataSetChanged(mapDatas);
        }
    }
}
