package com.jikexueyuan.jkwelcomelayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;

/**
 * 可用于动画的TextView组件 2017/5/15
 * By 心无波澜-极客学院
 */

public class JKAnimateTextView extends android.support.v7.widget.AppCompatTextView implements JKAnimateControl {
    public int animateType;
    public int translateDirection;
    public float xFraction;
    public float yFraction;

    public int destTop;
    public int destLeft;

    private int duration;
    private int delay;

    private ObjectAnimator animator;
    public boolean isAnimating = false;



    public JKAnimateTextView(Context context) {
        super(context);
    }

    public JKAnimateTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initParams(context, attrs);
    }

    public JKAnimateTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParams(context, attrs);
    }


    private void initParams(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JKAnimateTextView);
        animateType = typedArray.getInt(R.styleable.JKAnimateTextView_animate_type,-1);
        translateDirection = typedArray.getInt(R.styleable.JKAnimateTextView_translate_direction,-1);
        xFraction = typedArray.getFloat(R.styleable.JKAnimateTextView_x_fraction,-1f);
        yFraction = typedArray.getFloat(R.styleable.JKAnimateTextView_y_fraction,-1f);
        duration = typedArray.getInt(R.styleable.JKAnimateTextView_duration,500);
        delay = typedArray.getInt(R.styleable.JKAnimateTextView_delay,0);


        typedArray.recycle();
    }

    public void startAnim() {

        TimeInterpolator interpolator = new DecelerateInterpolator();
        isAnimating = true;
        if (animateType==0) {
            if (translateDirection==0||translateDirection==2) {
                animator = ObjectAnimator.ofFloat(this,"translationY",destTop);

            }
            else
            {
                animator = ObjectAnimator.ofFloat(this,"translationX",destLeft);
            }
        }
        else if (animateType==1) {
            animator = ObjectAnimator.ofFloat(this,"alpha",1f);
        }
        else {
            return;
        }
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        animator.setStartDelay(delay);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isAnimating = false;
            }
        });
        animator.start();


    }

    public void cancelAnim(){
        animator.cancel();
    }



    public void setDestTop(int destTop) {
        this.destTop = destTop;
    }

    public void setDestLeft(int destLeft) {
        this.destLeft = destLeft;
    }

    public float getxFraction() {
        return xFraction;
    }

    public float getyFraction() {
        return yFraction;
    }


    @Override
    public int getAnimateType() {
        return animateType;
    }

    @Override
    public int getTranslateDirection() {
        return translateDirection;
    }

    public boolean isAnimating() {
        return isAnimating;
    }
}
