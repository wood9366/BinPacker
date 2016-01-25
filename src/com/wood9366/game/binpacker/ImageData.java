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
		return Rect.Create(0, 0, _rectContent.width() + border() * 2, _rectContent.height() + border() * 2);
	}
	
	public Rect rectContent() {
		return _rectContent;
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
	
	public String path() {
		return _path;
	}
	
	public BufferedImage image() {
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(new File(_path));
			image = image.getSubimage(_rectContent.left(), _rectContent.top(), 
					_rectContent.width(), _rectContent.height());
		} catch (IOException e) {
		}
		
		return image;
	}
	
	private void load(String path) {
		Profiler.Instance().begin("load image " + path);
		BufferedImage image = null;
		
		try {
			image = ImageIO.read(new File(path));
			_path = path;
			_name = Paths.get(_path).getFileName().toString();
			_width = image.getWidth();
			_height = image.getHeight();
			_rectContent = Rect.Create(0, 0, _width, _height);
			
		} catch (IOException e) {}
		Profiler.Instance().end("load image " + path);
		
		Profiler.Instance().begin("trim image " + path);
		if (image != null) {
			trim(image);
		}
		Profiler.Instance().end("trim image " + path);
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
			
			_rectContent = Rect.CreateByEdge(left, top, right, bottom);
		}
	}
	
	private String _name = "";
	private String _path = "";
	private int _width = 0;
	private int _height = 0;
	private Rect _rectContent = null;
}
