package com.baidu.track.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.track.R;
import com.baidu.track.api.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.track.ui.activity.MainActivity.BASE_URL;

public class DealActivity extends AppCompatActivity implements View.OnClickListener {


   private String editContent,editTitle;

    private Spinner spinner;
    private ListView lv;

   private Button progress;

    private LinearLayout options;
    private TextView title,typeView;

    private List<String> data_list,item_list,subject_list,info_list,sum_list;
    private List<String> selectlist = new ArrayList<>();
    private List<String> sublist = new ArrayList<>();
    private ArrayAdapter<String> arr_adapter;
    private String url = BASE_URL+"/api/categories";
    private String result;

    //重点二：设置单选与多选框的样式
    //☆☆☆:创建adapter,布局加载系统默认的单选框
    private ArrayAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_deal);

        options = findViewById(R.id.btn_activity_options);
        options.setVisibility(View.INVISIBLE);
        title = findViewById(R.id.tv_activity_title);
        title.setText("事件处理");

        spinner = findViewById(R.id.Spinner01);
        lv = findViewById(R.id.lv_single);
        typeView = findViewById(R.id.ResponsibilityType);
        progress = findViewById(R.id.progress);
        //数据
        data_list = new ArrayList<String>();
        item_list = new ArrayList<>();
        info_list = new ArrayList<>();
        subject_list = new ArrayList<>();
        sum_list = new ArrayList<>();
        try{
            JSONParser jsonParser = new JSONParser();
            result = jsonParser.sendState(url,jsonParser.getInfo(this));
            JSONObject allobj = new JSONObject(result);

            if( allobj.optString("message").equals("Unauthenticated.")){
                JSONParser.changeToken(this);
                result = jsonParser.sendState(url,jsonParser.getInfo(this));
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
            }
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(this,"请先去登录",Toast.LENGTH_LONG).show();
            JSONParser.changeStatu(this);
        }

        adapter = new ArrayAdapter(this,R.layout.array_adapter);
        //适配器
        arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(arr_adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                // 将所选mySpinner 的值带入myTextView 中
                editTitle = data_list.get(arg2);
                selectlist.clear();
                adapter.clear();
                sublist.clear();
                String info = info_list.get(arg2);
                String subject1 = sum_list.get(arg2);

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

                //重点一：设置选择模式
                //☆☆☆:通过listView设置单选模式
                lv.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
                //将模拟数据添加到adapter适配器中
                adapter.addAll(selectlist);
                lv.setAdapter(adapter);
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        lv.setDivider(new ColorDrawable(Color.BLACK));
        lv.setDividerHeight(2);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editContent = selectlist.get(position);
                String copy = sublist.get(position);
                if(copy.equals("0")){
                    typeView.setText("配合责任");
                }else{
                    typeView.setText("主体责任");
                }
            }
        });
        progress.setOnClickListener(this);
    }

    @Override
    public void onClick (View v){
        switch (v.getId()) {
            case R.id.progress:

                int checkedItemPosition = lv.getCheckedItemPosition();
                if (checkedItemPosition == -1) {
                    // //-1未选择选项
                    Toast.makeText(this, "请选择所属分类具体事项", Toast.LENGTH_SHORT).show();
                } else {
                    String checked = sublist.get(checkedItemPosition);
                    Bundle bundle = new Bundle();
                    //将责任类型传到DealWidthActivity
                    bundle.putString("title", editTitle);
                    bundle.putString("content", editContent);
                    bundle.putString("subject", checked);
                    bundle.putString("tagname", "DealFragment");
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    intent.setClass(this, HandleActivity.class);
                    startActivity(intent);
                    finish();
                }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    /**
     * 回退事件
     */
    public void onBack(View v) {
        super.onBackPressed();
    }
}
