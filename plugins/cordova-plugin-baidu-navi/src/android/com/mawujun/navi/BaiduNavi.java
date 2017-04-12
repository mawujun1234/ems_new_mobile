package com.mawujun.navi;


import android.content.Intent;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

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
                    try {
                        longitude=args.getString(0);
                        latitude=args.getString(1);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        Log.e(LOG_TAG, e.getMessage());
                        callbackContext.error(PluginResult.Status.ERROR.toString());
                    }

                    Intent intent = new Intent().setClass(cordova.getActivity(), BNDemoMainActivity.class);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("latitude", latitude);
                    cordova.startActivityForResult(cordovaPlugin, intent, 1);

                    //下面三句为cordova插件回调页面的逻辑代码
                    PluginResult mPlugin = new PluginResult(PluginResult.Status.NO_RESULT);
                    mPlugin.setKeepCallback(true);

                    callbackContext.sendPluginResult(mPlugin);
                    callbackContext.success(200);
                }
            });
            return true;
        }
        return false;
    }

}
