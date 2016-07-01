package com.biu.biu.netimage;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface OnImageDownload {
	void onDownloadSucc(String Tag, Bitmap bitmap, String c_url, ImageView imageView);
}
