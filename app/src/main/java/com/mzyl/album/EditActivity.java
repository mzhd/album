package com.mzyl.album;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EditActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    int[] colors = {
            R.color.white,
            R.color.black,
            R.color.red,
            R.color.orange,
            R.color.green,
            R.color.blue,
            R.color.brown,
            R.color.pink
    };
    ImageEntry entry;
    private CustomImageView2 iv_image;
    private TextView tv_cancel;
    private TextView tv_finish;

    boolean isFullScreenEdit;
    private View rl_option_lay;

    private ImageView iv_step_back;
    private LinearLayout lly_option;
    private LinearLayout lly_option_pen;
    private RadioGroup rg_option_mosaic;
    CustomView choiceView;
    private CheckBox cb_pen;
    private CheckBox cb_emoticon;
    private CheckBox cb_text;
    private CheckBox cb_mosaic;
    private CheckBox cb_cut;
    private PopupWindow popEmoticon;
    private RadioButton checkedItem;
    private ViewGroup lly_emoticon_main;
    private HashMap iconMaps;
    private RadioGroup rg_emoticon;
    private ViewPager vp_emoticon;
    int numColumns = 4;
    int numRows = 2;
    Map maps;
    int pages;
    Map itemPages;
    Map pageRanges;
    Map pageOffset;
    int oldPosition;
    private RadioButton rb_tuzi;
    private RadioButton rb_baozou;
    private RadioButton rb_liumangtu;
    private RadioButton rb_houzi;
    private RadioButton rb_heart;
    private RadioButton rb_back;
    private RelativeLayout fl_sign;
    private ViewGroup lly_sign;
    private CircleView currentCircle;
    private MyOnGlobalLayoutListener myOnGlobalLayoutListener;
    private RelativeLayout rl_edit;
    private ImageView iv_checked_emoticon;

    private RelativeLayout rl_draw_layer;
    private ScaleGestureDetector scaleGestureDetector;
    private RelativeLayout rv_draw_layer;

    private PopupWindow popupWindowText;
    private TextView tv_rubbish;
    private EditText et_pop_text;
    private CustomImageView customImageView;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    hideStatusBar();
                    break;
            }
        }
    };
    private ViewGroup lly_option_cut;
    private View iv_rotate_90;
    private View iv_cancel;
    private View tv_restore;
    private View iv_finish;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        hideStatusBar();


        setContentView(R.layout.activity_edit);

        entry = getIntent().getParcelableExtra("image");

        rl_edit = findViewById(R.id.rl_edit);
        iv_image = findViewById(R.id.iv_image);

        iv_image.setOnClickListener(EditActivity.this);
        iv_image.setOnTouchedListener(new CustomImageView2.OnTouchedListener() {
            @Override
            public void OnTouchedListener(CustomImageView2.Item item) {

            }

            @Override
            public void OnScrollListener(CustomImageView2.Item item) {
                fullScreen();
                if (item != null) {
                    tv_rubbish.setVisibility(View.VISIBLE);
                    Rect outRect = new Rect();
                    tv_rubbish.getGlobalVisibleRect(outRect);
                    Point detectionPoint = new Point(outRect.left + tv_rubbish.getWidth() / 2, outRect.top);
                    if (item.isRectContainPoint(item.getContentPts(), detectionPoint)) {
                        tv_rubbish.setTextColor(Color.RED);
                    } else {
                        tv_rubbish.setTextColor(Color.WHITE);
                    }
                }

            }

            @Override
            public void OnUpListener(CustomImageView2.Item item) {
                if (item != null) {
                    tv_rubbish.setVisibility(View.INVISIBLE);
                    if (tv_rubbish.getCurrentTextColor() == Color.RED) {
                        item.removeSelf();
                        tv_rubbish.setTextColor(Color.WHITE);
                    }
                }

            }
        });
        Picasso.get().load(new File(entry.getData())).config(Bitmap.Config.RGB_565).resize(((MyApplication) getApplication()).screenWidth, ((MyApplication) getApplication()).screenHeight).centerInside().into(iv_image);

        rl_option_lay = findViewById(R.id.rl_option_lay);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(EditActivity.this);
        tv_finish = findViewById(R.id.tv_finish);
        tv_finish.setOnClickListener(EditActivity.this);

        cb_pen = findViewById(R.id.cb_pen);
        cb_pen.setOnCheckedChangeListener(EditActivity.this);
        cb_emoticon = findViewById(R.id.cb_emoticon);
        cb_emoticon.setOnCheckedChangeListener(EditActivity.this);
        cb_text = findViewById(R.id.cb_text);
        cb_text.setOnCheckedChangeListener(EditActivity.this);
        cb_mosaic = findViewById(R.id.cb_mosaic);
        cb_mosaic.setOnCheckedChangeListener(EditActivity.this);
        cb_cut = findViewById(R.id.cb_cut);
        cb_cut.setOnCheckedChangeListener(EditActivity.this);

        lly_option = findViewById(R.id.lly_option);
        lly_option_pen = findViewById(R.id.lly_option_pen);

        for (int i = 0; i < colors.length; i++) {
            final CustomView customView = new CustomView(EditActivity.this);
            customView.setColor(getResources().getColor(colors[i]));
            LinearLayout.LayoutParams layoutParams;
            if (i == 2) {
                choiceView = customView;
                layoutParams = new LinearLayout.LayoutParams(60, 60);
                iv_image.setPenColor(getResources().getColor(colors[i]));
            } else {
                layoutParams = new LinearLayout.LayoutParams(45, 45);
            }
            layoutParams.weight = 1;
            customView.setLayoutParams(layoutParams);
            final int finalI = i;
            customView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (choiceView != null) {
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) choiceView.getLayoutParams();
                        params.width = 45;
                        params.height = 45;
                        choiceView.setLayoutParams(params);
                    }
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) customView.getLayoutParams();
                    params.width = 60;
                    params.height = 60;
                    customView.setLayoutParams(params);
                    choiceView = customView;
                    iv_image.setPenColor(getResources().getColor(colors[finalI]));
                }
            });

            lly_option_pen.addView(customView);
        }
        rg_option_mosaic = findViewById(R.id.rg_option_mosaic);
        rg_option_mosaic.setOnCheckedChangeListener(EditActivity.this);
        iv_step_back = findViewById(R.id.iv_step_back);
        iv_step_back.setOnClickListener(EditActivity.this);
        lly_emoticon_main = findViewById(R.id.lly_emoticon_main);

        pageRanges = new HashMap();
        itemPages = new HashMap();
        iconMaps = new HashMap();
        pageOffset = new HashMap();
        final List iconsLists = new ArrayList();
        final List iconsLists2 = new ArrayList();
        final int[] icons = {R.drawable.icon_001, R.drawable.icon_002, R.drawable.icon_003, R.drawable.icon_004, R.drawable.icon_005, R.drawable.icon_006, R.drawable.icon_007, R.drawable.icon_008, R.drawable.icon_009, R.drawable.icon_010, R.drawable.icon_011, R.drawable.icon_012, R.drawable.icon_013, R.drawable.icon_014};

        for (int i = 0; i < icons.length; i++) {
            iconsLists.add(icons[i]);
            iconsLists2.add(icons[i]);
            iconsLists2.add(icons[i]);

        }

        iconMaps.put(0, iconsLists);
        iconMaps.put(1, iconsLists);
        iconMaps.put(2, iconsLists2);
        iconMaps.put(3, iconsLists);
        iconMaps.put(4, iconsLists2);

        getPageCounts();


        rb_tuzi = findViewById(R.id.rb_tuzi);
        checkedItem = rb_tuzi;
        rb_baozou = findViewById(R.id.rb_baozou);
        rb_liumangtu = findViewById(R.id.rb_liumangtu);
        rb_houzi = findViewById(R.id.rb_houzi);
        rb_heart = findViewById(R.id.rb_heart);
        rb_back = findViewById(R.id.rb_back);

        rb_tuzi.setOnClickListener(EditActivity.this);
        rb_baozou.setOnClickListener(EditActivity.this);
        rb_liumangtu.setOnClickListener(EditActivity.this);
        rb_houzi.setOnClickListener(EditActivity.this);
        rb_heart.setOnClickListener(EditActivity.this);
        rb_back.setOnClickListener(EditActivity.this);

        rg_emoticon = findViewById(R.id.rg_emoticon);
        rg_emoticon.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = rg_emoticon.findViewById(checkedId);
                if (radioButton.isChecked()) {
                    radioButton.setBackground(getResources().getDrawable(R.drawable.black_background));
                    if (checkedItem != null) {
                        checkedItem.setBackground(null);
                    }
                    checkedItem = radioButton;
                }


            }
        });
        vp_emoticon = findViewById(R.id.vp_emoticon);
        vp_emoticon.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {


            @Override
            public Fragment getItem(int position) {
                EmoticonFragment fragment = new EmoticonFragment();
                Bundle args = new Bundle();
                args.putIntegerArrayList("icons", (ArrayList<Integer>) maps.get(position));

                fragment.setArguments(args);
                return fragment;
            }

            @Override
            public int getCount() {
                return pages;
            }
        });
        vp_emoticon.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                Iterator iterator = pageRanges.keySet().iterator();
                int key = 0;
                while (iterator.hasNext()) {
                    key = (int) iterator.next();
                    int[] range = (int[]) pageRanges.get(key);
                    if (position >= range[0] && position <= range[1]) {
                        ((RadioButton) rg_emoticon.getChildAt(key)).setChecked(true);
                        break;
                    }
                }
                pageOffset.put(key, position);
                int counts = (int) itemPages.get(key);
                lly_sign.removeAllViews();

                for (int i = 0; i < counts; i++) {
                    final CircleView view = new CircleView(EditActivity.this);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
                    if (i != counts - 1) {
                        params.rightMargin = 30;
                    }
                    view.setLayoutParams(params);
                    view.changeColor(Color.GRAY);
                    lly_sign.addView(view);

                }
                int[] range = (int[]) pageRanges.get(key);
                final int relativeOffset = position - range[0];
                myOnGlobalLayoutListener.setIndex(relativeOffset);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fl_sign = findViewById(R.id.fl_sign);
        lly_sign = findViewById(R.id.lly_sign);
        int counts = (int) itemPages.get(0);
        for (int i = 0; i < counts; i++) {
            CircleView view = new CircleView(EditActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
            if (i != counts - 1) {
                params.rightMargin = 30;
            }
            view.setLayoutParams(params);
            view.changeColor(Color.GRAY);
            lly_sign.addView(view);
        }
        currentCircle = new CircleView(EditActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(30, 30);
        currentCircle.setLayoutParams(params);
        currentCircle.changeColor(Color.BLACK);
        fl_sign.addView(currentCircle);
        myOnGlobalLayoutListener = new MyOnGlobalLayoutListener();
        myOnGlobalLayoutListener.setIndex(0);
        lly_sign.getViewTreeObserver().addOnGlobalLayoutListener(myOnGlobalLayoutListener);

        //iv_checked_emoticon=findViewById(R.id.iv_checked_emoticon);

        tv_rubbish = findViewById(R.id.tv_rubbish);
        lly_option_cut = findViewById(R.id.lly_option_cut);
        iv_rotate_90 = findViewById(R.id.iv_rotate_90);
        iv_rotate_90.setOnClickListener(EditActivity.this);
        iv_cancel = findViewById(R.id.iv_cancel);
        iv_cancel.setOnClickListener(EditActivity.this);
        tv_restore = findViewById(R.id.tv_restore);
        tv_restore.setOnClickListener(EditActivity.this);
        iv_finish = findViewById(R.id.iv_finish);
        iv_finish.setOnClickListener(EditActivity.this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        hideStatusBar();
    }


    class MyOnGlobalLayoutListener implements ViewTreeObserver.OnGlobalLayoutListener {
        int index;

        public void setIndex(int index) {
            this.index = index;
        }


        @Override
        public void onGlobalLayout() {
            currentCircle.setX(lly_sign.getChildAt(index).getX() + lly_sign.getX());
            currentCircle.setY(lly_sign.getChildAt(index).getY() + lly_sign.getY());
        }
    }

    int index;

    private int getPageCounts() {
        if (pages != 0) {
            return pages;
        }
        if (maps == null) {
            maps = new HashMap();
        }
        int pageIConCounts = numColumns * numRows;
        for (int i = 0; i < iconMaps.keySet().size(); i++) {
            List list = (List) iconMaps.get(i);
            int page = list.size() % pageIConCounts == 0 ? list.size() / pageIConCounts : list.size() / pageIConCounts + 1;
            itemPages.put(i, page);
            for (int j = 0; j < page; j++) {
                maps.put(index++, new ArrayList<>(list.subList(j * pageIConCounts, (j + 1) * pageIConCounts > list.size() ? (list.size()) : ((j + 1) * pageIConCounts))));
            }
            int[] range = new int[2];
            range[0] = pages;
            range[1] = pages + page - 1;
            pageRanges.put(i, range);
            pageOffset.put(i, range[0]);
            pages += page;
        }
        return pages;
    }

    private void hideStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    mHandler.sendEmptyMessageDelayed(0, 3000);
                }
            }
        });
    }

    private void showStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_image:
                if (iv_image.changedTextItem != null) {
                    et_pop_text.setText((iv_image.changedTextItem).getText());
                    et_pop_text.setSelection(iv_image.changedTextItem.getText().length());
                    popupWindowText.showAsDropDown(getWindow().getDecorView(), 0, -getWindow().getDecorView().getHeight());
                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//这里给它设置了弹出的时间，
                    imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (lly_emoticon_main.getVisibility() == View.VISIBLE) {
                    lly_emoticon_main.setVisibility(View.INVISIBLE);
                    cb_emoticon.setChecked(false);
                    break;
                }

                if (!cb_cut.isChecked()) {
                    if (isFullScreenEdit) {
                        exitFullScreen();
                    } else {
                        fullScreen();
                    }
                }

                break;
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_finish:
                finish();
                Intent intent = new Intent();
                intent.putExtra("image", entry);
                setResult(PreviewActivity.REQUEST_CODE_EDIT, intent);
                break;
            case R.id.iv_step_back:
                //回退
                iv_image.stepBack();
                break;
            case R.id.rb_tuzi:
                int index = rg_emoticon.indexOfChild(rb_tuzi);
                int oldOffset = (int) pageOffset.get(index);
                vp_emoticon.setCurrentItem(oldOffset);
                break;
            case R.id.rb_baozou:
                index = rg_emoticon.indexOfChild(rb_baozou);
                oldOffset = (int) pageOffset.get(index);
                vp_emoticon.setCurrentItem(oldOffset);
                break;
            case R.id.rb_liumangtu:
                index = rg_emoticon.indexOfChild(rb_liumangtu);
                oldOffset = (int) pageOffset.get(index);
                vp_emoticon.setCurrentItem(oldOffset);
                break;
            case R.id.rb_houzi:
                index = rg_emoticon.indexOfChild(rb_houzi);
                oldOffset = (int) pageOffset.get(index);
                vp_emoticon.setCurrentItem(oldOffset);
                break;
            case R.id.rb_heart:
                index = rg_emoticon.indexOfChild(rb_heart);
                oldOffset = (int) pageOffset.get(index);
                vp_emoticon.setCurrentItem(oldOffset);
                break;
            case R.id.rb_back:
                lly_emoticon_main.setVisibility(View.INVISIBLE);
                cb_emoticon.setChecked(false);
                break;
            case R.id.tv_pop_finish:
                popupWindowText.dismiss();
                iv_image.addText(et_pop_text.getText().toString(), et_pop_text.getCurrentTextColor());
                break;
            case R.id.tv_pop_cancel:
                popupWindowText.dismiss();
                break;
            case R.id.iv_rotate_90:
                //裁剪旋转

                break;
            case R.id.iv_cancel:
                //关闭裁剪操作
                cb_cut.setChecked(false);
                break;
            case R.id.tv_restore:
                //裁剪还原
                break;
            case R.id.iv_finish:
                //裁剪完成

                break;

        }
    }

    public void exitFullScreen() {
        rl_option_lay.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rl_option_lay, "alpha", 1);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isFullScreenEdit = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
    }

    public void fullScreen() {
        hideStatusBar();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(rl_option_lay, "alpha", 0);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rl_option_lay.setVisibility(View.INVISIBLE);
                isFullScreenEdit = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_mosaic:
                RadioButton rb_mosaic = group.findViewById(checkedId);
                if (rb_mosaic.isChecked()) {
                    iv_image.setCanDraw(true);
                    iv_image.setDrawType(CustomImageView2.DrawType.MOSAIC);
                }
                break;
        }
    }

    View choiceView2;

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.cb_pen:
                //画笔
                if (isChecked) {
                    lly_option.setVisibility(View.VISIBLE);
                    lly_option_pen.setVisibility(View.VISIBLE);
                    cb_mosaic.setChecked(false);

                    iv_image.setCanDraw(true);
                    iv_image.setDrawType(CustomImageView2.DrawType.LINE);
                } else {
                    lly_option_pen.setVisibility(View.INVISIBLE);
                    if (!cb_mosaic.isChecked()) {
                        lly_option.setVisibility(View.INVISIBLE);
                        iv_image.setCanDraw(false);
                    }


                }
                break;
            case R.id.cb_emoticon:
                //表情
                if (isChecked) {
                    lly_emoticon_main.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.cb_text:
                //文本
                if (!cb_text.isChecked()) {
                    return;
                }
                customImageView = null;
                if (popupWindowText == null) {
                    View view = LayoutInflater.from(EditActivity.this).inflate(R.layout.popupwindow_text, null);
                    view.findViewById(R.id.tv_pop_cancel).setOnClickListener(EditActivity.this);
                    view.findViewById(R.id.tv_pop_finish).setOnClickListener(EditActivity.this);
                    et_pop_text = view.findViewById(R.id.et_pop_text);
                    LinearLayout lly_pop_colors = view.findViewById(R.id.lly_pop_colors);
                    for (int i = 0; i < colors.length; i++) {
                        final CustomView customView = new CustomView(EditActivity.this);
                        customView.setColor(getResources().getColor(colors[i]));
                        LinearLayout.LayoutParams layoutParams;
                        if (i == 0) {
                            choiceView2 = customView;
                            layoutParams = new LinearLayout.LayoutParams(60, 60);
                            et_pop_text.setTextColor(getResources().getColor(colors[i]));
                        } else {
                            layoutParams = new LinearLayout.LayoutParams(45, 45);
                        }
                        layoutParams.weight = 1;
                        customView.setLayoutParams(layoutParams);
                        final int finalI = i;
                        customView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (choiceView2 != null) {
                                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) choiceView2.getLayoutParams();
                                    params.width = 45;
                                    params.height = 45;
                                    choiceView2.setLayoutParams(params);
                                }
                                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) customView.getLayoutParams();
                                params.width = 60;
                                params.height = 60;
                                customView.setLayoutParams(params);
                                choiceView2 = customView;
                                et_pop_text.setTextColor(getResources().getColor(colors[finalI]));
                            }
                        });
                        lly_pop_colors.addView(customView);
                    }
                    popupWindowText = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                    popupWindowText.setFocusable(true);
                    popupWindowText.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    popupWindowText.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            cb_text.setChecked(false);
                        }
                    });
                    popupWindowText.setBackgroundDrawable(new BitmapDrawable(getResources(), ""));
                    popupWindowText.setAnimationStyle(R.style.popupwindow_animation_style);
                }
                et_pop_text.setText("");
                popupWindowText.showAsDropDown(getWindow().getDecorView(), 0, -getWindow().getDecorView().getHeight());
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//这里给它设置了弹出的时间，
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
            case R.id.cb_mosaic:
                //马赛克
                if (isChecked) {
                    lly_option.setVisibility(View.VISIBLE);
                    rg_option_mosaic.setVisibility(View.VISIBLE);
                    int checkedRadioButtonId = rg_option_mosaic.getCheckedRadioButtonId();
                    switch (checkedRadioButtonId) {
                        case R.id.rb_mosaic:
                            RadioButton rb_mosaic = rg_option_mosaic.findViewById(checkedRadioButtonId);
                            if (rb_mosaic.isChecked()) {
                                iv_image.setCanDraw(true);
                                iv_image.setDrawType(CustomImageView2.DrawType.MOSAIC);
                            }
                            break;
                    }
                    cb_pen.setChecked(false);
                } else {
                    rg_option_mosaic.setVisibility(View.INVISIBLE);
                    if (!cb_pen.isChecked()) {
                        lly_option.setVisibility(View.INVISIBLE);
                        iv_image.setCanDraw(false);
                    }
                }
                break;
            case R.id.cb_cut:
                //裁剪
                System.out.println("R.id.cb_cut");
                if (isChecked) {
                    rl_option_lay.setVisibility(View.INVISIBLE);
                    lly_option_cut.setVisibility(View.VISIBLE);

                    iv_image.startCutMode();
                } else {
                    rl_option_lay.setVisibility(View.VISIBLE);
                    lly_option_cut.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (lly_emoticon_main.getVisibility() == View.VISIBLE) {
            lly_emoticon_main.setVisibility(View.INVISIBLE);
            cb_emoticon.setChecked(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (myOnGlobalLayoutListener != null)
            lly_sign.getViewTreeObserver().removeOnGlobalLayoutListener(myOnGlobalLayoutListener);
        super.onDestroy();
    }


}
