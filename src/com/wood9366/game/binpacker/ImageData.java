package com.wood9366.game.binpacker;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

public class ImageData {
	public ImageData(String path) {
		load(path);
	}
	
	public int border() {
		return 1;
	}
	
	public Rect rect() {
		return Rect.Create(0, 0, _availableRect.width() + border() * 2, _availableRect.height() + border() * 2);
	}
	
	public Rect rectAvailable() {
		return _availableRect;
	}
	
	public int width() {
		return _width;
	}
	
	public int height() {
		return _height;
	}
	
	public String name() {
		return _name;
	}
	
	public String imagePath() {
		return _imagePath;
	}
	
	public BufferedImage image() {
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(new File(_imagePath));
			image = image.getSubimage(_availableRect.left(), _availableRect.top(), 
					_availableRect.width(), _availableRect.height());
		} catch (IOException e) {
		}
		
		return image;
	}
	
	private void load(String path) {
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(new File(path));
			_imagePath = path;
			_name = Paths.get(_imagePath).getFileName().toString();
			_width = image.getWidth();
			_height = image.getHeight();
			_availableRect = Rect.Create(0, 0, _width, _height);
			
		} catch (IOException e) {}
		
		if (image != null) {
			trim(image);
		}
	}
	
	private void trim(BufferedImage image) {
		if (image.getRaster().getNumBands() >= 4) {
			int left = image.getWidth();
			
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = 0; x < image.getWidth(); x++) {
					int a = image.getRGB(x, y) & 0xff00000;

					if (a > 0) {
						if (x < left) {
							left = x;
						}
						break;
					}
				}
			}
			
			int right = 0;
			
			for (int y = 0; y < image.getHeight(); y++) {
				for (int x = image.getWidth() - 1; x >= left; x--) {
					int a = image.getRGB(x, y) & 0xff00000;

					if (a > 0) {
						if (x > right) {
							right = x;
						}
						break;
					}
				}
			}
			
			int top = image.getHeight();
			
			for (int x = left; x <= right; x++) {
				for (int y = 0; y < image.getHeight(); y++) {
					int a = image.getRGB(x, y) & 0xff00000;

					if (a > 0) {
						if (y < top) {
							top = y;
						}
						break;
					}
				}
			}
			
			int bottom = 0;
			
			for (int x = left; x <= right; x++) {
				for (int y = image.getHeight() - 1; y >= 0; y--) {
					int a = image.getRGB(x, y) & 0xff00000;

					if (a > 0) {
						if (y > bottom) {
							bottom = y;
						}
						break;
					}
				}
			}
			
			_availableRect = Rect.CreateByEdge(left, top, right, bottom);
		}
	}
	
	private String _name = "";
	private String _imagePath = "";
	private int _width = 0;
	private int _height = 0;
	private Rect _availableRect = null;
}
