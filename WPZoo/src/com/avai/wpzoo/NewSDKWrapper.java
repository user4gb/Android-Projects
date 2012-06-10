package com.avai.wpzoo;

import android.view.MotionEvent;

public class NewSDKWrapper {
	public static final int ACTION_POINTER_DOWN = 5; // MotionEvent.ACTION_POINTER_DOWN
	public static final int ACTION_POINTER_UP = 6; //MotionEvent.ACTION_POINTER_UP

	static int getPointerCount(MotionEvent event) {
		return event.getPointerCount();
	}

	public static float getX(MotionEvent event, int i) {
		return event.getX(i);
	}
	
	public static float getY(MotionEvent event, int i) {
		return event.getY(i);
	}
}
