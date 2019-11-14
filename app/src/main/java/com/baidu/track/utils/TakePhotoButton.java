package com.baidu.track.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.baidu.track.R;

import static com.jorge.circlelibrary.DemiUitls.dip2px;


/**
 * Created by CKZ on 2017/8/9.
 */

public class TakePhotoButton extends View {
    public float circleWidth;//外圆环宽度
    public int outCircleColor;//外圆颜色
    public int innerCircleColor;//内圆颜色
    public int progressColor;//进度条颜色

    private Paint outRoundPaint = new Paint(); //外圆画笔
    private Paint mCPaint = new Paint();//进度画笔
    private Paint innerRoundPaint = new Paint();
    private float width; //自定义view的宽度
    private float height; //自定义view的高度
    private float outRaduis; //外圆半径
    private float innerRaduis;//内圆半径
    private GestureDetectorCompat mDetector;//手势识别
    private boolean isLongClick;//是否长按
    private float startAngle = -90;//开始角度
    private float mmSweepAngleStart = 0f;//起点
    private float mmSweepAngleEnd = 360f;//终点
    private float mSweepAngle;//扫过的角度
    private int mLoadingTime;



    public TakePhotoButton(Context context) {
        this(context,null);
}

    public TakePhotoButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);

    }

    public TakePhotoButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs){

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TakePhotoButton);
        outCircleColor = array.getColor(R.styleable.TakePhotoButton_outCircleColor, Color.RED);
        innerCircleColor = array.getColor(R.styleable.TakePhotoButton_innerCircleColor, Color.WHITE);
        progressColor = array.getColor(R.styleable.TakePhotoButton_readColor,  Color.parseColor("#00FFFF"));
        mLoadingTime = array.getInteger(R.styleable.TakePhotoButton_maxSeconds,10);
        mDetector = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                //单击
                isLongClick = false;
                if (listener != null) {
                   listener.onClick(TakePhotoButton.this);
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                //长按
                isLongClick = true;
                postInvalidate();
                if (listener != null) {
                    listener.onLongClick(TakePhotoButton.this);
                }
            }
        });
        mDetector.setIsLongpressEnabled(true);
        textstr="开始采集";
        textColor=0xFFFF0000;
        test();

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (width > height) {
            setMeasuredDimension(height, height);
        } else {
            setMeasuredDimension(width, width);
        }
    }
    private void resetParams() {
        width = getWidth();
        height = getHeight();
        circleWidth = width*0.08f;
        outRaduis = (float) (Math.min(width, height)/2.5);
        innerRaduis = outRaduis -circleWidth/2;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        resetParams();
        //画外圆
        outRoundPaint.setAntiAlias(true);
        outRoundPaint.setColor(outCircleColor);
        if (isLongClick){
            setMinimumWidth((int) (width*1.2));
           canvas.scale(1.0f,1.0f,width/2,width/2);

        }else {
            setMinimumWidth((int) width);
        }
        canvas.drawCircle(width/2,height/2, outRaduis, outRoundPaint);
        //画内圆
        innerRoundPaint.setAntiAlias(true);
        innerRoundPaint.setColor(innerCircleColor);
        if (isLongClick){
            canvas.drawCircle(width/2,height/2, innerRaduis , innerRoundPaint);
            //画外原环
            mCPaint.setAntiAlias(true);
            mCPaint.setColor(progressColor);
            mCPaint.setStyle(Paint.Style.STROKE);
            mCPaint.setStrokeWidth(circleWidth/2);
            RectF rectF = new RectF(0+circleWidth,0+circleWidth,width-circleWidth,height-circleWidth);
            canvas.drawArc(rectF,startAngle,mSweepAngle,false,mCPaint);
        }else {
            canvas.drawCircle(width/2,height/2, innerRaduis, innerRoundPaint);
        }


        mXCenter = getWidth() / 2;
        mYCenter = getHeight() / 2;


        mTxtWidth = textPaint.measureText(textstr, 0, textstr.length());
        textPaint.setColor(textColor);
        canvas.drawText(textstr, mXCenter - mTxtWidth / 2, mYCenter + mTxtHeight/ 4, textPaint);
    }

    public void start() {
        ValueAnimator animator = ValueAnimator.ofFloat(mmSweepAngleStart, mmSweepAngleEnd);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mSweepAngle = (float) valueAnimator.getAnimatedValue();
                //获取到需要绘制的角度，重新绘制
                invalidate();
            }
        });
        //这里是时间获取和赋值
        ValueAnimator animator1 = ValueAnimator.ofInt(mLoadingTime, 0);
        animator1.setInterpolator(new LinearInterpolator());
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int time = (int) valueAnimator.getAnimatedValue();
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator, animator1);
        set.setDuration(mLoadingTime * 100);
        set.setInterpolator(new LinearInterpolator());
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                clearAnimation();
                isLongClick = false;
                postInvalidate();
                if (listener != null) {
                    listener.onFinish();
                }
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        switch(MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN:
                isLongClick = false;
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    private OnProgressTouchListener listener;

    public void setOnProgressTouchListener(OnProgressTouchListener listener) {
        this.listener = listener;
    }

    /**
     * 进度触摸监听
     */
    public interface OnProgressTouchListener {
        /**
         * 单击
         * @param photoButton
         */
        void onClick(TakePhotoButton photoButton);

        /**
         * 长按
         * @param photoButton
         */
        void onLongClick(TakePhotoButton photoButton);


        void onFinish();
    }
    // 半径
    private float mRadius;
    // 字的长度
    private float mTxtWidth;
    // 字的高度
    private float mTxtHeight;

    // 圆心x坐标
    private int mXCenter;
    // 圆心y坐标
    private int mYCenter;

    private int mTextSize;
    private Paint textPaint;
    public String textstr;
    public int textColor;//字体颜色
    //private RectF mColorWheelRectangle = new RectF();
    private void test(){


        mTextSize = dip2px(getContext(), 15);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setColor(textColor);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(mTextSize);

        Paint.FontMetrics fm = textPaint.getFontMetrics();
        mTxtHeight = (int) Math.ceil(fm.descent - fm.ascent);

    }
    public void setTextstr(String textstr){
        this.textstr=textstr;
    }
    public void setTextColor(int textColor){
        this.textColor=textColor;
    }

}
