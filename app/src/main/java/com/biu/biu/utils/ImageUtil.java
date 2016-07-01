package com.biu.biu.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: FBi on 6/26/16.
 * Email: bofu1993@163.com
 */
public class ImageUtil {

  public static Bitmap getCompressedImage(String srcPath) {
    BitmapFactory.Options newOpts = new BitmapFactory.Options();
    // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
    newOpts.inJustDecodeBounds = true;
    Bitmap bitmap = null;
    bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

    newOpts.inJustDecodeBounds = false;
    int w = newOpts.outWidth;
    int h = newOpts.outHeight;
    float hh = 800f;
    float ww = 480f;
    // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
    int be = 1;// be=1表示不缩放
    if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
      be = (int) (newOpts.outWidth / ww);
    } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
      be = (int) (newOpts.outHeight / hh);
    }
    Log.i("pp", "缩放比例为" + be);
    if (be <= 0) {
      be = 1;
    }

    newOpts.inSampleSize = be;// 设置缩放比例
    // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
    bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
    return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
  }

  private static Bitmap compressImage(Bitmap image) {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
    int options = 70; // 获得他人建议，直接以90开始压缩
    while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
      baos.reset();// 重置baos即清空baos
      image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
      options -= 10;// 每次都减少10
    }
    ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//
    Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
    return bitmap;
  }

  public static void saveBitmap(Bitmap bitmap, String path) {
    File f = new File(path);
    if (!f.exists()) {
      try {
        f.createNewFile();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    FileOutputStream fOut = null;
    try {
      fOut = new FileOutputStream(f);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // 按照100kb的标准，得到压缩倍率
    int nsize = 100;
    ByteArrayOutputStream sp = new ByteArrayOutputStream();
    do {
      sp.reset();
      bitmap.compress(Bitmap.CompressFormat.JPEG, nsize, sp);
      nsize -= 10;
    } while (sp.toByteArray().length / 1024 > 100);
    // 按照试出来的压缩倍率，压缩图片到文件输出流，并写入文件。
    bitmap.compress(Bitmap.CompressFormat.JPEG, nsize, fOut);
    // Log.e();
    try {
      fOut.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      fOut.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
