package com.biu.biu.main;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;

import grf.biu.R;

public class MoonBoxPublishActivity extends Activity implements AMapLocationListener{
	private Double mlat = 0.0;
	private Double mlng = 0.0;
	
	private Button mtabTopSubmit = null;
	private ImageButton mtabBackbt = null;
	private EditText mPublishContent = null;
	private TextView mContentCount = null;
	private CheckBox showPlaceCheckbox = null;
	private TextView mshowPlace = null;
	
	private String mDev_ID = "";
	private boolean misshowmyplace = false;
	private String myPlace = null;
	private static final int MAX_COUNT = 200;
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLocationChanged(AMapLocation arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish);
		findId();
		initConfigParam();		// 获得并保存设备ID
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mPublishContent.addTextChangedListener(mTextWatcher);
		mPublishContent.setSelection(mPublishContent.length());
		
		setLeftCount();
		mPublishContent.setGravity(Gravity.TOP);
		mPublishContent.setSingleLine(false);
		mPublishContent.setHorizontallyScrolling(false);
		
		// 显示我的地址
		showPlaceCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					misshowmyplace = true;
					mshowPlace.setText(myPlace);
				}else{
					misshowmyplace = false;
					mshowPlace.setText(R.string.showmyplace);
				}
			}
		});
		
		// 右上角的提交内容
		mtabTopSubmit.setOnClickListener(new OnClickListener(){
			private String title = null;	// 标题
			private String content = null;	// 内容
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 发送Post消息给服务器
				String text = mPublishContent.getText().toString();
				if(text.isEmpty()){
					Toast.makeText(MoonBoxPublishActivity.this, "请输入新话题内容！", Toast.LENGTH_SHORT).show();
					return;
				}
				PostNewTip();
			}	
		});
	}

	protected void PostNewTip() {
		// TODO Auto-generated method stub
		
	}

	private long getInputCount() {
		// TODO Auto-generated method stub
		return calculateLength(mPublishContent.getText().toString());
	}

	private long calculateLength(CharSequence c) {
		// TODO Auto-generated method stub
		double len = 0;
		for (int i = 0; i < c.length(); i++) {
			int tmp = (int) c.charAt(i);
			if (tmp > 0 && tmp < 127) {
				len += 0.5;
			} else {
				len++;
			}
		}
		return Math.round(len);
	}

	private void initConfigParam() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = getSharedPreferences("user_Params", MODE_PRIVATE);
		mDev_ID = preferences.getString("device_ID", "");
	}

	private void findId() {
		// TODO Auto-generated method stub
		mPublishContent = (EditText)findViewById(R.id.publishcontent);		// 发表内容编辑框
		showPlaceCheckbox = (CheckBox)findViewById(R.id.showplacecheck);	// 
		mContentCount = (TextView)findViewById(R.id.publishcontcount);		// 字数
		mshowPlace = (TextView)findViewById(R.id.publish_showplace);
		mtabTopSubmit = (Button)findViewById(R.id.submit_new);			// 右上角的提交按钮
		mtabBackbt = (ImageButton)findViewById(R.id.publish_back);
	
	}

	
	private TextWatcher mTextWatcher = new TextWatcher(){
		private int editStart;
		private int editEnd;
		public void afterTextChanged(Editable s){
			editStart = mPublishContent.getSelectionStart();
			editEnd = mPublishContent.getSelectionEnd();

			// 先去掉监听器，否则会出现栈溢出
			mPublishContent.removeTextChangedListener(mTextWatcher);

			// 注意这里只能每次都对整个EditText的内容求长度，不能对删除的单个字符求长度
			// 因为是中英文混合，单个字符而言，calculateLength函数都会返回1
			while (calculateLength(s.toString()) > MAX_COUNT) { // 当输入字符个数超过限制的大小时，进行截断操作
				s.delete(editStart - 1, editEnd);
				editStart--;
				editEnd--;
			}
			mPublishContent.setText(s);
			mPublishContent.setSelection(editStart);

			// 恢复监听器
			mPublishContent.addTextChangedListener(mTextWatcher);

			setLeftCount();
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after){
			
		}
		
		public void onTextChanged(CharSequence s, int start, int before,
				int count){
			
		}
	};
	protected void setLeftCount() {
		// TODO Auto-generated method stub
		mContentCount.setText(String.valueOf((MAX_COUNT - getInputCount())));
	}
}
