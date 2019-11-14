package com.baidu.track.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.track.R;
import com.baidu.track.data.ListDataTool;
import com.baidu.track.data.Task;
import com.baidu.track.ui.custom.JKRecyclerView.TaskAdapter;
import com.baidu.track.ui.custom.JKRecyclerView.onItemClickListener;
import com.baidu.track.ui.custom.JKRecyclerView.onSwipeMenuListener;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.track.ui.activity.MainActivity.BASE_URL;

/**
 * 点击我的任务显示所有任务
 */
public class AllTaskActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener {
    private Context mContext;
    private ListDataTool tool;
    private List<Task> datas;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private TaskAdapter adapter;
    private LinearLayout options;
    private TextView title;

    private String url = BASE_URL+"/api/userMatters";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look);

        mContext = this;
        options = findViewById(R.id.btn_activity_options);
        options.setVisibility(View.INVISIBLE);
        title = findViewById(R.id.tv_activity_title);
        title.setText("任务清单");
        //初始化刷新插件
        swiper =  findViewById(R.id.swiper);
        swiper.setOnRefreshListener(this);
        swiper.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_green_light);

        datas = new ArrayList<>();
        adapter = new TaskAdapter(mContext,datas);
        //初始化RecyclerView插件
        recyclerView =  findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        tool = new ListDataTool(this);
        adapter.setItemClickListener(new onItemClickListener() {
            @Override
            public void itemClick(int position) {
                Bundle bundle = new Bundle();
                bundle.putString("id", datas.get(position).gettNo());
                bundle.putString("title",datas.get(position).getTaskName());//标题
                bundle.putString("content", datas.get(position).getContent());//内容
                bundle.putStringArrayList("imgurls", datas.get(position).getImgUrl());//图片（两个属性）
                bundle.putString("createTime", datas.get(position).getTime());//发现时间
                bundle.putString("lastTime", datas.get(position).getLastTime());//办结时限
                bundle.putString("address", datas.get(position).getAddress());//地址
                bundle.putString("information",datas.get(position).getInformation());//处理意见
                bundle.putString("tagname", "AllTaskActivity");

                if(datas.get(position).getState().equals("0") || datas.get(position).getState().equals("3")){
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(mContext, TaskActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    //跳转ReadedLookActivity
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(mContext, ReadedLookActivity.class);
                    startActivity(intent);
                }

            }
        } );
        adapter.setSwipeMenuListener(new onSwipeMenuListener() {
            @Override
            public void open() {

            }

            @Override
            public void close() {

            }
        });

        tool.getData( 1,url, new ListDataTool.NetCallback() {
            @Override
            public void success(List<Task> response) {
                datas = response;
                adapter.updateData(datas);
            }

            @Override
            public void fail(String errMsg) {

            }
        });
    }

    @Override
    public void onRefresh() {
        tool.getData( 1, url,new ListDataTool.NetCallback() {
            @Override
            public void success(List<Task> response) {
                datas = response;
                adapter.updateData(datas);
                swiper.setRefreshing(false);
            }

            @Override
            public void fail(String errMsg) {

            }
        });
    }
    /**
     * 回退事件
     */
    public void onBack(View v) {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}