package com.wood9366.game.binpacker;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BinPacker {
	public Set<BinData> bins() {
		return _bins;
	}
	
	public void addImage(ImageData image) {
		_images.add(image);
	}
	
	public int pack() {
		int numPacked = 0;
		
		calcualteNumberOfBinAndSize();
		
		for (ImageData image : _images) {
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
	
	private boolean packImage(ImageData image) {
		boolean isPack = false; 
		
		if (findBestMaxRect(image.rect())) {
			isPack = _bestBin.pack(_bestMaxRect, image);
			
			if (!isPack) {
				System.out.println(String.format("%s %s", (isPack ? "o" : "x"), image.imagePath()));
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
//		Profiler.Instance().on();
		
		if (args.length >= 2) {
			String srcPath = Paths.get(args[0]).toAbsolutePath().toString();
			String destPath = Paths.get(args[1]).toAbsolutePath().toString();
			
			if (new File(srcPath).exists() && new File(destPath).exists()) {
				BinPacker packer = new BinPacker();
				
				List<String> sprites = new ArrayList<String>();
				
				GatherPNGs(srcPath, sprites);
		
				if (sprites.size() > 0) {
					Profiler.Instance().begin("total");
					
					System.out.println("==> process input images");
					Profiler.Instance().begin("process images");
					for (String sprite : sprites) {
						Profiler.Instance().begin("process image " + sprite);
						packer.addImage(new ImageData(sprite));
						Profiler.Instance().end("process image " + sprite);
					}
					Profiler.Instance().end("process images");
					
					System.out.println("==> start packing");
					Profiler.Instance().begin("packing");
					int numPacked = packer.pack();
					Profiler.Instance().end("packing");
					
					System.out.println("==> export bin image and config data");
					Profiler.Instance().begin("exporting");
					packer.export(destPath);
					Profiler.Instance().end("exporting");
					
					Profiler.Instance().end("total");
					System.out.println("==> done");
					
					System.out.println("==> summary");
					System.out.println(String.format("pack %d/%d images totally from %s", 
							numPacked, sprites.size(), srcPath));
					System.out.println(String.format("output %d bins into %s", packer.bins().size(), destPath));
					for (BinData bin : packer.bins()) {
						System.out.println(String.format("  %s: %d x %d", bin.id(), bin.width(), bin.height()));
					}
				} else {
					System.out.println("no valid source image be found at specific path");
				}
			} else {
				System.out.println("invalid source image path [" + srcPath + "] or output bin path [" + destPath + "]");
			}
		} else {
			System.out.println("BinPacker SourceImagePath OutputBinPath");
		}
		

	}
}
