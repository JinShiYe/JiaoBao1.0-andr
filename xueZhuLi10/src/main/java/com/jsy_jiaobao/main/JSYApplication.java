package com.jsy_jiaobao.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.os.Environment;
import android.os.Vibrator;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDNotifyListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.jsy.xuezhuli.utils.Constant;
import com.jsy.xuezhuli.utils.EventBusUtil;
import com.jsy.xuezhuli.utils.HttpUtil;
import com.jsy_jiaobao.po.sys.UserIdentity;
import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.DbUtils;

public class JSYApplication extends Application {
	private static JSYApplication mInstance = null;
	public boolean m_bKeyRight = true;
	public BMapManager mBMapManager = null;
	public ArrayList<Activity> activityArray = new ArrayList<Activity>();
	public LocationClient mLocationClient;
	public GeofenceClient mGeofenceClient;
	private MyLocationListener mMyLocationListener;
	public Vibrator mVibrator;
	public NotifyLister mNotifyLister;
	private BDLocation mlocation;

	public BitmapUtils bitmap;
	public BitmapUtils bitmapUtils;// 单位logo
	public DbUtils dbUtils;
	public String DB_PATH, FILE_PATH, AV_PATH;
	// 用户所有身份
	public static List<UserIdentity> listUserIdentity;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		initBitmap();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());

		dbUtils = DbUtils.create(this.getApplicationContext());
		dbUtils.configAllowTransaction(true);
		dbUtils.configDebug(true);
		mInstance = this;
		initEngineManager(this);
		//
		mLocationClient = new LocationClient(this);
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);

		mGeofenceClient = new GeofenceClient(this);
		mNotifyLister = new NotifyLister();
		mVibrator = (Vibrator) getApplicationContext().getSystemService(
				Service.VIBRATOR_SERVICE);
		HttpUtil.getInstance().configHttpCacheSize(1024 * 200);
		HttpUtil.getInstance().configDefaultHttpCacheExpiry(1000 * 30);
	}

	private void initBitmap() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			// 有sd卡就存在sd卡中
			DB_PATH = Environment.getExternalStorageDirectory()
					+ "/goldeneyes/datebase/";
			FILE_PATH = Environment.getExternalStorageDirectory()
					+ "/goldeneyes/file/";
			AV_PATH = Environment.getExternalStorageDirectory()
					+ "/goldeneyes/VideoAudio/";
		} else {
			// 没有sd卡就存入data文件夹下
			DB_PATH = getApplicationContext().getFilesDir().getPath()
					+ "/datebase/";
			FILE_PATH = getApplicationContext().getFilesDir().getPath()
					+ "/file/";
			AV_PATH = getApplicationContext().getFilesDir().getPath()
					+ "/VideoAudio/";
		}
		// 如果目录不存在 新建这个目录
		File dir = new File(DB_PATH);
		if (!dir.exists()) {
			dir.mkdir();
		}
		File dir1 = new File(FILE_PATH);
		if (!dir1.exists()) {
			dir1.mkdir();
		}
		File dir11 = new File(AV_PATH);
		if (!dir11.exists()) {
			dir11.mkdir();
		}
		bitmap = new BitmapUtils(this, FILE_PATH);
		bitmap.configDefaultLoadFailedImage(R.drawable.photo);
		bitmap.configDefaultLoadingImage(R.drawable.photo);
		bitmap.configDiskCacheEnabled(true);
		bitmap.configMemoryCacheEnabled(true);
		bitmap.configDefaultCacheExpiry(1000L * 60 * 60 * 24 * 2);

		bitmapUtils = new BitmapUtils(this, FILE_PATH);
		bitmapUtils.configDefaultLoadFailedImage(R.drawable.icon_unit_logo);
		bitmapUtils.configDefaultLoadingImage(R.drawable.icon_unit_logo);
		bitmapUtils.configDiskCacheEnabled(true);
		bitmapUtils.configMemoryCacheEnabled(true);
		bitmapUtils.configDefaultCacheExpiry(1000L * 60 * 60 * 24 * 2);
	}

	public void addActivity(Activity activity) {
		activityArray.add(activity);
	}

	public void removeActivity(Activity activity) {
		activityArray.remove(activity);
	}

	public void finishActivities() {
		for (Activity activity : activityArray) {
			if (activity != null) {
				activity.finish();
			}
		}
	}

	public static JSYApplication getInstance() {
		return mInstance;
	}

	// 地图******************************************************************
	public void initEngineManager(Context context) {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(context);
		}

		if (!mBMapManager.init(new MyGeneralListener())) {
			Toast.makeText(
					JSYApplication.getInstance().getApplicationContext(),
					"BMapManager 初始化错误!", Toast.LENGTH_LONG).show();
		}
	}

	// 常用事件监听，用来处理通常的网络错误，授权验证错误等
	public static class MyGeneralListener implements MKGeneralListener {

		@Override
		public void onGetNetworkState(int iError) {
			if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
				Toast.makeText(
						JSYApplication.getInstance().getApplicationContext(),
						JSYApplication.getInstance().getApplicationContext()
								.getResources()
								.getString(R.string.error_internet),
						Toast.LENGTH_LONG).show();
			} else if (iError == MKEvent.ERROR_NETWORK_DATA) {
				Toast.makeText(
						JSYApplication.getInstance().getApplicationContext(),
						"输入正确的检索条件！", Toast.LENGTH_LONG).show();
			}
			//
		}

		@Override
		public void onGetPermissionState(int iError) {
			// 非零值表示key验证未通过
			// if (iError != 0) {
			// //授权Key错误：
			// Toast.makeText(JSYSignApplication.getInstance().getApplicationContext(),
			// "请在 Application.java文件输入正确的授权Key,并检查您的网络连接是否正常！error: "+iError,
			// Toast.LENGTH_LONG).show();
			// JSYSignApplication.getInstance().m_bKeyRight = false;
			// }
			// else{
			// JSYSignApplication.getInstance().m_bKeyRight = true;
			// Toast.makeText(JSYSignApplication.getInstance().getApplicationContext(),
			// "key认证成功", Toast.LENGTH_LONG).show();
			// }
		}
	}

	// 定位******************************************************
	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			ArrayList<Object> post = new ArrayList<Object>();
			post.add(Constant.appcenter_location_success);
			post.add(location);
			EventBusUtil.post(post);
		}

		@Override
		public void onReceivePoi(BDLocation arg0) {

		}
	}

	/**
	 * 获取定位结果
	 */
	public BDLocation getLocation() {

		return mlocation;

	}

	/**
	 * 高精度地理围栏回调
	 * 
	 * @author jpren
	 * 
	 */
	public class NotifyLister extends BDNotifyListener {
		public void onNotify(BDLocation mlocation, float distance) {
			// mVibrator.vibrate(1000);
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		this.finishActivities();
		System.exit(0);
	}
}