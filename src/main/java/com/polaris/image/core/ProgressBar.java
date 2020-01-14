package com.polaris.image.core;

public abstract class ProgressBar {
	/*
	 * 进度
	 */
	double progress = 0;
	/*
	 * 获取当前进度（整型）
	 */
	abstract  int getProgress();
}
