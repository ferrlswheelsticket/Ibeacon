package com.admin.ibeaconmeasure;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Administrator on 2016/8/25 0025.
 */
public class CircleView extends View {

    public static int DEFAULT_RADIUS_RATIO = 15;

    private Paint mPaint = new Paint();

    private Canvas mCanvas = null;

    private double mRadius = 1;

    private int cx,cy;

    public CircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleView(Context context) {
        this(context, null);
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint.setColor(Color.BLUE);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        cx = dm.widthPixels/3;
        cy = dm.heightPixels/2;

        ValueAnimator animator = ValueAnimator.ofFloat(0f,1f);
        animator.setDuration(2000);
        animator.addUpdateListener( new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                float a = (float) animation.getAnimatedValue();
                mRadius = (1+a);
                invalidate();
            }
        });
        animator.start();

    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        drawCircle(mRadius, cx, cy);

    }



    public void setRadius(double radius){
        this.mRadius = radius;
    }

    public void drawCircle( double radius, int x, int y) {

        mCanvas.drawCircle(x, y, (float) (radius * DEFAULT_RADIUS_RATIO), mPaint);

    }



}
