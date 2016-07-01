package com.biu.biu.main;


import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.biu.biu.app.BiuApp;
import com.biu.biu.contact.views.ContactListFragment;
import com.biu.biu.service.StickyService;
import com.biu.biu.thread.PostTempPosition;
import com.biu.biu.user.entity.AppUserInfo;
import com.biu.biu.user.entity.SimpleUserInfo;
import com.biu.biu.user.entity.UserPicInfo;
import com.biu.biu.user.entity.UserPicInfoCommons;
import com.biu.biu.user.model.UserModel;
import com.biu.biu.user.utils.CommonString;
import com.biu.biu.user.utils.UserPreferenceUtil;
import com.biu.biu.userconfig.UserConfigParams;
import com.biu.biu.utils.FileUtils;
import com.biu.biu.utils.GlobalString;
import com.biu.biu.utils.UUIDGenerator;
import com.biu.biu.views.base.BaseActivity;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import grf.biu.R;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author grf
 * @brief HOME和ME的滑动页面
 */
public class MainActivity extends BaseActivity implements AMapLocationListener {
  private static final int START_PAGE_INDEX = 0;
  private List<Fragment> mhomeFragmentList = new ArrayList<Fragment>();
  private HomeFragmentAdapter mFragmentAdapter;

  private static final int TAB_CONTACT = 1;
  private static final int TAB_HOME = 0;
  private static final int TAB_ME = 2;

  // private ImageView mTabLineIv;
  private HomeFragment mHomeFg;
  private MeFragment mMeFg;
  private ContactListFragment contactListFragment;
  // 定义热帖页面的碎片
  private HotHomeFragment hotHomeFragment;
  private PeepFragment mPeepFg;
  private ViewPager mPageVp;
  private LocationManagerProxy mLocationManagerProxy;
  private boolean mfirstStartInit = true;
  private Double mainLat = 38.00; // 维度
  private Double mainLng = 125.00; // 经度
  private boolean mtellupdateresult = false;
  final int AIRPLAY_MESSAGE_HIDE_TOAST = 2;
  MainHandler m_Handler;
  private Toast mToast;
  private MyReceiver myReceiver;

  // 定义两个改变首页内容的按钮控件
  private Button homeFrgNew;
  private Button homeFrgHot;

  // 定义两个imageView显示两个页面的新消息的状态
  private ImageView peepStatusView = null;
  private ImageView meStatusView = null;

  //简单使用，因此直接创建
  private UserModel userModel = new UserModel();

  @BindView(R.id.bottom_navigation_bar)
  BottomNavigationBar bottomNavigationBar;
  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.toolbar_tab)
  LinearLayout toolbarLinearLayout;
  @BindView(R.id.toolbar_title)
  TextView toolbarTitle;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    initReceiver();
    initSdk();
    initView();
    defineHomeBt();
    checkSoftwareUpdate();
    initUserName();
    Bundle bundle = getIntent().getExtras();
    if (bundle != null && bundle.getBoolean("newFriend")) {
      mPageVp.setCurrentItem(1);
    }
  }

  private void initUserName() {
    if (StringUtils.isEmpty(UserPreferenceUtil.getPreferences().getString(UserPreferenceUtil
        .USER_PREFERENCE_NAME_KEY, ""))) {
      if (UserPreferenceUtil.geteFirstLaunch()) {
        userModel.getMyInfo(UserConfigParams.device_id, new Subscriber<SimpleUserInfo>() {
          @Override
          public void onCompleted() {

          }

          @Override
          public void onError(Throwable e) {
            e.printStackTrace();
            showSetNameDialog();
          }

          @Override
          public void onNext(SimpleUserInfo simpleUserInfo) {
            if (simpleUserInfo != null && !StringUtils.isEmpty(simpleUserInfo.getDevice_id())) {
              UserPreferenceUtil.updateFirstLaunch();
              UserPreferenceUtil.updateUserInfo(simpleUserInfo);
              //更新JMessage
              Observable.just(simpleUserInfo).subscribeOn(Schedulers.newThread())
                  .subscribe(new Subscriber<SimpleUserInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(SimpleUserInfo simpleUserInfo) {
                      UserInfo userInfo = JMessageClient.getMyInfo();
                      userInfo.setNickname(simpleUserInfo.getNickname());
                      JMessageClient.updateMyInfo(UserInfo.Field.nickname, userInfo, new
                          BasicCallback() {
                            @Override
                            public void gotResult(int i, String s) {
                              if (i != 0) {
                                UserPreferenceUtil.setUpdateNickNameFail();
                              } else {
                                UserPreferenceUtil.setUpdateNickNameSuccess();
                              }
                            }
                          });
                    }
                  });
              //更新个人头像信息
              Observable.just(simpleUserInfo.getIcon_large()).subscribeOn(Schedulers.newThread())
                  .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                      e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                      try {
                        File file = Glide.with(BiuApp.getContext()).load(GlobalString.BASE_URL +
                            "/" + s).downloadOnly(1920, 1080).get();
                        String localAddress = CommonString.USER_ICON_PATH + UserPreferenceUtil
                            .getUserPreferenceId() + "-" + UUIDGenerator.getUUID() + ".png";
                        FileUtils.saveFile(file, localAddress);
                        UserPreferenceUtil.setUserIconLocalAddress(localAddress);
                      } catch (InterruptedException e) {
                        e.printStackTrace();
                      } catch (ExecutionException e) {
                        e.printStackTrace();
                      }
                    }
                  });
              //更新照片墙信息
              String[] picAddress = simpleUserInfo.getShowoff().split(";");
              Observable.from(picAddress).subscribeOn(Schedulers.newThread())
                  .subscribe(new Subscriber<String>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                      e.printStackTrace();
                    }

                    @Override
                    public void onNext(String s) {
                      UserPicInfo userPicInfo = new UserPicInfo();
                      String picId = UUIDGenerator.getUUID();
                      String localAddress = CommonString.USER_PIC_SHOW_PATH + UserPreferenceUtil
                          .getUserPreferenceId() + "-" + picId + ".png";
                      try {
                        File file = Glide.with(BiuApp.getContext()).load(GlobalString.BASE_URL +
                            "/" + s).downloadOnly(1920, 1080).get();
                        FileUtils.saveFile(file, localAddress);
                        userPicInfo.setLocalPath(localAddress);
                      } catch (InterruptedException e) {
                        e.printStackTrace();
                      } catch (ExecutionException e) {
                        e.printStackTrace();
                      }
                      userPicInfo.setUserId(UserPreferenceUtil.getUserPreferenceId());
                      userPicInfo.setNetAddress(s);
                      userPicInfo.setPicId(picId);
                      userPicInfo.setFlag(UserPicInfoCommons.FLAG_NORMAL);
                      userModel.saveUserPicToDB(userPicInfo);
                    }
                  });
            } else {
              showSetNameDialog();
            }
          }
        });
      }
    } else {
      if (UserPreferenceUtil.getPreferences().getBoolean(UserPreferenceUtil
          .USER_UPDATE_NICK_NAME_FAIL, true)) {
        updateUserNickName(UserPreferenceUtil.getPreferences().getString(UserPreferenceUtil
            .USER_PREFERENCE_NAME_KEY, ""));
      }
    }
  }

  private void showSetNameDialog() {
    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(MainActivity.this).inflate(R
        .layout.edit_dialog_layout, null);
    final EditText editText = (EditText) linearLayout.findViewById(R.id.et_dialog);
    final AlertDialog alertDialog = new AlertDialog.Builder(this).setTitle(getString(R.string
        .set_name)).setView(linearLayout)
        .setMessage(getString(R.string.please_set_your_user_name))
        .setCancelable(false).setPositiveButton(getString(R.string.confirm), new
            DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                if (!StringUtils.isEmpty(editText.getText().toString())) {
                  UserPreferenceUtil.getPreferences().edit().putString(UserPreferenceUtil
                      .USER_PREFERENCE_NAME_KEY, editText.getText().toString()).apply();
                  updateUserNickName(editText.getText().toString());
                }
              }
            }).create();
    alertDialog.show();
  }

  private void updateUserNickName(String nickName) {
    UserInfo myInfo = JMessageClient.getMyInfo();
    if (myInfo != null) {
      myInfo.setNickname(nickName);
      JMessageClient.updateMyInfo(UserInfo.Field.nickname, myInfo, new BasicCallback() {

        @Override
        public void gotResult(int i, String s) {
          if (i != 0) {
            UserPreferenceUtil.getPreferences().edit().putBoolean(UserPreferenceUtil
                .USER_UPDATE_NICK_NAME_FAIL, true).apply();
          } else {
            UserPreferenceUtil.getPreferences().edit().putBoolean(UserPreferenceUtil
                .USER_UPDATE_NICK_NAME_FAIL, false).apply();
          }
        }
      });
    }
    //TODO 服务器更新
    userModel.renameNickName(UserConfigParams.device_id, nickName);
  }

  private void initReceiver() {
    myReceiver = new MyReceiver();
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(MeFragment.ME_FRAGMENT_MSG_UPDATE_ACTION);
    intentFilter.addAction(PeepFragment.PEEP_FRAGMENT_MSG_UPDATE_ACTION);
    intentFilter.addAction(GlobalString.ACTION_NEW_FRIEND_REQUEST);
    intentFilter.addAction(GlobalString.ACTION_CLEAR_BADGE);
    registerReceiver(myReceiver, intentFilter);
  }

  private void defineNotificationStyle() {
    // 传统通知样式
    // 定义通知
    BasicPushNotificationBuilder basicBuild = new BasicPushNotificationBuilder(
        MainActivity.this);
    basicBuild.statusBarDrawable = R.drawable.icon;
    basicBuild.notificationFlags = Notification.FLAG_AUTO_CANCEL
        | Notification.FLAG_SHOW_LIGHTS; // 设置为自动消失和呼吸灯闪烁
    basicBuild.notificationDefaults = Notification.DEFAULT_SOUND
        | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS;
    JPushInterface.setPushNotificationBuilder(1, basicBuild);
    // 自定义样式
    CustomPushNotificationBuilder builder = new CustomPushNotificationBuilder(
        MainActivity.this, R.layout.biu_notication_style, R.id.icon,
        R.id.title, R.id.text);
    // 指定定制的 Notification Layout
    builder.statusBarDrawable = R.drawable.icon;
    // 指定最顶层状态栏小图标
    builder.layoutIconDrawable = R.drawable.icon;
    JPushInterface.setPushNotificationBuilder(2, builder);
  }

  // 判定是否显示菜单条上面的两个红点标记
  private void showOrHideStatus() {
    if (UserConfigParams.peepStatus) {
      peepStatusView.setVisibility(View.VISIBLE);
    } else {
      peepStatusView.setVisibility(View.INVISIBLE);
    }

    if (UserConfigParams.meStatus) {
      meStatusView.setVisibility(View.VISIBLE);
    } else {
      meStatusView.setVisibility(View.INVISIBLE);
    }

  }

  // 菜单按钮的初始化
  private void initMenuItem() {
    if (UserConfigParams.peepStatus) {
      peepStatusView.setVisibility(View.VISIBLE);
    } else {
      peepStatusView.setVisibility(View.INVISIBLE);
    }

    if (UserConfigParams.meStatus) {
      meStatusView.setVisibility(View.VISIBLE);
    } else {
      meStatusView.setVisibility(View.INVISIBLE);
    }
  }

  private void initSdk() {
    // 后台异步执行初始化操作
    JPushInterface.init(getApplicationContext());
    JPushInterface.setLatestNotificationNumber(getApplicationContext(), 3);
    this.defineNotificationStyle();
    //SDKInitializer.initialize(getApplicationContext());
    Log.d("packageName", getPackageName());
    Log.d("device_id", UserPreferenceUtil.getUserPreferenceId());
    checkIsSupportedByVersion();
    defineHWBadgeNum();
  }

  private void initView() {
    findById();
    initPager();
    initToolbar();
    initBottomNavigation();
    initMenuItem();
  }

  private void checkSoftwareUpdate() {
    UmengUpdateAgent.setUpdateAutoPopup(false); // 不自动弹出窗口
    UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {

      @Override
      public void onUpdateReturned(int updateStatus,
                                   UpdateResponse updateInfo) {
        // TODO Auto-generated method stub
        switch (updateStatus) {
          case UpdateStatus.Yes:
            // 有更新
            UmengUpdateAgent.showUpdateDialog(MainActivity.this, updateInfo);
            break;
          case UpdateStatus.No:
            // 没有更新
            daelUpdateNoResult();
            break;
          case UpdateStatus.NoneWifi: // none wifi
            if (mtellupdateresult) {
              Toast.makeText(MainActivity.this, "没有wifi连接， 只在wifi下更新", Toast.LENGTH_SHORT).show();
            }
            break;
          case UpdateStatus.Timeout: // time out
            Toast.makeText(MainActivity.this, "超时", Toast.LENGTH_SHORT).show();
            break;

        }
      }

      private void daelUpdateNoResult() {
        // TODO Auto-generated method stub
        if (mtellupdateresult) {
          showToast("已经是最新版本");
          Message delayMsg = m_Handler.obtainMessage(AIRPLAY_MESSAGE_HIDE_TOAST);
          m_Handler.sendMessageDelayed(delayMsg, 500);
        }
      }
    });
    m_Handler = new MainHandler();
    // 每次启动时自动检查一遍更新
    UmengUpdateAgent.update(getApplicationContext());
  }

  private void initToolbar() {
    setSupportActionBar(toolbar);
  }

  public Toolbar getToolbar() {
    return toolbar;
  }

  private void initBottomNavigation() {
    bottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.tab_home_page, getString(R
        .string.home)))
        .addItem(new BottomNavigationItem(R.drawable.tab_contact_page, getString(R.string.contact)))
        .addItem(new BottomNavigationItem(R.drawable.tab_me, getString(R.string.me)));
    bottomNavigationBar.initialise();
    bottomNavigationBar.selectTab(START_PAGE_INDEX);
    bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {

      @Override
      public void onTabSelected(int i) {
        mPageVp.setCurrentItem(i);
      }

      @Override
      public void onTabUnselected(int i) {

      }

      @Override
      public void onTabReselected(int i) {

      }
    });
  }

  class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent != null) {
        switch (intent.getAction()) {
          case MeFragment.ME_FRAGMENT_MSG_UPDATE_ACTION:
            int number = intent.getIntExtra(MeFragment.KEY_ME_FRAGMENT_MSG_NUMBER, 0);
            if (number > 0 && number <= 99) {
              bottomNavigationBar.showBadgeNumber(2, number + "");
            } else if (number > 99) {
              bottomNavigationBar.showBadgeNumber(2, "99+");
            } else {
              bottomNavigationBar.hideBadge(2);
            }
            break;
          case PeepFragment.PEEP_FRAGMENT_MSG_UPDATE_ACTION:
            bottomNavigationBar.showCircleBadge(0);
            break;
          case GlobalString.ACTION_NEW_FRIEND_REQUEST:
            bottomNavigationBar.showCircleBadge(1);
            break;
          case GlobalString.ACTION_CLEAR_BADGE:
            bottomNavigationBar.hideBadge(1);
            break;
          default:
            break;
        }
      }
    }
  }

  public BottomNavigationBar getBottomNavigationBar() {
    return bottomNavigationBar;
  }

  /*
   * 显示Toast
   */
  public void showToast(String text) {
    if (mToast == null) {
      mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
    } else {
      mToast.setText(text);
      mToast.setDuration(Toast.LENGTH_SHORT);
    }
    mToast.show();
  }

  // 终止显示Toast文本提示
  public void cancelToast() {
    if (mToast != null) {
      mToast.cancel();
    }
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    if (mLocationManagerProxy != null) {
      mLocationManagerProxy.destroy();
    }
    super.onPause();
    JPushInterface.onPause(this);
    MobclickAgent.onPause(this);
  }

  @Override
  protected void onResume() {
    // 显示或影藏红点状态
    showOrHideStatus();
    initGaodeLocation(); // 初始化高德定位系统（每分钟定位一次，500米定位一次）
    SharedPreferences preferences = getSharedPreferences("user_Params",
        MODE_PRIVATE);
    UserConfigParams.device_id = preferences.getString("device_ID", "");
    // 桌面角标为0
    UserConfigParams.badgeNum = 0;
    super.onResume();
    MobclickAgent.onResume(this);
    JPushInterface.onResume(this);
    initAppUserInfo();
    userModel.postJMessageId(UserPreferenceUtil.getUserPreferenceId(), UserPreferenceUtil
        .getUserPreferenceId());
    StickyService.startService(this);
  }

  //TODO 此处需要初始化更多的信息
  private void initAppUserInfo() {
    SharedPreferences preferences = getSharedPreferences("user_Params", MODE_PRIVATE);
    AppUserInfo.userId = preferences.getString("device_ID", "");
  }

  /*
   * 初始化高德地图的相关操作
   */
  private void initGaodeLocation() {
    // TODO Auto-generated method stub
    mLocationManagerProxy = LocationManagerProxy.getInstance(this);

    // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
    // 注意设置合适的定位时间的间隔，并且在合适时间调用removeUpdates()方法来取消定位请求
    // 在定位结束后，在合适的生命周期调用destroy()方法
    // 其中如果间隔时间为-1，则定位只定一次
    mLocationManagerProxy.requestLocationData(
        LocationProviderProxy.AMapNetwork, 5000, 20, this);
  }

  private void findById() {
    mPageVp = (ViewPager) this.findViewById(R.id.id_page_vp);
    peepStatusView = (ImageView) this.findViewById(R.id.peep_status);
    meStatusView = (ImageView) this.findViewById(R.id.me_status);
    homeFrgNew = (Button) this.findViewById(R.id.home_frg_new);
    homeFrgHot = (Button) this.findViewById(R.id.home_frg_hot);
  }

  private void initPager() {
    // TODO Auto-generated method stub
    // 创建两个Fragment对象
    if (mfirstStartInit) {
      mHomeFg = new HomeFragment();
      mMeFg = MeFragment.getInstance(meStatusView);
      //mPeepFg = PeepFragment.getInstance(peepStatusView);
      contactListFragment = ContactListFragment.getInstance();
      hotHomeFragment = new HotHomeFragment();
      //mhomeFragmentList.add(mPeepFg);
      mhomeFragmentList.add(mHomeFg);
      mhomeFragmentList.add(contactListFragment);
      mhomeFragmentList.add(mMeFg);
      mFragmentAdapter = new HomeFragmentAdapter(this.getSupportFragmentManager(),
          mhomeFragmentList);
      mPageVp.setAdapter(mFragmentAdapter);
      mPageVp.setCurrentItem(START_PAGE_INDEX, false);
      modifyToolbar(START_PAGE_INDEX);
      mPageVp.setOffscreenPageLimit(3);
      mPageVp.setOnPageChangeListener(new OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public void onPageScrolled(int position, float offset, int offsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
          // TODO Auto-generated method stub
          modifyToolbar(position);
          bottomNavigationBar.selectTab(position);
          bottomNavigationBar.hideBadge(position);
        }
      });
      mfirstStartInit = false;
    } else {
      // 不是第一次则只更新经纬度
      // mHomeFg.SetLatandLng(mainLat.toString(), mainLng.toString());
    }
  }


  public void modifyToolbar(int position) {
    switch (position) {
      case TAB_CONTACT:
        setContactToolbar();
        break;
      case TAB_HOME:
        setHomeToolbar();
        break;
      case TAB_ME:
        setMeToolbar();
        break;
      default:
        break;
    }
  }

  private void setContactToolbar() {
    toolbarLinearLayout.setVisibility(View.GONE);
    toolbarTitle.setVisibility(View.VISIBLE);
    toolbarTitle.setText(getString(R.string.contact));
  }

  private void setHomeToolbar() {
    toolbarLinearLayout.setVisibility(View.VISIBLE);
    toolbarTitle.setVisibility(View.GONE);
  }

  private void setMeToolbar() {
    toolbarLinearLayout.setVisibility(View.GONE);
    toolbarTitle.setVisibility(View.VISIBLE);
    toolbarTitle.setText(getString(R.string.me));
  }


  @Override
  public void onLocationChanged(Location location) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProviderEnabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onProviderDisabled(String provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onLocationChanged(AMapLocation amapLocation) {
    // TODO Auto-generated method stub
    if (amapLocation != null
        && amapLocation.getAMapException().getErrorCode() == 0) {
      // 获取位置信息
      mainLat = amapLocation.getLatitude();

      mainLng = amapLocation.getLongitude();
      UserConfigParams.latitude = mainLat.toString();
      UserConfigParams.longitude = mainLng.toString();
      UserConfigParams.setLocationGetted(true);
      // 将用户当前的位置信息反馈给服务器端
      String postUrl = "http://api.bbbiu.com:1234/userpoint";
      PostTempPosition pstTempPostiton = new PostTempPosition(m_Handler,
          mainLat, mainLng, postUrl);
      Thread pstTempPosThread = new Thread(pstTempPostiton);
      pstTempPosThread.start();

    } else {
      // Toast.makeText(this, "无法定位当前所在位置，请检查网络连接！",
      // Toast.LENGTH_LONG).show();
      Toast.makeText(this, amapLocation.getAMapException().getErrorMessage(), Toast
          .LENGTH_SHORT).show();
      mLocationManagerProxy.destroy();
      // Log.e("定位错误", amapLocation.getAMapException().getErrorMessage());
    }
  }

  /**
   * 自定义Handler，处理Mainactivity的事件
   *
   * @author grf
   */
  class MainHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
      // TODO Auto-generated method stub
      switch (msg.what) {
        case 0:
          break;
        case AIRPLAY_MESSAGE_HIDE_TOAST: {
          cancelToast();
          break;
        }
      }
      super.handleMessage(msg);
    }

  }

  /**
   * 重载按键按下效果，屏蔽返回键
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // TODO Auto-generated method stub
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      moveTaskToBack(false);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  private int homeBtStatus = 0;

  private void defineHomeBt() {
    homeFrgNew.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        if (homeBtStatus != 0) {
          // 点击显示新帖子
          mFragmentAdapter.changeIndex();
          homeFrgNew.setBackgroundResource(R.drawable.switch_active);
          // 字体为biu蓝
          homeFrgNew.setTextColor(getResources().getColor(
              R.color.biu_main_color));
          homeFrgHot.setBackgroundResource(R.drawable.switch_inactive);
          homeFrgHot.setTextColor(getResources().getColor(
              R.color.biu_font_white));
          homeBtStatus = 0;
          // 将首页改变为Home页面
          mhomeFragmentList.set(0, mHomeFg);
          //					mFragmentAdapter = new HomeFragmentAdapter(
          //							getSupportFragmentManager(), mhomeFragmentList);
          mFragmentAdapter.notifyDataSetChanged();
          mPageVp.setCurrentItem(START_PAGE_INDEX, false);
        }

      }
    });

    homeFrgHot.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        // TODO Auto-generated method stub
        if (homeBtStatus != 1) {
          mFragmentAdapter.changeIndex();
          Log.i("Hot", "changed..................");
          homeFrgNew.setBackgroundResource(R.drawable.switch_inactive);
          homeFrgHot.setTextColor(getResources().getColor(
              R.color.biu_main_color));
          homeFrgHot.setBackgroundResource(R.drawable.switch_active);
          homeFrgNew.setTextColor(getResources().getColor(
              R.color.biu_font_white));
          homeBtStatus = 1;
          mhomeFragmentList.set(0, hotHomeFragment);
          //					mFragmentAdapter = new HomeFragmentAdapter(
          //							getSupportFragmentManager(), mhomeFragmentList);
          mFragmentAdapter.notifyDataSetChanged();
          mPageVp.setCurrentItem(START_PAGE_INDEX, false);

        }
      }
    });

  }

  // 华为手机桌面角标设置
  private void defineHWBadgeNum() {
    if (!isSupportedBade) {
      Log.i("badgedemo", "not supported badge!");
      return;
    }
    try {
      Bundle bunlde = new Bundle();
      bunlde.putString("package", getPackageName());
      bunlde.putString("class", "com.biu.biu.main.MainActivity");
      // bunlde.putInt("badgenumber", UserConfigParams.badgeNum);
      bunlde.putInt("badgenumber", 50);
      ContentResolver t = this.getContentResolver();
      Bundle result = t
          .call(Uri
                  .parse("content://com.huawei.android.launcher.settings/badge/"),
              "change_launcher_badge", "", bunlde);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  boolean isSupportedBade = false;

  public void checkIsSupportedByVersion() {
    try {
      PackageManager manager = getPackageManager();
      PackageInfo info = manager.getPackageInfo(
          "com.huawei.android.launcher", 0);
      if (info != null && info.versionCode >= 63029) {
        isSupportedBade = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onDestroy() {
    unregisterReceiver(myReceiver);
    super.onDestroy();
  }

}
