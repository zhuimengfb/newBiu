package com.biu.biu.main;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.biu.biu.replysuggestion.ChatAdapter;
import com.biu.biu.replysuggestion.ChatEntity;
import com.biu.biu.views.base.BaseActivity;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.SyncListener;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Reply;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import grf.biu.R;

public class ResponseSuggestActivity extends BaseActivity {
  private EditText contentEditText = null;
  private ListView chatListView = null;
  // 下拉刷新组件
  private SwipeRefreshLayout mSwipeRefreshLayout = null;
  private List<ChatEntity> chatList = null;
  private ChatAdapter chatAdapter = null;

  private Conversation mComversation = null;
  @BindView(R.id.toolbar_suggestion)
  Toolbar toolbar;
  @BindView(R.id.bt_send)
  ImageView sendSimpleDraweeView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_response_suggest);
    ButterKnife.bind(this);
    findViewId();    // 获得控件对象
    initToolbar();
    // 初始化友盟会话对象
    mComversation = new FeedbackAgent(this).getDefaultConversation();
    InitView();
    initEvent();
  }

  private void initEvent() {
    sendSimpleDraweeView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String userreply = contentEditText.getText().toString();
        if (userreply.isEmpty()) {
          Toast.makeText(ResponseSuggestActivity.this, "请输入内容", Toast.LENGTH_SHORT).show();
        } else {
          // 内容不为空，发送消息
          contentEditText.setText("");
          sendUserReply(userreply);
        }
      }
    });
  }

  private void initToolbar() {
    setSupportActionBar(toolbar);
    setBackableToolbar(toolbar);
  }

  /*
   * 获得页面控件对象
   */
  private void findViewId() {
    // 下拉刷新组件，这个以后要改
    mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.fb_reply_refresh);
    // 编辑框
    contentEditText = (EditText) this.findViewById(R.id.replysug_inputcontent);
    chatListView = (ListView) findViewById(R.id.suggest_history_lv);
  }

  private void InitView() {
    // TODO Auto-generated method stub

//		InitSampleData();		// 初始化示例数据
    // 创建Adapter并为ListView设置Adapter
    chatList = new ArrayList<ChatEntity>();
    chatAdapter = new ChatAdapter(this, chatList, mComversation);
    chatListView.setDivider(null);  // 不要分割线
    chatListView.setAdapter(chatAdapter);
    // 下拉刷新
    mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {

      @Override
      public void onRefresh() {
        // TODO Auto-generated method stub
        // sync();
      }
    });


    // 下拉刷新
//		mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
//			
//			@Override
//			public void onRefresh() {
//				// TODO Auto-generated method stub
//				sync();
//			}
//		});
  }

  private void showSubmitSuccess() {
    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
    sweetAlertDialog.setTitleText("反馈已成功");
    sweetAlertDialog.setContentText("谢谢你的建议");
    sweetAlertDialog.setConfirmText(getString(R.string.confirm));
    sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
      @Override
      public void onClick(SweetAlertDialog sweetAlertDialog) {
        sweetAlertDialog.dismiss();
      }
    });
    sweetAlertDialog.show();
  }

  protected void sendUserReply(String userreply) {
    mComversation.addUserReply(userreply);
    // 刷新ListView
//		ChatEntity chatEntity = new ChatEntity();  
//		chatEntity.setChatTime("2012-09-20 15:16:34");  
//		chatEntity.setContent(contentEditText.getText().toString());  
//		chatEntity.setComeMsg(false);  
//		chatList.add(chatEntity); 
    chatAdapter.notifyDataSetChanged();
    // 刷新到底部
    scrollToBottom();
    // 数据同步
    sync();
    showSubmitSuccess();
//		 
//        chatAdapter.notifyDataSetChanged();  
//        chatListView.setSelection(chatList.size() - 1);  
//        contentEditText.setText("");

    // 实现会话：
//        com.umeng.fb.model.Conversation con;
//        con.addUserReply();


    // 自动填入回复信息（最初测试UI效果用）
//        ChatEntity autoResponse = new ChatEntity();
//        SimpleDateFormat dateFormat24 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
//        autoResponse.setChatTime(dateFormat24.format(Calendar.getInstance().getTime()));
//        autoResponse.setContent("Bug已经提交，谢谢您的反馈");
//        autoResponse.setComeMsg(true);
//        try{
//        	Thread.currentThread().sleep(500);		// 等待一段时间
//        }
//        catch(InterruptedException e){
//        	e.printStackTrace();
//        }

//        chatList.add(autoResponse);

  }

  /**
   * 利用Conversation.sync方法来进行APP端的数据与服务器端同步。
   * onReceiveDevReply方法获得replyList为开发者在友盟的Web端的回复。
   * onSendUserReply方法获得的replyList则为本次同步数据中发送到服务器的用户反馈数据列表
   */
  private void sync() {
    // TODO Auto-generated method stub
    mComversation.sync(new SyncListener() {

      @Override
      public void onReceiveDevReply(List<Reply> arg0) {
        // SwipeRefreshlayout停止刷新

        // 刷新ListView

        scrollToBottom();
      }

      @Override
      public void onSendUserReply(List<Reply> arg0) {
        // TODO Auto-generated method stub
        scrollToBottom();
      }

    });

    // 同步完成之后，停止刷新，并更新ListView
    mSwipeRefreshLayout.setRefreshing(false);
    chatAdapter.notifyDataSetChanged();
  }

  /*
   * 令ListView滑动到底部
   */
  protected void scrollToBottom() {
    // TODO Auto-generated method stub
    // 选中listview的指定列
    chatListView.setSelection(chatAdapter.getCount() - 1);
  }

  /*
   * 初始化示例数据
   */
  private void InitSampleData() {
    // TODO Auto-generated method stub

    ChatEntity chatEntity = null;
    for (int i = 0; i < 2; i++) {
      chatEntity = new ChatEntity();
      if (i % 2 == 0) {
        chatEntity.setComeMsg(false);
        chatEntity.setContent("点击发布文字闪退");
        chatEntity.setChatTime("2014-11-12 10:12:32");
      } else {
        chatEntity.setComeMsg(true);
        chatEntity.setContent("Bug已经提交，谢谢您的反馈");
        chatEntity.setChatTime("2014-11-20 15:13:32");
      }
      chatList.add(chatEntity);
    }
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    MobclickAgent.onResume(this);
    //sync();        // 每次加载页面时进行一次同步操作
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    MobclickAgent.onPause(this);
  }
}
