package com.polaris.image.core;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

public class PhotoPrint {
	public static void main(String[] args) {
		try {
			Font font = new Font("黑体", Font.PLAIN, 30);
			AffineTransform at = new AffineTransform();
			FontRenderContext frc = new FontRenderContext(at, true, true);
			GlyphVector gv = font.createGlyphVector(frc, "JinCeon"); // 要显示的文字
			Shape shape = gv.getOutline(5, 30);
			int weith = 200;
			int height = 40;
			boolean[][] view = new boolean[weith][height];
			for (int i = 0; i < weith; i++) {
				for (int j = 0; j < height; j++) {
					if (shape.contains(i, j)) {
						view[i][j] = true;
					} else {
						view[i][j] = false;
					}
				}
			}
			for (int j = 0; j < height; j++) {
				for (int i = 0; i < weith; i++) {
					if (view[i][j]) {
						System.out.print("@");// 替换成你喜欢的图案
					} else {
						System.out.print(" ");
					}
				}
				System.out.println();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}