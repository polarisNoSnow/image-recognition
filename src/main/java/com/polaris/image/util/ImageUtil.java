package com.polaris.image.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * 参考：https://blog.csdn.net/lazy_p/article/details/7165999
 * 
 * @author 北辰不落雪
 * @date 2019年2月11日 下午4:34:56
 * @Description 二值化、灰度化工具类
 */
public class ImageUtil {
	
	public static void main(String[] args) throws Exception {
		ImageUtil demo = new ImageUtil();
		demo.setpName("caton.jpg");
		demo.setpPath(GeneralContants.DESTOP_PATH);
		//demo.binaryImage();
		//demo.grayImage();
		demo.createAsciiPic();
	}

	public ImageUtil() {
	}

	public ImageUtil(String pPath, String pName) {
		this.pPath = pPath;
		this.pName = pName;
	}

	private String pPath;
	private String pName;

	public String getpPath() {
		return pPath;
	}

	public void setpPath(String pPath) {
		this.pPath = pPath + File.separator;
	}

	public String getpName() {
		return pName;
	}

	public void setpName(String pName) {
		this.pName = pName;
	}

	/**
	 * 获取图片地址
	 * 
	 * @return
	 * @throws Exception
	 */
	private String getPhotoUrl() throws Exception {
		if (this.getpPath() == null || this.getpPath().length() <= 0) {
			this.setpPath(GeneralContants.DESTOP_PATH);
		}
		if (this.getpName() == null || this.getpName().length() <= 0) {
			throw new Exception("photo name is not null");
		}
		return this.getpPath() + this.getpName();
	}

	/**
	 * 二值化
	 * 
	 * @throws Exception
	 */
	public void binaryImage() throws Exception {
		File file = new File(getPhotoUrl());
		BufferedImage image = ImageIO.read(file);

		int width = image.getWidth();
		int height = image.getHeight();

		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);// 重点，技巧在这个参数BufferedImage.TYPE_BYTE_BINARY
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}
		String outPath = this.getpPath() + "二值化_" + this.getpName();
		File newFile = new File(outPath);
		ImageIO.write(grayImage, CommonUtil.getSuffix(this.getpName()), newFile);
		System.out.println("二值化完成："+outPath);
	}

	/**
	 * 灰度化
	 * 
	 * @throws Exception
	 */
	public void grayImage() throws Exception {
		File file = new File(getPhotoUrl());
		BufferedImage image = ImageIO.read(file);

		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);// 重点，技巧在这个参数BufferedImage.TYPE_BYTE_GRAY
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}
		String outPath = this.getpPath() + "灰度化_" + this.getpName();
		File newFile = new File(outPath);
		ImageIO.write(grayImage, CommonUtil.getSuffix(this.getpName()), newFile);
		System.out.println("灰度化完成："+outPath);
	}

	/**
	 * 图片符号化输出
	 * @param path 文件路径
	 */
	public void createAsciiPic() throws Exception{
		// 字符串由复杂到简单
		String base = "@#&$%*o!;.";
		String outTxt = CommonUtil.getPrefix(this.getpName())+".txt";
		String outPhoto = CommonUtil.getPrefix(this.getpName())+"_ascii.jpeg";
		BufferedWriter bw = null;
		Graphics graphics = null;
		try {
			File file = new File(this.getpPath()+outTxt);
			if(!file.exists()){
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fileWriter);
			final BufferedImage image = ImageIO.read(new File(getPhotoUrl()));
			int w = image.getWidth();
			int h = image.getHeight();
			// 获取图像上下文
			BufferedImage bufferedImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
			graphics = createGraphics(bufferedImage, w , h, 3);
			for (int y = 0; y < image.getHeight(); y += 2) {
				for (int x = 0; x < image.getWidth(); x++) {
					int pixel = image.getRGB(x, y); //获取RGB值
					int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
					//获取灰度值（0-255）
					float gray =GrayUtil.gray16(r, g, b);
					//对应的字符（灰度值越小，颜色越黑也就是使用复杂的字符）
					int index = Math.round(gray * (base.length() + 1) / 255);
					String indexValue = index >= base.length() ? " " : String.valueOf(base.charAt(index));
					//输出到文本
					bw.write(indexValue);
					//输出到图片
					graphics.drawString(indexValue, x, y);
				}
				bw.newLine();
			}
			String outPath = this.getpPath()+outPhoto;
			FileOutputStream out = new FileOutputStream(outPath);//输出图片的地址
			ImageIO.write(bufferedImage, CommonUtil.getSuffix(outPath), out);
			System.out.println("输出完毕");
		} catch (final IOException e) {
			e.printStackTrace();
		}finally {
			if(bw != null) {
				bw.close();
			}
			if(graphics != null) {
				graphics.dispose();
			}
		}
	}
	
	/**
	 * 画板默认一些参数设置
	 * @param image 图片
	 * @param width 图片宽
 	 * @param height 图片高
	 * @param size 字体大小（包含Font），会影响到输出效果，越小越清晰
	 * @return
	 */
	private static Graphics createGraphics(BufferedImage image, int width,
			int height, int size) {
		Graphics g = image.createGraphics();
		g.setColor(Color.WHITE); // 设置背景色
		g.fillRect(0, 0, width, height);// 绘制背景
		g.setColor(Color.BLACK); // 设置前景色
		g.setFont(new Font("微软雅黑", Font.PLAIN, size)); // 设置字体
		return g;
	}
}
