package com.jikexueyuan.jkwelcomelayout;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义欢迎页面 2017/5/15
 * By 心无波澜-极客学院
 */

public class JKWelcomeLayout extends LinearLayout {
    //子布局数量、当前显示界面的编号以及上一次显示的界面编号
    int count;
    int prevIndex = 0;
    int index = 0;

    //布局滚动相关参数
    int scrollStartY;
    int startY;
    int destY;
    int distance;
    float flyingFraction = 0.3f;
    int flyingDistace = 0;
    boolean isScrolling = false;

    //手机屏幕可操作区域的宽高
    int screenWidth;
    int screenHeight;

    //平滑滚动辅助类
    Scroller scroller;
    //所有带动画的子控件集合
    SparseArray<List<JKAnimateControl>> childTextViews;


    public JKWelcomeLayout(Context context) {
        super(context);
        initLayout(context);
    }

    public JKWelcomeLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initLayout(context);
    }


    public JKWelcomeLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    private void initLayout(Context context) {

        setClickable(true);     //这个设置不可缺少，否则页面无法响应MOVE和UP事件

        scroller = new Scroller(context);
        childTextViews = new SparseArray<>();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //获取当前布局的宽高，因为使用了FullScreen的主题，所以默认为屏幕可操作区域的大小
        screenWidth = getMeasuredWidth();
        screenHeight = getMeasuredHeight();
        //设置翻页的滑动最小距离
        flyingDistace = (int) (screenHeight * flyingFraction);

        count = getChildCount();
        for (int i = 0; i < count; i++) {
            ViewGroup child = (ViewGroup) getChildAt(i);
            //对子布局设置宽高属性，使之全屏显示
            child.getLayoutParams().width = screenWidth;
            child.getLayoutParams().height = screenHeight;

            initializeSubLayout(i);     //遍历所有带有动画属性的子控件，初始化位置和相关属性
        }

        //启动动画效果
        postAnimation(0);

    }




    private void initializeSubLayout(int childIndex) {
        ViewGroup child = (ViewGroup) getChildAt(childIndex);
        List<JKAnimateControl> tvList = childTextViews.get(childIndex);
        if (tvList == null){
            tvList = new ArrayList<>();
        }

        for (int i=0;i<child.getChildCount();i++){
            if (child.getChildAt(i) instanceof JKAnimateControl){

                JKAnimateControl jkAnimateControl = (JKAnimateControl) child.getChildAt(i);
                if (jkAnimateControl.getxFraction()>0){
                    int layoutLeft = (int) (screenWidth*jkAnimateControl.getxFraction() - jkAnimateControl.getMeasuredWidth()/2);
                    jkAnimateControl.layout(layoutLeft,jkAnimateControl.getTop(),layoutLeft+jkAnimateControl.getMeasuredWidth(),jkAnimateControl.getBottom());
                }
                if (jkAnimateControl.getyFraction()>0){
                    int layoutTop = (int) (screenHeight*jkAnimateControl.getyFraction() - jkAnimateControl.getMeasuredHeight()/2);
                    jkAnimateControl.layout(jkAnimateControl.getLeft(),layoutTop,jkAnimateControl.getRight(),layoutTop+jkAnimateControl.getMeasuredHeight());
                }

                if (jkAnimateControl.getAnimateType()==0){
                    switch (jkAnimateControl.getTranslateDirection()){
                        case 0:
                            jkAnimateControl.setDestTop(jkAnimateControl.getTop()+jkAnimateControl.getMeasuredHeight());
                            jkAnimateControl.layout(jkAnimateControl.getLeft(),-jkAnimateControl.getMeasuredHeight(),jkAnimateControl.getRight(),0);
                            break;
                        case 1:
                            jkAnimateControl.setDestLeft(jkAnimateControl.getLeft()+jkAnimateControl.getMeasuredWidth());
                            jkAnimateControl.layout(-jkAnimateControl.getMeasuredWidth(),jkAnimateControl.getTop(),0,jkAnimateControl.getBottom());
                            break;
                        case 2:
                            jkAnimateControl.setDestTop(-(screenHeight-jkAnimateControl.getTop()));
                            jkAnimateControl.layout(jkAnimateControl.getLeft(),screenHeight,jkAnimateControl.getRight(),screenHeight+jkAnimateControl.getMeasuredHeight());
                            break;
                        case 3:
                            jkAnimateControl.setDestLeft(-(screenWidth-jkAnimateControl.getLeft()));
                            jkAnimateControl.layout(screenWidth,jkAnimateControl.getTop(),screenWidth+jkAnimateControl.getMeasuredWidth(),jkAnimateControl.getBottom());
                            break;
                    }

                }
                else if (jkAnimateControl.getAnimateType()==1){
                    jkAnimateControl.setAlpha(0f);
                }
                if (!tvList.contains(jkAnimateControl))
                    tvList.add(jkAnimateControl);


            }
        }
        childTextViews.put(childIndex,tvList);
    }

    /**
     * 翻页时，初始化上一个页面的所有控件位置，在这里使用layout方法是无效的，所以通过设置属性的方法来初始化
     * @param childIndex 上一次显示的子布局编号
     */
    private void reinitSubLayout(int childIndex) {
        List<JKAnimateControl> viewList = childTextViews.get(childIndex);
        if (viewList == null){
            throw new RuntimeException("animating control not exists");
        }
        for (JKAnimateControl view:viewList) {

            if (view.getAnimateType()==0){
                switch (view.getTranslateDirection()){
                    case 0:
                        view.setTranslationY(-(view.getTop()+view.getMeasuredHeight()));
                        break;
                    case 1:
                        view.setTranslationX(-(view.getLeft()+view.getMeasuredWidth()));
                        break;
                    case 2:
                        view.setTranslationY(screenHeight-view.getTop());
                        break;
                    case 3:
                        view.setTranslationX(screenWidth-view.getLeft());
                        break;
                }

            }
            else if (view.getAnimateType()==1){
                view.setAlpha(0f);
            }


        }
    }

    /**
     *  开始编号为 i 的页面的所有动画对象的动画
     */
    private void postAnimation(int i) {
        List<JKAnimateControl> viewList = childTextViews.get(i);
        if (viewList == null){
            throw new RuntimeException("animating control not exists");
        }
        for (JKAnimateControl jkac:viewList) {
            if (!jkac.isAnimating())
            {
                jkac.startAnim();
            }
        }

    }

    /**
     *  停止编号为 i 的页面的所有动画对象的动画
     */
    private void postCancelAnimation(int i) {
        List<JKAnimateControl> viewList = childTextViews.get(i);
        if (viewList == null){
            throw new RuntimeException("animating control not exists");
        }
        for (JKAnimateControl jkac:viewList) {
            if (jkac.isAnimating())
            {
                jkac.cancelAnim();
            }
        }
    }

    /**
     * 实现页面滑动和翻页的核心方法
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isScrolling){
            return false;
        }
        int action = event.getAction();


        int y = (int) event.getY();
        int scrollY = getScrollY();


        switch (action) {
            case MotionEvent.ACTION_DOWN:
                scrollStartY = scrollY;
                startY = y;
                distance = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                int destY = scrollY + (startY - y);

                if (destY <= 0)
                    destY = 0;
                if (destY >= screenHeight * (count - 1))
                    destY = screenHeight * (count - 1);

                scrollTo(0, destY);
                startY = y;

                distance = scrollY - scrollStartY;
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(distance) > 0) {
                    isScrolling = true;
                }


                caculateScroll(distance);

                break;
        }
        return super.onTouchEvent(event);
    }


    /**
     * 计算滑动距离，判断是否翻页
     * @param distance 滑动距离
     */
    private void caculateScroll(int distance) {
        if (Math.abs(distance) > flyingDistace) {
            postCancelAnimation(index);
            if (distance < 0) {
                scrollUp();
            } else {
                scrollDown();
            }
        } else {
            startScroll();
        }


    }

    public void scrollUp() {
        if (index > 0) {
            prevIndex = index;
            index--;
        }
        startScroll();
    }

    public void scrollDown() {
        if (index < count - 1) {
            prevIndex = index;
            index++;
        }
        startScroll();
    }

    private void startScroll() {
        destY = index * screenHeight;
        scroller.startScroll(0, getScrollY(), 0, destY - getScrollY());
        invalidate();
    }


    @Override
    public void computeScroll() {
        //滑动结束的判断
        if (scroller.isFinished() && scroller.getCurrY() == destY && isScrolling) {
            if (prevIndex != index){

                reinitSubLayout(prevIndex);
                postAnimation(index);
            }
            isScrolling = false;
        }
        // 重写computeScroll()方法，并在其内部完成平滑滚动的逻辑
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            invalidate();
        }
    }


}
