package com.biu.biu.main;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.biu.biu.app.BiuApp;
import com.biu.biu.userconfig.UserConfigParams;
import com.umeng.analytics.MobclickAgent;

import java.util.UUID;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import grf.biu.R;

//import com.biu.biu.badge.BadgeUtil;

public class SplashActivity extends Activity {
  private final int SPLASH__DISPLAY_LENGTH = 500;
  private SharedPreferences preferences; // 存储设备ID

  // private ImageView imageview;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
      // Activity was brought to front and not created,
      // Thus finishing this will get us to the last viewed
      // activity
      finish();
    }

    //		BadgeUtil.setBadgeCount(getApplicationContext(), MyReceiver.newNum);
    //		BadgeUtil.resetBadgeCount(getApplicationContext());
  }

  /*
   * 获得设备ID编号
   */
  private String getDeviceID() {
    // TODO Auto-generated method stub
    String result = null;
    final TelephonyManager tm = (TelephonyManager) getBaseContext()
        .getSystemService(Context.TELEPHONY_SERVICE);
    final String tmDevice, tmSerial, androidId;
    tmDevice = "" + tm.getDeviceId();
    Log.d("deviceId", tmDevice);
    tmSerial = "" + tm.getSimSerialNumber();
    androidId = ""
        + android.provider.Settings.Secure.getString(
        getContentResolver(),
        android.provider.Settings.Secure.ANDROID_ID);
    UUID deviceUuid = new UUID(androidId.hashCode(),
        ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
    result = deviceUuid.toString();
    return result;
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    getDeviceuuId(); // 每次程序启动时，首先获取设备ID作为全局参数。
    MobclickAgent.onResume(this);
    if (!preferences.getBoolean("registered", false)) {
      register(getDeviceID(), "123456");
    } else {
      if (System.currentTimeMillis() - preferences.getLong("lastLogin", 0L) > 60000 * 60 * 24 *
          5) {
        login(preferences.getString("userName", ""), preferences.getString("password", ""));
      }
      toMainActivity();
    }
  }

  private void login(String userName, String password) {
    JMessageClient.login(userName, password, new BasicCallback() {
      @Override
      public void gotResult(int i, String s) {
        if (i == 0) {
          preferences.edit().putLong("lastLogin", System.currentTimeMillis()).apply();
          UserInfo userInfo = JMessageClient.getMyInfo();
          userInfo.setNickname(getString(R.string.anonymity));
          JMessageClient.updateMyInfo(UserInfo.Field.nickname, userInfo, new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
              toMainActivity();
            }
          });
        }
      }
    });
  }

  private void toMainActivity() {
    UserConfigParams.loginSuccess = true;
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        Intent mainIntent = new Intent();
        mainIntent.setClass(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(mainIntent);
        SplashActivity.this.finish();
      }
    }, SPLASH__DISPLAY_LENGTH);
  }

  private void register(final String userName, final String password) {
    JMessageClient.register(userName, password, new BasicCallback() {
      @Override
      public void gotResult(int i, String s) {
        Log.d("register callback", i + s);
        if (i == 0) {
          //注册成功，进行登陆
          preferences.edit().putBoolean("registered", true).putString("userName", userName)
              .putString("password", password).apply();
          login(userName, password);
        } else if (i == 898001 || i == 899001) {
          //用户已经存在，则直接登陆
          preferences.edit().putBoolean("registered", true).putString("userName", userName)
              .putString("password", password).apply();
          login(userName, password);
        } else {
          Toast.makeText(BiuApp.getContext(), getString(R.string.register_fail), Toast
              .LENGTH_SHORT).show();
        }
      }
    });
  }

  private void getDeviceuuId() {
    preferences = getSharedPreferences("user_Params", MODE_PRIVATE);
    // device_ID = preferences.getString("device_ID", "");
    String device_ID = getDeviceID();
    UserConfigParams.device_id = device_ID;
    Editor editor = preferences.edit();
    editor.putString("device_ID", device_ID);
    editor.commit();
    // if(UserConfigParams.device_id.isEmpty()){
    // // 如果设备ID为空，则获得并重写配置参数
    // Editor editor = preferences.edit();
    // getDeviceID();
    // // 存入数据
    // editor.putString("device_ID", device_ID);
    // editor.commit();
    // }
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    MobclickAgent.onPause(this);

  }
}
