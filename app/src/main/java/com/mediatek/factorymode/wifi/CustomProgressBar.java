package com.mediatek.factorymode.wifi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class CustomProgressBar extends ProgressBar {
    public static final int DEFAULT_REACHERD_COLOR = 0xFF698B22;
    public static final int DEFAULT_UNREACHED_COLOR = 0xFFCCCCCC;

    private Paint paint;
    private int reachedColor;  //已到达进度条颜色
    private int unreachedColor;

    public CustomProgressBar(Context context) {
        this(context, null);
    }

    public CustomProgressBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CustomProgressBar(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        paint = new Paint();
        reachedColor = DEFAULT_REACHERD_COLOR;
        unreachedColor = DEFAULT_UNREACHED_COLOR;
    }

    @Override
    public void onDraw(Canvas canvas) {
        //获取画布的宽高
        int width = getWidth();
        int height = getHeight();
        //获取进度条的实际宽高
        int lineWidth = width - getPaddingLeft() - getPaddingRight();
        int lineHeight = height - getPaddingTop() - getPaddingBottom();
        //获取当前进度
        float ratio = getProgress() * 1.0f / getMax();
        //获取未完成和已完成进度大小
        int unreachedWidth = (int) (lineWidth * (1 - ratio));
        int reachedWidth = lineWidth - unreachedWidth;
        //绘制已完成进度条，设置画笔颜色和大小
        paint.setColor(reachedColor);
        paint.setStrokeWidth(lineHeight);
        //计算已完成进度条起点和终点的坐标
        int startX = getPaddingLeft();
        int startY = getHeight() / 2;
        int stopX = startX + reachedWidth;
        int stopY = startY;
        //画线
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        //绘制未完成进度条
        paint.setColor(unreachedColor);
        startX = getPaddingLeft() + reachedWidth;
        stopX = width - getPaddingRight();
        canvas.drawLine(startX, startY, stopX, stopY, paint);
    }
}

