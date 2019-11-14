package com.baidu.track.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.track.R;
import com.baidu.track.ui.custom.JKRecyclerView.ListAdapter;
import com.baidu.track.ui.custom.JKRecyclerView.onItemClickListener;

import java.util.ArrayList;
import java.util.List;


/**
 * 法规手册根据责任清单分类
 */
public class RuleActivity extends Activity {

    private Context mContext;
    private List<String> alllist;

    private RecyclerView recyclerView;
    private ListAdapter recycleAdapter;
    private LinearLayout options;
    private TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);
        mContext = this;
        recyclerView =  findViewById(R.id.recycler);

        options = findViewById(R.id.btn_activity_options);
        options.setVisibility(View.INVISIBLE);
        title = findViewById(R.id.tv_activity_title);
        title.setText("法规手册");
        alllist = new ArrayList<>();
        alllist.add("责任清单");
        alllist.add("治理标准");

        recycleAdapter= new ListAdapter(mContext , alllist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        recyclerView.setAdapter( recycleAdapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recycleAdapter.setItemClickListener(new onItemClickListener() {
            @Override
            public void itemClick(int position) {
                if(alllist.get(position).equals("治理标准")){
                    startActivity(new Intent(mContext,StandardActivity.class));
                }else {
                    startActivity(new Intent(mContext,RespondActivity.class));
                }
            }
        });
    }

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
