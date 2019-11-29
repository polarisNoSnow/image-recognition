package com.polaris.image.core;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.Scalar;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import com.polaris.image.util.GeneralContants;

/**
 * 参考：https://blog.csdn.net/eguid_1/article/details/53259649
 *
 * @author 北辰不落雪
 * @date 2019年2月15日 下午3:08:50
 * @Description 对视频每帧做处理，如添加水印等
 */
public class ImageWatermark {

    public static void main(String[] args) {
        try {
            addWateramrk();
        } catch (Exception | InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void addWateramrk() throws Exception, InterruptedException {
        // 转换器，用于Frame/Mat/IplImage相互转换
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        // 使用OpenCV抓取本机摄像头，摄像头设备号默认0
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        // 开启抓取器
        grabber.start();
        CanvasFrame cFrame = new CanvasFrame("go polaris",
                CanvasFrame.getDefaultGamma() / grabber.getGamma());
        cFrame.setAlwaysOnTop(true);
        cFrame.setVisible(true);
        // 水印文字位置
        Point point = new Point(5, 20);
        // 颜色，使用黄色
        Scalar scalar = new Scalar(0, 255, 255, 0);
        Frame frame = null;
        int index = 0;

        Mat logo = opencv_imgcodecs.imread(GeneralContants.DESTOP_PATH + "wm.png");
        Mat mask = opencv_imgcodecs.imread(GeneralContants.DESTOP_PATH + "wm2.png", 0);

        opencv_imgproc.threshold(mask, mask, 254, 255,
                opencv_imgcodecs.IMWRITE_PNG_BILEVEL);

        double alpha = 0.8;// 图像透明权重值,0-1之间
        while (cFrame.isShowing()) {
            try {
                frame = grabber.grabFrame();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (frame != null) {
                // 取一帧视频（图像），并转换为Mat
                Mat mat = converter.convertToMat(frame);
                // 加文字水印，opencv_imgproc.putText（图片，水印文字，文字位置，字体，字体大小，字体颜色，字体粗度，平滑字体，是否翻转文字）
                opencv_imgproc.putText(mat, "beta watermark by polairs", point,
                        opencv_imgproc.CV_FONT_VECTOR0, 0.8, scalar, 1, 20,
                        false);
                // 定义感兴趣区域(位置，logo图像大小)
                Mat ROI = mat
                        .apply(new Rect(grabber.getImageWidth() - logo.cols() - 5, grabber.getImageHeight() - logo.rows() - 5, logo.cols(), logo.rows()));

                opencv_core
                        .addWeighted(ROI, alpha, logo, 1.0 - alpha, 0.0, ROI);
                // 把logo图像复制到感兴趣区域
                // logo.copyTo(ROI, mask);
                // 显示图像到窗口
                cFrame.showImage(converter.convert(mat));
                if (index == 0) {
                    index++;
                    // 保存第一帧图片到本地
                    opencv_imgcodecs.imwrite(GeneralContants.DESTOP_PATH + "watermark.jpg", mat);
                }
                // 释放Mat资源
                ROI.release();
                ROI.close();
                mat.release();
                //mat.close(); //原作者此处未注释，第二次取frame的时候 会报错（）
                Thread.sleep(40);
            }

        }
        // 关闭窗口
        cFrame.dispose();
        // 停止抓取器
        grabber.stop();
        // 释放资源
        logo.release();
        logo.close();
        mask.release();
        mask.close();
        scalar.close();
        point.close();
    }
}
