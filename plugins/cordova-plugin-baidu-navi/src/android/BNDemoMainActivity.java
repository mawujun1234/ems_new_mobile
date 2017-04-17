package com.mawujun.navi;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
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
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
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

	private CoordinateType mCoordinateType = CoordinateType.BD09LL;

	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = new BDLocationListener() {
		@Override
		public void onReceiveLocation(BDLocation bdLocation) {
			//Log.w(TAG, "onLocationChanged. loc: " + bdLocation);
			if (bdLocation != null) {
				//Log.i(TAG, " location is not null" + bdLocation.getLatitude() + " , longtitude: "+bdLocation.getLongitude());

				BNDemoMainActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						//Toast.makeText(BNDemoMainActivity.this, msg, Toast.LENGTH_SHORT).show();
						mDb06llBtn.setClickable(true);
						mDb06llBtn.setText("出        发");
					}
				});

				//routeplanToNavi();
			} else {
				Toast.makeText( getApplicationContext(), "不能获取当前的位置.", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onConnectHotSpotMessage(String s, int i) {

		}
	};

	public void initLocation(CoordinateType coordinateType) {
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

		option.setCoorType(CoordinateType.BD09LL.toString());// 返回的定位结果是百度经纬度，默认值gcj02,//wgs84:国际经纬度坐标  "gcj02":国家测绘局标准,"bd09ll":百度经纬度标准,"bd09":百度墨卡托标准
		option.setProdName("BaiduLoc");
		mLocationClient.setLocOption(option);

	}

	//目标经纬度
	Double target_longitude=null;
	Double target_latitude=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		activityList.add(this);
		setContentView(R.layout.activity_bndemomain);

		mLocationClient = new LocationClient(getApplicationContext());
		//声明LocationClient类
		mLocationClient.registerLocationListener( myListener );
		//注册监听函数,暂时固定为百度经纬度
		initLocation(mCoordinateType);
		mLocationClient.start();

		Intent intent=getIntent();
		target_longitude=Double.parseDouble(intent.getStringExtra("longitude"));
		target_latitude=Double.parseDouble(intent.getStringExtra("latitude"));


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
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
		mDb06llBtn.setClickable(true);
		mDb06llBtn.setText("出        发");
	}

	private void initListener() {
		if (mDb06llBtn != null) {
			mDb06llBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (BaiduNaviManager.isNaviInited()) {
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
//					auth_success=true;
//					if(hasInitSuccess){
//						mDb06llBtn.setClickable(true);
//						mDb06llBtn.setText("出        发");
//					}
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
				mDb06llBtn.setText("正在获取当前位置.....，请稍候!");
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
		switch (coType) {
//			case GCJ02: {
//				sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
//				eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
//				break;
//			}
//			case WGS84: {
//				sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
//				eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
//				break;
//			}
//			case BD09_MC: {
//				sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
//				eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
//				break;
//			}
			case BD09LL: {
				BDLocation bDLocation=mLocationClient.getLastKnownLocation();
//				int i=0;
//				while(bDLocation==null && i<30){
//					Log.w(TAG,i+"获取不到重新获取定位经纬度!");
//					bDLocation=mLocationClient.getLastKnownLocation();
//					i++;
//				}
				//						Toast.makeText(BNDemoMainActivity.this, "正在获取当前地址，请稍候!", Toast.LENGTH_SHORT).show();

				mLocationClient.stop();


				//http://blog.csdn.net/eastmount/article/details/42534721
				Log.i(TAG, "定位坐标为："+bDLocation.getLongitude()+"----"+bDLocation.getLatitude());
				sNode = new BNRoutePlanNode(bDLocation.getLongitude(), bDLocation.getLatitude(), "开始位置", null, coType);
				eNode = new BNRoutePlanNode(target_longitude, target_latitude, "目标位置", null, coType);
				break;
			}
			default:
				;
		}
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
}
