package com.polaris.image.util;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
/** 
 * 参考：https://blog.csdn.net/lazy_p/article/details/7165999
 * @author 北辰不落雪 
 * @date 2019年2月11日 下午4:34:56 
 * @Description 二值化、灰度化工具类
 */
public class ImageUtil {
	
	public ImageUtil(){
	}
	
	public ImageUtil(String pPath,String pName){
		this.pPath = pPath;
		this.pName = pName;
	}
	
	private String pPath;
	private String pName;
	
	public String getpPath() {
		return pPath;
	}

	public void setpPath(String pPath) {
		this.pPath = pPath+File.separator;
	}

	public String getpName() {
		return pName;
	}

	public void setpName(String pName) {
		this.pName = pName;
	}

	private String getPhoto() throws Exception{
		if(this.getpPath() ==null || this.getpPath().length() <=0 ){
			this.setpPath(GeneralContants.DESTOP_PATH);
		}
		if(this.getpName() ==null || this.getpName().length() <=0 ){
			throw new Exception("photo name is not null");
		}
		return this.getpPath()+this.getpName();
	}
	
	public void binaryImage() throws Exception{
		File file = new File(getPhoto());
		BufferedImage image = ImageIO.read(file);
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);//重点，技巧在这个参数BufferedImage.TYPE_BYTE_BINARY
		for(int i= 0 ; i < width ; i++){
		    for(int j = 0 ; j < height; j++){
			int rgb = image.getRGB(i, j);
			grayImage.setRGB(i, j, rgb);
		    }
		}
		
		File newFile = new File(this.getpPath()+"二值化_"+this.getpName());
		ImageIO.write(grayImage, "jpg", newFile);
	}
	    
	public void grayImage() throws Exception{
		File file = new File(getPhoto());
		BufferedImage image = ImageIO.read(file);
		
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);//重点，技巧在这个参数BufferedImage.TYPE_BYTE_GRAY
		for(int i= 0 ; i < width ; i++){
		    for(int j = 0 ; j < height; j++){
		    	int rgb = image.getRGB(i, j);
		    	grayImage.setRGB(i, j, rgb);
		    }
		}
		
		File newFile = new File(this.getpPath()+"灰度化_"+this.getpName());
		ImageIO.write(grayImage, "jpg", newFile);
	    }
	    
	    public static void main(String[] args) throws Exception {
	    	ImageUtil demo = new ImageUtil();
	    	demo.setpName("1.jpg");
	    	demo.setpPath(GeneralContants.DESTOP_PATH);
			demo.binaryImage();
			demo.grayImage();
	    }
}
