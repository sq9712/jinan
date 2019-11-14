package com.baidu.track.ui.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.api.track.AddPointRequest;
import com.baidu.trace.api.track.AddPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.LatLng;
import com.baidu.trace.model.Point;
import com.baidu.track.R;
import com.baidu.track.TrackApplication;
import com.baidu.track.api.JSONParser;
import com.baidu.track.model.CurrentLocation;
import com.baidu.track.ui.view.ImageAdapter;
import com.baidu.track.ui.view.PhotoAdapter;
import com.baidu.track.utils.CommonUtil;
import com.baidu.track.utils.camera.GifSizeFilter;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import static com.baidu.track.ui.activity.MainActivity.BASE_URL;

/**
 * 处理意见或建议界面
 */
public class HandleActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private Button cancel,submit;
    private TextView username,nowTime,taskTitle,taskContent;
    private EditText content;
    private JSONArray imgString = new JSONArray();
    private String id,subject,tagment,title,contentdetail,name,information,suggest;
    private Intent intent;

    private RadioButton radioButton=null;
    private RadioGroup nRg1;

    private JSONObject allObj = new JSONObject();
    private ImageAdapter imageAdapter;

    /**
     * 添加三张图片
     */
    private RecyclerView recyclerView,recyclerView1;
    private PhotoAdapter photoAdapter;

    private List<Uri> mUris=new ArrayList<>();
    private List<String> mPaths=new ArrayList<>();
    private static final int REQUEST_CODE_CHOOSE = 23;

    private ArrayList<String> urlList  = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_deal);
        mContext = this;

        content = findViewById(R.id.result);
        cancel = findViewById(R.id.cancel_button);
        submit = findViewById(R.id.publish);

        username = findViewById(R.id.peplename);
        nowTime = findViewById(R.id.nowtime);
        taskTitle = findViewById(R.id.Title);
        taskContent = findViewById(R.id.Details);

        nRg1 = findViewById(R.id.rg_1);
        radioButton = findViewById(nRg1.getCheckedRadioButtonId());
        nRg1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                radioButton = radioGroup.findViewById(i);
            }
        });

        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        name = sp.getString("name","");
        //获取DetailsActivity传来的责任类型
        Bundle bundle=getIntent().getExtras();
        tagment = bundle.getString("tagname");
        recyclerView1 = findViewById(R.id.recyclerView1);

        if(tagment.equals("TaskActivity")){
            id=bundle.getString("id");
            //标题，内容
            title = bundle.getString("taskTitle");
            contentdetail = bundle.getString("content");
            information = bundle.getString("information");

            //放到最外层
            urlList = bundle.getStringArrayList("imgurls");
            Log.i("urlList",String.valueOf(urlList));
            Log.i("information",String.valueOf(information));
            if (urlList.get(0).equals(BASE_URL+"null")){
                //无操作
            }else{
                //显示图片
                recyclerView1.setVisibility(View.VISIBLE);
                recyclerView1.setLayoutManager(new GridLayoutManager(this, 3));
                imageAdapter = new ImageAdapter(this, urlList);
                recyclerView1.setAdapter(imageAdapter);
            }
            if(information.equals("null")){

            }else{
                //编辑框显示之前编辑的内容
                content.setText(information);
            }

        }else if (tagment.equals("DealActivity")){
            title = bundle.getString("title");
            contentdetail = bundle.getString("content");
        }else{
            id=bundle.getString("id");
            //标题，内容
            title = bundle.getString("title");
            contentdetail = bundle.getString("content");
            information = bundle.getString("information");

            //放到最外层
            urlList = bundle.getStringArrayList("imgurls");
            Log.i("urlList",String.valueOf(urlList));
            Log.i("information",String.valueOf(information));
            if (urlList.get(0).equals("https://www.vorin.netnull")){
                //无操作
            }else{
                //显示图片
                recyclerView1.setVisibility(View.VISIBLE);
                recyclerView1.setLayoutManager(new GridLayoutManager(this, 3));
                imageAdapter = new ImageAdapter(this, urlList);
                recyclerView1.setAdapter(imageAdapter);
            }
            if(information.equals("null")){

            }else{
                //编辑框显示之前编辑的内容
                content.setText(information);
            }
        }
        subject=bundle.getString("subject");
        if (subject.equals("0")){
            nRg1.setVisibility(View.GONE);
        }
        taskContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        // 获取当前时间
        Date date1 = new Date(System.currentTimeMillis());

        username.setText(name);
        //现在的时间
        nowTime.setText(simpleDateFormat1.format(date1));
        taskTitle.setText(title);
        taskContent.setText(contentdetail);

        cancel.setOnClickListener(this);
        submit.setOnClickListener(this);



        /**
         * 添加三张图片
         */
        //设置RecyclerView
        recyclerView =  findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        photoAdapter = new PhotoAdapter(this, onAddPicListener);
        photoAdapter.setSelectMax(3);
        recyclerView.setAdapter(photoAdapter);

    }


    //加号的点击事件
    private PhotoAdapter.onAddPicListener onAddPicListener = new PhotoAdapter.onAddPicListener() {
        @Override
        public void onAddPicClick(final int type, final int position) {
            RxPermissions rxPermissions=new RxPermissions(HandleActivity.this);
            rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@io.reactivex.annotations.NonNull Boolean aBoolean) {
                            if (aBoolean){
                                switch (type) {
                                    case 0:
                                        Matisse.from(HandleActivity.this)
                                                .choose(MimeType.ofImage(),false)
                                                .countable(true)
                                                .capture(true)
                                                .captureStrategy(
                                                        new CaptureStrategy(true, "com.baidu.track.fileprovider"))
                                                .maxSelectable(3-mPaths.size())
                                                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                                .gridExpectedSize(
                                                        getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                                                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                                                .thumbnailScale(0.85f)
                                                .imageEngine(new GlideEngine())
                                                .forResult(REQUEST_CODE_CHOOSE);

                                        break;
                                    case 1:
                                        // 删除图片
                                        mUris.remove(position);
                                        mPaths.remove(position);
                                        photoAdapter.notifyItemRemoved(position);
                                        break;
                                }
                            }else {
                                Toast.makeText(HandleActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button :
                if(tagment.equals("TaskActivity")) {
                    intent = new Intent(mContext, LookActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    finish();
                }
                break;
            //将数据都提交到服务器
            case R.id.publish:

                //单选框为处理中
                if(radioButton.getText().equals("处理中")){
                    subject = "2";
                }else{
                    subject = "1";
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");// HH:mm:ss
                // 获取当前时间
                Date date = new Date(System.currentTimeMillis());
                suggest = content.getText()+simpleDateFormat.format(date)+";";
                if(mPaths.size() == 0){
                    Toast.makeText(mContext, "请现场取证", Toast.LENGTH_SHORT).show();
                }else{
                    JSONParser jsonParser = new JSONParser();
                    try{
                        for (int j=0;j<mPaths.size();j++){
                            // 获取输入流
                            is = new FileInputStream(mPaths.get(j));
                            // 把流解析成bitmap,此时就得到了清晰的原图
                            Bitmap bitmap = BitmapFactory.decodeStream(is);
                            //对Bitmap对象进行压缩处理
                            Bitmap bm = imageZoom(bitmap, 310.00);
                            imgString.put(bitmapToBase64(bm));
                        }
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                    if(tagment.equals("TaskActivity")){
                        //上传12345派单过来处理的问题
                        try{
                            allObj.put("id", id);//任务编号
                            allObj.put("suggest", suggest);//处理意见
                            allObj.put("result", subject);//责任类型
                            allObj.put("img" , imgString);//拍摄好的图
                            jsonParser.post(allObj,BASE_URL+"/api/endMatter",jsonParser.getInfo(mContext));//将任务处理信息传入服务器
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        startActivity(new Intent(this,LookActivity.class));
                        finish();
                    }else if(tagment.equals("DealActivity")){
                        String location = getLngAndLat(this);
                        List<String> list= Arrays.asList(location.split(","));

                        //上传巡查员发现的问题
                        try{
                            SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
                            allObj.put("id", sp.getString("track_id",""));//轨迹ID
                            allObj.put("title", title);//任务标题
                            allObj.put("content", contentdetail);//任务问题描述
                            //巡查员的实时位置
                            allObj.put("latitude", list.get(1));//纬度
                            allObj.put("longitude",  list.get(0));//经度
                            allObj.put("suggest", suggest);//处理意见
                            allObj.put("result", subject);//责任类型
                            allObj.put("img" , imgString);//拍摄好的图片

                            jsonParser.post(allObj,BASE_URL+"/api/importMatter",jsonParser.getInfo(mContext));//将任务处理信息传入服务器
                           // startActivity(new Intent(this, StartAndEndActivity.class));
                            finish();
                        }catch (JSONException e){
                            e.printStackTrace();
                         }
                    }else{
                        String location = getLngAndLat(this);
                        List<String> list= Arrays.asList(location.split(","));
                        //上传巡查员发现的问题
                        try{
                            allObj.put("id", id);//任务id
                            allObj.put("title", title);//任务标题
                            allObj.put("content", contentdetail);//任务问题描述
                            //巡查员的实时位置
                            allObj.put("latitude", list.get(1));//纬度
                            allObj.put("longitude",  list.get(0));//经度
                            allObj.put("suggest", suggest);//处理意见
                            allObj.put("result", subject);//责任类型
                            allObj.put("img" , imgString);//拍摄好的图片

                            jsonParser.post(allObj,BASE_URL+"/api/importMatter",jsonParser.getInfo(mContext));//将任务处理信息传入服务器
                            // startActivity(new Intent(this, StartAndEndActivity.class));
                            finish();
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
    private FileInputStream is;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {

            for (int i=0;i<Matisse.obtainResult(data).size();i++){
                mUris.add(Matisse.obtainResult(data).get(i));
                mPaths.add(Matisse.obtainPathResult(data).get(i));
            }
            photoAdapter.setData(mUris,mPaths);
        }
    }

    public static Bitmap imageZoom(Bitmap bitMap, double maxSize) {
        if (bitMap != null) {
            //将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            //将字节换成KB
            double mid = b.length / 1024;
            //判断bitmap占用空间是否大于允许最大空间  如果大于则压缩 小于则不压缩
            if (mid > maxSize) {
                //获取bitmap大小 是允许最大大小的多少倍
                double i = mid / maxSize;
                //开始压缩  此处用到平方根 将宽带和高度压缩掉对应的平方根倍 （1.保持刻度和高度和原bitmap比率一致，压缩后也达到了最大大小占用空间的大小）
                bitMap = zoomImage(bitMap, bitMap.getWidth() / Math.sqrt(i),
                        bitMap.getHeight() / Math.sqrt(i));
            }
        }
        return bitMap;
    }
    /***
     * 图片的缩放方法
     *
     * @param bgimage   ：源图片资源
     * @param newWidth  ：缩放后宽度
     * @param newHeight ：缩放后高度
     * @return
     */
    public static Bitmap zoomImage(Bitmap bgimage, double newWidth,double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }


    //如上参需要64位编码可调用此方法，不需要可以忽略
    public static String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 获取经纬度
     *
     * @param context
     * @return
     */
    private String getLngAndLat(Context context) {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {  //从gps获取经纬度
            try{
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    //当GPS信号弱没获取到位置的时候又从网络获取
                    return getLngAndLatWithNetwork();
                }
            }catch (SecurityException e){
                e.printStackTrace();
            }
        } else {
            //从网络获取经纬度
            return getLngAndLatWithNetwork();
        }
        return longitude + "," + latitude;
    }

    //从网络获取经纬度
    public String getLngAndLatWithNetwork() {
        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }catch (SecurityException e){
            e.printStackTrace();
        }
        return longitude + "," + latitude;
    }


    LocationListener locationListener = new LocationListener() {

        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {

        }

        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {

        }

        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
