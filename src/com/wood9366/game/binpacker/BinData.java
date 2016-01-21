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
import java.util.Set;

import javax.imageio.ImageIO;

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
		changeSize(width, height);
		addRect(Rect.Create(0, 0, width(), height()));
	}
	
	public int no() { return _no; }
	public int width() { return _width; }
	public int height() { return _height; }
	
	public boolean pack(Rect maxrect, ImageData image) {
		boolean isPack = false;
		
		if (_maxrects.contains(maxrect)) {
			Rect packedRect = packInMaxRect(maxrect, image.rect());
			
			if (packedRect != null) {
//				System.out.println("----------------------------------------------");
//				for (Rect r : _maxrects) {
//					System.out.println(String.format("%d, %d, %d, %d", r.left(), r.top(), r.width(), r.height()));
//				}
				
				isPack = true;
				calcualteMaxRectsAfterPack(packedRect);
				_packedImages.add(new PackImageData(image, packedRect));

//				System.out.println(String.format("pack %s to %d,%d,%d,%d", image.name(), packedRect.left(), packedRect.top(), packedRect.width(), packedRect.height()));
//				for (Rect r : _maxrects) {
//					System.out.println(String.format("%d, %d, %d, %d", r.left(), r.top(), r.width(), r.height()));
//				}
			}
		} else {
			System.out.println("incorrect maxrect");
		}
		
		return isPack;
	}
	
	public void export(String outputPath) {
		exportImage(outputPath);
		exportConfig(outputPath);
	}
	
	private void exportImage(String outputPath) {
		BufferedImage image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);
		
		for (PackImageData img : _packedImages) {
			copyImageRect(image, img.rect().left(), img.rect().top(), img.image().image());
		}
		
		Path imagePath = Paths.get(outputPath, String.format("bin%d.png", _no));
		
		try {
			ImageIO.write(image, "png", new File(imagePath.toString()));
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	private void exportConfig(String outputPath) {
		StringBuilder s = new StringBuilder();
		
		s.append("[\n");
		
		for (int i = 0; i < _packedImages.size(); i++) {
			PackImageData img = _packedImages.get(i);
			
			s.append(String.format("{ name: \"%s\", x: %d, y: %d, w: %d, h: %d }",
					img.image().name(), img.rect().left(), img.rect().top(), img.rect().width(), img.rect().height()));
			
			if (i < _packedImages.size() - 1) {
				s.append(",");
			}
			
			s.append("\n");
		}
		
		s.append("]\n");
		
		Path configPath = Paths.get(outputPath, String.format("bin%d.txt", _no));
		
		BufferedWriter o = null;
		
		try {
			o = new BufferedWriter(new FileWriter(configPath.toString()));
			o.write(s.toString());
			o.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	void changeSize(int width, int height) {
		_width = width;
		_height = height;
	}
	
	public void removeRect(Rect rect) {
		_maxrects.remove(rect);
	}
	
	public void addRect(Rect rect) {
		_maxrects.add(rect);
	}
	
	public List<Rect> maxRects() {
		return _maxrects;
	}
	
	private void copyImageRect(BufferedImage dest, int dx, int dy, BufferedImage src) {
		copyImageRect(dest, dx, dy, src, 0, 0, src.getWidth(), src.getHeight());
	}
	
	private void copyImageRect(BufferedImage dest, int dx, int dy, BufferedImage src, int sx, int sy) {
		copyImageRect(dest, dx, dy, src, sx, sy, src.getWidth(), src.getHeight());
	}
	
	private void copyImageRect(BufferedImage dest, int dx, int dy, BufferedImage src, int sx, int sy, int w, int h) {
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (src.getRaster().getNumBands() < 4 && dest.getRaster().getNumBands() >= 4) {
					dest.getRaster().setSample(dx + x, dy + y, 3, 1);
				}
				
				int numBands = Math.min(dest.getRaster().getNumBands(), src.getRaster().getNumBands());
				
				for (int b = 0; b < numBands; b++) {
					dest.getRaster().setSample(dx + x, dy + y, b, src.getRaster().getSample(sx + x, sy + y, b));
				}
				
				// correct alpha channel for source image which don't have alpha channel
				if (src.getRaster().getNumBands() < 4 && dest.getRaster().getNumBands() >= 4) {
					dest.getRaster().setSample(dx + x, dy + y, 3, 255);
				}
			}
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
		
		for (int i = _maxrects.size() - 1; i >= 0; i--) {
			Rect maxrect = _maxrects.get(i);
			
			if (maxrect.intersect(rect)) {
				_maxrects.remove(i);
				addlist.addAll(leftMaxRectsAfterPack(maxrect, rect));
			}
		}

		_maxrects.addAll(addlist);
		
		Set<Integer> removeset = new HashSet<Integer>();
		
		// check and remove overlap rect
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
	
	private Set<Rect> leftMaxRectsAfterPack(Rect maxrect, Rect rect) {
		Set<Rect> rects = new HashSet<Rect>();
		
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
