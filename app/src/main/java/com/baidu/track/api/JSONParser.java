package com.baidu.track.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;
import static com.baidu.track.ui.activity.MainActivity.BASE_URL;

public class JSONParser {
    static InputStream is = null;
    static int jObj;
    static String json = "";
    static String result="";
    static String strResult = "";
    // constructor
    public JSONParser() {
    }

    /**
     * post上传List<NameValuePair>
     * @param url
     * @param method
     * @param params
     * @return
     */
    public String makeHttpRequest(final String url, String method, final List<NameValuePair> params) {
        // Making HTTP request
        final Thread t1= new Thread(
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            //使用POST请求
                            DefaultHttpClient httpClient = new DefaultHttpClient();
                            HttpPost httpPost = new HttpPost(url);
                            httpPost.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
                            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
                            HttpResponse httpResponse = httpClient.execute(httpPost);
                            HttpEntity httpEntity = httpResponse.getEntity();
                            is = httpEntity.getContent();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                    is, "UTF-8"));
                            StringBuilder sb = new StringBuilder();
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            is.close();
                            json = sb.toString();
                            Log.d("json1", json);
                            jObj = 1;
                        } catch (Exception e) {
                            Log.e("Buffer Error", "Error converting result " + e.toString());
                            Log.d("json", json);
                            jObj = 2;
                        }
                    }
                });
        t1.start();
        try{
            t1.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        return json;
    }

    public static String sendGETRequest(final String path, final Map<String, String> map, final String ecoding){
        // Making HTTP request
        final Thread t1= new Thread(
                new Thread(){
                    @Override
                    public void run() {
                        /*将路径拼成http://10.20.124.72:8080/videonews/ManagerServlet?title=XXX&timelength=90*/
                        try{
                            StringBuilder url=new StringBuilder(path);
                            url.append("?");
                            //map迭代器Entry<Key, Value>
                            for(Map.Entry<String, String> entry:map.entrySet()){
                                url.append(entry.getKey()).append("=");
                                //ecoding是上面传来的“UTF-8”，为了防止中文乱码
                                url.append(URLDecoder.decode(entry.getValue(), ecoding));
                                url.append("&");
                            }
                            url.deleteCharAt(url.length()-1);
                            URL url2=new URL(url.toString());
                            Log.i("url2",url2.toString());
                            HttpURLConnection conn=(HttpURLConnection) url2.openConnection();
                            conn.setConnectTimeout(5000);
                            conn.setRequestMethod("GET");
                            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                            StringBuffer buffer = new StringBuffer();
                            String line = "";
                            while ((line = in.readLine()) != null){
                                buffer.append(line);
                            }
                            strResult = buffer.toString();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (ProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        t1.start();
        try{
            t1.join();
        }catch (Exception e){
            e.printStackTrace();
        }

        return strResult;
    }


    /**
     * post上传JSONObject
     * @param url
     * @param method
     * @param jsonObject
     */
    public void makeHttpRequests(final String url, String method, final JSONObject jsonObject) {
        // Making HTTP request
        final Thread t2= new Thread(
                new Thread(){
                    @Override
                    public void run() {
                        try{

                            String backJson = jsonObject.toString();
                            backJson = backJson.replace("\\","");
                            Log.d("backJson", backJson);
                            //设置请求方式与参数
                            URI uri = new URI(url);
                            HttpPost httpPost = new HttpPost(uri);
                            httpPost.getParams().setParameter("http.socket.timeout", new Integer(500000));
                            httpPost.setHeader("Content-type", "pplication/json; charset=UTF-8");
                            httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows 2000)");
                            httpPost.setHeader("IConnection", "close");
                            httpPost.setEntity(new StringEntity(backJson));

                            //执行请求
                            HttpClient httpclient = new DefaultHttpClient();
                            httpclient.getParams().setParameter("Content-Encoding", "UTF-8");
                            HttpResponse response = httpclient.execute(httpPost);
                            Log.d("sanhui", "响应码： " + response.getStatusLine().getStatusCode()); //获取响应码
                            Log.d("san", "result = " + EntityUtils.toString(response.getEntity(), "GB2312")); //获取响应内容
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
        t2.start();
        try{
            t2.join();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取当前SharedPreferences中的token
     * @param context
     * @return
     */
    public static String getInfo(Context context){
        SharedPreferences sp=context.getSharedPreferences("loginInfo", MODE_PRIVATE);
        String token=sp.getString("access_token",null);
        return token;
    }

    /**
     * 用tokenGet数据
     * @param url
     * @param token
     * @return
     */
    public String sendState(final String url,final String token){

        final Thread t2= new Thread(
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            HttpClient httpClient = new DefaultHttpClient();
                            HttpGet httpGet = new HttpGet(url);
                            httpGet.setHeader("Authorization", "Bearer"+""+token);
                            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
                            HttpResponse httpResponse = httpClient.execute(httpGet);
                            Log.d("sanhui", "响应码： " + httpResponse.getStatusLine().getStatusCode()); //获取响应码
                            result = EntityUtils.toString(httpResponse.getEntity(), "GB2312");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
        t2.start();
        try{
            t2.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 以旧token申请新token
     * @param token
     * @return
     */
    public static String sendStateToken(final String token){

        final Thread t2= new Thread(
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            HttpClient httpClient = new DefaultHttpClient();
                            HttpPut httpPut = new HttpPut(BASE_URL+"/api/authorizations/current");
                            httpPut.setHeader("Authorization", "Bearer"+""+token);
                            httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));
                            HttpResponse httpResponse = httpClient.execute(httpPut);
                            Log.d("sanhui1", "响应码1： " + httpResponse.getStatusLine().getStatusCode()); //获取响应码
                            result = EntityUtils.toString(httpResponse.getEntity(), "GB2312");
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
        t2.start();
        try{
            t2.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     *
     * @param json  json数据
     * @param path 上传接口路径
     * @return
     * @throws
     */
    @SuppressWarnings({ "resource", "deprecation" })
    public static String post(final JSONObject json, final String path,final String token) {
        final Thread t2= new Thread(new Thread(){
            @Override
            public void run() {
                try {

                    URI url = new URI(path);
                    HttpClient client=new DefaultHttpClient();
                    HttpPost post=new HttpPost(url);
                    post.setHeader("Content-Type", "appliction/json");
                    post.addHeader("Authorization", "Bearer"+""+token);
                    StringEntity s=new StringEntity(json.toString(), "utf-8");
                    s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "appliction/json"));
                    post.setEntity(s);
                    client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", SSLSocketFactory.getSocketFactory(),443));

                    HttpResponse httpResponse=client.execute(post);
                    result  = EntityUtils.toString(httpResponse.getEntity(), "GB2312");
                    Log.d("sanhui", "响应码： " + httpResponse.getStatusLine().getStatusCode()); //获取响应码
                    Log.i("result",result);
                    if(httpResponse.getStatusLine().getStatusCode()!= HttpStatus.SC_OK){
                        result="服务器异常";
                    }
                } catch (Exception e) {
                    System.out.println("请求异常");
                    throw new RuntimeException(e);
                }
            }
        });
        t2.start();
        try{
            t2.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("result=="+result);
        return result;
    }

    /**
     * 更行token
     * @param context
     */
    public static void changeToken(Context context){
        String newToken =  sendStateToken(JSONParser.getInfo(context));
        Log.i("newToken",newToken);
        try{
            JSONObject tokenJson = new JSONObject(newToken);
            String newtoken =  tokenJson.getString("access_token");

            SharedPreferences sp=context.getSharedPreferences("loginInfo", MODE_PRIVATE);
            //获取编辑器
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("access_token", newtoken);
            //提交修改
            editor.commit();
        }catch (JSONException e){
            e.printStackTrace();
        }

    }

    /**
     * 修改登录状态
     * @param context
     */
    public static void changeStatu(Context context){
        SharedPreferences sp=context.getSharedPreferences("loginInfo", MODE_PRIVATE);
        //获取编辑器
        SharedPreferences.Editor editor=sp.edit();
        //存入boolean类型的登录状态
        editor.putBoolean("isLogin", false);
        editor.putString("access_token", null);
        //提交修改
        editor.commit();
    }


}