package com.baidu.track.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * 任务清单列表
 */
public class LookActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private Context mContext;
    private ListDataTool tool;
    private List<Task> datas;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swiper;
    private TaskAdapter adapter;
    private String url = BASE_URL +"/api/userHasMatters";
    private LinearLayout options;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_look);
        mContext = this;
        swiper =findViewById(R.id.swiper);
        swiper.setOnRefreshListener(this);
        swiper.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_green_light);

            options = findViewById(R.id.btn_activity_options);
            options.setVisibility(View.INVISIBLE);
            title = findViewById(R.id.tv_activity_title);
            title.setText("任务清单");

        datas = new ArrayList<>();
        adapter = new TaskAdapter(mContext,datas);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);

        tool = new ListDataTool(this);
        adapter.setItemClickListener(new onItemClickListener() {
            @Override
            public void itemClick(int position) {
                Bundle bundle = new Bundle();
                bundle.putString("title", datas.get(position).getTaskName());
                bundle.putString("id", datas.get(position).gettNo());
                bundle.putString("content", datas.get(position).getContent());
                bundle.putString("createTime", datas.get(position).getTime());
                bundle.putString("lastTime", datas.get(position).getLastTime());
                bundle.putString("address", datas.get(position).getAddress());
                bundle.putString("tagname", "lessonfragment");
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(mContext, TaskActivity.class);
                startActivity(intent);
                finish();
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

    @Override
    public void onBackPressed(){
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
     /**
     * 回退事件
     */
    public void onBack(View v) {
        super.onBackPressed();
    }

}
