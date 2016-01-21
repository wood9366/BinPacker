package com.wood9366.game.binpacker;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class BinPacker {
	public void addImage(ImageData image) {
		_images.add(image);
	}
	
	public void pack() {
		calcualteNumberOfBinAndSize();
		
		for (ImageData image : _images) {
			packImage(image);
		}
	}
	
	public void export(String outputPath) {
		if (new File(outputPath).exists()) {
			for (BinData bin : _bins) {
				bin.export(outputPath);
			}
		} else {
			System.out.println(" output path [" + outputPath + "] don't exist");
		}
	}
	
	private void calcualteNumberOfBinAndSize() {
		float totalArea = 0;
		
		for (ImageData image : _images) {
			totalArea += image.rect().area();
		}
		
		int minSize = 128;
		int maxSize = 4096;

		totalArea *= 1.2f;
		
		while (totalArea > 0) {
			int size = minSize;
			
			while (totalArea > size * size && size < maxSize) {
				size *= 2;
			}
			
			totalArea -= size * size;
			_bins.add(new BinData(_bins.size(), size, size));
		}
	}
	
	private void packImage(ImageData image) {
		if (findBestMaxRect(image.rect())) {
			boolean isPack = _bestBin.pack(_bestMaxRect, image);
			
			System.out.println(String.format("%s %s", (isPack ? "o" : "x"), image.imagePath()));
			
			for (BinData bin : _bins) {
				System.out.println(String.format(" bin %d, maxrects: %d", bin.no(), bin.maxRects().size()));
			}
		}
	}
	
	private boolean findBestMaxRect(Rect rect) {
		_bestBin = null;
		_bestMaxRect = null;
		
		boolean isFind = false;
		int weight = 4096;
		
		for (BinData bin : _bins) {
			for (Rect maxrect : bin.maxRects()) {
				// find perfect match
				if (rect.width() == maxrect.width() && rect.height() == maxrect.height()) {
					_bestBin = bin;
					_bestMaxRect = maxrect;
					return true;
				} else if (rect.width() < maxrect.width() && rect.height() < maxrect.height()) {
					int w = calculatePackWeight(maxrect, rect);
					
					if (w < weight) {
						isFind = true;
						weight = w;
						_bestBin = bin;
						_bestMaxRect = maxrect;
					}
				}
			}
		}
		
		return isFind;
	}
	
	BinData _bestBin = null;
	Rect _bestMaxRect = null;	

	private int calculatePackWeight(Rect maxrect, Rect rect) {
		// with best short side fit
		return Math.min(Math.abs(maxrect.width() - rect.width()), Math.abs(maxrect.height() - rect.height()));
	}
	
	private Set<BinData> _bins = new HashSet<BinData>();
	private Set<ImageData> _images = new HashSet<ImageData>();
	
	public static void main(String[] args) {
		BinPacker packer = new BinPacker();
		
		for (int i = 0; i < 1000; i++) {
			packer.addImage(new ImageData("res/sprites/" + Integer.toString(i) + ".png"));
		}
		
		System.out.println("==> start packing");
		packer.pack();
		System.out.println("==> export bin image and config data");
		packer.export("res/");
		System.out.println("==> done");
	}
}
