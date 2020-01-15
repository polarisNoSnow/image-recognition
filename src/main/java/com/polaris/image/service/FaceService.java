package com.polaris.image.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacv.Java2DFrameUtils;

import com.polaris.image.util.CommonUtil;
import com.polaris.image.util.ImageUtil;
import com.polaris.image.util.PropertiesUtil;

public class FaceService extends ProgressBar{
	
	/**
	 * 人脸检测
	 * @param bufferImage
	 * @return
	 */
	public BufferedImage detectFace(BufferedImage bufferImage) {
		progress=0;
		BufferedImage grayBufferImage = ImageUtil.changeImage(bufferImage, BufferedImage.TYPE_BYTE_GRAY);
		Graphics graphics = bufferImage.createGraphics();
		graphics.setColor(Color.red); // 设置前景色
		graphics.setFont(new Font("微软雅黑", Font.PLAIN, 3)); // 设置字体
		// 灰度化
		Mat grayscr = Java2DFrameUtils.toMat(grayBufferImage);
		// 均衡化直方图(提高对比度)
		//equalizeHist(grayscr, grayscr);
		RectVector faces = new RectVector();
		CascadeClassifier cascade = new CascadeClassifier(PropertiesUtil.getInstance().getStringValue("face"));//初始化人脸检测器
		//检测人脸，grayscr为要检测的图片，faces用来存放检测结果
		cascade.detectMultiScale(grayscr, faces);
		progress=25;
		//遍历检测出来的人脸
		for (int i = 0; i < faces.size(); i++) { 
			Rect rect = faces.get(i);
			//左上角
			Point leftPoint = rect.tl(); 
			Point rightPoint = rect.br();
			//在原图上画出人脸的区域
			for (int j = leftPoint.x(); j < rightPoint.x(); j++) {
				graphics.drawString("·", j, rightPoint.y());
				graphics.drawString("·", j, leftPoint.y());
			}
			for (int j = leftPoint.y(); j < rightPoint.y(); j++) {
				graphics.drawString("·", leftPoint.x(), j);
				graphics.drawString("·", rightPoint.x(), j);
			}
		}
		progress=80;
		return bufferImage;
	}

	/**
	 * 人脸检测
	 * @param inputFile 待检测图片路径
	 * @param outputFile 结果图片保存路径
	 * @throws IOException
	 */
	public void detectFace(String inputFile, String outputFile) throws IOException{
		System.out.println("检测开始");
		File file = new File(inputFile);
		BufferedImage image = ImageIO.read(file);
		image = detectFace(image);
		ImageIO.write(image, CommonUtil.getSuffix(inputFile),new FileOutputStream(outputFile));
		progress=100;
		System.out.println("检测完毕");
	}

}
