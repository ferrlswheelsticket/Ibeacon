package com.admin.ibeaconmeasure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import java.util.HashMap;

/**
 * Created by Administrator on 2016/8/29 0029.
 */
public class MapView extends View {

    public static int RATIO = 20;
    private float temp = 0.551915024494f;

    HashMap<String, Path> mPathMap = new HashMap<>();

    private int sWidth = 0;
    private int scale;

    private Canvas mCanvas = null;
    private Paint mPaint = null;



    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        sWidth = dm.widthPixels;
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
    }

    public MapView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context) {
        this(context, null);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        scale = sWidth / RATIO;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;
        for (int i = 0; i < RATIO; i++) {
            canvas.drawLine(0, i * scale, sWidth, i * scale, mPaint);
            canvas.drawLine(i * scale, 0, i * scale, sWidth, mPaint);
        }
        canvas.drawCircle(1 * scale, 1 * scale, 5, mPaint);
        canvas.drawCircle(1 * scale, (RATIO - 1) * scale, 5, mPaint);
        canvas.drawCircle((RATIO - 1) * scale, 1 * scale, 5, mPaint);
        canvas.drawCircle((RATIO - 1) * scale, (RATIO - 1) * scale, 5, mPaint);
        //eg:r1 = 5 r2 = 7 r3 = 9
        Path path1 = drawArc(15, 19, 19, 1);
        mPathMap.put("lt", path1);
        Path path2 = drawArc(12, 1, 1, 2);
        mPathMap.put("rb", path2);
        Path path3 = drawArc(13, 1, 19, 3);
        mPathMap.put("rt", path3);
//        Path path4 = drawArc(13, 19, 1, 4);
//        mPathMap.put("lb", path4);

        Path path = new Path();
        path.op(path1,path2, Path.Op.INTERSECT);
        path.op(path3,path, Path.Op.INTERSECT);
       // canvas.drawPath(path, mPaint);
        RectF rectF = new RectF();
        path.computeBounds(rectF,false);
        drawPoint(rectF.centerX(),rectF.centerY());

    }

    public Path drawArc(int r, int cx, int cy, int direction) {
        Path path = new Path();
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        path.reset();

        //left top
        if (direction == 1) {
            path.moveTo(cx * scale, (cy - r) * scale);
            path.cubicTo((cx - temp * r) * scale, (cy - r) * scale, (cx - r) * scale, (cy - temp
                    * r) * scale, (cx - r) * scale, cy * scale);
            mCanvas.drawPath(path, paint);
            mCanvas.drawPath(path, paint);
            //mCanvas.drawLine(cx * scale, cy * scale, cx * scale, (cy - r) * scale, paint);
            //mCanvas.drawLine(cx * scale, cy * scale, (cx - r) * scale, cy * scale, paint);

            //right bottom
        } else if (direction == 2) {
            path.moveTo(cx * scale, (cy + r) * scale);
//        // 连接路径到点
            path.cubicTo((temp * r + cx) * scale, (cy + r) * scale, (cx + r) * scale, (temp * r
                    + cy) * scale, (cx + r) * scale, cy * scale);
            mCanvas.drawPath(path, paint);
            //mCanvas.drawLine(cx * scale, cy * scale, cx * scale, (r + cy) * scale, paint);
            //mCanvas.drawLine(cx * scale, cy * scale, (cx + r) * scale, cy * scale, paint);
            //right top
        } else if (direction == 3) {
            path.moveTo(cx * scale, (cy - r) * scale);
//        // 连接路径到点
            path.cubicTo((temp * r + cx) * scale, (cy - r) * scale, (cx + r) * scale, (cy - temp
                    * r) * scale, (cx + r) * scale, cy * scale);
            mCanvas.drawPath(path, paint);
            //mCanvas.drawLine(cx * scale, cy * scale, cx * scale, (cy - r) * scale, paint);
            //mCanvas.drawLine(cx * scale, cy * scale, (cx + r) * scale, cy * scale, paint);

            //left bottom
        } else if (direction == 4) {

            path.moveTo(cx * scale, (cy + r) * scale);
            // 连接路径到点
            path.cubicTo((cx - temp * r) * scale, (cy + r) * scale, (cx - r) * scale, (cy + temp
                    * r) * scale, (cx - r) * scale, cy * scale);
            mCanvas.drawPath(path, paint);
            //mCanvas.drawLine(cx * scale, cy * scale, cx * scale, (cy + r) * scale, paint);
            //mCanvas.drawLine(cx * scale, cy * scale, (cx - r) * scale, cy * scale, paint);

        }

        return path;
    }


    public void drawCircle(int r, int cx, int cy) {
        drawArc(r, cx, cy, 1);
        drawArc(r, cx, cy, 2);
        drawArc(r, cx, cy, 3);
        drawArc(r, cx, cy, 4);
    }

    public void drawPoint(float cx, float cy) {
        mCanvas.drawCircle(cx, cy, 4, mPaint);
    }


}
