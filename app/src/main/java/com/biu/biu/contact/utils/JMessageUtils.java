package com.biu.biu.contact.utils;

import cn.jpush.im.android.api.JMessageClient;

/**
 * Created by fubo on 2016/6/1 0001.
 * email:bofu1993@163.com
 */
public class JMessageUtils {

  public static void sendSimpleMessage(String userId, String message) {
    JMessageClient.sendMessage(JMessageClient.createSingleTextMessage(userId, message));
  }
}
