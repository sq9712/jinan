package com.baidu.track.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.track.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RuleDetailActivity extends Activity implements View.OnClickListener {
    private LinearLayout btn_back;
    private TextView txt_title;
    private Button btn_top;
    private TextView wView;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        bindViews();
    }

    private void bindViews() {
        Bundle bundle=getIntent().getExtras();
        title=bundle.getString("legal");
        btn_back = findViewById(R.id.btn_back);
        txt_title = findViewById(R.id.txt_title);
        btn_top = findViewById(R.id.btn_top);
        txt_title.setText(title);
        wView = findViewById(R.id.wView);

        wView.setMovementMethod(ScrollingMovementMethod.getInstance());

        btn_back.setOnClickListener(this);
        btn_top.setOnClickListener(this);

        if(title.equals("《无证无照经营查处办法》")){
            wView.setText(
                    readStream(getResources().openRawResource(R.raw.rule1))
            );
        }else if(title.equals("《食品安全法》")){
            wView.setText(
                    readStream(getResources().openRawResource(R.raw.rule2))
            );
        }else if(title.equals("《城市道路管理条例》")){
            wView.setText(
                    readStream(getResources().openRawResource(R.raw.rule3))
            );
        }else if(title.equals("《环境噪声污染防治法》")){
            wView.setText(
                    readStream(getResources().openRawResource(R.raw.rule4))
            );
        }else if(title.equals("《山东省环境噪声污染防治条例》")){
            wView.setText(
                    readStream(getResources().openRawResource(R.raw.rule5))
            );
        }else if(title.equals("《卫星电视广播地面接收设施管理规定》")){
            wView.setText(
                    readStream(getResources().openRawResource(R.raw.rule6))
            );
        }else if(title.equals("《城市建筑垃圾和工程渣土管理规定》")){
            wView.setText(
                    readStream(getResources().openRawResource(R.raw.rule7))
            );
        }
    }


    @Override
    public void onClick (View v){
        switch (v.getId()) {
            case R.id.btn_back:
                finish();          //关闭当前Activity
                break;
            case R.id.btn_top:
                wView.setScrollY(0);   //滚动到顶部
                break;
        }
    }
    //读写流
    private String readStream(InputStream is) {
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            int i = is.read();
            while(i != -1) {
                bo.write(i);
                i = is.read();
            }
            return bo.toString();
        } catch (IOException e) {
            return "";
        }
    }
}
