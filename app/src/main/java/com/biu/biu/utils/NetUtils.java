package com.biu.biu.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.biu.biu.app.BiuApp;

/**
 * Created by fubo on 2016/5/25 0025.
 * email:bofu1993@163.com
 */
public class NetUtils {

  public static boolean isNetConnected() {
    ConnectivityManager connectivityManager = (ConnectivityManager) BiuApp.getContext()
        .getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
    if (networkInfo != null && networkInfo.length > 0) {
      for (int i = 0; i < networkInfo.length; i++) {
        if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
          return true;
        }
      }
    }
    return false;
  }
}
