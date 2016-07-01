package com.biu.biu.thread;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 发表新话题Post线程，支持上传图片功能
 * @author grf
 *
 */
public class PostNewTopicThread extends Thread {
	

	private String mImagePath = null;		// 存储图片路径
	private Handler mHandler = null;		// 完成操作后发送消息的Handler
	private String mUrl;					// 目标url地址
	private String mTopicId;
	private String mContent;
	private byte[] mBitmapArray = null;
	private Map<String, String> mParametersMap = null;
	private int POST_ERROR = -1;
	private int POST_SUCCESS = 0;
//	private Bitmap mBitmap = null;
	
	/**
	 * 用于发表Peep页面的Topic
	 * @param imgPath
	 * @param handler
	 * @param url
	 * @param topicId
	 * @param content
	 */
	public PostNewTopicThread(String imgPath, Handler handler, String url,
			String topicId, String content) {
		super();
//		this.mBitmap = bitmap;
		this.mImagePath = imgPath;
		this.mHandler = handler;
		this.mUrl = url;
		this.mTopicId = topicId;
		this.mContent = content;
	}
	
	/**
	 * 用于发表Peep页面的Topic，图片使用字节流
	 * @param bitmapbytearray
	 * @param handler
	 * @param url
	 * @param topicId
	 * @param content
	 */
	public PostNewTopicThread(byte[] bitmapbytearray, Handler handler, String url,
			String topicId, String content) {
		super();
//		this.mBitmap = bitmap;
		this.mImagePath = null;
		this.mBitmapArray = bitmapbytearray;
		this.mHandler = handler;
		this.mUrl = url;
		this.mTopicId = topicId;
		this.mContent = content;
	}
	
	/**
	 * 带多个Post方法参数时，使用此构造方法,用于HOM和月光宝盒发帖
	 * @param imgPath
	 * @param handler
	 * @param url
	 * @param parametersmap
	 */
	public PostNewTopicThread(String imgPath,
			Handler handler, String url,
			Map<String, String> parametersmap) {
		// TODO Auto-generated constructor stub
		super();
//		this.mBitmap = bitmap;		// 这里是压缩好的图片
		this.mImagePath = imgPath;
		this.mHandler = handler;
		this.mUrl = url;
		this.mParametersMap = parametersmap;
	}
	
	/**
	 * 带多个Post方法参数时，使用此构造方法,用于HOM和月光宝盒发帖
	 * @param bitmapbytearray
	 * @param handler
	 * @param url
	 * @param parametersmap
	 */
	public PostNewTopicThread(byte[] bitmapbytearray,
			Handler handler, String url,
			Map<String, String> parametersmap) {
		// TODO Auto-generated constructor stub
		super();
		this.mImagePath = null;
		this.mBitmapArray = bitmapbytearray;
		this.mHandler = handler;
		this.mUrl = url;
		this.mParametersMap = parametersmap;
	}

	/**
	 * 将给定的图片进行压缩，返回压缩后的图片
	 * @param src
	 * @return
	 */
	private Bitmap scaleBitmap(Bitmap src) {
		 int width = src.getWidth();//原来尺寸大小
	        int height = src.getHeight();
	        final float destSize = 500;//缩放到这个大小,你想放大多少就多少
	 
	//图片缩放比例，新尺寸/原来的尺寸
	        float scaleWidth = ((float) destSize) / width;
	        float scaleHeight = ((float) destSize) / height;
	 
	        Matrix matrix = new Matrix();
	        matrix.postScale(scaleWidth, scaleHeight);
	 
	//返回缩放后的图片
	        return Bitmap.createBitmap(src, 0, 0, width, height, matrix, true);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		 HttpPost httpPost = new HttpPost(mUrl);
		 MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//	     builder.setCharset(Charset.forName("UTF-8"));//设置请求的编码格式
		 builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);//设置浏览器兼容模式
		 if (mImagePath != null){
			 // 到这里，得到的bitmap已经是压缩过的bitmap
//			 ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//			 mbitmap = mbitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//			 builder.addBinaryBody(name, b)
//			 builder.add
//			 FileBody fileBody = new FileBody(new File(mImagePath)); //image should be a String
//			 byte[] byteArray = mImgByteArrayOutStream.toByteArray();
//			 builder.addBinaryBody("img", byteArray);
			 // 这个是本来可以的代码，但是有大小限制
			 // 上传之前，将图片压缩为jpg,按100比例。
			 File file = new File(mImagePath);
			 builder.addBinaryBody("img", file);
//			 builder.add
//			 builder.addPart("img", fileBody);
		 }else if(mBitmapArray != null){
			 // 这种方式好像始终是不行啊，可能需要服务器的支持。还是使用老办法吧。
			 String strbimtmap = Base64.encodeBase64String(mBitmapArray);
//			 ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
//			 StringBody strimgBody = new StringBody(strbimtmap, contentType);
//			 builder.addBinaryBody("img", mBitmapArray);
			 builder.addTextBody("img", strbimtmap);
//			 builder.addPart("img", strimgBody);
		 }
		 // 添加文本参数
		 if (mParametersMap != null) {
			 ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
			 for (Entry<String, String> entry : mParametersMap.entrySet()) {
				 builder.addPart(entry.getKey(), new StringBody(entry.getValue(), contentType));
			 }
		 }else {
			 ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
			 StringBody strcontentBody = new StringBody(mContent, contentType);
			 StringBody strtopicidBody = new StringBody(mTopicId, contentType);
			 builder.addPart("content", strcontentBody);
			 builder.addPart("topic_id", strtopicidBody);
		 }
		 HttpEntity entity = builder.build();	// 生成 HTTP POST 实体
		 httpPost.setEntity(entity);			//设置请求参数
		 
		 // 执行操作
		 HttpClient httpClient = new DefaultHttpClient();
		 try {
			 HttpResponse response = httpClient.execute(httpPost);// 发起请求 并返回请求的响应
			 int statusCode = response.getStatusLine().getStatusCode();
			 Message msg = Message.obtain();
			 msg.what = statusCode == HttpStatus.SC_OK ? POST_SUCCESS : POST_ERROR;
				//通过Handler发布传送消息，handler
				this.mHandler.sendMessage(msg);
		} catch (Exception e) {
			// TODO: handle exception
			Message msg = Message.obtain();
			msg.what = POST_ERROR;
			//通过Handler发布传送消息，handler
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
