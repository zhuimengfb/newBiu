package com.biu.biu.main;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.biu.biu.thread.GetMoonThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.tools.AutoListView.OnLoadListener;
import com.biu.biu.tools.AutoListView.OnRefreshListener;
import com.biu.biu.userconfig.UserConfigParams;
import com.umeng.socialize.utils.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import grf.biu.R;

public class MoonboxActivity extends Activity implements OnRefreshListener,
		OnLoadListener, AMapLocationListener {
	private AutoListView mMoonBoxListView;
	private MoonBoxListViewAdapter mMoonAdapter;
	private ArrayList<TipItemInfo> mTipItems;
	private MoonHandler mMoonHandler;
	private GetMoonThread mGetThread = null;
	private boolean mIsEmpty = true; // 当前数据栈是否为空
	private int mGettedCount = 0; // 当前缓存中帖子数
	private JSONArray mJsonArray = null; // 存储网络中获得的数据
	private TextView mTabTopicHomeTv;
	private ImageButton mHomePublishBtn;
	private ImageButton mTabBackBtn;
	private final static int MSG_GET_OK_0 = 0; // 清空现有数据并获得新数据
	private final static int MSG_GET_OK_1 = 1; // 将新数据添加到现有数据
	private final static int MSG_GET_ERROR = -1;

	// 高德地图位置刷新
	private LocationManagerProxy mLocationManagerProxy;
	private Double mainLat = 38.00; // 维度
	private Double mainLng = 125.00; // 经度

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_moonbox);
		findId();
		initView();
		Log.i("MOONACTIVITY", "月光宝盒页面");
	}

	private void initView() {
		// TODO Auto-generated method stub
		mTabTopicHomeTv.setText(R.string.title_moonbox);
		mMoonHandler = new MoonHandler(this);
		mTipItems = new ArrayList<TipItemInfo>();
		mMoonAdapter = new MoonBoxListViewAdapter(this, mTipItems);

		mMoonAdapter.setHandler(mMoonHandler);
		mMoonAdapter.setListView(mMoonBoxListView);
		mMoonBoxListView.setAdapter(mMoonAdapter);

		mMoonBoxListView.setOnRefreshListener(this);
		mMoonBoxListView.setOnLoadListener(this);
		mMoonBoxListView.setPageSize(5); // 每次获得5条帖子信息
		// mMoonBoxListView.setfootVisibility(false); // 不显示下脚
		mGettedCount = 0;
		// 添加点击进入详情页的功能
		mMoonBoxListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (mIsEmpty)
					return;
				if (position > mTipItems.size())
					return;
				int nPosition = position - 1; // 减去header
				Intent intent = new Intent();
				// 详情页另开一个activity，暂时复用详情页。
				intent.setClass(MoonboxActivity.this, PeepDetailActivity.class);
				intent.putExtra("thread_id", mTipItems.get(nPosition).id);
				intent.putExtra("DetailMode",
						PeepDetailActivity.TIPDETAIL_FOR_MOONBOOX);
				// intent.putExtra("displayformoon", true); // 设置详情页格式为月光宝盒详情页
				startActivity(intent);
			}
		});
		// 左上角回退按钮
		mTabBackBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		// 右上角发表按钮
		mHomePublishBtn.setOnClickListener(new OnClickListener() {

			@Override
			// 跳转到发表页
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MoonboxActivity.this,
						PublishTopicActivity.class);
				intent.putExtra("PublishMode",
						PublishTopicActivity.PUBLISH_FOR_MOONBOOX);
				startActivity(intent);
			}

		});
	}

	private void findId() {
		// TODO Auto-generated method stub
		mTabTopicHomeTv = (TextView) this.findViewById(R.id.TabTopTitle);
		mMoonBoxListView = (AutoListView) findViewById(R.id.moonlistview);
		// 回退按钮
		mTabBackBtn = (ImageButton) findViewById(R.id.publish_back);
		// 右上角发表按钮
		mHomePublishBtn = (ImageButton) this.findViewById(R.id.submit_new);
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		getMoonBoxFirst(mGettedCount);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		mGettedCount = 0;
		getMoonBoxFirst(0);
	}

	private void getMoonBoxFirst(int nGettedCount) {
		// TODO Auto-generated method stub
		mGetThread = new GetMoonThread(mMoonHandler, nGettedCount);
		mGetThread.start();
	}

	private static class MoonHandler extends Handler {
		WeakReference<MoonboxActivity> mMoonwekref;

		public MoonHandler(MoonboxActivity activity) {
			mMoonwekref = new WeakReference<MoonboxActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int nMsg = msg.what;
			MoonboxActivity mActivity = mMoonwekref.get();
			switch (nMsg) {
			case MSG_GET_OK_0: // 更新数据
				mActivity.addNewData(true);
				break;
			case MSG_GET_OK_1: // 加载更多数据
				mActivity.addNewData(false);
				break;
			}
			super.handleMessage(msg);
		}

	}

	public void addNewData(boolean isclear) {
		// TODO Auto-generated method stub
		// 判断是否为更新数据
		if (isclear) {
			mTipItems.clear();
			mGettedCount = 0;
			mIsEmpty = true;
		} else
			mMoonBoxListView.onLoadComplete();
		mJsonArray = mGetThread.getmJsonArray();
		try {
			MyDateTimeDeal timedeal = new MyDateTimeDeal();
			for (int i = 0; i < mJsonArray.length(); i++) {
				JSONObject everyJsonObject = mJsonArray.getJSONObject(i);
				TipItemInfo item = new TipItemInfo();
				item.content = everyJsonObject.getString("content");
				item.created_at = timedeal.getTimeGapDesc(everyJsonObject
						.getString("created_at"));
				item.imgurl = everyJsonObject.getString("img_url");
				item.device_id = everyJsonObject.getString("device_id");
				item.id = everyJsonObject.getString("id");
				item.lat = everyJsonObject.getString("lat");
				Integer likeresult = Integer.parseInt(everyJsonObject
						.getString("like_num"))
						- Integer.parseInt(everyJsonObject
								.getString("tread_num"));
				item.like_num = likeresult.toString();
				item.lng = everyJsonObject.getString("lng");
				item.reply_num = everyJsonObject.getString("reply_num");
				item.reply_to = everyJsonObject.getString("reply_to");
				item.title = everyJsonObject.getString("title");
				item.tread_num = everyJsonObject.getString("tread_num");
				item.updated_at = everyJsonObject.getString("updated_at");
				item.pubplace = everyJsonObject.getString("address");
				item.hasliked = everyJsonObject.getBoolean("has_liked");
				item.hastreaded = everyJsonObject.getBoolean("has_treaded");
				mTipItems.add(item);
			}
			mMoonBoxListView.setResultSize(mJsonArray.length());
			mMoonBoxListView.onRefreshComplete();
			mMoonBoxListView.onLoadComplete();
			// 若处理完成之后仍然无数据，则显示空数据提示
			if (mTipItems.isEmpty()) {
				mIsEmpty = true;
				mGettedCount = 0;
				TipItemInfo temp = new TipItemInfo();
				temp.content = (String) getResources().getText(
						R.string.nomoonbox);
				mMoonAdapter.setNoTips(true); // 设置为无帖子类型，用于getView响应
				mTipItems.add(temp);

			} else {
				mIsEmpty = false;
				mGettedCount = mTipItems.size();
				mMoonAdapter.setNoTips(false); // 设置适配器中的存在帖子标记
			}

			mMoonAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mMoonBoxListView.setSelection(0);
		// TODO Auto-generated method stub
		initGaodeLocation(); // 初始化高德定位系统（每分钟定位一次，500米定位一次）

		SharedPreferences preferences = getSharedPreferences("user_Params",
				MODE_PRIVATE);
		UserConfigParams.device_id = preferences.getString("device_ID", "");
		super.onResume();
		onRefresh();
		Log.i("MOONACTIVITY", "月光宝盒页面");
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (mLocationManagerProxy != null) {
			mLocationManagerProxy.destroy();
		}
		super.onPause();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
		// TODO Auto-generated method stub
		if (amapLocation != null
				&& amapLocation.getAMapException().getErrorCode() == 0) {
			// 获取位置信息
			mainLat = amapLocation.getLatitude();

			mainLng = amapLocation.getLongitude();
			UserConfigParams.latitude = mainLat.toString();
			UserConfigParams.longitude = mainLng.toString();
			UserConfigParams.setLocationGetted(true);
			Log.i("GAODE", "当前的坐标位置为:" + mainLat + "     " + mainLng);
		} else {
			Toast.makeText(this,
					amapLocation.getAMapException().getErrorMessage(),
					Toast.LENGTH_SHORT).show();
			mLocationManagerProxy.destroy();
		}

	}

	/*
	 * 初始化高德地图的相关操作
	 */
	private void initGaodeLocation() {
		// TODO Auto-generated method stub
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);

		// 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
		// 注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
		// 在定位结束后，在合适的生命周期调用destroy()方法
		// 其中如果间隔时间为-1，则定位只定一次
		mLocationManagerProxy.requestLocationData(
				LocationProviderProxy.AMapNetwork, 5000, 20, this);
		Log.e("GAODE", "定位刷新");
		// mLocationManagerProxy.setGpsEnable(false);
	}
}
