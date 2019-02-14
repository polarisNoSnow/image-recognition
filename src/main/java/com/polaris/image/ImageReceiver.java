package com.polaris.image;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

/**
 * 参考：https://blog.csdn.net/eguid_1/article/details/52680802
 * @author 北辰不落雪 
 * @date 2019年2月12日 上午11:35:23 
 * @Description 收流器
 */
public class ImageReceiver {
	/**
	 * 按帧录制视频
	 * 
	 * @param inputFile-该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
	 * @param outputFile
	 *            -该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
	 * @throws FrameGrabber.Exception
	 * @throws FrameRecorder.Exception
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	public static void frameRecord(String inputFile, String outputFile, int audioChannel)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		
	        boolean isStart=true;//该变量建议设置为全局控制变量，用于控制录制结束
		// 获取视频源
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
		grabber.setOption("rtsp_transport", "tcp"); //默认UDP 会丢包
		// 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, 1280, 720, audioChannel);
		// 开始取视频源
		recordByFrame(grabber, recorder, isStart);
	}
	
	private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		try {//建议在线程中使用该方法
			grabber.start();
			recorder.start();
			Frame frame = null;
			while (status&& (frame = grabber.grabFrame()) != null) {
				recorder.record(frame);
			}
			recorder.stop();
			grabber.stop();
		} finally {
			if (grabber != null) {
				grabber.stop();
			}
		}
	}
	public static void main(String[] args)
			throws FrameRecorder.Exception, FrameGrabber.Exception, InterruptedException {
 
		 String inputFile = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
		 // Decodes-encodes
		 String outputFile = "C:\\Users\\tyb\\Desktop\\recorde.mp4";
		 try {
			frameRecord(inputFile, outputFile,1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}