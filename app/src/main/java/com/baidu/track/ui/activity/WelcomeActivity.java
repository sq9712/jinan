package com.baidu.track.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.baidu.track.R;

import butterknife.ButterKnife;

//欢迎页面
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //判断是否为第一次安装应用,并返回要加载的欢迎页面
        int layoutId = isFirstOpen();
        setContentView(layoutId);
        ButterKnife.bind(this);
    }

    public int isFirstOpen() {
        int layoutId;
        layoutId = R.layout.activity_welcome_nor;
        //启动延时跳转Activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                WelcomeActivity.this.finish();  //销毁欢迎页，否则按下返回键会退回到欢迎页面
            }
        }, 1000);
        return layoutId;
    }

}
