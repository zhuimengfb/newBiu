package com.biu.biu.thread;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;
import android.os.Message;

/**
 * 发送评论的线程
 * @author grf
 *
 */
public class PostTopicReplyThread extends Thread {
	private Handler mHandler;		// 执行完毕后发送消息
	private String mUrl;				// 执行的目标url
	private HttpEntity mHttpEntity;	// 用于存储参数和数据的http实体

	private int POST_ERROR = -1;
	private int POST_SUCCESS = 0;
	
	public PostTopicReplyThread(Handler handler, String url, HttpEntity httpentity) {
		// TODO Auto-generated constructor stub
		this.mHandler = handler;
		this.mUrl = url;
		this.mHttpEntity = httpentity;
	}

	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		HttpPost httppost = new HttpPost(mUrl);
		httppost.setEntity(mHttpEntity);
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse = null;
		Message msg = Message.obtain();
		try {
			httpResponse = httpclient.execute(httppost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			msg.what = statusCode == HttpStatus.SC_OK ? POST_SUCCESS : POST_ERROR;
			//通过Handler发布传送消息，handler
			this.mHandler.sendMessage(msg);
		} catch (Exception e) {
			msg.what = POST_ERROR;
//			msg.obj = "网络故障，发表回复失败！";
			this.mHandler.sendMessage(msg);
		}
	}

	public void setReturnMsgCode(int nSuccessCode, int nErrorCode) {
		// TODO Auto-generated method stub
		this.POST_SUCCESS = nSuccessCode; 
		this.POST_ERROR = nErrorCode;
	}
}
