package com.polaris.image.util;

/**
 * 公共工具类
 * @author polaris
 * @date 2019年11月13日
 */
public class CommonUtil {

	/**
	 * 获取文件前缀
	 * @param filename
	 * @return
	 */
	public static String getPrefix(String filename) {
		if(isNotBlank(filename)) {
			return filename.substring(0,filename.indexOf("."));
		}
		return null;
	}
	
	/**
	 * 获取文件后缀
	 * @param filename
	 * @return
	 */
	public static String getSuffix(String filename) {
		if(isNotBlank(filename)) {
			return filename.substring(filename.lastIndexOf(".")+1, filename.length());
		}
		return null;
	}
	
	/**
	 * 字符串是否为空检测
	 * @param str 字符串
	 * @return true or false
	 */
	public static boolean isBlank(String str){
		return !isNotBlank(str);
	}
	
	/**
	 * 字符串是否不为空检测
	 * @param str 字符串
	 * @return true or false
	 */
	public static boolean isNotBlank(String str){
		return str !=null && str.length()>0 && str.trim().length() >0;
	}
}
