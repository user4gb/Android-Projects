package com.avai.wpzoo;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

public class AmpMapView extends View  {
	private Context mCtx;
	private Bitmap mMap, blueDot, yellowDot, redDot, bluePointer, bluePointerGlowing;
	private Paint mPaint;
	private double zoom, minZoom;
	private static final double MAX_ZOOM = 2.0f;
	private MyPoint ref1Met, ref2Met, ref1Pxl, ref2Pxl;
	
	private MyPoint mapPxl, currLocPxl;
	private Float currBearing;
	private MyPoint pre0Cvs, pre1Cvs;
	private int currAccurPxl;
	
	private final int LANDMARK = 1;
	private final int FRIEND = 2;
	
	private boolean moving = false;
	
	private ArrayList<Place> places;
	
	public AmpMapView(Context ctx, MyPoint ref1Ugs, MyPoint ref1Pxl, MyPoint ref2Ugs, MyPoint ref2Pxl) {
		super(ctx);
		mCtx = ctx;
		mPaint = new Paint();
        mPaint.setAntiAlias(true);
        blueDot = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.com_blue_dot);
        bluePointer = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.com_blue_pointer);
        bluePointerGlowing = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.com_blue_pointer_glowing);
        yellowDot = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.com_yellow_dot);
        redDot = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.com_red_dot);
        ref1Met = ugsToMet(ref1Ugs);
        ref2Met = ugsToMet(ref2Ugs);
        this.ref1Pxl = ref1Pxl;
        this.ref2Pxl = ref2Pxl;
        zoom = 1;
        places = new ArrayList<Place>();
	}
	
	public void setBitmap(Bitmap map) {
		mMap = map;	
		mapPxl = new MyPoint(mMap.getWidth()/2, mMap.getHeight()/2);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
        if(minZoom == 0) {
			float minZoomX = ((float)getMeasuredWidth())/((float)mMap.getWidth());
	        float minZoomY = ((float)getMeasuredHeight())/((float)mMap.getHeight());
	        minZoom = Math.min(minZoomX, minZoomY);
        }

        //Draw map
		MyPoint topLeftPxl = new MyPoint(Math.max(mapPxl.x - getMeasuredWidth()/2/zoom, 0),
										 Math.max(mapPxl.y - getMeasuredHeight()/2/zoom, 0));
		MyPoint botRgtPxl = new MyPoint(Math.min(mapPxl.x + getMeasuredWidth()/2/zoom, mMap.getWidth()),
										Math.min(mapPxl.y + getMeasuredHeight()/2/zoom, mMap.getHeight()));
		
		Rect src = new Rect((int)topLeftPxl.x, (int)topLeftPxl.y, (int)botRgtPxl.x,  (int)botRgtPxl.y);
		
		MyPoint topLeftCvs = pxlToCvs(topLeftPxl);
		MyPoint botRgtCvs = pxlToCvs(botRgtPxl);
		RectF dst = new RectF((float)topLeftCvs.x, (float)topLeftCvs.y, (float)botRgtCvs.x, (float)botRgtCvs.y);
		
		canvas.drawBitmap(mMap, src, dst, null);

		// Draw current location
		if(currLocPxl != null) {
			MyPoint currLocCvs = pxlToCvs(currLocPxl);
			mPaint.setColor(Color.argb(255,0,0,187));
			mPaint.setStyle(Paint.Style.STROKE);
			canvas.drawCircle((float)currLocCvs.x, (float)currLocCvs.y, (float)(currAccurPxl*zoom), mPaint);
			mPaint.setColor(Color.argb(30,0,0,187));
			mPaint.setStyle(Paint.Style.FILL);
			canvas.drawCircle((float)currLocCvs.x, (float)currLocCvs.y, (float)(currAccurPxl*zoom), mPaint);
			mPaint.setAlpha(255);
			if(currBearing == null) {
				canvas.drawBitmap(blueDot, (float)currLocCvs.x - blueDot.getWidth()/2 , (float)currLocCvs.y - blueDot.getHeight()/2, mPaint);	
			} else {
				Matrix m = new Matrix();
				m.setRotate((float)(currBearing.floatValue() + 45.0), bluePointer.getWidth()/2, bluePointer.getHeight()/2);
				Bitmap rotatedBluePointer = Bitmap.createBitmap(bluePointer, 0,0,bluePointer.getWidth(), bluePointer.getHeight(), m, true);
				canvas.drawBitmap(rotatedBluePointer, (float)currLocCvs.x - rotatedBluePointer.getWidth()/2 , (float)currLocCvs.y - rotatedBluePointer.getHeight()/2, mPaint);
			}
		}
		
		// Draw places
		for(int i=0;i<places.size();i++) {
			Place place = places.get(i);
			MyPoint placeCvs = pxlToCvs(place.location);
			if(place.type == FRIEND)
				canvas.drawBitmap(yellowDot, (float)placeCvs.x - yellowDot.getWidth()/2, (float)placeCvs.y - yellowDot.getHeight()/2, mPaint);
			else if(place.type == LANDMARK)
				canvas.drawBitmap(redDot, (float)placeCvs.x - redDot.getWidth()/2, (float)placeCvs.y - redDot.getHeight()/2, mPaint);
			String text = place.name;
			
			MyPoint placeTextCvs = new MyPoint(placeCvs.x + 10, placeCvs.y + 6);
			
			Rect textBounds = new Rect();
			mPaint.getTextBounds(text, 0, text.length(), textBounds);
			float radius = 5;
			float padding = 3;
			RectF textRect = new RectF();
			textRect.left = (int) placeTextCvs.x + textBounds.left - padding;
			textRect.bottom = (int) placeTextCvs.y + textBounds.bottom + padding;
			textRect.right = (int) placeTextCvs.x + textBounds.right + padding;
			textRect.top = (int) placeTextCvs.y + textBounds.top - padding;
			
			//Draw text label background
			mPaint.setColor(Color.WHITE);
			mPaint.setAlpha(127);
			canvas.drawRoundRect(textRect, radius, radius, mPaint);
			
			//Draw text label
			mPaint.setColor(Color.BLACK);
			mPaint.setTextSize(14);
			canvas.drawText(text, (float)placeTextCvs.x, (float)placeTextCvs.y, mPaint);
		}
	}
	
	private MyPoint pxlToCvs (MyPoint pointPxl) {
		MyPoint pointCvs = new MyPoint();
		pointCvs.x = getMeasuredWidth()/2 + (pointPxl.x - mapPxl.x)*zoom;
		pointCvs.y = getMeasuredHeight()/2 + (pointPxl.y - mapPxl.y)*zoom;
		return pointCvs;
	}
	
	private double saturate(double a, double b, double c) {
		return (b<a?c<b?b:a<c?a:c:c<a?a:b<c?b:c); 
	}
	
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction() & 255/*MotionEvent.ACTION_MASK*/;
		
		boolean hasMultiTouch = Integer.parseInt(Build.VERSION.SDK) >= 5;
		
	    switch (action) {
	    case MotionEvent.ACTION_MOVE:
	    	//System.out.println("ACTION_MOVE");
	    	moving = true;
	    	MyPoint cur0Cvs = new MyPoint();
	    	MyPoint cur1Cvs = new MyPoint();
	    	cur0Cvs.x = cur1Cvs.x = (int) event.getX();
	    	cur0Cvs.y = cur1Cvs.y = (int) event.getY();
	    	if(hasMultiTouch && NewSDKWrapper.getPointerCount(event) > 1) {
	    		cur1Cvs.x = (int) NewSDKWrapper.getX(event, 1);
	    		cur1Cvs.y = (int) NewSDKWrapper.getY(event, 1);
	    		float distCurPxl = (float) (Math.pow(cur0Cvs.x-cur1Cvs.x, 2) + Math.pow(cur0Cvs.y-cur1Cvs.y, 2));
	    		float distPrePxl = (float) (Math.pow(pre0Cvs.x-pre1Cvs.x, 2) + Math.pow(pre0Cvs.y-pre1Cvs.y, 2));
	    		//System.out.println("distCurPxl: "+distCurPxl+"   distPrePxl: "+distPrePxl + "   zoom: "+zoom);
	    		if(distPrePxl > 0)
	    			zoom /= distPrePxl/distCurPxl;
	    		//System.out.println("Zoom: " + zoom);
	    		zoom = saturate(zoom, minZoom, MAX_ZOOM);
	    	}
	   
	    	MyPoint deltaCvs = new MyPoint(((cur0Cvs.x - pre0Cvs.x) + (cur1Cvs.x - pre1Cvs.x))/2,
	    								   ((cur0Cvs.y - pre0Cvs.y) + (cur1Cvs.y - pre1Cvs.y))/2);
	    	mapPxl = mapPxl.minus(deltaCvs.dividedBy(zoom));
	    	mapPxl.x = saturate(mapPxl.x, (int)(getMeasuredWidth()/2/zoom), (int)(mMap.getWidth() - getMeasuredWidth()/2/zoom));
			mapPxl.y = saturate(mapPxl.y, (int)(getMeasuredHeight()/2/zoom), (int)(mMap.getHeight() - getMeasuredHeight()/2/zoom));
			pre0Cvs = cur0Cvs;
			pre1Cvs = cur1Cvs;
	    	
    		invalidate();

    		break;
	    case MotionEvent.ACTION_DOWN:
	    	//System.out.println("ACTION_DOWN");
	    	moving = false;
	    	pre0Cvs = new MyPoint(event.getX(), event.getY());
	    	pre1Cvs = pre0Cvs;
	    	if(hasMultiTouch && NewSDKWrapper.getPointerCount(event) > 1) {
	    		pre1Cvs = new MyPoint(NewSDKWrapper.getX(event, 1), NewSDKWrapper.getY(event, 1));
	    	}
	    	break;
	    case NewSDKWrapper.ACTION_POINTER_DOWN:
	    	//System.out.println("ACTION_POINTER_DOWN");
	    	if(NewSDKWrapper.getPointerCount(event) > 1) {
	    		pre1Cvs = new MyPoint(NewSDKWrapper.getX(event, 1), NewSDKWrapper.getY(event, 1));
	    	}
	    	break;
	    case NewSDKWrapper.ACTION_POINTER_UP:
	    	//System.out.println("ACTION_POINTER_UP");
    		pre1Cvs = pre0Cvs;
    		break;
	    case MotionEvent.ACTION_UP:
	    	//System.out.println("ACTION_UP");
	    	if(!moving)
	    		toggleFullScreen();
    		break;
	    }
	    return true;
	}

	private void toggleFullScreen() {
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	public void updateCurrentLocation(MyPoint currLocUgs, float currAccurMet) {
		currLocPxl = ugsToPxl(currLocUgs);
		currAccurPxl = (int) (currAccurMet * mMap.getWidth() / (ref2Met.x - ref1Met.x));
		currLocUgs.println("updating current location to ");
		invalidate();
	}
	
	public boolean locationIsOnMap(MyPoint p) {
		return pxlIsOnBitmap(ugsToPxl(p));
	}
	
	private boolean pxlIsOnBitmap(MyPoint u) {
		return (u.x >= 0 && u.x <= mMap.getWidth() && u.y >= 0 && u.y <= mMap.getHeight());
	}

	public void addFriend(MyPoint friendUgs, String name) {
		System.out.print("Adding friend: " + name);
		friendUgs.println(" at location");
		places.add(new Place(ugsToPxl(friendUgs), name, FRIEND));
	}
	
	public void removeAllFriends() {
		for(int i=0;i<places.size();i++) {
			if(places.get(i).type == FRIEND) {
				places.remove(i);
			}
		}
	}
 	
	public void addLandmark(MyPoint landmarkUgs, String name) {
		places.add(new Place(ugsToPxl(landmarkUgs), name, LANDMARK));
	}
	
	public void removeAllLandmarks() {
		for(int i=0;i<places.size();i++) {
			if(places.get(i).type == LANDMARK) {
				places.remove(i);
			}
		}
	}
	
	private MyPoint ugsToPxl (MyPoint pointUgs) {
		MyPoint pointMet = ugsToMet(pointUgs);
		
		MyPoint pointPxl = new MyPoint();
		pointPxl.x = interpolate (pointMet.x, ref1Met.x, ref2Met.x, ref1Pxl.x, ref2Pxl.x);
		pointPxl.y = interpolate (pointMet.y, ref1Met.y, ref2Met.y, ref1Pxl.y, ref2Pxl.y);
		//pointMet.print("pointMet");
		//pointPxl.println("pointPxl");
		//ref1Met.print("ref1Met");
		//ref1Pxl.println("ref1Pxl");
		//ref2Met.print("ref2Met");
		//ref2Pxl.println("ref2Pxl");
		return pointPxl;
	}
	
	private MyPoint ugsToMet(MyPoint u) {
		String utm = new CoordinateConversion().latLon2UTM(u.x, u.y);
		//System.out.println(utm);
		String[] utmSplit = utm.split(" ");
		return new MyPoint(Integer.parseInt(utmSplit[2]), Integer.parseInt(utmSplit[3]));
	}
	
	private double interpolate(double x, double x0, double x1, double y0, double y1) {
		if(x1-x0 == 0) {
			//System.out.println("returning zero to prevent dividing by zero");
			return 0;
		}
		return y0+(x-x0)*(y1-y0)/(x1-x0);
	}
	
	private class Place {
		public MyPoint location;
		public String name;
		public int type;
		public Place(MyPoint p, String n, int t) {
			location = p;
			name = n;
			type = t;
		}
	}

	public void updateCurrentBearing(float f) {
		currBearing = new Float(f);
		invalidate();
	}

	public void releaseMap() {
		mMap = null;
	}



}
