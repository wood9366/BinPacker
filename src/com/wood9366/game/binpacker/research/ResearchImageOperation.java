package com.wood9366.game.binpacker.research;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ResearchImageOperation {
	public static void main(String[] args) {
		BufferedImage pic1 = null;
		BufferedImage pic2 = null;
		
		try {
			pic1 = ImageIO.read(new File("res/1.png"));
			pic2 = ImageIO.read(new File("res/2.png"));
		} catch (IOException e) {
			
		}
		
		BufferedImage pic = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		
		for (int i = 0; i < pic.getWidth(); i++) {
			for (int j = 0; j < pic.getHeight(); j++) {
				pic.getRaster().setPixel(i, j, new int[] { 128,128,128,128 });
			}
		}
		
		copyRect(pic, 0, 0, pic1, 0, 0, pic1.getWidth(), pic1.getHeight());
		copyRect(pic, 40, 0, pic2, 0, 0, pic2.getWidth(), pic2.getHeight());
		
		copyRect(pic, 0, 40, pic1, 0, 0, 10, 10);
		copyRect(pic, 40, 40, pic1, 10, 10, 10, 10);
		
		try {
			ImageIO.write(pic, "png", new File("res/o.png"));
		} catch (IOException e) {
			
		}
	}
	
	static public void copyRect(BufferedImage dest, int dx, int dy, BufferedImage src, int sx, int sy, int w, int h) {
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int numBands = Math.min(dest.getRaster().getNumBands(), src.getRaster().getNumBands());
				
				for (int b = 0; b < numBands; b++) {
					dest.getRaster().setSample(dx + x, dy + y, b, src.getRaster().getSample(sx + x, sy + y, b));
				}
			}
		}
	}
}
