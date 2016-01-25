package com.wood9366.game.binpacker;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;

import org.json.JSONObject;

public class BinData {
	class PackImageData {
		public PackImageData(ImageData image, Rect rect) {
			_image = image;
			_rect = rect;
		}
		
		public ImageData image() {
			return _image;
		}
		
		public Rect rect() {
			return _rect;
		}
		
		private ImageData _image = null;
		private Rect _rect = null;
	}
	
	public BinData(int no, int width, int height) {
		_no = no;
		_width = width;
		_height = height;
		
		_maxrects.clear();
		_maxrects.add(Rect.Create(0, 0, width(), height()));
	}
	
	public int no() { return _no; }
	public int width() { return _width; }
	public int height() { return _height; }

	public List<Rect> maxRects() {
		return _maxrects;
	}
	
	public boolean pack(Rect maxrect, ImageData image) {
		boolean isPack = false;
		
		if (_maxrects.contains(maxrect)) {
			Rect packedRect = packInMaxRect(maxrect, image.rect());
			
			if (packedRect != null) {
				isPack = true;
				calcualteMaxRectsAfterPack(packedRect);
				_packedImages.add(new PackImageData(image, packedRect));
			}
		} else {
			System.out.println("incorrect maxrect");
		}
		
		return isPack;
	}
	
	public void export(String outputPath) {
		Profiler.Instance().begin("export bin " + Integer.toString(no()) + " image");
		exportImage(outputPath);
		Profiler.Instance().end("export bin " + Integer.toString(no()) + " image");
		Profiler.Instance().begin("export bin " + Integer.toString(no()) + " config");
		exportConfig(outputPath);
		Profiler.Instance().end("export bin " + Integer.toString(no()) + " config");
	}
	
	@SuppressWarnings("unused")
	private void drawLeftMaxRects(BufferedImage image) {
		Random rand = new Random();
		
		for (Rect r : _maxrects) {
			int c = 0xff << 24 | rand.nextInt(256) << 16 | rand.nextInt(256) << 8 | rand.nextInt(256);
			
			for (int y = r.top(); y <= r.bottom() - 1; y++) {
				boolean isYEdge = (y > r.top() && y <= r.top() + 2) || (y < r.bottom() && y >= r.bottom() - 2);
				
				for (int x = r.left(); x <= r.right() - 1; x++) {
					boolean isXEdge = (x > r.left() && x <= r.left() + 2) || (x < r.right() && x >= r.right() - 2);
					
					if (isYEdge || isXEdge) {
						image.setRGB(x, y, c);
					}
				}
			}
		}
	}
	
	private String name() {
		return "sprite" + Integer.toString(_no);
	}
	
	private void exportImage(String outputPath) {
		BufferedImage image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
		
		for (PackImageData img : _packedImages) {
			Utils.copyImageRect(image, img.rect().left() + img.image().border(), img.rect().top() + img.image().border(), 
					img.image().image());
		}
		
//		drawLeftMaxRects(image);
		
		Path imagePath = Paths.get(outputPath, name() + ".png");
		
		try {
			ImageIO.write(image, "png", new File(imagePath.toString()));
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	private void exportConfig(String outputPath) {
		JSONObject frames = new JSONObject();
		
		for (PackImageData img : _packedImages) {
			JSONObject rect = new JSONObject();
			rect.put("x", img.rect().left());
			rect.put("y", img.rect().top());
			rect.put("w", img.rect().width());
			rect.put("h", img.rect().height());
			
			JSONObject availableRect = new JSONObject();
			availableRect.put("x", img.image().rectAvailable().left());
			availableRect.put("y", img.image().rectAvailable().top());
			availableRect.put("w", img.image().rectAvailable().width());
			availableRect.put("h", img.image().rectAvailable().height());
			
			JSONObject sourceSize = new JSONObject();
			sourceSize.put("w", img.image().width());
			sourceSize.put("h", img.image().height());
			
			JSONObject frame = new JSONObject();
			frame.put("frame", rect);
			frame.put("spriteSourceSize", availableRect);
			frame.put("sourceSize", sourceSize);
			
			frames.put(img.image().name(), frame);
		}
		
		JSONObject size = new JSONObject();
		size.put("w", width());
		size.put("h", height());
		
		JSONObject meta = new JSONObject();
		meta.put("image", name() + ".png");
		meta.put("size", size);
		
		JSONObject config = new JSONObject();
		config.put("frames", frames);
		config.put("meta", meta);
		
		Path configPath = Paths.get(outputPath, name() + ".json");
		
		BufferedWriter o = null;
		
		try {
			o = new BufferedWriter(new FileWriter(configPath.toString()));
			o.write(config.toString(4));
			o.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	private Rect packInMaxRect(Rect maxrect, Rect rect) {
		Rect ret = null;
		
		if (rect.width() <= maxrect.width() && rect.height() <= maxrect.height()) {
			// put rect at left bottom of max rect
			ret = Rect.Create(maxrect.left(), maxrect.bottom() - rect.height(), rect.width(), rect.height());
		} else {
			System.out.println(String.format("pack rect fail, try to pack %d, %d, %d, %d in %d, %d, %d, %d", 
					rect.left(), rect.top(), rect.width(), rect.height(), maxrect.left(), maxrect.top(), maxrect.width(), maxrect.height()));
		}
		
		return ret;
	}
	
	private void calcualteMaxRectsAfterPack(Rect rect) {
		List<Rect> addlist = new ArrayList<Rect>();
		
		// check every maxrect which intersect with packed rect.
		// if intersect, remove original one and calculate left max rects.
		for (int i = _maxrects.size() - 1; i >= 0; i--) {
			Rect maxrect = _maxrects.get(i);
			
			if (maxrect.intersect(rect)) {
				_maxrects.remove(i);
				addlist.addAll(leftMaxRectsAfterPack(maxrect, rect));
			}
		}

		_maxrects.addAll(addlist);
		
		Set<Integer> removeset = new HashSet<Integer>();
		
		// check and remove rect includes by others
		for (int i = 0; i < _maxrects.size() - 1; i++) {
			for (int j = i + 1; j < _maxrects.size(); j++) {
				if (_maxrects.get(i).include(_maxrects.get(j))) {
					removeset.add(j);
				} else if (_maxrects.get(j).include(_maxrects.get(i))) {
					removeset.add(i);
				}
			}
		}
		
		List<Integer> removelist = new ArrayList<>(removeset);
		Collections.sort(removelist, Collections.reverseOrder());
		
		for (int i : removelist) {
			_maxrects.remove(i);
		}
	}
	
	private List<Rect> leftMaxRectsAfterPack(Rect maxrect, Rect rect) {
		List<Rect> rects = new ArrayList<Rect>();
		
		if (maxrect.intersect(rect)) {
			if (rect.left() <= maxrect.left() && rect.right() > maxrect.left() && rect.right() < maxrect.right()) {
				if (rect.bottom() >= maxrect.bottom()) {
					if (rect.top() < maxrect.bottom() && rect.top() > maxrect.top()) {
						// left bottom
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), maxrect.right(), rect.top())); // h
						rects.add(Rect.CreateByEdge(rect.right(), maxrect.top(), maxrect.right(), maxrect.bottom())); // v
					} else if (rect.top() <= maxrect.top()) {
						// left
						rects.add(Rect.CreateByEdge(rect.right(), maxrect.top(), maxrect.right(), rect.top()));
					}
				} else if (rect.bottom() < maxrect.bottom() && rect.bottom() > maxrect.top()) {
					if (rect.top() > maxrect.top()) {
						// left middle
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), maxrect.right(), rect.top())); // h1
						rects.add(Rect.CreateByEdge(maxrect.left(), rect.bottom(), maxrect.right(), maxrect.bottom())); // h2
						rects.add(Rect.CreateByEdge(rect.right(), maxrect.top(), maxrect.right(), maxrect.bottom())); // v
					} else {
						// left top
						rects.add(Rect.CreateByEdge(maxrect.left(), rect.bottom(), maxrect.right(), maxrect.bottom())); // h
						rects.add(Rect.CreateByEdge(rect.right(), maxrect.top(), maxrect.right(), maxrect.bottom())); // v
					}
				}
			} else if (rect.left() <= maxrect.left() && rect.right() >= maxrect.right()) {
				if (rect.bottom() >= maxrect.bottom()) {
					if (rect.top() < maxrect.bottom() && rect.top() > maxrect.top()) {
						// bottom
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), maxrect.right(), rect.top()));
					} else if (rect.top() <= maxrect.top()) {
						// all, rect max then max rect, so no more max rect generated
					}
				} else if (rect.bottom() < maxrect.bottom() && rect.bottom() > maxrect.top()) {
					if (rect.top() > maxrect.top()) {
						// middle h
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), maxrect.right(), rect.top())); // h1
						rects.add(Rect.CreateByEdge(maxrect.left(), rect.bottom(), maxrect.right(), maxrect.bottom())); // h2
					} else {
						// top
						rects.add(Rect.CreateByEdge(maxrect.left(), rect.bottom(), maxrect.right(), maxrect.bottom()));
					}
				}
			} else if (rect.left() > maxrect.left() && rect.right() < maxrect.right()) {
				if (rect.bottom() >= maxrect.bottom()) {
					if (rect.top() < maxrect.bottom() && rect.top() > maxrect.top()) {
						// bottom middle
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), rect.left(), maxrect.bottom())); // v1
						rects.add(Rect.CreateByEdge(rect.right(), maxrect.top(), maxrect.right(), maxrect.bottom())); // v2
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), maxrect.right(), rect.top())); // h
					} else if (rect.top() <= maxrect.top()) {
						// middle v
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), rect.left(), maxrect.bottom())); // v1
						rects.add(Rect.CreateByEdge(rect.right(), maxrect.top(), maxrect.right(), maxrect.bottom())); // v2
					}
				} else if (rect.bottom() < maxrect.bottom() && rect.bottom() > maxrect.top()) {
					if (rect.top() > maxrect.top()) {
						// center
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), rect.left(), maxrect.bottom())); // v1
						rects.add(Rect.CreateByEdge(rect.right(), maxrect.top(), maxrect.right(), maxrect.bottom())); // v2
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), maxrect.right(), rect.top())); // h1
						rects.add(Rect.CreateByEdge(maxrect.left(), rect.bottom(), maxrect.right(), maxrect.bottom())); // h2
					} else {
						// top middle
						rects.add(Rect.CreateByEdge(maxrect.left(), rect.bottom(), maxrect.right(), maxrect.bottom()));
					}
				}
			} else if (rect.left() > maxrect.left() && rect.left() < maxrect.right() && rect.right() >= maxrect.right()) {
				if (rect.bottom() >= maxrect.bottom()) {
					if (rect.top() < maxrect.bottom() && rect.top() > maxrect.top()) {
						// right bottom
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), maxrect.right(), rect.top())); // h
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), rect.left(), maxrect.bottom())); // v
					} else if (rect.top() <= maxrect.top()) {
						// right
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), rect.left(), maxrect.bottom())); // v
					}
				} else if (rect.bottom() < maxrect.bottom() && rect.bottom() > maxrect.top()) {
					if (rect.top() > maxrect.top()) {
						// right middle
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), maxrect.right(), rect.top())); // h1
						rects.add(Rect.CreateByEdge(maxrect.left(), rect.bottom(), maxrect.right(), maxrect.bottom())); // h2
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), rect.left(), maxrect.bottom())); // v
					} else {
						// right top
						rects.add(Rect.CreateByEdge(maxrect.left(), rect.bottom(), maxrect.right(), maxrect.bottom())); // h
						rects.add(Rect.CreateByEdge(maxrect.left(), maxrect.top(), rect.left(), maxrect.bottom())); // v
					}
				}
			}
		}
		
		return rects;
	}
	
	private List<PackImageData> _packedImages = new ArrayList<PackImageData>();
	private List<Rect> _maxrects = new ArrayList<Rect>();

	private int _no = 0;
	private int _width = 0;
	private int _height = 0;
}
