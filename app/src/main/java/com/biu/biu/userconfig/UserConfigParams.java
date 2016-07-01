/**
 * 存储用户信息，在欢迎页面的onCreate初次实例化
 */
package com.biu.biu.userconfig;

public class UserConfigParams {
  public static String device_id = null; // 用户设备ID
  public static String latitude = "38.2345"; // 纬度
  public static String longitude = "125.11"; // 经度
  public static String hosturl = "http://api.bbbiu.com:1234";
  private static boolean bhasgettedlocation = false;
  public static boolean isHomeRefresh = false; // 发表新帖、回复、踩赞等之后首页进行一次额外的刷新操作
  // 桌面角标数字
  public static int badgeNum = 0;
  // 参数peepStatus表示话题页面是否有新的状态
  public static Boolean peepStatus = false;
  // 参数meStatus表示话题页面是否有新的状态
  public static Boolean meStatus = false;

  // 游客身份记录的经纬度坐标
  public static double poiLat = - 1;
  public static double poiLng = - 1;
  public static boolean loginSuccess = false;

  // public static String TOPIC_PHOTO_SAVED_DIR_PATH = "/biu/Topicphoto/";

  /**
   * 是否已经得到位置信息
   *
   * @return true：已经得到位置信息，可以获取。false：还未得到位置信息
   */
  public static boolean hasGettedLocation() {
    return bhasgettedlocation;
  }

  /**
   * 设置是否得到位置信息标记
   *
   * @param bState
   */
  public static void setLocationGetted(boolean bState) {
    bhasgettedlocation = bState;
  }
}
