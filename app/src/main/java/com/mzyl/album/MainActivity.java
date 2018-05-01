package com.mzyl.album;

import android.content.res.TypedArray;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    RecyclerView rv_content;
    private Toolbar toolbar;
    private TextView tv_classify;
    private RadioButton rb_origin_picture;
    private TextView tv_preview;
    public RecyclerView.Adapter adapter;
    public Map mapDatas;
    public List datas;
    public List checkedDatas;
    private View rv_bottom;
    int screenWidth;
    int screenHeight;
    public BottomSheetBehavior behavior;
    public RecyclerView.Adapter classifyAdapter;
    private RecyclerView rv_popwindow_content;
    private View touch_outside;
    private Button btn_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("图片和视频");
        //toolbar.setLogo(R.drawable.ic_launcher_background);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_send=findViewById(R.id.btn_send);
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

        checkedDatas = new ArrayList<ImageEntry>();
        mapDatas = new HashMap<String, List<ImageEntry>>();
        MyTask myTask = new MyTask(MainActivity.this);
        myTask.execute();
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    PopupWindow popupWindow;
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
             break;
            case R.id.rb_origin_picture:
                if (rb_isChecked) {
                    rb_isChecked=false;
                    rb_origin_picture.setChecked(rb_isChecked);
                }else{
                    rb_isChecked=true;
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


    private void showPopupWindow() {
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popupdinwdow_classify, null, false);
        RecyclerView rv_popwindow_content = contentView.findViewById(R.id.rv_popwindow_content);
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        rv_popwindow_content.setLayoutManager(manager);
        rv_popwindow_content.setAdapter(new RecyclerViewClassifyAdapter(MainActivity.this, mapDatas));

        popupWindow = new PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, rv_content.getHeight() - rv_bottom.getHeight());
        int xoff = 0;
        int yoff = rv_content.getHeight();
        popupWindow.setAnimationStyle(R.style.popupwindow_animation_style);
        popupWindow.showAsDropDown(rv_bottom, xoff, -yoff);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });
    }


}
