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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MattersActivity extends Activity {

    private Context mContext;

    private RecyclerView recyclerView;
    private ListAdapter recycleAdapter;
    private LinearLayout options;
    private TextView title;

    private String info,subject1;
    private List<String> selectlist,sublist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);
        mContext = this;

        selectlist = new ArrayList<>();
        sublist = new ArrayList<>();

        //初始化控件
        recyclerView = findViewById(R.id.recycler);
        options = findViewById(R.id.btn_activity_options);
        options.setVisibility(View.INVISIBLE);
        title = findViewById(R.id.tv_activity_title);
        title.setText("责任清单");

        Bundle bundle=getIntent().getExtras();
        info = bundle.getString("info_list");
        subject1=bundle.getString("sum_list");
        try {
            JSONArray infoarr = new JSONArray(info);
            JSONArray subarr = new JSONArray(subject1);
            for (int i = 0; i < infoarr.length(); i++) {
                selectlist.add(infoarr.getString(i));
                sublist.add(subarr.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        recycleAdapter = new ListAdapter(mContext, selectlist);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        recyclerView.setLayoutManager(layoutManager);
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置Adapter
        recyclerView.setAdapter(recycleAdapter);
        //设置增加或删除条目的动画
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recycleAdapter.setItemClickListener(new onItemClickListener() {
            @Override
            public void itemClick(int position) {
                if (sublist.get(position).equals("null")) {

                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString("sublist", sublist.get(position));
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(mContext, LegalActivity.class);
                    startActivity(intent);
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