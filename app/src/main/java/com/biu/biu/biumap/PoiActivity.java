package com.biu.biu.biumap;


import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.biu.biu.main.HomeFragment;
import com.biu.biu.main.HotHomeFragment;
import com.biu.biu.main.TipItemInfo;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.views.base.BaseActivity;

import org.json.JSONArray;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

//兴趣点的2500米页面
public class PoiActivity extends BaseActivity implements OnClickListener {
	// 定义两个fragment 最新和热点
	private HomeFragment HomeFg;
	private HotHomeFragment hotHomeFragment;
	private FrameLayout poiContent;
	// 两个Button按钮
	// 定义两个改变首页内容的按钮控件
	private Button homeFrgNew;
	private Button homeFrgHot;
	// 定义跳转后的经纬度坐标
	private double centerLat;
	private double centerLng;
	// 保存兴趣点位置按钮
	private Button poiSave;
	private GeoCoder mSearch;
	// 是否保存过的布尔型变量
	private boolean savedOrNot = false;
	// 保存线性区域
	private LinearLayout saveContentArea;
	private AutoListView mHomeListView;
	private final int FIRST_PAGE_GET_OK = 1;
	private final int FIRST_PAGE_GET_ERROR = 2;
	private final int NEXT_PAGE_GET_OK = 3; // 得到下一页成功
	private final int NEXT_PAGE_GET_ERROR = 4; // 或许下一页信息失败
	private final int HOME_LISTVIEW_REFRESH = 5; // 刷新页面
	private final int HOME_LISTVIEW_LOADMORE = 6; // 加载更多
	public final int UPDATE_HOMELISTVIEW = 7; // 更新ListView
	private final int GETLAT_LNGFAILURE = 8; // 获得定位失败

	private String url = "http://api.bbbiu.com:1234/first-page";
	private ArrayList<TipItemInfo> mHomeListItems = new ArrayList<TipItemInfo>();
	private JSONArray jsonArray = null; // 存储从网络中获得的数据
	private PoiListAdapter listViewAdapter = null;
	// private boolean mlatlnghasgetted = false;
	private boolean usefirstBuffer = true;
	private boolean mthreadisrunning = false;
	private boolean mfirstRefresh = true;
	private TextView mMoreHottv = null;
	// 设置一个变量也用识别是否是在刷新 使得nextlistItemsBuffer清空
	private boolean ctrlRefresh = false;
	// 兴趣点的地点名称
	private String poiPlaceName = null;
	// 显示当前位置的TextView
	private TextView poiPlaceShow;
	@BindView(R.id.toolbar)
	Toolbar toolbar;
	// 专门用于定时刷新的Handler
	private Handler refreshHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_poi);
		ButterKnife.bind(this);
		homeFrgNew = (Button) findViewById(R.id.home_frg_new);
		homeFrgHot = (Button) findViewById(R.id.home_frg_hot);

		mHomeListView = (AutoListView) findViewById(R.id.home_showtopic);
		poiPlaceShow = (TextView) findViewById(R.id.poi_place_name);
		saveContentArea = (LinearLayout) findViewById(R.id.save_description);
		poiContent = (FrameLayout) findViewById(R.id.poi_content);
		// 地理编码相关的初始化S
		initGeocode();

		// 获取意图对象中的兴趣点的位置坐标数值
		centerLat = getIntent().getDoubleExtra("centerLat", -1.0);
		centerLng = getIntent().getDoubleExtra("centerLng", -1.0);
		// 修改游客身份记录的静态变量
		UserConfigParams.poiLat = centerLat;
		UserConfigParams.poiLng = centerLng;
		// 保存按钮初始化并添加点击事件
		poiSave = (Button) this.findViewById(R.id.poi_save);
		poiSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 此处点击保存弹出一个对话框然后保存三个变量，。经度坐标和用户的描述信息
				/*Dialog saveMyPoiDialog = new SavePoiDialog(PoiActivity.this,
						centerLat, centerLng, poiPlaceName,
						R.style.poi_save_dialog, poiSave);
				saveMyPoiDialog.show();*/
				final PoiDatabaseHelper poiDbHelper = new PoiDatabaseHelper(PoiActivity.this, "poi.db", null, 1);
				final EditText content = new EditText(PoiActivity.this);
				content.setHint("帮这个兴趣点起个容易识别的名称");
				final AlertDialog alertDialog = new AlertDialog.Builder(PoiActivity.this).setTitle
						("保存位置").setView(content).setPositiveButton("保存", new
						DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								SQLiteDatabase db = poiDbHelper.getWritableDatabase();
								ContentValues contentValues = new ContentValues();
								contentValues.put("lat", centerLat);
								contentValues.put("lng", centerLng);
								contentValues.put("placename", poiPlaceName);
								contentValues.put("description", content.getText()
										.toString());
								db.insert("poi", null, contentValues);
								// 那个保存按钮消失
								poiSave.setVisibility(View.GONE);
							}
						}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create();
				alertDialog.show();
			}
		});
		savedOrNot = getIntent().getBooleanExtra("savedornot", false);
		if (savedOrNot) {
			saveContentArea.setVisibility(View.GONE);
		}

		if (getIntent().getStringExtra("placeName").equals("unknown")) {
			Log.i("placeName", getIntent().getStringExtra("placeName"));
			Log.i("当全班的坐标", centerLat + "  " + centerLng);
			LatLng tempLatlng = new LatLng(centerLat, centerLng);
			mSearch.reverseGeoCode(new ReverseGeoCodeOption()
					.location(tempLatlng));
		} else {
			Log.i("placeName1111", getIntent().getStringExtra("placeName"));
			poiPlaceName = getIntent().getStringExtra("placeName");
			poiPlaceShow.setText(poiPlaceName);
		}

		// if (getIntent().getStringExtra("placeName") != ""
		// && getIntent().getStringExtra("placeName") != null
		// && getIntent().getStringExtra("placeName").equals("unknown")) {
		// Log.i("placeName1111", getIntent().getStringExtra("placeName"));
		// poiPlaceName = getIntent().getStringExtra("placeName");
		// poiPlaceShow.setText(poiPlaceName);
		// } else {
		// Log.i("placeName", getIntent().getStringExtra("placeName"));
		// Log.i("当全班的坐标", centerLat + "  " + centerLng);
		// LatLng tempLatlng = new LatLng(centerLat, centerLng);
		// mSearch.reverseGeoCode(new ReverseGeoCodeOption()
		// .location(tempLatlng));

		// }

		// 向最新和热点按钮添加单击事件
		homeFrgNew.setOnClickListener(this);
		homeFrgHot.setOnClickListener(this);
		Log.i("latlng", centerLat + "   " + centerLng);
		setDefaultFragment();
		initView();
	}

	private void initView() {
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setTitle("");
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// TODO Auto-generated method stub
		// 获取首页帖子信息
		if (mfirstRefresh) { // 第一次启动时自动获取首页帖子信息
			mfirstRefresh = false;
		} else {
			// 如果不是第一次启动，但是在发表新帖之后，也进行一次获取首页刷新。
			if (UserConfigParams.isHomeRefresh) {
				UserConfigParams.isHomeRefresh = false;
			}
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSearch.destroy();
	}

	/**
	 * 线程可以通过传入的上下文调用此函数最终获得Handler对象。
	 * 
	 * @return
	 */

	// 初始化地理编码相关
	public void initGeocode() {
		// 根据坐标反地理编码
		mSearch = GeoCoder.newInstance();
		Log.i("reverseGeoCode", "反地理编码结果1111");
		OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
			public void onGetGeoCodeResult(GeoCodeResult result) {
				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {
					// 没有检索到结果
				}
				// 获取地理编码结果
			}

			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				if (result == null
						|| result.error != SearchResult.ERRORNO.NO_ERROR) {
					// 没有找到检索结果
					return;
				}
				Log.i("reverseGeoCode", "反地理编码结果2222");
				// 获取反向地理编码结果
				ReverseGeoCodeResult.AddressComponent detailAddress = result
						.getAddressDetail();
				poiPlaceName = detailAddress.province + detailAddress.city
						+ detailAddress.district + detailAddress.street
						+ detailAddress.streetNumber;
				poiPlaceShow.setText(poiPlaceName);

			}
		};
		mSearch.setOnGetGeoCodeResultListener(listener);
	}

	@Override
	public void onClick(View v) {
		FragmentManager fm = getSupportFragmentManager();
		// 开启Fragment事务
		FragmentTransaction transaction = fm.beginTransaction();
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.home_frg_new:
			if (HomeFg == null) {
				HomeFg = HomeFragment.getInstance(false);
			}
			homeFrgNew.setBackgroundResource(R.drawable.switching2);
			// 字体为biu蓝
			homeFrgNew.setTextColor(getResources().getColor(
					R.color.biu_main_color));
			homeFrgHot.setBackgroundResource(R.drawable.switching4);
			homeFrgHot.setTextColor(getResources().getColor(
					R.color.biu_font_white));
			// 使用当前的碎片替代poi_content;
			transaction.replace(R.id.poi_content, HomeFg);
			break;
		case R.id.home_frg_hot:
			Log.i("兴趣点首页", "点击了热点页面。。。。。");
			if (hotHomeFragment == null) {
				hotHomeFragment = HotHomeFragment.getInstance(false);
			}
			homeFrgNew.setBackgroundResource(R.drawable.switching1);
			homeFrgHot.setTextColor(getResources().getColor(
					R.color.biu_main_color));
			homeFrgHot.setBackgroundResource(R.drawable.switching3);
			homeFrgNew.setTextColor(getResources().getColor(
					R.color.biu_font_white));
			// 使用当前的碎片替代poi_content;
			transaction.replace(R.id.poi_content, hotHomeFragment);
			break;

		default:
			break;
		}

		transaction.commit();
	}

	private void setDefaultFragment() {
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		HomeFg = HomeFragment.getInstance(false);
		transaction.replace(R.id.poi_content, HomeFg);
		transaction.commit();
	}

}
