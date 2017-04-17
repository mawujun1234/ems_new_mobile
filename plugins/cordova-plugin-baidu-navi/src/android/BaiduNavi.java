package com.mawujun.navi;


import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.baidu.location.LocationClientOption.*;

/**
 * Created by mawujun on 2017/4/11.
 */
public class BaiduNavi extends CordovaPlugin {
    //private static final String NAVI_ACTION = "navi";
    public static final String LOG_TAG = "BaiduNavi";
    @Override
    public boolean execute(String action,final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if ("navi".equals(action)) {
            final CordovaPlugin cordovaPlugin=this;
            cordova.getThreadPool().execute(new Runnable() {//使用这个线程，一运行就报错，一运行就报错
                @Override
                public void run() {
                    Log.i(LOG_TAG, "开始调用MainActivity");
                    String longitude=null;
                    String latitude=null;
                    String coordinateType=null;

                    try {
                        longitude=args.getString(0);
                        latitude=args.getString(1);
//                        coordinateType=args.getString(2);
//                        if(coordinateType==null){
//                            coordinateType= BNRoutePlanNode.CoordinateType.BD09LL.toString();
//                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e(LOG_TAG, e.getMessage());
                        navi_paramsException(callbackContext);
                        return;
                    }
                    if (longitude==null || latitude==null) {
                        navi_paramsException(callbackContext);
                    }

                    Intent intent = new Intent().setClass(cordova.getActivity(), BNDemoMainActivity.class);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("latitude", latitude);
                    //intent.putExtra("coordinateType", coordinateType);
                    cordova.startActivityForResult(cordovaPlugin, intent, 1);

                    //下面三句为cordova插件回调页面的逻辑代码
                    PluginResult mPlugin = new PluginResult(PluginResult.Status.NO_RESULT);
                    mPlugin.setKeepCallback(true);

                    callbackContext.sendPluginResult(mPlugin);
                    callbackContext.success(200);
                }
            });
            return true;
        } else if("loc".equals(action)){//如果是发送定位数据到后台
            Intent intent=new Intent(cordova.getActivity(), LocService.class);
//            intent.putExtra("uploadUrl", params.getString("uploadUrl"));
//            intent.putExtra("gps_interval", params.getInt("gps_interval"));
//            intent.putExtra("params", params.toString());
//            //initGPS();
            cordova.getActivity().startService(intent);
            callbackContext.success("success");
            return true;
        }
        return false;
    }

    private void navi_paramsException(CallbackContext callbackContext){

        JSONObject json = new JSONObject();
        try {
            json.put("msg","请传入目标经纬度");
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        callbackContext.error(json);
    }

    public LocationClient mLocationClient = null;

//    @Override
//    public void onStart() {
//        super.onStart();
//        mLocationClient = new LocationClient(super.cordova.getActivity().getApplicationContext());
//        //声明LocationClient类
//        mLocationClient.registerLocationListener( myListener );
//        //注册监听函数
//        initLocation();
//    }


}
