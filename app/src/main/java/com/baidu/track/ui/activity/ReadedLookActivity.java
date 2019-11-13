package com.baidu.track.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.track.R;
import com.bm.library.Info;
import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;

/**
 * 已完成任务详情页
 */
public class ReadedLookActivity extends Activity {
        private LinearLayout options;
        private TextView titles;

        private PhotoView imgRead;
        private TextView titleView,contentView,crTime;
        private String title,content,createTime,imgUrl;
        private View imgEntryView;
        private com.baidu.track.photoview.PhotoView img;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_readed_task);

            options = findViewById(R.id.btn_activity_options);
            options.setVisibility(View.INVISIBLE);
            titles = findViewById(R.id.tv_activity_title);
            titles.setText("任务清单");

            Bundle bundle=getIntent().getExtras();
            title = bundle.getString("title");
            createTime = bundle.getString("createTime");
            content = bundle.getString("content");
            imgUrl = bundle.getString("imgurl");

            imgRead = findViewById(R.id.img_read);
            titleView = findViewById(R.id.Title);
            contentView = findViewById(R.id.Details);
            crTime = findViewById(R.id.Time);

            titleView.setText(title);
            contentView.setText(content);
            crTime.setText(createTime);

            Glide.with(this)
                    .load(imgUrl)
                    .into( imgRead);
            LayoutInflater inflater = LayoutInflater.from(this);
            imgEntryView = inflater.inflate(R.layout.dialog_photo, null);
            // 加载自定义的布局文件
            final AlertDialog dialog = new AlertDialog.Builder(ReadedLookActivity.this).create();
             img = imgEntryView.findViewById(R.id.large_image);

            //设置不可以双指缩放移动放大等操作，跟普通的image一模一样,默认情况下就是disenable()状态
            imgRead.disenable();
            imgRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   Glide.with(getApplicationContext())
                            .load(imgUrl)
                            .into( img);
                    dialog.setView(imgEntryView); // 自定义dialog
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialog.show();

                }
            });
            // 点击大图关闭dialog
            imgEntryView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View paramView) {
                    dialog.cancel();
                }
            });

        }


        public void onBack(View v){ super.onBackPressed(); }
}
