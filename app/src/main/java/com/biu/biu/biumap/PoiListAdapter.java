package com.biu.biu.biumap;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.biu.biu.main.PeepDetailActivity;
import com.biu.biu.main.TipItemInfo;
import com.biu.biu.morehottips.MoreHotActivity;
import com.biu.biu.netimage.ImageDownloader;
import com.biu.biu.netimage.OnImageDownload;
import com.biu.biu.netoperate.TipLikeTreadThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.userconfig.UserConfigParams;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.util.ArrayList;

import grf.biu.R;

/*
 * Home页的ListView的Adapter,自定义的适配器一般继承BaseAdapter类
 */
public class PoiListAdapter extends BaseAdapter {
	// private HomeListAdapter mAdapter = this; // 用于内部类的引用
	private ArrayList<TipItemInfo> mlistItemsinfo; // 信息集合
	private Context context; // 运行上下文
	private LayoutInflater listContainer; // 视图容器工厂
	private Handler myhandler = null;
	// private String hosturl = "http://api.bbbiu.com:1234"; // 主机域名
	private AutoListView mListView = null;
	// private ArrayList<HomeTopbtnListener> mTopbtnListener;
	// private ArrayList<HomeDownbtnListener> mDownbtnListener;
	private ArrayList<Integer> TopOrDownState = null; // 0：未操作；1：顶；2：踩
	private final int TYPE_MOREHOT = 0;
	private final int TYPE_SIMPLEITEM = 1;
	private final int TYPE_ITEM_COUNT = 2;
	View convertView = null;
	ImageDownloader mDownloader;

	public void setListView(AutoListView listview) {
		this.mListView = listview;
	}

	private final class ListItemView {

		// public boolean noItemInfo; // 没有内容时的提示
		public TextView homeContenttv; // 内容控件
		public ImageView img; // 图片控件
		public TextView publishTimetv; // 发表时间
		public TextView replayCounttv; // 回复时间
		public TextView TopCounttv; // 顶的数量
		public TextView DownCounttv; // 踩的计数
		public TextView publishPlacetv; // 发表时间
		public ImageView pubtimeImg; // 发表时间图标
		public ImageView replyImg; // 回复数图标
		public ImageButton hometopbtn; // 顶按钮
		public ImageButton homedownbtn; // 踩按钮
		public TextView morehottips; // 更多热帖
		public RelativeLayout biumain = null; // 响应单击事件进入详情页的部分
	}

	public PoiListAdapter(Context context, ArrayList<TipItemInfo> topicInfo) {
		// 将list_item.xml布局作为一个视图，添加到listView中。
		this.context = context;
		this.mlistItemsinfo = topicInfo;
		listContainer = LayoutInflater.from(context); // 创建视图容器工厂并设置上下文
		TopOrDownState = new ArrayList<Integer>();
		// convertView = listContainer.inflate(R.layout.homelistitemlayout,
		// null); // 创建list_item.xml布局文件视图

	}

	/**
	 * 供activity使用
	 * 
	 * @param context
	 * @param listItems
	 */
	// public HomeListAdapter(Context context, ArrayList<HashMap<String,
	// Object>> listItems){
	// this.context = context;
	// this.mlistItemsinfo = listItems;
	// listContainer = LayoutInflater.from(context);
	// TopOrDownState = new ArrayList<Integer>();
	// convertView = listContainer.inflate(R.layout.homelistitemlayout, null);
	// }

	/**
	 * 设置HomeFragment的Handler，用于发送目标消息。
	 * 
	 * @param handler
	 */
	public void setHandler(Handler handler) {
		this.myhandler = handler;
	}

	/**
	 * 设置每个帖子相对这个设备ID的顶赞状态
	 * 
	 * @param arg0
	 */
	public void setTopOrDownStateArray(ArrayList<Integer> arg0) {
		this.TopOrDownState = arg0;
	}

	// /**
	// * 由帖子信息列表构造适配器
	// * @param context
	// * @param tipInfo
	// */
	// public HomeListAdapter(Context context, ArrayList<TipItemInfo> tipInfo){
	//
	// }

	/*
	 * 获得顶、赞缓存对象
	 */
	public ArrayList<Integer> getLikeTreadSaveBuffer() {
		return TopOrDownState;
	}

	/**
	 * 获得数据缓存对象
	 * 
	 * @return
	 */
	public ArrayList<TipItemInfo> getListItems() {
		return mlistItemsinfo;
	}

	@Override
	/*
	 * ListView Item设置
	 * 
	 * @see android.widget.Adapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		// 自定义视图
		boolean isShowMore = false;
		ListItemView listItemView = null; // 与ViewHolder类似
		isShowMore = mlistItemsinfo.get(position).isDisplayMore;
		if (convertView == null) {
			listItemView = new ListItemView();
			// 获取list_item布局文件的视图
			if (isShowMore)
				// convertView =
				// listContainer.inflate(R.layout.homelistmorehotitemlayout,
				// null);
				convertView = listContainer.inflate(
						R.layout.peeptopicitemhotlayout, null);
			else
				convertView = listContainer.inflate(
						R.layout.peeptopicitemlayout, null);
			// convertView = listContainer.inflate(R.layout.homelistitemlayout,
			// null);

			// 获取控件对象
			listItemView.homeContenttv = (TextView) convertView
					.findViewById(R.id.topiccontent);
			listItemView.img = (ImageView) convertView
					.findViewById(R.id.topicimg);
			listItemView.publishPlacetv = (TextView) convertView
					.findViewById(R.id.publishplace);
			listItemView.publishTimetv = (TextView) convertView
					.findViewById(R.id.create_at_tv);
			listItemView.replayCounttv = (TextView) convertView
					.findViewById(R.id.reply_num_tv);
			listItemView.biumain = (RelativeLayout) convertView
					.findViewById(R.id.biumain);
			// 将这三个控件隐藏
			listItemView.hometopbtn = (ImageButton) convertView
					.findViewById(R.id.likebtn);
			listItemView.hometopbtn.setVisibility(View.GONE);
			listItemView.homedownbtn = (ImageButton) convertView
					.findViewById(R.id.treadbtn);
			listItemView.homedownbtn.setVisibility(View.GONE);
			listItemView.TopCounttv = (TextView) convertView
					.findViewById(R.id.likecounttv);
			listItemView.TopCounttv.setVisibility(View.GONE);
			if (isShowMore) {
				listItemView.morehottips = (TextView) convertView
						.findViewById(R.id.morehottv);
				listItemView.morehottips
						.setOnClickListener(new MoreHotTipsClickListener());
			}
			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		// 设置默认显示资源图片
		// listItemView.hometopbtn.setImageResource(R.drawable.home_icon3);
		// listItemView.homedownbtn.setImageResource(R.drawable.home_icon4);
		if (mlistItemsinfo.get(position).isEmpty) {
			// 无数据
			listItemView.homeContenttv.setText("您所在位置还没有发帖的人，请按右上角 + 第一个发贴吧");
			listItemView.publishPlacetv.setVisibility(TextView.GONE); // 位置信息
			listItemView.replayCounttv.setVisibility(TextView.GONE); // 回复数
			// listItemView.DownCounttv.setVisibility(TextView.GONE); // 踩数
			listItemView.TopCounttv.setVisibility(TextView.GONE); // 顶数
			listItemView.homedownbtn.setVisibility(ImageButton.GONE);
			listItemView.hometopbtn.setVisibility(ImageButton.GONE);
			listItemView.img.setVisibility(View.GONE);
			// 发表时间图标
			// listItemView.pubtimeImg.setVisibility(ImageView.GONE);
			// 回复数图标
			// listItemView.replyImg.setVisibility(ImageView.GONE);
		} else {
			// 显示所有控件
			listItemView.publishPlacetv.setVisibility(TextView.VISIBLE); // 位置信息
			listItemView.replayCounttv.setVisibility(TextView.VISIBLE); // 回复数
			// listItemView.DownCounttv.setVisibility(TextView.VISIBLE); // 踩数
			listItemView.TopCounttv.setVisibility(TextView.GONE); // 顶数
			listItemView.homedownbtn.setVisibility(ImageButton.GONE);
			listItemView.hometopbtn.setVisibility(ImageButton.GONE);
			// 发表时间图标
			// listItemView.pubtimeImg.setVisibility(ImageView.VISIBLE);
			// 回复数图标
			// listItemView.replyImg.setVisibility(ImageView.VISIBLE);

			// 设置动态改变资源
			boolean blikestate = mlistItemsinfo.get(position).hasliked;
			boolean btreadstate = mlistItemsinfo.get(position).hastreaded;
			if (blikestate) {
				listItemView.hometopbtn
						.setImageResource(R.drawable.arrow1click);
			} else {
				listItemView.hometopbtn.setImageResource(R.drawable.arrow1);
			}
			// 设置踩的状态
			if (btreadstate) {
				listItemView.homedownbtn
						.setImageResource(R.drawable.arrow2click);
			} else {
				listItemView.homedownbtn.setImageResource(R.drawable.arrow2);
			}
			// 设置Topic内容
			if (position < 3) {
				String contenttemp = mlistItemsinfo.get(position).content;
				contenttemp = "<img src=\"" + R.drawable.hot_icon + "\" />"
						+ contenttemp;
				listItemView.homeContenttv.setText(Html.fromHtml(contenttemp,
						imageGetter, null));
			} else {
				listItemView.homeContenttv
						.setText(mlistItemsinfo.get(position).content);
			}

			// 设置发表时间、回复数、顶、踩数量
			Integer topcount = Integer
					.parseInt(mlistItemsinfo.get(position).like_num);
			// Integer downcount =
			// Integer.parseInt(mlistItemsinfo.get(position).tread_num);
			listItemView.publishTimetv
					.setText(mlistItemsinfo.get(position).created_at);
			listItemView.replayCounttv
					.setText(mlistItemsinfo.get(position).reply_num + "条回复");
			listItemView.TopCounttv.setText(topcount.toString());
			// listItemView.DownCounttv.setText(downcount.toString());
			String itemPlace = mlistItemsinfo.get(position).pubplace;
			if (itemPlace.isEmpty() || itemPlace.equals("null")) {
				listItemView.publishPlacetv.setVisibility(TextView.INVISIBLE);
			} else {
				listItemView.publishPlacetv.setVisibility(TextView.VISIBLE);
				listItemView.publishPlacetv.setText(itemPlace);
			}

			// 给Button添加单击事件，添加Button之后ListView将失去焦点，需要将Button的焦点去掉
			int nlikestate = 0;
			if (blikestate) {
				nlikestate = 1;
			} else {
				nlikestate = btreadstate ? -1 : 0;
			}
			String tipid = mlistItemsinfo.get(position).id;
			listItemView.hometopbtn.setOnClickListener(new HomeTopbtnListener(
					position, tipid, nlikestate));
			listItemView.homedownbtn
					.setOnClickListener(new HomeDownbtnListener(position,
							tipid, nlikestate));
			//区域添加事件进入帖子详情
			listItemView.biumain
			.setOnClickListener(new HomeContentClickListener(tipid));
			// 给内容按钮添加单击事件
//			listItemView.homeContenttv
//					.setOnClickListener(new HomeContentClickListener(tipid));
//			listItemView.img.setOnClickListener(new HomeContentClickListener(
//					tipid));
			// 处理图片
			String urltemp = mlistItemsinfo.get(position).imgurl;
			if (urltemp != null && !urltemp.equals("null")) {
				final String url = "http://api.bbbiu.com:1234/" + urltemp;
				listItemView.img.setTag(String.valueOf(position)); // 用索引作为标记
				// listItemView.img.setImageResource(android.R.drawable.stat_sys_download_done);
				if (mDownloader == null) {
					mDownloader = new ImageDownloader();
				}
				// 异步下载图片
				mDownloader.imageDownload(String.valueOf(position), url,
						listItemView.img, context.getExternalCacheDir()
								.getPath(), (Activity) this.context,
						new OnImageDownload() {
							@Override
							public void onDownloadSucc(String tag,
									Bitmap bitmap, String c_url,
									ImageView mimageView) {
								ImageView imageView = (ImageView) mListView
										.findViewWithTag(tag);
								if (imageView != null) {
									imageView.setVisibility(View.VISIBLE);
									imageView.setImageBitmap(bitmap);
									imageView.setTag("");
								}
							}
						});
			} else {
				listItemView.img.setVisibility(View.GONE);
			}
		}
		return convertView;
	}

	// public void setHomeTopicInfo(HomeTopicInfo topicInfo){
	// mHomeTopicInfo = topicInfo;
	// }

	private class HomeContentClickListener implements OnClickListener {
		private String mTipId = "";

		public HomeContentClickListener(String tipid) {
			// TODO Auto-generated constructor stub
			mTipId = tipid;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(context, PeepDetailActivity.class);
			intent.putExtra("thread_id", mTipId);
			intent.putExtra("DetailMode",
					PeepDetailActivity.TIPDETAIL_FOR_HOMETIP);
			intent.putExtra("localornot", false);
			context.startActivity(intent);
		}

	}

	/*
	 * 顶按钮的监听器
	 */
	private class HomeTopbtnListener implements OnClickListener {
		private Integer mtopcount = 0; // 当前的顶贴数
		int mPosition = 0;
		String mtipid = null;
		boolean mhasliked = false;
		private boolean flag = true;
		private int mlikeState = 0; // 0：没踩没顶 1：顶了 -1：踩了
		private String murl = "";

		public HomeTopbtnListener(int nPosition, int topcount, String tread_id,
				boolean hasliked) {
			this.mtipid = tread_id;
			mPosition = nPosition;
			this.mtopcount = topcount;
			// Log.i("topcount", "创建监听器时：" + mtopcount.toString());
			this.mhasliked = hasliked;
			// this.nlikestate = lkstate;
		}

		/**
		 * 顶、赞分别计数更改为单独计数的增补构造函数
		 * 
		 * @param position
		 *            ：处理之后要更新的当前帖子内容标识
		 * @param tipid
		 *            ：帖子ID
		 * @param nlikestate
		 *            ：0：无历史操作；1：已顶 -1：已踩
		 */
		public HomeTopbtnListener(int position, String tipid, int nlikestate) {
			this.mPosition = position;
			this.mlikeState = nlikestate;
			this.mtipid = tipid;
		}

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

		/**
		 * 顶贴
		 */
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (!flag) {
				return;
			} else {
				setFlag();
				new TimeThread().start();
			}
			murl = "http://api.bbbiu.com:1234/threads/" + mtipid;
			Integer nlikenum = Integer
					.parseInt(mlistItemsinfo.get(mPosition).like_num);
			switch (mlikeState) {
			case -1:
				// 当前为踩，点击顶，取消踩。
				murl += "/action:tread" + "/?device_id="
						+ UserConfigParams.device_id + "&is_repeal=1";
				++nlikenum;
				mlistItemsinfo.get(mPosition).hastreaded = false;
				break;
			case 0:
				// 当前无，点击顶，设为顶
				murl += "/action:like" + "/?device_id="
						+ UserConfigParams.device_id;
				++nlikenum;
				mlistItemsinfo.get(mPosition).hasliked = true;
				break;
			case 1:
				// 当前顶，点击顶，取消顶
				murl += "/action:like" + "/?device_id="
						+ UserConfigParams.device_id + "&is_repeal=1";
				--nlikenum;
				mlistItemsinfo.get(mPosition).hasliked = false;
				break;
			}
			mlistItemsinfo.get(mPosition).like_num = nlikenum.toString();
			// 更新显示ListView的状态
			PoiListAdapter.this.notifyDataSetChanged();
			TipLikeTreadThread mthread = new TipLikeTreadThread(myhandler,
					mtipid, mPosition, murl);
			// mthread.setSendMessage(false); // 只执行操作，不发送消息
			Thread thread = new Thread(mthread);
			thread.start();
		}
	}

	/*
	 * 踩按钮的监听器
	 */
	private class HomeDownbtnListener implements OnClickListener {
		private Integer mdowncount = 0; // 当前踩贴数
		private int mPosition = 0;
		private String mtipid = null;
		TextView mtreadCounttv = null;
		private int mlikeState = 0; // 0：没踩没顶 1：顶了 -1：踩了
		private boolean flag = true;
		private String murl = "";

		public HomeDownbtnListener(int position, String tipid, int nlikestate) {
			this.mPosition = position;
			this.mtipid = tipid;
			this.mlikeState = nlikestate;
		}

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

		/**
		 * 踩贴
		 */
		@Override
		public void onClick(View v) {
			if (!flag) {
				return;
			} else {
				setFlag();
				new TimeThread().start();
			}
			murl = "http://api.bbbiu.com:1234/threads/" + mtipid;
			Integer nlikenum = Integer
					.parseInt(mlistItemsinfo.get(mPosition).like_num);
			// 当前处于顶的状态
			switch (mlikeState) {
			case -1:
				// 当前为踩，点击踩，取消踩。
				murl += "/action:tread" + "/?device_id="
						+ UserConfigParams.device_id + "&is_repeal=1";
				++nlikenum;
				mlistItemsinfo.get(mPosition).hastreaded = false;
				break;
			case 0:
				// 当前无，点击踩，设为踩
				murl += "/action:tread" + "/?device_id="
						+ UserConfigParams.device_id;
				--nlikenum;
				mlistItemsinfo.get(mPosition).hastreaded = true;
				break;
			case 1:
				// 当前顶，点击踩，取消顶
				murl += "/action:like" + "/?device_id="
						+ UserConfigParams.device_id + "&is_repeal=1";
				--nlikenum;
				mlistItemsinfo.get(mPosition).hasliked = false;
				break;
			}
			mlistItemsinfo.get(mPosition).like_num = nlikenum.toString();
			// 更新显示ListView的状态
			PoiListAdapter.this.notifyDataSetChanged();
			TipLikeTreadThread mthread = new TipLikeTreadThread(myhandler,
					mtipid, mPosition, murl);
			// mthread.setSendMessage(false); // 只执行操作，不发送消息
			Thread thread = new Thread(mthread);
			thread.start();

		}

	}

	/**
	 * 显示更多热帖
	 * 
	 * @author grf
	 * 
	 */
	private class MoreHotTipsClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(context, MoreHotActivity.class);
			intent.putExtra("localornot", false);
			context.startActivity(intent);
		}

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mlistItemsinfo.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * 修改赞/踩状态
	 */
	private void topDownStateChange(int changedID, int newState) {
		TopOrDownState.set(changedID, newState);

	}

	/**
	 * 自定义Handler，处理顶、赞事件
	 * 
	 * @author grf
	 * 
	 */
	class MyHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}

	}

	/*
	 * 顶贴线程
	 */
	class PutupThread implements Runnable {
		private Handler mhandler = null;
		private String thread_id = null; // 帖子ID
		private String url = null;

		// private int mposition = 0;
		public PutupThread(Handler targethandler, String threadid, String puturl) {
			this.mhandler = targethandler;
			this.thread_id = threadid;
			this.url = puturl;
			// this.mposition = nPosition;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			HttpPut httpput = new HttpPut(url);
			HttpClient httpClient = new DefaultHttpClient();
			HttpResponse httpResponse = null;

			try {
				// HttpClient发出一个HttpGet请求
				httpResponse = httpClient.execute(httpput);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 得到httpResponse的状态响应码
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				// 刷新首页
				Message msg = Message.obtain();
				msg.what = 7;
				// errordesc = e.getMessage() + "执行请求错误";
				// 通过Handler发布传送消息，handler
				// this.mhandler.sendMessage(msg);
				// HashMap<String, Object> map = new HashMap<String, Object>();
				// map.put("hasliked", true);
				// mlistItemsinfo.set(mposition, map);
				// mlistItemsinfo.get(mposition).("hasliked")
			} else {
				// 不存在关联的发表帖子，关闭此activity
			}
		}

	}

	/*
	 * 踩贴操作线程
	 */
	class TreadDownThread implements Runnable {
		private Handler mhandler = null;
		private String thread_id = null; // 帖子ID
		private String url = null;

		/**
		 * 构造函数
		 * 
		 * @param targethandler
		 *            ：处理消息的handler
		 * @param threadid
		 *            ：帖子ID
		 * @param treadurl
		 *            :操作的url地址
		 */
		public TreadDownThread(Handler targethandler, String threadid,
				String treadurl) {
			this.mhandler = targethandler;
			this.thread_id = threadid;
			this.url = treadurl;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			JSONObject resultJsonObject = null;
			HttpPut httpput = new HttpPut(url);
			HttpClient httpClient = new DefaultHttpClient();
			StringBuilder urlStringBuilder = new StringBuilder(url);
			StringBuilder entityStringBuilder = new StringBuilder();
			BufferedReader bufferedReader = null;
			HttpResponse httpResponse = null;

			try {
				// HttpClient发出一个HttpGet请求
				httpResponse = httpClient.execute(httpput);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 得到httpResponse的状态响应码
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				// 成功，更新帖子状态
				Message msg = Message.obtain();
				msg.what = 7;
				// errordesc = e.getMessage() + "执行请求错误";
				// 通过Handler发布传送消息，handler
				// this.mhandler.sendMessage(msg);
			} else {
				// 不存在关联的发表帖子，关闭此activity
				// Toast.makeText(context, "服务器错误，操作失败！",
				// Toast.LENGTH_SHORT).show();
				// Toast.makeText(this, , duration)（还是通过发消息到activity里面。）
			}
		}

	}

	final Html.ImageGetter imageGetter = new Html.ImageGetter() {
		private Drawable mdrawable = null;

		@Override
		public Drawable getDrawable(String source) {
			if (mdrawable != null)
				return mdrawable;
			int rId = Integer.parseInt(source);
			mdrawable = context.getResources().getDrawable(rId);
			mdrawable.setBounds(0, 0, mdrawable.getIntrinsicWidth(),
					mdrawable.getIntrinsicHeight());

			return mdrawable;
		}
	};

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		if (mlistItemsinfo.get(position).isDisplayMore)
			return TYPE_MOREHOT;
		else
			return TYPE_SIMPLEITEM;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return TYPE_ITEM_COUNT;
	}

}
