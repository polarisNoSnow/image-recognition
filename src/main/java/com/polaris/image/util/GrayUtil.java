package com.polaris.image.util;

/**
 * 灰度值计算
 * 算法参考：https://www.cnblogs.com/zhangjiansheng/p/6925722.html
 *
 * @author polaris
 * @date 2019年11月13日
 */
public class GrayUtil {

    /**
     * 彩色转灰度，著名的心理学公式
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static float publicMothod(int r, int g, int b) {
        return 0.299f * r + 0.578f * g + 0.114f * b;
    }

    /**
     * 16位长度灰度化
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static float gray16(int r, int g, int b) {
        return (r * 19595 + g * 38469 + b * 7472) >> 16;
    }

    /**
     * 低于16位的灰度化
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static float gray7(int r, int g, int b) {
        return (r * 38 + g * 75 + b * 15) >> 7;
    }

    /**
     * Adobe Photoshop的灰度化方式
     * 效果很好，但是计算速度慢
     *
     * @param r
     * @param g
     * @param b
     * @return
     */
    public static float grayPS(double r, double g, double b) {
        return (float) Math.pow(Math.pow(r, 2.2) * 0.2973 + Math.pow(g, 2.2) * 0.6274 + Math.pow(b, 2.2) * 0.0753, 1 / 2.2);
    }

}
