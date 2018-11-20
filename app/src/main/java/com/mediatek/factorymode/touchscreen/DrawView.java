package com.mediatek.factorymode.touchscreen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawView extends SurfaceView implements
        SurfaceHolder.Callback, Runnable {

    private float preX;
    private float preY;

    public DrawView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        initView();
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initView();
    }

    public DrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
        initView();
    }

    // SurfaceHolder实例
    private SurfaceHolder mSurfaceHolder;
    // Canvas对象
    private Canvas mCanvas;
    // 控制子线程是否运行
    private boolean startDraw;
    // Path实例
    private Path mPath = new Path();
    // Paint实例
    private Paint mpaint = new Paint();


    private void initView() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        // 设置可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        // 设置常亮
        this.setKeepScreenOn(true);

    }

    @Override
    public void run() {
        // 如果不停止就一直绘制
        while (startDraw) {
            // 绘制
            draw();
        }
    }

    /*
     * 创建
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startDraw = true;
        new Thread(this).start();
    }

    /*
     * 改变
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    /*
     * 销毁
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        startDraw = false;
    }

    private void draw() {
        try {
            mCanvas = mSurfaceHolder.lockCanvas();
            mCanvas.drawColor(Color.WHITE);
            mpaint.setStyle(Paint.Style.STROKE);

            mpaint.setStrokeWidth(10);
            mpaint.setColor(Color.BLACK);
            mCanvas.drawPath(mPath, mpaint);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 对画布内容进行提交
            if (mCanvas != null) {
                try{
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();    //获取手指移动的x坐标
        int y = (int) event.getY();    //获取手指移动的y坐标
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = x;
                preY = y;
                mPath.moveTo(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - preX);
                float dy = Math.abs(y - preY);
                if(dx >= 2.0 || dy >= 2.0) {
                    /*
                     * 二次贝塞尔曲线参数说明：
                     * 控制点：为每次event获得的点(x, y)
                     * 起点和终点：为获得的点(x, y)的一系列中间点。
                     * 说明：这样贝塞尔曲线一般是不会经过(x, y)（除非是直线的时候），而是经过所有中间点。
                     * 		 但却能保证曲线向(x, y)的方向弯曲。
                     * 		 如果终点设置为(x, y)，控制点很难去计算。
                     * 缺点：根据贝塞尔曲线公式可以知道：控制点和启动终点共线的话，曲线段变成直线。
                     * 		 刚开始绘线的一小段会变成直线，因为起点和控制点重叠，不过效果很不明显。
                     * 		 如果是path.quadTo(preX, preY, x, y);也变成画直线，跟path.lineTo(x,y);一样效果
                     */
                    //二次贝塞尔曲线：控制点设置为起点，终点设置为中间点
                    mPath.quadTo(preX, preY, (preX + x) / 2, (preY + y) / 2);
                    preX = x;
                    preY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                mPath.lineTo(x,y);
                break;
        }
        return true;
    }

    // 重置画布
    public void reset() {
        mPath.reset();
    }

    public void exit() {
        startDraw = false;
    }

}

