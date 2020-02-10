package com.polaris.image.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 属性文件工具
 * @author polaris
 * @date 2020年1月14日
 */
public class PropertiesUtil {
	
	private static class PropertiesUtilHolder{
        private static final PropertiesUtil INSTANCE = new PropertiesUtil();
    }
    
	private PropertiesUtil() {
		loadProperties();
	}
	
	public static PropertiesUtil getInstance() {
		return PropertiesUtilHolder.INSTANCE;
	}
	
	Properties pro = new Properties();
	
	public Object getValue(Object key) {
		return pro.get(key);
	}
	
	public String getStringValue(Object key) {
		return pro.get(key)==null?"":pro.get(key)+"";
	}
	
	/* 
	 * 加载文件读取属性
	 */
	public void loadProperties(){
		FileInputStream in = null;
		try {
			in = new FileInputStream("src/main/resources/polaris.properties");
			pro.load(in);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(in !=null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
}
