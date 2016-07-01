package com.biu.biu.thread;

import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Http协议的Get方法
 * 
 * @author grf
 * 
 */
public class GetHttpThread extends Thread {
	private Handler mHandler; // 执行完毕后发送消息
	private String mUrl; // 执行的目标url
	private JSONObject mJsonObject = null; // 存储得到的内容
	private int GET_ERROR = -1;
	private int GET_SUCCESS = 0;

	public GetHttpThread(Handler handler, String url) {
		// TODO Auto-generated constructor stub
		this.mHandler = handler;
		this.mUrl = url;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(mUrl);
		HttpResponse httpResponse = null;
		try {
			// HttpClient发出一个HttpGet请求
			httpResponse = httpClient.execute(httpGet);
		} catch (Exception e) {
			// TODO: handle exception
			Message msg = Message.obtain();
			msg.what = GET_ERROR;
			// 通过Handler发布传送消息，handler
			this.mHandler.sendMessage(msg);
			e.printStackTrace();
			return;
		}
		// 完成GET操作
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		StringBuilder entityStringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		if (statusCode == HttpStatus.SC_OK) {
			// 得到httpResponse的实体数据
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				try {
					bufferedReader = new BufferedReader(new InputStreamReader(
							httpEntity.getContent(), "UTF-8"), 8 * 1024);
					String line = null;
					while ((line = bufferedReader.readLine()) != null) {
						entityStringBuilder.append(line + "/n");
					}
					// 利用从HttpEntity中得到的String生成JsonObject
					// 这次确实jsonObject
					mJsonObject = new JSONObject(entityStringBuilder.toString());
					Message msg = Message.obtain();
					msg.what = GET_SUCCESS;
					// 通过Handler发布传送消息，handler
					this.mHandler.sendMessage(msg);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		} else {
			Message msg = Message.obtain();
			msg.what = GET_ERROR;
			this.mHandler.sendMessage(msg);
		}
	}

	/**
	 * 设置操作执行之后返回的错误编码
	 * 
	 * @param nSuccessCode
	 * @param nErrorCode
	 */
	public void setReturnMsgCode(int nSuccessCode, int nErrorCode) {
		// TODO Auto-generated method stub
		this.GET_SUCCESS = nSuccessCode;
		this.GET_ERROR = nErrorCode;
	}

	public void setJsonObject(JSONObject jsonObject) {
		// TODO Auto-generated method stub
		mJsonObject = jsonObject;
	}

	public JSONObject getJsonObject() {
		return mJsonObject;
	}
}
