package com.avai.wpzoo;


public class MyPoint {
	public double x;
	public double y;
	public MyPoint() {
		x=0;
		y=0;
	}
	public MyPoint(double new_x, double new_y) {
		x = new_x;
		y = new_y;
	}
	
	public MyPoint plus (MyPoint a) {
		x += a.x;
		y += a.y;
		return this;
	}
	
	public MyPoint minus (MyPoint a) {
		x -= a.x;
		y -= a.y;
		return this;
	}
	public MyPoint dividedBy(double zoom) {
		x /= zoom;
		y /= zoom;
		return this;
	}
	public void print(String name){
		System.out.print(name+":("+x+" , "+y+") ");
	}
	
	public void println(String name) {
		print(name);
		System.out.println();
	}
}
