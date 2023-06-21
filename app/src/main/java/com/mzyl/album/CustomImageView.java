package com.mzyl.album;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import com.almeros.android.multitouch.RotateGestureDetector;

public class CustomImageView extends AppCompatImageView {


    public static final float MAX_SCALE = 20;
    private static final float MIN_SCALE = .1f;
    public static final float BEYOND_EGED_LENGTH = 30;
    private final int screenWidth;
    private final int screenHeight;

    private TextPaint textPaint;
    private final RectF contentRect;
    float contentPivotX = 0;
    float contentPivotY = 0;
    float contentWidth = 0;
    float contentHeight = 0;

    private final RectF regionRect;
    private final Path regionPath;
    private final Path outFramePath;
    private final RectF leftTopCornerRect;
    private final RectF rightTopCornerRect;
    private final RectF rightBottomCornerRect;
    private final RectF leftBottomConrnerRect;
    private final float[] leftTopCornerPts;
    private final float[] rightTopCornerPts;
    private final float[] rigthBottomCornerPts;
    private final float[] leftBottomCornerPts;
    private final Path leftTopCornerPath;
    private final Path rightTopCornerPath;
    private final Path rightBottomCornerPath;
    private final Path leftBottomCornerPath;
    private float frameSize;
    private float touchRegionSize;

    private Runnable action;
    private float MAX_SCALE_FACTOR;
    private Paint paint;
    public RectF outFrameRect;
    int stokeWidth = 6;
    int hornRectEdgeLength = 30;
    //外边框的大小
    public static final int FRAME_SIZE_EXTRA = 50;
    public static final int REGION_SIZE_EXTRA = 100;

    private RotateGestureDetector rotateGestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector detector;
    float scale;
    boolean isZoomed;

    Matrix matrix;
    //图片的中心点
    private Point anchor;
    private float degrees;
    private float scaleFactor = 1;
    private DealRunnable runnable;



    boolean isFirstShow = true;
    //代表矩形的四个角坐标值，顺序为： 左上 右上 右下 左下
    private float[] regionPts;
    public float[] outFramePts;
    Layout selfLayout;
    Layout layout;

    public void setLayout(Layout layout) {
        this.layout = layout;
        textPaint.setColor(layout.getPaint().getColor());
    }

    String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;

        reset();
    }

    private int color;

    public void setColor(int color) {
        this.color = color;
    }

    public CustomImageView(Context context) {
        this(context, null);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        detector = new GestureDetector(getContext(), new MyGestureListener());
        rotateGestureDetector = new RotateGestureDetector(getContext(), new MyOnRotateGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new MyOnScaleGestureListener());
        matrix = new Matrix();
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stokeWidth);


        contentRect = new RectF();
        outFrameRect = new RectF();
        leftTopCornerRect = new RectF();
        rightTopCornerRect = new RectF();
        rightBottomCornerRect = new RectF();
        leftBottomConrnerRect = new RectF();

        regionRect = new RectF();

        regionPts = new float[8];
        outFramePts = new float[8];
        leftTopCornerPts = new float[8];
        rightTopCornerPts = new float[8];
        rigthBottomCornerPts = new float[8];
        leftBottomCornerPts = new float[8];

        regionPath = new Path();
        outFramePath = new Path();
        leftTopCornerPath = new Path();
        rightTopCornerPath = new Path();
        rightBottomCornerPath = new Path();
        leftBottomCornerPath = new Path();


        anchor = new Point();
        action = new Runnable() {
            @Override
            public void run() {
                hiddenOutFrame = true;
                postInvalidate();
            }
        };
        textPaint = new TextPaint();
        color=Color.WHITE;
        textPaint.setColor(color);
        textPaint.setTextSize(Utils.sp2px(getContext(),20));
        textPaint.setAntiAlias(true);

         screenWidth = ((MyApplication) getContext().getApplicationContext()).screenWidth;
         screenHeight = ((MyApplication) getContext().getApplicationContext()).screenHeight;
    }

    //重新设置文本时必须调用
    public void reset() {
        removeCallbacks(action);
        contentRect.setEmpty();
        showOutFrame();


        float textWidth = layout.getPaint().measureText(text);
        int width=(int) Math.min(screenWidth - 2*FRAME_SIZE_EXTRA ,textWidth );
        selfLayout = new StaticLayout(text, textPaint,width , Layout.Alignment.ALIGN_CENTER, 1.2f, 0, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {


        if (getDrawable() != null) {
            contentPivotX = getDrawable().getBounds().right / 2 + getDrawable().getBounds().left / 2;
            contentPivotY = getDrawable().getBounds().bottom / 2 + getDrawable().getBounds().top / 2;
            contentWidth = getDrawable().getIntrinsicWidth();
            contentHeight = getDrawable().getIntrinsicHeight();
        }
        if (selfLayout != null) {
            contentPivotX = selfLayout.getWidth()/2f;
            contentPivotY = selfLayout.getHeight() / 2f;
            contentWidth = contentPivotX * 2f;
            contentHeight = contentPivotY * 2f;
        }
        if (contentRect.isEmpty()) {
            contentRect.set(0, 0, contentWidth, contentHeight);
            outFrameRect.set(-FRAME_SIZE_EXTRA, -FRAME_SIZE_EXTRA, contentWidth + FRAME_SIZE_EXTRA, contentHeight + FRAME_SIZE_EXTRA);
            regionRect.set(-REGION_SIZE_EXTRA, -REGION_SIZE_EXTRA, contentWidth + REGION_SIZE_EXTRA, contentHeight + REGION_SIZE_EXTRA);
            leftTopCornerRect.set(outFrameRect.left - hornRectEdgeLength / 2, outFrameRect.top - hornRectEdgeLength / 2, outFrameRect.left + hornRectEdgeLength / 2, outFrameRect.top + hornRectEdgeLength / 2);
            rightTopCornerRect.set(outFrameRect.right - hornRectEdgeLength / 2, outFrameRect.top - hornRectEdgeLength / 2, outFrameRect.right + hornRectEdgeLength / 2, outFrameRect.top + hornRectEdgeLength / 2);
            rightBottomCornerRect.set(outFrameRect.right - hornRectEdgeLength / 2, outFrameRect.bottom - hornRectEdgeLength / 2, outFrameRect.right + hornRectEdgeLength / 2, outFrameRect.bottom + hornRectEdgeLength / 2);
            leftBottomConrnerRect.set(outFrameRect.left - hornRectEdgeLength / 2, outFrameRect.bottom - hornRectEdgeLength / 2, outFrameRect.left + hornRectEdgeLength / 2, outFrameRect.bottom + hornRectEdgeLength / 2);
        }


        if (getDrawable() == null) {
            //无图 绘制文本时使用
            if (matrix.isIdentity())
                matrix.setTranslate(screenWidth / 2 - contentPivotX, screenHeight / 2 - contentPivotY);
        } else {
            matrix = getImageMatrix();
        }
        //printMatrix(matrix);

        float[] fps = {contentPivotX, contentPivotY};
        matrix.mapPoints(fps);
        float pivotX = fps[0];
        float pivotY = fps[1];
        anchor.set(pivotX, pivotY);

        initPts(outFramePts, outFrameRect);
        matrix.mapPoints(outFramePts);
        pathByPts(outFramePath, outFramePts);

        initPts(regionPts, regionRect);
        matrix.mapPoints(regionPts);
        pathByPts(regionPath, regionPts);

        initPts(leftTopCornerPts, leftTopCornerRect);
        matrix.mapPoints(leftTopCornerPts);
        pathByPts(leftTopCornerPath, leftTopCornerPts);

        initPts(rightTopCornerPts, rightTopCornerRect);
        matrix.mapPoints(rightTopCornerPts);
        pathByPts(rightTopCornerPath, rightTopCornerPts);

        initPts(rigthBottomCornerPts, rightBottomCornerRect);
        matrix.mapPoints(rigthBottomCornerPts);
        pathByPts(rightBottomCornerPath, rigthBottomCornerPts);

        initPts(leftBottomCornerPts, leftBottomConrnerRect);
        matrix.mapPoints(leftBottomCornerPts);
        pathByPts(leftBottomCornerPath, leftBottomCornerPts);

        if (!hiddenOutFrame) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(outFramePath, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(leftTopCornerPath, paint);
            canvas.drawPath(rightTopCornerPath, paint);
            canvas.drawPath(rightBottomCornerPath, paint);
            canvas.drawPath(leftBottomCornerPath, paint);
        }


        if (layout != null) {
            float[] values = new float[9];
            matrix.getValues(values);
            canvas.save();
            canvas.translate(values[Matrix.MTRANS_X], values[Matrix.MTRANS_Y]);
            canvas.rotate(-degrees);
            //使用canvas.scale(values[Matrix.MSCALE_X], values[Matrix.MSCALE_Y]);出错，不知为啥
            //values[Matrix.MSCALE_X]的值随着旋转在改变
            //System.out.println("Matrix.MSCALE_X:"+values[Matrix.MSCALE_X]);
            //System.out.println("scaleFactor:"+scaleFactor);
            canvas.scale(scaleFactor, scaleFactor);

            selfLayout.draw(canvas);
            canvas.restore();
        }


        if (isFirstShow) {
            isFirstShow = false;
            postDelayed(action, 4000);
        }
        super.onDraw(canvas);
    }

    public void initPts(float[] pts, RectF rectF) {
        if (pts == null || pts.length < 8) {
            return;
        }
        pts[0] = rectF.left;
        pts[1] = rectF.top;
        pts[2] = rectF.right;
        pts[3] = rectF.top;
        pts[4] = rectF.right;
        pts[5] = rectF.bottom;
        pts[6] = rectF.left;
        pts[7] = rectF.bottom;
    }

    public void pathByPts(Path path, float[] pts) {
        path.reset();
        path.moveTo(pts[0], pts[1]);
        path.lineTo(pts[2], pts[3]);
        path.lineTo(pts[4], pts[5]);
        path.lineTo(pts[6], pts[7]);
        path.lineTo(pts[0], pts[1]);
    }

    public void printPts(float[] pts, String tag) {
        System.out.println(tag);
        System.out.print("leftTop:" + pts[0] + "," + pts[1]);
        System.out.print("rightTop:" + pts[2] + "," + pts[3]);
        System.out.print("rightBottom:" + pts[4] + "," + pts[5]);
        System.out.print("leftBottom:" + pts[6] + "," + pts[7]);
    }

    public void printMatrix(Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        for (int i = 0; i < values.length; i++) {
            System.out.print(values[i] + " ");
        }
        System.out.println("");
    }

    boolean hasHandle;
    boolean hasSingleHandle;
    boolean hasPointerHandle;
    boolean hasScaleHandle;
    boolean hiddenOutFrame;

    public boolean IsRectContainPoint(Point point) {
        float left = Math.min(Math.min(regionPts[0], regionPts[2]), Math.min(regionPts[4], regionPts[6]));
        float right = Math.max(Math.max(regionPts[0], regionPts[2]), Math.max(regionPts[4], regionPts[6]));
        float top = Math.min(Math.min(regionPts[1], regionPts[3]), Math.min(regionPts[5], regionPts[7]));
        float bottom = Math.max(Math.max(regionPts[1], regionPts[3]), Math.max(regionPts[5], regionPts[7]));
        if (point.x > left && point.x < right && point.y > top && point.y < bottom) {
            return true;
        } else {
            return false;
        }
    }

    boolean isInRegion;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        System.out.println("onTouchEvent");
        //检测点击区域
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Point primaryPoint = new Point(event.getX(), event.getY());
                isInRegion = IsRectContainPoint(primaryPoint);
                if (!isInRegion) {
                    return false;
                }
                if (onTouchedListener != null)
                    onTouchedListener.OnTouchedListener(this);
                break;
        }

        ViewGroup parent = (ViewGroup) getParent();
        int childCount = parent.getChildCount();
        CustomImageView child = (CustomImageView) parent.getChildAt(childCount - 1);
        child.hideOutFrame();

        bringToFront();
        removeCallbacks(action);
        showOutFrame();


        int pointerCount = event.getPointerCount();
        if (pointerCount == 1 && !hasHandle || hasSingleHandle) {
            hasSingleHandle = detector.onTouchEvent(event);
            hasHandle = hasSingleHandle;
        }
        if (pointerCount == 2 && !hasHandle || hasPointerHandle || hasScaleHandle) {
            hasPointerHandle = rotateGestureDetector.onTouchEvent(event);
            hasScaleHandle = scaleGestureDetector.onTouchEvent(event);
            hasHandle = true;
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (onTouchedListener != null)
                onTouchedListener.OnUpListener(this);
            if (runnable != null) {
                runnable.run();
            }
            postDelayed(action, 4000);
            hasHandle = false;
            hasSingleHandle = false;
            hasPointerHandle = false;
            hasScaleHandle = false;
        }

        return true;

    }


    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        Point startPoint;

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d("test", "onDown");
            startPoint = new Point(e.getX(), e.getY());

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (anchor.x < -BEYOND_EGED_LENGTH || anchor.y < -BEYOND_EGED_LENGTH || anchor.x > getWidth() + BEYOND_EGED_LENGTH || anchor.y > getHeight() + BEYOND_EGED_LENGTH) {
                //越界
                runnable = new DealRunnable();
                return false;
            }
            setScaleType(ScaleType.MATRIX);
            matrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(matrix);
            if (onTouchedListener != null) {
                onTouchedListener.OnScrollListener(CustomImageView.this);
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            performClick();
            return true;
        }


        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return super.onDoubleTap(e);
        }


    }

    public void hideOutFrame() {
        hiddenOutFrame = true;
        invalidate();
    }

    public void showOutFrame() {
        hiddenOutFrame = false;
        invalidate();
    }


    class DealRunnable implements Runnable {

        @Override
        public void run() {
            if (!(anchor.x > 0 && anchor.x < getWidth() && anchor.y > 0 && anchor.y < getHeight())) {
                setScaleType(ScaleType.MATRIX);
                if (anchor.x < 0 && anchor.y < 0) {
                    matrix.postTranslate(5, 5);
                } else if (anchor.x < 0 && anchor.y > 0) {
                    matrix.postTranslate(5, 0);
                } else if (anchor.x > 0 && anchor.y < 0) {
                    matrix.postTranslate(0, 5);
                } else if (anchor.x > getWidth() && anchor.y > getHeight()) {
                    matrix.postTranslate(-5, -5);
                } else if (anchor.x > getWidth() && anchor.y < getHeight()) {
                    matrix.postTranslate(-5, 0);
                } else if (anchor.x < getWidth() && anchor.y > getHeight()) {
                    matrix.postTranslate(0, -5);
                }
                setImageMatrix(matrix);
                postDelayed(this, 16);
            }

        }
    }

    class MyOnRotateGestureListener implements RotateGestureDetector.OnRotateGestureListener {


        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            System.out.println("getRotationDegreesDelta:" + detector.getRotationDegreesDelta());
            setScaleType(ScaleType.MATRIX);
            matrix.postRotate(-detector.getRotationDegreesDelta(), anchor.x, anchor.y);
            setImageMatrix(matrix);
            degrees += detector.getRotationDegreesDelta();

            return true;
        }

        @Override
        public boolean onRotateBegin(RotateGestureDetector detector) {
            System.out.println("RotationDegrees:" + detector.getRotationDegreesDelta());
            return true;
        }

        @Override
        public void onRotateEnd(RotateGestureDetector detector) {
            System.out.println("onRotateEnd");
        }
    }

    class MyOnScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.getScaleFactor() > 1) {
                if (scaleFactor >= MAX_SCALE) {
                    hasScaleHandle = false;
                    return false;
                }
            } else {
                if (scaleFactor <= MIN_SCALE) {
                    hasScaleHandle = false;
                    return false;
                }
            }
            setScaleType(ScaleType.MATRIX);
            matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), anchor.x, anchor.y);
            setImageMatrix(matrix);
            scaleFactor *= detector.getScaleFactor();
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            System.out.println("onScaleBegin:" + detector.getScaleFactor());
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
        }
    }

    OnTouchedListener onTouchedListener;

    public void setOnTouchedListener(OnTouchedListener onTouchedListener) {
        this.onTouchedListener = onTouchedListener;
    }

    interface OnTouchedListener {
        void OnTouchedListener(View v);

        void OnScrollListener(View v);

        void OnUpListener(View v);
    }
}
