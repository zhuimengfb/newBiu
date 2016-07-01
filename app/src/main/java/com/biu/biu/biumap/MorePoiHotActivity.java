package com.biu.biu.biumap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.biu.biu.main.MyDateTimeDeal;
import com.biu.biu.main.TipItemInfo;
import com.biu.biu.morehottips.GetMoreHotThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.tools.AutoListView.OnLoadListener;
import com.biu.biu.tools.AutoListView.OnRefreshListener;
import com.biu.biu.userconfig.UserConfigParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import grf.biu.R;

public class MorePoiHotActivity extends Activity implements OnRefreshListener,
		OnLoadListener {

	final int AIRPLAY_MESSAGE_HIDE_TOAST = 2;
	// final int MORE_HOT_REFRESH = 3;
	private AutoListView mlistView;
	private MoreHotPoiAdapter mlistViewAdapter = null;
	private TextView mtabToptv = null; // Tab标题
	private ImageButton msubmitnew = null; // 提交新贴
	private ArrayList<TipItemInfo> mlistItemsinfo; // 帖子内容信息
	private Handler myHandler = null;
	private Toast mToast; // 自定义提示文本显示时间
	private GetMoreHotThread mGetThread;
	private JSONArray mjsonArray;
	private ImageButton mtabBackbt;
	private Button mtabTopbt;
	private int mOffset = 0;
	private boolean mfirstRefreshMore; // 只在首次启动时自动刷新一次
	// 定义游客的地理坐标

	private double lat;
	private double lng;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_hot);
		lat = getIntent().getDoubleExtra("lat", -1.0);
		lng = getIntent().getDoubleExtra("lng", -1.0);
		initHandler();
		findViewId();
		initView();
		mfirstRefreshMore = true;

	}

	private void initHandler() {
		// TODO Auto-generated method stub
		myHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case 0:
					// 操作执行成功
					try {
						mjsonArray = new JSONArray(mGetThread.getHttpResult());
						parseHttpResult();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					mlistView.onRefreshComplete();
					mlistView.onLoadComplete();
					break;
				case -1:
					// 操作执行失败
					String errorMsg = msg.getData().getString("e_msg");
					showToast(errorMsg, 1500); // 显示1.5秒
					break;
				case AIRPLAY_MESSAGE_HIDE_TOAST:
					cancelToast();
					break;
				// case MORE_HOT_REFRESH:
				// 下拉刷新

				}
			}

			// 转化帖子信息情况并更新view
			private void parseHttpResult() {
				// TODO Auto-generated method stub
				try {
					// ArrayList<Integer> topDownFlags = new
					// ArrayList<Integer>();
					MyDateTimeDeal timedeal = new MyDateTimeDeal();
					if (mOffset == 0)
						mlistItemsinfo.clear();
					for (int i = 0; i < mjsonArray.length(); i++) {
						JSONObject everyJsonObject = mjsonArray
								.getJSONObject(i);
						TipItemInfo item = new TipItemInfo();
						item.content = everyJsonObject.getString("content");
						item.created_at = timedeal
								.getTimeGapDesc(everyJsonObject
										.getString("created_at"));
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
						item.imgurl = everyJsonObject.getString("img_url");
						item.tread_num = everyJsonObject.getString("tread_num");
						item.updated_at = everyJsonObject
								.getString("updated_at");
						item.pubplace = everyJsonObject.getString("address");
						item.hasliked = everyJsonObject.getBoolean("has_liked");
						item.hastreaded = everyJsonObject
								.getBoolean("has_treaded");

						// if(everyJsonObject.getBoolean("has_liked")){
						// item.likestate = 1; // 顶
						// }else if(everyJsonObject.getBoolean("has_treaded")){
						// item.likestate = 2; // 踩
						// }else{
						// item.likestate = 0; // 无
						// }

						mlistItemsinfo.add(item);
					}
					mOffset += mjsonArray.length(); // 更新已获得的帖子数
					if (mlistItemsinfo.isEmpty()) {
						// 添加一个空选项
						TipItemInfo itemempty = new TipItemInfo();
						itemempty.isEmpty = true;
						mlistItemsinfo.add(itemempty);
					}
					mlistView.setResultSize(mjsonArray.length());
					mlistViewAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
	}

	private void findViewId() {
		// TODO Auto-generated method stub
		mlistView = (AutoListView) findViewById(R.id.morehotlistview);
		mtabToptv = (TextView) findViewById(R.id.TabTopTitle);
		mtabTopbt = (Button) findViewById(R.id.submit_new);
		mtabBackbt = (ImageButton) findViewById(R.id.publish_back);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (mfirstRefreshMore) { // 第一次启动时自动获取首页帖子信息
			getMoreHotFromServer(0);
			mfirstRefreshMore = false;
		}
		super.onResume();
	}

	/**
	 * 从服务器获取更多热帖
	 */
	private void getMoreHotFromServer(int nOffset) {
		// TODO Auto-generated method stub
		if (nOffset == 0)
			mOffset = 0;
		// 此处一次性请求帖子的数量改为30条
		String url = "http://api.bbbiu.com:1234/hot-threads?lat="
				+ UserConfigParams.latitude + "&lng="
				+ UserConfigParams.longitude + "&offset="
				+ String.valueOf(nOffset) + "&limit=" + String.valueOf(30)
				+ "&device_id=" + UserConfigParams.device_id;
		mGetThread = new GetMoreHotThread(myHandler, url);
		Thread thread = new Thread(mGetThread);
		thread.start();
	}

	private void initView() {
		// TODO Auto-generated method stub

		// 初始化TabTop相关描述文字
		mtabToptv.setText(R.string.title_morehot);
		mtabTopbt.setVisibility(Button.GONE);
		// 回退按钮
		mtabBackbt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				MorePoiHotActivity.this.finish();
			}

		});
		mlistItemsinfo = new ArrayList<TipItemInfo>();
		mlistViewAdapter = new MoreHotPoiAdapter(this, mlistItemsinfo,
				myHandler, lat, lng);
		mlistViewAdapter.setListView(mlistView);
		mlistView.setAdapter(mlistViewAdapter);
		mlistView.setOnRefreshListener(this);
		mlistView.setOnLoadListener(this);
		// mlistView.setfootVisibility(false); // 不显示下脚
		// 2015年10月7日：为防止误触，缩小触发范围为ContentTextView
		// mlistView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// // TODO Auto-generated method stub
		// int nPosition = position - 1; // 减去header
		// //
		// if(nPosition >= mlistItemsinfo.size())
		// return;
		// if(mlistItemsinfo.get(nPosition).isEmpty)
		// return;
		// Intent intent = new Intent();
		// intent.setClass(MoreHotActivity.this, TipDetailActivity.class);
		// intent.putExtra("thread_id", mlistItemsinfo.get(nPosition).id);
		// MoreHotActivity.this.startActivity(intent);
		// }
		//
		// });
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		// 更多热帖暂无加载更多接口，直接结束操作
		getMoreHotFromServer(mOffset);
	}

	@Override
	public void onRefresh() {
		// // TODO Auto-generated method stub
		// Message msg = myHandler.obtainMessage();
		// msg.what = MORE_HOT_REFRESH;
		// myHandler.sendMessage(msg);
		getMoreHotFromServer(0);
	}

	/*
	 * 显示Toast
	 */
	public void showToast(String text, long showlength) {
		if (mToast == null) {
			mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		} else {
			mToast.setText(text);
			mToast.setDuration(Toast.LENGTH_SHORT);
		}
		mToast.show();

		Message delayMsg = myHandler.obtainMessage(AIRPLAY_MESSAGE_HIDE_TOAST);
		myHandler.sendMessageDelayed(delayMsg, 500);
	}

	// 终止显示Toast文本提示
	public void cancelToast() {
		if (mToast != null) {
			mToast.cancel();
		}
	}
}
