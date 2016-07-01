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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PostRegIDThread implements Runnable {
	private static final String TAG = "BiuPush";
	private Handler regIdHandler;
	private String regIdUrl;
	private String device_id;
	private String registration_id;

	public final static int RIGID_OK = 0;
	public final static int RIGID_ERR = -1;

	public PostRegIDThread(Handler regIdHandler, String regIdUrl,
			String device_id, String registration_id) {
		super();
		this.regIdHandler = regIdHandler;
		this.regIdUrl = regIdUrl;
		this.device_id = device_id;
		this.registration_id = registration_id;
	}

	@Override
	public void run() {
		Log.d(TAG, "[PostRegIDThread] PostRegIDThread -running... ");
		// TODO Auto-generated method stub
		HttpClient rigIdClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(regIdUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("device_id", device_id));
		params.add(new BasicNameValuePair("registration_id", registration_id));
		UrlEncodedFormEntity entity = null;
		HttpResponse httpResponse = null;
		Message msg = Message.obtain();
		try {
			Log.d(TAG, "post regId");
			entity = new UrlEncodedFormEntity(params, "utf-8");
			httpPost.setEntity(entity);
			httpResponse = rigIdClient.execute(httpPost);
			int status = httpResponse.getStatusLine().getStatusCode();
			msg.what = status == HttpStatus.SC_OK ? RIGID_OK : RIGID_ERR;
			regIdHandler.sendMessage(msg);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			this.sendErrMessage(msg);
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			this.sendErrMessage(msg);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			this.sendErrMessage(msg);
			e.printStackTrace();
		}

	}

	public void sendErrMessage(Message msg) {
		msg.what = RIGID_ERR;
		regIdHandler.sendMessage(msg);
	}
}
