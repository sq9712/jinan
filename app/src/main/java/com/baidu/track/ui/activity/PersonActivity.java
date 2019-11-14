package com.baidu.track.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.track.R;
import com.baidu.track.TrackApplication;
import com.baidu.track.activity.TestActivity;
import com.baidu.track.api.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.baidu.track.ui.activity.MainActivity.BASE_URL;

public class PersonActivity extends Activity implements View.OnClickListener {
    private Button loginBtn;

    private TextView workload,username,distanceView;
    private Boolean bool;

    private  NavigationView personNav;

    private String url = BASE_URL+"/api/userMatters";

    private LinearLayout options;
    private TextView title;

    private SharedPreferences sp;
    private String msg;
    private int distance;
    private TrackApplication trackApp = null;
    private long timeInMillis;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person_head_center);
        personNav = findViewById(R.id.person_navigation );
        username = findViewById(R.id.tv_user_name);
        loginBtn = findViewById(R.id.login_btn);
        workload =  findViewById(R.id.workload);
        distanceView = findViewById(R.id.distance);
        options = findViewById(R.id.btn_activity_options);
        options.setVisibility(View.INVISIBLE);
        title = findViewById(R.id.tv_activity_title);
        title.setText("个人中心");

        sp=getSharedPreferences("loginInfo", MODE_PRIVATE);

        //设置菜单图标颜色为原色
        personNav.setItemIconTintList(null);

        personNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.person_menu_2:
                        Intent intent = new Intent(PersonActivity.this, AllTaskActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.person_menu_4:
                        startActivity(new Intent(PersonActivity.this, TestActivity.class));
                        break;
                    case R.id.person_menu_5:
                        startActivity(new Intent(PersonActivity.this, MineTaskActivity.class));
                        break;
                }
                return false;
            }
        });
        loginBtn.setOnClickListener(this);
    }

    public int getDistance(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss +0800");
        SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy年MM月dd日");
        Date curDate = new Date(System.currentTimeMillis());
        //获取当前时
        String str = formatter.format(curDate);
        //获取当前日期
        String str1 = formatter1.format(curDate);
        //GET请求
        String gettrackUrl = "http://yingyan.baidu.com/api/v3/track/gettrack";
        trackApp = (TrackApplication)getApplicationContext();
        ApplicationInfo appInfo;
        try {
            appInfo =getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            //ak
            msg = appInfo.metaData.getString("com.baidu.lbsapi.API_KEY");
        } catch (PackageManager.NameNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Map<String,String> map=new HashMap<>();
        map.put("ak", msg);
        map.put("service_id", String.valueOf(trackApp.serviceId));
        map.put("entity_name", String.valueOf(trackApp.entityName));
        map.put("mcode","19:D1:85:60:FA:F4:17:24:CD:D7:10:47:64:6E:52:3D:A7:7D:7F:17;com.baidu.track");
        map.put("start_time", String.valueOf(gettimeInMillis(str1+" 00:00:00 +0800")/1000));
        map.put("end_time", String.valueOf(gettimeInMillis(str)/1000));
        map.put("is_processed","0");


        JSONParser jsonParser = new JSONParser();
        String result = jsonParser.sendGETRequest(gettrackUrl,map,"UTF-8");
        try{
            JSONObject object = new JSONObject(result);
            distance = object.getInt("distance");
        }catch (JSONException e){
            e.printStackTrace();
        }
        return distance;
    }

    public long gettimeInMillis(String str){
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss +0800", Locale.getDefault()).parse(str));
            timeInMillis = calendar.getTimeInMillis();
        }catch (ParseException e){
            e.printStackTrace();
        }
        return timeInMillis;
    }

    @Override
    public void onClick (View v){
        switch (v.getId()) {
         case R.id.login_btn:
             //loginInfo表示文件名  SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
            SharedPreferences sp = getSharedPreferences("loginInfo", MODE_PRIVATE);
            //获取编辑器
            SharedPreferences.Editor editor = sp.edit();
            //存入boolean类型的登录状态
            editor.putBoolean("isLogin", false);
            editor.putString("access_token", null);
            editor.commit();
            startActivity(new Intent(PersonActivity.this, LoginActivity.class));
            finish();
        }
    }


    public int init(final String url){
        //任务设置详细内容
        try{
            JSONParser jsonParser = new JSONParser();
            String str = jsonParser.sendState(url,jsonParser.getInfo(this));
            Log.i("token",jsonParser.getInfo(this));
            JSONObject alljson = new JSONObject(str);
            if(alljson.optString("message").equals("Unauthenticated.")){
                JSONParser.changeToken(this);
                str = jsonParser.sendState(url,jsonParser.getInfo(this));
                JSONObject newJson = new JSONObject(str);
                String  data= newJson.getString("data");
                JSONArray taskArray = new JSONArray(data);
                return taskArray.length();
            }else{
                String  datas= alljson.getString("data");
                JSONArray taskArrays = new JSONArray(datas);
                return taskArrays.length();
            }
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(this,"请先去登录",Toast.LENGTH_LONG).show();
            SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
            //获取编辑器
            SharedPreferences.Editor editor=sp.edit();
            //存入boolean类型的登录状态
            editor.putBoolean("isLogin", false);
            editor.putString("access_token", null);
            //提交修改
            editor.commit();
        }
        return 0;
    }


    @Override
    public void onStart() {
        bool = sp.getBoolean("isLogin",true);
        if(bool){
            username.setText(sp.getString("name",""));
            //显示任务量，巡查里程，出勤情况
            workload.setText(String.valueOf(init(url)));
            distanceView.setText(String.valueOf(getDistance()));
            loginBtn.setText("退出登录");
        }else{
            username.setText("未登录");
            loginBtn.setText("登录");
        }
        super.onStart();
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
