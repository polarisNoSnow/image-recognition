package com.polaris.image.core;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Java2DFrameUtils;

import com.polaris.image.util.CommonUtil;
import com.polaris.image.util.GeneralContants;
import com.polaris.image.util.ImageUtil;

/**
 * 参考：https://blog.csdn.net/eguid_1/article/details/52680802
 * 
 * @author 北辰不落雪
 * @date 2019年2月12日 上午11:35:23
 * @Description 收流器
 */
public class ImageReceiver {
	/**
	 * 按帧录制视频
	 * 
	 * @param inputFile
	 *            该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
	 * @param outputFile
	 *            该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
	 * @param audioChannel
	 *            是否录制音频（0:不录制/1:录制）
	 * @throws FrameGrabber.Exception
	 * @throws FrameRecorder.Exception
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	public static void frameRecord(String inputFile, String outputFile, int audioChannel)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		boolean isStart = true;// 该变量建议设置为全局控制变量，用于控制录制结束
		// 获取视频源
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
		// 默认UDP 会丢包
		grabber.setOption("rtsp_transport", "tcp"); 
		grabber.start();
		//grabber.setFrameRate(30);
		//grabber.setVideoBitrate(3000000);
		// 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(),
				grabber.getImageHeight(), audioChannel);
		//帧数和码率 两边设为相同，否则会出现视频帧加速的情况
		//FPS(frames per second)
		recorder.setFrameRate(grabber.getFrameRate());
		//码率 3000kb/s
		recorder.setVideoBitrate(grabber.getVideoBitrate());
		
		// 开始取视频源
		recordByFrame(grabber, recorder, isStart);
	}

	private static void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		try {
			// 建议在线程中使用该方法
			recorder.start();
			Frame frame = null;
			//总帧数（视频帧）
			int frameNum = grabber.getLengthInFrames();
			//总时长(秒)
			double time = (double)grabber.getLengthInTime()/1000/1000;
			System.out.printf("视频总时长：%f秒，总帧数：%d，视频开始处理.....\n",time,frameNum);
			int i = 1;
			//划十等分
			int y = frameNum/100;
			long startTime = System.currentTimeMillis();
			while (status && (frame = grabber.grab()) != null) {
				BufferedImage bufferedImage = Java2DFrameUtils.toBufferedImage(frame);
				// 音频帧直接填充
				if (bufferedImage == null) {
					recorder.record(frame);
					continue;
				}
				/**
				 * 存在的问题：
				 * 1.符号化时新建的BufferedImage采用RGB方式时候alpha有问题，会被当成视频帧的前景色（猜测是24位中的高8位），目前采用灰度化解决
				 */
				BufferedImage newImage = ImageUtil.hiperSymbolization(bufferedImage);
				//recorder.record(Java2DFrameUtils.toFrame(newImage));
				if(i%y==0) {
					System.out.printf("已处理：%.2f%%，剩余：%d帧\n",(double)i/frameNum*100,frameNum-i);
					//ImageIO.write(newImage, "png", new FileOutputStream(GeneralContants.DESTOP_PATH + "\\video\\"+i+".png"));
				}
				i++;
			}
			System.out.printf("处理完毕，耗时：%.2f秒\n",(double)(System.currentTimeMillis()-startTime)/1000);
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
		inputFile = GeneralContants.DESTOP_PATH + "1.mp4";
		String outputFile = CommonUtil.getPrefix(inputFile)+ "_符号化."+CommonUtil.getSuffix(inputFile);
		try {
			frameRecord(inputFile, outputFile, 1);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
