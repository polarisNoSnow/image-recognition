package com.polaris.image.service;

import java.text.DecimalFormat;

public abstract class ProgressBar {
	/*
	 * 进度
	 */
	protected double progress = 0;
	/*
	 * 获取当前进度（整型）
	 */
	public int getProgress() {
        return  Double.valueOf(new DecimalFormat("0").format(this.progress)).intValue();
	}
}
