package com.sunshine.smart.widget;

import android.animation.Animator;
import android.animation.PointFEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;

/**
 * Created by fmk on 2016/7/13.
 */
public class RotateCircle extends View {

    private int radius = 60;
    private Paint p;
    private int width = 0;
    private int height = 0;
    private float progress = 270;
    //是否在转
    public boolean isT() {
        return t;
    }

    public boolean t ;

    public RotateCircle(Context context) {
        super(context);
        init(context,null);
    }

    public RotateCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public RotateCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    public void init(Context context,AttributeSet attrs){
        p = new Paint();
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        width = this.getMeasuredWidth();
        height = this.getMeasuredHeight();
        p.setAntiAlias(true);
        p.reset();
        p.setColor(Color.LTGRAY);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(radius);
        //外面大圆
        canvas.drawCircle(width/2, height/2, (width-radius)/2, p);

        p.reset();
        p.setAntiAlias(true);
        p.setColor(Color.RED);
        p.setStrokeWidth(0);

        int x = (int) ((width/2)+(width/2-radius/2)*Math.cos(1*progress*3.14/180));
        int y = (int) ((width/2)+(width/2-radius/2)*Math.sin(1*progress*3.14/180));

        canvas.drawCircle(x, y, radius/2, p);


    }

    private ValueAnimator valueAnimator;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void moveTo(){
        valueAnimator=ValueAnimator.ofFloat(progress,630);
        Log.e("progress","progress:"+progress);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setRepeatCount(Animation.INFINITE);
        valueAnimator.setDuration(5000).start();
    }


    public void start(boolean t){
        this.t = t;
        if (t){
            progress = 270;
            moveTo();
        }else{
            valueAnimator.end();
        }
    }



}
