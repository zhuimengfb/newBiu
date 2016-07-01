package com.biu.biu.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import com.biu.biu.main.dialog.ShowImgDialog;

import grf.biu.R;

/**
 * Author: FBi on 6/21/16.
 * Email: bofu1993@163.com
 */
public class ShowImageUtil {
  private Context context;
  private ImageView imageView;

  public ShowImageUtil(Context context, ImageView imageView) {
    this.context = context;
    this.imageView = imageView;
  }

  public void showImage() {
    imageView.setDrawingCacheEnabled(true);
    Bitmap content = Bitmap.createBitmap(imageView
        .getDrawingCache());
    imageView.setDrawingCacheEnabled(false);
    Dialog showImgDialog = new ShowImgDialog(context,
        R.style.ShowImgDialog, content);
    // 定义新的显示图片
    showImgDialog.show();
    Activity activity = (Activity) context;
    WindowManager windowManager = activity.getWindowManager();
    Display display = windowManager.getDefaultDisplay();
    WindowManager.LayoutParams lp = showImgDialog
        .getWindow().getAttributes();
    lp.width = (int) (display.getWidth()); // 设置宽度
    lp.height = (int) (display.getHeight());
    showImgDialog.getWindow().setAttributes(lp);
  }
}
