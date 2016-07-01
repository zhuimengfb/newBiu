package com.biu.biu.main;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.biu.biu.biumap.BiumapActivity;
import com.biu.biu.biumap.PoiActivity;
import com.biu.biu.biumap.PoiDatabaseHelper;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.tools.AutoListView.OnRefreshListener;
import com.biu.biu.userconfig.UserConfigParams;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bingoogolapple.badgeview.BGABadgeRelativeLayout;
import grf.biu.R;

public class PeepFragment extends Fragment implements OnRefreshListener {

	public static final String PEEP_FRAGMENT_MSG_UPDATE_ACTION = "peep_fragment_msg_update_action";
	private AutoListView mPeepListView;
	private PeepListViewAdapter mPeepListViewAdapter;
	private ArrayList<PeepTopicInfo> mListItems = new ArrayList<PeepTopicInfo>(); // 存储各个帖子的文本信息
	private PeepHandler mPeepHandler;
	private JSONArray mJsonArray;
	private final int MSG_GET_OK = 0;
	private final int MSG_GET_ERROR = -1;
	private final int MSG_PUT_OK = 1;
	private final int MSG_PUT_ERROR = 2;
	// 临时用
	private String mTopicId;

	private ImageView peepStatusView;
	@BindView(R.id.fab_search)
	FloatingActionButton fabSearch;

	public static PeepFragment getInstance(ImageView peepStatusView){
		PeepFragment peepFragment = new PeepFragment();
		peepFragment.peepStatusView = peepStatusView;
		return peepFragment;
	}

	// 定义一个构造函数
	/*public PeepFragment(ImageView peepStatusView) {
		this.peepStatusView = peepStatusView;
	}*/

	class PeepHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int nMsgNo = msg.what;
			mPeepListView.onRefreshComplete();
			mPeepListView.onLoadComplete();
			switch (nMsgNo) {
			case MSG_GET_OK:
				DealPeekTopicInfo();
				if (UserConfigParams.peepStatus == true) {
					//peepStatusView.setVisibility(View.VISIBLE);
					sendBroadCast(PEEP_FRAGMENT_MSG_UPDATE_ACTION);
					UserConfigParams.peepStatus = false;
				} else {
					if (peepStatusView!=null) {
						peepStatusView.setVisibility(View.INVISIBLE);
					}
				}
				UserConfigParams.peepStatus = false;
				break;
			case MSG_GET_ERROR:
				break;
			case MSG_PUT_OK:
				break;
			case MSG_PUT_ERROR:
				break;
			}
			super.handleMessage(msg);
		}

	}

	private void sendBroadCast(String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		getActivity().sendBroadcast(intent);
	}
	// 线程类获取活跃中的话题内容
	class PeepTopicThread extends Thread {
		private PeepHandler mHandler;

		public PeepTopicThread(PeepHandler mHandler) {
			super();
			this.mHandler = mHandler;
		}

		@Override
		public void run() {
			// 修改后需要添加自身的设备ID
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			// 获取自身的设备ID
			String device_id = UserConfigParams.device_id;
			String url = "http://api.bbbiu.com:1234/topic/checkTopics"
					+ "?device_id=" + device_id;
			HttpClient httpClient = new DefaultHttpClient();
			StringBuilder urlStringBuilder = new StringBuilder(url);
			StringBuilder entityStringBuilder = new StringBuilder();
			// 利用URL生成一个HttpGet请求
			HttpGet httpGet = new HttpGet(urlStringBuilder.toString());
			httpGet.setHeader("Content-Type",
					"application/x-www-form-urlencoded; charset=utf-8");
			BufferedReader bufferedReader = null;
			HttpResponse httpResponse = null;
			try {
				// HttpClient发出一个HttpGet请求
				httpResponse = httpClient.execute(httpGet);
			} catch (UnknownHostException e) {
				// 无法连接到主机
				Message msg = Message.obtain();
				// msg.what = NEXT_PAGE_GET_ERROR;
				// 通过Handler发布传送消息，handler
				// this.mhandler.sendMessage(msg);
				return;
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			// 得到httpResponse的状态响应码
			int statusCode = httpResponse.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				// 得到httpResponse的实体数据
				HttpEntity httpEntity = httpResponse.getEntity();
				if (httpEntity != null) {
					try {
						bufferedReader = new BufferedReader(
								new InputStreamReader(httpEntity.getContent(),
										"UTF-8"), 8 * 1024);
						String line = null;
						while ((line = bufferedReader.readLine()) != null) {
							entityStringBuilder.append(line + "/n");
						}
						// 利用从HttpEntity中得到的String生成JsonObject
						mJsonArray = new JSONArray(
								entityStringBuilder.toString());
						// 得到了首页数据，传递消息，进行解析并显示
						Message msg = Message.obtain();
						msg.what = MSG_GET_OK;
						// 通过Handler发布传送消息，handler
						this.mHandler.sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				// 获取数据错误
				Message msg = Message.obtain();
				msg.what = MSG_GET_ERROR;
				this.mHandler.sendMessage(msg);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		mPeepHandler = new PeepHandler();
		// 布局含有一个自定义的ListView
		View peepView = inflater.inflate(R.layout.fragment_peep, container,
				false);
		return peepView;
	}

	/**
	 * 处理获得的新鲜事标题信息
	 */
	public void DealPeekTopicInfo() {
		// TODO Auto-generated method stub
		try {
			mListItems.clear();
			PeepTopicInfo itemp = new PeepTopicInfo();
			itemp.isTitle = true;
			itemp.content = "新鲜事";
			mListItems.add(itemp);

			for (int i = 0; i < mJsonArray.length(); i++) {
				JSONObject everyJsonObject = mJsonArray.getJSONObject(i);
				PeepTopicInfo item = new PeepTopicInfo();
				item.id = everyJsonObject.getString("id");

				// 获取当前活跃中的话题ID
				Log.i("当前活跃中的话题ID为————", item.id);
				item.content = everyJsonObject.getString("topic_content");
				item.allowimg = everyJsonObject.getInt("img_or_not");
				item.isDeleted = everyJsonObject.getInt("under_or_not");
				item.creat_at = everyJsonObject.getString("created_at");
				item.update_at = everyJsonObject.getString("updated_at");
				//服务端没有下发status造成异常
				/*
				item.status = everyJsonObject.getInt("status");
				if (item.status == 1) {
					if (UserConfigParams.peepStatus == false) {
						UserConfigParams.peepStatus = true;
					}
				}*/
				mListItems.add(item);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		mPeepListView.setResultSize(mJsonArray.length());
		mPeepListViewAdapter.notifyDataSetChanged();

	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mPeepListView = (AutoListView) getActivity().findViewById(
				R.id.peep_listview);
		ButterKnife.bind(this,view);
		// 显示兴趣点列表控件
		myPoiList = (ListView) getActivity().findViewById(R.id.my_poi_list);
		// 兴趣点提示TextView控件
		poiTitle = (TextView) getActivity().findViewById(R.id.poi_title);
		super.onViewCreated(view, savedInstanceState);
		initView();
		refrestPoiListView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mPeepListViewAdapter = new PeepListViewAdapter(this.getActivity(),
				mListItems);
		mPeepListView.setOnRefreshListener(this);
		// mPeepListView.setOnLoadListener(this);
		mPeepListView.setPageSize(100); // 话题暂时没有加载更多操作，认为100以内个都一次加载完成，以后加的时候再改。
		// mPeepListView.setfootVisibility(false);
		// mPeepListView.setLoadEnable(false);
		mPeepListView.setAdapter(mPeepListViewAdapter);
		// 点击项目进入话题列表
		mPeepListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 判断话题是否有新的消息，如果有则开启新线程，发起请求来重置HTTP状态
				// TODO Auto-generated method stub
				if (position <= 1 || position >= mListItems.size() + 1)
					return;
				int nPosition = position - 1; // 减去header
				if (mListItems.get(nPosition).status != 0) {
					// 发起一个线程修改重置该设备话题状态
					new Thread(new ResetTopicStatus(
							mListItems.get(nPosition).id, mPeepHandler))
							.start();
				}
				Intent intent = new Intent();
				intent.setClass(getActivity(), PeepHomeActivity.class);
				intent.putExtra("TopicTitle", mListItems.get(nPosition).content);
				intent.putExtra("topic_id", mListItems.get(nPosition).id);
				getActivity().startActivity(intent);
			}
		});
		fabSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(getActivity(), BiumapActivity.class);
				startActivity(intent);
			}
		});
	}

	// 管理员
	protected void modifytopic() {
		// TODO Auto-generated method stub
		Toast.makeText(getActivity(), "中了", Toast.LENGTH_SHORT).show();
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				String url = "http://api.bbbiu.com:1234/topic/raiseTopic";
				NameValuePair nameValuePair1 = new BasicNameValuePair(
						"topic_content", "随意吐槽、爆照、交友");
				// 如果自己定位了，就使用发表页定的位，更精确。否则使用全局定位信息
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(nameValuePair1);
				HttpPost httpPost = new HttpPost(url);
				try {
					HttpEntity requestHttpEntity = new UrlEncodedFormEntity(
							nameValuePairs, HTTP.UTF_8);
					httpPost.setEntity(requestHttpEntity);
				} catch (Exception e) {
					e.printStackTrace();
				}
				HttpClient httpClient = new DefaultHttpClient();
				StringBuilder entityStringBuilder = new StringBuilder();
				BufferedReader bufferedReader = null;
				HttpResponse httpResponse = null;
				try {
					// HttpClient发出一个HttpGet请求
					httpResponse = httpClient.execute(httpPost);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				// 得到httpResponse的状态响应码
				int statusCode = httpResponse.getStatusLine().getStatusCode();
			}
		});
		thread.start();
	}

	// 偷看页面的listview适配器
	public class PeepListViewAdapter extends BaseAdapter {
		private LayoutInflater listContainer;
		private ArrayList<PeepTopicInfo> mListItems; // 存储各个帖子的文本信息
		private Context context;

		private final int TYPE_FRAG_TOPIC = 0;
		private final int TYPE_FRAG_TITLE = 1;
		private final int TYPE_ITEM_COUNT = 2;

		public PeepListViewAdapter(Context context,
				ArrayList<PeepTopicInfo> listItems) {
			this.context = context;
			this.mListItems = listItems;
			listContainer = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mListItems.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mListItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d("STATUS", "status ing!!");
			// TODO Auto-generated method stub
			PeepListView listItemView = null;
			boolean isTitle = mListItems.get(position).isTitle;
			if (convertView == null) {
				listItemView = new PeepListView();
				if (isTitle)
					convertView = listContainer.inflate(
							R.layout.peeplisttitleitemlayout, null);
				else
					convertView = listContainer.inflate(
							R.layout.peeplistitemlayout, null);

				// 获取控件ID
				listItemView.peepTopic = (TextView) convertView
						.findViewById(R.id.peeplistitem_topic);
				listItemView.status_topic = (ImageView) convertView
						.findViewById(R.id.status_topic);
				listItemView.bgaBadgeRelativeLayout = (BGABadgeRelativeLayout) convertView
						.findViewById(R.id.topic_status_layout);
				convertView.setTag(listItemView);
			} else {
				listItemView = (PeepListView) convertView.getTag();
			}
			Log.d("STATUS", "status ing!!" + mListItems.get(position).content
					+ "  " + mListItems.get(position).status);
			// 设置item视图信息
			listItemView.peepTopic.setText(mListItems.get(position).content);
			if (mListItems.get(position).status == 0) {
				if (position != 0) {
					//listItemView.status_topic.setVisibility(ImageView.INVISIBLE);
					listItemView.bgaBadgeRelativeLayout.hiddenBadge();
				} else {
					//listItemView.status_topic.setVisibility(ImageView.GONE);
				}
			} else {
				//listItemView.status_topic.setVisibility(ImageView.VISIBLE);
				listItemView.bgaBadgeRelativeLayout.showCirclePointBadge();
			}
			return convertView;
		}

		// item的缓冲
		private final class PeepListView {
			public TextView peepTopic; // 标题内容
			// 添加一个imageview显示话题与用户是否有新消息
			public ImageView status_topic;
			public BGABadgeRelativeLayout bgaBadgeRelativeLayout;
		}

		@Override
		public int getItemViewType(int position) {
			// TODO Auto-generated method stub
			if (mListItems.get(position).isTitle)
				return TYPE_FRAG_TITLE;
			else
				return TYPE_FRAG_TOPIC;
		}

		@Override
		public int getViewTypeCount() {
			// TODO Auto-generated method stub
			return TYPE_ITEM_COUNT;
		}

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		// 最开始将标识是否有新消息状态的红点标记为false
		UserConfigParams.peepStatus = false;

		onRefresh();
		refrestPoiListView();
		super.onResume();
	}

	// 获得当前活跃话题,避免多次启用线程
	private void getTopic() {
		// TODO Auto-generated method stub
		PeepTopicThread thread = new PeepTopicThread(mPeepHandler);
		thread.start();
	}

	// @Override
	// public void onLoad() {
	// // TODO Auto-generated method stub
	// // mPeepListView.setSelection(mPeepListViewAdapter.getCount() - 1);
	// // mPeepListView.onLoadComplete();
	// }

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		getTopic(); // 获得当前活跃话题
	}

	// 重置话题状态的线程
	class ResetTopicStatus implements Runnable {
		private String topic_id;
		private PeepHandler peepHandler;

		public ResetTopicStatus(String topic_id, PeepHandler peepHandler) {
			this.peepHandler = peepHandler;
			this.topic_id = topic_id;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			String device_id = UserConfigParams.device_id;
			String url = "http://api.bbbiu.com:1234/message/" + device_id
					+ "/clear";
			HttpClient client = new DefaultHttpClient();
			HttpPut httpPut = new HttpPut(url);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("topic_id", topic_id));
			UrlEncodedFormEntity entity = null;
			HttpResponse response = null;
			try {
				entity = new UrlEncodedFormEntity(params, "utf-8");
				httpPut.setEntity(entity);
				response = client.execute(httpPut);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// 得到httpResponse的状态响应码
			int statusCode = response.getStatusLine().getStatusCode();

			if (statusCode == HttpStatus.SC_OK) {
				Message msg = Message.obtain();
				msg.what = MSG_PUT_OK;
				this.peepHandler.sendMessage(msg);
			} else {
				// 获取数据错误
				Message msg = Message.obtain();
				msg.what = MSG_PUT_ERROR;
				this.peepHandler.sendMessage(msg);
			}
		}
	}

	// 获取更新兴趣点列表
	private void refrestPoiListView() {
		poiObjs.clear();
		poiDatabaseHelper = new PoiDatabaseHelper(getActivity(), "poi.db",
				null, 1);
		SQLiteDatabase db = poiDatabaseHelper.getWritableDatabase();
		Cursor cursor = db.query("poi", null, null, null, null, null, null);

		if (cursor.moveToFirst()) {
			do {
				Poiobj temp = new Poiobj();
				temp.id = cursor.getInt(cursor.getColumnIndex("id"));
				temp.lat = cursor.getDouble(cursor.getColumnIndex("lat"));
				temp.lng = cursor.getDouble(cursor.getColumnIndex("lng"));
				temp.placename = cursor.getString(cursor
						.getColumnIndex("placename"));
				temp.description = cursor.getString(cursor
						.getColumnIndex("description"));
				poiObjs.add(temp);

			} while (cursor.moveToNext());
		}
		if (poiObjs.size() <= 0) {
			poiTitle.setVisibility(View.GONE);
		} else {
			poiTitle.setVisibility(View.VISIBLE);
		}
		poiAdapter = new PoiAdapter(getActivity(), R.layout.poi_item, poiObjs);
		myPoiList.setAdapter(poiAdapter);
		poiAdapter.notifyDataSetChanged();

	}

	private ListView myPoiList;
	private TextView poiTitle;
	private List<Poiobj> poiObjs = new ArrayList<Poiobj>();
	private PoiAdapter poiAdapter;
	private PoiDatabaseHelper poiDatabaseHelper;

	public class Poiobj {
		public int id;
		public double lat;
		public double lng;
		public String placename;
		public String description;

	}

	// 定义显示兴趣点列表的适配器
	class PoiAdapter extends ArrayAdapter<Poiobj> {
		private int resourceId;

		public PoiAdapter(Context context, int resource, List<Poiobj> objects) {
			super(context, resource, objects);
			// TODO Auto-generated constructor stub
			this.resourceId = resource;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final Poiobj poiObj = getItem(position);
			View view;
			ViewHolder viewHolder;
			if (convertView == null) {
				view = LayoutInflater.from(getContext()).inflate(resourceId,
						null);
				viewHolder = new ViewHolder();
				view.setTag(viewHolder);
			} else {
				view = convertView;
				viewHolder = (ViewHolder) view.getTag();
			}
			viewHolder.eachPoiItem = (TextView) view
					.findViewById(R.id.poi_each_item);
			// 删除按钮
			viewHolder.poiItemCancel = (ImageButton) view
					.findViewById(R.id.poi_item_cancel);
			viewHolder.eachPoiItem.setText(poiObj.description);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// 跳转到兴趣点位置的2500米范围
					Intent intent = new Intent(PeepFragment.this.getActivity(),
							PoiActivity.class);
					intent.putExtra("centerLat", poiObj.lat);
					intent.putExtra("centerLng", poiObj.lng);
					if (poiObj.placename == null) {
						intent.putExtra("placeName", "unknown");
					} else {
						intent.putExtra("placeName", poiObj.placename);
					}

					// 提示给PoiActivity页面我已经保存过了 不需要有 保存栏
					intent.putExtra("savedornot", true);
					startActivity(intent);
				}
			});
			view.findViewById(R.id.poi_item_cancel).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Log.i("dbsql", "delete");
							poiDatabaseHelper = new PoiDatabaseHelper(
									getActivity(), "poi.db", null, 1);
							SQLiteDatabase db = poiDatabaseHelper
									.getWritableDatabase();
							db.delete("poi", "id = ?",
									new String[] { String.valueOf(poiObj.id) });
							refrestPoiListView();
						}
					});
			return view;
		}

		class ViewHolder {
			public TextView eachPoiItem;
			public ImageButton poiItemCancel;
		}

	}
}
