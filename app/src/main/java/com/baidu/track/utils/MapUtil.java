package com.baidu.track.utils;

import android.graphics.Color;
import android.graphics.Path;

import com.baidu.mapapi.map.ArcOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.model.inner.Point;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.TraceLocation;
import com.baidu.track.model.CurrentLocation;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.track.model.CurrentLocation.longitude;
import static com.baidu.track.utils.BitmapUtil.bmEnd;
import static com.baidu.track.utils.BitmapUtil.bmStart;

/**
 * 地图工具类
 * Created by zhh .
 */

public class MapUtil {

    private static MapUtil INSTANCE = new MapUtil();
    private MapStatus mapStatus = null;
    private Marker mMoveMarker = null;
    public MapView mapView = null;
    public BaiduMap baiduMap = null;
    public LatLng lastPoint = null;
    private MyLocationData locData;

    /**
     * 路线覆盖物
     */
    public Overlay polylineOverlay = null;

    private float mCurrentZoom = 18.0f;

    private MapUtil() {
    }

    public static MapUtil getInstance() {
        return INSTANCE;
    }

    public void init(MapView view) {
        mapView = view;
        baiduMap = mapView.getMap();
        mapView.showZoomControls(false);
        baiduMap.setMyLocationEnabled(true);

        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING,true,null));
        baiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {//缩放比例变化监听
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
                mCurrentZoom = mapStatus.zoom;
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {

            }
        });
    }

    public void onPause() {
        if (null != mapView) {
            mapView.onPause();
        }
    }

    public void onResume() {
        if (null != mapView) {
            mapView.onResume();
        }
    }

    public void clear() {
        lastPoint = null;
        if (null != mMoveMarker) {
            mMoveMarker.remove();
            mMoveMarker = null;
        }
        if (null != polylineOverlay) {
            polylineOverlay.remove();
            polylineOverlay = null;
        }
        if (null != baiduMap) {
            baiduMap.clear();
            baiduMap = null;
        }
        mapStatus = null;
        if (null != mapView) {
            mapView.onDestroy();
            mapView = null;
        }
    }

    /**
     * 将轨迹实时定位点转换为地图坐标
     */
    public static LatLng convertTraceLocation2Map(TraceLocation location) {
        if (null == location) {
            return null;
        }
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if (Math.abs(latitude - 0.0) < 0.000001 && Math.abs(longitude - 0.0) < 0.000001) {
            return null;
        }
        LatLng currentLatLng = new LatLng(latitude, longitude);
        if (CoordType.wgs84 == location.getCoordType()) {
            LatLng sourceLatLng = currentLatLng;
            CoordinateConverter converter = new CoordinateConverter();
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(sourceLatLng);
            currentLatLng = converter.convert();
        }
        return currentLatLng;
    }

    /**
     * 将轨迹坐标对象转换为地图坐标对象
     */
    public static LatLng convertTrace2Map(com.baidu.trace.model.LatLng traceLatLng) {
        return new LatLng(traceLatLng.latitude, traceLatLng.longitude);
    }

    /**
     * 设置地图中心：使用已有定位信息；
     */
    public void setCenter(float direction) {
        if (!CommonUtil.isZeroPoint(CurrentLocation.latitude, longitude)) {
            LatLng currentLatLng = new LatLng(CurrentLocation.latitude, longitude);
            updateMapLocation(currentLatLng, direction);
            animateMapStatus(currentLatLng);
            return;
        }
    }

    public void updateMapLocation(LatLng currentPoint,float direction) {

        if(currentPoint == null){
            return;
        }

        locData = new MyLocationData.Builder().accuracy(0).
                        direction(direction).
                        latitude(currentPoint.latitude).
                        longitude(currentPoint.longitude).build();
        baiduMap.setMyLocationData(locData);

    }

    /**
     * 绘制历史轨迹
     */
    public void drawHistoryTrack(List<LatLng> points,boolean staticLine,float direction) {
        // 绘制新覆盖物前，清空之前的覆盖物
        baiduMap.clear();
        if (points == null || points.size() == 0) {
            if (null != polylineOverlay) {
                polylineOverlay.remove();
                polylineOverlay = null;
            }
            return;
        }

        if (points.size() == 1) {
            OverlayOptions startOptions = new MarkerOptions().position(points.get(0)).icon(bmStart)
                    .zIndex(9).draggable(true);
            baiduMap.addOverlay(startOptions);
            updateMapLocation(points.get(0),direction);
            animateMapStatus(points.get(0));
            return;
        }

        LatLng startPoint = points.get(0);
        LatLng endPoint = points.get(points.size() - 1);

        // 添加起点图标
        OverlayOptions startOptions = new MarkerOptions()
                .position(startPoint).icon(BitmapUtil.bmStart)
                .zIndex(9).draggable(true);

        // 添加路线（轨迹）
        OverlayOptions polylineOptions = new PolylineOptions().width(13)
                .color(Color.BLUE).points(points);



//        for (int i = 0; i < points.size() - 1; i++)
//        {
//            // 添加弧线坐标数据
//            LatLng p1 =  points.get(i);//起点
//            LatLng p3 = points.get(i+1);//终点
//
//           double d1=(  points.get(i).latitude + p3.latitude) / 2;
//           double d2=(  points.get(i).longitude + p3.longitude) / 2;
//            LatLng p2 = new LatLng(d1,d2);//中间点
//
//            //构造ArcOptions对象
//            OverlayOptions mArcOptions = new ArcOptions()
//                    .color(Color.RED)
//                    .width(10)
//                    .points(p1, p2, p3);
//
//            //在地图上显示弧线
//            polylineOverlay = baiduMap.addOverlay(mArcOptions);
//            // 获取一系列点的曲线路径
//
//
//        }
        if(staticLine){
            // 添加终点图标
            drawEndPoint(endPoint);
        }

        baiduMap.addOverlay(startOptions);
        polylineOverlay = baiduMap.addOverlay(polylineOptions);

        if(staticLine){
            animateMapStatus(points);
        }else{
            updateMapLocation(points.get(points.size() - 1),direction);
            animateMapStatus(points.get(points.size() - 1));
        }


    }

    public void drawEndPoint(LatLng endPoint) {
        // 添加终点图标
        OverlayOptions endOptions = new MarkerOptions().position(endPoint)
                .icon(bmEnd).zIndex(9).draggable(true);
        baiduMap.addOverlay(endOptions);
    }

    public void animateMapStatus(List<LatLng> points) {
        if (null == points || points.isEmpty()) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }
        MapStatusUpdate msUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build());
        baiduMap.animateMapStatus(msUpdate);
    }

    public void animateMapStatus(LatLng point) {
        MapStatus.Builder builder = new MapStatus.Builder();
        mapStatus = builder.target(point).zoom(mCurrentZoom).build();
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }








//    private Path getPointCurvePath(List<Point> points)	{
//        Point startp;
//        Point endp;
//        Point p3 = new Point();
//        Point p4 = new Point();
//        Path path = new Path();
//        if (null == points || 0 == points.size())
//        {
//            return path;
//        }
//        //startp点集合points的第一个点
//        startp = points.get(0);
//        //路径移到第一个点
//        path.moveTo(startp.x, startp.y);
//        int xCenter, yCenter;
//        //循环遍历点集合points
//        for (int i = 0; i < points.size() - 1; i++)
//        {
//            //起始点移到下一个点，第i点
//            startp = points.get(i);
//            //结束点变为起始点的下一个点
//            endp = points.get(i+1);
//            //中间点的横坐标是起始点和结束点的横坐标的中间
//            xCenter = (int)(startp.getmPtx() + endp.getmPtx()) / 2;
//            //中间点的纵坐标同理
//            yCenter = (int)(startp.getmPty() + endp.getmPty()) / 2;
//            //p3的纵坐标是起始点的纵坐标，p3的横坐标是中间点的横坐标
//            p3.y = startp.y;
//            p3.x = xCenter;
//            //p4的纵坐标是结束点的纵坐标，p4的横坐标是中间点的横坐标
//            p4.y = endp.y;
//            p4.x = xCenter;
//            // 确定曲线的路径
//            path.cubicTo(p3.x, p3.y, p4.x, p4.y, endp.x, endp.y);//cubicTo 同样是用来实现贝塞尔曲线的。mPath.cubicTo(x1, y1, x2, y2, x3, y3) (x1,y1) 为控制点，(x2,y2)为控制点，(x3,y3) 为结束点。
//            path.quadTo(xCenter, yCenter,  endp.x, endp.y);//quadTo 用于绘制圆滑曲线，即贝塞尔曲线。mPath.quadTo(x1, y1, x2, y2) (x1,y1) 为控制点，(x2,y2)为结束点。
//        }
//        return path;
//    }
}

