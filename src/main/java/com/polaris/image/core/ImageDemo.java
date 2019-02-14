package com.polaris.image.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
 
import javax.imageio.ImageIO;
/** 
 * 参考：https://blog.csdn.net/lazy_p/article/details/7165999
 * @author 北辰不落雪 
 * @date 2019年2月11日 下午4:34:56 
 * @Description
 */
public class ImageDemo {
	public String PHOTO = "C:\\Users\\tyb\\Desktop\\1.jpg";
	public void binaryImage() throws IOException{
		File file = new File(PHOTO);
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
		
		File newFile = new File("C:\\Users\\tyb\\Desktop\\二值化.jpg");
		ImageIO.write(grayImage, "jpg", newFile);
	}
	    
	public void grayImage() throws IOException{
		File file = new File(PHOTO);
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
		
		File newFile = new File("C:\\Users\\tyb\\Desktop\\灰度化.jpg");
		ImageIO.write(grayImage, "jpg", newFile);
	    }
	    
	    public static void main(String[] args) throws IOException {
			ImageDemo demo = new ImageDemo();
			demo.binaryImage();
			demo.grayImage();
	    }
}
