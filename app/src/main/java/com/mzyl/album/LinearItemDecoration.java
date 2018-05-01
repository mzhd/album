package com.mzyl.album;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class LinearItemDecoration extends RecyclerView.ItemDecoration {
    private int strokeWidth;

    public LinearItemDecoration(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    /**
     * strokeWidth / 2在偏移距离的中间位置开始画线，如下图
     *
     *---------------------------------
     * 画线画线画线画线画线画线画线画线画线画线
     *---------------------------------
     */
    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int childCount = parent.getChildCount();
        Paint paint = new Paint();
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setColor(Color.LTGRAY);
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            ViewGroup.MarginLayoutParams margin = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
            c.drawLine(child.getLeft() - margin.leftMargin, child.getBottom() + margin.bottomMargin + strokeWidth / 2, child.getRight() + margin.rightMargin, child.getBottom() + margin.bottomMargin + strokeWidth / 2, paint);
        }
    }

    /**
     * strokeWidth item向下偏移的距离
     */
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.set(0, 0, 0, strokeWidth);
    }
}
