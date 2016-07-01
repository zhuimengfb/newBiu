package com.biu.biu.main;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.biu.biu.netimage.ImageDownloader;
import com.biu.biu.netimage.OnImageDownload;
import com.biu.biu.netoperate.TipLikeTreadThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.userconfig.UserConfigParams;

import java.util.ArrayList;

import grf.biu.R;

public class MoonBoxListViewAdapter extends BaseAdapter{
	private LayoutInflater listContainer;
	private ArrayList<TipItemInfo> mListItems;		// 帖子
	private AutoListView mListView = null;			// 持有一个自己所适配的listview，用来按照Tag搜索ImageView控件
	ImageDownloader mDownloader;
	private Context context;						// 运行上下文
	private Handler myhandler = null;
	private boolean mIsNotips = false;
	View converView = null;
	public MoonBoxListViewAdapter(Context context,
			ArrayList<TipItemInfo> listItems) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.mListItems = listItems;
		listContainer = LayoutInflater.from(context);
		converView = listContainer.inflate(R.layout.homelistitemlayout, null);
	}

	public void setListView(AutoListView listview) {
		this.mListView = listview;
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

	public void setHandler(Handler handler){
		this.myhandler = handler;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ListViewItemView listItemView = null;
		if(convertView == null){
			listItemView = new ListViewItemView();
			// 获取list_item布局文件的视图
			convertView = listContainer.inflate(R.layout.moonboxlistitemlayout, null);
			// 获取控件对象
//			listItemView.hometopbtn = (ImageButton)convertView.findViewById(R.id.topbtn);
//			listItemView.homedownbtn = (ImageButton)convertView.findViewById(R.id.downbtn);
			listItemView.homeContenttv = (TextView)convertView.findViewById(R.id.tipcontent);
			listItemView.moonImg = (ImageView)convertView.findViewById(R.id.moonboximg);
//			listItemView.pubtimeImg = (ImageView)(convertView.findViewById(R.id.pub_timeimg));
			listItemView.publishTimetv = (TextView)convertView.findViewById(R.id.pub_timetxv);
//			listItemView.replyImg = (ImageView)(convertView.findViewById(R.id.replyimage));
			listItemView.replayCounttv = (TextView)convertView.findViewById(R.id.replynumtv);
//			listItemView.TopCounttv = (TextView)convertView.findViewById(R.id.topcounttv);
//			listItemView.DownCounttv = (TextView)convertView.findViewById(R.id.downcounttv);
//			listItemView.publishPlacetv = (TextView)convertView.findViewById(R.id.publishplace);
//			listItemView.morehottips = (TextView)convertView.findViewById(R.id.morehottv);
//			listItemView.fourcontrollayout = (LinearLayout)convertView.findViewById(R.id.fourbtnlinearlayout);
			// 设置控件集到convertView
			convertView.setTag(listItemView);
		}else{
			listItemView = (ListViewItemView)convertView.getTag();
		}
		
		if(mIsNotips){
			listItemView.homeContenttv.setText(mListItems.get(position).content);
//			listItemView.pubtimeImg.setVisibility(TextView.INVISIBLE);
//			listItemView.homedownbtn.setVisibility(TextView.INVISIBLE);
//			listItemView.hometopbtn.setVisibility(TextView.INVISIBLE);
//			listItemView.morehottips.setVisibility(TextView.GONE);
//			listItemView.fourcontrollayout.setVisibility(View.GONE);
			return convertView;
		}
		
		// 显示所有控件
//		listItemView.fourcontrollayout.setVisibility(View.VISIBLE);
//		listItemView.morehottips.setVisibility(TextView.GONE);			// 所有帖子就不出现更多热帖的触发接口了
//		listItemView.publishPlacetv.setVisibility(TextView.VISIBLE);	// 位置信息
//		listItemView.replayCounttv.setVisibility(TextView.INVISIBLE);	// 回复数
//		listItemView.DownCounttv.setVisibility(TextView.VISIBLE);		// 踩数
//		listItemView.TopCounttv.setVisibility(TextView.VISIBLE);		// 顶数
//		listItemView.homedownbtn.setVisibility(ImageButton.VISIBLE);
//		listItemView.hometopbtn.setVisibility(ImageButton.VISIBLE);
		// 发表时间图标
//		listItemView.pubtimeImg.setVisibility(ImageView.VISIBLE);
		// 回复数图标
//		listItemView.replyImg.setVisibility(ImageView.VISIBLE);
		
		
//		设置动态改变资源
//		boolean blikestate = mListItems.get(position).hasliked;
//		boolean btreadstate = mListItems.get(position).hastreaded;
//		if(blikestate){
//			listItemView.hometopbtn.setImageResource(R.drawable.home_icon3_click);
//			listItemView.TopCounttv.setTextColor(Color.rgb(0x25, 0xd4, 0xb3));
//		}else{
//			listItemView.hometopbtn.setImageResource(R.drawable.home_icon3);
//			listItemView.TopCounttv.setTextColor(Color.GRAY);
//		}
//		// 设置踩的状态
//		if(btreadstate){
//			listItemView.homedownbtn.setImageResource(R.drawable.home_icon4_click_stamp);
//			listItemView.DownCounttv.setTextColor(Color.rgb(0xFF, 0xd3, 0x25));
//		}else{
//			listItemView.homedownbtn.setImageResource(R.drawable.home_icon4);
//			listItemView.DownCounttv.setTextColor(Color.GRAY);
//		}
		
		listItemView.homeContenttv.setText(mListItems.get(position).content);
//				}
		
		// 设置发表时间、回复数、顶、踩数量
//		Integer topcount = Integer.parseInt(mListItems.get(position).like_num);
//		Integer downcount = Integer.parseInt(mListItems.get(position).tread_num);
		listItemView.publishTimetv.setText(mListItems.get(position).created_at);
		listItemView.replayCounttv.setText(mListItems.get(position).reply_num + "条回复");
//		listItemView.TopCounttv.setText(topcount.toString());
//		listItemView.DownCounttv.setText(downcount.toString());
//		String itemPlace = mListItems.get(position).pubplace;
//		if(null == itemPlace || itemPlace.isEmpty() || itemPlace.equals("null")){
//			listItemView.publishPlacetv.setVisibility(TextView.GONE);
//		}else{
//			listItemView.publishPlacetv.setVisibility(TextView.VISIBLE);
//			listItemView.publishPlacetv.setText(itemPlace);
//		}
		
		String tipid = mListItems.get(position).id;
//		listItemView.hometopbtn.setOnClickListener(new TipLikeClickListener(position, tipid, blikestate));
//		listItemView.homedownbtn.setOnClickListener(new TipTreadClickListener(position, tipid, btreadstate));
		
		// 给Button添加单击事件，添加Button之后ListView将失去焦点，需要将Button的焦点去掉
//			String tipid = mlistItemsinfo.get(position).get("id").toString();
//			listItemView.hometopbtn.setOnClickListener(new HomeTopbtnListener(position, topcount, tipid, blikestate));
//			listItemView.homedownbtn.setOnClickListener(new HomeDownbtnListener(position, downcount, mlistItemsinfo.get(position).get("id").toString(), listItemView.DownCounttv, btreadstate));

		// 去掉更多热帖
//		listItemView.morehottips.setVisibility(TextView.GONE);
		// 处理图片的操作
		String urltemp = mListItems.get(position).imgurl;
		if (urltemp != null && !urltemp.equals("null")) {
			final String url = "http://api.bbbiu.com:1234/" + urltemp;
			listItemView.moonImg.setTag(String.valueOf(position));		// 用索引作为标记
//			listItemView.img.setImageResource(android.R.drawable.stat_sys_download_done);
			if (mDownloader == null) {
				mDownloader = new ImageDownloader();
			}
			//异步下载图片  
            mDownloader.imageDownload(String.valueOf(position), url, listItemView.moonImg, context.getExternalCacheDir().getPath(), (Activity)this.context, new OnImageDownload() {  
                @Override  
                public void onDownloadSucc(String tag, Bitmap bitmap,  
                        String c_url,ImageView mimageView) {  
                    ImageView imageView = (ImageView) mListView.findViewWithTag(tag);
                    if (imageView != null) {
                    	imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(bitmap);  
                        imageView.setTag("");  
                    }   
                }  
            });
		} else {
			// 没有图片则不显示该控件
			listItemView.moonImg.setVisibility(View.GONE);
		}
		return convertView;
	}

	
	private final class ListViewItemView{
		public TextView  homeContenttv;			// 内容控件
		public ImageView moonImg;				// 图片控件
		public TextView  publishTimetv;			// 发表时间
		public TextView  replayCounttv;			// 回复时间
	}
	
	// 月光宝盒取消顶踩功能
//	private class TipTreadClickListener implements OnClickListener {
//		private TipLikeTreadThread mthread = null;
//		private int mposition = 0;
//		private boolean mhastreaded = false;
//		private String mtipid;
//		private String murl = "";
//		public TipTreadClickListener(int position, String tipid,
//				boolean bhastreaded) {
//			this.mposition = position;
//			this.mtipid = tipid;
//			this.mhastreaded = bhastreaded;
//			
//			
//			// TODO Auto-generated constructor stub
//		}
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			murl = "http://api.bbbiu.com:1234/threads/" + mtipid +
//					"/action:tread" + "/?device_id=" + UserConfigParams.device_id;
//			if(mhastreaded){
//				murl += "&is_repeal=1";
//				// 撤销踩
//				mListItems.get(mposition).hastreaded = false;
//				Integer ncount = Integer.parseInt(mListItems.get(mposition).tread_num) - 1;
//				mListItems.get(mposition).tread_num = ncount.toString(); 
//			}else{
//				mListItems.get(mposition).hastreaded = true;
//				Integer ncount = Integer.parseInt(mListItems.get(mposition).tread_num) + 1;
//				mListItems.get(mposition).tread_num = ncount.toString();
//			}
//			// 更新显示ListView的状态
//			MoonBoxListViewAdapter.this.notifyDataSetChanged();
////			this.notify();
//			mthread = new TipLikeTreadThread(myhandler, mtipid, mposition, murl);
////			mthread.setSendMessage(false);
//			Thread thread = new Thread(mthread);
//			thread.start();
//		}
//
//	}


	private class TipLikeClickListener implements OnClickListener {
		private TipLikeTreadThread mthread = null;
		private int mposition = 0;
		private boolean mhasliked = false;
		private String mtipid;
		private String murl = "";
		
		public TipLikeClickListener(int position, String tipid,
				boolean bhasliked) {
			// TODO Auto-generated constructor stub
			this.mposition = position;
			this.mhasliked = bhasliked;
			this.mtipid = tipid;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			murl = "http://api.bbbiu.com:1234/threads/" + mtipid +
					"/action:like" + "/?device_id=" + UserConfigParams.device_id;
			if(mhasliked){
				murl += "&is_repeal=1";
				mListItems.get(mposition).hasliked = false;
				Integer ncount = Integer.parseInt(mListItems.get(mposition).like_num) - 1;
				mListItems.get(mposition).like_num = ncount.toString(); 
			}else{
				mListItems.get(mposition).hasliked = true;
				Integer ncount = Integer.parseInt(mListItems.get(mposition).like_num) + 1;
				mListItems.get(mposition).like_num = ncount.toString(); 
			}
			// 更新显示ListView的状态
			MoonBoxListViewAdapter.this.notifyDataSetChanged();
			mthread = new TipLikeTreadThread(myhandler, mtipid, mposition, murl);
//			mthread.setSendMessage(false);		// 只执行操作，不发送消息
			Thread thread = new Thread(mthread);
			thread.start();
			
		}

	}


	public void setNoTips(boolean notipsflag) {
		// TODO Auto-generated method stub
		this.mIsNotips = notipsflag;
	}
	
}
