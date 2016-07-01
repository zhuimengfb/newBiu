package com.biu.biu.main;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/*
 * 处理时间
 */
public class MyDateTimeDeal {
	private final long lminute = 1000 * 60;
	private final long lhour = lminute * 60;
	private final long lday = lhour * 24; 
	
	
	public String getTimeGapDesc(String userTime){
		String result = new String();
		if(userTime.isEmpty())	// 若给定参数为空，则返回null
			return null;
		// 将给定的字符串转化为Calendar对象，若失败则返回错误
		Calendar usercalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));	// 获得北京时间，防止时区设置的关系导致显示错误。	
		try{
			usercalendar.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(userTime));
		}catch(ParseException e){
			// 转换失败
			e.printStackTrace();
		}
		
		// 获得当前系统时间对象
		Calendar systime = Calendar.getInstance();
		
		long lnGap =  systime.getTimeInMillis() - usercalendar.getTimeInMillis();
		if(lnGap > lday){
			result = userTime.substring(0, 10);	// 大于一天，则取前10位，只获得具体的天数
		}else if(lnGap < lday && lnGap > lhour){
			// 小于一天，但是多余一小时
			Integer nhourtimes = (int) (lnGap / lhour);
			result = nhourtimes.toString() + "小时" + "前";
		}else if(lnGap < lhour && lnGap > lminute){
			Integer nminutetimes = (int) (lnGap / lminute);
			result = nminutetimes.toString() + "分钟" + "前";
		}else if(lnGap < lminute){
			Integer nsecondtimes = (int) (lnGap / lminute);
			if(nsecondtimes < 1)	// 最小为1秒
				nsecondtimes = 1;
			result = nsecondtimes.toString() + "秒钟" + "前";
		}
		
		return result;
	}
} 
