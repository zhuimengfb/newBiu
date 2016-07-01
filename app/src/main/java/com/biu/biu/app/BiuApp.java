package com.biu.biu.app;

import android.content.Context;
import android.graphics.Typeface;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.biu.biu.contact.model.MessageReceiver;

import cn.jpush.im.android.api.JMessageClient;
import grf.biu.R;
import io.realm.RealmConfiguration;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class BiuApp extends MultiDexApplication {
  private static final String TAG = "BiuPush";
  public static Typeface globalTypeface;
  private static Context context;
  private static RealmConfiguration realmConfiguration;
  private static final int CURRENT_DB_VERSION = 1;

  @Override
  public void onCreate() {
    // TODO Auto-generated method stub
    super.onCreate();
    Log.d("BiuJPush", "JPush is started!!");
    // 百度地图的初始化，。在应用启动的时候初始化
    globalTypeface = Typeface.createFromAsset(getAssets(), "font/qianhei.TTF");
    context = getApplicationContext();
    CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("font/qianhei.TTF")
        .setFontAttrId(R.attr.fontPath)
        .build()
    );
    JMessageClient.init(this);
    JMessageClient.registerEventReceiver(new MessageReceiver(), 1000);
    new NotificationClickEventReceiver(getApplicationContext());
    realmConfiguration = new RealmConfiguration.Builder(this).name("biu.realm").schemaVersion
        (CURRENT_DB_VERSION).deleteRealmIfMigrationNeeded().build();
//    SDKInitializer.initialize(this);
  }

  public static Context getContext() {
    return context;
  }

  public static RealmConfiguration getRealmConfiguration() {
    return realmConfiguration;
  }
}
