package com.wood9366.game.binpacker;

public class Rect {
	static public Rect Create(int x, int y, int w, int h) {
		return new Rect().setXYWH(x, y, w, h);
	}
	
	static public Rect CreateByEdge(int left, int top, int right, int bottom) {
		return new Rect().setLTRB(left, top, right, bottom);
	}
	
	Rect() {
		_x = _y = _w = _h = 0;
	}
	
	public int area() { return _w * _h; }
	
	public int left() { return _x; }
	public int top() { return _y; }
	public int right() { return _x + _w; }
	public int bottom() { return _y + _h; }
	public int width() { return _w; }
	public int height() { return _h; }
	
	public boolean intersect(Rect r) {
		return !(r.right() <= left() ||
				r.left() >= right() ||
				r.bottom() <= top() ||
				r.top() >= bottom());
	}
	
	public boolean include(Rect r) {
		return (left() <= r.left() && right() >= r.right() && top() <= r.top() && bottom() >= r.bottom());
	}
	
	private Rect setXYWH(int x, int y, int w, int h) {
		_x = x;
		_y = y;
		_w = w;
		_h = h;
		
		return this;
	}
	
	private Rect setLTRB(int l, int t, int r, int b) {
		_x = l;
		_y = t;
		_w = Math.max(0, r - l);
		_h = Math.max(0, b - t);
		
		return this;
	}
	
	private int _x;
	private int _y;
	private int _w;
	private int _h;
}