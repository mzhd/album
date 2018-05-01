package com.mzyl.album;

public class ImageEntry {
    //文件名
    private String display_name;
    //文件名不带后缀
    private  String title;
    //类型
    private  String bucket_display_name;
    //位置
    private  String data;
    //大小
    private  int size;
    //图片类型
    private   String  mime_type;
    //日期
    private    long data_add;

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBucket_display_name() {
        return bucket_display_name;
    }

    public void setBucket_display_name(String bucket_display_name) {
        this.bucket_display_name = bucket_display_name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public long getData_add() {
        return data_add;
    }

    public void setData_add(long data_add) {
        this.data_add = data_add;
    }
}
