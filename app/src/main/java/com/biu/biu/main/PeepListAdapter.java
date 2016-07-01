package com.biu.biu.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.biu.biu.morehottips.MoreHotPeepTopicActivity;
import com.biu.biu.netimage.ImageDownloader;
import com.biu.biu.netimage.OnImageDownload;
import com.biu.biu.netoperate.TipLikeTreadThread;
import com.biu.biu.tools.AutoListView;
import com.biu.biu.userconfig.UserConfigParams;

import java.util.ArrayList;

import grf.biu.R;

public class PeepListAdapter extends BaseAdapter {
	private class PeepContentClickListener implements OnClickListener {
		private String mTipId = "";

		public PeepContentClickListener(String tipid) {
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
					PeepDetailActivity.TIPDETAIL_FOR_PEEPTOPIC);
			context.startActivity(intent);
		}

	}

	private ArrayList<TipItemInfo> mListItems;
	private Context context;
	private LayoutInflater listContainer;
	private AutoListView mListView = null;
	ImageDownloader mDownloader;
	private final int TYPE_MOREHOT_ITEM = 0;
	private final int TYPE_SIMPLE_ITEM = 1;
	private final int TYPE_ITEM_COUNT = 2;
	public Handler myhandler;

	private class MoreHotTopicClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.putExtra("topic_id",
					((PeepHomeActivity) context).getTopicId());
			intent.setClass(context, MoreHotPeepTopicActivity.class);
			context.startActivity(intent);
		}

	}

	public PeepListAdapter(Context context, ArrayList<TipItemInfo> peeptopic) {
		this.context = context;
		this.mListItems = peeptopic;
		listContainer = LayoutInflater.from(context);

	}

	public void setListView(AutoListView listview) {
		this.mListView = listview;
	}

	private final class ListItemView {
		public TextView Contenttv; // 文本内容
		public ImageView img; // 图片控件
		public TextView pubtimetv; // 发表时间
		public TextView replytv; // 回复数文本
		public ImageButton Topbtn; // 顶按钮
		public ImageButton Treadbtn; // 踩按钮
		public TextView likeNumtv; // 被顶的总数
		public TextView morehottips; // 更多热帖

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
		// TODO Auto-generated method stub
		boolean isShowMore = false;
		ListItemView listItemView = null; // 与ViewHolder类似
		isShowMore = mListItems.get(position).isDisplayMore;
		if (convertView == null) {
			listItemView = new ListItemView();
			// 获取list_item布局文件的视图(消除热帖的标记)
			if (isShowMore)
				convertView = listContainer.inflate(
						R.layout.peeptopicitemlayout, null);
			else
				convertView = listContainer.inflate(
						R.layout.peeptopicitemlayout, null);

			// 获取控件对象
			listItemView.Contenttv = (TextView) convertView
					.findViewById(R.id.topiccontent);
			listItemView.img = (ImageView) convertView
					.findViewById(R.id.topicimg);
			listItemView.pubtimetv = (TextView) convertView
					.findViewById(R.id.create_at_tv);
			listItemView.replytv = (TextView) (convertView
					.findViewById(R.id.reply_num_tv));
			listItemView.Topbtn = (ImageButton) convertView
					.findViewById(R.id.likebtn);
			listItemView.Treadbtn = (ImageButton) (convertView
					.findViewById(R.id.treadbtn));
			listItemView.likeNumtv = (TextView) convertView
					.findViewById(R.id.likecounttv);

			// 设置控件集到convertView
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		if (mListItems.get(position).isEmpty) {
			listItemView.Contenttv.setText("该话题附近没有人发帖，快来发帖吧！");
			listItemView.img.setVisibility(View.GONE);
			listItemView.pubtimetv.setVisibility(View.GONE);
			listItemView.Topbtn.setVisibility(View.GONE);
			listItemView.Treadbtn.setVisibility(View.GONE);
			listItemView.likeNumtv.setVisibility(View.GONE);
			listItemView.replytv.setVisibility(View.GONE);

		} else {
			// 设置动态改变资源
			listItemView.img.setVisibility(View.VISIBLE);
			listItemView.pubtimetv.setVisibility(View.VISIBLE);
			listItemView.Topbtn.setVisibility(View.VISIBLE);
			listItemView.Treadbtn.setVisibility(View.VISIBLE);
			listItemView.likeNumtv.setVisibility(View.VISIBLE);
			listItemView.replytv.setVisibility(View.VISIBLE);
			if (isShowMore) {
				listItemView.morehottips = (TextView) convertView
						.findViewById(R.id.morehottv);
				listItemView.morehottips
						.setOnClickListener(new MoreHotTopicClickListener());
			}
			boolean blikestate = mListItems.get(position).hasliked;
			boolean btreadstate = mListItems.get(position).hastreaded;
			if (blikestate) {
				listItemView.Topbtn.setImageResource(R.drawable.arrow1click);
			} else {
				listItemView.Topbtn.setImageResource(R.drawable.arrow1);
			}
			// 设置踩的状态
			if (btreadstate) {
				listItemView.Treadbtn.setImageResource(R.drawable.arrow2click);
			} else {
				listItemView.Treadbtn.setImageResource(R.drawable.arrow2);
			}
			// 设置Topic内容
			// if (position < 3) {
			// String contenttemp = mListItems.get(position).content;
			// contenttemp = "<img src=\"" + R.drawable.hot_icon + "\" />"
			// + contenttemp;
			// listItemView.Contenttv.setText(Html.fromHtml(contenttemp,
			// imageGetter, null));
			// } else {
			listItemView.Contenttv.setText(mListItems.get(position).content);
			// }
			// // 设置发表时间、回复数、顶、踩数量
			listItemView.pubtimetv.setText(mListItems.get(position).created_at);
			listItemView.replytv.setText(mListItems.get(position).reply_num
					+ "条回复");
			listItemView.likeNumtv.setText(mListItems.get(position).like_num);
			// 给顶、踩添加事件监听器
			String tipid = mListItems.get(position).id;
			int nlikestate = 0;
			if (blikestate) {
				nlikestate = 1;
			} else {
				nlikestate = btreadstate ? -1 : 0;
			}
			listItemView.Topbtn.setOnClickListener(new TipLikeClickListener(
					position, tipid, nlikestate));
			listItemView.Treadbtn.setOnClickListener(new TipTreadClickListener(
					position, tipid, nlikestate));
			// listItemView.Contenttv.setOnClickListener(new
			// PeepContentClickListener(tipid));
			// warning
			convertView.setOnClickListener(new PeepContentClickListener(tipid));
			listItemView.img.setOnClickListener(new PeepContentClickListener(
					tipid));
			// 处理图片的操作
			String urltemp = mListItems.get(position).imgurl;
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

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		if (mListItems.get(position).isDisplayMore)
			return TYPE_MOREHOT_ITEM;
		else
			return TYPE_SIMPLE_ITEM;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return TYPE_ITEM_COUNT;
	}

	// 顶贴按钮
	private class TipLikeClickListener implements OnClickListener {
		private TipLikeTreadThread mthread = null;
		private int mposition = 0;
		private int mlikeState = 0; // 0：没踩没顶 1：顶了 -1：踩了
		private String mtipid;
		private String murl = "";

		public TipLikeClickListener(int position, String tipid, int nlikestate) {
			// TODO Auto-generated constructor stub
			this.mposition = position;
			this.mlikeState = nlikestate;
			this.mtipid = tipid;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			murl = "http://api.bbbiu.com:1234/topic/" + mtipid;
			Integer nlikenum = Integer
					.parseInt(mListItems.get(mposition).like_num);
			switch (mlikeState) {
			case -1:
				// 当前为踩，点击顶，取消踩。
				murl += "/action:tread" + "/?device_id="
						+ UserConfigParams.device_id + "&is_repeal=1";
				++nlikenum;
				mListItems.get(mposition).hastreaded = false;
				break;
			case 0:
				// 当前无，点击顶，设为顶
				murl += "/action:like" + "/?device_id="
						+ UserConfigParams.device_id;
				++nlikenum;
				mListItems.get(mposition).hasliked = true;
				break;
			case 1:
				// 当前顶，点击顶，取消顶
				murl += "/action:like" + "/?device_id="
						+ UserConfigParams.device_id + "&is_repeal=1";
				--nlikenum;
				mListItems.get(mposition).hasliked = false;
				break;
			}
			mListItems.get(mposition).like_num = nlikenum.toString();
			// 更新显示ListView的状态
			PeepListAdapter.this.notifyDataSetChanged();
			mthread = new TipLikeTreadThread(myhandler, mtipid, mposition, murl);
			// mthread.setSendMessage(false); // 只执行操作，不发送消息
			Thread thread = new Thread(mthread);
			thread.start();

		}

	}

	// 踩贴点击
	private class TipTreadClickListener implements OnClickListener {
		private TipLikeTreadThread mthread = null;
		private int mposition = 0;
		private int mlikeState = 0; // 0：没踩没顶 1：顶了 -1：踩了
		private String mtipid;
		private String murl = "";

		public TipTreadClickListener(int position, String tipid, int nlikestate) {
			this.mposition = position;
			this.mtipid = tipid;
			this.mlikeState = nlikestate;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			murl = "http://api.bbbiu.com:1234/topic/" + mtipid;
			Integer nlikenum = Integer
					.parseInt(mListItems.get(mposition).like_num);
			// 当前处于顶的状态
			switch (mlikeState) {
			case -1:
				// 当前为踩，点击踩，取消踩。
				murl += "/action:tread" + "/?device_id="
						+ UserConfigParams.device_id + "&is_repeal=1";
				++nlikenum;
				mListItems.get(mposition).hastreaded = false;
				break;
			case 0:
				// 当前无，点击踩，设为踩
				murl += "/action:tread" + "/?device_id="
						+ UserConfigParams.device_id;
				--nlikenum;
				mListItems.get(mposition).hastreaded = true;
				break;
			case 1:
				// 当前顶，点击踩，取消顶
				murl += "/action:like" + "/?device_id="
						+ UserConfigParams.device_id + "&is_repeal=1";
				--nlikenum;
				mListItems.get(mposition).hasliked = false;
				break;
			}
			mListItems.get(mposition).like_num = nlikenum.toString();
			// 更新显示ListView的状态
			PeepListAdapter.this.notifyDataSetChanged();
			mthread = new TipLikeTreadThread(myhandler, mtipid, mposition, murl);
			// mthread.setSendMessage(false);
			Thread thread = new Thread(mthread);
			thread.start();
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

}
