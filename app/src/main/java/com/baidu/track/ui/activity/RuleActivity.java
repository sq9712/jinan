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
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.track.R;
import com.baidu.track.api.JSONParser;
import com.baidu.track.ui.custom.JKRecyclerView.ListAdapter;
import com.baidu.track.ui.custom.JKRecyclerView.onItemClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.track.ui.activity.MainActivity.BASE_URL;


/**
 * 法规手册根据责任清单分类
 */
public class RuleActivity extends Activity {

    private Context mContext;
    private List<String> data_list,item_list,subject_list,info_list,sum_list,selectlist,sublist,legalList;
    private String url = BASE_URL+"/api/categories";
    private String result;


    private RecyclerView recyclerView;
    private ListAdapter recycleAdapter;
    private LinearLayout options;
    private TextView title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule);
        mContext = this;
        legalList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recycler);

//        recyclerView.addItemDecoration(new SpacesItemDecoration(0,40));

        options = findViewById(R.id.btn_activity_options);
        options.setVisibility(View.INVISIBLE);
        title = findViewById(R.id.tv_activity_title);
        title.setText("法规手册");

        initData();
        recycleAdapter= new ListAdapter(mContext , data_list );
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
                String info = info_list.get(position);
                String subject1 = sum_list.get(position);
                try{
                    JSONArray infoarr = new JSONArray(info);
                    JSONArray subarr = new JSONArray(subject1);
                    for(int i=0;i<infoarr.length();i++){
                        selectlist.add(infoarr.getString(i));
                        sublist.add(subarr.getString(i));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                recycleAdapter.updateData(selectlist);
                recycleAdapter.setItemClickListener(new onItemClickListener() {
                    @Override
                    public void itemClick(int position) {
                        legalList.clear();
                        String legal = sublist.get(position);
                        String [] result = legal.split("《");
                        //解析获取的法规字符串并从第二项开始（第一项未空）
                        for(int a = 1;a<result.length;a++){
                            legalList.add("《"+result[a]);
                        }
                        Log.i("legal_doc",String.valueOf(legalList));

                        recycleAdapter.updateData(legalList);
                        recycleAdapter.setItemClickListener(new onItemClickListener() {
                            @Override
                            public void itemClick(int position) {
                                Bundle bundle = new Bundle();
                                bundle.putString("legal",legalList.get(position));
                                Intent intent = new Intent();
                                intent.putExtras(bundle);
                                intent.setClass(mContext, RuleDetailActivity.class);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }
        } );
    }

    /**
     * 获取责任清单类别
     */
    private void initData() {
        data_list = new ArrayList<>();
        item_list = new ArrayList<>();
        info_list = new ArrayList<>();
        subject_list = new ArrayList<>();
        sum_list = new ArrayList<>();
        selectlist = new ArrayList<>();
        sublist = new ArrayList<>();
        try{
            JSONParser jsonParser = new JSONParser();
            result = jsonParser.sendState(url,jsonParser.getInfo(this));
            Log.i("Token1",jsonParser.getInfo(this));
            Log.i("ruleresult",result);
            JSONObject allobj = new JSONObject(result);

            if( allobj.optString("message").equals("Unauthenticated.")){
                JSONParser.changeToken(mContext);
                result = jsonParser.sendState(url,jsonParser.getInfo(mContext));
                JSONObject newJson = new JSONObject(result);
                String data = newJson.getString("data");
                JSONArray allarr = new JSONArray(data);
                for (int i=0;i<allarr.length();i++){
                    String str = allarr.getString(i);
                    JSONObject object = new JSONObject(str);
                    String name = object.getString("name");
                    data_list.add(name);
                    String responsibilities = object.getString("responsibilities");
                    JSONArray arrrespon = new JSONArray(responsibilities);
                    for(int j= 0;j<arrrespon.length();j++){
                        String category = arrrespon.getString(j);
                        JSONObject object1 = new JSONObject(category);
                        String item= object1.getString("item");
                        String subject= object1.getString("subject_duty");//0是部门，1是街道
                        Log.i("subject",subject);
                        subject_list.add(subject);
                        item_list.add(item);
                    }
                    info_list.add(item_list.toString());
                    sum_list.add(subject_list.toString());
                    subject_list.clear();
                    item_list.clear();
                    Log.i("info_list",info_list.toString());
                    Log.i("sum_list",sum_list.toString());
                }
            }else{
                String data = allobj.getString("data");
                JSONArray allarr = new JSONArray(data);
                for (int i=0;i<allarr.length();i++){
                    String str = allarr.getString(i);
                    JSONObject object = new JSONObject(str);
                    String name = object.getString("name");
                    data_list.add(name);
                    String responsibilities = object.getString("responsibilities");
                    JSONArray arrrespon = new JSONArray(responsibilities);
                    for(int j= 0;j<arrrespon.length();j++){
                        String category = arrrespon.getString(j);
                        JSONObject object1 = new JSONObject(category);
                        String item= object1.getString("item");
                        String subject= object1.getString("legal_doc");//法规依据
                        subject_list.add(subject);
                        item_list.add(item);
                    }
                    info_list.add(item_list.toString());
                    sum_list.add(subject_list.toString());
                    subject_list.clear();
                    item_list.clear();
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(mContext,"请先去登录",Toast.LENGTH_LONG).show();
            JSONParser.changeStatu(mContext);
        }
    }

    /**
     * 回退事件
     */
    public void onBack(View v) {
        super.onBackPressed();
    }
}
