package com.biu.biu.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;


/**
 * Author: FBi on 6/27/16.
 * Email: bofu1993@163.com
 */
public class StickyService extends Service {
  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    Intent localService = new Intent(this, StickyService.class);
    startService(localService);
    super.onDestroy();
  }

  public static void startService(Context context) {
    Intent intent = new Intent();
    intent.setClass(context, StickyService.class);
    context.startService(intent);
  }
}
