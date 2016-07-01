package com.biu.biu.main;


import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.biu.biu.userconfig.UserConfigParams;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import grf.biu.R;

public class PublishActivity extends Activity implements AMapLocationListener{
	private Button mtabTopSubmit = null;
	private ImageButton mtabBackbt = null;
	private EditText mPublishContent = null;
	private TextView mContentCount = null;
	private static final int MAX_COUNT = 200;
	private String myPlace = null;
	private CheckBox showPlaceCheckbox = null;
	private TextView mshowPlace = null;
	private LocationManagerProxy mLocationManagerProxy;
	private Double lat = 3.0;
	private Double lng = 125.0;
	private boolean mhasgetlatlng = false;
	private boolean misshowmyplace = false;
	private HttpPost httpPost = null;
	private String baseUrl =  "http://api.bbbiu.com:1234"; 
	private PublishTipHandler publishHandler;
	private JSONObject jsonObject = null;
	private final int PUBLISHTIP_OK = 0;
	private final int PUBLISHTIP_ERROR = -1;
	private boolean mIsMoon = false;
	private boolean mIsPublishing = false;
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mhasgetlatlng = false;
		
		InitLocation();		// 初始化定位操作
		SharedPreferences preferences = getSharedPreferences("user_Params", MODE_PRIVATE);
		UserConfigParams.device_id = preferences.getString("device_ID", "");
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (mLocationManagerProxy != null) {
			mLocationManagerProxy.destroy();
		}
		MobclickAgent.onPause(this);
		super.onPause();
		
		
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish);
		mIsPublishing = false;
		findId();		
		InitConfigParam();	// 初始化配置参数，如设备ID等。
		InitView();			// 初始化显示内容
		
	}

	private void InitConfigParam() {
		// TODO Auto-generated method stub
		// 读出设备参数
//		SharedPreferences preferences = getSharedPreferences("user_Params", MODE_PRIVATE);
//		device_ID = preferences.getString("device_ID", "");
		// 初始化是否为月光宝盒
		mIsMoon = getIntent().getBooleanExtra("IsMoon", false);
		baseUrl += ("/devices/" + UserConfigParams.device_id + "/threads");
		
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

	private void InitLocation() {
		// TODO Auto-generated method stub
		mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        //其中如果间隔时间为-1，则定位只定一次
        mLocationManagerProxy.requestLocationData(
        		LocationProviderProxy.AMapNetwork, 30, 500, this);
 
//        mLocationManagerProxy.setGpsEnable(false);
	}

	private void InitView() {
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
					Toast.makeText(PublishActivity.this, "请输入新话题内容！", Toast.LENGTH_SHORT).show();
					return;
				}
				if(!mIsPublishing){
					mIsPublishing = true;
					PostNewTip();
				}
				
			}

			/**
			 * 发表帖子
			 */
			private void PostNewTip() {
				// TODO Auto-generated method stub
				
				NameValuePair nameValuePair1 = new BasicNameValuePair("device_uuid", UserConfigParams.device_id);
				content = mPublishContent.getText().toString();
				title = content;
				NameValuePair nameValuePair2 = new BasicNameValuePair("title", title);
				NameValuePair nameValuePair3 = new BasicNameValuePair("content", content);
				NameValuePair nameValuePair4, nameValuePair5;
				// 如果自己定位了，就使用发表页定的位，更精确。否则使用全局定位信息
				if(mhasgetlatlng){
					nameValuePair4 = new BasicNameValuePair("lng", lng.toString());
					nameValuePair5 = new BasicNameValuePair("lat", lat.toString());
				}else{
					if(!UserConfigParams.hasGettedLocation()){
						Toast.makeText(PublishActivity.this, "无定位信息，无法发表新话题！", Toast.LENGTH_SHORT).show();
						return;
					}
						
					nameValuePair4 = new BasicNameValuePair("lng", UserConfigParams.longitude);
					nameValuePair5 = new BasicNameValuePair("lat", UserConfigParams.latitude);
				}
				NameValuePair nameValuePair6 = new BasicNameValuePair("address", myPlace);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(nameValuePair1);
				nameValuePairs.add(nameValuePair2);
				nameValuePairs.add(nameValuePair3);
				nameValuePairs.add(nameValuePair4);
				nameValuePairs.add(nameValuePair5); 
				if(misshowmyplace)
					nameValuePairs.add(nameValuePair6);
				NameValuePair nameValuePair7 = new BasicNameValuePair("type", "1");
				if(mIsMoon)
					nameValuePairs.add(nameValuePair7);
				try{
					HttpEntity requestHttpEntity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
					httpPost = new HttpPost(baseUrl);
					httpPost.setEntity(requestHttpEntity);
				}catch(Exception e){
					mIsPublishing = false;
					e.printStackTrace();
				}
				// 向服务器发送发表帖子的post信息
				publishHandler = new PublishTipHandler();
				Thread thread = new Thread(new PostNewTipThread(publishHandler));
				thread.start();
			}
			
			
			
		});
		// 左上角的回退ImageButton
		mtabBackbt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				PublishActivity.this.finish();
			}
			
		});
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
	
	/**
	 * 刷新剩余字数，最大值为200字
	 */
	private void setLeftCount() {
		// TODO Auto-generated method stub
		mContentCount.setText(String.valueOf((MAX_COUNT - getInputCount())));
	}
	
	/**
	 * 获取输入框内容字数
	 * @return
	 */
	private long getInputCount() {
		// TODO Auto-generated method stub
		return calculateLength(mPublishContent.getText().toString());
	}

	/**
	 * 计算发表内容的字数；一个汉字=两个英文字母，一个中文标点=两个英文标点
	 * 
	 * @param c
	 * @return
	 */
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
	public void onLocationChanged(AMapLocation amapLocation) {
		// TODO Auto-generated method stub
		if(amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0){
			//获取位置信息
            lat = amapLocation.getLatitude();	// 维度
            lng = amapLocation.getLongitude();	// 经度
            mhasgetlatlng = true;	// 得到经纬度
            String desc = "";
            Bundle locBundle = amapLocation.getExtras();
            if(locBundle != null){
            	desc = locBundle.getString("desc");
            }
			myPlace = amapLocation.getCity() + amapLocation.getDistrict() + amapLocation.getStreet();
        }else
        {
        	Toast.makeText(this, amapLocation.getAMapException().getErrorMessage(), Toast.LENGTH_SHORT).show();
        	mLocationManagerProxy.destroy();
        }
		
	}

	/**
	 * Handler
	 * @author grf
	 *
	 */
	class PublishTipHandler extends Handler{

		@Override
		public void handleMessage(Message msg) {
			int nMsgNo = msg.what;
			if(PUBLISHTIP_OK == nMsgNo){
				// 将得到的帖子内容赋给详情页，并启动详情页
				String tip_id = new String();
				try{
					tip_id = jsonObject.getString("id").toString();
//					Intent intent = new Intent();
//					intent.setClass(PublishActivity.this, TipDetailActivity.class);
//					intent.putExtra("thread_id", tip_id);
					// 发表成功后，关闭发表页面，回到首页，并设置令首页刷新一次
					UserConfigParams.isHomeRefresh = true;
					PublishActivity.this.finish();
//					startActivity(intent);
				}catch(Exception e){
					e.printStackTrace();
				}
				
			}else if(PUBLISHTIP_ERROR == nMsgNo){
				Toast.makeText(PublishActivity.this, "发表新主题失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
			}
			
			super.handleMessage(msg);
		}
		
	}
	
	/**
	 * 线程，发表新的帖子
	 * @author grf
	 *
	 */
	class PostNewTipThread implements Runnable{
		private Handler mhandler = null;				// 上下文信息，用于获得activity
		public PostNewTipThread(Handler arg0){
			this.mhandler = arg0;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			// 检查url的有效性
			if ("".equals(baseUrl)||baseUrl==null) {
	        	return;
	        }
	        
	        HttpClient httpClient = new DefaultHttpClient();
//	        StringBuilder urlStringBuilder=new StringBuilder(baseUrl);
	        StringBuilder entityStringBuilder=new StringBuilder();
	        BufferedReader bufferedReader=null;
	        HttpResponse httpResponse=null;
			
			try {
	            //HttpClient发出一个HttpGet请求
	            httpResponse=httpClient.execute(httpPost);      
	        } catch (Exception e) {
	        	Message msg =Message.obtain();
	        	msg.what = PUBLISHTIP_ERROR;
	        	this.mhandler.sendMessage(msg);
	            e.printStackTrace();
	            return;
	        }
	        //得到httpResponse的状态响应码
	        int statusCode=httpResponse.getStatusLine().getStatusCode();
	        if (statusCode==HttpStatus.SC_OK) {
	            //得到httpResponse的实体数据
	            HttpEntity httpEntity=httpResponse.getEntity();
	            if (httpEntity!=null) {
	            	
	                try {
	                    bufferedReader=new BufferedReader
	                    (new InputStreamReader(httpEntity.getContent(), "UTF-8"), 8*1024);
	                    String line=null;
	                    while ((line=bufferedReader.readLine())!=null) {
	                        entityStringBuilder.append(line+"/n");
	                    }
	                    //利用从HttpEntity中得到的String生成JsonObject
	                    // 这次确实jsonObject
	                    jsonObject = new JSONObject(entityStringBuilder.toString());
	                    // 发表帖子成功后，得到帖子详情信息
	                    Message msg = Message.obtain();
	                    msg.what = PUBLISHTIP_OK;
	        			//通过Handler发布传送消息，handler
	        			this.mhandler.sendMessage(msg);
	                } catch (Exception e) {
	                    e.printStackTrace(); }
	            }
	        }else
	        {
	        	Message msg =Message.obtain();
	        	msg.what = PUBLISHTIP_ERROR;
	        	this.mhandler.sendMessage(msg);
	        }
		}
		
	}
	
}
