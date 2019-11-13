package com.jikexueyuan.jkwelcomelayout;

/**
 * 通过接口对外公布控件的调用方法，包括从View类继承的方法
 * 这样可以把不同控件转换为统一的接口对象进行操作
 */

interface JKAnimateControl {
    int getAnimateType();
    int getTranslateDirection();
    float getxFraction();
    float getyFraction();
    void setDestTop(int destTop);
    void setDestLeft(int destLeft);
    boolean isAnimating();
    void startAnim();
    void cancelAnim();


    int getTop();
    int getLeft();
    int getRight();
    int getBottom();
    int getMeasuredHeight();
    int getMeasuredWidth();
    void setTranslationX(float translationX);
    void setTranslationY(float translationY);
    void setAlpha(float alpha);
    void layout(int l, int t, int r, int b);

}
