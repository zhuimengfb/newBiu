package com.biu.biu.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.biu.biu.userconfig.UserConfigParams;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
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
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import grf.biu.R;

public class TipDetailActivity extends Activity {
	
//	private String mthread_id = null;		// 帖子的uuid
//	private TextView topicContent = null;
//	private TextView created_attv = null;
//	private TextView reply_numtv = null;
//	private TextView like_numtv = null;
//	private TextView tread_numtv = null;
//	private ImageButton tiptopbtn = null;
//	private ImageButton tipdownbtn = null;
	
//	private TextView pub_placetv = null;	// 发表位置
	private ListView mreplylistview = null;
	private EditText mreplyContent = null;
	private ImageButton msendreplybtn = null;
	private ImageButton mtabBackbt = null;
//	private ImageButton msubnewibtn = null;
	private ThreadDetailHandler detailHandler;
	private HttpPost httpPost = null;
	private DetailListAdapter msimpleAdapter = null;	// 回复的列表适配器
	private ArrayList<TipItemInfo> simpledatalist = new ArrayList<TipItemInfo>();
	private TipItemInfo tipInfo = new TipItemInfo();	// 存储帖子的所有信息
	private JSONArray jsonArray = null;
	private JSONObject jsonObject = null;
	private String url = "http://api.bbbiu.com:1234/threads/";
	private String hosturl = "http://api.bbbiu.com:1234";
	private boolean misscrolltoend = false;
//	private String device_uuid = null;
//	private String tip_uuid = null;
//	private String mmyPlace = "";
	private final int GET_DETAILMSG_OK = 1;
	private final int GET_DETAILMSG_ERROR = 2;
	private final int POST_NEWREPLY_OK = 3;
	private final int POST_NEWREPLY_ERROR = 4;
	private String mlng = null;
	private String mlat = null;
//	private boolean topichasliked = false;		// 是否顶过该贴
//	private boolean topichastreaded = false;	// 是否踩过该贴
	private boolean mIsDisplayMoon = false;		// true：显示月光宝盒详情页
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SharedPreferences preferences = getSharedPreferences("user_Params", MODE_PRIVATE);
		UserConfigParams.device_id = preferences.getString("device_ID", "");
		MobclickAgent.onResume(this);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tip_detail);
		findId();
		initParam();
		initView();			// 初始化显示
		// 配置全部回复的列表项的适配器
//		int[] tipdetailto = new int[]{R.id.tipcontent, R.id.pub_timetxv,
//				R.id.topcounttv, R.id.downcounttv};
//		String[] tipdetailfrom = new String[]{"content", "created_at", "like_num", "tread_num"};
//		msimpleAdapter = new SimpleAdapter(this, simpledatalist, 
//				R.layout.tipdetail, tipdetailfrom, tipdetailto );
		msimpleAdapter = new DetailListAdapter(this, simpledatalist);
		msimpleAdapter.setmDisplayForMoon(mIsDisplayMoon);
		mreplylistview.setAdapter(msimpleAdapter);
		// 从服务器中得到指定帖子ID的详细信息
		detailHandler = new ThreadDetailHandler();
		Thread thread = new Thread(new GetDetailThread(detailHandler));
		thread.start();
		
	}
	
	/**
	 * 初始化详情页的各个按钮信息
	 */
	private void initView() {
		// TODO Auto-generated method stub
		// 发表新回复
		msendreplybtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String replycontent = mreplyContent.getText().toString();
				if(replycontent.isEmpty()){
					Toast.makeText(TipDetailActivity.this, "请输入评论内容！", Toast.LENGTH_SHORT).show();
					return;
				}
				misscrolltoend = true;
				mreplyContent.setText("");
//				mreplyContent.clearFocus();
				hintkbTwo();		// 关闭软键盘
				postNewReply(replycontent);
			}

			private void hintkbTwo() {
				// TODO Auto-generated method stub
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				if(imm.isActive() && getCurrentFocus() != null){
					if(getCurrentFocus().getWindowToken() != null){
						imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					}
				}
			}
			
		});
		// 回退按钮
		mtabBackbt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TipDetailActivity.this.finish();
			}
			
		});
		// 顶贴
//		tiptopbtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				String tiptopurl = hosturl +"/threads/" +  tipInfo.id + "/action:like" + "/?device_id=" + UserConfigParams.device_id;
//				Integer mtopcount = Integer.parseInt(like_numtv.getText().toString());
//				if(tipInfo.hasliked){
//					mtopcount--;
//					tiptopurl = tiptopurl + "&is_repeal=1";
//				}else{
//					mtopcount++;
//				}
//				// 修改顶的状态和获顶的数量
//				like_numtv.setText(mtopcount.toString());
//				tipInfo.hasliked = !tipInfo.hasliked;
//				if(tipInfo.hasliked){
//					tiptopbtn.setImageResource(R.drawable.home_icon3_click);
//					like_numtv.setTextColor(Color.rgb(0x25, 0xd4, 0xb3));
//				}else{
//					tiptopbtn.setImageResource(R.drawable.home_icon3);
//					like_numtv.setTextColor(Color.GRAY);
//				}
//				
//				// 启动顶贴的线程，向服务器发送顶帖请求
//				TipPutLikeThread putlikethread = new TipPutLikeThread(tipInfo.id, tiptopurl);
//				Thread thread = new Thread(putlikethread);
//				thread.start();
//			}
//		});
		// 踩贴
//		tipdownbtn.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				String tiptopurl = hosturl +  "/threads/" + tipInfo.id + "/action:tread" + "/?device_id=" + UserConfigParams.device_id;
//				Integer mtreadcount = Integer.parseInt(tread_numtv.getText().toString());
//				if(tipInfo.hastreaded){
//					mtreadcount--;
//					tiptopurl = tiptopurl + "&is_repeal=1";
//				}else{
//					mtreadcount++;
//				}
//				tread_numtv.setText(mtreadcount.toString());
//				// 修改顶的状态和获顶的数量
//				tipInfo.hastreaded = !tipInfo.hastreaded;
//				if(tipInfo.hastreaded){
//					tipdownbtn.setImageResource(R.drawable.home_icon4_click_stamp);
//					tread_numtv.setTextColor(Color.rgb(0xFF, 0xd3, 0x25));
//				}else{
//					tipdownbtn.setImageResource(R.drawable.home_icon4);
//					tread_numtv.setTextColor(Color.GRAY);
//				}
//				
//				// 启动顶贴的线程，向服务器发送顶帖请求
//				TipPutLikeThread putlikethread = new TipPutLikeThread(tipInfo.id, tiptopurl);
//				Thread thread = new Thread(putlikethread);
//				thread.start();
//			}
//		});
		// 发表新帖按钮
//		msubnewibtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent();
//				intent.setClass(TipDetailActivity.this, PublishActivity.class);
//				TipDetailActivity.this.startActivity(intent);
//				TipDetailActivity.this.finish();
//			}
//		});
				
	}

	/**
	 * 向服务器发送新的评论
	 */
	protected void postNewReply(String replycontent) {
		// TODO Auto-generated method stub
		String content = replycontent;
		NameValuePair nameValuePair2 = new BasicNameValuePair("title", "content");
		NameValuePair nameValuePair3 = new BasicNameValuePair("content", content);
		NameValuePair nameValuePair4 = new BasicNameValuePair("lng", mlng);
		NameValuePair nameValuePair5 = new BasicNameValuePair("lat", mlat);
		NameValuePair nameValuePair6 = new BasicNameValuePair("reply_to", tipInfo.id);
		NameValuePair nameValuePair7 = new BasicNameValuePair("type", mIsDisplayMoon ? String.valueOf(1) : String.valueOf(0));
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(nameValuePair2);
		nameValuePairs.add(nameValuePair3);
		nameValuePairs.add(nameValuePair4);
		nameValuePairs.add(nameValuePair5); 
		nameValuePairs.add(nameValuePair6);
		nameValuePairs.add(nameValuePair7);
		try{
			HttpEntity requestHttpEntity = new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8);
			String url = "http://api.bbbiu.com:1234/devices/" + UserConfigParams.device_id + "/threads";
			httpPost = new HttpPost(url);
			httpPost.setEntity(requestHttpEntity);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		// 向服务器发送发表帖子的post信息
		Thread thread = new Thread(new PostNewReplyThread(detailHandler));
		thread.start();
	}

	/**
	 * 初始化应用程序的配置参数
	 */
	private void initParam() {
		// TODO Auto-generated method stub
		Intent intent = this.getIntent();
		tipInfo.id = intent.getStringExtra("thread_id");		// 帖子ID
		mIsDisplayMoon = intent.getBooleanExtra("displayformoon", false);
		url = url + tipInfo.id + "?device_id=" + UserConfigParams.device_id;
		mlat = UserConfigParams.latitude;
		mlng = UserConfigParams.longitude;
//		SharedPreferences preferences = getSharedPreferences("user_Params", MODE_PRIVATE);
//		device_uuid = preferences.getString("device_ID", "");
		
	}
	// 找到要用到的控件ID
	private void findId() {
		// TODO Auto-generated method stub
//		topicContent = (TextView)findViewById(R.id.tipcontent);	// 帖子内容
//		created_attv = (TextView)findViewById(R.id.pub_timetxv);
//		reply_numtv = (TextView)findViewById(R.id.replynumtv);
//		like_numtv = (TextView)findViewById(R.id.topcounttv);
//		tread_numtv = (TextView)findViewById(R.id.downcounttv);
//		pub_placetv = (TextView)findViewById(R.id.publishplace);
//		tiptopbtn = (ImageButton)findViewById(R.id.topbtn);
//		tipdownbtn = (ImageButton)findViewById(R.id.downbtn);
		mreplylistview = (ListView)findViewById(R.id.replylistview);
		mreplyContent = (EditText)findViewById(R.id.myreplyedit);
		msendreplybtn = (ImageButton)findViewById(R.id.sendreplybtn);	// 发送按钮
		mtabBackbt = (ImageButton)findViewById(R.id.publish_back);		// 回退按钮
//		msubnewibtn = (ImageButton)findViewById(R.id.submit_new);
	}
	
	/**
	 * 详情页的自定义Handler
	 * @author grf
	 *
	 */
	class ThreadDetailHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			int nMsgno = msg.what;
			switch(nMsgno){
			case GET_DETAILMSG_OK:
				// 得到了帖子的详情页
				MyDateTimeDeal dealtime = new MyDateTimeDeal();
				try{
					ArrayList<Integer> topDownFlags = new ArrayList<Integer>();
					// 获得帖子的内容信息
					tipInfo.content = jsonObject.getString("content");
					tipInfo.created_at = dealtime.getTimeGapDesc(jsonObject.getString("created_at"));
					tipInfo.reply_num = jsonObject.getString("reply_num") + "条回复";
					Integer likeresult = Integer.parseInt(jsonObject.getString("like_num")) - Integer.parseInt(jsonObject.getString("tread_num"));
					tipInfo.like_num = likeresult.toString();
					tipInfo.tread_num = jsonObject.getString("tread_num");
					tipInfo.pubplace= jsonObject.getString("address"); 
					tipInfo.id = jsonObject.getString("id");
					tipInfo.hasliked = jsonObject.getBoolean("has_liked");
					tipInfo.hastreaded = jsonObject.getBoolean("has_treaded");
					tipInfo.isTitle = true;
					simpledatalist.clear();		// 每次获得帖子评论都重新输入评论内容
					simpledatalist.add(tipInfo);
//					fillTipinfo();
					// 该帖子的评论
					jsonArray = jsonObject.getJSONArray("replies");
					MyDateTimeDeal timedeal = new MyDateTimeDeal();
		            for (int i = 0; i < jsonArray.length(); i++) {
		            	TipItemInfo replyIteminfo = new TipItemInfo();
		                JSONObject everyJsonObject=jsonArray.getJSONObject(i);
		                replyIteminfo.content = everyJsonObject.getString("content");
		                replyIteminfo.created_at = timedeal.getTimeGapDesc(everyJsonObject.getString("created_at"));
		                replyIteminfo.like_num = everyJsonObject.getString("like_num");
		                replyIteminfo.tread_num = everyJsonObject.getString("tread_num");
		                replyIteminfo.hasliked = everyJsonObject.getBoolean("has_liked");
		                replyIteminfo.hastreaded = everyJsonObject.getBoolean("has_treaded");
		                simpledatalist.add(replyIteminfo);
		            }
//		                					
					msimpleAdapter.notifyDataSetChanged();
					if(misscrolltoend){
						mreplylistview.setSelection(msimpleAdapter.getCount() - 1);
						misscrolltoend = false;
					}
					
				}catch(Exception e){
					e.printStackTrace();
				}
				break;
			case GET_DETAILMSG_ERROR:
				Toast.makeText(TipDetailActivity.this, "获取帖子详情失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
				break;
			case POST_NEWREPLY_OK:
				// 回复成功,关闭这个页，然后重新启动一个页
//				Intent intent = new Intent();
//				intent.setClass(TipDetailActivity.this, TipDetailActivity.class);
//				intent.putExtra("thread_id", mthread_id);
//				startActivity(intent);
//				TipDetailActivity.this.finish();
				// 发表评论成功，设置主页需要刷新
				UserConfigParams.isHomeRefresh = true;
//				重新获取一次详情页
				Thread thread = new Thread(new GetDetailThread(detailHandler));
				thread.start();
				break;
			case POST_NEWREPLY_ERROR:
				Toast.makeText(TipDetailActivity.this, "发表新评论失败，请检查网络连接！", Toast.LENGTH_SHORT).show();
				break;
			}
			
			super.handleMessage(msg);
		}

		/**
		 * 将获得的帖子内容填充到详情页各个控件
		 */
//		private void fillTipinfo() {
//			// TODO Auto-generated method stub
//			topicContent.setText(tipInfo.content);		// 原帖内容
//			created_attv.setText(tipInfo.created_at);	// 原帖创建时间
//			reply_numtv.setText(tipInfo.reply_num);		// 回复数
//			like_numtv.setText(tipInfo.like_num);		// 获赞数
//			// 赞的表现
//			if(tipInfo.hasliked){
//				tiptopbtn.setImageResource(R.drawable.home_icon3_click);
//				like_numtv.setTextColor(Color.rgb(0x25, 0xd4, 0xb3));
//			}else{
//				tiptopbtn.setImageResource(R.drawable.home_icon3);
//				like_numtv.setTextColor(Color.GRAY);
//			}
//			
//			tread_numtv.setText(tipInfo.tread_num);		// 获踩数
//			// 踩的表现
//			if(tipInfo.hastreaded){
//				tipdownbtn.setImageResource(R.drawable.home_icon4_click_stamp);
//				tread_numtv.setTextColor(Color.rgb(0xFF, 0xd3, 0x25));
//			}else{
//				tipdownbtn.setImageResource(R.drawable.home_icon4);
//				tread_numtv.setTextColor(Color.GRAY);
//			}
//			if(tipInfo.pubplace.isEmpty() || tipInfo.pubplace.equals("null")){
//				pub_placetv.setVisibility(TextView.GONE);
//			}
//			pub_placetv.setText(tipInfo.pubplace);		// 发表位置
//			
//		}
		
	}
	
	
	
	/*
	 * 发送新的回复线程
	 */
	class PostNewReplyThread implements Runnable{
		private Handler mhandler = null;
		
		public PostNewReplyThread(Handler arg0){
			this.mhandler = arg0;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			JSONObject resultJsonObject=null;
			
	        HttpClient httpClient= new DefaultHttpClient();
	        StringBuilder entityStringBuilder=new StringBuilder();
	        BufferedReader bufferedReader=null;
	        HttpResponse httpResponse=null;
			
			try {
	            //HttpClient发出一个HttpGet请求
	            httpResponse=httpClient.execute(httpPost);      
	        } catch (Exception e) {
	        	Message msg = Message.obtain();
				msg.what = POST_NEWREPLY_ERROR;
				// 通过Handler发布传送消息，handler
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
	                    msg.what = POST_NEWREPLY_OK;
	        			//通过Handler发布传送消息，handler
	        			this.mhandler.sendMessage(msg);
	                } catch (Exception e) {
	                    e.printStackTrace(); }
	            }
	        }else
	        {
	        	Message msg =Message.obtain();
	        	msg.what = POST_NEWREPLY_ERROR;
	        	this.mhandler.sendMessage(msg);
	        }
		}
		
	}
	/**
	 * 线程，从服务器获得指定帖子的详情
	 * @author grf
	 *
	 */
	class GetDetailThread implements Runnable{
		private Handler mhandler = null;				// 上下文信息，用于获得activity
		public GetDetailThread(Handler arg0){
			this.mhandler = arg0;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			JSONObject resultJsonObject=null;
			// 检查url的有效性
	        if ("".equals(url)||url==null) {
	        	return;
	        }
	        
	        HttpClient httpClient = new DefaultHttpClient();
	        
	        StringBuilder urlStringBuilder=new StringBuilder(url);
	        StringBuilder entityStringBuilder=new StringBuilder();
	        //利用URL生成一个HttpGet请求
	        HttpGet httpGet=new HttpGet(urlStringBuilder.toString());
	        BufferedReader bufferedReader=null;
	        HttpResponse httpResponse=null;
			
			try {
	            //HttpClient发出一个HttpGet请求
	            httpResponse = httpClient.execute(httpGet);      
	        } catch (Exception e) {
	        	Message msg = Message.obtain();
				msg.what = GET_DETAILMSG_ERROR;
				// 通过Handler发布传送消息，handler
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
	                    jsonObject=new JSONObject(entityStringBuilder.toString());
	                    // 得到了首页数据，传递消息，进行解析并显示
	                    Message msg = Message.obtain();
	                    msg.what = GET_DETAILMSG_OK;
	        			//通过Handler发布传送消息，handler
	        			this.mhandler.sendMessage(msg);
	                } catch (Exception e) {
	                    e.printStackTrace(); }
	            }
	        }
		}
		
	}
	
	/**
	 * 顶贴的线程
	 * @author grf
	 *
	 */
	class TipPutLikeThread implements Runnable{
		private String thread_id = null;		// 帖子ID
		private String url = null;
		
		public TipPutLikeThread(String id, String puturl){
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
			//得到httpResponse的状态响应码
	        int statusCode=httpResponse.getStatusLine().getStatusCode();
	        if (statusCode==HttpStatus.SC_OK) {
	        	UserConfigParams.isHomeRefresh = true;	// 主页需要刷新
	        }
		}
		
	}
	
	/**
	 * 踩贴的线程
	 * @author grf
	 *
	 */
	class TipPutTreadThread implements Runnable{
		private String thread_id = null;		// 帖子ID
		private String url = null;
		
		public TipPutTreadThread(String id, String puturl) {
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
			//得到httpResponse的状态响应码
	        int statusCode=httpResponse.getStatusLine().getStatusCode();
	        if (statusCode==HttpStatus.SC_OK) {
	        	UserConfigParams.isHomeRefresh = true;	// 主页需要刷新
	        }
		}
		
	}
}
