package com.biu.biu.replysuggestion;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import grf.biu.R;

/*-
 * 反馈建议页面中的聊天历史列表框适配器
 */
public class ChatAdapter extends BaseAdapter {
	private Context context = null;
	private List<ChatEntity> chatList = null;
	private LayoutInflater inflater = null;
	private final int VIEW_TYPE_DEV = 0;
	private final int VIEW_TYPE_USER = 1;
	private final int VIEW_TYPE_COUNT = 2;
	private Conversation mconversation = null;

	public ChatAdapter(Context context, List<ChatEntity> chatList,
			Conversation umengconversation) {
		this.context = context;
		this.chatList = chatList;
		inflater = LayoutInflater.from(this.context);
		this.mconversation = umengconversation;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		// 获取单条回复
		Reply reply = mconversation.getReplyList().get(position);
		if (Reply.TYPE_DEV_REPLY.equals(reply.type)) {
			// 开发者回复
			return VIEW_TYPE_DEV;
		} else {
			// 用户反馈，回复布局
			return VIEW_TYPE_USER;
		}
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		// 两种不同的Item布局
		return VIEW_TYPE_COUNT;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mconversation.getReplyList().size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		// return chatList.get(position);
		return mconversation.getReplyList().get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		// 获取单条回复
		Reply reply = mconversation.getReplyList().get(position);
		if (convertView == null) {
			// 根据Type的类型来加载不同的Item布局
			if (Reply.TYPE_DEV_REPLY.equals(reply.type)) {
				// 开发者的回复
				convertView = inflater.inflate(R.layout.chat_from_item, null);
			} else {
				convertView = inflater.inflate(R.layout.chat_to_item, null);
			}

			// 创建ViewHolder并获取各种View
			holder = new ViewHolder();
			holder.replyData = (TextView) convertView
					.findViewById(R.id.tv_time);
			holder.replyContent = (TextView) convertView
					.findViewById(R.id.tv_content);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 设置holder的内容
		holder.replyContent.setText(reply.content);
		// chatHolder.contentTextView.setText(chatList.get(position).getContent());
		// chatHolder.userImageView.setImageResource(chatList.get(position).getUserImage());
		// 设置回复的时间数据
		Date replyTime = new Date(reply.created_at);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		holder.replyData.setText(sdf.format(replyTime));

		return convertView;
	}

	private class ViewHolder {
		TextView replyContent; // 数据
		ProgressBar replyProgressBar; // 表示过程执行的进度条
		// private ImageView userImageView;
		ImageView replyStateFailed;
		TextView replyData; // 时间
	}

}
