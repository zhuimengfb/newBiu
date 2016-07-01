package com.biu.biu.main;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.biu.biu.userconfig.UserConfigParams;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.ArrayList;

import grf.biu.R;

public class DetailListAdapter extends BaseAdapter{
	private String hosturl = "http://api.bbbiu.com:1234";	// 主机域名
	private boolean mDisplayForMoon = false;
	private ArrayList<TipItemInfo> mlistItemsinfo;	// 列表控件数据缓存
	private LayoutInflater listContainer;			// 视图容器工厂
	private final int VIEW_TYPE_TITLE = 0;				// 标题
	private final int VIEW_TYPE_REPLY = 1;				// 评论
	private final int VIEW_TYPE_COUNT = 2;
	View convertView = null;						// item布局视图文件
	/**
	 * 踩贴的线程
	 * @author grf
	 *
	 */
	private class DetailPutTreadThread implements Runnable{
		private String thread_id = null;		// 帖子ID
		private String url = null;
		
		public DetailPutTreadThread(String id, String puturl) {
			// TODO Auto-generated constructor stub
			this.thread_id = id;
			this.url = puturl;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			HttpPut httpput = new HttpPut(url);
	        HttpClient httpClient= new DefaultHttpClient();
	        HttpResponse httpResponse=null;
			try {
	            //HttpClient发出一个HttpGet请求
	            httpResponse=httpClient.execute(httpput);      
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}

	}

	/**
	 * 顶帖的线程
	 * @author grf
	 *
	 */
	private class DetailPutLikeThread implements Runnable {
		private String thread_id = null;		// 帖子ID
		private String url = null;
		
		public DetailPutLikeThread(String id, String puturl){
			this.thread_id = id;
			this.url = puturl;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
	        HttpPut httpput = new HttpPut(url);
	        HttpClient httpClient= new DefaultHttpClient();
	        HttpResponse httpResponse=null;
			try {
	            //HttpClient发出一个HttpGet请求
	            httpResponse=httpClient.execute(httpput);      
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}

	}
	
	/**
	 * 对回复的踩按钮
	 * @author grf
	 *
	 */
	private class DetailTreadButtonListener implements OnClickListener {
		private Integer mtreadcount = 0;		// 当前的踩帖数
		private String mid = null;		// 当前操作帖子的ID
		boolean mhastreaded = false;		// 是否已经踩过
		private int mPosition = 0;		// 当前View的索引
		private boolean flag = true;
		
		// 计时线程，1秒钟只能点一次
    	private class TimeThread extends Thread{
    		public void run(){
    			try{
    				Thread.sleep(1000);
    				flag = true;
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	private synchronized void setFlag(){
    		flag = false;
    	}
    	
		public DetailTreadButtonListener(int nPosition, Integer treadcount,
				String tipid, boolean bhastreaded) {
			// TODO Auto-generated constructor stub
			this.mPosition = nPosition;
			this.mtreadcount = treadcount;
			this.mid = tipid;
			this.mhastreaded = bhastreaded;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!flag){
				return;
			}else{
				setFlag();
				new TimeThread().start();
			}
			String url = null;
			url = hosturl + "/threads/" + mid + "/action:tread" + "/?device_id=" + UserConfigParams.device_id;
			if(mhastreaded){
				mtreadcount--;
				url = url + "&is_repeal=1";
			}else{
				mtreadcount++;
			}
			
			// 修改踩的状态和获顶的数量
			mlistItemsinfo.get(mPosition).tread_num = mtreadcount.toString();
			mlistItemsinfo.get(mPosition).hastreaded = !mhastreaded;
//			map.put("tread_num", mtreadcount.toString());
//			map.put("hastreaded", !mhastreaded);
			// 启动顶贴的线程，向服务器发送顶帖请求,这里暂时先复用顶贴请求，第一个参数是handler，没用到，暂时传空吧。
			DetailPutTreadThread putlikethread = new DetailPutTreadThread(mid, url);
			Thread thread = new Thread(putlikethread);
			thread.start();
			DetailListAdapter.this.notifyDataSetChanged();	// 更新条数
		}

	}

	public class DetailTopButtonListener implements OnClickListener {
		private Integer mlikecount = 0;		// 当前的顶帖数
		private String mid = null;		// 当前操作帖子的ID
		boolean mhasliked = false;		// 是否已经顶过
		private int mPosition = 0;		// 当前View的索引
		private boolean flag = true;
		
		public DetailTopButtonListener(int nPosition, int likecount, String tipid, boolean hasliked){
			this.mPosition = nPosition;
			this.mlikecount = likecount;
			this.mid = tipid;
			this.mhasliked = hasliked;
		}
		
		// 计时线程，1秒钟只能点一次
    	private class TimeThread extends Thread{
    		public void run(){
    			try{
    				Thread.sleep(1000);
    				flag = true;
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    	}
    	
    	private synchronized void setFlag(){
    		flag = false;
    	}
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!flag){
				return;
			}else{
				setFlag();
				new TimeThread().start();
			}
			String url = null;
			url = hosturl + "/threads/" + mid + "/action:like" + "/?device_id=" + UserConfigParams.device_id;
			if(mhasliked){
				mlikecount--;
				url = url + "&is_repeal=1";
			}else{
				mlikecount++;
			}
			
			// 修改顶的状态和获顶的数量
//			HashMap<String, Object> map = mlistItemsinfo.get(mPosition);
			mlistItemsinfo.get(mPosition).like_num = mlikecount.toString();
			mlistItemsinfo.get(mPosition).hasliked = !mhasliked;
			// 启动顶贴的线程，向服务器发送顶帖请求
			DetailPutLikeThread putlikethread = new DetailPutLikeThread(mid, url);
			Thread thread = new Thread(putlikethread);
			thread.start();
			DetailListAdapter.this.notifyDataSetChanged();	// 更新条数
		}

	}

	
	
	public final class DetailListItemView{
		public ImageView pubtimeImg;			// 发表时间图标
		public TextView replyContenttv;			// 回复内容
		public TextView replytimetv;			// 回复时间
		public ImageButton replyimage;			// 回复图标
		public TextView replyNumtv;				// 回复数
		public ImageButton likebtn;				// 顶按钮
		public TextView likecounttv;
		public ImageButton treadbtn;
		public TextView treadcounttv;
	}
	
	/**
	 * 构造函数
	 */
	public DetailListAdapter(Context context, ArrayList<TipItemInfo> listItems){
		// 将list_item.xml布局作为一个视图，添加到listView中。
//		this.context = context;
		this.mlistItemsinfo = listItems;
		listContainer = LayoutInflater.from(context);		// 创建视图容器工厂并设置上下文
//		convertView = listContainer.inflate(R.layout.tipdetail, null);		// 创建list_item.xml布局文件视图
		
	}
	
	/**
	 * 
	 * @param context：上下文环境
	 * @param simpledatalist：用于Biu帖子信息列表
	 */
	public DetailListAdapter(TipDetailActivity context,
			ArrayList<TipItemInfo> simpledatalist) {
		// TODO Auto-generated constructor stub
		this.mlistItemsinfo = simpledatalist;
		listContainer = LayoutInflater.from(context);
//		convertView = listContainer.inflate(R.layout.tipdetail, null);
	
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		boolean isTitle = false;
		DetailListItemView listItemView = null;
		isTitle = mlistItemsinfo.get(position).isTitle;
		if(convertView == null){
			if(isTitle)
				convertView = listContainer.inflate(R.layout.tiptitledetail, null);
			else
				convertView = listContainer.inflate(R.layout.tipdetail, null);
			
			// 获取要用到的控件
			listItemView = new DetailListItemView();
			listItemView.pubtimeImg = (ImageView)convertView.findViewById(R.id.pub_timeimg);
			listItemView.replyContenttv = (TextView)convertView.findViewById(R.id.tipcontent);
			listItemView.replytimetv = (TextView)convertView.findViewById(R.id.pub_timetxv);
			listItemView.replyimage = (ImageButton)convertView.findViewById(R.id.replyimage);
			listItemView.replyNumtv = (TextView)convertView.findViewById(R.id.replynumtv);
			listItemView.likebtn = (ImageButton)convertView.findViewById(R.id.topbtn);
			listItemView.likecounttv = (TextView)convertView.findViewById(R.id.topcounttv);
			listItemView.treadbtn = (ImageButton)convertView.findViewById(R.id.downbtn);
			listItemView.treadcounttv = (TextView)convertView.findViewById(R.id.downcounttv);
			convertView.setTag(listItemView);
		}else{
			listItemView = (DetailListItemView)convertView.getTag();
		}
		
		// 设置发表时间图标
		if(mDisplayForMoon){
			listItemView.pubtimeImg.setVisibility(View.GONE);
			listItemView.replyimage.setVisibility(View.INVISIBLE);
			listItemView.likebtn.setVisibility(View.INVISIBLE);
			listItemView.likecounttv.setVisibility(View.INVISIBLE);
			listItemView.treadbtn.setVisibility(View.INVISIBLE);
			listItemView.treadcounttv.setVisibility(View.INVISIBLE);
		}else{
			
		}
		
		
		// 设置回复数
		if(isTitle){
//			listItemView.replyimage.setVisibility(View.VISIBLE);
//			listItemView.replyNumtv.setVisibility(View.VISIBLE);
			listItemView.replyNumtv.setText(mlistItemsinfo.get(position).reply_num);
		}else{
//			listItemView.replyimage.setVisibility(View.INVISIBLE);
			listItemView.replyNumtv.setVisibility(View.INVISIBLE);
		}
		
		// 设置动态改变顶、赞的情况
		boolean blikestate = mlistItemsinfo.get(position).hasliked;
		boolean btreadstate = mlistItemsinfo.get(position).hastreaded;
		if(blikestate){
			listItemView.likebtn.setImageResource(R.drawable.home_icon3_click);
			listItemView.likecounttv.setTextColor(Color.rgb(0x25, 0xd4, 0xb3));
		}else{
			listItemView.likebtn.setImageResource(R.drawable.home_icon3);
			listItemView.likecounttv.setTextColor(Color.GRAY);
		}
		// 设置踩的状态
		if(btreadstate){
			listItemView.treadbtn.setImageResource(R.drawable.home_icon4_click_stamp);
			listItemView.treadcounttv.setTextColor(Color.rgb(0xFF, 0xd3, 0x25));
		}else{
			listItemView.treadbtn.setImageResource(R.drawable.home_icon4);
			listItemView.treadcounttv.setTextColor(Color.GRAY);
		}
		
		// 设置回复内容、发表时间、顶、踩数量
		Integer likecount = Integer.parseInt(mlistItemsinfo.get(position).like_num);
		Integer treadcount = Integer.parseInt(mlistItemsinfo.get(position).tread_num);
		listItemView.replyContenttv.setText(mlistItemsinfo.get(position).content);
		listItemView.replytimetv.setText(mlistItemsinfo.get(position).created_at);
		listItemView.likecounttv.setText(likecount.toString());
		listItemView.treadcounttv.setText(treadcount.toString());
		
		// 给顶、赞两个按钮添加单击事件
		String tipid = mlistItemsinfo.get(position).id;
		
		listItemView.likebtn.setOnClickListener(new DetailTopButtonListener(position, likecount, tipid, blikestate));
		listItemView.treadbtn.setOnClickListener(new DetailTreadButtonListener(position, treadcount, tipid, btreadstate));
		
//		// 显示为月光宝盒详情页
//		listItemView.pubtimeImg.setVisibility(View.GONE);
//		listItemView.replyimage.setVisibility(View.INVISIBLE);
		
		
		return convertView;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		boolean isTitle = mlistItemsinfo.get(position).isTitle;
		if(isTitle)
			return VIEW_TYPE_TITLE;
		else
			return VIEW_TYPE_REPLY;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return VIEW_TYPE_COUNT;
	}

	public boolean ismDisplayForMoon() {
		return mDisplayForMoon;
	}

	public void setmDisplayForMoon(boolean mDisplayForMoon) {
		this.mDisplayForMoon = mDisplayForMoon;
	}

}
