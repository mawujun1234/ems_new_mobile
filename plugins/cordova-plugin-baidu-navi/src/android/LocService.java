package com.mawujun.navi;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.navisdk.adapter.BNRoutePlanNode;

import org.apache.cordova.LOG;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 为了能在后台定时的发送gps数据到服务器
 * Created by mawujun on 2017/4/17.
 */

public class LocService extends Service {
    public LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;

    /**
     * 实现实时位置回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.w("11111","1111"+location);

//            //JSONObject jsonObj = new JSONObject();
//            if (location == null) {
//                //Toast.makeText(activityContex, "没有获取到经纬度数据!", Toast.LENGTH_LONG).show();
//                String msg="没有获取到经纬度数据!";
//                LOG.e(BaiduMapAll.LOG_TAG, msg);
//                toast(msg,Toast.LENGTH_LONG);
//                return;
//            }
//            //toast(location.getRadius()+"",Toast.LENGTH_LONG);
//            //if (location.getLocType() != BDLocation.TypeGpsLocation) {// 网络定位结果
//
//
//            Log.i(BaiduMapAll.LOG_TAG, "接收到定位信息!!"+location.getLocType());
//            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
//                //toast("开始获取gps信息了", Toast.LENGTH_SHORT);
//                postCoords(location);
//
//            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
//                //toast("开始获取gps信息了", Toast.LENGTH_SHORT);
//                postCoords(location);
//            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
//                Log.i(BaiduMapAll.LOG_TAG, "离线定位成功，离线定位结果也是有效的!!");
//                postCoords(location);
//                // toast("离线定位成功，离线定位结果也是有效的",Toast.LENGTH_LONG);
//            } else if (location.getLocType() == BDLocation.TypeServerError) {
//                toast("服务端网络定位失败", Toast.LENGTH_LONG);
//            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
//                toast("网络不通导致定位失败，请检查网络是否通畅",Toast.LENGTH_LONG);
//            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
//                toast("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机",Toast.LENGTH_LONG);
//            }else {
//                int locationType = location.getLocType();
//                String msg="获取地址失败:"+getErrorMessage(locationType);
//                //MyLog.i(BaiduMapAll.LOG_TAG, msg);
//                LOG.e(BaiduMapAll.LOG_TAG, msg);
//                //Toast.makeText(activityContex, "获取地址失败:"+getErrorMessage(locationType), Toast.LENGTH_LONG).show();
//                toast(msg,Toast.LENGTH_LONG);
//            }

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }


    }
    public void initLocation(BNRoutePlanNode.CoordinateType coordinateType) {
        //配置参数是可以每次定位的时候都不同的
        LocationClientOption option = new LocationClientOption();
        //高精度定位模式：这种定位模式下，会同时使用网络定位和GPS定位，优先返回最高精度的定位结果；
        //低功耗定位模式：这种定位模式下，不会使用GPS，只会使用网络定位（Wi-Fi和基站定位）；
        //仅用设备定位模式：这种定位模式下，不需要连接网络，只使用GPS进行定位，这种模式下不支持室内环境的定位。
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);////可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setScanSpan(2000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
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
    @Override
    public IBinder onBind(Intent intent) {
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(BNDemoMainActivity.TAG, "启动LocationApplication!");

        initLocation(BNRoutePlanNode.CoordinateType.BD09LL);

        mLocationClient.start();
        mLocationClient.requestLocation();

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
