package com.polaris.image.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

/**
 * 参考：https://blog.csdn.net/lazy_p/article/details/7165999
 * 
 * @author 北辰不落雪
 * @date 2019年2月11日 下午4:34:56
 * @Description 二值化、灰度化工具类
 */
public class ImageUtil {
	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 20, 10, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());
	
	// 字符串由复杂到简单
	private static final String BASE = "@#&$%*o!;. ";
			
	public static void main(String[] args) throws Exception {
		String fileName = "1.png";
		BufferedImage image = ImageIO.read(new File(GeneralContants.DESTOP_PATH +fileName));
		int a = 0,b = 0;
		for (int i = 0; i < 10; i++) {
			long startTime = System.currentTimeMillis();
			ImageUtil.symbolization(image);
			long midTime = System.currentTimeMillis();
			BufferedImage bufferedImage_ = ImageUtil.hiperSymbolization(image);
			long endTime = System.currentTimeMillis();
			double first = (double)(midTime-startTime)/1000;
			double second = (double)(endTime-midTime)/1000;
			System.out.println(first+":"+second);
			if(second < first) {
				b++;
			}else {
				a++;
			}
			if(i == 0) {
				ImageIO.write(bufferedImage_, CommonUtil.getSuffix(fileName), 
						new FileOutputStream(GeneralContants.DESTOP_PATH + CommonUtil.getPrefix(fileName)+"_符号化."+CommonUtil.getSuffix(fileName)));
			}
		}
		System.err.println(a+":"+b);
		System.exit(0);
	}

	/**
	 * 图片二值化
	 * @param origUrl 源图片地址
	 * @param destUrl 目标地址
	 * @throws Exception
	 */
	public static void binaryImage(String origUrl,String destUrl) throws Exception {
		BufferedImage image =  changeImage(origUrl,BufferedImage.TYPE_BYTE_BINARY);
		if(CommonUtil.isBlank(destUrl)) {
			destUrl =  CommonUtil.getPrefix(origUrl)+"_二值化."+CommonUtil.getSuffix(origUrl);
		}
		ImageIO.write(image, CommonUtil.getSuffix(origUrl), new File(destUrl));
		System.out.println("二值化完成：" + destUrl);
	}

	/**
	 * 图片灰度化
	 * @param origUrl 源地址
	 * @param destUrl 目标地址
	 * @throws Exception
	 */
	public static void grayImage(String origUrl,String destUrl) throws Exception {
		BufferedImage image = changeImage(origUrl,BufferedImage.TYPE_BYTE_GRAY);
		if(CommonUtil.isBlank(destUrl)) {
			destUrl =  CommonUtil.getPrefix(origUrl)+"_灰度化."+CommonUtil.getSuffix(origUrl);
		}
		ImageIO.write(image, CommonUtil.getSuffix(origUrl), new File(destUrl));
		System.out.println("灰度化完成：" + destUrl);
	}

	/**
	 * 转换图片的类型
	 * @param origUrl 图片源地址
	 * @param imageType 转换的类型
	 * @return BufferedImage 
	 * @throws IOException
	 */
	private static BufferedImage changeImage(String origUrl, int imageType) throws IOException {
		File file = new File(origUrl);
		BufferedImage image = ImageIO.read(file);
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage grayImage = new BufferedImage(width, height,imageType);
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int rgb = image.getRGB(i, j);
				grayImage.setRGB(i, j, rgb);
			}
		}
		return grayImage;
	}

	/**
	 * 将图片符号化
	 * 1.图片输出
	 * 2.文本输出
	 * @param origUrl
	 * @throws Exception
	 */
	public static void createAsciiPic(String origUrl) throws Exception {
		String outTxt = CommonUtil.getPrefix(origUrl) + ".txt";
		String outPhoto = CommonUtil.getPrefix(origUrl) + "_符号化."+CommonUtil.getSuffix(origUrl);
		BufferedWriter bw = null;
		Graphics graphics = null;
		try {
			File textFile = new File(outTxt);
			if (!textFile.exists()) {
				textFile.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(textFile.getAbsoluteFile());
			bw = new BufferedWriter(fileWriter);
			final BufferedImage image = ImageIO.read(new File(origUrl));
			int w = image.getWidth();
			int h = image.getHeight();
			// 获取图像上下文
			BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			graphics = createGraphics(bufferedImage, w, h, 3);
			for (int y = 0; y < image.getHeight(); y += 2) {
				for (int x = 0; x < image.getWidth(); x++) {
					int pixel = image.getRGB(x, y); // 获取RGB值
					int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
					// 获取灰度值（0-255）
					float gray = GrayUtil.gray16(r, g, b);
					// 对应的字符（灰度值越小，颜色越黑也就是使用复杂的字符）
					int index = Math.round(gray * (BASE.length() + 1) / 255);
					String indexValue = index >= BASE.length() ? " " : String.valueOf(BASE.charAt(index));
					// 输出到文本
					bw.write(indexValue);
					// 输出到图片
					graphics.drawString(indexValue, x, y);
				}
				bw.newLine();
			}
			System.out.println("文件输出完毕");
			FileOutputStream out = new FileOutputStream(outPhoto);// 输出图片的地址
			ImageIO.write(bufferedImage, CommonUtil.getSuffix(outPhoto), out);
			System.out.println("图片输出完毕");
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				bw.close();
			}
			if (graphics != null) {
				graphics.dispose();
			}
		}
	}

	/**
	 * 画板默认一些参数设置
	 * 
	 * @param image
	 *            图片
	 * @param width
	 *            图片宽
	 * @param height
	 *            图片高
	 * @param size
	 *            字体大小（包含Font），会影响到输出效果，越小越清晰
	 * @return
	 */
	private static Graphics createGraphics(BufferedImage image, int width, int height, int size) {
		Graphics g = image.createGraphics();
		g.setColor(Color.WHITE); // 设置背景色
		g.fillRect(0, 0, width, height);// 绘制背景
		g.setColor(Color.BLACK); // 设置前景色
		g.setFont(new Font("微软雅黑", Font.PLAIN, size)); // 设置字体
		return g;
	}

	/**
	 * 符号化
	 * @param bufferedImage 输入源图片
	 * @return 符号化后的bufferedImage
	 */
	public static BufferedImage symbolization(BufferedImage image) {
		//每次跨越行数，可自行调节，受视频清晰度及视频里面物体远近的影响
		//越小处理速度越慢、但展示效果越靠近原图
		int discardNum = 7;
		int w = image.getWidth();
		int h = image.getHeight();
		//新建图像
		BufferedImage newImage =  new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		//字体大小为1.5倍跨越行数，符号化展示效果比较好
		Graphics graphics = createGraphics(newImage, w, h, discardNum+(discardNum>>1));
		for (int y = 0; y < h; y += discardNum) {
			for (int x = 0; x < w; x += discardNum) {
				int pixel = image.getRGB(x, y); // 获取RGB值
				int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
				// 获取灰度值（0-255）
				float gray = GrayUtil.grayPS(r, g, b);
				// 对应的字符（灰度值越小，颜色越黑也就是使用复杂的字符）
				int index = Math.round(gray * (BASE.length() + 1) / 255);
				String indexValue = index >= BASE.length() ? " " : String.valueOf(BASE.charAt(index));
				// 输出到图片
				graphics.drawString(indexValue, x, y);
			}
		}
		return newImage;
	}
	
	/**
	 * 多线程符号化，在高质量图片+每个像素都打印的前提下效果不是很好，因为上下文切换频繁会增加耗时
	 * @param bufferedImage 输入源图片
	 * @return 符号化后的bufferedImage
	 */
	public  static BufferedImage hiperSymbolization(final BufferedImage image) {
		//跨越的像素，可自行调节，越小处理速度越慢、但展示效果越靠近原图（图像效果也会受物体远近影响）
		//TODO 等一个具体算法（可以根据分辨率归纳出，如按分辨率的百分比）
		int discardNumY = 7;
		int discardNumX = 7;
		//均值
		int discardNum = (discardNumX+discardNumY)/2;
		
		int w = image.getWidth();
		int h = image.getHeight();
		//新建图像
		BufferedImage newImage =  new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		//字体大小为1.5倍跨越行数，符号化展示效果比较好，,可自行调节
		final Graphics graphics = createGraphics(newImage, w, h, discardNum+(discardNum>>1));
		//设置CountDownLatch次数为循环次数，h、w很小的情况暂不考虑
		CountDownLatch countDownLatch = new CountDownLatch((h/discardNumY)*(w/discardNumX));
		for (int y = 0; y < h; y += discardNumY) {
			for (int x = 0; x < w; x += discardNumX) {
				executor.execute(new threadDrawString(x, y) {
					@Override
					public void run() {
						try {
							int pixel = image.getRGB(x, y); // 获取RGB值
							int r = (pixel & 0xff0000) >> 16, g = (pixel & 0xff00) >> 8, b = pixel & 0xff;
							// 获取灰度值（0-255）
							float gray = GrayUtil.grayPS(r, g, b);
							// 对应的字符（灰度值越小，颜色越黑也就是使用复杂的字符）
							int index = Math.round(gray * (BASE.length() + 1) / 255);
							String indexValue = index >= BASE.length() ? " " : String.valueOf(BASE.charAt(index));
							// 输出到图片
							graphics.drawString(indexValue, x, y);
						}catch (Exception e) {
							e.printStackTrace();
						}finally {
							countDownLatch.countDown();
						}
					}
				});
				
			}
		}
		try {
			countDownLatch.await();
			//System.out.println("多线程符号化运行完成");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return newImage;
	}
	
	static class threadDrawString implements Runnable{
		protected int x;
		protected int y;
		public threadDrawString(final int x,final int y) {
			this.x = x;
			this.y = y;
		}
		@Override
		public void run() {
		}
	}
}
