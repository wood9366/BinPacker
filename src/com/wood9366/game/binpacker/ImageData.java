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
	
	public Rect rect() {
		return Rect.Create(0, 0, _image.getWidth(), _image.getHeight());
	}
	
	public String name() {
		return _name;
	}
	
	public String imagePath() {
		return _imagePath;
	}
	
	public BufferedImage image() {
		return _image;
	}
	
	private void load(String path) {
		try {
			_image = ImageIO.read(new File(path));
			_imagePath = path;
			_name = Paths.get(_imagePath).getFileName().toString();
		} catch (IOException e) {}
	}
	
	private String _name = "";
	private String _imagePath = "";
	private BufferedImage _image = null;
}
