package com.mzyl.album;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_EDIT = 0;
    public List datas;
    public List checkedDatas;
    public int position;
    private Button btn_send;
    private Toolbar toolbar;

    private ViewPager vp_content;
    private RecyclerView rv_preview;
    private RadioButton rb_origin_picture;
    private boolean rb_isChecked;
    private CheckBox cb_check;
    /**
     * isPreview返回true时使用
     */
    private List preDelete;

    private View ll_bottom;
    boolean isFullScreen;
    boolean isPreview;
    private TextView tv_edit;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //下面两行是防止设置状态栏时，actvitiy重新布局
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        showStatusBar();

        setContentView(R.layout.activity_preview);

        datas = getIntent().getParcelableArrayListExtra("data");
        checkedDatas = getIntent().getIntegerArrayListExtra("checked");
        position = getIntent().getIntExtra("position", 0);
        isPreview = getIntent().getBooleanExtra("isPreview", false);

        toolbar = findViewById(R.id.toolbar);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
        layoutParams.topMargin = getStatusBarHeight(PreviewActivity.this);
        toolbar.setLayoutParams(layoutParams);
        if (isPreview) {
            toolbar.setTitle(1 + "/" + checkedDatas.size());

        } else {
            toolbar.setTitle(position + 1 + "/" + datas.size());

        }
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_send = findViewById(R.id.btn_send);
        if (checkedDatas.size() == 0) {
            btn_send.setText("发送");
        } else {
            btn_send.setText("发送(" + checkedDatas.size() + "/9)");
        }
        btn_send.setBackgroundColor(Color.GREEN);
        btn_send.setOnClickListener(PreviewActivity.this);

        vp_content = findViewById(R.id.vp_content);
        List useDatas = new ArrayList();
        if (isPreview) {
            for (int i = 0; i < checkedDatas.size(); i++) {
                useDatas.add(datas.get((Integer) checkedDatas.get(i)));
            }
        } else {
            useDatas = datas;
        }
        vp_content.setAdapter(new VPAdapter(getSupportFragmentManager(), useDatas));

        vp_content.setCurrentItem(position);
        vp_content.setOffscreenPageLimit(3);


        ll_bottom = findViewById(R.id.ll_bottom);
        rv_preview = findViewById(R.id.rv_preview);
        rv_preview.setLayoutManager(new LinearLayoutManager(PreviewActivity.this, LinearLayoutManager.HORIZONTAL, false));
        rv_preview.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.recyclerview_preview_item, parent, false);
                return new MyHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
                if (holder instanceof MyHolder) {
                    ImageEntry entry = (ImageEntry) datas.get((Integer) checkedDatas.get(position));
                    MyHolder myHolder = (MyHolder) holder;

                    if (preDelete.contains(checkedDatas.get(position))) {
                        myHolder.view_shade.setBackground(PreviewActivity.this.getResources().getDrawable(R.drawable.recyclerview_preview_view_unchecked));
                    } else {
                        myHolder.view_shade.setBackground(PreviewActivity.this.getResources().getDrawable(R.drawable.recyclerview_preview_view_checked));

                    }

                    Picasso.get().load(new File(entry.getData())).fit().into(myHolder.iv_rv_preview);
                    ViewGroup.MarginLayoutParams margin = (ViewGroup.MarginLayoutParams) myHolder.iv_rv_preview.getLayoutParams();
                    if ((position + 1) == checkedDatas.size()) {
                        margin.rightMargin = 0;
                    } else {
                        margin.rightMargin = Utils.dp2px(PreviewActivity.this, 20);
                    }
                    myHolder.iv_rv_preview.setLayoutParams(margin);
                    myHolder.fly.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isPreview) {
                                vp_content.setCurrentItem(position);
                            } else {
                                vp_content.setCurrentItem((Integer) checkedDatas.get(position));
                            }
                        }
                    });
                }
            }

            @Override
            public int getItemCount() {
                return checkedDatas.size();
            }

            class MyHolder extends RecyclerView.ViewHolder {
                ImageView iv_rv_preview;
                View view_shade;
                ViewGroup fly;

                public MyHolder(View itemView) {
                    super(itemView);
                    fly = itemView.findViewById(R.id.fly);
                    iv_rv_preview = itemView.findViewById(R.id.iv_rv_preview);
                    view_shade = itemView.findViewById(R.id.view_shade);
                }
            }
        });

        tv_edit = findViewById(R.id.tv_edit);
        tv_edit.setOnClickListener(PreviewActivity.this);

        rb_origin_picture = findViewById(R.id.rb_origin_picture);
        rb_origin_picture.setOnClickListener(PreviewActivity.this);

        cb_check = findViewById(R.id.cb_check);
        cb_check.setOnClickListener(PreviewActivity.this);
        if (isPreview) {
            cb_check.setChecked(true);
        } else {
            if (checkedDatas.contains(position))
                cb_check.setChecked(true);
        }


        preDelete = new ArrayList<Integer>();

        vp_content.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                int currentPosition;
                if (isPreview) {
                    toolbar.setTitle(position + 1 + "/" + checkedDatas.size());
                    currentPosition = (int) checkedDatas.get(position);
                } else {
                    toolbar.setTitle(position + 1 + "/" + datas.size());
                    currentPosition = position;
                }
                if (preDelete.contains(currentPosition)) {
                    cb_check.setChecked(false);
                } else {
                    if (checkedDatas.contains(currentPosition))
                        cb_check.setChecked(true);
                    else
                        cb_check.setChecked(false);
                }
                rv_preview.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFullScreen) {
            hideStatusBar();
        } else {
            showStatusBar();
        }
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

    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                //发送
                break;
            case R.id.tv_edit:
                //编辑
                Intent intent = new Intent(PreviewActivity.this, EditActivity.class);
                ImageEntry entry = null;
                if (isPreview) {
                    entry = (ImageEntry) datas.get((Integer) checkedDatas.get(vp_content.getCurrentItem()));
                } else {
                    entry = (ImageEntry) datas.get(vp_content.getCurrentItem());
                }
                intent.putExtra("image", entry);
                startActivityForResult(intent, REQUEST_CODE_EDIT);
                break;
            case R.id.rb_origin_picture:
                //原图
                if (rb_isChecked) {
                    rb_isChecked = false;
                    rb_origin_picture.setChecked(rb_isChecked);
                } else {
                    rb_isChecked = true;
                    rb_origin_picture.setChecked(rb_isChecked);
                }
                break;
            case R.id.cb_check:
                //选择
                boolean checked = cb_check.isChecked();
                if (checked) {
                    if (isPreview) {
                        preDelete.remove(preDelete.indexOf(checkedDatas.get(vp_content.getCurrentItem())));
                        int lastSize = checkedDatas.size() - preDelete.size();
                        if (lastSize == 0) {
                            btn_send.setText("发送");
                        } else {
                            btn_send.setText("发送(" + lastSize + "/9");
                        }
                    } else {
                        checkedDatas.add(vp_content.getCurrentItem());
                        if (checkedDatas.size() == 0) {
                            btn_send.setText("发送");
                        } else {
                            btn_send.setText("发送(" + checkedDatas.size() + "/9)");
                        }
                    }
                    rv_preview.setVisibility(View.VISIBLE);
                } else {
                    if (isPreview) {
                        preDelete.add(checkedDatas.get(vp_content.getCurrentItem()));
                        if (preDelete.size() == checkedDatas.size()) {
                            rv_preview.setVisibility(View.GONE);
                        }
                        int lastSize = checkedDatas.size() - preDelete.size();
                        if (lastSize == 0) {
                            btn_send.setText("发送");
                        } else {
                            btn_send.setText("发送(" + lastSize + "/9");
                        }
                    } else {
                        checkedDatas.remove(checkedDatas.indexOf(vp_content.getCurrentItem()));
                        if (checkedDatas.size() == 0) {
                            rv_preview.setVisibility(View.GONE);
                        }
                        if (checkedDatas.size() == 0) {
                            btn_send.setText("发送");
                        } else {
                            btn_send.setText("发送(" + checkedDatas.size() + "/9)");
                        }
                    }
                }
                rv_preview.getAdapter().notifyDataSetChanged();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_EDIT:

                break;
        }
    }

    public void exitFullScreen() {
        showStatusBar();
        ll_bottom.setVisibility(View.VISIBLE);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(toolbar, "y", getStatusBarHeight(PreviewActivity.this));
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isFullScreen = false;

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
        Animator animator = AnimatorInflater.loadAnimator(PreviewActivity.this, R.animator.preview_bottom_enter_animator);
        animator.setTarget(ll_bottom);
        animator.start();
    }

    public void fullScreen() {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(toolbar, "y", -(toolbar.getHeight() + getStatusBarHeight(PreviewActivity.this)));
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ll_bottom.setVisibility(View.INVISIBLE);
                hideStatusBar();
                isFullScreen = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();
        Animator animator = AnimatorInflater.loadAnimator(PreviewActivity.this, R.animator.preview_bottom_exit_animator);
        animator.setTarget(ll_bottom);
        animator.start();
    }


    @Override
    public void finish() {
        dealData();
        super.finish();
    }

    private void dealData() {
        if (isPreview) {
            Iterator iterator = preDelete.iterator();
            while (iterator.hasNext()) {
                checkedDatas.remove(checkedDatas.indexOf(iterator.next()));
            }
        }
        Intent result = new Intent();
        result.putParcelableArrayListExtra("checked", (ArrayList<? extends Parcelable>) checkedDatas);
        setResult(0, result);
    }
}
