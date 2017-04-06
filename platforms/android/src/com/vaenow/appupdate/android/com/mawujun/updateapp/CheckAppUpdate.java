package com.mawujun.updateapp;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;


/**
 * Created by mawujun on 2017/4/6.
 */

public class CheckAppUpdate extends CordovaPlugin {
    public static final String TAG = "CheckAppUpdate";

    private UpdateManager updateManager = null;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)
            throws JSONException {

        if (action.equals("checkAppUpdate")) {
            //verifyStoragePermissions(this.cordova.getActivity());
            getUpdateManager(args, callbackContext).checkUpdate();
            return true;
        }
        callbackContext.error(Utils.makeJSON(Constants.NO_SUCH_METHOD, "no such method: " + action));
        return false;
    }

    public UpdateManager getUpdateManager(JSONArray args, CallbackContext callbackContext)
            throws JSONException {

        if (this.updateManager == null) {
            this.updateManager = new UpdateManager(this.cordova.getActivity(), this.cordova);
        }

        return this.updateManager.options(args, callbackContext);
    }

//    private static void verifyStoragePermissions(Activity activity) {
//        // Check if we have write permission
//        int permission = ContextCompat.checkSelfPermission(activity,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE);
//
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            // We don't have permission so prompt the user
//            ContextCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
//                    REQUEST_EXTERNAL_STORAGE);
//        }
//    }
}
