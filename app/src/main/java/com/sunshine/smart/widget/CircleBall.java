package com.sunshine.smart.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.sunshine.smart.R;

/**
 * Created by mac on 16/7/12.
 */
public class CircleBall extends View {


    private Context context;
    private Canvas canvas;
    private Paint paint;

    public CircleBall(Context context) {
        super(context);
        this.context = context;
    }

    public CircleBall(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public CircleBall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.paint = new Paint();
        this.paint.setAntiAlias(true); //消除锯齿
        this.paint.setStyle(Paint.Style.STROKE); //绘制空心圆
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleBall(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        this.paint = new Paint();
        this.paint.setAntiAlias(true); //消除锯齿
        this.paint.setStyle(Paint.Style.STROKE); //绘制空心圆
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int center = getWidth()/2;
        int innerCircle = 100; //设置内圆半径
        int ringWidth = 50; //设置圆环宽度


        //绘制内圆
//        this.paint.setARGB(155, 167, 190, 206);
        this.paint.setStrokeWidth(2);
        canvas.drawCircle(center,center, innerCircle, this.paint);

        //绘制圆环
        this.paint.setARGB(255, 212 ,225, 233);
        this.paint.setStrokeWidth(ringWidth);
        canvas.drawCircle(center,center, innerCircle+1+ringWidth/2, this.paint);

        //绘制外圆
        this.paint.setARGB(155, 167, 190, 206);
        this.paint.setStrokeWidth(2);
        canvas.drawCircle(center,center, innerCircle+ringWidth, this.paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
