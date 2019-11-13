package com.polaris.image.core;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.imageio.ImageIO;

import com.polaris.image.util.GeneralContants;
 
/**
 * 参考：https://blog.csdn.net/ubuntu_yanglei/article/details/46443929
 * @author 北辰不落雪 
 * @date 2019年2月11日 下午4:03:13 
 * @Description
 */
public class ReadColorTest {
	/**
	 * 读取一张图片的RGB值，转化为符号记录在日志中
	 * 
	 * @throws Exception
	 */
	public void getImagePixel(String image) throws Exception {
		int[] rgb = new int[3];
		File file = new File(image);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int width = bi.getWidth();
		int height = bi.getHeight();
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		System.out.println("width=" + width + ",height=" + height + ".");
		System.out.println("minx=" + minx + ",miniy=" + miny + ".");
		int acu = 0;
		StringBuffer buffer = new StringBuffer();
		for (int j = miny; j < height; j++) {
			for (int i = minx; i < width; i++) {
				int pixel = bi.getRGB(i, j); // 下面三行代码将一个数字转换为RGB数字
				rgb[0] = (pixel & 0xff0000) >> 16;
				rgb[1] = (pixel & 0xff00) >> 8;
				rgb[2] = (pixel & 0xff);
				//System.out.println("i=" + i + ",j=" + j + ":(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")");
				if(rgb[0]+rgb[1]+rgb[2] < 100){
					buffer.append("@");
				}else{
					buffer.append(" ");
				}
			}
			buffer.append("\r\n");
		}
		PrintWriter	out = new PrintWriter(new FileWriter(new File(GeneralContants.DESTOP_PATH+ "photoPrintTest.txt"),true),true);
		out.println(buffer.toString());
		out.close();
	}
 
	/**
	 * 返回屏幕色彩值
	 * 
	 * @param x
	 * @param y
	 * @return
	 * @throws AWTException
	 */
	private int getScreenPixel(int x, int y) throws AWTException { // 函数返回值为颜色的RGB值。
		Robot rb = null; // java.awt.image包中的类，可以用来抓取屏幕，即截屏。
		rb = new Robot();
		Toolkit tk = Toolkit.getDefaultToolkit(); // 获取缺省工具包
		Dimension di = tk.getScreenSize(); // 屏幕尺寸规格
		System.out.println(di.width);
		System.out.println(di.height);
		Rectangle rec = new Rectangle(0, 0, di.width, di.height);
		BufferedImage bi = rb.createScreenCapture(rec);
		int pixelColor = bi.getRGB(x, y);
		return 16777216 + pixelColor; // pixelColor的值为负，经过实践得出：加上颜色最大值就是实际颜色值。
	}
 
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		zoomImage(GeneralContants.DESTOP_PATH+ "二值化.jpg",GeneralContants.DESTOP_PATH+ "test.jpg");
		int x = 0;
		ReadColorTest rc = new ReadColorTest();
		x = rc.getScreenPixel(100, 345);
		System.out.println(x + " - ");
		rc.getImagePixel(GeneralContants.DESTOP_PATH+ "test.jpg");
	}
	
	/*
     * 图片缩放,w，h为缩放的目标宽度和高度
     * src为源文件目录，dest为缩放后保存目录
     */
    public static void zoomImage(String src,String dest) throws Exception {
		File file = new File(src);
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int w = bi.getWidth();
		int h = bi.getHeight();
        
        double wr=0,hr=0;
        File srcFile = new File(src);
        File destFile = new File(dest);

        BufferedImage bufImg = ImageIO.read(srcFile); //读取图片
        Image Itemp = bufImg.getScaledInstance(w, h, bufImg.SCALE_SMOOTH);//设置缩放目标图片模板
        
        wr=w*0.1 / bufImg.getWidth();     //获取缩放比例
        hr=h*0.1 / bufImg.getHeight();
        
        AffineTransformOp ato = new AffineTransformOp(AffineTransform.getScaleInstance(wr, hr), null);
        Itemp = ato.filter(bufImg, null);
        try {
            ImageIO.write((BufferedImage) Itemp,dest.substring(dest.lastIndexOf(".")+1), destFile); //写入缩减后的图片
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

