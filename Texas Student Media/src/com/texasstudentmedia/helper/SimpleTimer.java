package com.texasstudentmedia.helper;

public class SimpleTimer {
	private long start,
				 end;
	
	public SimpleTimer(){
		reset();
	}
	
	public void start(){
		start = System.currentTimeMillis();
	}
	
	public void stop(){
		end = System.currentTimeMillis();
	}
	
	public long getTimeDiff(){
		long diff = end - start;
		reset();
		return diff;
	}
	
	public void reset(){
		start = end = -1;
	}
}
