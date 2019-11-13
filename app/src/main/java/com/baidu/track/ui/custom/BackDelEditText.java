package com.baidu.track.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/**
 * 带动态删除按钮的文本框控件
 * 作者：心无波澜
 * QQ：30548842
 */

@SuppressLint("AppCompatCustomView")
public class BackDelEditText extends EditText {
    private Drawable backDrawable;
    private Drawable originLeftDrawable;
    private Context mContext;
    private boolean isFocused=false;

    public BackDelEditText(Context context) {
        super(context);
        mContext = context;
    }

    public BackDelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public BackDelEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setDrawableBack(int originLeft, int drawableId) {
        //此处不使用 mContext.getResources().getDrawable(drawableId,null)   以确保向前兼容
        backDrawable = ContextCompat.getDrawable(mContext, drawableId);
        originLeftDrawable = ContextCompat.getDrawable(mContext, originLeft);


        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setDrawable();
            }
        });

        setDrawable();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        isFocused = focused;
        setDrawable();
    }

    private void setDrawable() {
        if (length() < 1 || !isFocused) {
            setCompoundDrawablesWithIntrinsicBounds(originLeftDrawable, null, null, null);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(originLeftDrawable, null, backDrawable, null);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //如果不是按下事件或没有清除按钮，不再处理
        if (backDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getX() > getWidth() - getPaddingRight() - backDrawable.getIntrinsicWidth()) {
                setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        backDrawable.setCallback(null);
		originLeftDrawable.setCallback(null);
    }


}
