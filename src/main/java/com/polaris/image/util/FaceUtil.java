package com.polaris.image.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class FaceUtil {
	public static Mat face(Mat image) {
		Mat dst = new Mat();
 
		// int value1 = 3, value2 = 1; 磨皮程度与细节程度的确定
		int value1 = 3, value2 = 1; 
		int dx = value1 * 5; // 双边滤波参数之一
		double fc = value1 * 12.5; // 双边滤波参数之一
		double p = 0.1f; // 透明度
		Mat temp1 = new Mat(), temp2 = new Mat(), temp3 = new Mat(), temp4 = new Mat();
 
		// 双边滤波
		Imgproc.bilateralFilter(image, temp1, dx, fc, fc);
 
		// temp2 = (temp1 - image + 128);
		Mat temp22 = new Mat();
		Core.subtract(temp1, image, temp22);
		// Core.subtract(temp22, new Scalar(128), temp2);
		Core.add(temp22, new Scalar(128, 128, 128, 128), temp2);
		// 高斯模糊
		Imgproc.GaussianBlur(temp2, temp3, new Size(2 * value2 - 1, 2 * value2 - 1), 0, 0);
 
		// temp4 = image + 2 * temp3 - 255;
		Mat temp44 = new Mat();
		temp3.convertTo(temp44, temp3.type(), 2, -255);
		Core.add(image, temp44, temp4);
		// dst = (image*(100 - p) + temp4*p) / 100;
		Core.addWeighted(image, p, temp4, 1 - p, 0.0, dst);
		
		Core.add(dst, new Scalar(10, 10, 10), dst);
		return dst;
	}
	public static void main(String[] args) throws IOException {
		BufferedImage image = ImageIO.read(new File("F:\\images\\people.jpg"));
		Mat src2 = Imgcodecs.imread("F:\\images\\people.jpg");
        Mat src3 = face(src2);
      
        Mat dest = new Mat(new Size(src2.cols()+src3.cols(), src2.rows()), src2.type());
        Mat temp1 = dest.colRange(0, src2.cols());
        Mat temp2 = dest.colRange(src2.cols(), dest.cols());
        src2.copyTo(temp1);
        src3.copyTo(temp2);
        Imgcodecs.imwrite("F:\\images\\people_美颜.jpg",dest);	
	}
}
