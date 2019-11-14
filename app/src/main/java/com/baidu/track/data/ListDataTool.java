package com.baidu.track.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.baidu.track.api.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.baidu.track.ui.activity.MainActivity.BASE_URL;

/**
 * 从网络读取数据的工具类用于展示在lookActivity
 */

public class ListDataTool {
    private Context mContext;
    private List<Task> data;

    //所有任务数据
    private String str ;

    boolean cancel = false;

    //存放多张图片
    private ArrayList<String> imgList;

    public ListDataTool(Context mContext) {
        this.mContext = mContext;
        data = new ArrayList<>();

    }

    public void getData( final int start,final String url, final NetCallback callback) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cancel) return;

                if (start>2){
                    callback.fail("没有更多数据了");
                    return;
                }
                else {
                    if (data==null){
                        data = new ArrayList<>();
                    }else{
                        data.clear();
                    }

                }
                //任务设置详细内容
                try{
                    JSONParser jsonParser = new JSONParser();
                    str = jsonParser.sendState(url,jsonParser.getInfo(mContext));
                    Log.i("token",jsonParser.getInfo(mContext));
                    JSONObject alljson = new JSONObject(str);
                    if(alljson.optString("message").equals("Unauthenticated.")){
                        JSONParser.changeToken(mContext);
                        str = jsonParser.sendState(url,jsonParser.getInfo(mContext));
                        JSONObject newJson = new JSONObject(str);
                        data = getData(newJson);
                    }else{
                        data = getData(alljson);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(mContext,"请先去登录",Toast.LENGTH_LONG).show();
                    SharedPreferences sp=mContext.getSharedPreferences("loginInfo", MODE_PRIVATE);
                    //获取编辑器
                    SharedPreferences.Editor editor=sp.edit();
                    //存入boolean类型的登录状态
                    editor.putBoolean("isLogin", false);
                    editor.putString("access_token", null);
                    //提交修改
                    editor.commit();
                }
                callback.success(data);

            }
        }, 1000);

    }

    public void getReport( final int start,final String url, final NetCallback callback) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cancel) return;

                if (start>2){
                    callback.fail("没有更多数据了");
                    return;
                }
                else {
                    if (data==null){
                        data = new ArrayList<>();
                    }else{
                        data.clear();
                    }

                }
                //任务设置详细内容
                try{
                    JSONParser jsonParser = new JSONParser();
                    str = jsonParser.sendState(url,jsonParser.getInfo(mContext));
                    Log.i("token",jsonParser.getInfo(mContext));
                    JSONObject alljson = new JSONObject(str);
                    if(alljson.optString("message").equals("Unauthenticated.")){
                        JSONParser.changeToken(mContext);
                        str = jsonParser.sendState(url,jsonParser.getInfo(mContext));
                        JSONObject newJson = new JSONObject(str);
                        data = getReport(newJson);
                    }else{
                        data = getReport(alljson);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    Toast.makeText(mContext,"请先去登录",Toast.LENGTH_LONG).show();
                    SharedPreferences sp=mContext.getSharedPreferences("loginInfo", MODE_PRIVATE);
                    //获取编辑器
                    SharedPreferences.Editor editor=sp.edit();
                    //存入boolean类型的登录状态
                    editor.putBoolean("isLogin", false);
                    editor.putString("access_token", null);
                    //提交修改
                    editor.commit();
                }
                callback.success(data);

            }
        }, 1000);

    }

    public List<Task> getData(final JSONObject alljson) {
        List<Task> data = new ArrayList<>();

        try{
            String  datas= alljson.getString("data");
            JSONArray taskArray = new JSONArray(datas);
            for (int j = 0;j<taskArray.length();j++){
                imgList = new ArrayList<>();
                String object = taskArray.get(j).toString();
                JSONObject jsonObject = new JSONObject(object);
                Task task = new Task();
                task.settNo(jsonObject.getString("id"));//编号
                task.setTaskName(jsonObject.getString("title"));//标题
                task.setContent(jsonObject.getString("content"));//内容
                task.setTime(jsonObject.getString("created_at"));//发现时间
                task.setLastTime(jsonObject.getString("time_limit"));//办结时限
                task.setAddress(jsonObject.getString("address"));//巡查地址
                String pivot = jsonObject.getString("pivot");
                JSONObject pivotObject = new JSONObject(pivot);
                task.setState(pivotObject.getString("status"));//状态

                imgList.add(BASE_URL+pivotObject.getString("see_image"));
                String images = pivotObject.getString("see_images");
                String[] strarray=images.split("[;]");
                for (int i = 0; i < strarray.length; i++){
                    imgList.add(BASE_URL+strarray[i]);
                }
                task.setImgUrl(imgList);//图片（两个字段）
                task.setInformation(pivotObject.getString("information"));//处理意见
                data.add(task);
            }
        }catch (JSONException e ){
            e.printStackTrace();
        }
        return data;
    }


    public List<Task> getReport(final JSONObject alljson) {
        List<Task> data = new ArrayList<>();

        try{
            String  datas= alljson.getString("data");
            JSONArray taskArray = new JSONArray(datas);
            for (int j = 0;j<taskArray.length();j++){
                imgList = new ArrayList<>();
                String object = taskArray.get(j).toString();
                JSONObject jsonObject = new JSONObject(object);
                Task task = new Task();
                task.settNo(jsonObject.getString("id"));//编号
                task.setTaskName(jsonObject.getString("title"));//标题
                task.setContent(jsonObject.getString("content"));//内容
                task.setState(jsonObject.getString("status"));//状态
                task.setTime(jsonObject.getString("created_at"));//发现时间
                imgList.add(BASE_URL+jsonObject.getString("image"));
                String images = jsonObject.getString("images");
                String[] strarray=images.split("[;]");
                for (int i = 0; i < strarray.length; i++){
                    imgList.add(BASE_URL+strarray[i]);
                }
                task.setImgUrl(imgList);//图片（两个字段）
                task.setInformation(jsonObject.getString("suggest"));//处理意见
                data.add(task);
            }
        }catch (JSONException e ){
            e.printStackTrace();
        }
        return data;
    }

    public interface NetCallback {
        void success(List<Task> response);

        void fail(String errMsg);

    }

}
