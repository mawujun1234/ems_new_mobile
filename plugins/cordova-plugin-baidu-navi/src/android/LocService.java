package com.mawujun.navi;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.mawujun.mobile.gps.model.Constants;
import com.mawujun.mobile.gps.model.GpsMsg;
import com.mawujun.mobile.gps.model.MsgType;

import org.apache.cordova.LOG;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static com.baidu.location.LocationClient.getBDLocationInCoorType;
import static com.baidu.location.h.g.aa;

/**
 * 为了能在后台定时的发送gps数据到服务器
 *
 *
 * Created by mawujun on 2017/4/17.
 */

public class LocService extends Service {
    public LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;
    //播放定位信息
    private Intent intent = new Intent("com.mawujun.navi.RECEIVER");
    NettyClientBootstrap nettyClientBootstrap=new NettyClientBootstrap();
    /**
     * 实现实时位置回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location!=null){

                //广播到导航的界面去，
                intent.putExtra("longitude", location.getLongitude());
                intent.putExtra("latitude", location.getLatitude());
                sendBroadcast(intent);

                Date loc_time=new Date();
                Double distance=0.0;
                Long loc_time_interval=0L;
                if(LocCurrent.getLongitude()!=null && LocCurrent.getLatitude()!=null){
                    //上次的经纬度
                    LatLng pt1 = new LatLng(LocCurrent.getLatitude(), LocCurrent.getLongitude());
                    //本次经纬度
                    LatLng pt2 = new LatLng(location.getLatitude(), location.getLongitude());
                    //计算p1、p2两点之间的直线距离，单位：米
                    distance= DistanceUtil. getDistance(pt1, pt2);

                    loc_time_interval=loc_time.getTime()-LocCurrent.getLoc_time();
                }

                LocCurrent.setLongitude( location.getLongitude());
                LocCurrent.setLatitude(location.getLatitude());
                LocCurrent.setLoc_time(loc_time.getTime());

                //把gps经纬度转换成百度经纬度，因为web上用到的坐标多是百度坐标
                BDLocation bad06ll_location=wgs84_gcj02_bad06ll(location.getLongitude(),location.getLatitude());

                //把经纬度数据上传到服务器中去
                //http://www.cnblogs.com/chaoxiyouda/p/5432216.html
                GpsMsg gpsMsg=new GpsMsg();
                gpsMsg.setType(MsgType.GPS);
                gpsMsg.setUuid(Constants.getUuid());
                gpsMsg.setSessionId(Constants.getClientId());
                gpsMsg.setLoginName(Constants.getLoginName());
                gpsMsg.setUser_id(Constants.getUser_id());

                gpsMsg.setLongitude(bad06ll_location.getLongitude()+"");
                gpsMsg.setLongitude_orgin( location.getLongitude()+"");
                gpsMsg.setLatitude(bad06ll_location.getLatitude()+"");
                gpsMsg.setLatitude_orgin(location.getLatitude()+"");

                gpsMsg.setAltitude((location.getAltitude()==Double.MIN_VALUE?0:location.getAltitude()));
                gpsMsg.setRadius(location.getRadius());
                gpsMsg.setDirection(location.getDirection());
                gpsMsg.setSpeed(location.getSpeed());
                gpsMsg.setDistance(distance);
                gpsMsg.setLoc_type(location.getLocType()+"");
                gpsMsg.setLoc_time(loc_time);
                gpsMsg.setLoc_time_interval(loc_time_interval);
                gpsMsg.setGps_interval(Constants.getGps_interval());
                //aa.setFailInfo("gps数据："+bad06ll_location.getLongitude()+"---"+bad06ll_location.getLatitude());
                nettyClientBootstrap.push(gpsMsg);
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    /**
     * GPS全球卫星定位系统使用的坐标系
     * 把标准的gps坐标转换成百度的坐标
     * @param wgs84_longitude
     * @param wgs84_latitude
     * @return
     */
    private BDLocation wgs84_gcj02_bad06ll(Double wgs84_longitude,Double wgs84_latitude){
        BDLocation wgs84=new BDLocation();
        wgs84.setLatitude(wgs84_longitude);
        wgs84.setLongitude(wgs84_latitude);
        BDLocation gcj02=LocationClient.getBDLocationInCoorType(wgs84, BDLocation.BDLOCATION_WGS84_TO_GCJ02);
        Log.w("11111国标",gcj02.getLongitude()+"===="+gcj02.getLatitude());
        BDLocation bad06ll=LocationClient.getBDLocationInCoorType(gcj02,BDLocation.BDLOCATION_GCJ02_TO_BD09LL);
        Log.w("11111百度",bad06ll.getLongitude()+"===="+bad06ll.getLatitude());
        return bad06ll;
    }
    public void initLocation(BNRoutePlanNode.CoordinateType coordinateType) {
        //配置参数是可以每次定位的时候都不同的
        LocationClientOption option = new LocationClientOption();
        //高精度定位模式：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
        //低功耗定位模式：这种定位模式下，不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）；
        //仅用设备定位模式：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);////可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setScanSpan(Constants.getGps_interval());//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        //option.setScanSpan(0);
        option.setIsNeedAddress(false);//可选，设置是否需要地址信息，默认不需要
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(false);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        //option.disableCache(true);

        option.setCoorType(coordinateType.toString());// 返回的定位结果是百度经纬度，默认值gcj02,//wgs84:国际经纬度坐标  "gcj02":国家测绘局标准,"bd09ll":百度经纬度标准,"bd09":百度墨卡托标准
        option.setProdName("BaiduLoc");
        mLocationClient.setLocOption(option);

    }

    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    public void onCreate() {
        Log.i(BNDemoMainActivity.TAG, "初始化LocationApplication!");
        super.onCreate();

        //onCreate(getBaseContext());
        mLocationClient = new LocationClient(this.getBaseContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        //this.activityContex=context;
        //不初始化，不能计算距离
        SDKInitializer.initialize(this.getBaseContext().getApplicationContext());
        //handler = new Handler(Looper.getMainLooper());


    }
    private BNRoutePlanNode.CoordinateType mCoordinateType = null;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(BNDemoMainActivity.TAG, "启动LocationApplication!");

        boolean isconnected=nettyClientBootstrap.connected(Constants.getClientId());
        if(isconnected) {
            mCoordinateType = BNRoutePlanNode.CoordinateType.valueOf(intent.getStringExtra("coordinateType"));
            //Log.w("22222",mCoordinateType.toString());
            initLocation(mCoordinateType);//BNRoutePlanNode.CoordinateType.WGS84

            mLocationClient.start();
            mLocationClient.requestLocation();
        } else {
            Toast.makeText(this.getBaseContext(), "连接失败，不能连接后台gps服务器!", Toast.LENGTH_SHORT).show();
        }


        //toast("开始获取gps信息了", Toast.LENGTH_LONG);
        //LOG.i(BNDemoMainActivity.TAG, "开始获取gps信息了================================================");
        //return super.onStartCommand(intent, flags,startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mLocationClient != null && mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
        super.onDestroy();
    }


}
