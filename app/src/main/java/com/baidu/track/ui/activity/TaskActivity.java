package com.baidu.track.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.track.R;
import com.baidu.track.api.JSONParser;
import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.baidu.track.ui.activity.MainActivity.BASE_URL;


/**
 * 任务详情类别界面
 */
public class TaskActivity extends AppCompatActivity {
    @BindView(R.id.progress)
    Button progress;

    private Context mContext;
    private TextView view,typeView,contentView,ctimeView,ltimeView,addressView,titleView;
    private Spinner spinner;
    private ListView lv;
    private List<String> data_list,item_list,subject_list,info_list,sum_list;
    private List<String> selectlist = new ArrayList<>();
    private List<String> sublist = new ArrayList<>();
    private ArrayAdapter<String> arr_adapter;
    private String url = BASE_URL+"/api/categories";
    private String taskTitle,id,content,createTime,lastTime,address,result,information,tagname;

    private ArrayList<String> urlList  = new ArrayList<>();

    //重点二：设置单选与多选框的样式
    //☆☆☆:创建adapter,布局加载系统默认的单选框
    private ArrayAdapter adapter;

    private LinearLayout options;
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_task);
        mContext= this;
        ButterKnife.bind(this);



        options = findViewById(R.id.btn_activity_options);
        options.setVisibility(View.INVISIBLE);
        title = findViewById(R.id.tv_activity_title);
        title.setText("事件处理");

        Bundle bundle=getIntent().getExtras();
        taskTitle = bundle.getString("title");
        id=bundle.getString("id");
        createTime = bundle.getString("createTime");
        lastTime = bundle.getString("lastTime");
        address = bundle.getString("address");
        content = bundle.getString("content");
        information = bundle.getString("information");
        urlList = bundle.getStringArrayList("imgurls");
        tagname = bundle.getString("tagname");
        contentView = findViewById(R.id.Details);
        contentView.setMovementMethod(ScrollingMovementMethod.getInstance());

        ctimeView = findViewById(R.id.Time);
        ltimeView = findViewById(R.id.lastTime);
        addressView = findViewById(R.id.Place);
        titleView = findViewById(R.id.Title);
        titleView.setText(taskTitle);
        ctimeView.setText(createTime);
        ltimeView.setText(lastTime);
        contentView.setText(content);
         if(address.equals("null")){
             addressView.setText("空");
         }else{
             addressView.setText(address);
         }
        view =  findViewById(R.id.spinnerText);
        spinner = findViewById(R.id.Spinner01);
        lv = findViewById(R.id.lv_single);
        typeView = findViewById(R.id.ResponsibilityType);

        //数据
        data_list = new ArrayList<>();
        item_list = new ArrayList<>();
        info_list = new ArrayList<>();
        subject_list = new ArrayList<>();
        sum_list = new ArrayList<>();

        try{
            JSONParser jsonParser = new JSONParser();
            result = jsonParser.sendState(url,jsonParser.getInfo(this));
            JSONObject allobj = new JSONObject(result);
            if( allobj.optString("message").equals("Unauthenticated.")){
                JSONParser.changeToken(mContext);
                result = jsonParser.sendState(url,jsonParser.getInfo(mContext));
                JSONObject newJson = new JSONObject(result);
                getJson(newJson);
            }else{
                getJson(allobj);
            }
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(mContext,"请先去登录",Toast.LENGTH_LONG).show();
            JSONParser.changeStatu(mContext);
        }

        adapter = new ArrayAdapter(mContext,R.layout.array_adapter);
        //适配器
        arr_adapter= new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(arr_adapter);
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {//选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                // TODO Auto-generated method stub
                // 将所选mySpinner 的值带入myTextView 中
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
                view.setText("Nothing");
            }
        });
        lv.setDivider(new ColorDrawable(Color.BLACK));
        lv.setDividerHeight(2);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String copy = sublist.get(position);
                if(copy.equals("0")){
                    typeView.setText("配合责任");
                }else{
                    typeView.setText("主体责任");
                }
            }
        });

    }

    @OnClick(R.id.progress)
    void OnClickProgress(){
        int checkedItemPosition = lv.getCheckedItemPosition();
        //-1未选择选项
        if(checkedItemPosition==-1)
        {
            Toast.makeText(mContext, "请选择所属分类具体事项", Toast.LENGTH_SHORT).show();
        }else{
            //获取到对应条目的position后，我们可以do something....
            String checked = sublist.get(checkedItemPosition);
            Bundle bundle = new Bundle();
            //将责任类型传到DetailsActivity
            bundle.putString("id", id);
            bundle.putString("subject", checked);
            bundle.putString("taskTitle", taskTitle);
            bundle.putString("content", content);
            bundle.putString("address", address);
            bundle.putString("information",information);
            bundle.putStringArrayList("imgurls", urlList);
            bundle.putString("tagname", "TaskActivity");
            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(this, HandleActivity.class);
            startActivity(intent);
            finish();
        }
    }
    /**
     * 回退事件
     */
    public void onBack(View v) {
        if (tagname.equals("LookActivity")){
            startActivity(new Intent(this,LookActivity.class));
            finish();
        }else{
            startActivity(new Intent(this,AllTaskActivity.class));
            finish();
        }

    }

    public void getJson(JSONObject jsonObject){
        try{
            String data = jsonObject.getString("categories");
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
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

}
