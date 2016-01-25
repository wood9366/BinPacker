package com.wood9366.game.binpacker;

import java.awt.image.BufferedImage;

public class Utils {
	static public void copyImageRect(BufferedImage dest, int dx, int dy, BufferedImage src) {
		copyImageRect(dest, dx, dy, src, 0, 0, src.getWidth(), src.getHeight());
	}
	
	static public void copyImageRect(BufferedImage dest, int dx, int dy, BufferedImage src, int sx, int sy) {
		copyImageRect(dest, dx, dy, src, sx, sy, src.getWidth(), src.getHeight());
	}
	
	static public void copyImageRect(BufferedImage dest, int dx, int dy, BufferedImage src, int sx, int sy, int w, int h) {
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				dest.setRGB(dx + x, dy + y, src.getRGB(sx + x, sy + y));
			}
		}
	}
}
