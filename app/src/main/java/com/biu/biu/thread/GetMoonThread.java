package com.biu.biu.thread;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import com.biu.biu.userconfig.UserConfigParams;

import android.os.Handler;
import android.os.Message;

public class GetMoonThread extends Thread{
		private static final int MSG_GET_ERROR = -1;
		private static final int MSG_GET_OK_0 = 0;
		private static final int MSG_GET_OK_1 = 1;
		private Handler mhandler = null;
		private int mGettedCount = 0;
		private int ncount = 1;
		private JSONArray mJsonArray;
		
		public GetMoonThread(Handler handler, int nGetted){
			this.mhandler = handler;
			this.mGettedCount = nGetted;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			ncount = 1;
			while(!UserConfigParams.hasGettedLocation()){
				try{
					Thread.sleep(200);
					ncount++;
					if(ncount > 50){
						Message msg = Message.obtain();
						msg.what = MSG_GET_ERROR;
						this.mhandler.sendMessage(msg);
						return;
					}
				}catch(InterruptedException e){
					e.printStackTrace();
					return;
				}
			}
			String nextpageurl = "http://api.bbbiu.com:1234/threads";
			nextpageurl = nextpageurl + "?lat=" + UserConfigParams.latitude + "&lng="
					+ UserConfigParams.longitude +"&offset=" + (Integer.valueOf(mGettedCount).toString()) +
					"&limit=5" + "&device_id=" + UserConfigParams.device_id + "&type=1";

			HttpClient httpClient = new DefaultHttpClient();

			StringBuilder urlStringBuilder = new StringBuilder(nextpageurl);
			StringBuilder entityStringBuilder = new StringBuilder();
			// 利用URL生成一个HttpGet请求
			HttpGet httpGet = new HttpGet(urlStringBuilder.toString());
			httpGet.setHeader("Content-Type",
					"application/x-www-form-urlencoded; charset=utf-8");
			BufferedReader bufferedReader = null;
			HttpResponse httpResponse = null;

			try {
				// HttpClient发出一个HttpGet请求
				httpResponse = httpClient.execute(httpGet);
			}catch(UnknownHostException e){
				// 无法连接到主机
				Message msg = Message.obtain();
//				msg.what = NEXT_PAGE_GET_ERROR;
				// 通过Handler发布传送消息，handler
				this.mhandler.sendMessage(msg);
				return;
			}
			catch (Exception e) {
				e.printStackTrace();
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
						// 利用从HttpEntity中得到的String生成JsonObject
						mJsonArray = new JSONArray(
								entityStringBuilder.toString());
						// 得到了首页数据，传递消息，进行解析并显示
						Message msg = Message.obtain();
						if(0 == this.mGettedCount)
							msg.what = MSG_GET_OK_0;
						else
							msg.what = MSG_GET_OK_1;
						
						// 通过Handler发布传送消息，handler
						this.mhandler.sendMessage(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else{
				// 获取数据错误
				Message msg = Message.obtain();
				msg.what = MSG_GET_ERROR;
				this.mhandler.sendMessage(msg);
			}
		}
		public JSONArray getmJsonArray() {
			return mJsonArray;
		}
		public void setmJsonArray(JSONArray mJsonArray) {
			this.mJsonArray = mJsonArray;
		}
}
