package com.baidu.track;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.LocRequest;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.LatestPointRequest;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.BaseRequest;
import com.baidu.trace.model.OnCustomAttributeListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.TransportMode;
import com.baidu.track.activity.StartAndEndActivity;
import com.baidu.track.utils.CommonUtil;
import com.baidu.track.utils.NetUtil;
import com.baidu.track.utils.SharedPreferencesUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhh
 */

public class TrackApplication extends Application {

    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    private LocRequest locRequest = null;

    public Context mContext = null;

    public SharedPreferences trackConf = null;

    /**
     * 轨迹客户端
     */
    public LBSTraceClient mClient = null;

    /**
     * 轨迹服务
     */
    public Trace mTrace = null;

    /**
     * 轨迹服务ID
     */
    public long serviceId = 216815;//这里是申请的鹰眼服务id

    /**
     * Entity标识
     * 设备序列号
     */
    private String androidId;
    public String entityName = "myTrace";

    public boolean isRegisterReceiver = false;

    /**
     * 服务是否开启标识
     */
    public boolean isTraceStarted = false;

    /**
     * 采集是否开启标识
     */
    public boolean isGatherStarted = false;

    public static int screenWidth = 0;

    public static int screenHeight = 0;

    private String name;


    private Notification notification = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();


        registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

        androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        entityName = androidId;

        Log.e("用户名唯一标识",entityName);
        // 若为创建独立进程，则不初始化成员变量
        if ("com.baidu.track:remote".equals(CommonUtil.getCurProcessName(mContext))) {
            return;
        }

        SDKInitializer.initialize(mContext);
        getScreenSize();
        mClient = new LBSTraceClient(mContext);
        mTrace = new Trace(serviceId, entityName);

        trackConf = getSharedPreferences("track_conf", MODE_PRIVATE);
        locRequest = new LocRequest(serviceId);


        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
        name= sp.getString("name","");

        mClient.setOnCustomAttributeListener(new OnCustomAttributeListener() {
            @Override
            public Map<String, String> onTrackAttributeCallback() {
                Map<String, String> map = new HashMap<>();
                map.put("desc_name", name);
                map.put("key2", "value2");
                return map;
            }
        });
        mTrace.setNotification(createNotification());
        clearTraceStatus();
    }

    /**
     * 获取当前位置
     */
    public void getCurrentLocation(OnEntityListener entityListener, OnTrackListener trackListener) {
        // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位
        if (NetUtil.isNetworkAvailable(mContext)
                && trackConf.contains("is_trace_started")
                && trackConf.contains("is_gather_started")
                && trackConf.getBoolean("is_trace_started", false)
                && trackConf.getBoolean("is_gather_started", false)) {
            LatestPointRequest request = new LatestPointRequest(getTag(), serviceId, entityName);
            ProcessOption processOption = new ProcessOption();
            // 设置精度过滤值(定位精度大于100米的过滤掉)
            processOption.setRadiusThreshold(20);
            processOption.setTransportMode(TransportMode.walking);
            processOption.setNeedDenoise(true);
            processOption.setNeedMapMatch(false);
            request.setProcessOption(processOption);
            mClient.queryLatestPoint(request, trackListener);
        } else {
            mClient.queryRealTimeLoc(locRequest, entityListener);
        }
    }
    private String userName;

    private static final String CHANNEL_ID_SERVICE_RUNNING = "CHANNEL_ID_SERVICE_RUNNING";
    /**
     * 在8.0以上手机，如果app切到后台，系统会限制定位相关接口调用频率
     * 可以在启动轨迹上报服务时提供一个通知，这样Service启动时会使用该通知成为前台Service，可以避免此限制
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification createNotification() {
//        SharedPreferences sp=getSharedPreferences("loginInfo", MODE_PRIVATE);
//        userName = sp.getString("name","");
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_SERVICE_RUNNING, "app service", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
            builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID_SERVICE_RUNNING);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        Intent nfIntent = new Intent(this, StartAndEndActivity.class);
        nfIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle("巡查任务")
                .setContentText("巡查进行中");
        Notification notification = builder.build();
        return notification;
    }



    /**
     * 获取屏幕尺寸
     */
    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
    }

    /**
     * 清除Trace状态：初始化app时，判断上次是正常停止服务还是强制杀死进程，根据trackConf中是否有is_trace_started字段进行判断。
     *
     * 停止服务成功后，会将该字段清除；若未清除，表明为非正常停止服务。
     */
    private void clearTraceStatus() {
        if (trackConf.contains("is_trace_started") || trackConf.contains("is_gather_started")) {
            SharedPreferences.Editor editor = trackConf.edit();
            editor.remove("is_trace_started");
            editor.remove("is_gather_started");
            editor.apply();
        }
    }

    /**
     * 初始化请求公共参数
     *
     * @param request
     */
    public void initRequest(BaseRequest request) {
        request.setTag(getTag());
        request.setServiceId(serviceId);
    }

    /**
     * 获取请求标识
     *
     * @return
     */
    public int getTag() {
        return mSequenceGenerator.incrementAndGet();
    }




    /**
     * 当前Acitity个数
     */
    private int activityAount = 0;
    public boolean isForeground = false;

    /**
     * Activity 生命周期监听，用于监控app前后台状态切换
     */
    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (activityAount == 0) {
                //app回到前台
                isForeground = true;
                Log.e("应用状态","在前台");
                long frontTime= CommonUtil.getCurrentTime();
                SharedPreferencesUtils.setParam(mContext, "frontTime", frontTime);//保存切换应用时间到SharedPreferences中
                Log.e("应用状态", String.valueOf(frontTime));
            }
            activityAount++;
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityAount--;
            if (activityAount == 0) {
                isForeground = false;
                System.out.println("在后台");
                Log.e("应用状态","在后台");
                long groundTime= CommonUtil.getCurrentTime();
                SharedPreferencesUtils.setParam(mContext, "groundTime", groundTime);//保存切换应用时间到SharedPreferences中
                Log.e("应用状态", String.valueOf(groundTime));
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

}
