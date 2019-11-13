package com.polaris.image.core;

import java.awt.Font;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

/**
 * 将文字以符号化形式显示
 * @author polaris
 * @date 2019年11月13日
 */
public class WordsPrint {
	
	public static void main(String[] args) {
		WordsPrint.wordsToSymbol("polaris");
	}
	
	/**
	 * 以符号化形式打印文字
	 * @param txt 要显示的文字
	 */
	public static void wordsToSymbol(String txt) {
		try {
			Font font = new Font("黑体", Font.PLAIN, 24);
			AffineTransform at = new AffineTransform();
			//缩放
			at.setToScale(0.1, 0.1); 
			FontRenderContext frc = new FontRenderContext(at, true, true);
			GlyphVector gv = font.createGlyphVector(frc, txt); // 要显示的文字
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
						System.out.print(".");// 替换成你喜欢的图案
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