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

public class PostTempPosition implements Runnable {
	private double lat;
	private double lng;
	private String postUrl;
	private Handler handler;
	public final static int Post_TEMP_OK = 0;
	public final static int Post_TEMP_ERR = -1;

	public PostTempPosition(Handler handler, double lat, double lng,
			String postUrl) {
		super();
		this.lat = lat;
		this.lng = lng;
		this.postUrl = postUrl;
		this.handler = handler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		HttpClient postTempClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(postUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("lat", String.valueOf(lat)));
		params.add(new BasicNameValuePair("lng", String.valueOf(lng)));
		UrlEncodedFormEntity entity = null;
		HttpResponse httpResponse = null;
		Message msg = Message.obtain();
		try {
			entity = new UrlEncodedFormEntity(params, "utf-8");
			httpPost.setEntity(entity);
			httpResponse = postTempClient.execute(httpPost);
			int status = httpResponse.getStatusLine().getStatusCode();
			msg.what = status == HttpStatus.SC_OK ? Post_TEMP_OK
					: Post_TEMP_ERR;
			handler.sendMessage(msg);
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

	}

}
