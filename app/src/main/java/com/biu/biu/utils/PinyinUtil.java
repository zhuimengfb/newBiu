package com.biu.biu.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fubo on 2016/6/10 0010.
 * email:bofu1993@163.com
 */
public class PinyinUtil {

  public static String getPinYin(String hanzhis) {
    return getPinYin(hanzhis, false);
  }

  /**
   * Description : 根据汉字获得此汉字的拼音首字母
   *
   * @param hanzhis
   * @return
   */
  public static String getPinYinHeadChar(String hanzhis) {
    return getPinYin(hanzhis, true);
  }

  private static String getPinYin(String hanzhis, boolean isHeadChar) {
    int len = hanzhis.length();
    char[] hanzhi = hanzhis.toCharArray();

    // 设置输出格式
    HanyuPinyinOutputFormat formatParam = new HanyuPinyinOutputFormat();
    formatParam.setCaseType(HanyuPinyinCaseType.UPPERCASE);
    formatParam.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    formatParam.setVCharType(HanyuPinyinVCharType.WITH_V);

    StringBuilder py = new StringBuilder();
    Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]");
    for (int i = 0; i < len; i++) {
      char c = hanzhi[i];
      Matcher matcher = pattern.matcher(String.valueOf(c));
      // 检查是否是汉字,如果不是汉字就不转换
      if (! matcher.matches()) {
        py.append(c);
        continue;
      }
      // 对汉字进行转换成拼音
      try {
        String[] t2 = PinyinHelper.toHanyuPinyinStringArray(c,
            formatParam);
        if (isHeadChar) {
          py.append(t2[0].charAt(0));
        } else {
          py.append(t2[0]);
        }

      } catch (BadHanyuPinyinOutputFormatCombination e) {
        py.append(c);
      }
    }

    return py.toString();
  }


}
