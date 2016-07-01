package com.biu.biu.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.jpush.im.android.api.JMessageClient;

/**
 * Author: FBi on 6/27/16.
 * Email: bofu1993@163.com
 */
public class WakeReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    JMessageClient.init(context);
  }
}
