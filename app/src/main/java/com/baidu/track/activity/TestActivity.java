package com.baidu.track.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.LinearLayout;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.DistanceRequest;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.SupplementMode;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.baidu.trace.model.TransportMode;
import com.baidu.track.R;
import com.baidu.track.TrackApplication;
import com.baidu.track.model.CurrentLocation;
import com.baidu.track.utils.CommonUtil;
import com.baidu.track.utils.Constants;
import com.baidu.track.utils.MapUtil;
import com.baidu.track.utils.ViewUtil;

import java.util.ArrayList;
import java.util.List;
/**
 * 轨迹查询
 */
public class TestActivity extends ControlActivity

        implements View.OnClickListener {
    private TrackApplication trackApp = null;

    private ViewUtil viewUtil = null;


    /**
     * 地图工具
     */

    private MapUtil mapUtil = null;


    /**
     * 历史轨迹请求
     */

    private HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest();


    /**
     * 轨迹监听器（用于接收历史轨迹回调）
     */

    private OnTrackListener mTrackListener = null;


    /**
     * 查询轨迹的开始时间
     */

    private long startTime = CommonUtil.getCurrentTime();


    /**
     * 查询轨迹的结束时间
     */

    private long endTime = CommonUtil.getCurrentTime();


    /**
     * 轨迹点集合
     */

    private List<LatLng> trackPoints = new ArrayList<>();


    /**
     * 轨迹排序规则
     */

    private SortType sortType = SortType.asc;


    private int pageIndex = 1;

    private OnEntityListener entityListener = null;
    private PowerManager powerManager = null;
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setTitle(R.string.track_query_title);

        setOptionsText();

        setOnClickListener(this);

        trackApp = (TrackApplication) getApplicationContext();

        init();

    }


    public void setOptionsText() {

        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_top);

//        TextView textView = (TextView) layout.findViewById(R.id.tv_options);

//        textView.setText("查询条件设置");

    }


    /**
     * 初始化
     */

    private void init() {
//        // apikey的授权需要一定的时间，在授权成功之前地图相关操作会出现异常；apikey授权成功后会发送广播通知，我们这里注册 SDK 广播监听者
//        IntentFilter iFilter = new IntentFilter();
//        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
//        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
//        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
//        mReceiver = new TestActivity.SDKReceiver();
//        registerReceiver(mReceiver, iFilter);
        viewUtil = new ViewUtil();

        mapUtil = MapUtil.getInstance();

        mapUtil.init((MapView) findViewById(R.id.track_query_mapView));
        powerManager = (PowerManager) trackApp.getSystemService(Context.POWER_SERVICE);
        initListener();


    }


    /**
     * 轨迹查询设置回调
     *
     * @param historyTrackRequestCode
     * @param resultCode
     * @param data
     */

    @Override

    protected void onActivityResult(int historyTrackRequestCode, int resultCode, Intent data) {

        if (null == data) {

            return;

        }


        trackPoints.clear();

        pageIndex = 1;


        if (data.hasExtra("startTime")) {

            startTime = data.getLongExtra("startTime", CommonUtil.getCurrentTime());

        }

        if (data.hasExtra("endTime")) {

            endTime = data.getLongExtra("endTime", CommonUtil.getCurrentTime());

        }


        ProcessOption processOption = new ProcessOption();

        if (data.hasExtra("radius")) {

            processOption.setRadiusThreshold(data.getIntExtra("radius", Constants.DEFAULT_RADIUS_THRESHOLD));

        }

        processOption.setTransportMode(TransportMode.walking);


        if (data.hasExtra("denoise")) {//去噪

            processOption.setNeedDenoise(data.getBooleanExtra("denoise", true));

        }

        if (data.hasExtra("vacuate")) {//抽稀

            processOption.setNeedVacuate(data.getBooleanExtra("vacuate", true));

        }

        if (data.hasExtra("mapmatch")) {//绑路

            processOption.setNeedMapMatch(data.getBooleanExtra("mapmatch", true));

        }

        historyTrackRequest.setProcessOption(processOption);


        if (data.hasExtra("processed")) {//纠偏

            historyTrackRequest.setProcessed(data.getBooleanExtra("processed", true));

        }


        queryHistoryTrack();

    }


    /**
     * 查询历史轨迹
     */

    private void queryHistoryTrack() {

        trackApp.initRequest(historyTrackRequest);

        historyTrackRequest.setSupplementMode(SupplementMode.no_supplement);

        historyTrackRequest.setSortType(SortType.asc);

        historyTrackRequest.setCoordTypeOutput(CoordType.bd09ll);

        historyTrackRequest.setEntityName(trackApp.entityName);

        historyTrackRequest.setStartTime(startTime);

        historyTrackRequest.setEndTime(endTime);

        historyTrackRequest.setPageIndex(pageIndex);

        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);

        trackApp.mClient.queryHistoryTrack(historyTrackRequest, mTrackListener);

    }


    /**
     * 按钮点击事件
     *
     * @param view
     */

    @Override

    public void onClick(View view) {

        switch (view.getId()) {

            // 轨迹查询选项

            case R.id.btn_activity_options:

                ViewUtil.startActivityForResult(this, TrackQueryOptionsActivity.class, Constants.REQUEST_CODE);

                break;


            default:

                break;

        }

    }


    private void initListener() {

        mTrackListener = new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                try {
                    int total = response.getTotal();
                    if (StatusCodes.SUCCESS != response.getStatus()) {
                        viewUtil.showToast(TestActivity.this, response.getMessage());
                    } else if (0 == total) {
                        viewUtil.showToast(TestActivity.this, getString(R.string.no_track_data));
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
                        queryHistoryTrack();
                    } else {
                        mapUtil.drawHistoryTrack(trackPoints, true, 0);//画轨迹
                    }
                    queryDistance();// 查询里程
                } catch (Exception e) {
                }
            }

            @Override
            public void onDistanceCallback(DistanceResponse response) {
                viewUtil.showToast(TestActivity.this, "里程：" + response.getDistance());
                super.onDistanceCallback(response);
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
                        mapUtil.updateMapLocation(currentLatLng, 0);//显示当前位置
                        mapUtil.animateMapStatus(currentLatLng);//缩放
                    }

                } catch (Exception x) {

                }


            }

        };
//            traceListener = new OnTraceListener() {
//
//                @Override
//            public void onBindServiceCallback(int errorNo, String message) {
//                viewUtil.showToast(TestActivity.this,
//                        String.format("onBindServiceCallback, errorNo:%d, message:%s ", errorNo, message));
//            }
//
//            @Override
//            public void onStartTraceCallback(int errorNo, String message) {
//                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
//                    trackApp.isTraceStarted = true;
//                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
//                    editor.putBoolean("is_trace_started", true);
//                    editor.apply();
//                }
//                viewUtil.showToast(TestActivity.this,
//                        String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message));
//            }
//            @Override
//            public void onStopTraceCallback(int errorNo, String message) {
//
//            }
//
//            @Override
//            public void onStartGatherCallback(int errorNo, String message) {
//
//            }
//
//            @Override
//            public void onStopGatherCallback(int errorNo, String message) {
//
//            }
//
//            @Override
//            public void onPushCallback(byte messageType, PushMessage pushMessage) {
//
//            }
//
//        };


    }


    private void queryDistance() {

        DistanceRequest distanceRequest = new DistanceRequest(trackApp.getTag(), trackApp.serviceId, trackApp.entityName);

        distanceRequest.setStartTime(startTime);// 设置开始时间

        distanceRequest.setEndTime(endTime);// 设置结束时间

        distanceRequest.setProcessed(true);// 纠偏

        ProcessOption processOption = new ProcessOption();// 创建纠偏选项实例

        processOption.setNeedDenoise(true);// 去噪

        processOption.setNeedMapMatch(true);// 绑路

        processOption.setTransportMode(TransportMode.walking);// 交通方式为步行

        distanceRequest.setProcessOption(processOption);// 设置纠偏选项

        distanceRequest.setSupplementMode(SupplementMode.no_supplement);// 里程填充方式为无

        trackApp.mClient.queryDistance(distanceRequest, mTrackListener);// 查询里程


    }


    @Override

    protected void onResume() {

        super.onResume();

        mapUtil.onResume();

    }


    @Override

    protected void onPause() {

        super.onPause();

        mapUtil.onPause();

    }


    @Override

    protected void onDestroy() {

        super.onDestroy();


        if (null != trackPoints) {

            trackPoints.clear();

        }


        trackPoints = null;

        mapUtil.clear();


    }


    @Override

    protected int getContentViewId() {

        return R.layout.activity_trackquery;

    }

//    private OnTraceListener traceListener = null;
//    private TestActivity.RealTimeLocRunnable realTimeLocRunnable = null;
//    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;
//    private TestActivity.RealTimeHandler realTimeHandler = new TestActivity.RealTimeHandler();
//    private OnTrackListener trackListener = null;
//    private OnEntityListener entityListener = null;
//    private TestActivity.SDKReceiver mReceiver;
//    private PowerManager.WakeLock wakeLock = null;
//    private MyService trackReceiver = null;
//    private PowerManager powerManager = null;
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // 适配android M，检查权限
//        List<String> permissions = new ArrayList<>();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
//            requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
//        }
//        if (trackApp.trackConf.contains("is_trace_started")
//                && trackApp.trackConf.contains("is_gather_started")
//                && trackApp.trackConf.getBoolean("is_trace_started", false)
//                && trackApp.trackConf.getBoolean("is_gather_started", false)) {
//            startRealTimeLoc(packInterval);
//        } else {
//            startRealTimeLoc(Constants.LOC_INTERVAL);
//        }
//
//    }
//
//    private boolean isNeedRequestPermissions(List<String> permissions) {
//        // 定位精确位置
//        addPermission(permissions, Manifest.permission.ACCESS_FINE_LOCATION);
//        // 存储权限
//        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        // 读取手机状态
//        addPermission(permissions, Manifest.permission.READ_PHONE_STATE);
//        return permissions.size() > 0;
//    }
//
//    private void addPermission(List<String> permissionsList, String permission) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
//                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
//            permissionsList.add(permission);
//        }
//    }
//
//    /**
//     * 实时定位任务
//     */
//    class RealTimeLocRunnable implements Runnable {
//
//        private int interval = 0;
//
//        public RealTimeLocRunnable(int interval) {
//            this.interval = interval;
//        }
//
//        @Override
//        public void run() {
//            trackApp.getCurrentLocation(entityListener, trackListener);
//            realTimeHandler.postDelayed(this, interval * 1000);
//        }
//    }
//
//    public void startRealTimeLoc(int interval) {
//        realTimeLocRunnable = new TestActivity.RealTimeLocRunnable(interval);
//        realTimeHandler.post(realTimeLocRunnable);
//    }
//
//    public void stopRealTimeLoc() {
//        if (null != realTimeHandler && null != realTimeLocRunnable) {
//            realTimeHandler.removeCallbacks(realTimeLocRunnable);
//        }
//        trackApp.mClient.stopRealTimeLoc();
//    }
//
//    static class RealTimeHandler extends Handler {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    }
//    public class SDKReceiver extends BroadcastReceiver {
//
//        public void onReceive(Context context, Intent intent) {
//            String s = intent.getAction();
//
//            if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
//                Toast.makeText(TestActivity.this,"apikey验证失败，地图功能无法正常使用",Toast.LENGTH_SHORT).show();
//            } else if (s.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
//                Toast.makeText(TestActivity.this,"apikey验证成功",Toast.LENGTH_SHORT).show();
//            } else if (s.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
//                Toast.makeText(TestActivity.this,"网络错误",Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//    /**
//     * 注册广播（电源锁、GPS状态）
//     */
//    @SuppressLint("InvalidWakeLockTag")
//    private void registerReceiver() {
//        if (trackApp.isRegisterReceiver) {
//            return;
//        }
//
//        if (null == wakeLock) {
//            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track upload");
//        }
//        if (null == trackReceiver) {
//            trackReceiver = new MyService(wakeLock);
//        }
//
//        IntentFilter filter = new IntentFilter();
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        filter.addAction(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_USER_PRESENT);
//        filter.addAction(StatusCodes.GPS_STATUS_ACTION);
//        trackApp.registerReceiver(trackReceiver, filter);
//        trackApp.isRegisterReceiver = true;
//
//    }
//
//    private void unregisterPowerReceiver() {
//        if (!trackApp.isRegisterReceiver) {
//            return;
//        }
//        if (null != trackReceiver) {
//            trackApp.unregisterReceiver(trackReceiver);
//        }
//        trackApp.isRegisterReceiver = false;
//    }

}