package com.biu.biu.biumap;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMapTouchListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionResult.SuggestionInfo;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.views.base.BaseActivity;
import com.umeng.socialize.utils.Log;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

public class BiumapActivity extends BaseActivity {
	private MapView biuMapView;
	// 中心位置// 声明一个中心位置的变量
	private LatLng mapCenter = new LatLng(0, 0);
	// 地图控制器
	private BaiduMap biuBaiduMap;
	// 显示中心位置的地标叠加层
	private OverlayOptions optionsCenter = null;
	// 地图中心的地标
	private Marker centerMarker = null;;
	private ListView showResult;
	// 定义结果显示的适配器
	private ShowResultAdapter showResultAdapter;
	// 定义动态查询的结果集
	private List<SuggestionInfo> showResultList = null;
	// 定义建议查询实例
	private SuggestionSearch biuSuggestionSearch;
	// 建议查询的监听
	private OnGetSuggestionResultListener biuListener;
	// 跳转按钮
	// 定义中心位置的地点名称
	private String placeName = "";
	private RelativeLayout putInPart;
	// 顶一个布尔型的变量，点击地图操作时确保是normao状态
	private boolean normalOrNot = true;
	@BindView(R.id.normal_toolbar)
	Toolbar toolbar;
  @BindView(R.id.fab_location_done)
  FloatingActionButton fabLocationDone;
  @BindView(R.id.search_map)
  SearchView mapSearchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_biumap);
		ButterKnife.bind(this);
      initToolbar();
      initEvent();
		biuMapView = (MapView) this.findViewById(R.id.biumapview);
		biuBaiduMap = biuMapView.getMap();
		LatLng clientCenter = new LatLng(
				Double.parseDouble(UserConfigParams.latitude),
				Double.parseDouble(UserConfigParams.longitude));
		MapStatus biuMapStatus = new MapStatus.Builder().target(clientCenter)
				.zoom(16).build();
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(biuMapStatus);
		biuBaiduMap.setMapStatus(mapStatusUpdate);
		// 获取地图显示页面的顶部top
		showResult = (ListView) this.findViewById(R.id.showResult);
		biuBaiduMap.setOnMapTouchListener(new OnMapTouchListener() {

			@Override
			public void onTouch(MotionEvent arg0) {
				// TODO Auto-generated method stub
				if (!normalOrNot) {
					biuMapView.requestFocus();
					showResult.setVisibility(View.GONE);
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(
							BiumapActivity.this.getCurrentFocus()
									.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
					normalOrNot = true;
				}
			}
		});
		biuBaiduMap.setOnMapStatusChangeListener(new OnMapStatusChangeListener() {

					@Override
					public void onMapStatusChangeStart(MapStatus status) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onMapStatusChangeFinish(MapStatus status) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onMapStatusChange(MapStatus status) {
						// TODO Auto-generated method stub
						// 随着状态的变化，兴趣位置的地标也随着发生变化
						mapCenter = status.target;
						centerMarker.setPosition(mapCenter);
					}
				});
		biuBaiduMap.setOnMapLoadedCallback(new OnMapLoadedCallback() {

			@Override
			public void onMapLoaded() {
				// TODO Auto-generated method stub
				mapCenter = biuBaiduMap.getMapStatus().target;
				BitmapDescriptor icon = BitmapDescriptorFactory
						.fromResource(R.drawable.center_tag);
				optionsCenter = new MarkerOptions().position(mapCenter).icon(
						icon);
				centerMarker = (Marker) biuBaiduMap.addOverlay(optionsCenter);
			}
		});
		initControllers();
		// 初始化putIn文本框
		initPutIn();
	}

	private void initToolbar() {
      setSupportActionBar(toolbar);
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
        actionBar.setHomeButtonEnabled(false);
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
	protected void onDestroy() {
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        biuMapView=null;
      biuBaiduMap=null;
      if (biuSuggestionSearch != null) {
        biuSuggestionSearch.destroy();
      }
      super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		normalOrNot = true;
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		biuMapView.onResume();
		biuMapView.setFocusable(true);
		biuMapView.setFocusableInTouchMode(true);
		biuMapView.requestFocus();
		biuMapView.requestFocusFromTouch();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		biuMapView.onPause();
	}

  private void initEvent(){
    fabLocationDone.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(BiumapActivity.this,
            PoiActivity.class);
        intent.putExtra("centerLat", mapCenter.latitude);
        intent.putExtra("centerLng", mapCenter.longitude);
        if (placeName != null && placeName != "") {
          intent.putExtra("placeName", placeName);
        } else {
          intent.putExtra("placeName", "unknown");
        }
        intent.putExtra("savedornot", false);
        // 跳转到兴趣点位置的2500米范围
        startActivity(intent);
      }
    });
  }
	// }
	// 控制地图页面控件的显示和隐藏
	private void initControllers() {
		// 新点击响应区域
		/*goToPoiArea.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BiumapActivity.this,
						PoiActivity.class);
				intent.putExtra("centerLat", mapCenter.latitude);
				intent.putExtra("centerLng", mapCenter.longitude);
				if (placeName != null && placeName != "") {
					intent.putExtra("placeName", placeName);
				} else {
					intent.putExtra("placeName", "unknown");
				}
				intent.putExtra("savedornot", false);
				// 跳转到兴趣点位置的2500米范围
				startActivity(intent);
			}
		});*/

		// 像goToPoi控件添加事件监听
		/*goToPoi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(BiumapActivity.this,
						PoiActivity.class);
				intent.putExtra("centerLat", mapCenter.latitude);
				intent.putExtra("centerLng", mapCenter.longitude);
				if (placeName != null && placeName != "") {
					intent.putExtra("placeName", placeName);
				} else {
					intent.putExtra("placeName", "unknown");
				}
				intent.putExtra("savedornot", false);
				// 跳转到兴趣点位置的2500米范围
				startActivity(intent);
			}
		});*/


		// 点击地图窗口焦点设置在地图窗口
		biuMapView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				biuMapView.requestFocus();
			}
		});

	}

	// 定义和初始化EditText控件putIn
	private void initPutIn() {
		// 定义查询时候的适配器
		showResultList = new ArrayList<>();
		Log.i("BIUMAPADAPTER", "定义地图查询的适配器");
		// 初始化动态显示查询结果的适配器
		showResultAdapter = new ShowResultAdapter(this,
				R.layout.show_result_item, showResultList);
		// 将适配器赋值给ListView
		showResult.setAdapter(showResultAdapter);
		// 定义建议查询
		biuSuggestionSearch = SuggestionSearch.newInstance();
		// 实例化一个建议查询的监听
		biuListener = new OnGetSuggestionResultListener() {
			public void onGetSuggestionResult(SuggestionResult res) {
				if (res == null || res.getAllSuggestions() == null) {
					return;
					// 未找到相关结果
				}
				// 获取在线建议检索结果(处理和显示建议查询的结果)返回SuggestionInfo数组对象集
				List<SuggestionInfo> onceResult = res.getAllSuggestions();
				showResultList.clear();
				showResultList.addAll(onceResult);
				// 结果集发生变化则通知适配器改变ListView
				if (showResultList != null && showResultList.size() > 0) {
					for (SuggestionInfo a : showResultList) {
						System.out.println(a.key);
					}
					showResultAdapter.notifyDataSetChanged();
					Log.i("SUGGESTION_RESULT_NUM",
							Integer.toString(showResultList.size()));
				}

			}
		};
		// 添加建议查询的监听
		biuSuggestionSearch.setOnGetSuggestionResultListener(biuListener);
      mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
          return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
          if (newText==null|| "".equals(newText)){
            showResult.setVisibility(View.GONE);
          } else {
			  showResult.setVisibility(View.VISIBLE);
            biuSuggestionSearch
                .requestSuggestion(new SuggestionSearchOption()
                    .keyword(newText).city(""));
          }
          return false;
        }
      });
	}

	// 定义动态显示查询的结果
	private class ShowResultAdapter extends ArrayAdapter<SuggestionInfo> {
		private int resourceId;

		public ShowResultAdapter(Context context, int resourceId,
				List<SuggestionInfo> objects) {
			super(context, resourceId, objects);
			// TODO Auto-generated constructor stub
			this.resourceId = resourceId;
			android.util.Log.i("ADAPTER", "适配器的构造函数");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final SuggestionInfo suggestionInfo = getItem(position);
			View view;
			ViewHolder viewHolder;
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(resourceId,
						null);
				viewHolder = new ViewHolder();
				viewHolder.descriptionView = (TextView) view
						.findViewById(R.id.eachdescription);
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			// 定义各个空间的属性或者赋值
			viewHolder.descriptionView.setText(suggestionInfo.key);
			// 添加view 的单击
			// 暂时省略
			Log.i("RESULT", "测试动态查询的结果显示");
			// 对每一个view添加一个click事件
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// 对应的经纬度坐标
					LatLng point = suggestionInfo.pt;
					// 根据坐标位置跳转到，(此处的活动暂且为空)
					// Intent intent = new Intent(BiumapActivity.this, null);
					// intent.putExtra("lat", point.latitude);
					// intent.putExtra("lng", point.latitude);
					// startActivity(intent);
					if (point != null) {
						android.util.Log.i("动态查询的结果响应点击事件", point.latitude
								+ "   " + point.longitude);
						// 重置页面控件的状态
						resetController(suggestionInfo.key, point);
					} else {
						Toast.makeText(BiumapActivity.this, "该项位置数据已丢失",
								Toast.LENGTH_SHORT);
					}

					// 让输入法消失
					// 关闭输入法
					InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(
							BiumapActivity.this.getCurrentFocus()
									.getWindowToken(),
							InputMethodManager.HIDE_NOT_ALWAYS);
				}
			});
			return view;
		}

		class ViewHolder {
			TextView descriptionView;
		}
	}

	// 定义showResult列表中的每一个数据项类
	private void resetController(String placeName, LatLng point) {
		// 背景恢复为灰色
		showResultList.clear();
		showResultAdapter.notifyDataSetChanged();
		biuMapView.requestFocus();
		// 点击一个结果项将试图的中心移动该处
		if (point != null) {
			mapCenter = new LatLng(point.latitude, point.longitude);
		} else {
			Toast.makeText(this, "该项位置数据已丢失", Toast.LENGTH_SHORT);
		}

		MapStatus biuMapStatus = new MapStatus.Builder().target(mapCenter)
				.zoom(16).build();
		MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
				.newMapStatus(biuMapStatus);
		biuBaiduMap.setMapStatus(mapStatusUpdate);
		this.placeName = placeName;
	}



}
