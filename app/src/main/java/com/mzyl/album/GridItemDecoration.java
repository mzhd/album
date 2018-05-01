package com.mzyl.album;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridItemDecoration extends RecyclerView.ItemDecoration{
    private int strokeWidth ;

    public GridItemDecoration(int strokeWidth) {
        this.strokeWidth=strokeWidth;
    }
    /**
     * strokeWidth / 2在偏移距离的中间位置开始画线，如下图
     *---------------------------------
     * 画线画线画线画线画线画线画线画线画线画线
     *---------------------------------
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int count = parent.getChildCount();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(strokeWidth );
        GridLayoutManager gridManager=null;
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
             gridManager = (GridLayoutManager) parent.getLayoutManager();

        }
        if (gridManager == null || strokeWidth == 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            Rect rect = new Rect();
            View childView = parent.getChildAt(i);
            getItemOffsets(rect, parent.getChildAt(i), parent, state);
            //画横线
            int left = childView.getLeft() - strokeWidth/2;
            int top = childView.getBottom() + strokeWidth/2;
            int right = childView.getRight() + strokeWidth/2;
            int bottom = top;
            c.drawLine(left, top, right, bottom, paint);
            //画纵线
            left = childView.getRight() + strokeWidth/2;
            top = childView.getTop() - strokeWidth/2;
            right = left;
            bottom = childView.getBottom() + strokeWidth/2;
            c.drawLine(left, top, right, bottom, paint);
            //画最上面线
            if (i < gridManager.getSpanCount()) {
                left = childView.getLeft() - strokeWidth/2;
                top = childView.getTop() - strokeWidth/2;
                right = childView.getRight() + strokeWidth/2;
                bottom = top;
                c.drawLine(left, top, right, bottom, paint);
            }
            if (i % gridManager.getSpanCount() == 0) {
                //画最左边线
                left = childView.getLeft() - strokeWidth/2;
                top = childView.getTop() - strokeWidth/2;
                right = left;
                bottom = childView.getBottom() + strokeWidth/2;
                c.drawLine(left, top, right, bottom, paint);
            }
        }
    }

    /**
    * strokeWidth item向下偏移的距离
    */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(strokeWidth, strokeWidth, strokeWidth, strokeWidth);
    }
}
