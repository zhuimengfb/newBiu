package com.biu.biu.user.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by fubo on 2016/6/5 0005.
 * email:bofu1993@163.com
 */
public class CommonString {

  public static final String USER_ICON_PATH = Environment.getExternalStorageDirectory() +
      "/biu/user/icon/";
  public static final String USER_PIC_SHOW_PATH = Environment.getExternalStorageDirectory() +
      "/biu/user/show/";

  static {
    initFolder();
  }

  private static void initFolder() {
    File iconFile = new File(USER_ICON_PATH);
    if (! iconFile.exists()) {
      iconFile.mkdirs();
    }
    File showPath = new File(USER_PIC_SHOW_PATH);
    if (! showPath.exists()) {
      showPath.mkdirs();
    }
  }
}
