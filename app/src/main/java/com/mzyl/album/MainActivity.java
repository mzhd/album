package com.mzyl.album;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_EXTERNAL_STORAGE = 0;
    public static final int REQUEST_CODE_PREVIEW = 1;

    private String[] PERMISSIONS_STORAGE = {
            READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
    };

    RecyclerView rv_content;
    private Toolbar toolbar;
    private TextView tv_classify;
    private RadioButton rb_origin_picture;
    private TextView tv_preview;
    public RecyclerView.Adapter adapter;
    public Map mapDatas;
    public List datas;
    public List checkedDatasPosition;
    private View rv_bottom;

    public BottomSheetBehavior behavior;
    public RecyclerView.Adapter classifyAdapter;
    private RecyclerView rv_popwindow_content;
    private View touch_outside;
    private Button btn_send;
    private MyTask myTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("图片和视频");
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_send = findViewById(R.id.btn_send);
        btn_send.setOnClickListener(MainActivity.this);

        rv_content = findViewById(R.id.rv_content);
        final GridLayoutManager gridManager = new GridLayoutManager(MainActivity.this, 4);
        rv_content.setLayoutManager(gridManager);
        adapter = new RecyclerViewContentAdapter(MainActivity.this, datas);
        rv_content.setAdapter(adapter);
        rv_content.addItemDecoration(new GridItemDecoration(2));
        rv_content.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING) {
                    Picasso.get().resumeTag("content");
                } else {
                    Picasso.get().pauseTag("content");
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        rv_bottom = findViewById(R.id.rv_bottom);
        tv_classify = findViewById(R.id.tv_classify);
        tv_classify.setOnClickListener(this);
        rb_origin_picture = findViewById(R.id.rb_origin_picture);
        rb_origin_picture.setOnClickListener(MainActivity.this);
        tv_preview = findViewById(R.id.tv_preview);
        tv_preview.setOnClickListener(this);
        rv_popwindow_content = findViewById(R.id.rv_popwindow_content);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_popwindow_content.setLayoutManager(manager);
        classifyAdapter = new RecyclerViewClassifyAdapter(MainActivity.this, mapDatas);
        rv_popwindow_content.setAdapter(classifyAdapter);
        rv_popwindow_content.addItemDecoration(new LinearItemDecoration(1));
        FrameLayout bottom_sheet = findViewById(R.id.design_bottom_sheet);
        behavior = BottomSheetBehavior.from(bottom_sheet);
        behavior.setPeekHeight(0);
        behavior.setHideable(true);

        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
//                    //
//                    touch_outside.setVisibility(View.VISIBLE);
//                } else {
//                    touch_outside.setVisibility(View.GONE);
//                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        touch_outside = findViewById(R.id.touch_outside);
        touch_outside.setOnClickListener(MainActivity.this);

        datas = new ArrayList();
        checkedDatasPosition = new ArrayList();
        mapDatas = new HashMap<String, List<ImageEntry>>();
        myTask = new MyTask(MainActivity.this);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        ((MyApplication) getApplication()).screenWidth = outMetrics.widthPixels;
        ((MyApplication) getApplication()).screenHeight = outMetrics.heightPixels;

        int result = ActivityCompat.checkSelfPermission(MainActivity.this, READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE, REQUEST_CODE_EXTERNAL_STORAGE);
        } else {
            executeTask();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PREVIEW:
                checkedDatasPosition =data.getIntegerArrayListExtra("checked");
                updateView();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_EXTERNAL_STORAGE:
                boolean grantAllPermissions = true;
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        //获取权限成功  这里可以具体判断是什么权限

                    } else {
                        Toast.makeText(MainActivity.this, permissions[i] + "获取失败", Toast.LENGTH_LONG).show();
                        grantAllPermissions = false;
                        break;
                    }
                }
                if (grantAllPermissions)
                    executeTask();
                break;
        }
    }


    boolean rb_isChecked;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //发送
                break;
            case R.id.tv_classify:
                //选择类型
                if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    touch_outside.setVisibility(View.GONE);
                } else {
                    touch_outside.setVisibility(View.VISIBLE);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                break;
            case R.id.tv_preview:
                //预览
                if (checkedDatasPosition.size() == 0) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                intent.putExtra("isPreview",true);
                intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) datas);
                intent.putIntegerArrayListExtra("checked", (ArrayList<Integer>) checkedDatasPosition);
                startActivityForResult(intent, REQUEST_CODE_PREVIEW);

                break;
            case R.id.rb_origin_picture:
                if (rb_isChecked) {
                    rb_isChecked = false;
                    rb_origin_picture.setChecked(rb_isChecked);
                } else {
                    rb_isChecked = true;
                    rb_origin_picture.setChecked(rb_isChecked);
                }
                break;
            case R.id.touch_outside:
                dismissBottomSheet();
                break;

        }
    }

    public void dismissBottomSheet() {
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        touch_outside.setVisibility(View.GONE);
    }

    public void updateView() {
        if (checkedDatasPosition.size() == 0) {
            btn_send.setText("发送");
            btn_send.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
            tv_preview.setText("预览");
            tv_preview.setTextColor(getResources().getColor(android.R.color.darker_gray));
        } else {
            btn_send.setBackgroundColor(Color.GREEN);
            btn_send.setText("发送(" + checkedDatasPosition.size() + "/9)");
            tv_preview.setText("预览(" + checkedDatasPosition.size() + ")");
            tv_preview.setTextColor(getResources().getColor(android.R.color.white));
        }
        adapter.notifyDataSetChanged();
    }


    private void executeTask() {
        myTask.execute();
    }


}
