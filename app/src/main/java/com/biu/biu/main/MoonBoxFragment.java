package com.biu.biu.main;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.biu.biu.tools.AutoListView;
import com.biu.biu.tools.AutoListView.OnLoadListener;
import com.biu.biu.tools.AutoListView.OnRefreshListener;
import com.biu.biu.userconfig.UserConfigParams;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.UnknownHostException;
import java.util.ArrayList;

import grf.biu.R;

// 月光宝盒分页

public class MoonBoxFragment extends Fragment implements OnRefreshListener, OnLoadListener {
	private AutoListView mMoonBoxListView;
//	private TextView mNoItemInfo = null;
	private MoonBoxListViewAdapter mMoonAdapter;
	private ArrayList<TipItemInfo> mTipItems;
	private boolean mthreadisrunning = false;
	private MoonHandler mMoonHandler;
	private int mMoonGettedCount = 0;			// 已经得到的月光宝盒帖子数，用于下拉加载更多。
	private JSONArray mJsonArray = null; 		// 存储从网络中获得的数据
	private boolean mIsEmpty = true;
	private final static int MSG_GET_OK_0 = 0;		// 清空现有数据并获得新数据
	private final static int MSG_GET_OK_1 = 1;		// 将新数据添加到现有数据
	private final static int MSG_GET_ERROR = -1;
//	private boolean isLoad = false;
	
	// 配置是否
//	public void setLoadFlag(boolean isload){
//		this.isLoad = isload;
//	}
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreateView(inflater, container, savedInstanceState);
		View moonBoxView = inflater.inflate(R.layout.fragment_moonbox, container, false);
		return moonBoxView;
//		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	// 视图完成创建后触发
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mMoonBoxListView = (AutoListView)getActivity().findViewById(R.id.frgmoonbox_lv);
//		mNoItemInfo = (TextView)getActivity().findViewById(R.id.noinfotv);
		mMoonHandler = new MoonHandler(this);
		mTipItems = new ArrayList<TipItemInfo>();
		mMoonAdapter = new MoonBoxListViewAdapter(this.getActivity(), mTipItems);
		mMoonAdapter.setHandler(mMoonHandler);
		mMoonBoxListView.setAdapter(mMoonAdapter);
		mMoonBoxListView.setOnRefreshListener(this);
		mMoonBoxListView.setOnLoadListener(this);
		mMoonBoxListView.setPageSize(5);			// 每次获得5条帖子信息
		mMoonGettedCount = 0;
		// 添加点击进入详情页的功能
		mMoonBoxListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(mIsEmpty)
					return;
				if(position > mTipItems.size())
					return;
				int nPosition = position - 1;		// 减去header
				Intent intent = new Intent();
				// 详情页另开一个activity，暂时复用详情页。
				intent.setClass(getActivity(), TipDetailActivity.class);
				intent.putExtra("thread_id", mTipItems.get(nPosition).id);
				intent.putExtra("displayformoon", true);		// 设置详情页格式为月光宝盒详情页
				getActivity().startActivity(intent);
			}
			
		});
		super.onViewCreated(view, savedInstanceState);
		
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		getMoonBoxFirst(mMoonGettedCount);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
//		Toast.makeText(getActivity(), "Refresh", Toast.LENGTH_SHORT).show();
		mMoonGettedCount = 0;
		getMoonBoxFirst(0);
	}
	
	
	private void getMoonBoxFirst(int i) {
		// TODO Auto-generated method stub
		if(mthreadisrunning)
			return;
		
		GetMoonThread  getthread = new GetMoonThread(mMoonHandler, mMoonGettedCount);
		Thread thread= new Thread(getthread);
		thread.start();
		
	}

	private class GetMoonThread implements Runnable{
		private Handler mhandler = null;
		private int mGettedCount = 0;
		private int ncount = 1;
		
		public GetMoonThread(Handler handler, int nGetted){
			this.mhandler = handler;
			this.mGettedCount = nGetted;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			mthreadisrunning = true;
			ncount = 1;
			while(!UserConfigParams.hasGettedLocation()){
				try{
					Thread.sleep(200);
					ncount++;
					if(ncount > 50){
						Message msg = Message.obtain();
						msg.what = MSG_GET_ERROR;
						this.mhandler.sendMessage(msg);
						return;
					}
				}catch(InterruptedException e){
					e.printStackTrace();
					return;
				}finally{
					mthreadisrunning = false;
				}
				
			}
			String nextpageurl = "http://api.bbbiu.com:1234/threads";
			nextpageurl = nextpageurl + "?lat=" + UserConfigParams.latitude + "&lng="
					+ UserConfigParams.longitude +"&offset=" + (Integer.valueOf(mGettedCount).toString()) +
					"&limit=5" + "&device_id=" + UserConfigParams.device_id + "&type=1";

			HttpClient httpClient = new DefaultHttpClient();

			StringBuilder urlStringBuilder = new StringBuilder(nextpageurl);
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
			}catch(UnknownHostException e){
				// 无法连接到主机
				Message msg = Message.obtain();
//				msg.what = NEXT_PAGE_GET_ERROR;
				// 通过Handler发布传送消息，handler
				this.mhandler.sendMessage(msg);
				return;
			}
			catch (Exception e) {
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
						if(0 == this.mGettedCount)
							msg.what = MSG_GET_OK_0;
						else
							msg.what = MSG_GET_OK_1;
						
						// 通过Handler发布传送消息，handler
						this.mhandler.sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else{
				// 获取数据错误
				Message msg = Message.obtain();
				msg.what = MSG_GET_ERROR;
				this.mhandler.sendMessage(msg);
			}
			mthreadisrunning = false;
		}
	}
		

	static class MoonHandler extends Handler{
		WeakReference<MoonBoxFragment> mMoonwekfre;
		
		public MoonHandler(MoonBoxFragment frg){
			mMoonwekfre = new WeakReference<MoonBoxFragment>(frg);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int nMsg = msg.what;
			MoonBoxFragment monfrg = mMoonwekfre.get();
			switch(nMsg){
			case MSG_GET_OK_0:		// 清除现有数据并添加
				monfrg.addNewData(true);	// 参数表示是否清除当前已有数据
				break;
			case MSG_GET_OK_1:		// 直接添加
				monfrg.addNewData(false);
				break;
//			case MSG_NOTIFY_LIST:
//				monfr
			case MSG_GET_ERROR:
				break;
			}
			
			super.handleMessage(msg);
		}
		
	}


	public void addNewData(boolean isclear) {
		// TODO Auto-generated method stub
		if(isclear){
			mTipItems.clear();
			mMoonGettedCount = 0;
			mIsEmpty = true;
//			mMoonBoxListView.onRefreshComplete();
		}else
			mMoonBoxListView.onLoadComplete();
		
		try {
			MyDateTimeDeal timedeal = new MyDateTimeDeal();
			for (int i = 0; i < mJsonArray.length(); i++) {
				JSONObject everyJsonObject = mJsonArray.getJSONObject(i);
				TipItemInfo item = new TipItemInfo();
				item.content = everyJsonObject.getString("content");
				item.created_at = timedeal.getTimeGapDesc(everyJsonObject.getString("created_at"));
				item.device_id = everyJsonObject.getString("device_id");
				item.id = everyJsonObject.getString("id");
				item.lat = everyJsonObject.getString("lat");
				Integer likeresult = Integer.parseInt(everyJsonObject.getString("like_num")) - Integer.parseInt(everyJsonObject.getString("tread_num"));
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
			if(mTipItems.isEmpty()){
				mIsEmpty = true;
				TipItemInfo temp = new TipItemInfo();
				temp.content = (String)getResources().getText(R.string.nomoonbox);
				mMoonAdapter.setNoTips(true);	// 设置为无帖子类型，用于getView响应
				mTipItems.add(temp);
//				mNoItemInfo.setVisibility(View.VISIBLE);
//				mMoonBoxListView.setVisibility(View.INVISIBLE);
				
			}else{
				mIsEmpty = false;
				mMoonAdapter.setNoTips(false);	// 设置适配器中的存在帖子标记
				mMoonGettedCount = mTipItems.size();
//				mNoItemInfo.setVisibility(View.GONE);
//				mMoonBoxListView.setVisibility(View.VISIBLE);
			}
			
			mMoonAdapter.notifyDataSetChanged();
//			.notify();
			// 在具体的fill操作中进行
//			if(!usefirstBuffer)
//				mtipsGettedCount += tempBuffer.size();	// 加载更多
//			else
//				mtipsGettedCount = 0;					// 下拉刷新则归零
//			listViewAdapter.setTopOrDownStateArray(topDownFlags);
//			listViewAdapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		mMoonBoxListView.setSelection(0);
		onRefresh();
		super.onResume();
	}
}
