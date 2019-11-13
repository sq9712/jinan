package com.baidu.track.ui.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.track.R;
import com.baidu.track.api.JSONParser;
import com.baidu.track.ui.custom.BackDelEditText;
import com.baidu.track.utils.NetUtil;
import com.dd.processbutton.iml.ActionProcessButton;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;

import static com.baidu.track.ui.activity.MainActivity.BASE_URL;

/**
 * 登录界面
 */
public class LoginActivity extends AppCompatActivity {
    @BindView(R.id.input_username)
    BackDelEditText inputUsername;
    @BindView(R.id.input_password)
    BackDelEditText inputPassword;
    @BindView(R.id.btnSignIn)
    ActionProcessButton btnSignIn;

    //用户登录状态
    static String statu;
    private Button canbtn;

    private Context mContext;
    private String userName,userPassword,datas,token,user,name;
    private String url = BASE_URL+"/api/authorizations";//判断登录信息是否正确的接口地址

    //接口返回信息
    private String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mContext = this;
        //BackDelEditText 是一个自定义控件，实现文本框内有文字时，出现删除文字的小图标
        inputUsername.setDrawableBack(R.mipmap.icon_user_phone, R.drawable.icon_deltext);
        inputPassword.setDrawableBack(R.mipmap.icon_user_password, R.drawable.icon_deltext);
        canbtn = findViewById(R.id.cancel_button);
        canbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
            }
        });


        if(NetUtil.isNetworkAvailable(mContext)){
            Toast.makeText(mContext,"请检查网络是否打开",Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btnSignIn)
    public void onViewClicked() {
        userName = inputUsername.getText().toString();
        userPassword = inputPassword.getText().toString();
        String str = sendState(userName,userPassword);

        // TextUtils.isEmpty
        if(TextUtils.isEmpty(userName)){
            Toast.makeText(LoginActivity.this, "请输入用户名", Toast.LENGTH_SHORT).show();
            return;
        }else if(TextUtils.isEmpty(userPassword)){
            Toast.makeText(LoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
            return;
            //向接口提交用户信息获取返回信息
        }else {
            try{
                JSONObject json = new JSONObject(str);
                statu = json.optString("status");
                datas = json.getString("data");
                JSONObject object = new JSONObject(datas);
                token = object.getString("access_token");
                //获取用户名
                user = object.getString("user");
                JSONObject userJson = new JSONObject(user);
                name = userJson.getString("name");
            }catch (JSONException e) {
                e.printStackTrace();
            }
            if(statu.equals("success")){
                //一致登录成功
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                //根据权限分配内容

                //保存登录状态，在界面保存登录的用户名 定义个方法 saveLoginStatus boolean 状态 , userName 用户名;
                saveLoginStatus(true, userName,token,name);
                //登录成功后关闭此页面进入主页
                Intent data=new Intent();
                data.putExtra("isLogin",true);
                //RESULT_OK为Activity系统常量，状态码为-1
                // 表示此页面下的内容操作成功将data返回到上一页面，如果是用back返回过去的则不存在用setResult传递data值
                setResult(RESULT_OK,data);
                //销毁登录界面
                LoginActivity.this.finish();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                return;
            }else{
                Toast.makeText(LoginActivity.this, "用户名或密码错误,请检查用户名或密码", Toast.LENGTH_SHORT).show();
                JSONParser.changeStatu(mContext);
                return;
            }
        }

    }

    public String sendState(String userName,String userPassword){
        //服务器操作
        List<NameValuePair> params = new ArrayList<>();
        //传递序用户名，密码,RegId,entity_name别名
        params.add(new BasicNameValuePair("phone", userName));
        params.add(new BasicNameValuePair("password", userPassword));
        String rid = JPushInterface.getRegistrationID(getApplicationContext());
        params.add(new BasicNameValuePair("reg_id", rid));
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        params.add(new BasicNameValuePair("entity_name", androidId));
        JSONParser jsonParser = new JSONParser();
        try{
            result = jsonParser.makeHttpRequest(url,"POST", params);
        }catch(Exception e){
            e.printStackTrace();
            Looper.prepare();
            Toast.makeText(LoginActivity.this, "文件解析错误！", Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
        return result;
    }


    /**
     *保存登录状态和登录用户名到SharedPreferences中
     */
    private void saveLoginStatus(boolean status,String userName,String token,String name){
        //loginInfo表示文件名  SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        //获取编辑器
        SharedPreferences.Editor editor=sp.edit();
        //存入boolean类型的登录状态
        editor.putBoolean("isLogin", status);
        editor.putString("loginUserName", userName);
        editor.putString("access_token", token);
        editor.putString("name", name);
        //提交修改
        editor.commit();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onStart() {
        super.onStart();
        // 适配android M，检查权限
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
        }
    }



    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String s = intent.getAction();

            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Toast.makeText(LoginActivity.this,"apikey验证失败，地图功能无法正常使用",Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Toast.makeText(LoginActivity.this,"apikey验证成功",Toast.LENGTH_SHORT).show();
            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Toast.makeText(LoginActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isNeedRequestPermissions(List<String> permissions) {
        // 定位精确位置
        addPermission(permissions, Manifest.permission.ACCESS_FINE_LOCATION);
        // 存储权限
        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // 读取手机状态
        addPermission(permissions, Manifest.permission.READ_PHONE_STATE);
        return permissions.size() > 0;
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }

}
