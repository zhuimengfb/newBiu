package com.biu.biu.morehottips;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.biu.biu.main.MyDateTimeDeal;
import com.biu.biu.main.PeepListAdapter;
import com.biu.biu.main.TipItemInfo;
import com.biu.biu.thread.GetPeepMoreHotThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.tools.AutoListView.OnLoadListener;
import com.biu.biu.tools.AutoListView.OnRefreshListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import grf.biu.R;

public class MoreHotPeepTopicActivity extends Activity implements OnRefreshListener, OnLoadListener {
	private AutoListView mListView;
	private boolean mfirstRefreshMore;				// 只在首次启动时自动刷新一次
	private PeepListAdapter mListViewAdapter = null;
	private ArrayList<TipItemInfo> mlistItemsinfo; // 帖子内容信息
	private Handler myHandler = null;
	private int mOffset = 0;
	private TextView mtabToptv;
	private Button mtabTopbt;
	private ImageButton mtabBackbt;
	private String mTopicId;
	private boolean mthreadisrunning = false;		// 线程是否正在运行
	private GetPeepMoreHotThread mGetThread;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_hot_peep_topic);
		initHandler();
		findId();
		initView();
		initParam();
		mfirstRefreshMore = true;
	}
	private void initParam() {
		// TODO Auto-generated method stub
		Intent intent = this.getIntent();
		mTopicId = intent.getStringExtra("topic_id");		// 话题所属ID
	}
	private void initHandler() {
		// TODO Auto-generated method stub
		myHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch(msg.what){
				case 0:
					mListView.onRefreshComplete();
					parseHttpResult(true);		// 刷新
					break;
				case 1:
					// 操作执行成功
					mListView.onLoadComplete();
					parseHttpResult(false);		// 加载更多
					break;
				case -1:
					// 操作执行失败
//					String errorMsg = msg.getData().getString("e_msg");
//					showToast(errorMsg, 1500);	// 显示1.5秒
					break;
					
//				case AIRPLAY_MESSAGE_HIDE_TOAST:
//					cancelToast();
//					break;
//				case MORE_HOT_REFRESH:
					// 下拉刷新
				}
				mthreadisrunning = false;
			}

			private void parseHttpResult(boolean isClear) {
				// TODO Auto-generated method stub
				if(isClear){
					mOffset = 0;
					mlistItemsinfo.clear();
					mListView.onRefreshComplete();
				}else {
					mListView.onLoadComplete();
				}
				JSONArray jsonarray = new JSONArray();
				try {
					MyDateTimeDeal timedeal = new MyDateTimeDeal();
					jsonarray = mGetThread.getmJsonArray();
					for (int i = 0; i < jsonarray.length(); i++) {
						JSONObject everyJsonObject = jsonarray.getJSONObject(i);
						TipItemInfo item = new TipItemInfo();
						item.content = everyJsonObject.getString("content");
						item.device_id = everyJsonObject.getString("device_id");
						item.reply_num = everyJsonObject.getString("reply_num");
						Integer likeresult = Integer.parseInt(everyJsonObject.getString("like_num")) - Integer.parseInt(everyJsonObject.getString("tread_num"));
						item.like_num = likeresult.toString();
						item.tread_num = everyJsonObject.getString("tread_num");
						item.ishot = everyJsonObject.getString("is_hot").equals("1");
						item.imgurl = everyJsonObject.getString("img_url");
						item.topic_id = everyJsonObject.getString("topic_id");
						item.id = everyJsonObject.getString("id");
						item.created_at = timedeal.getTimeGapDesc(everyJsonObject.getString("created_at"));
						item.hasliked = everyJsonObject.getBoolean("has_liked");
						item.hastreaded = everyJsonObject.getBoolean("has_treaded");
//						if (i == 2){
//							item.isDisplayMore = true;
//						}
						mlistItemsinfo.add(item);
					}
					mOffset+= jsonarray.length();
					mListView.setResultSize(jsonarray.length());
					// 若处理完成之后仍然无数据，则显示空数据提示
					
					mListViewAdapter.notifyDataSetChanged();
				} catch (Exception e) {
					e.printStackTrace();
				}	
					
			}
		};
	}
	private void initView() {
		// TODO Auto-generated method stub
		// 初始化TabTop相关描述文字
		mtabToptv.setText(R.string.title_more_peep_topic);
				
		mtabTopbt.setVisibility(Button.GONE);
		// 回退按钮
		mtabBackbt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		mlistItemsinfo = new ArrayList<TipItemInfo>();
		mListViewAdapter = new PeepListAdapter(this,
				mlistItemsinfo);
		mListViewAdapter.setListView(mListView);
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnRefreshListener(this);
		mListView.setOnLoadListener(this);
//		mListView.setfootVisibility(false);	// 不显示下边角
		// 单击项目进入详情页
//		mListView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//				// TODO Auto-generated method stub
//				int nPosition = position - 1;		// 减去header
//				if(nPosition >= mlistItemsinfo.size())
//					return;
//				Intent intent = new Intent();
//				intent.setClass(MoreHotPeepTopicActivity.this, PeepDetailActivity.class);
//				intent.putExtra("thread_id", mlistItemsinfo.get(nPosition).id);
//				startActivity(intent);
//			}
//		});
	}
	private void findId() {
		// TODO Auto-generated method stub
		mListView = (AutoListView)findViewById(R.id.morehotpeeptopiclistview);
		mtabToptv = (TextView)findViewById(R.id.TabTopTitle);   // 页面标题
		mtabTopbt = (Button)findViewById(R.id.submit_new);		// 发表新帖(更多热帖时不显示)
		mtabBackbt = (ImageButton)findViewById(R.id.publish_back);	
	}
	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		// 更多热帖暂无加载更多接口，直接结束操作
		getMoreHotFromServer(mOffset);
	}
	private void getMoreHotFromServer(int mOffset2) {
		// TODO Auto-generated method stub
		if(mthreadisrunning)
			return;
		mthreadisrunning = true;
		mGetThread = new GetPeepMoreHotThread(myHandler, mOffset, mTopicId);
		mGetThread.start();
	}
	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		mOffset = 0;
		getMoreHotFromServer(0);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if(mfirstRefreshMore ){	// 第一次启动时自动获取首页帖子信息
			onRefresh();
			mfirstRefreshMore = false;
		}
		super.onResume();
	}
}
