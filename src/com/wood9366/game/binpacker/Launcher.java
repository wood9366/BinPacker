package com.wood9366.game.binpacker;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Launcher {

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
