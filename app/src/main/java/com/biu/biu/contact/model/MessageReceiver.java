package com.biu.biu.contact.model;

import android.app.NotificationManager;
import android.content.Context;
import android.util.Log;

import com.biu.biu.app.BiuApp;

import cn.jpush.im.android.api.event.MessageEvent;

/**
 * Created by fubo on 2016/6/2 0002.
 * email:bofu1993@163.com
 */
public class MessageReceiver {

  public void onEvent(MessageEvent event) {
    Log.d("getMessage", event.getMessage().getFromUser().getUserName());
    NotificationManager notificationManager = (NotificationManager) BiuApp.getContext()
        .getSystemService(Context.NOTIFICATION_SERVICE);
  }
}
