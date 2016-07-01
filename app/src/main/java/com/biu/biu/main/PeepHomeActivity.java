package com.biu.biu.main;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.biu.biu.netimage.ImageDownloader;
import com.biu.biu.thread.GetPeepThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.tools.AutoListView.OnLoadListener;
import com.biu.biu.tools.AutoListView.OnRefreshListener;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.views.base.BaseActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import grf.biu.R;

public class PeepHomeActivity extends BaseActivity implements OnRefreshListener,
															  OnLoadListener {

	private class PeepPublishClickListener implements OnClickListener {
		private boolean flag = true;

		// 计时线程，1秒钟只能点一次
		private class TimeThread extends Thread {
			public void run() {
				try {
					Thread.sleep(1000);
					flag = true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private synchronized void setFlag() {
			flag = false;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (!flag) {
				return;
			} else {
				setFlag();
				new TimeThread().start();
			}
			Intent intent = new Intent();
			intent.putExtra("topic_id", mTopic_id);
			intent.putExtra("PublishMode",
					PublishTopicActivity.PUBLISH_FOR_PEEPTOPIC);
			intent.setClass(PeepHomeActivity.this, PublishTopicActivity.class);
			startActivity(intent);
		}
	}

	ImageDownloader mDownloader;
	private AutoListView mListView;
	private PeepListAdapter mListviewAdapter;
	private ImageButton mSubNewBtn;
	private ImageButton mBackBtn;
	private int mPeepHomeGettedCount = 0; // 已经得到的项目数量
	private PeepHomeHander mPeepHomeHandler;
	private ArrayList<TipItemInfo> mTipItems;
	private boolean mthreadisrunning = false;
	private String mTopic_id;
	private JSONArray mJsonArray;
	GetPeepThread mGetThread = null;
	private boolean mfirstRefresh = true;
	private TextView mTitletv;
	private String mTopicTitle;
	private final static int MSG_GET_OK_0 = 0; // 清空现有数据并获得新数据
	private final static int MSG_GET_OK_1 = 1; // 将新数据添加到现有数据
	private final static int MSG_GET_ERROR = -1;

	// 发表帖子“+”新的响应区域、
	private LinearLayout peepHomePublishBt;

	@BindView(R.id.peep_toolbar)
	Toolbar toolbar;
	@BindView(R.id.fab_add_topic)
	FloatingActionButton addTopicFab;
	@BindView(R.id.peep_title)
	TextView peepTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peep_home);
		ButterKnife.bind(this);
		findId();
		intView();
		initToolbar();
		initParam();
		peepTitle.setText(mTopicTitle);
		initEvent();
		//mTitletv.setText(mTopicTitle); // 设置标题
	}
	private void initToolbar(){
		toolbar.setTitle("");
		setSupportActionBar(toolbar);
		setBackableToolbar(toolbar);
	}
	private void initEvent() {
		addTopicFab.setOnClickListener(new PeepPublishClickListener());
	}
	private void initParam() {
		// TODO Auto-generated method stub
		Intent intent = this.getIntent();
		mTopic_id = intent.getStringExtra("topic_id"); // 帖子ID
		String title = intent.getStringExtra("TopicTitle");
		if (title.length() >= 10) {
			title = title.substring(0, 9) + "…";
		}
		mTopicTitle = title;
	}

	private void intView() {
		// TODO Auto-generated method stub
		mPeepHomeHandler = new PeepHomeHander(this);
		mTipItems = new ArrayList<TipItemInfo>();
		mListviewAdapter = new PeepListAdapter(this, mTipItems);
		mListviewAdapter.setListView(mListView);
		mListView.setAdapter(mListviewAdapter);
		mListView.setOnRefreshListener(this);
		mListView.setOnLoadListener(this);
		mListView.setPageSize(30);
		mPeepHomeGettedCount = 0;
		/*// 发表新话题
		mSubNewBtn.setOnClickListener(new PeepPublishClickListener());
		// 新的响应区域发帖添加单击事件
		peepHomePublishBt.setOnClickListener(new PeepPublishClickListener());
		// 点击回退
		mBackBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});*/

		// // 点击进入话题的详情页
		// mListView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view,
		// int position, long id) {
		// // TODO Auto-generated method stub
		// int nPosition = position - 1; // 减去header
		// Intent intent = new Intent();
		// intent.setClass(PeepHomeActivity.this, PeepDetailActivity.class);
		// intent.putExtra("thread_id", mTipItems.get(nPosition).id);
		// intent.putExtra("DetailMode",
		// PeepDetailActivity.TIPDETAIL_FOR_PEEPTOPIC);
		// startActivity(intent);
		// }
		// });
	}

	private void findId() {
		// TODO Auto-generated method stub
		mListView = (AutoListView) findViewById(R.id.peep_showtopic_lv);
		/*mSubNewBtn = (ImageButton) findViewById(R.id.submit_new);
		mBackBtn = (ImageButton) findViewById(R.id.publish_back);*/
		//mTitletv = (TextView) findViewById(R.id.TabTopTitle);
		/*peepHomePublishBt = (LinearLayout) this
				.findViewById(R.id.peep_home_publish_bt);*/
	}

	public class PeepHomeHander extends Handler {
		WeakReference<PeepHomeActivity> mWeakref;

		public PeepHomeHander(PeepHomeActivity activity) {
			// TODO Auto-generated constructor stub
			mWeakref = new WeakReference<PeepHomeActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int nMsg = msg.what;
			PeepHomeActivity activity = mWeakref.get();
			switch (nMsg) {
			case MSG_GET_OK_0: // 清除现有数据并添加
				activity.addNewData(true); // 参数表示是否清除当前已有数据
				break;
			case MSG_GET_OK_1: // 直接添加
				activity.addNewData(false);
				break;
			// case MSG_NOTIFY_LIST:
			// monfr
			case MSG_GET_ERROR:
				break;
			}
			mthreadisrunning = false;
			super.handleMessage(msg);
		}

	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		getPeepItem(mPeepHomeGettedCount);
	}

	/**
	 * 
	 * @param isclear
	 *            : 指定是否要清除数据缓存
	 */
	public void addNewData(boolean isclear) {
		// TODO Auto-generated method stub
		if (isclear) {
			mTipItems.clear();
			mPeepHomeGettedCount = 0;
		}
		try {
			MyDateTimeDeal timedeal = new MyDateTimeDeal();
			mJsonArray = mGetThread.getmJsonArray();
			for (int i = 0; i < mJsonArray.length(); i++) {
				JSONObject everyJsonObject = mJsonArray.getJSONObject(i);
				TipItemInfo item = new TipItemInfo();
				item.content = everyJsonObject.getString("content");
				item.device_id = everyJsonObject.getString("device_id");
				item.reply_num = everyJsonObject.getString("reply_num");
				Integer likeresult = Integer.parseInt(everyJsonObject
						.getString("like_num"))
						- Integer.parseInt(everyJsonObject
								.getString("tread_num"));
				item.like_num = likeresult.toString();
				item.tread_num = everyJsonObject.getString("tread_num");
				item.ishot = everyJsonObject.getString("is_hot").equals("1");
				item.imgurl = everyJsonObject.getString("img_url");
				item.topic_id = everyJsonObject.getString("topic_id");
				item.id = everyJsonObject.getString("id");
				item.created_at = timedeal.getTimeGapDesc(everyJsonObject
						.getString("created_at"));
				item.hasliked = everyJsonObject.getBoolean("has_liked");
				item.hastreaded = everyJsonObject.getBoolean("has_treaded");
				// if (isclear && i == 2) {
				// item.isDisplayMore = true;
				// }
				mTipItems.add(item);
			}

			// 此处还有疑问

			if (isclear) {
				if (mJsonArray.length() >= 3)
					mPeepHomeGettedCount = mJsonArray.length() - 3;
			} else {
				mPeepHomeGettedCount += mJsonArray.length();
			}

			if (mTipItems.isEmpty()) {
				// 添加一个空选项
				TipItemInfo itemempty = new TipItemInfo();
				itemempty.isEmpty = true;
				mTipItems.add(itemempty);
			}
			mListView.setResultSize(mJsonArray.length());
			mListView.onRefreshComplete();
			mListView.onLoadComplete();
			// 若处理完成之后仍然无数据，则显示空数据提示

			mListviewAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获得指定新鲜事话题中的主题内容
	 * 
	 * @param GettedCount
	 */
	private void getPeepItem(int GettedCount) {
		// TODO Auto-generated method stub
		if (mthreadisrunning)
			return;
		mthreadisrunning = true;
		// 根据坐标位置请求话题帖子
		mGetThread = new GetPeepThread(mPeepHomeHandler, mPeepHomeGettedCount,
				mTopic_id, Double.valueOf(UserConfigParams.latitude),
				Double.valueOf(UserConfigParams.longitude));
		mGetThread.start();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		mPeepHomeGettedCount = 0;
		getPeepItem(0);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		// 获取首页帖子信息
		if (mfirstRefresh) { // 第一次启动时自动获取首页帖子信息
			onRefresh();
			mfirstRefresh = false;
		} else {
			// 如果不是第一次启动，但是在发表新帖之后，也进行一次获取首页刷新。
			if (UserConfigParams.isHomeRefresh) {
				UserConfigParams.isHomeRefresh = false;
				onRefresh();
			}
		}
		super.onResume();
	}

	public String getTopicId() {
		return mTopic_id;
	}
}
