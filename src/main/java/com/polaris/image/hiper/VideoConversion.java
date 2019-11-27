package com.polaris.image.hiper;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
public class VideoConversion {
	private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 10, 10, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>());
	// 文件输出地址
	private String outputFile;
	// 是否录制音频（0:不录制/1:录制）
	private int audioChannel = 1;
	// 当前帧计数器
	private int currFrame = 0;
	// 生产者完成标记
	private Boolean endFlag = false;
	// 视频帧数
	private int videoFrameNum;
	// 视频帧计数器
	private AtomicInteger videoInteger = new AtomicInteger(1);
	// 多线程计数器
	private static CountDownLatch latch = new CountDownLatch(1);
	// 录制器
	private FFmpegFrameRecorder recorder;
	private FFmpegFrameGrabber grabber;

	private int getPercent() {
		// 划一百等分
		return videoFrameNum / 100;
	}

	// 消费队列
	private Map<Integer, BufferedImage> mq = new ConcurrentHashMap<Integer, BufferedImage>();

	/**
	 * 生产者
	 * 注意：直接存入frame是有问题的，特别是Java2DFrameUtils读取之后，流会丢失
	 * @param key
	 * @param data
	 */
	public void consumer(Integer key, BufferedImage data) {
		mq.put(key, data);
	}

	/**
	 * 消费者
	 * 
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 * @throws org.bytedeco.javacv.FrameGrabber.Exception
	 * @throws InterruptedException
	 */
	public void producer() throws org.bytedeco.javacv.FrameRecorder.Exception,
			org.bytedeco.javacv.FrameGrabber.Exception, InterruptedException {
		// Thread.sleep(8000);
		long startTime = System.currentTimeMillis();
		int i = 0;
		BufferedImage bufferedImage;
		while (!endFlag || i < mq.size()) {
			if (i % 100 == 0) {
				System.out.printf("消费者已消费%d帧\n", i);
			}
			for (;;) {
				if ((bufferedImage=mq.get(i)) != null) {
					try {
						/**
						 * 存在的问题： 1.新建的BufferedImage采用RGB方式时候alpha有问题，会被当成视频帧的前景色（猜测是24位中的高8位），目前采用灰度化解决
						 */
						BufferedImage newImage = ImageUtil.hiperSymbolization(bufferedImage);
						recorder.record(Java2DFrameUtils.toFrame(newImage));
					} catch (Exception e) {
						System.out.println("---------出错帧：" + i);
						e.printStackTrace();
					}
					break;
				}
			}
			i++;
		}
		mq = null;
		latch.countDown();
		System.out.printf("%d帧消费完毕，耗时：%.2f秒\n", i, (double) (System.currentTimeMillis() - startTime) / 1000);
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
	public void frameRecord(String inputFile) throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		boolean isStart = true;// 该变量建议设置为全局控制变量，用于控制录制结束
		// 获取视频源
		grabber = new FFmpegFrameGrabber(inputFile);
		// 默认UDP 会丢包
		grabber.setOption("rtsp_transport", "tcp");
		grabber.start();

		// 流媒体输出地址，分辨率（长，高），是否录制音频（0:不录制/1:录制）
		recorder = new FFmpegFrameRecorder(outputFile, grabber.getImageWidth(), grabber.getImageHeight(), audioChannel);
		// 帧数和码率 两边设为相同，否则会出现视频帧加速的情况
		// FPS(frames per second)
		recorder.setFrameRate(grabber.getFrameRate());
		// 码率
		recorder.setVideoBitrate(grabber.getVideoBitrate());
		recorder.start();
		// grabber.setFrameRate(30);
		// grabber.setVideoBitrate(3000000);

		// 开始取视频源
		recordByFrame(grabber, isStart);
	}

	/**
	 * 多线程考虑： 1.录制和符号化比较耗时 2.录制器record需要按顺序 目前方案： 1.视频帧符号化用多线程跑（完成）
	 * 2.音视频帧采用多线程生产按顺序消费的方式（可能存在卡帧的情况）
	 * 
	 * @param grabber
	 * @param recorder
	 * @param status
	 * @throws Exception
	 * @throws org.bytedeco.javacv.FrameRecorder.Exception
	 */
	private void recordByFrame(FFmpegFrameGrabber grabber, Boolean status)
			throws Exception, org.bytedeco.javacv.FrameRecorder.Exception {
		// 视频帧
		videoFrameNum = grabber.getLengthInFrames();
		// 总时长(秒)
		double time = (double) grabber.getLengthInTime() / 1000 / 1000;
		System.out.printf("视频总时长：%f秒，总视频帧数：%d，视频开始处理.....\n", time, videoFrameNum);
		long startTime = System.currentTimeMillis();
		Frame frame;
		int i = 0;
		while (status && (frame = grabber.grab()) != null) {
			BufferedImage bufferedImage = Java2DFrameUtils.toBufferedImage(frame);
			// 音频帧直接填充
			if (bufferedImage == null) {
				recorder.record(frame);
			} else {
				consumer(currFrame, bufferedImage);
				if ((i = videoInteger.getAndAdd(1)) % getPercent() == 0) {
					System.out.printf("生产者：%.2f%%，剩余视频帧：%d帧\n", (double) i / videoFrameNum * 100, videoFrameNum - i);
				}
				currFrame++;
			}
			
		}
		endFlag = true;
		System.out.printf("生产完毕，耗时：%.2f秒\n", (double) (System.currentTimeMillis() - startTime) / 1000);
	}

	public static void main(String[] args)
			throws FrameRecorder.Exception, FrameGrabber.Exception, InterruptedException {
		long startTime = System.currentTimeMillis();
		// final String inputFile =
		// "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
		final String inputFile = GeneralContants.DESTOP_PATH + "1.mp4";

		try {
			VideoConversion videoConversion = new VideoConversion();
			videoConversion.outputFile = CommonUtil.getPrefix(inputFile) + "_符号化." + CommonUtil.getSuffix(inputFile);

			// 线程1：生产音视频帧
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						videoConversion.frameRecord(inputFile);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			// 线程1：消费音视频帧
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						videoConversion.producer();
					} catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
						e.printStackTrace();
					} catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			latch.await();
			videoConversion.recorder.stop();
			videoConversion.grabber.stop();
			System.out.printf("总耗时：%.2f秒\n", (double) (System.currentTimeMillis() - startTime) / 1000);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
