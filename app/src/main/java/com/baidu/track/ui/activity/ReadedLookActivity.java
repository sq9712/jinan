package com.baidu.track.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.track.R;
import com.baidu.track.ui.custom.JKRecyclerView.onItemClickListener;
import com.baidu.track.ui.view.ImageAdapter;
import com.baidu.track.ui.view.PhotoAdapter;
import com.bm.library.Info;
import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * 已完成任务详情页
 */
public class ReadedLookActivity extends Activity {
        private LinearLayout options;
        private TextView titles;
        private Context mContext;
        private TextView titleView,contentView,crTime;
        private String title,content,createTime;
        private View imgEntryView;
        private com.baidu.track.photoview.PhotoView img;

        private RecyclerView recyclerView;
        private ImageAdapter imageAdapter;
        private ArrayList<String> urlList  = new ArrayList<>();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_readed_task);
            mContext = this;

            options = findViewById(R.id.btn_activity_options);
            options.setVisibility(View.INVISIBLE);
            titles = findViewById(R.id.tv_activity_title);
            titles.setText("任务清单");

            Bundle bundle=getIntent().getExtras();
            title = bundle.getString("title");
            createTime = bundle.getString("createTime");
            content = bundle.getString("content");
            urlList = bundle.getStringArrayList("imgurls");

            titleView = findViewById(R.id.Title);
            contentView = findViewById(R.id.Details);
            contentView.setMovementMethod(ScrollingMovementMethod.getInstance());
            crTime = findViewById(R.id.Time);

            titleView.setText(title);
            contentView.setText(content);
            crTime.setText(createTime);
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            imageAdapter = new ImageAdapter(this, urlList);
            recyclerView.setAdapter(imageAdapter);
            imageAdapter.setItemClickListener(new onItemClickListener() {
                @Override
                public void itemClick(final int position) {
                    LayoutInflater inflater = LayoutInflater.from(mContext);
                    imgEntryView = inflater.inflate(R.layout.dialog_photo, null);
                    // 加载自定义的布局文件
                    final AlertDialog dialog = new AlertDialog.Builder(ReadedLookActivity.this).create();
                    img = imgEntryView.findViewById(R.id.large_image);
                            Glide.with(getApplicationContext())
                                    .load(urlList.get(position))
                                    .into( img);
                            dialog.setView(imgEntryView); // 自定义dialog
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            dialog.show();

                    // 点击大图关闭dialog
                    imgEntryView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View paramView) {
                            dialog.cancel();
                        }
                    });
                }
            } );
        }

        public void onBack(View v){ super.onBackPressed(); }
}
