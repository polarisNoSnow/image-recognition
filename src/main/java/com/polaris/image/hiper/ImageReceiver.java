package com.polaris.image.hiper;

import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameUtils;

import com.polaris.image.util.CommonUtil;
import com.polaris.image.util.GeneralContants;
import com.polaris.image.util.ImageUtil;

/**
 * 
 * @author 北辰不落雪
 * @date 2019年11月22日 上午11:35:23
 * @Description 多线程视频符号化
 */
public class ImageReceiver {
	//视频帧数
	private int frameNum;
	//视频帧计数器
	private AtomicInteger atomicInteger = new AtomicInteger(1);
	//多线程计数器
	private CountDownLatch latch;
	//录制器
	private FFmpegFrameRecorder recorder;
	
	private int getPercent() {
		// 划一百等分
		return frameNum / 100;
	}
	/**
	 * 按帧录制视频
	 * 
	 * @param inputFile    该地址可以是网络直播/录播地址，也可以是远程/本地文件路径
	 * @param outputFile   该地址只能是文件地址，如果使用该方法推送流媒体服务器会报错，原因是没有设置编码格式
	 * @param audioChannel 是否录制音频（0:不录制/1:录制）
	 * @throws FrameGrabber.Exception
	 * @throws FrameRecorder.Exception
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	public void frameRecord(String inputFile, String outputFile, int audioChannel)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		boolean isStart = true;// 该变量建议设置为全局控制变量，用于控制录制结束
		// 获取视频源
		FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
		// 默认UDP 会丢包
		grabber.setOption("rtsp_transport", "tcp");
		grabber.start();
		// grabber.setFrameRate(30);
		// grabber.setVideoBitrate(3000000);
		// 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
		recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(),
				grabber.getImageHeight(), audioChannel);
		// 帧数和码率 两边设为相同，否则会出现视频帧加速的情况
		// FPS(frames per second)
		recorder.setFrameRate(grabber.getFrameRate());
		// 码率 3000kb/s
		recorder.setVideoBitrate(grabber.getVideoBitrate());

		// 开始取视频源
		recordByFrame(grabber, recorder, isStart);
	}

	/**
	 * 多线程考虑：
	 * 1.录制和符号化比较耗时
	 * 2.录制器record需要按顺序
	 * 目前方案：
	 * 1.视频帧符号化用多线程跑
	 * 2.按顺序录制音视频帧
	 * 
	 * @param grabber
	 * @param recorder
	 * @param status
	 * @throws Exception
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	private void recordByFrame(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder, Boolean status)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		try {
			ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS,
					new LinkedBlockingQueue<Runnable>());
			// 建议在线程中使用该方法
			recorder.start();
			// 总帧数（视频帧）
			frameNum = grabber.getLengthInFrames();
			// 总时长(秒)
			double time = (double) grabber.getLengthInTime() / 1000 / 1000;
			System.out.printf("视频总时长：%f秒，总帧数：%d，视频开始处理.....\n", time, frameNum);
			long startTime = System.currentTimeMillis();
			Frame frame;
			latch = new CountDownLatch(frameNum);
			while (status && (frame = grabber.grab()) != null) {
				final Frame frame_ = frame;
				int i = 0;
				BufferedImage bufferedImage = Java2DFrameUtils.toBufferedImage(frame_);
				// 音频帧直接填充
				if (bufferedImage == null) {
					recorder.record(frame_);
				}else {
					/**
					 * 	存在的问题：
					 * 1.符号化时新建的BufferedImage采用RGB方式时候alpha有问题，会被当成视频帧的前景色（猜测是24位中的高8位），目前采用灰度化解决
					 */
					BufferedImage newImage = ImageUtil.symbolization(bufferedImage);
					recorder.record(Java2DFrameUtils.toFrame(newImage));
					if ((i = atomicInteger.getAndAdd(1)) % getPercent() == 0) {
						System.out.printf("已处理：%.2f%%，剩余：%d帧\n", (double) i / frameNum * 100, frameNum - i);
					}
				}
				
				
				executor.execute(new ThreadRecorder(frame_));
			}
			try {
                latch.await();
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
			System.out.printf("处理完毕，耗时：%.2f秒\n", (double) (System.currentTimeMillis() - startTime) / 1000);
			recorder.stop();
			grabber.stop();
		} finally {
			if (grabber != null) {
				grabber.stop();
			}
		}
	}
	
	class ThreadRecorder implements Runnable{
		private Frame frame_;
		
		public ThreadRecorder(Frame frame_) {
			this.frame_ = frame_;
		}
		
		@Override
		public void run() {
			try {
				
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				latch.countDown();
			}
		}
	}

	public static void main(String[] args)throws FrameRecorder.Exception, FrameGrabber.Exception, InterruptedException {
		String inputFile = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
		inputFile = GeneralContants.DESTOP_PATH + "1.mp4";
		String outputFile = CommonUtil.getPrefix(inputFile) + "_符号化." + CommonUtil.getSuffix(inputFile);
		try {
			ImageReceiver imageReceiver = new ImageReceiver();
			imageReceiver.frameRecord(inputFile, outputFile, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
