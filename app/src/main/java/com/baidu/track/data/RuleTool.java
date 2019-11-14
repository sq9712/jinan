package com.baidu.track.data;

import android.content.Context;
import android.widget.Toast;

import com.baidu.track.api.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.track.ui.activity.MainActivity.BASE_URL;

public class RuleTool {
    private Context mContext;
    private List<String> data_list,item_list,subject_list,info_list,sum_list,categorylist,standardlist;
    private String url = BASE_URL+"/api/categories";
    private String result;

    public RuleTool(Context mContext) {
        this.mContext = mContext;
        data_list = new ArrayList<>();
        item_list = new ArrayList<>();
        info_list = new ArrayList<>();
        subject_list = new ArrayList<>();
        sum_list = new ArrayList<>();
        categorylist = new ArrayList<>();
        standardlist = new ArrayList<>();
        initData();
    }
    /**
     * 获取责任清单类别
     */
    private void initData() {

        try{
            JSONParser jsonParser = new JSONParser();
            result = jsonParser.sendState(url,jsonParser.getInfo(mContext));
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
    }


    public void getJson(JSONObject jsonObject){
        try{
            String categories = jsonObject.getString("categories");
            JSONArray allarr1 = new JSONArray(categories);
            for (int i=0;i<allarr1.length();i++){
                String str = allarr1.getString(i);
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
            //standards
            String standards = jsonObject.getString("standards");
            JSONArray allarr2 = new JSONArray(standards);
            for (int i=0;i<allarr2.length();i++){
                String strmain = allarr2.getString(i);
                JSONObject jsonObjects = new JSONObject(strmain);
                String category = jsonObjects.getString("category");
                String standard = jsonObjects.getString("standard");
                categorylist.add(category);
                standardlist.add(standard);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public List<String> getDataList(){
        return data_list;
    }

    public List<String> getInfo_list(){
        return info_list;
    }

    public List<String> getSum_list(){
        return sum_list;
    }

    public List<String> getCategorylist(){
        return categorylist;
    }

    public List<String> getStandardlist(){
        return standardlist;
    }
}
