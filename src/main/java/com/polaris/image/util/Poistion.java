package com.polaris.image.util;

import org.bytedeco.javacpp.opencv_core.Point;

public class Poistion {
	private Point start;
	private Point end;
	
	
	public Point getStart() {
		return start;
	}


	public void setStart(Point start) {
		this.start = start;
	}


	public Point getEnd() {
		return end;
	}


	public void setEnd(Point end) {
		this.end = end;
	}


	public Poistion(Point start, Point end) {
		this.start = start;
		this.end = end;
	}
}
