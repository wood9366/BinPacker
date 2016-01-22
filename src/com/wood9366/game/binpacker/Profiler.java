package com.wood9366.game.binpacker;

import java.util.HashMap;
import java.util.Map;

public class Profiler {
	static public Profiler Instance() {
		if (_Instance == null) {
			_Instance = new Profiler();
		}
		
		return _Instance;
	}
	
	static private Profiler _Instance = null;
	
	Profiler() {
	}
	
	public void on() {
		_isON = true;
	}
	
	public void off() {
		_isON = false;
	}
	
	public void begin(String name) {
		if (!_isON) return;
		
		_marks.put(name, System.currentTimeMillis());
	}
	
	public void end(String name) {
		if (!_isON) return;
		
		Long start = _marks.get(name);
		
		if (start != null) {
			long t = System.currentTimeMillis() - start;
			int ts = (int)(t / 1000);
			int ms = (int)(t % 1000);
			int h = (int)(ts / 3600);
			int left = (int)(ts % 3600);
			int m = (int)(left / 60);
			int s = (int)(left % 60);
			
			System.out.println(String.format("P> %d:%d:%d.%d - %s", h, m, s, ms, name));
			
			_marks.remove(name);
		}
	}
	
	private boolean _isON = false;
	private Map<String, Long> _marks = new HashMap<String, Long>();
}
