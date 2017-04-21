package com.mawujun.navi;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

//import com.baidu.navi.sdkdemo.R;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.mawujun.ems.R;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BNDemoMainActivity extends Activity {
	public static final String TAG = "BNDemoMainActivity";
	public static List<Activity> activityList = new LinkedList<Activity>();

	private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";

//	private Button mWgsNaviBtn = null;
//	private Button mGcjNaviBtn = null;
//	private Button mBdmcNaviBtn = null;
	private Button mDb06llBtn = null;
	private String mSDCardPath = null;

	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
	public static final String RESET_END_NODE = "resetEndNode";
	public static final String VOID_MODE = "voidMode";

	private static final String[] authBaseArr = { Manifest.permission.WRITE_EXTERNAL_STORAGE,
			Manifest.permission.ACCESS_FINE_LOCATION };
	private static final String[] authComArr = { Manifest.permission.READ_PHONE_STATE };
	private static final int authBaseRequestCode = 1;
	private static final int authComRequestCode = 2;

	private boolean hasInitSuccess = false;
	private boolean hasRequestComAuth = false;

	private CoordinateType mCoordinateType = null;

	private LocReceiver msgReceiver;

	//目标经纬度
	Double target_longitude=null;
	Double target_latitude=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activityList.add(this);
		setContentView(R.layout.activity_bndemomain);

//		mLocationClient = new LocationClient(getApplicationContext());
//		//声明LocationClient类
//		mLocationClient.registerLocationListener( myListener );
//		//注册监听函数,暂时固定为百度经纬度
//		initLocation(mCoordinateType);
//		mLocationClient.start();

		Intent intent=getIntent();
		target_longitude=Double.parseDouble(intent.getStringExtra("longitude"));
		target_latitude=Double.parseDouble(intent.getStringExtra("latitude"));
		mCoordinateType=CoordinateType.valueOf(intent.getStringExtra("coordinateType"));


//		mWgsNaviBtn = (Button) findViewById(R.id.wgsNaviBtn);
//		mGcjNaviBtn = (Button) findViewById(R.id.gcjNaviBtn);
//		mBdmcNaviBtn = (Button) findViewById(R.id.bdmcNaviBtn);
		mDb06llBtn = (Button) findViewById(R.id.mDb06llNaviBtn);
		BNOuterLogUtil.setLogSwitcher(true);

		initListener();
		if (initDirs()) {
			initNavi();
		}
		mDb06llBtn.setClickable(false);

		// BNOuterLogUtil.setLogSwitcher(true);

		msgReceiver = new LocReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.mawujun.navi.RECEIVER");
		registerReceiver(msgReceiver, intentFilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
		btn_clicked=false;
		mDb06llBtn.setClickable(true);
		mDb06llBtn.setText("出        发");
	}

	private boolean btn_clicked=false;
	private void initListener() {
		if (mDb06llBtn != null) {
			mDb06llBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (BaiduNaviManager.isNaviInited()) {
						btn_clicked=true;
						mDb06llBtn.setClickable(false);
						mDb06llBtn.setText("正在算路，请稍候....");
						routeplanToNavi();
						//Toast.makeText(BNDemoMainActivity.this, "正在获取当前地址，请稍候!", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(BNDemoMainActivity.this, "导航正在初始化，请稍候!", Toast.LENGTH_SHORT).show();
					}
				}
			});
		}

	}

	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	String authinfo = null;

	/**
	 * 内部TTS播报状态回传handler
	 */
	private Handler ttsHandler = new Handler() {
		public void handleMessage(Message msg) {
			int type = msg.what;
			switch (type) {
				case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
					// showToastMsg("Handler : TTS play start");
					break;
				}
				case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
					// showToastMsg("Handler : TTS play end");
					break;
				}
				default:
					break;
			}
		}
	};

	/**
	 * 内部TTS播报状态回调接口
	 */
	private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

		@Override
		public void playEnd() {
			// showToastMsg("TTSPlayStateListener : TTS play end");
		}

		@Override
		public void playStart() {
			// showToastMsg("TTSPlayStateListener : TTS play start");
		}
	};

	public void showToastMsg(final String msg) {
		BNDemoMainActivity.this.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(BNDemoMainActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private boolean hasBasePhoneAuth() {
		// TODO Auto-generated method stub

		PackageManager pm = this.getPackageManager();
		for (String auth : authBaseArr) {
			if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	private boolean hasCompletePhoneAuth() {
		// TODO Auto-generated method stub

		PackageManager pm = this.getPackageManager();
		for (String auth : authComArr) {
			if (pm.checkPermission(auth, this.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
				return false;
			}
		}
		return true;
	}

	//private boolean auth_success=false;
	private void initNavi() {

		BNOuterTTSPlayerCallback ttsCallback = null;

		// 申请权限
		if (android.os.Build.VERSION.SDK_INT >= 23) {

			if (!hasBasePhoneAuth()) {

				this.requestPermissions(authBaseArr, authBaseRequestCode);
				return;

			}
		}

		BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				if (0 == status) {
					authinfo = "key校验成功!";
				} else {
					authinfo = "key校验失败, " + msg;
				}
				BNDemoMainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(BNDemoMainActivity.this, authinfo, Toast.LENGTH_SHORT).show();
					}
				});
			}

			public void initSuccess() {
				Toast.makeText(BNDemoMainActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
				hasInitSuccess = true;
				initSetting();
				//if(auth_success){
				//mDb06llBtn.setClickable(false);
				mDb06llBtn.setText("正在获取当前地址，请稍候...!");
				//}
			}

			public void initStart() {
				Toast.makeText(BNDemoMainActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
			}

			public void initFailed() {
				Toast.makeText(BNDemoMainActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
			}

		}, null, ttsHandler, ttsPlayStateListener);

	}

	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	/**
	 * 广播接收器
	 * @author len
	 *
	 */
	Double longitude_start =null;
	Double latitude_start =null;
	public class LocReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//拿到经纬度
			longitude_start = intent.getDoubleExtra("longitude", 0);
			latitude_start = intent.getDoubleExtra("latitude", 0);
			//mProgressBar.setProgress(progress);
			Log.w(TAG, "开始定位坐标为："+longitude_start+"----"+latitude_start);
			BNDemoMainActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if(hasInitSuccess &&btn_clicked==false){
						mDb06llBtn.setClickable(true);
						mDb06llBtn.setText("出        发");
					}

				}
			});
		}

	}

	private void routeplanToNavi() {
		 CoordinateType coType=mCoordinateType;


		if (!hasInitSuccess) {
			Toast.makeText(BNDemoMainActivity.this, "还未初始化!", Toast.LENGTH_SHORT).show();
			return;
		}
		// 权限申请
		if (android.os.Build.VERSION.SDK_INT >= 23) {
			// 保证导航功能完备
			if (!hasCompletePhoneAuth()) {
				if (!hasRequestComAuth) {
					hasRequestComAuth = true;
					this.requestPermissions(authComArr, authComRequestCode);
					return;
				} else {
					Toast.makeText(BNDemoMainActivity.this, "没有完备的权限!", Toast.LENGTH_SHORT).show();
				}
			}

		}
//		Intent intent=getIntent();
//		Double longitude=Double.parseDouble(intent.getStringExtra("longitude"));
//		Double latitude=Double.parseDouble(intent.getStringExtra("latitude"));

		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;
//		switch (coType) {
//			case BD09LL: {
////				BDLocation bDLocation=mLocationClient.getLastKnownLocation();
////				mLocationClient.stop();
//
//				//BDLocation bDLocation=conn.getBindService().mLocationClient.getLastKnownLocation();
//				//Log.i(TAG, "定位坐标为："+bDLocation.getLongitude()+"----"+bDLocation.getLatitude());
//				sNode = new BNRoutePlanNode(longitude_start, latitude_start, "开始位置", null, coType);
//				//sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
//				eNode = new BNRoutePlanNode(target_longitude, target_latitude, "目标位置", null, coType);
//				break;
//			}
//			default:
//				;
//		}
		sNode = new BNRoutePlanNode(longitude_start, latitude_start, "开始位置", null, coType);
		//sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
		eNode = new BNRoutePlanNode(target_longitude, target_latitude, "目标位置", null, coType);

		if (sNode != null && eNode != null) {
			mDb06llBtn.setClickable(false);
			mDb06llBtn.setText("正在计算路线，请稍候!");
			List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
			list.add(sNode);
			list.add(eNode);
			BaiduNaviManager.getInstance().launchNavigator(this, list, BaiduNaviManager.RoutePlanPreference.ROUTE_PLAN_MOD_MIN_DIST, true, new DemoRoutePlanListener(sNode));
		} else {
			mDb06llBtn.setText("获取当前位置失败，请检查网络和GPS是否打开!");
		}
	}

	public class DemoRoutePlanListener implements RoutePlanListener {

		private BNRoutePlanNode mBNRoutePlanNode = null;

		public DemoRoutePlanListener(BNRoutePlanNode node) {
			mBNRoutePlanNode = node;
		}

		@Override
		public void onJumpToNavigator() {
            /*
             * 设置途径点以及resetEndNode会回调该接口
             */

			for (Activity ac : activityList) {

				if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

					return;
				}
			}
			Intent intent = new Intent(BNDemoMainActivity.this, BNDemoGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);
			startActivity(intent);

		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
			Toast.makeText(BNDemoMainActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
		}
	}

	private void initSetting() {
		// BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
		BNaviSettingManager
				.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
		BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
		// BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
		BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
		Bundle bundle = new Bundle();
		// 必须设置APPID，否则会静音
		bundle.putString(BNCommonSettingParam.TTS_APP_ID, "9496452");
		BNaviSettingManager.setNaviSdkParam(bundle);
	}

	private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

		@Override
		public void stopTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "stopTTS");
		}

		@Override
		public void resumeTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "resumeTTS");
		}

		@Override
		public void releaseTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "releaseTTSPlayer");
		}

		@Override
		public int playTTSText(String speech, int bPreempt) {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

			return 1;
		}

		@Override
		public void phoneHangUp() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneHangUp");
		}

		@Override
		public void phoneCalling() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneCalling");
		}

		@Override
		public void pauseTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "pauseTTS");
		}

		@Override
		public void initTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "initTTSPlayer");
		}

		@Override
		public int getTTSState() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "getTTSState");
			return 1;
		}
	};

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		// TODO Auto-generated method stub
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == authBaseRequestCode) {
			for (int ret : grantResults) {
				if (ret == 0) {
					continue;
				} else {
					Toast.makeText(BNDemoMainActivity.this, "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
					return;
				}
			}
			initNavi();
		} else if (requestCode == authComRequestCode) {
			for (int ret : grantResults) {
				if (ret == 0) {
					continue;
				}
			}
			//routeplanToNavi(mCoordinateType);
			routeplanToNavi();
		}

	}




//	//http://blog.163.com/allegro_tyc/blog/static/337437682013629348791/
//	private boolean flag = false;
//
//	private void bindService() {
//		Intent intent = new Intent(BNDemoMainActivity.this,LocService.class);
//		bindService(intent, conn, Context.BIND_AUTO_CREATE);
//	}
//
//	private void unBind() {
//		if (flag == true) {
//			// Log.i(TAG, "BindService-->unBind()");
//			unbindService(conn);
//			flag = false;
//		}
//	}
//
//	public class MyServiceConnection implements ServiceConnection {
//		LocService bindService;
//
//		public LocService getBindService() {
//			return bindService;
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			// TODO Auto-generated method stub
//			LocService.MyBinder binder = (LocService.MyBinder) service;
//			bindService = binder.getService();
//			//bindService.mLocationClient.requestLocation();
//			// bindService.
//			flag = true;
//		}
//
//	}
//
//	private MyServiceConnection conn = new MyServiceConnection();
}
