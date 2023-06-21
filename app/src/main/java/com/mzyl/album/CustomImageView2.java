package com.mzyl.album;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.almeros.android.multitouch.RotateGestureDetector;

import java.util.ArrayList;
import java.util.List;

public class CustomImageView2 extends android.support.v7.widget.AppCompatImageView {
    //外边框的大小
    public static final int FRAME_SIZE_EXTRA = 50;
    public static final int REGION_SIZE_EXTRA = 100;
    public static final float BEYOND_EGED_LENGTH = 30;

    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private final RotateGestureDetector rotateGestureDetector;

    public static final float MAX_SCALE = 5;
    private static final float MIN_SCALE = .3f;
    public static final float MAX_SCALE_ITEM = 20;
    private static final float MIN_SCALE_ITEM = .1f;

    private final PorterDuffXfermode porterDuffXfermode;
    private final Runnable hideOutFrameAction;
    private final int screenWidth;
    private final int screenHeight;


    private Bitmap mosaicBitmap;
    private float STROKE_WIDTH_LINE = 15;
    private float STROKE_WIDTH_MOSAIC = 60;
    private Bitmap bitmap;


    private float defaultTextSizeSp = 20;
    private int hornRectEdgeLength = 30;

    private boolean isFirstShow = true;
    private Grid grid;


    public void setPenColor(int penColor) {
        this.penColor = penColor;
    }

    private int penColor;
    private boolean canDraw;

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    private Paint paint;

    private float scaleFactor = 1;

    List linePathList;
    List mosaicPathList;
    List allPathList;

    private final List emotionList;
    List itemList;
    RectF bitmapRectf;


    Layout selfLayout;


    public void addText(String text) {
        addText(text, Color.WHITE);
    }

    public void addText(String text, int color) {
        if (TextUtils.isEmpty(text)) {
            if (changedTextItem == null) {
                return;
            } else {
                changedTextItem.removeSelf();
                changedTextItem = null;
                return;
            }
        }
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(color);
        textPaint.setTextSize(Utils.sp2px(getContext(), defaultTextSizeSp));
        int width = (int) Math.min(getWidth() - 2 * FRAME_SIZE_EXTRA, textPaint.measureText(text));
        Layout layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_CENTER, 1.2f, 0, false);
        TextItem item = new TextItem(text, layout);
        if (changedTextItem != null) {
            int index = itemList.indexOf(changedTextItem);
            itemList.remove(changedTextItem);
            changedTextItem = null;
            itemList.add(index, item);
        } else {
            itemList.add(item);
        }
        focusedItem = item;
        reset();
    }

    public void addEmotion(int res) {
        Paint emotionPaint = new Paint();
        emotionPaint.setColor(Color.WHITE);
        emotionPaint.setAntiAlias(true);
        emotionPaint.setStrokeWidth(2);
        EmotionItem item = new EmotionItem(res, emotionPaint);
        itemList.add(item);
        focusedItem = item;
        reset();
    }

    public void startCutMode() {

        grid = new Grid(new RectF(getWidth() * 0.05f, (getHeight() * 0.8f) * 0.05f, getWidth() * 0.95f, (getHeight() * 0.8f) * 0.95f));
        setScaleType(ScaleType.MATRIX);
        Matrix imageMatrix = getImageMatrix();
        float scaleFactor = grid.gridRect.width() / getDrawable().getIntrinsicWidth();
        int times = 10;

        float scale = (float) Math.pow(scaleFactor, 1f / times);
        while (times-- != 0) {
            imageMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(imageMatrix);
            invalidate();
        }
        times = 10;
        getBitmapRectf();
        float translate = (grid.gridRect.top - bitmapRectf.top) / times;
        while (times-- != 0) {
            imageMatrix.postTranslate(0, translate);
            setImageMatrix(imageMatrix);
            invalidate();
        }

    }

    class Grid {
        public static final int GRID_FRAME_STROKEWIDTH = 4;
        public static final int GRID_INSIDE_STROKEWIDTH = 2;
        public static final int GRID_CORNER_STROKEWIDTH = 8;
        public static final int GRID_CORNER_LENGTH = 80;
        private final MyRunnable myRunnable;

        //裁剪区域
        public RectF gridRect;

        private RectF maxGridRect;
        private RectF originGridRect;

        Paint paint;

        RectF leftTopCornerL;
        RectF leftTopCornerT;

        RectF rightTopCornerR;
        RectF rightTopCornerT;

        RectF leftBottomCornerL;
        RectF leftBottomCornerB;

        RectF rightBottomCornerR;
        RectF rightBottomCornerB;
        private RectF screenRectf;

        public Grid(RectF gridRect) {
            this.maxGridRect = gridRect;
            float left, right, top, bottom, newWidth, newHeight;
            float scaleFactor;
            int intrinsicWidth = getDrawable().getIntrinsicWidth();
            int intrinsicHeight = getDrawable().getIntrinsicHeight();
            if (intrinsicWidth > intrinsicHeight) {
                //横向
                left = maxGridRect.left;
                right = maxGridRect.right;
                newWidth = right - left;
                scaleFactor = newWidth / intrinsicWidth;
                newHeight = intrinsicHeight * scaleFactor;
                top = maxGridRect.top + (maxGridRect.height() - newHeight) / 2;
                bottom = maxGridRect.bottom - (maxGridRect.height() - newHeight) / 2;
                this.gridRect = new RectF(left, top, right, bottom);
            } else {
                //纵向
                top = maxGridRect.top;
                bottom = maxGridRect.bottom;
                newHeight = bottom - top;
                scaleFactor = newHeight / intrinsicHeight;
                newWidth = intrinsicWidth * scaleFactor;
                left = maxGridRect.left + (maxGridRect.width() - newWidth) / 2;
                right = maxGridRect.right - (maxGridRect.width() - newWidth) / 2;
                this.gridRect = new RectF(left, top, right, bottom);
            }

            originGridRect = new RectF(gridRect);

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);

            screenRectf = new RectF(0, 0, screenWidth, screenHeight);

            leftTopCornerL = new RectF();
            leftTopCornerT = new RectF();

            rightTopCornerR = new RectF();
            rightTopCornerT = new RectF();

            leftBottomCornerL = new RectF();
            leftBottomCornerB = new RectF();

            rightBottomCornerR = new RectF();
            rightBottomCornerB = new RectF();

            initRect();

            myRunnable = new MyRunnable();
        }

        public void initRect() {

            leftTopCornerL.set(gridRect.left - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH, gridRect.top - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH, gridRect.left - GRID_INSIDE_STROKEWIDTH / 2, gridRect.top - -GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH + GRID_CORNER_LENGTH);
            leftTopCornerT.set(gridRect.left - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH, gridRect.top - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH, gridRect.left - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH + GRID_CORNER_LENGTH, gridRect.top - GRID_INSIDE_STROKEWIDTH / 2);
            rightTopCornerR.set(gridRect.right + GRID_INSIDE_STROKEWIDTH / 2, gridRect.top - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH, gridRect.right + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH, gridRect.top - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH + GRID_CORNER_LENGTH);
            rightTopCornerT.set(gridRect.right + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH - GRID_CORNER_LENGTH, gridRect.top - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH, gridRect.right + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH, gridRect.top - GRID_INSIDE_STROKEWIDTH / 2);
            leftBottomCornerL.set(gridRect.left - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH, gridRect.bottom + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH - GRID_CORNER_LENGTH, gridRect.left - GRID_INSIDE_STROKEWIDTH / 2, gridRect.bottom + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH);
            leftBottomCornerB.set(gridRect.left - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH, gridRect.bottom + GRID_INSIDE_STROKEWIDTH / 2, gridRect.left - GRID_INSIDE_STROKEWIDTH / 2 - GRID_CORNER_STROKEWIDTH + GRID_CORNER_LENGTH, gridRect.bottom + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH);
            rightBottomCornerR.set(gridRect.right + GRID_INSIDE_STROKEWIDTH / 2, gridRect.bottom + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH - GRID_CORNER_LENGTH, gridRect.right + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH, gridRect.bottom + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH);
            rightBottomCornerB.set(gridRect.right + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH - GRID_CORNER_LENGTH, gridRect.bottom + GRID_INSIDE_STROKEWIDTH / 2, gridRect.right + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH, gridRect.bottom + GRID_INSIDE_STROKEWIDTH / 2 + GRID_CORNER_STROKEWIDTH);


        }

        public boolean drawBackground;

        public void onDraw(final Canvas canvas) {

            int saveLayer = canvas.saveLayer(screenRectf, null, Canvas.ALL_SAVE_FLAG);

            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(GRID_FRAME_STROKEWIDTH);
            paint.setColor(Color.WHITE);
            canvas.drawRect(gridRect, paint);

            paint.setStrokeWidth(GRID_INSIDE_STROKEWIDTH);
            float startX = gridRect.left;
            float startY = (gridRect.bottom - gridRect.top) / 3 + gridRect.top;
            float stopX = gridRect.right;
            float stopY = startY;
            canvas.drawLine(startX, startY, stopX, stopY, paint);
            startY = (gridRect.bottom - gridRect.top) * 2 / 3 + gridRect.top;
            stopY = startY;
            canvas.drawLine(startX, startY, stopX, stopY, paint);
            startX = (gridRect.right - gridRect.left) / 3 + gridRect.left;
            startY = gridRect.top;
            stopX = startX;
            stopY = gridRect.bottom;
            canvas.drawLine(startX, startY, stopX, stopY, paint);
            startX = (gridRect.right - gridRect.left) * 2 / 3 + gridRect.left;
            stopX = startX;
            canvas.drawLine(startX, startY, stopX, stopY, paint);

            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawRect(leftTopCornerL, paint);
            canvas.drawRect(leftTopCornerT, paint);

            canvas.drawRect(rightTopCornerR, paint);
            canvas.drawRect(rightTopCornerT, paint);

            canvas.drawRect(leftBottomCornerL, paint);
            canvas.drawRect(leftBottomCornerB, paint);

            canvas.drawRect(rightBottomCornerR, paint);
            canvas.drawRect(rightBottomCornerB, paint);

            if (drawBackground) {
                paint.setColor(Color.BLACK);
                paint.setAlpha(200);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRect(0, 0, gridRect.left, screenHeight, paint);
                canvas.drawRect(0, 0, screenWidth, gridRect.top, paint);
                canvas.drawRect(gridRect.right, 0, screenWidth, screenHeight, paint);
                canvas.drawRect(0, gridRect.bottom, screenWidth, screenHeight, paint);
            }

            canvas.restoreToCount(saveLayer);


        }

        int orientation = -1;
        Point preEvent;

        public void onTouchEvent(MotionEvent event) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    preEvent = new Point(event.getX(), event.getY());
                    Point downPoint = new Point(event.getX(), event.getY());
                    if (isContainPoint(bitmapRectf, downPoint)) {
                        drawBackground = false;
                        invalidate();
                    } else {
                        drawBackground = true;
                    }

                    Path detectedRectPath = new Path();
                    RectF bounds = new RectF();
                    detectedRectPath.addRect(leftTopCornerL, Path.Direction.CW);
                    detectedRectPath.addRect(leftTopCornerT, Path.Direction.CW);
                    detectedRectPath.computeBounds(bounds, true);
                    if (isContainPoint(bounds, downPoint)) {
                        //点击的是左上角
                        orientation = 0;
                        break;
                    }
                    detectedRectPath.reset();
                    bounds.setEmpty();
                    detectedRectPath.addRect(rightTopCornerR, Path.Direction.CW);
                    detectedRectPath.addRect(rightTopCornerT, Path.Direction.CW);
                    detectedRectPath.computeBounds(bounds, true);
                    if (isContainPoint(bounds, downPoint)) {
                        //点击的是右上角
                        orientation = 1;
                        break;
                    }
                    detectedRectPath.reset();
                    bounds.setEmpty();
                    detectedRectPath.addRect(leftBottomCornerL, Path.Direction.CW);
                    detectedRectPath.addRect(leftBottomCornerB, Path.Direction.CW);
                    detectedRectPath.computeBounds(bounds, true);
                    if (isContainPoint(bounds, downPoint)) {
                        //点击的是左下角
                        orientation = 2;
                        break;
                    }
                    detectedRectPath.reset();
                    bounds.setEmpty();
                    detectedRectPath.addRect(rightBottomCornerR, Path.Direction.CW);
                    detectedRectPath.addRect(rightBottomCornerB, Path.Direction.CW);
                    detectedRectPath.computeBounds(bounds, true);
                    if (isContainPoint(bounds, downPoint)) {
                        //点击的是右下角
                        orientation = 3;
                        break;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (orientation != -1)
                        reSizeByGesture(preEvent.x - event.getX(), preEvent.y - event.getY());
                    preEvent.set(event.getX(), event.getY());

                    break;
                case MotionEvent.ACTION_UP:
                    if (!drawBackground) {
                        removeCallbacks(myRunnable);
                        postDelayed(myRunnable, 1000);
                    }


                    orientation = -1;
                    break;
            }
        }

        class MyRunnable implements Runnable {

            @Override
            public void run() {
                drawBackground = true;
                postInvalidate();
            }
        }

        public void reSizeByGesture(float distanceX, float distanceY) {

            switch (orientation) {
                case 0:
                    //点击的是左上角
                    float left;
                    float top;
                    float right;
                    float bottom;
                    left = gridRect.left - distanceX;
                    top = gridRect.top - distanceY;
                    right = gridRect.right;
                    bottom = gridRect.bottom;
                    left = Math.max(originGridRect.left, left);
                    left = Math.min(gridRect.right - GRID_INSIDE_STROKEWIDTH - 2 * (GRID_CORNER_LENGTH - GRID_CORNER_STROKEWIDTH - GRID_INSIDE_STROKEWIDTH), left);
                    top = Math.max(originGridRect.top, top);
                    top = Math.min(gridRect.bottom - GRID_INSIDE_STROKEWIDTH - 2 * (GRID_CORNER_LENGTH - GRID_CORNER_STROKEWIDTH - GRID_INSIDE_STROKEWIDTH), top);
                    gridRect.set(left, top, right, bottom);
                    break;
                case 1:
                    //点击的是右上角
                    left = gridRect.left;
                    top = gridRect.top - distanceY;
                    right = gridRect.right - distanceX;
                    bottom = gridRect.bottom;
                    right = Math.min(originGridRect.right, right);
                    right = Math.max(gridRect.left + GRID_INSIDE_STROKEWIDTH + 2 * (GRID_CORNER_LENGTH - GRID_CORNER_STROKEWIDTH - GRID_INSIDE_STROKEWIDTH), right);
                    top = Math.max(originGridRect.top, top);
                    top = Math.min(gridRect.bottom - GRID_INSIDE_STROKEWIDTH - 2 * (GRID_CORNER_LENGTH - GRID_CORNER_STROKEWIDTH - GRID_INSIDE_STROKEWIDTH), top);
                    gridRect.set(left, top, right, bottom);
                    break;
                case 2:
                    //点击的是左下角
                    left = gridRect.left - distanceX;
                    top = gridRect.top;
                    right = gridRect.right;
                    bottom = gridRect.bottom - distanceY;
                    left = Math.max(originGridRect.left, left);
                    left = Math.min(gridRect.right - GRID_INSIDE_STROKEWIDTH - 2 * (GRID_CORNER_LENGTH - GRID_CORNER_STROKEWIDTH - GRID_INSIDE_STROKEWIDTH), left);
                    bottom = Math.min(originGridRect.bottom, bottom);
                    bottom = Math.max(gridRect.top + GRID_INSIDE_STROKEWIDTH + 2 * (GRID_CORNER_LENGTH - GRID_CORNER_STROKEWIDTH - GRID_INSIDE_STROKEWIDTH), bottom);
                    gridRect.set(left, top, right, bottom);
                    break;
                case 3:
                    //点击的是右下角
                    left = gridRect.left;
                    top = gridRect.top;
                    right = gridRect.right - distanceX;
                    bottom = gridRect.bottom - distanceY;
                    right = Math.min(originGridRect.right, right);
                    right = Math.max(gridRect.left + GRID_INSIDE_STROKEWIDTH + 2 * (GRID_CORNER_LENGTH - GRID_CORNER_STROKEWIDTH - GRID_INSIDE_STROKEWIDTH), right);
                    bottom = Math.min(originGridRect.bottom, bottom);
                    bottom = Math.max(gridRect.top + GRID_INSIDE_STROKEWIDTH + 2 * (GRID_CORNER_LENGTH - GRID_CORNER_STROKEWIDTH - GRID_INSIDE_STROKEWIDTH), bottom);
                    gridRect.set(left, top, right, bottom);
                    break;
            }

            initRect();

            invalidate();
        }

        private boolean isContainPoint(RectF rectF, Point point) {
            return rectF.contains(point.x, point.y);
        }
    }

    class Item {


        private Matrix matrix;
        private int res;
        private Paint paint;

        //原始数据
        private RectF bounds;
        private RectF outFrameRect;
        private RectF regionRect;
        private RectF leftTopCornerRect;
        private RectF rightTopCornerRect;
        private RectF rightBottomCornerRect;
        private RectF leftBottomConrnerRect;
        private Point originPivot;

        private Point pivot;
        private Path contentPath;
        private Path outFramePath;
        private Path regionPath;
        private Path leftTopCornerPath;
        private Path rightTopCornerPath;
        private Path rightBottomCornerPath;
        private Path leftBottomCornerPath;

        private float width;
        private float height;
        private float[] contentPts;
        private float[] outFramePts;
        private float[] regionPts;
        private float[] leftTopCornerPts;
        private float[] rightTopCornerPts;
        private float[] rigthBottomCornerPts;
        private float[] leftBottomCornerPts;

        public float rotateDegrees;
        public float scaleFactorItem = 1;

        public void initFrame(RectF bounds) {
            this.bounds = bounds;
            originPivot.set(bounds.right / 2 - bounds.left / 2, bounds.bottom / 2 - bounds.top / 2);
            pivot.set(originPivot.x, originPivot.y);

            width = bounds.right - bounds.left;
            height = bounds.bottom - bounds.top;

            outFrameRect.set(-FRAME_SIZE_EXTRA, -FRAME_SIZE_EXTRA, width + FRAME_SIZE_EXTRA, height + FRAME_SIZE_EXTRA);
            regionRect.set(-REGION_SIZE_EXTRA, -REGION_SIZE_EXTRA, width + REGION_SIZE_EXTRA, height + REGION_SIZE_EXTRA);
            leftTopCornerRect.set(outFrameRect.left - hornRectEdgeLength / 2, outFrameRect.top - hornRectEdgeLength / 2, outFrameRect.left + hornRectEdgeLength / 2, outFrameRect.top + hornRectEdgeLength / 2);
            rightTopCornerRect.set(outFrameRect.right - hornRectEdgeLength / 2, outFrameRect.top - hornRectEdgeLength / 2, outFrameRect.right + hornRectEdgeLength / 2, outFrameRect.top + hornRectEdgeLength / 2);
            rightBottomCornerRect.set(outFrameRect.right - hornRectEdgeLength / 2, outFrameRect.bottom - hornRectEdgeLength / 2, outFrameRect.right + hornRectEdgeLength / 2, outFrameRect.bottom + hornRectEdgeLength / 2);
            leftBottomConrnerRect.set(outFrameRect.left - hornRectEdgeLength / 2, outFrameRect.bottom - hornRectEdgeLength / 2, outFrameRect.left + hornRectEdgeLength / 2, outFrameRect.bottom + hornRectEdgeLength / 2);

            matrix.setTranslate(getWidth() / 2 - pivot.x, getHeight() / 2 - pivot.y);
            onMatrixUpdate();

        }

        public Item(Paint paint) {
            this.paint = paint;
            outFrameRect = new RectF();
            regionRect = new RectF();
            leftTopCornerRect = new RectF();
            rightTopCornerRect = new RectF();
            rightBottomCornerRect = new RectF();
            leftBottomConrnerRect = new RectF();

            originPivot = new Point();
            pivot = new Point();

            contentPath = new Path();
            contentPts = new float[8];

            outFramePath = new Path();
            outFramePts = new float[8];

            regionPath = new Path();
            regionPts = new float[8];

            leftTopCornerPath = new Path();
            leftTopCornerPts = new float[8];

            rightTopCornerPath = new Path();
            rightTopCornerPts = new float[8];

            rightBottomCornerPath = new Path();
            rigthBottomCornerPts = new float[8];

            leftBottomCornerPath = new Path();
            leftBottomCornerPts = new float[8];

            matrix = new Matrix();
        }

        public void onDraw(Canvas canvas) {

        }

        public void onDrawFrame(Canvas canvas) {
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(outFramePath, paint);
            canvas.drawPath(regionPath, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(leftTopCornerPath, paint);
            canvas.drawPath(rightTopCornerPath, paint);
            canvas.drawPath(rightBottomCornerPath, paint);
            canvas.drawPath(leftBottomCornerPath, paint);
        }

        public void onMatrixUpdate() {
            //printMatrix(matrix);
            float[] fps = {originPivot.x, originPivot.y};
            matrix.mapPoints(fps);
            pivot.set(fps[0], fps[1]);

            ptsByRectF(contentPts, bounds);
            matrix.mapPoints(contentPts);
            pathByPts(contentPath, contentPts);

            ptsByRectF(outFramePts, outFrameRect);
            matrix.mapPoints(outFramePts);
            pathByPts(outFramePath, outFramePts);

            ptsByRectF(regionPts, regionRect);
            matrix.mapPoints(regionPts);
            pathByPts(regionPath, regionPts);

            ptsByRectF(leftTopCornerPts, leftTopCornerRect);
            matrix.mapPoints(leftTopCornerPts);
            pathByPts(leftTopCornerPath, leftTopCornerPts);

            ptsByRectF(rightTopCornerPts, rightTopCornerRect);
            matrix.mapPoints(rightTopCornerPts);
            pathByPts(rightTopCornerPath, rightTopCornerPts);

            ptsByRectF(rigthBottomCornerPts, rightBottomCornerRect);
            matrix.mapPoints(rigthBottomCornerPts);
            pathByPts(rightBottomCornerPath, rigthBottomCornerPts);

            ptsByRectF(leftBottomCornerPts, leftBottomConrnerRect);
            matrix.mapPoints(leftBottomCornerPts);
            pathByPts(leftBottomCornerPath, leftBottomCornerPts);
            //  printPts(outFramePts, "outFramePts");
        }

        public float[] getContentPts() {
            return contentPts;
        }

        public float[] getRegionPts() {
            return regionPts;
        }


        public Matrix getMatrix() {
            return matrix;
        }


        public Paint getPaint() {
            return paint;
        }


        public void removeSelf() {
            removeCallbacks(hideOutFrameAction);
            focusedItem = null;
            itemList.remove(this);
            invalidate();
        }


        public double getLengthP1P2(Point p1, Point p2) {
            return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
        }


        //性质
        //对于任意三角形，任何一边的平方等于其他两边平方的和减去这两边与它们夹角的余弦的两倍积，若三边为a，b，c 三角为A，B，C ，则满足性质——
        //cosA=c*c+b*b-a*a/2bc;
        //求的是夹角A  A的对角线是a
        public double getIncludeAngle(double a, double b, double c) {
            System.out.println("a:" + a + "b:" + b + "c:" + c);
            System.out.println("getIncludeAngle:" + (Math.pow(b, 2) + Math.pow(c, 2) - Math.pow(a, 2)) / (2 * b * c));
            return Math.acos((Math.pow(b, 2) + Math.pow(c, 2) - Math.pow(a, 2)) / (2d * b * c));
        }

        //判断某一点是否在矩形当中
        public boolean isRectContainPoint(float[] regionPts, Point p) {
            Point p1 = new Point(regionPts[0], regionPts[1]);
            Point p2 = new Point(regionPts[2], regionPts[3]);
            Point p3 = new Point(regionPts[4], regionPts[5]);
            Point p4 = new Point(regionPts[6], regionPts[7]);
            double p1p = getLengthP1P2(p1, p);
            double p1p2 = getLengthP1P2(p1, p2);
            double p2p = getLengthP1P2(p2, p);
            double p2p1p = getIncludeAngle(p2p, p1p, p1p2);

            double p1p4 = getLengthP1P2(p1, p4);
            double p4p = getLengthP1P2(p4, p);
            double p4p1p = getIncludeAngle(p4p, p1p, p1p4);

            double p3p4 = getLengthP1P2(p4, p3);
            double p3p = getLengthP1P2(p3, p);
            double p4p3p = getIncludeAngle(p4p, p3p4, p3p);

            double p3p2 = getLengthP1P2(p3, p2);
            double p2p3p = getIncludeAngle(p2p, p3p, p3p2);
            if (p2p1p < Math.PI / 2 && p4p1p < Math.PI / 2 && p4p3p < Math.PI / 2 && p2p3p < Math.PI / 2) {
                return true;
            } else {
                return false;
            }
        }

        boolean isItemOutOfBitmapRectWhenScroll;
        private ResetPositionThread resetPositionThread;

        public void resetPosition() {
            if (resetPositionThread == null) {
                resetPositionThread = new ResetPositionThread(this);
            }
            //todo resetPositionThread.isAlive()有错误
//            if (!resetPositionThread.isAlive())
//                resetPositionThread.start();
        }
    }

    class EmotionItem extends Item {
        private Bitmap emotionBitmap;
        private int res;

        public EmotionItem(int res, Paint paint) {
            super(paint);
            this.res = res;
            emotionBitmap = BitmapFactory.decodeResource(getResources(), res);
            initFrame(new RectF(0, 0, emotionBitmap.getWidth(), emotionBitmap.getHeight()));
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(emotionBitmap, getMatrix(), getPaint());
        }
    }

    class TextItem extends Item {

        private String text;

        private Layout layout;


        public TextItem(String text, Layout layout) {
            super(layout.getPaint());
            this.text = text;
            this.layout = layout;
            initFrame(new RectF(0, 0, layout.getWidth(), layout.getHeight()));
        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.save();
            canvas.translate(getMatrixVale(getMatrix(), Matrix.MTRANS_X), getMatrixVale(getMatrix(), Matrix.MTRANS_Y));
            //tips:这里旋转的轴心是(0,0)，因为canvas已经translate
            canvas.rotate(rotateDegrees, 0, 0);
            //tips:这里缩放的轴心是(0,0)，因为canvas已经translate
            //问题1?这里用scaleFactorItem代替Matrix.MSCALE_X，是因为Matrix.postRotate会改变Matrix.MSCALE_X的值，具体不知道为什么！！！
            canvas.scale(scaleFactorItem, scaleFactorItem, 0, 0);

            layout.draw(canvas);
            canvas.restore();
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Layout getLayout() {
            return layout;
        }

        public void setLayout(Layout layout) {
            this.layout = layout;
        }


    }

    public CustomImageView2(Context context) {
        this(context, null);
    }

    public CustomImageView2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomImageView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        gestureDetector = new GestureDetector(getContext(), new MyGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new MyOnScaleGestureListener());
        rotateGestureDetector = new RotateGestureDetector(getContext(), new MyOnRotateGestureListener());

        linePathList = new ArrayList();
        mosaicPathList = new ArrayList();
        allPathList = new ArrayList();
        emotionList = new ArrayList();
        itemList = new ArrayList();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeWidth(STROKE_WIDTH_LINE);
        paint.setStrokeCap(Paint.Cap.ROUND);//圆角
        paint.setStrokeJoin(Paint.Join.ROUND);//拐点圆角
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);

        //叠加效果
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        bitmapRectf = new RectF();

        hideOutFrameAction = new Runnable() {
            @Override
            public void run() {
                focusedItem = null;
                postInvalidate();
            }
        };
        screenWidth = ((MyApplication) getContext().getApplicationContext()).screenWidth;
        screenHeight = ((MyApplication) getContext().getApplicationContext()).screenHeight;


    }


    private void getBitmapRectf() {
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        int intrinsicWidth = drawable.getIntrinsicWidth();
        int intrinsicHeight = drawable.getIntrinsicHeight();
        RectF originRectf = new RectF(0, 0, intrinsicWidth, intrinsicHeight);
        Matrix imageMatrix = getImageMatrix();
        imageMatrix.mapRect(bitmapRectf, originRectf);

    }

    //重置到添加item时的状态
    public void reset() {
        removeCallbacks(hideOutFrameAction);
        isFirstShow = true;
        invalidate();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getDrawable() != null) {
            bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
            getBitmapRectf();
        } else {
            return;
        }
//        if (mosaicBitmap == null) {
//            if (getDrawable() != null) {
//                postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mosaicBitmap = Masic(bitmap);
//                        Toast.makeText(getContext(), "完成马赛克制作", Toast.LENGTH_SHORT).show();
//                    }
//                }, 0);
//
//            }
//        }
//        if (drawType == null) {
//            return;
//        }


        int saveLayer = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);
        canvas.scale(scaleFactor, scaleFactor, getWidth() / 2, getHeight() / 2);
        onDrawMosaic(canvas);

        onDrawLine(canvas);

        onDrawItem(canvas);

        canvas.restoreToCount(saveLayer);

        onDrawCut(canvas);


    }

    private void onDrawCut(Canvas canvas) {
        if (grid == null) {
            return;
        }

        grid.onDraw(canvas);


    }


    protected void onDrawMosaic(Canvas canvas) {
        int saveLayer = canvas.saveLayer(bitmapRectf, null, Canvas.ALL_SAVE_FLAG);
        canvas.clipRect(bitmapRectf);
        paint.setAntiAlias(false);
        paint.setStrokeCap(Paint.Cap.SQUARE);//
        paint.setStrokeJoin(Paint.Join.MITER);//拐点锐角
        for (int i = 0; i < mosaicPathList.size(); i++) {
            DrawObj drawObj = (DrawObj) mosaicPathList.get(i);
            paint.setStrokeWidth(STROKE_WIDTH_MOSAIC);
            paint.setColor(drawObj.getColor());
            canvas.drawPath(drawObj.getPath(), paint);
        }
        paint.setXfermode(porterDuffXfermode);
        if (mosaicBitmap != null && mosaicPathList.size() != 0)
            canvas.drawBitmap(mosaicBitmap, bitmapRectf.left, bitmapRectf.top, paint);
        paint.setXfermode(null);
        canvas.restoreToCount(saveLayer);
    }

    protected void onDrawLine(Canvas canvas) {
        int saveLayer = canvas.saveLayer(bitmapRectf, null, Canvas.ALL_SAVE_FLAG);
        canvas.clipRect(bitmapRectf);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);//
        paint.setStrokeJoin(Paint.Join.ROUND);//拐点锐角
        for (int i = 0; i < linePathList.size(); i++) {
            DrawObj drawObj = (DrawObj) linePathList.get(i);
            paint.setColor(drawObj.getColor());
            paint.setStrokeWidth(STROKE_WIDTH_LINE);
            canvas.drawPath(drawObj.getPath(), paint);
        }
        canvas.restoreToCount(saveLayer);

    }

    private void onDrawItem(Canvas canvas) {
        if (focusedItem != null) {
            focusedItem.onDrawFrame(canvas);
        }
        for (Object textItem : itemList) {
            Item item = (Item) textItem;
            item.onDraw(canvas);
        }
        if (isFirstShow) {
            isFirstShow = false;
            postDelayed(hideOutFrameAction, 4000);
        }
    }


    TextItem changedTextItem;
    Item focusedItem;
    boolean hasHandle;
    boolean hasSingleHandle;
    boolean hasPointerHandle;
    boolean hasScaleHandle;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (grid != null) {
            grid.onTouchEvent(event);
        }
        //检测点击区域
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                removeCallbacks(hideOutFrameAction);
                focusedItem = null;
                invalidate();
                Point primaryPoint = new Point(event.getX(), event.getY());
                for (Object obj : itemList) {
                    Item item = (Item) obj;
                    if (item.isRectContainPoint(item.getRegionPts(), primaryPoint)) {
                        itemList.set(itemList.indexOf(item), itemList.get(itemList.size() - 1));
                        itemList.set(itemList.size() - 1, item);
                        focusedItem = item;
                        invalidate();
                        break;
                    }
                }
                if (onTouchedListener != null)
                    onTouchedListener.OnTouchedListener(focusedItem);

                break;
        }

        int pointerCount = event.getPointerCount();
        if (pointerCount == 1 && !hasHandle || hasSingleHandle) {
            hasSingleHandle = gestureDetector.onTouchEvent(event);
            hasHandle = hasSingleHandle;
        }
        if (pointerCount == 2 && !hasHandle || hasPointerHandle || hasScaleHandle) {
            hasPointerHandle = rotateGestureDetector.onTouchEvent(event);
            hasScaleHandle = scaleGestureDetector.onTouchEvent(event);
            hasHandle = true;
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {

            if (focusedItem != null) {
                if (focusedItem.isItemOutOfBitmapRectWhenScroll) {
                    focusedItem.resetPosition();
                }
                postDelayed(hideOutFrameAction, 4000);
            }

            if (onTouchedListener != null)
                onTouchedListener.OnUpListener(focusedItem);
            Matrix imageMatrix = getImageMatrix();
            float matrixVale = getMatrixVale(imageMatrix, Matrix.MSCALE_X);
            if (grid != null) {
                float scaleFactor = grid.gridRect.width() / getDrawable().getIntrinsicWidth();
                setScaleType(ScaleType.MATRIX);
                if (matrixVale < scaleFactor) {
                    while (matrixVale < scaleFactor) {
                        imageMatrix.postScale(1.1f, 1.1f, getWidth() / 2, getHeight() / 2);
                        setImageMatrix(imageMatrix);
                        invalidate();
                        matrixVale = getMatrixVale(imageMatrix, Matrix.MSCALE_X);

                    }
                    imageMatrix.postScale(scaleFactor / matrixVale, scaleFactor / matrixVale, getWidth() / 2, getHeight() / 2);
                    this.scaleFactor = scaleFactor;
                }
            } else {
                setScaleType(ScaleType.MATRIX);
                if (matrixVale < 1) {
                    while (matrixVale < 1) {
                        imageMatrix.postScale(1.1f, 1.1f, getWidth() / 2, getHeight() / 2);
                        setImageMatrix(imageMatrix);
                        invalidate();
                        matrixVale = getMatrixVale(imageMatrix, Matrix.MSCALE_X);

                    }
                    imageMatrix.postScale(1 / matrixVale, 1 / matrixVale, getWidth() / 2, getHeight() / 2);
                    this.scaleFactor = 1;
                }

            }


            hasHandle = false;
            hasSingleHandle = false;
            hasPointerHandle = false;
            hasScaleHandle = false;

            if (grid != null) {
                grid.onTouchEvent(event);
            }
        }
        return true;

    }

    public void stepBack() {
        if (allPathList.size() == 0) {
            return;
        }
        Object o = allPathList.get(allPathList.size() - 1);
        if (linePathList.contains(o)) {
            linePathList.remove(o);
        }
        if (mosaicPathList.contains(o)) {
            mosaicPathList.remove(o);
        }
        allPathList.remove(o);
        invalidate();
    }


    public enum DrawType {
        LINE(0),
        MOSAIC(1),
        OTHER(2);

        DrawType(int type) {
            this.type = type;
        }

        int type;

    }

    DrawType drawType;

    public void setDrawType(DrawType drawType) {
        this.drawType = drawType;
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        Point startPoint;

        private DrawObj drawObj;

        @Override
        public boolean onDown(MotionEvent e) {
            if (onTouchedListener != null)
                onTouchedListener.OnTouchedListener(focusedItem);
            if (canDraw) {
                startPoint = new Point(e.getX(), e.getY());
                drawObj = new DrawObj();
                Path path = new Path();
                path.moveTo(startPoint.x, startPoint.y);
                drawObj.setPath(path);
                drawObj.setColor(penColor);
                switch (drawType) {
                    case LINE:
                        linePathList.add(drawObj);
                        break;
                    case MOSAIC:
                        mosaicPathList.add(drawObj);
                        break;
                }
                allPathList.add(drawObj);
            }

            return true;
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (focusedItem != null) {
                if (focusedItem.pivot.x < -BEYOND_EGED_LENGTH || focusedItem.pivot.y < (-BEYOND_EGED_LENGTH + bitmapRectf.top) || focusedItem.pivot.x > (bitmapRectf.right + BEYOND_EGED_LENGTH) || focusedItem.pivot.y > (bitmapRectf.bottom + BEYOND_EGED_LENGTH))
                    //越界
                    focusedItem.isItemOutOfBitmapRectWhenScroll = true;
            }
            if (onTouchedListener != null)
                onTouchedListener.OnScrollListener(focusedItem);
            if (canDraw && focusedItem == null) {
                drawObj.path.lineTo(e2.getX(), e2.getY());
                invalidate();
            }
            if (focusedItem != null) {
                focusedItem.matrix.postTranslate(-distanceX, -distanceY);
                focusedItem.onMatrixUpdate();
                invalidate();
                if (onTouchedListener != null) {
                    onTouchedListener.OnScrollListener(focusedItem);
                }
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (focusedItem instanceof TextItem)
                changedTextItem = (TextItem) focusedItem;
            performClick();
            return true;
        }


    }


    class ResetPositionThread extends Thread {
        Item item;

        public ResetPositionThread(Item item) {
            this.item = item;
        }

        @Override
        public void run() {

            while (item != null && !bitmapRectf.contains(item.pivot.x, item.pivot.y)) {
                if (item.pivot.x < 0 && item.pivot.y < bitmapRectf.top) {
                    item.matrix.postTranslate(5, 5);
                } else if (item.pivot.x < 0 && item.pivot.y > bitmapRectf.top) {
                    item.matrix.postTranslate(5, 0);
                } else if (item.pivot.x > 0 && item.pivot.y < bitmapRectf.top) {
                    item.matrix.postTranslate(0, 5);
                } else if (item.pivot.x > bitmapRectf.right && item.pivot.y > bitmapRectf.bottom) {
                    item.matrix.postTranslate(-5, -5);
                } else if (item.pivot.x > bitmapRectf.right && item.pivot.y < bitmapRectf.bottom) {
                    item.matrix.postTranslate(-5, 0);
                } else if (item.pivot.x < bitmapRectf.right && item.pivot.y > bitmapRectf.bottom) {
                    item.matrix.postTranslate(0, -5);
                }
                item.onMatrixUpdate();
                postInvalidate();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            item.isItemOutOfBitmapRectWhenScroll = false;
        }
    }

    public void ptsByRectF(float[] pts, RectF rectF) {
        if (pts == null || pts.length < 8 || rectF == null) {
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
        if (path == null || pts == null || pts.length == 0) {
            return;
        }
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
        System.out.println();
    }

    public void printMatrix(Matrix matrix) {
        float[] values = new float[9];
        matrix.getValues(values);
        for (int i = 0; i < values.length; i++) {
            System.out.print(values[i] + " ");
        }
        System.out.println("");
    }

    private float getMatrixVale(Matrix matrix, int index) {
        float[] values = new float[9];
        matrix.getValues(values);
        return values[index];
    }

    //马赛克
    public static Bitmap Masic(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);


        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];

        bitmap.getPixels(inPixels, 0, width, 0, 0, width, height);
        int index = 0;

        int offsetX = 0, offsetY = 0;
        int newX = 0, newY = 0;
        int size = 10;
        double total = size * size;
        double sumred = 0, sumgreen = 0, sumblue = 0;
        for (int row = 0; row < height; row++) {
            int ta = 0, tr = 0, tg = 0, tb = 0;
            for (int col = 0; col < width; col++) {
                newY = (row / size) * size;
                newX = (col / size) * size;
                offsetX = newX + size;
                offsetY = newY + size;
                for (int subRow = newY; subRow < offsetY; subRow++) {
                    for (int subCol = newX; subCol < offsetX; subCol++) {
                        if (subRow < 0 || subRow >= height) {
                            continue;
                        }
                        if (subCol < 0 || subCol >= width) {
                            continue;
                        }
                        index = subRow * width + subCol;
                        ta = (inPixels[index] >> 24) & 0xff;
                        sumred += (inPixels[index] >> 16) & 0xff;
                        sumgreen += (inPixels[index] >> 8) & 0xff;
                        sumblue += inPixels[index] & 0xff;
                    }
                }
                index = row * width + col;
                tr = (int) (sumred / total);
                tg = (int) (sumgreen / total);
                tb = (int) (sumblue / total);
                outPixels[index] = (ta << 24) | (tr << 16) | (tg << 8) | tb;

                sumred = sumgreen = sumblue = 0; // reset them...
            }
        }
        result.setPixels(outPixels, 0, width, 0, 0, width, height);
        return result;
    }

    class MyOnScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener {


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (detector.getScaleFactor() > 1) {
                if (scaleFactor >= MAX_SCALE || (focusedItem != null && focusedItem.scaleFactorItem >= MAX_SCALE_ITEM)) {
                    hasScaleHandle = false;
                    return false;
                }
            } else {
                if (scaleFactor <= MIN_SCALE || (focusedItem != null && focusedItem.scaleFactorItem <= MIN_SCALE_ITEM)) {
                    hasScaleHandle = false;
                    return false;
                }
            }
            if (focusedItem != null) {
                focusedItem.scaleFactorItem *= detector.getScaleFactor();
                setScaleType(ScaleType.MATRIX);
                focusedItem.matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), focusedItem.pivot.x, focusedItem.pivot.y);
                focusedItem.onMatrixUpdate();
                invalidate();
            } else {
                scaleFactor *= detector.getScaleFactor();
                setScaleType(ScaleType.MATRIX);
                Matrix imageMatrix = getImageMatrix();
                imageMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), getWidth() / 2, getHeight() / 2);
                setImageMatrix(imageMatrix);
                invalidate();
            }

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

    class MyOnRotateGestureListener implements RotateGestureDetector.OnRotateGestureListener {


        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if (focusedItem != null) {
                focusedItem.rotateDegrees += detector.getRotationDegreesDelta() * -1;
                focusedItem.matrix.postRotate(-detector.getRotationDegreesDelta(), focusedItem.pivot.x, focusedItem.pivot.y);
                focusedItem.onMatrixUpdate();
                invalidate();
            }
            return true;
        }

        @Override
        public boolean onRotateBegin(RotateGestureDetector detector) {
            return true;
        }

        @Override
        public void onRotateEnd(RotateGestureDetector detector) {
        }
    }

    class DrawObj {
        int color;
        Path path;

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public Path getPath() {
            return path;
        }

        public void setPath(Path path) {
            this.path = path;
        }
    }

    OnTouchedListener onTouchedListener;

    public void setOnTouchedListener(OnTouchedListener onTouchedListener) {
        this.onTouchedListener = onTouchedListener;
    }

    interface OnTouchedListener {
        void OnTouchedListener(Item item);

        void OnScrollListener(Item item);

        void OnUpListener(Item item);
    }
}
