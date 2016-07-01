package com.biu.biu.netoperate;

import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * 顶贴/踩贴基类
 * @author grf
 *
 */
public class TipLikeTreadThread implements Runnable{
	private Handler mhandler = null;
	private String thread_id = null;	// 帖子ID
	private String url = null;			// url地址
	private int MSG_OK = 0;			
	private int MSG_ERROR = -1;
	private boolean isSendMessage = false;
	public TipLikeTreadThread(Handler targethandler, String threadid, int position, String puturl) {
		// TODO Auto-generated constructor stub
		this.mhandler = targethandler;
		this.thread_id = threadid;
		this.url = puturl;
	}
	
	
	public Handler getHandler() {
		return mhandler;
	}
	public void setHandler(Handler mhandler) {
		this.mhandler = mhandler;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
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
        	Message msg = Message.obtain();
        	msg.what = MSG_ERROR;
        	this.mhandler.sendMessage(msg);
        	e.printStackTrace();
        }
        //得到httpResponse的状态响应码
        int statusCode=httpResponse.getStatusLine().getStatusCode();
        if (statusCode==HttpStatus.SC_OK) {
        	if(isSendMessage){
        		// 刷新首页
        		Message msg = Message.obtain();
				msg.what = MSG_OK;
				// 通过Handler发布传送消息，handler
				this.mhandler.sendMessage(msg);
        	}
        }else {
        	if(isSendMessage){
	        	Message msg = Message.obtain();
	        	msg.what = MSG_ERROR;
	        	this.mhandler.sendMessage(msg);
        	}
        }
	}

	
	public int getMSG_OK() {
		return MSG_OK;
	}


	public void setMSG_OK(int mSG_OK) {
		MSG_OK = mSG_OK;
	}


	public int getMSG_ERROR() {
		return MSG_ERROR;
	}


	public void setMSG_ERROR(int mSG_ERROR) {
		MSG_ERROR = mSG_ERROR;
	}


	public boolean isSendMessage() {
		return isSendMessage;
	}


	public void setSendMessage(boolean isSendMessage) {
		this.isSendMessage = isSendMessage;
	}

}
