package com.biu.biu.morehottips;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * 从服务器获取热帖操作线程
 * @author grf
 *
 */
public class GetMoreHotThread implements Runnable{
	private Handler mhandler = null;
	private final int E_OK = 0;				// 执行成功
	private final int E_ERROR = -1;			// 执行出错
	private String httpurl = null;
	private String mresult = null;			// 存储操作结果
	private String merrorMsg = null;		// 错误信息
	
	/**
	 * 
	 * @param mhandler
	 * @param hosturl:要操作的目标IP
	 */
	public GetMoreHotThread(Handler mhandler, String url) {
		super();
		this.mhandler = mhandler;
		this.httpurl = url;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		HttpClient httpClient = new DefaultHttpClient();
		StringBuilder urlStringBuilder = new StringBuilder(httpurl);
		StringBuilder entityStringBuilder = new StringBuilder();
		// 利用URL生成一个HttpGet请求
		HttpGet httpGet = new HttpGet(urlStringBuilder.toString());
		BufferedReader bufferedReader = null;
		HttpResponse httpResponse = null;

		try {
			// HttpClient发出一个HttpGet请求
			httpResponse = httpClient.execute(httpGet);
		} catch (Exception e) {
			setMerrorMsg(e.getMessage());
			sendErrorMsg();
			return;
		}
		// 得到httpResponse的状态响应码
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode == HttpStatus.SC_OK) {
			// 得到httpResponse的实体数据
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				try {
					bufferedReader = new BufferedReader(
							new InputStreamReader(httpEntity.getContent(),
									"UTF-8"), 8 * 1024);
					String line = null;
					while ((line = bufferedReader.readLine()) != null) {
						entityStringBuilder.append(line + "/n");
					}
					// 将得到的数据信息以文本的形式存到本地字符串,在外部引用时再生成JSONArray对象进行解析。
					this.setHttpResult(entityStringBuilder.toString());
					
					// 得到了首页数据，传递消息，进行解析并显示
					Message msg = Message.obtain();
					msg.what = E_OK;
					// 通过Handler发布传送消息，handler
					this.mhandler.sendMessage(msg);
				} catch (Exception e) {
					setMerrorMsg(e.getMessage());
					sendErrorMsg();
				}
			}
		}
	}


	/**
	 * 发送错误消息
	 */
	private void sendErrorMsg() {
		// TODO Auto-generated method stub
		Message msg = Message.obtain();
		msg.what = E_ERROR;
		Bundle bundle = new Bundle();
		bundle.putString("e_msg", this.merrorMsg);
		msg.setData(bundle);
		this.mhandler.sendMessage(msg);
	}



	/**
	 * 得到操作执行的结果
	 * @return
	 */
	public String getHttpResult() {
		return mresult;
	}


	/**
	 * 
	 * @param mresult
	 */
	public void setHttpResult(String mresult) {
		this.mresult = mresult;
	}

	public String getMerrorMsg() {
		return merrorMsg;
	}

	public void setMerrorMsg(String merrorMsg) {
		this.merrorMsg = merrorMsg;
	}
	
	
	
}
