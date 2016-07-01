package com.biu.biu.thread;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 发表新话题Post线程，支持上传图片功能
 * 
 * @author grf
 * 
 */
public class PostNewTopicTempThread extends Thread {

	private String mImagePath = null; // 存储图片路径
	private Handler mHandler = null; // 完成操作后发送消息的Handler
	private String mUrl; // 目标url地址
	private String mTopicId;
	private String mContent;
	private Map<String, String> mParametersMap = null;
	private int POST_ERROR = -1;
	private int POST_SUCCESS = 0;

	private double topicLat;
	private double topicLng;

	// private Bitmap mBitmap = null;

	/**
	 * 用于发表Peep页面的Topic
	 * 
	 * @param imgPath
	 * @param handler
	 * @param url
	 * @param topicId
	 * @param content
	 *            @
	 */
	public PostNewTopicTempThread(String imgPath, Handler handler, String url,
			String topicId, String content, double topicLat, double topicLng) {
		super();
		// this.mBitmap = bitmap;
		this.mImagePath = imgPath;
		this.mHandler = handler;
		this.mUrl = url;
		this.mTopicId = topicId;
		this.mContent = content;
		this.topicLat = topicLat;
		this.topicLng = topicLng;
	}

	/**
	 * 带多个Post方法参数时，使用此构造方法,用于HOM和月光宝盒发帖
	 * 
	 * @param imgPath
	 * @param handler
	 * @param url
	 * @param parametersmap
	 */
	public PostNewTopicTempThread(String imgPath, Handler handler, String url,
			Map<String, String> parametersmap) {
		// TODO Auto-generated constructor stub
		super();
		// this.mBitmap = bitmap; // 这里是压缩好的图片
		this.mImagePath = imgPath;
		this.mHandler = handler;
		this.mUrl = url;
		this.mParametersMap = parametersmap;
	}

	/**
	 * 将给定的图片进行压缩，返回压缩后的图片
	 * 
	 * @param src
	 * @return
	 */
	private Bitmap scaleBitmap(Bitmap src) {
		int width = src.getWidth();// 原来尺寸大小
		int height = src.getHeight();
		final float destSize = 500;// 缩放到这个大小,你想放大多少就多少

		// 图片缩放比例，新尺寸/原来的尺寸
		float scaleWidth = ((float) destSize) / width;
		float scaleHeight = ((float) destSize) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		// 返回缩放后的图片
		return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		// TODO Auto-generated method stub
		HttpPost httpPost = new HttpPost(mUrl);
		MultipartEntity reqEntity = new MultipartEntity();
		if (mImagePath != null) {
			// 到这里，得到的bitmap已经是压缩过的bitmap
			File file = new File(mImagePath);
			FileBody filebody = new FileBody(file);
			reqEntity.addPart("img", filebody);
		}
		// 添加文本参数
		if (mParametersMap != null) {
			ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE,
					HTTP.UTF_8);
			for (Entry<String, String> entry : mParametersMap.entrySet()) {
				reqEntity.addPart(entry.getKey(),
						new StringBody(entry.getValue(), contentType));
			}
		} else {
			// 需要在话题帖子上添加位置信息
			ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE,
					HTTP.UTF_8);
			StringBody strcontentBody = new StringBody(mContent, contentType);
			StringBody strtopicidBody = new StringBody(mTopicId, contentType);
			StringBody strTypeBody = new StringBody("3", contentType);
			StringBody StrLatBody = new StringBody(String.valueOf(topicLat),
					contentType);
			StringBody StrLngBody = new StringBody(String.valueOf(topicLng),
					contentType);
			reqEntity.addPart("content", strcontentBody);
			reqEntity.addPart("topic_id", strtopicidBody);
			reqEntity.addPart("type", strTypeBody);
			reqEntity.addPart("lat", StrLatBody);
			reqEntity.addPart("lng", StrLngBody);
		}
		httpPost.setEntity(reqEntity); // 设置请求参数
		// 执行操作
		HttpClient httpClient = new DefaultHttpClient();
		try {
			HttpResponse response = httpClient.execute(httpPost);// 发起请求
																	// 并返回请求的响应
			int statusCode = response.getStatusLine().getStatusCode();
			Message msg = Message.obtain();
			msg.what = statusCode == HttpStatus.SC_OK ? POST_SUCCESS
					: POST_ERROR;
			// 通过Handler发布传送消息，handler
			this.mHandler.sendMessage(msg);
		} catch (Exception e) {
			// TODO: handle exception
			Message msg = Message.obtain();
			msg.what = POST_ERROR;
			// 通过Handler发布传送消息，handler
			this.mHandler.sendMessage(msg);
			e.printStackTrace();
		}
	}

	public void setReturnMsgCode(int nSuccessCode, int nErrorCode) {
		// TODO Auto-generated method stub
		this.POST_SUCCESS = nSuccessCode;
		this.POST_ERROR = nErrorCode;
	}

}
