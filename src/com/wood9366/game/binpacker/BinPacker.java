package com.wood9366.game.binpacker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	
	private void packImage(ImageData image) {
		if (findBestMaxRect(image.rect())) {
			boolean isPack = _bestBin.pack(_bestMaxRect, image);
			
			if (!isPack) {
				System.out.println(String.format("%s %s", (isPack ? "o" : "x"), image.imagePath()));
			}
			
//			for (BinData bin : _bins) {
//				System.out.println(String.format(" bin %d, maxrects: %d", bin.no(), bin.maxRects().size()));
//			}
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
	
	private Set<BinData> _bins = new HashSet<BinData>();
	private Set<ImageData> _images = new HashSet<ImageData>();
	
	public static void GatherPNGs(String dir, List<String> pngs) {
		File d = new File(dir);
		
		if (d.exists()) {
			for (File f : d.listFiles()) {
				if (f.isDirectory()) {
					GatherPNGs(f.toString(), pngs);
				} else {
					if (f.getName().endsWith(".png")) {
						pngs.add(f.toPath().toString());
					}
				}
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length >= 2) {
			if (new File(args[0]).exists() && new File(args[1]).exists()) {
				BinPacker packer = new BinPacker();
				
				List<String> sprites = new ArrayList<String>();
				
				GatherPNGs(args[0], sprites);
		
				if (sprites.size() > 0) {
					System.out.println("==> process input images");
					
					for (String sprite : sprites) {
						packer.addImage(new ImageData(sprite));
					}
					
					System.out.println("==> start packing");
					packer.pack();
					
					System.out.println("==> export bin image and config data");
					packer.export(args[1]);
					
					System.out.println("==> done");
				} else {
					System.out.println("no valid source image be found at specific path");
				}
			} else {
				System.out.println("invalid source image path or output bin path");
			}
		} else {
			System.out.println("BinPacker SourceImagePath OutputBinPath");
		}
		

	}
}
