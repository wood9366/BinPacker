package com.wood9366.game.binpacker;

import java.awt.image.BufferedImage;

public interface ISpriteData {
	public int border();
	public Rect rect();
	public Rect rectContent();
	public int width();
	public int height();
	public String name();
	public BufferedImage image();
	public String info();
}
