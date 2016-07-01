package com.biu.biu.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.biu.biu.userconfig.UserConfigParams;
import com.umeng.analytics.MobclickAgent;

import grf.biu.R;

public class MoonDetailActivity extends Activity {
	

	private TextView topicContent = null;
	private TextView created_attv = null;
	private TextView reply_numtv = null;
	private TipItemInfo mtipInfo = new TipItemInfo();
	private EditText mreplyContent = null;
	private ImageButton msendreplybtn = null;	
	private ListView mreplylistview = null;
	private ImageButton mtabBackbt = null;
	private Handler mMoonHandler = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_moon_detail);
		findId();
		initParam();
		initView();
		mMoonHandler = new MoonHandler();
	}

	private void initView() {
		// TODO Auto-generated method stub
		hintkbTwo();		// 关闭软键盘
	}

	// 关闭软键盘
	private void hintkbTwo() {
		// TODO Auto-generated method stub
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive() && getCurrentFocus() != null){
			if(getCurrentFocus().getWindowToken() != null){
				imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	private void initParam() {
		// TODO Auto-generated method stub
		Intent intent = this.getIntent();
		mtipInfo.id = intent.getStringExtra("thread_id");		// 帖子ID
	}

	private void findId() {
		// TODO Auto-generated method stub
		topicContent = (TextView)findViewById(R.id.tipcontent);	// 帖子内容
		created_attv = (TextView)findViewById(R.id.pub_timetxv);
		reply_numtv = (TextView)findViewById(R.id.replynumtv);
		mreplylistview = (ListView)findViewById(R.id.replylistview);
		mreplyContent = (EditText)findViewById(R.id.myreplyedit);
		msendreplybtn = (ImageButton)findViewById(R.id.sendreplybtn);	// 发送按钮
		mtabBackbt = (ImageButton)findViewById(R.id.publish_back);
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		SharedPreferences preferences = getSharedPreferences("user_Params", MODE_PRIVATE);
		UserConfigParams.device_id = preferences.getString("device_ID", "");
		MobclickAgent.onResume(this);
		GetTipInfo();		// 获取目标帖子的信息
	}
	
	private void GetTipInfo() {
		// TODO Auto-generated method stub
		
//		Thread thread = new Thread(new GetDetailThread(detailHandler));
//		thread.start();
	}

	@Override
	protected void onPause(){
		super.onPause();
		MobclickAgent.onPause(this);
	}
	
	
	
	private class MoonHandler extends Handler {
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}
	}
}
