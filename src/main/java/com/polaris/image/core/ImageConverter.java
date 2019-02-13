package com.polaris.image.core;

import javax.swing.JFrame;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.avcodec;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameConverter;

import com.polaris.image.util.GeneralContants;

/**
 * @author 北辰不落雪 
 * @date 2019年2月12日 下午2:45:12 
 * @Description 转流器
 */
public class ImageConverter {
	
	/**
	 * 转流器
	 * @param inputFile
	 * @param outputFile
	 * @throws Exception
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 * @throws InterruptedException
	 */
	public static void recordPush(String inputFile,String outputFile,int v_rs) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception, InterruptedException{
		Loader.load(opencv_objdetect.class);
		long startTime=0;
		FrameGrabber grabber =FFmpegFrameGrabber.createDefault(inputFile);
		//grabber.setOption("rtsp_transport", "tcp");
		try {
			grabber.start();
		} catch (Exception e) {
			try {
				grabber.restart();
			} catch (Exception e1) {
				throw e;
			}
		}
		OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
		Frame grabframe =grabber.grab();
		IplImage grabbedImage =null;
		if(grabframe!=null){
			System.out.println("取到第一帧");
			grabbedImage = converter.convert(grabframe);
		}else{
			System.out.println("没有取到第一帧");
		}
		//如果想要保存图片,可以使用 
		//opencv_imgcodecs.cvSaveImage("C:\\Users\\tyb\\Desktop\\hello.jpg", grabbedImage); //来保存图片
		FrameRecorder recorder;
		try {
			recorder = FrameRecorder.createDefault(outputFile, 1280, 720);
		} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
			throw e;
		}
		recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264); // avcodec.AV_CODEC_ID_H264
		recorder.setFormat("flv");
		recorder.setFrameRate(v_rs);
		recorder.setGopSize(v_rs);
		System.out.println("准备开始推流...");
		try {
			recorder.start();
		} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
			try {
				System.out.println("录制器启动失败，正在重新启动...");
				if(recorder!=null)
				{
					System.out.println("尝试关闭录制器");
					recorder.stop();
					System.out.println("尝试重新开启录制器");
					recorder.start();
				}
				
			} catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
				throw e;
			}
		}
		System.out.println("开始推流");
		CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setAlwaysOnTop(true);
		while (frame.isVisible() && (grabframe=grabber.grab()) != null) {
			System.out.println("推流...");
			frame.showImage(grabframe);
			grabbedImage = converter.convert(grabframe);
			Frame rotatedFrame = converter.convert(grabbedImage);
			
			if (startTime == 0) {
				startTime = System.currentTimeMillis();
			}
			recorder.setTimestamp(1000 * (System.currentTimeMillis() - startTime));//时间戳
			if(rotatedFrame!=null){
			recorder.record(rotatedFrame);
			}
			
			Thread.sleep(10);
		}
		frame.dispose();
		recorder.stop();
		recorder.release();
		grabber.stop();
		System.exit(-1);
	}
	public static void main(String[] args)
			throws FrameRecorder.Exception, FrameGrabber.Exception, InterruptedException {
		String inputFile = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
		String outputFile = GeneralContants.desktop_path + "ImageConverter.mp4";
		try {
			recordPush(inputFile, outputFile,60);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
