package com.wood9366.game.binpacker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Random;

import javax.imageio.ImageIO;

public class SpriteGenerator {
	static public void main(String[] args) {
		String outputPath = "res/sprites/";
		
		int min = 70;
		int max = 250;
		
		int num = 1000;
		Random r = new Random();
		
		while (num-- > 0) {
			BufferedImage image = new BufferedImage(r.nextInt(max - min + 1) + min, r.nextInt(max - min + 1) + min, BufferedImage.TYPE_INT_ARGB);
			
			int c = r.nextInt(4);
			int[] p = new int[] { 255, 255, 255, 255 };
			int[] b = new int[] { 0, 0, 0, 255 };
			
			switch (c) {
			case 0: p = new int[] { 255, 0, 0, 255 }; b = new int[] { 0, 0, 0, 255 }; break;
			case 1: p = new int[] { 0, 255, 0, 255 }; b = new int[] { 0, 0, 0, 255 }; break;
			case 2: p = new int[] { 0, 0, 255, 255 }; b = new int[] { 0, 0, 0, 255 }; break;
			case 3: p = new int[] { 0, 0, 0, 255 }; b = new int[] { 255, 255, 255, 255 }; break;
			default: p = new int[] { 255, 255, 255, 255 }; b = new int[] { 0, 0, 0, 255 }; break;
			}
			
			for (int i = 0; i < image.getWidth(); i++) {
				for (int j = 0; j < image.getHeight(); j++) {
					if (i <= 4 || i >= image.getWidth() - 5 || j <= 4 || j >= image.getHeight() - 5) {
						image.getRaster().setPixel(i, j, b);
					} else {
						image.getRaster().setPixel(i, j, p);
					}
				}
			}
			
			String path = Paths.get(outputPath, Integer.toString(num) + ".png").toString();
			
			try {
				ImageIO.write(image, "png", new File(path));
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
	}
}
