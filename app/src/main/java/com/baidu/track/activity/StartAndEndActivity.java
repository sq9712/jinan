package com.baidu.track.activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.DistanceRequest;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.SupplementMode;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.baidu.trace.model.TransportMode;
import com.baidu.track.R;
import com.baidu.track.TrackApplication;
import com.baidu.track.api.JSONParser;
import com.baidu.track.model.CurrentLocation;
import com.baidu.track.ui.activity.DealActivity;
import com.baidu.track.utils.BitmapUtil;
import com.baidu.track.utils.CommonUtil;
import com.baidu.track.utils.Constants;
import com.baidu.track.utils.LedTextView;
import com.baidu.track.utils.MapUtil;
import com.baidu.track.utils.MixSpeakUtil;
import com.baidu.track.utils.SharedPreferencesUtils;
import com.baidu.track.utils.TakePhotoButton;
import com.baidu.track.utils.TrackReceiver;
import com.baidu.track.utils.ViewUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import static com.baidu.track.ui.activity.MainActivity.BASE_URL;
/**
 * 巡查界面
 */
public class StartAndEndActivity extends Activity implements View.OnClickListener, SensorEventListener {
    private String url = BASE_URL+"/api/startAndEndPatrol";
    private TrackApplication trackApp = null;
    private ViewUtil viewUtil = null;
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private TrackReceiver trackReceiver = null;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private LinearLayout addBtn = null;
    private LinearLayout backBtn = null;
    private MapUtil mapUtil = null;//地图工具
    private OnTraceListener traceListener = null;//轨迹服务监听器
    private OnTrackListener trackListener = null;//轨迹监听器(用于接收纠偏后实时位置回调)
    private OnEntityListener entityListener = null;//Entity监听器(用于接收实时定位回调)
    private RealTimeHandler realTimeHandler = new RealTimeHandler();//实时定位任务
    private RealTimeLocRunnable realTimeLocRunnable = null;
    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;//打包周期
    private List<LatLng> trackPoints;//轨迹点集合
    private boolean firstLocate = true;
    private String id = null;
    private long Time;//开始采集时间
    public long beginTime;//查询历史轨迹开始时间
    public long endTime = CommonUtil.getCurrentTime();//查询历史轨迹结束时间
    private HistoryTrackRequest historyTrackRequest;
    private int pageIndex = 1;
    private MixSpeakUtil mixSpeakUtil;//百度语音
    private LocationManager locationManager;//GPS卫星
    private String provider;
    private String TAG = "GPS";
    private TextView  editText;
    private TextView mTextView;//电子时钟
    private Boolean addOrRealtime;//判断是补偿还是实时
    private  int counts = 0;//记录卫星个数
    private TextView textdistance;//里程
    private TakePhotoButton button;//长按按钮
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracing_1);
        //语音
        mixSpeakUtil=MixSpeakUtil.getInstance(this);
        BitmapUtil.init();
        setTitle(R.string.tracing_title);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);//监听卫星信号强度locationManager.addGpsStatusListener(statusListener)
        setOnClickListener(this);
        init();
        startGatherButton();
        //创建Alarm并启动
        Intent intent = new Intent("LOCATION_CLOCK");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        // 每五秒唤醒一次
        Context context=this;
        long second = 15 * 1000;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), second, pendingIntent);
        if (powerManager == null) {
            //针对熄屏后cpu休眠导致的无法联网、定位失败问题,通过定期点亮屏幕实现联网,本操作会导致cpu无法休眠耗电量增加,谨慎使用
            powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl=powerManager
                    .newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            wl.acquire();
            //点亮屏幕
            wl.release();
            //释放
        }
    }
    /**
     * 设置点击监听器
     */
    public void setOnClickListener(View.OnClickListener listener) {
        LinearLayout optionsButton = (LinearLayout) findViewById(R.id.btn_activity_options);
        optionsButton.setOnClickListener(listener);
    }
    private void init() {
        initListener();
        trackApp = (TrackApplication) getApplicationContext();
        viewUtil = new ViewUtil();
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) findViewById(R.id.tracing_mapView));
        mapUtil.setCenter(mCurrentDirection);//设置地图中心点
        powerManager = (PowerManager) trackApp.getSystemService(Context.POWER_SERVICE);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);// 获取传感器管理服务
        addBtn = findViewById(R.id.add);
        backBtn = findViewById(R.id.btn_activity_back);
        textdistance=(TextView)findViewById(R.id.text_total_distance_detail);
        editText=(TextView)findViewById(R.id.editText);
        mTextView = (TextView) findViewById(R.id.main_clock_time);
        addBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        addOrRealtime=false;

    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //每次方向改变，重新给地图设置定位数据，用上一次的经纬度
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {// 方向改变大于1度才设置，以免地图上的箭头转动过于频繁
            mCurrentDirection = (int) x;
            if (!CommonUtil.isZeroPoint(CurrentLocation.latitude, CurrentLocation.longitude)) {
                mapUtil.updateMapLocation(new LatLng(CurrentLocation.latitude, CurrentLocation.longitude), (float) mCurrentDirection);
            }
        }
        lastX = x;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    /**
     * 结束巡逻，上传结束时间
     */
    private void StopTimePost(){
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
        // 获取当前时间
        Date date1 = new Date(System.currentTimeMillis());
        String endTime = simpleDateFormat1.format(date1);
        JSONObject alljson1 = new JSONObject();
        try{
            SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
            alljson1.put("end_time",endTime);
            alljson1.put("id",sp.getString("track_id",""));
            String str = JSONParser.post(alljson1,url, JSONParser.getInfo(this));
            JSONObject lastjson = new JSONObject(str);
            if(lastjson.optString("message").equals("Unauthenticated.")){
                JSONParser.changeToken(this);
                JSONParser.post(alljson1,url, JSONParser.getInfo(this));
            }
            //获取编辑器
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("track_id", null);
            //提交修改
            editor.commit();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    /**
     * 开始巡逻，上传开始时间
     */
    private void StartTimePost(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        // 获取当前时间
        Date date = new Date(System.currentTimeMillis());
        String startTime = simpleDateFormat.format(date);
        JSONObject alljson = new JSONObject();
        try{
            alljson.put("start_time",startTime);
            String str = JSONParser.post(alljson,url, JSONParser.getInfo(this));
            JSONObject lastjson = new JSONObject(str);
            if(lastjson.optString("message").equals("Unauthenticated.")){
                JSONParser.changeToken(this);
                JSONParser.post(alljson,url, JSONParser.getInfo(this));
            }
            String data = lastjson.getString("data");
            Log.i("轨迹id",data);
            JSONObject datajson = new JSONObject(data);
            id = datajson.getString("id");
            SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
            //获取编辑器
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("track_id", id);
            //提交修改
            editor.commit();
        }catch(JSONException e){
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 追踪选项设置
            case R.id.btn_activity_options:
//                ViewUtil.startActivityForResult(this, RecordActivity.class, Constants.REQUEST_CODE);
//               ViewUtil.startActivityForResult(this, TracingOptionsActivity.class, Constants
//                        .REQUEST_CODE);
                break;
            case R.id.add:
                Intent intent = new Intent(this, DealActivity.class);
                intent.putExtra("case",1);
                startActivity(intent);
                break;
            case R.id.btn_activity_back:
                if(trackApp.isGatherStarted) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog.setTitle("提示");
                    dialog.setMessage("请先结束巡查任务");
                    dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("", "___退出");
                        }
                    });
                    if (dialog != null) {
                        dialog.show();
                    }
                }else{
                    finish();
                }
                break;
            default:
                break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (trackApp.isGatherStarted) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("提示");
                dialog.setMessage("请先结束巡查任务");
                dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d("", "___退出");
                    }
                });
                if (dialog != null) {
                    dialog.show();
                }
            } else {
                finish();
            }
        }
        return false;
    }
    /**
     * 实时定位任务
     */
    class RealTimeLocRunnable implements Runnable {
        private int interval = 0;
        public RealTimeLocRunnable(int interval) {
            this.interval = interval;
        }
        @Override
        public void run() {
            trackApp.getCurrentLocation(entityListener, trackListener);
            realTimeHandler.postDelayed(this, interval * 1000);
        }
    }
    public void startRealTimeLoc(int interval) {
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }
    public void stopRealTimeLoc() {
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
        trackApp.mClient.stopRealTimeLoc();
    }
    /**
     * 回调的监听
     */
    private void initListener() {
        trackPoints = new ArrayList<>();
        trackListener = new OnTrackListener() {
            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                //经过服务端纠偏后的最新的一个位置点，回调
                Log.i("接口调用","onLatestPointCallback");
                try {
                    if (StatusCodes.SUCCESS != response.getStatus()) {
                        return;
                    }
                    LatestPoint point = response.getLatestPoint();
                    if (null == point || CommonUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation().getLongitude()))
                    {
                        return;
                    }
                    LatLng currentLatLng = mapUtil.convertTrace2Map(point.getLocation());
                    if (null == currentLatLng) {
                        return;
                    }
                    //当前经纬度
                    CurrentLocation.locTime = point.getLocTime();
                    CurrentLocation.latitude = currentLatLng.latitude;
                    CurrentLocation.longitude = currentLatLng.longitude;

                    if(firstLocate){
                        firstLocate = false;
                        mixSpeakUtil.speak("起点获取中，请稍后...");
                        Toast.makeText(StartAndEndActivity.this,"起点获取中，请稍后...",Toast.LENGTH_SHORT).show();
//                        trackPoints = new ArrayList<>();
//                        trackPoints.add(mapUtil.convertTrace2Map(point.getLocation()));
//                        mapUtil.drawHistoryTrack(trackPoints, false, 0);//画轨迹
                        return;
                    }
                    if (trackPoints == null) {
                        return;
                    }
                    endTime = CommonUtil.getCurrentTime();
                    beginTime=endTime-15;
                    Time = SharedPreferencesUtils.getParam(StartAndEndActivity.this, "Time", 0L);
                    String DATE_FORMAT1 = "%02d时%02d分%02d秒";
                    long count=endTime-Time;
                    int s= (int) (count%60);//秒
                    int m= (int) (count/60);//分
                    int h=(int) (count/3600);//时

                    String speak="你现在已经连续巡查"+m+"分钟了，请注意休息！或者关闭采集";

                    int alarm=m%5;
                    if(m>=30&&alarm==0)
                    {
                        mixSpeakUtil.speak(speak);
                    }
                    count=count*1000-8*60*60*1000;;//总秒数
                    //mTextView.setText(String.format(DATE_FORMAT1,h,m,s));
                    mTextView.setText(CommonUtil.getHMS(count));
                    if (addOrRealtime)
                    {
                        long groundTime=SharedPreferencesUtils.getParam(StartAndEndActivity.this, "groundTime", 0L);
                        long frontTime = SharedPreferencesUtils.getParam(StartAndEndActivity.this, "frontTime", 0L);
                        queryHistoryTrack(groundTime,frontTime);
                        Log.e("绘制轨迹", "补偿路线");
                    }else {
                        queryHistoryTrack();
                        Log.e("绘制轨迹", "实时路线");
                    }
                } catch (Exception x) {
                }
            }
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                try {
                    int total = response.getTotal();
                    if (StatusCodes.SUCCESS != response.getStatus()) {
                        viewUtil.showToast(StartAndEndActivity.this, response.getMessage());
                    } else if (0 == total) {
                        // viewUtil.showToast(StartAndEndActivity1.this, getString(R.string.no_track_data));
                    } else {
                        List<TrackPoint> points = response.getTrackPoints();
                        if (null != points) {
                            for (TrackPoint trackPoint : points) {
                                if (!CommonUtil.isZeroPoint(trackPoint.getLocation().getLatitude(),
                                        trackPoint.getLocation().getLongitude())) {
                                    trackPoints.add(MapUtil.convertTrace2Map(trackPoint.getLocation()));
                                }
                            }
                        }
                    }
                    //查找下一页数据
                    if (total > Constants.PAGE_SIZE * pageIndex) {
                        historyTrackRequest.setPageIndex(++pageIndex);
                        if (addOrRealtime)
                        {
                            long groundTime=SharedPreferencesUtils.getParam(StartAndEndActivity.this, "groundTime", 0L);
                            long frontTime = SharedPreferencesUtils.getParam(StartAndEndActivity.this, "frontTime", 0L);
                            queryHistoryTrack(groundTime,frontTime);
                        }else {
                            queryHistoryTrack();
                        }
                    } else {
                        mapUtil.drawHistoryTrack(trackPoints, false, mCurrentDirection);//画轨迹
                        if (addOrRealtime)
                        {
                            long groundTime=SharedPreferencesUtils.getParam(StartAndEndActivity.this, "groundTime", 0L);
                            long frontTime = SharedPreferencesUtils.getParam(StartAndEndActivity.this, "frontTime", 0L);
                            addOrRealtime=false;
                            queryDistance(groundTime,frontTime);// 查询里程
                        }else {
                            queryDistance();// 查询里程
                        }
                    }
                } catch (Exception e) {
                }
            }
            @Override
            public void onDistanceCallback(DistanceResponse response) {
                super.onDistanceCallback(response);
                int  bbbb=(int)response.getDistance();
                String DATE_FORMAT = "%02d米";
                textdistance.setText(String.format(DATE_FORMAT,bbbb));
            }
        };
        entityListener = new OnEntityListener() {

            @Override
            public void onReceiveLocation(TraceLocation location) {
                //本地LBSTraceClient客户端获取的位置
                try {
                    if (StatusCodes.SUCCESS != location.getStatus() || CommonUtil.isZeroPoint(location.getLatitude(),
                            location.getLongitude())) {
                        return;
                    }
                    LatLng currentLatLng = mapUtil.convertTraceLocation2Map(location);
                    if (null == currentLatLng) {
                        return;
                    }
                    CurrentLocation.locTime = CommonUtil.toTimeStamp(location.getTime());
                    CurrentLocation.latitude = currentLatLng.latitude;
                    CurrentLocation.longitude = currentLatLng.longitude;
                    if (null != mapUtil) {
                        mapUtil.updateMapLocation(currentLatLng, mCurrentDirection);//显示当前位置
                        mapUtil.animateMapStatus(currentLatLng);//缩放
                    }
                } catch (Exception x) {
                }
            }

        };
        traceListener = new OnTraceListener() {
            @Override
            public void onBindServiceCallback(int errorNo, String message) {
            }
            @Override
            public void onStartTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
                    trackApp.isTraceStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_trace_started", true);
                    editor.apply();
                    registerReceiver();
                    startLocationService();//卫星状态
                    Log.e("服务状态","开启");
                }
                if(errorNo==0){
                    mixSpeakUtil.speak("服务开启"+message);
                }else if(errorNo!=10006){
                    mixSpeakUtil.speak(message);
                }
            }
            @Override
            public void onStopTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.CACHE_TRACK_NOT_UPLOAD == errorNo) {
                    trackApp.isTraceStarted = false;
                    trackApp.isGatherStarted = false;
                    // 停止成功后，直接移除is_trace_started记录（便于区分用户没有停止服务，直接杀死进程的情况）
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_trace_started");
                    editor.remove("is_gather_started");
                    editor.apply();
                    unregisterPowerReceiver();
                    stopLocationService();//卫星状态
                    firstLocate = true;
                    Log.e("服务状态","关闭");
                }
                if(errorNo==0){
                    mixSpeakUtil.speak("服务关闭"+message);
                    mixSpeakUtil.speak("停止采集");
                }
                if(errorNo==11003){
                    mixSpeakUtil.speak(message);
                }
            }
            @Override
            public void onStartGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STARTED == errorNo) {
                    trackApp.isGatherStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_gather_started", true);
                    editor.apply();
                    StartTimePost();
                    stopRealTimeLoc();
                    startRealTimeLoc(packInterval);
                    mixSpeakUtil.speak("开始采集");
                }
                if(errorNo==12002){
                    mixSpeakUtil.speak(message);
                }
            }
            @Override
            public void onStopGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STOPPED == errorNo) {
                    trackApp.isGatherStarted = false;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_gather_started");
                    editor.apply();
                    StopTimePost();
                    firstLocate = true;
                    isPause=false;
                    stopRealTimeLoc();
                    startRealTimeLoc(Constants.LOC_INTERVAL);
                    if (trackPoints.size() >= 1) {
                        try {
                            mapUtil.drawEndPoint(trackPoints.get(trackPoints.size() - 1));
                        } catch (Exception e) {
                        }
                    }
                    if (null != trackPoints) {
                        trackPoints.clear();
                        pageIndex = 1;
                        //trackPoints=null;
                        //trackPoints=new ArrayList<>();
                    }
                }
            }
            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {
            }
        };
    }
    /**
     * 查询历史里程
     */
    private void queryDistance() {
        DistanceRequest distanceRequest = new DistanceRequest(trackApp.getTag(), trackApp.serviceId, trackApp.entityName);
        distanceRequest.setStartTime(Time); // 设置开始时间
        distanceRequest.setEndTime(endTime);     // 设置结束时间
        distanceRequest.setProcessed(true);      // 纠偏
        ProcessOption processOption = new ProcessOption();// 创建纠偏选项实例
        processOption.setNeedDenoise(true);// 去噪
        processOption.setNeedMapMatch(false );// 绑路
        processOption.setTransportMode(TransportMode.walking);// 交通方式为步行
        distanceRequest.setProcessOption(processOption);// 设置纠偏选项
        distanceRequest.setSupplementMode(SupplementMode.no_supplement);// 里程填充方式为无
        trackApp.mClient.queryDistance(distanceRequest, trackListener);// 查询里程
    }
    /**
     * 查询历史里程、补偿
     */
    private void queryDistance(long beginTime,long endTime) {
        DistanceRequest distanceRequest = new DistanceRequest(trackApp.getTag(), trackApp.serviceId, trackApp.entityName);
        distanceRequest.setStartTime(beginTime); // 设置开始时间
        distanceRequest.setEndTime(endTime);     // 设置结束时间
        distanceRequest.setProcessed(true);      // 纠偏
        ProcessOption processOption = new ProcessOption();// 创建纠偏选项实例
        processOption.setNeedDenoise(true);// 去噪
        processOption.setNeedMapMatch(false );// 绑路
        processOption.setTransportMode(TransportMode.walking);// 交通方式为步行
        distanceRequest.setProcessOption(processOption);// 设置纠偏选项
        distanceRequest.setSupplementMode(SupplementMode.no_supplement);// 里程填充方式为无
        trackApp.mClient.queryDistance(distanceRequest, trackListener);// 查询里程
    }
    /**
     * 查询历史轨迹
     */
    private void queryHistoryTrack() {
        historyTrackRequest = new HistoryTrackRequest();
        ProcessOption processOption = new ProcessOption();//纠偏选项
        processOption.setRadiusThreshold(20);//精度过滤
        processOption.setTransportMode(TransportMode.walking);//交通方式，默认为驾车
        processOption.setNeedDenoise(true);//去噪处理，默认为false，不处理
        processOption.setNeedVacuate(true);//设置抽稀，仅在查询历史轨迹时有效，默认需要false
        processOption.setNeedMapMatch(false);//
        historyTrackRequest.setProcessOption(processOption);
        trackApp.initRequest(historyTrackRequest);
        /**
         * 设置里程补偿方式，当轨迹中断5分钟以上，会被认为是一段中断轨迹，默认不补充
         * 比如某些原因造成两点之间的距离过大，相距100米，那么在这两点之间的轨迹如何补偿
         * SupplementMode.driving：补偿轨迹为两点之间最短驾车路线
         * SupplementMode.riding：补偿轨迹为两点之间最短骑车路线
         * SupplementMode.walking：补偿轨迹为两点之间最短步行路线
         * SupplementMode.straight：补偿轨迹为两点之间直线
         */
        historyTrackRequest.setSupplementMode(SupplementMode.no_supplement);
        historyTrackRequest.setSortType(SortType.asc);//设置返回结果的排序规则，默认升序排序；升序：集合中index=0代表起始点；降序：结合中index=0代表终点。
        historyTrackRequest.setCoordTypeOutput(CoordType.bd09ll);//设置返回结果的坐标类型，默认为百度经纬度
        /**
         * 设置是否返回纠偏后轨迹，默认不纠偏
         * true：打开轨迹纠偏，返回纠偏后轨迹;
         * false：关闭轨迹纠偏，返回原始轨迹。
         * 打开纠偏时，请求时间段内轨迹点数量不能超过2万，否则将返回错误。
         */
        historyTrackRequest.setProcessed(true);
        historyTrackRequest.setServiceId(trackApp.serviceId);//设置轨迹服务id，Trace中的id
        historyTrackRequest.setEntityName(trackApp.entityName);//Trace中的entityName
        /**
         * 设置startTime和endTime，会请求这段时间内的轨迹数据;
         * 这里查询采集开始到采集结束之间的轨迹数据
         */
        historyTrackRequest.setStartTime(beginTime);
        historyTrackRequest.setEndTime(endTime);
        historyTrackRequest.setPageIndex(pageIndex);
        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        trackApp.mClient.queryHistoryTrack(historyTrackRequest, trackListener);//发起请求，设置回调监听
    }
    /**
     * 查询历史轨迹,补偿
     */
    public void queryHistoryTrack(long beginTime,long endTime) {
        historyTrackRequest = new HistoryTrackRequest();
        ProcessOption processOption = new ProcessOption();//纠偏选项
        processOption.setRadiusThreshold(20);//精度过滤
        processOption.setTransportMode(TransportMode.walking);//交通方式，默认为驾车
        processOption.setNeedDenoise(true);//去噪处理，默认为false，不处理
        processOption.setNeedVacuate(true);//设置抽稀，仅在查询历史轨迹时有效，默认需要false
        processOption.setNeedMapMatch(false);//
        historyTrackRequest.setProcessOption(processOption);
        trackApp.initRequest(historyTrackRequest);
        historyTrackRequest.setSupplementMode(SupplementMode.no_supplement);
        historyTrackRequest.setSortType(SortType.asc);//设置返回结果的排序规则，默认升序排序；升序：集合中index=0代表起始点；降序：结合中index=0代表终点。
        historyTrackRequest.setCoordTypeOutput(CoordType.bd09ll);//设置返回结果的坐标类型，默认为百度经纬度
        historyTrackRequest.setProcessed(true);
        historyTrackRequest.setServiceId(trackApp.serviceId);//设置轨迹服务id，Trace中的id
        historyTrackRequest.setEntityName(trackApp.entityName);//Trace中的entityName
        historyTrackRequest.setStartTime(beginTime);
        historyTrackRequest.setEndTime(endTime);
        historyTrackRequest.setPageIndex(pageIndex);
        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        trackApp.mClient.queryHistoryTrack(historyTrackRequest, trackListener);//发起请求，设置回调监听
    }
    static class RealTimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
    /**
     * 注册广播（电源锁、GPS状态）
     */
    private void registerReceiver() {
        if (trackApp.isRegisterReceiver) {
            return;
        }
        if (null == wakeLock) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track upload");
        }
        if (null == trackReceiver) {
            trackReceiver = new TrackReceiver(wakeLock);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(StatusCodes.GPS_STATUS_ACTION);
        trackApp.registerReceiver(trackReceiver, filter);
        trackApp.isRegisterReceiver = true;
    }
    private void unregisterPowerReceiver() {
        if (!trackApp.isRegisterReceiver) {
            return;
        }
        if (null != trackReceiver) {
            trackApp.unregisterReceiver(trackReceiver);
        }
        trackApp.isRegisterReceiver = false;
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("生命周期","onStart");
        if (trackApp.trackConf.contains("is_trace_started")
                && trackApp.trackConf.contains("is_gather_started")
                && trackApp.trackConf.getBoolean("is_trace_started", false)
                && trackApp.trackConf.getBoolean("is_gather_started", false)) {
            startRealTimeLoc(packInterval);
            addOrRealtime=true;
            isPause=true;
           // mhandle.removeCallbacks(timeRunable);
        }
        else {
            startRealTimeLoc(Constants.LOC_INTERVAL);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();
        Log.e("生命周期","onResume");
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        // 在Android 6.0及以上系统，若定制手机使用到doze模式，请求将应用添加到白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = trackApp.getPackageName();
            boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName);
            if (!isIgnoring) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapUtil.onPause();
        Log.e("生命周期","onPause");
    }
    @Override
    protected void onStop() {
        super.onStop();
        Log.e("生命周期","onStop");
        stopRealTimeLoc();
        if (trackApp.trackConf.contains("is_trace_started")
                && trackApp.trackConf.contains("is_gather_started")
                && trackApp.trackConf.getBoolean("is_trace_started", false)
                && trackApp.trackConf.getBoolean("is_gather_started", false)) {
            mhandle.post(timeRunable);
        }
        //mhandle.post(timeRunable);
        mSensorManager.unregisterListener(this);
    }
        //计时器
    private Handler mhandle = new Handler();
    private boolean isPause = false;//是否暂停
    private boolean isNumber = true;//是否暂停


    /*****************计时器*******************/
    private Runnable timeRunable = new Runnable() {
        @Override
        public void run() {
            long  endTime = CommonUtil.getCurrentTime();
            Time = SharedPreferencesUtils.getParam(StartAndEndActivity.this, "Time", 0L);
            long count=endTime-Time;
            int m= (int) (count/60);//分
            String speak="你现在已经连续巡查"+m+"分钟了，请关闭采集";
            if(count>=30*60&&isNumber)
            {
                mixSpeakUtil.speak(speak);
                isNumber=false;
            }
            //mixSpeakUtil.speak("线程");
            if (!isPause) {
                //递归调用本runable对象，实现每隔一秒一次执行任务
                mhandle.postDelayed(this, 5*60*1000);
                isNumber=true;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("生命周期","onDestroy");
        if (null != trackPoints) {
            trackPoints.clear();
        }
        trackPoints = null;
        mapUtil.clear();
        stopRealTimeLoc();
        stopAlarm();
    }
    public boolean isOpenProvider() {
        //获取定位服务
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //获取当前可用的位置控制器
        List<String> list = locationManager.getProviders(true);

        if (list.contains(LocationManager.GPS_PROVIDER)) {
            //是否为GPS位置控制器
            provider = LocationManager.GPS_PROVIDER;
        } else {
            return false;
        }
        return true;
    }
    private void startLocationService() {
        //检查位置提供器是否开启，如果开启则给provider赋值
        if (isOpenProvider()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //绑定定位事件，监听位置是否改变
            //第一个参数为控制器类型第二个参数为监听位置变化的时间间隔（单位：毫秒）
            //第三个参数为位置变化的间隔（单位：米）第四个参数为位置监听器
            locationManager.requestLocationUpdates(provider, 10000, (float) 15,
                    locationListener);
            //监听卫星信号强度
            locationManager.addGpsStatusListener(gpsStatusListener);
        } else {

        }
    }
    private void stopLocationService() {
        //检查位置提供器是否开启，如果开启则给provider赋值
        if (isOpenProvider()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.removeGpsStatusListener(gpsStatusListener);
            editText.setText("");
        } else {
        }
    }
    /**
     * gps状态监听类,用于gps获取gps信号强弱
     */
    GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");

                    break;
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    try {
                        Log.i(TAG, "卫星状态改变");
                        //获取当前状态
                        @SuppressLint("MissingPermission") GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                        //获取卫星颗数的默认最大值
                        int maxSatellites = gpsStatus.getMaxSatellites();
                        //创建一个迭代器保存所有卫星
                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                        int totalCount = 0;
                        int validCount = 0;
                        while (iters.hasNext() && totalCount <= maxSatellites) {
                            GpsSatellite s = iters.next();
                            //已定位卫星数量
                            if (s.usedInFix()) {
                                validCount++;
                            }
                            totalCount++;
                        }
                        System.out.println("搜索到：" + totalCount + "颗卫星");
                        System.out.println("有效卫星：" + validCount + "颗");
                        String signal;
                        if(validCount<4)
                        {  signal="弱";
                            counts++;
                            Log.i("counts",String.valueOf(counts));
                            if(counts ==1 || counts>119){
                                if(counts%120==0){
                                    mixSpeakUtil.speak(" GPS信号"+signal);
                                }
                            }
                        }else {
                            signal="正常";
                            counts=0;
                        }
                        editText.setText("搜索到：" + totalCount + "颗卫星"+" 有效卫星：" + validCount + "颗"+" GPS信号："+signal);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
    //当位置发生变化时，会触发这个监听
    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            //实时获取Location 位置信息
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        /**
         * GPS开启时触发
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
//            mixSpeakUtil.speak(" GPS开启");
        }
        /**
         *  GPS禁用时触发
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {
        }
    };
    public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("LOCATION_CLOCK")) {
                Log.e("ggb", "--->>>   onReceive  LOCATION_CLOCK");
                Intent locationIntent = new Intent(context, StartAndEndActivity.class);
                context.startService(locationIntent);
            }
        }
    }
    private int  startGatherButton(){
        button = (TakePhotoButton) findViewById(R.id.normal_btn);
        button.setOnProgressTouchListener(new TakePhotoButton.OnProgressTouchListener() {
            @Override
            public void onClick(TakePhotoButton photoButton) {
                Toast.makeText(StartAndEndActivity.this,"长按开始/结束",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onLongClick(TakePhotoButton photoButton) {
                button.start();
            }
            @Override
            public void onFinish() {
                if (trackApp.isGatherStarted) {
                    //停止采集
                    trackApp.mClient.stopTrace(trackApp.mTrace, null);
                    trackApp.mClient.stopGather(traceListener);
                    Toast.makeText(StartAndEndActivity.this, "关闭服务中，请稍后...", Toast.LENGTH_LONG).show();
                    setStopGatherStyle();
                }
                else {
                    //开始采集
                    if (!isOpenProvider()) {
                        Toast.makeText(StartAndEndActivity.this, "请检查网络或GPS是否打开", Toast.LENGTH_LONG).show();
                        return;
                    }
                    trackApp.mClient.startTrace(trackApp.mTrace, traceListener);//开始服务
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Thread.sleep(2000);//休眠2秒
                                trackApp.mClient.setInterval(Constants.DEFAULT_GATHER_INTERVAL, packInterval);
                                trackApp.mClient.startGather(traceListener);//开启采集
                                setStartGatherStyle();
                                Time = CommonUtil.getCurrentTime();
                                SharedPreferencesUtils.setParam(StartAndEndActivity.this, "Time", Time);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }.start();
                }
            }
        });
        return 1;
    }
    //结束按钮
    private void setStartGatherStyle(){
        button.outCircleColor =  Color.parseColor("#F10D27");
        button.innerCircleColor =  Color.parseColor("#F10D27");
        button.progressColor = Color.parseColor("#00FFFF");
        button.setTextColor(0xFFFFFFFF);
        button.setTextstr("结束采集");
    }
    //开始按钮样式
    private void setStopGatherStyle(){
        button.outCircleColor =  Color.parseColor("#F10D27");
        button.innerCircleColor =  Color.parseColor("#ffffffff");
        button.progressColor = Color.parseColor("#00FFFF");
        button.setTextColor(0xFFFF0000);
        button.setTextstr("开始采集");
    }
    /**
     * 关闭Alarm
     */
    public void stopAlarm() {
        Intent intent = new Intent("LOCATION_CLOCK");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        try {
            alarmManager.cancel(pendingIntent);
            Log.d("销毁", "Alarm is Canceled.");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("销毁", "Alarm is not Canceled: " + e.toString());
        }
    }
}
