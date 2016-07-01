package com.biu.biu.netimage;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class ImageDownloader {
	private static final String TAG = "ImageDownloader";
	private HashMap<String, MyAsyncTask> map = new HashMap<String, MyAsyncTask>();
	private Map<String, SoftReference<Bitmap>> imageCaches = new HashMap<String, SoftReference<Bitmap>>();
//	private int mOutWidth = 0;		// 输出图片的高度
//	private int mOutHeight = 0;		// 输出图片的宽度
	
//	public void setOutSize(int nWidth, int nHeight) {
//		this.mOutWidth = nWidth;
//		this.mOutHeight = nHeight;
//	}
	/**
	 * 
	 * @param url 该mImageView对应的url
	 * @param mImageView
	 * @param path 文件存储路径
	 * @param mActivity
	 * @param download OnImageDownload回调接口，在onPostExecute()中被调用
	 */
	public void imageDownload(String strTag, String url,ImageView mImageView,String path,Activity mActivity,OnImageDownload download){
		SoftReference<Bitmap> currBitmap = imageCaches.get(url);	// 通过url从软引用中取图片
		Bitmap softRefBitmap = null;
		if(currBitmap != null){	// 若软引用中存在，则直接使用。
			softRefBitmap = currBitmap.get();
		}
		String imageName = "";
		if(url != null){
			imageName = Util.getInstance().getImageName(url);
		}
		Bitmap bitmap = getBitmapFromFile(mActivity,imageName,path);
		//先从软引用中拿数据
		if(currBitmap != null && mImageView != null && softRefBitmap != null && url.equals(mImageView.getTag())){
			mImageView.setVisibility(View.VISIBLE);		// 若获得了帖子就直接设置图片了。
			mImageView.setImageBitmap(softRefBitmap);
		}
		//软引用中没有，从文件中拿数据
		else if(bitmap != null && mImageView != null && strTag.equals(mImageView.getTag())){
			mImageView.setVisibility(View.VISIBLE);
			mImageView.setImageBitmap(bitmap);
		}
		//文件中也没有，此时根据mImageView的tag，即url去判断该url对应的task是否已经在执行，如果在执行，本次操作不创建新的线程，否则创建新的线程。
		else if(url != null && needCreateNewTask(mImageView)){
			MyAsyncTask task = new MyAsyncTask(strTag, url, mImageView, path,mActivity,download);
			if(mImageView != null){
				Log.i(TAG, "执行MyAsyncTask --> " + Util.flag);
				Util.flag ++;
				task.execute();
				//将对应的url对应的任务存起来
				// 这里不应该是url，而应该存储tag.
				map.put(strTag, task);
			}
		}
	}
	
	/**
	 * 判断是否需要重新创建线程下载图片，如果需要，返回值为true。
	 * @param mImageView
	 * @return
	 */
	private boolean needCreateNewTask(ImageView mImageView){
		boolean b = true;
		if(mImageView != null){
//			String curr_task_tagurl = (String)mImageView.getTag();	// url如果不唯一的话就有了各种问题
			String curr_task_tag = (String)mImageView.getTag();
			if(isTasksContains(curr_task_tag)){
				b = false;
			}
		}
		return b;
	}
	
	/**
	 * 检查该url（最终反映的是当前的ImageView的tag，tag会根据position的不同而不同）对应的task是否存在
	 * @param url
	 * @return
	 */
	private boolean isTasksContains(String url){
		boolean b = false;
		if(map != null && map.get(url) != null){
			b = true;
		}
		return b;
	}
	
	/**
	 * 删除map中该url的信息，这一步很重要，不然MyAsyncTask的引用会“一直”存在于map中
	 * @param tag
	 */
	private void removeTaskFormMap(String tag){
		if(tag != null && map != null && map.get(tag) != null){
			map.remove(tag);
			System.out.println("当前map的大小=="+map.size());
		}
	}
	
	/**
	 * 从文件中拿图片
	 * @param mActivity 
	 * @param imageName 图片名字
	 * @param path 图片路径
	 * @return
	 */
	private Bitmap getBitmapFromFile(Activity mActivity,String imageName,String path){
		Bitmap bitmap = null;
		if(imageName != null){
			File file = null;
			String real_path = "";
			try {
				if(Util.getInstance().hasSDCard()){
					real_path = Util.getInstance().getExtPath() + (path != null && path.startsWith("/") ? path : "/" + path);
				}else{
					real_path = Util.getInstance().getPackagePath(mActivity) + (path != null && path.startsWith("/") ? path : "/" + path);
				}
				file = new File(real_path, imageName);	// 完成了文件的下载操作。
				if(file.exists()){
					// 获得图片的方向：下载的时候把方向给弄没了。
					ExifInterface exif = null;
					try {
						exif = new ExifInterface(file.getAbsolutePath());
					} catch (IOException e) {
						e.printStackTrace();
						exif = null;
					}
					int degree = 0;
					if (exif != null) {
						// 读取图片中相机的方向信息
						int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
//						Log.i("ImageDownloader", "图片中相机的方向为" + ori);
						// 计算角度
						switch (ori) {
						case ExifInterface.ORIENTATION_ROTATE_90:  
							degree = 90;  
		                    break;  
		                case ExifInterface.ORIENTATION_ROTATE_180:  
		                	degree = 180;  
		                    break;  
		                case ExifInterface.ORIENTATION_ROTATE_270:  
		                	degree = 270;  
		                    break;  
		                default:  
		                	degree = 0;  
		                    break;
						}
					}
					
//					bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
					// 获取指定大小的缩略图
					Options options = new Options();
					options.inJustDecodeBounds = true;  // 设置true,decoder会返回null,
			        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);//此时返回bm为空  
			          
			        options.inJustDecodeBounds = false;  
			        int w = options.outWidth;  
			        int h = options.outHeight;  
			        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为  
			        float hh = 800f;//这里设置高度为800f  
			        float ww = 480f;//这里设置宽度为480f  
			        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可  
			        int be = 1;//be=1表示不缩放  
			        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放  
			            be = (int) (options.outWidth / ww);  
			        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放  
			            be = (int) (options.outHeight / hh);  
			        }  
			        if (be <= 0)  
			            be = 1;  
			        options.inSampleSize = be;//设置缩放比例  
			        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了  
			        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);  
					if (degree != 0){
				    	// 旋转图片
				    	Matrix m = new Matrix();
				    	m.postRotate(degree);
				    	bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
				    }
				}
			} catch (Exception e) {
				e.printStackTrace();
				bitmap = null;
			}
		}
		return bitmap;
	}
	
	/**
	 * 将下载好的图片存放到文件中
	 * @param path 图片路径
	 * @param mActivity
	 * @param imageName 图片名字
	 * @param bitmap 图片
	 * @return
	 */
	private boolean setBitmapToFile(String path,Activity mActivity,String imageName,Bitmap bitmap){
		File file = null;
		String real_path = "";
		try {
			if(Util.getInstance().hasSDCard()){
				real_path = Util.getInstance().getExtPath() + (path != null && path.startsWith("/") ? path : "/" + path);
			}else{
				real_path = Util.getInstance().getPackagePath(mActivity) + (path != null && path.startsWith("/") ? path : "/" + path);
			}
			file = new File(real_path, imageName);
			if(!file.exists()){
				File file2 = new File(real_path + "/");
				file2.mkdirs();
			}
			file.createNewFile();
			FileOutputStream fos = null;
			if(Util.getInstance().hasSDCard()){
				fos = new FileOutputStream(file);
			}else{
				fos = mActivity.openFileOutput(imageName, Context.MODE_PRIVATE);
			}
			
			if (imageName != null && (imageName.contains(".png") || imageName.contains(".PNG"))){
				bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
			}
			else{
				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
			}
			fos.flush();
			if(fos != null){
				fos.close();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 辅助方法，一般不调用
	 * @param path
	 * @param mActivity
	 * @param imageName
	 */
	private void removeBitmapFromFile(String path,Activity mActivity,String imageName){
		File file = null;
		String real_path = "";
		try {
			if(Util.getInstance().hasSDCard()){
				real_path = Util.getInstance().getExtPath() + (path != null && path.startsWith("/") ? path : "/" + path);
			}else{
				real_path = Util.getInstance().getPackagePath(mActivity) + (path != null && path.startsWith("/") ? path : "/" + path);
			}
			file = new File(real_path, imageName);
			if(file != null)
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 异步下载图片的方法
	 * @author yanbin
	 *
	 */
	private class MyAsyncTask extends AsyncTask<String, Void, Bitmap>{
		private ImageView mImageView;
		private String url;
		private String mTag;
		private OnImageDownload download;
		private String path;
		private Activity mActivity;
		
		public MyAsyncTask(String tag, String url,ImageView mImageView,String path,Activity mActivity,OnImageDownload download){
			this.mImageView = mImageView;
			this.url = url;
			this.path = path;
			this.mActivity = mActivity;
			this.download = download;
			this.mTag = tag; 
		}

		@Override
		protected Bitmap doInBackground(String... params){
			// 以下载文件的形式，下载图片并写入文件。
			//获取文件名
			try {
				URL myURL = new URL(url);
				URLConnection conn = myURL.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				if (is == null){
					Log.e("ImageDownloader", "无法获得下载输入流，下载文件失败！");
					return null;
				}
	    	// 获得文件大小
	    	int nfileSize = conn.getContentLength();//根据响应获取文件大小
		    if (nfileSize < 0) {
		    	Log.e("ImageDownloader", "无法获得文件大小，下载文件失败！");
		    	return null;
		    }
		    String file_path = "";
		    if(Util.getInstance().hasSDCard()){
				file_path = Util.getInstance().getExtPath() + (path != null && path.startsWith("/") ? path : "/" + path);
			}else{
				file_path = Util.getInstance().getPackagePath(mActivity) + (path != null && path.startsWith("/") ? path : "/" + path);
			}
			
		    String imageName = Util.getInstance().getImageName(url);
		    File file = new File(file_path, imageName);		// 文件路径 + 文件名
		    // 如果文件不存在，则先建立路径
			if(!file.exists()){
				File file2 = new File(file_path + "/");
				file2.mkdirs();
			}
			
			file.createNewFile();		// 建立路径之后，建立文件
			FileOutputStream fos = new FileOutputStream(file);
		    //把数据存入路径+文件名
		    byte buf[] = new byte[1024];
		    do {
		    	//循环读取:逻辑很简单，每次写入至多1024字节，直到读取时读到了-1为止，表示写入完成。
		        int numread = is.read(buf);
		        if (numread == -1)
		        {
		          break;
		        }
		        fos.write(buf, 0, numread);
		      } while (true);
		    // 别忘了关闭文件
		    try
		      {
		        is.close();
		      } catch (Exception ex)
		      {
		        Log.e("tag", "error: " + ex.getMessage(), ex);
		      }
			
		    // 至此，文件就下载完毕了，现在，做这个函数本来要做的事情
		    // 检查图片方向，若需要旋转，则返回旋转后的bitmap。
		    int digree = 0;
		    ExifInterface exif = null;
		    try {
		    	exif = new ExifInterface(file.getAbsolutePath());
		    } catch (IOException e) {
		    	e.printStackTrace();
		    	exif = null;
		    }
		    if (exif != null) {
		    	// 读取图片中相机的方向信息
		    	int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,  
                        ExifInterface.ORIENTATION_UNDEFINED);  
                // 计算旋转角度  
                switch (ori) {  
                case ExifInterface.ORIENTATION_ROTATE_90:  
                    digree = 90;  
                    break;  
                case ExifInterface.ORIENTATION_ROTATE_180:  
                    digree = 180;  
                    break;  
                case ExifInterface.ORIENTATION_ROTATE_270:  
                    digree = 270;  
                    break;  
                default:  
                    digree = 0;  
                    break;  
                }  
		    }
		    
		    Options bitOptions = new Options();
		    // 开始读入图片
		    bitOptions.inJustDecodeBounds = true;
		    // 先得到文件宽高
		    Bitmap data = BitmapFactory.decodeFile(file.getAbsolutePath(), bitOptions); 
		    // 根据返回的图片尺寸计算缩放比例
		    int w = bitOptions.outWidth;  
	        int h = bitOptions.outHeight;  
	        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为  
	        float hh = 800f;//这里设置高度为800f  
	        float ww = 480f;//这里设置宽度为480f  
	        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可  
	        int be = 1;//be=1表示不缩放  
	        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放  
	            be = (int) (bitOptions.outWidth / ww);  
	        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放  
	            be = (int) (bitOptions.outHeight / hh);  
	        }  
	        if (be <= 0)  
	            be = 1;  
	        bitOptions.inSampleSize = be;//设置缩放比例
	        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
	        bitOptions.inJustDecodeBounds = false;
	        data = BitmapFactory.decodeFile(file.getAbsolutePath(), bitOptions);
		    if (digree != 0){
		    	// 旋转图片
		    	Matrix m = new Matrix();
		    	m.postRotate(digree);
		    	data = Bitmap.createBitmap(data, 0, 0, data.getWidth(), data.getHeight(), m, true);
		    }
		    
	    	// 不需要旋转，就直接返回生成的图片
	    	return data;
			} catch(IOException e) {
				e.printStackTrace();
				return null;
			}
		/*
			String imageName = Util.getInstance().getImageName(url);
			if(!setBitmapToFile(path,mActivity,imageName, data)){
				removeBitmapFromFile(path,mActivity,imageName);
			// 这段代码将图片做了压缩，但是压缩之后图片的方向信息就没有啦，遇到非正向图片就准备尴尬吧。
			Bitmap data = null;
			if(url != null){
				try {
					URL c_url = new URL(url);
					InputStream bitmap_data = c_url.openStream();		// 从网络上得到图片
					
					BitmapFactory.Options bitOptions = new BitmapFactory.Options();
					bitOptions.inSampleSize = 4;
					data = BitmapFactory.decodeStream(bitmap_data, null, bitOptions);		// 这句会出现out of memory 错误。
					String imageName = Util.getInstance().getImageName(url);
					if(!setBitmapToFile(path,mActivity,imageName, data)){
						removeBitmapFromFile(path,mActivity,imageName);
					}
					// 当url因为某种原因做不了主键的时候，这句就会达不到预期效果。
//					imageCaches.put(url, new SoftReference<Bitmap>(data.createScaledBitmap(data, 100, 100, true)));
					imageCaches.put(mTag, new SoftReference<Bitmap>(data.createScaledBitmap(data, 100, 100, true)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return data;*/
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			//回调设置图片
			if(download != null){
				download.onDownloadSucc(mTag, result,url,mImageView);
				//该url对应的task已经下载完成，从map中将其删除
//				removeTaskFormMap(url);	// 同样的，url不为主键时
				removeTaskFormMap(mTag);
			}
			super.onPostExecute(result);
		}
		
	}
}