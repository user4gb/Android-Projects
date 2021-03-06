package com.texasstudentmedia.helper;


public class SafeThread extends Thread {
	private boolean isAlive,
					isPaused;
	
	public SafeThread(){
		isAlive = true;
		isPaused = true;
	}

	public void start(){
		super.start();
		isPaused = false;
	}
	public void pause(){
		isPaused = true;
	}
	public void unpause(){
		isPaused = false;
	}
	public void kill(){
		isAlive = false;
	}
	public boolean isPaused(){
		return isPaused;
	}
	
	public void run(){
		while(isAlive){
			if(!isPaused){
				doStuff();
			}
		}
	}
	
	protected void doStuff(){}
}
