package com.biu.biu.contact.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * Created by fubo on 2016/6/8 0008.
 * email:bofu1993@163.com
 */
public class DateParseUtil {
  private static final long NOT_SHOW = 1000 * 60 * 2;
  private static int counter = 0;//计算连续不显示时间

  public static String parseDate(long lastTime, long nowTime) {
    long period = nowTime - lastTime;
    String result = "";
    /*if (lastTime == 0) {
      return DateFormatUtils.format(nowTime, "HH:mm");
    }*/
    if (counter >= 10) {
      counter = 0;
      return DateFormatUtils.format(nowTime, "HH:mm");
    }
    if (period < NOT_SHOW) {
      counter++;
      return result;
    } else {
      counter = 0;
      if (StringUtils.equals(DateFormatUtils.format(nowTime, "yyyy-MM-dd"), DateFormatUtils.format
          (System.currentTimeMillis(), "yyyy-MM-dd"))) {
        return DateFormatUtils.format(nowTime, "HH:mm");
      } else {
        return DateFormatUtils.format(nowTime, "yyyy-MM-dd HH:mm");
      }
    }
  }
}
