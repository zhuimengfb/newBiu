package com.biu.biu.thread;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.biu.biu.userconfig.UserConfigParams;

import android.os.Handler;
import android.os.Message;

public class ClearPushNumThread implements Runnable {
	private String thread_id;
	private Handler handler;
	public final static int MSG_PUT_OK = 3;
	public final static int MSG_PUT_ERROR = 4;

	public ClearPushNumThread(String thread_id, Handler handler) {
		super();
		this.thread_id = thread_id;
		this.handler = handler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		String device_id = UserConfigParams.device_id;
		String url = "http://api.bbbiu.com:1234/message/" + device_id
				+ "/clear";
		HttpClient client = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(url);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("thread_id", thread_id));
		UrlEncodedFormEntity entity = null;
		HttpResponse response = null;
		try {
			entity = new UrlEncodedFormEntity(params, "utf-8");
			httpPut.setEntity(entity);
			response = client.execute(httpPut);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 得到httpResponse的状态响应码
		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK) {
			Message msg = Message.obtain();
			msg.what = MSG_PUT_OK;
			this.handler.sendMessage(msg);
		} else {
			// 获取数据错误
			Message msg = Message.obtain();
			msg.what = MSG_PUT_ERROR;
			this.handler.sendMessage(msg);
		}
	}

}
