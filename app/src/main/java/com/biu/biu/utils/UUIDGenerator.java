package com.biu.biu.utils;

import java.util.Random;
import java.util.UUID;

public class UUIDGenerator {

	public UUIDGenerator() {
	}

	/**
	 * 获得一个UUID
	 * 
	 * @return String UUID
	 */
	public static String getUUID() {
		String s = UUID.randomUUID().toString().toUpperCase();
		// 去掉“-”符号
		return s.substring(0, 8) + s.substring(9, 13) + s.substring(14, 18)
				+ s.substring(19, 23) + s.substring(24);
	}

	/**
	 * 获得指定数目的UUID
	 * 
	 * @param number
	 *            int 需要获得的UUID数量
	 * @return String[] UUID数组
	 */
	public static String[] getUUID(int number) {
		if (number < 1) {
			return null;
		}
		String[] ss = new String[number];
		for (int i = 0; i < number; i++) {
			ss[i] = getUUID();
		}
		return ss;
	}
	public static String getNumberUUID(int length){
		String result="";
		Random random=new Random();
		for (int i=0;i<length;i++){
			result+=Math.abs((random.nextInt()))%10;
		}
		return result;
	}
	public static String getLetterUUID(int length){
		String result="";
		Random random=new Random();
		for (int i=0;i<length;i++){
			result+=(char)('A'+Math.abs((random.nextInt()))%26);
		}
		return result;
	}
	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			System.out.println(UUIDGenerator.getUUID());
		}
	}

}
