package com.biu.biu.utils;

import android.app.Activity;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import java.lang.ref.WeakReference;

import grf.biu.R;

/**
 * Created by fubo on 2016/6/6 0006.
 * email:bofu1993@163.com
 */
public class ShareUtils {

  // 分享
  private final UMSocialService mController = UMServiceFactory
      .getUMSocialService("com.umeng.share");

  private WeakReference<Activity> activityWeakReference;

  public ShareUtils(Activity activity) {
    activityWeakReference = new WeakReference<Activity>(activity);
    configPlatforms();
  }

  private void configPlatforms() {
    // 添加新浪SSO授权
    // mController.getConfig().setSsoHandler(new SinaSsoHandler());
    // 添加QQ、QZone平台
    addQQQZonePlatform();
    // 添加微信、微信朋友圈平台
    addWXPlatform();
  }

  /**
   * @return
   * @功能描述 : 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
   * image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
   * 要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
   * : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
   */
  private void addQQQZonePlatform() {
    String appId = "100424468";
    String appKey = "c7394704798a158208a74ab60104f0ba";
    // 添加QQ支持, 并且设置QQ分享内容的target url
    UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(activityWeakReference.get(), appId, appKey);
    qqSsoHandler.setTargetUrl("http://www.bbbiu.com");
    qqSsoHandler.addToSocialSDK();

    // 添加QZone平台
    QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(activityWeakReference.get(), appId,
        appKey);
    qZoneSsoHandler.addToSocialSDK();
  }

  /**
   * @return
   * @功能描述 : 添加微信平台分享
   */
  private void addWXPlatform() {
    // 注意：在微信授权的时候，必须传递appSecret
    // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
    String appId = "wxcef5bcd54791e409";
    // String appSecret = "5bb696d9ccd75a38c8a0bfe0675559b3";
    String appSecret = "d4624c36b6795d1d99dcf0547af5443d";
    // 添加微信平台
    UMWXHandler wxHandler = new UMWXHandler(activityWeakReference.get(), appId, appSecret);
    wxHandler.addToSocialSDK();

    // 支持微信朋友圈
    UMWXHandler wxCircleHandler = new UMWXHandler(activityWeakReference.get(), appId, appSecret);
    wxCircleHandler.setToCircle(true);
    wxCircleHandler.addToSocialSDK();
  }

  private void setShareContent(String content, String imgUrl) {
    UMImage urlImage = null;
    if (imgUrl != null) {
      urlImage = new UMImage(activityWeakReference.get(), imgUrl);
    } else {
      urlImage = new UMImage(activityWeakReference.get(), R.drawable.icon);
    }

    // 微信分享
    WeiXinShareContent weiXinShareContent = new WeiXinShareContent();
    weiXinShareContent.setShareContent(content);
    weiXinShareContent.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    weiXinShareContent.setTargetUrl("http://www.bbbiu.com");
    weiXinShareContent.setShareMedia(urlImage);
    mController.setShareMedia(weiXinShareContent);
    // 朋友圈分享
    CircleShareContent circleShareContent = new CircleShareContent();
    // circleShareContent.setShareContent(content);
    // circleShareContent.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    circleShareContent.setTargetUrl("http://www.bbbiu.com");
    // 朋友圈不能显示content的内容
    circleShareContent.setShareContent(content);
    circleShareContent.setTitle(content);
    circleShareContent.setShareMedia(urlImage);
    mController.setShareMedia(circleShareContent);
    // 新浪微博分享
    // SinaShareContent sinaShareContent = new SinaShareContent();
    // sinaShareContent.setShareContent(content);
    // sinaShareContent.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    // sinaShareContent.setShareImage(urlImage);
    // sinaShareContent.setShareMedia(sinaShareContent);

    // qq
    // 设置QQ空间分享内容
    QZoneShareContent qzone = new QZoneShareContent();
    qzone.setShareContent(content);
    qzone.setTargetUrl("http://www.bbbiu.com");
    qzone.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    qzone.setShareMedia(urlImage);
    // qzone.setShareMedia(uMusic);
    mController.setShareMedia(qzone);

    QQShareContent qqShareContent = new QQShareContent();
    qqShareContent.setShareContent(content);
    qqShareContent.setTitle("这是来自Biu的内容，欢迎使用Biu！！！");
    qqShareContent.setShareMedia(urlImage);
    qqShareContent.setTargetUrl("http://www.bbbiu.com");
    mController.setShareMedia(qqShareContent);
  }

  public void openShare() {
    mController.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,
        SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.QQ,
        SHARE_MEDIA.QZONE);
    mController.openShare(activityWeakReference.get(), false);
  }

}
