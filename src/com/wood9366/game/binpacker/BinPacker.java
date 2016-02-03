package com.wood9366.game.binpacker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BinPacker {
	public List<BinData> bins() {
		return _bins;
	}
	
	public void addImage(ISpriteData image) {
		_images.add(image);
	}
	
	public int pack() {
		int numPacked = 0;
		
		calcualteNumberOfBinAndSize();
		
		for (ISpriteData image : _images) {
			if (packImage(image)) {
				numPacked++;
			}
		}
		
		return numPacked;
	}
	
	public void export(String outputPath) {
		if (new File(outputPath).exists()) {
			for (BinData bin : _bins) {
				Profiler.Instance().begin("export bin " + Integer.toString(bin.id()));
				bin.export(outputPath);
				Profiler.Instance().end("export bin " + Integer.toString(bin.id()));
			}
		} else {
			System.out.println(" output path [" + outputPath + "] don't exist");
		}
	}
	
	private void calcualteNumberOfBinAndSize() {
		float totalArea = 0;
		
		for (ISpriteData image : _images) {
			totalArea += image.rect().area();
		}
		
		int minSize = 128;
		int maxSize = 2048;

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
	
	private boolean packImage(ISpriteData image) {
		boolean isPack = false; 
		
		if (findBestMaxRect(image.rect())) {
			isPack = _bestBin.pack(_bestMaxRect, image);
			
			if (!isPack) {
				System.out.println(String.format("%s %s", (isPack ? "o" : "x"), image.info()));
			}
		}
		
		return isPack;
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
			
			if (isFind) {
				break;
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
	
	private List<BinData> _bins = new ArrayList<BinData>();
	private Set<ISpriteData> _images = new HashSet<ISpriteData>();
}
