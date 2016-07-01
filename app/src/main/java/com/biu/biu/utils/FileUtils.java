package com.biu.biu.utils;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fubo on 2016/6/7 0007.
 * email:bofu1993@163.com
 */
public class FileUtils {

  public static void copy(String sourcePath, String desPath) {
    try {
      int bytesum = 0;
      int byteread = 0;
      File oldfile = new File(sourcePath);
      if (oldfile.exists()) { //文件存在时
        InputStream inStream = new FileInputStream(sourcePath); //读入原文件
        FileOutputStream fs = new FileOutputStream(desPath);
        byte[] buffer = new byte[1024];
        while ( (byteread = inStream.read(buffer)) != -1) {
          bytesum += byteread; //字节数 文件大小
          fs.write(buffer, 0, byteread);
        }
        inStream.close();
      }
    }
    catch (Exception e) {
      System.out.println("复制单个文件操作出错");
      e.printStackTrace();

    }
  }

  public static void saveFile(File file, String path) {
    try {
      int bytesum = 0;
      int byteread = 0;
      if (file.exists()) { //文件存在时
        InputStream inStream = new FileInputStream(file); //读入原文件
        FileOutputStream fs = new FileOutputStream(path);
        byte[] buffer = new byte[1024];
        while ( (byteread = inStream.read(buffer)) != -1) {
          bytesum += byteread; //字节数 文件大小
          fs.write(buffer, 0, byteread);
        }
        inStream.close();
      }
    }
    catch (Exception e) {
      System.out.println("复制单个文件操作出错");
      e.printStackTrace();

    }
  }

  public static void deleteFile(String path) {
    File file = new File(path);
    if (file.exists()) {
      file.delete();
    }
  }
}
