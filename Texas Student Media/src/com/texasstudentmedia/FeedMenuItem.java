package com.texasstudentmedia;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

public class FeedMenuItem extends TableLayout{
	private AnimationDrawable anim; 
	
	public FeedMenuItem(Context context, Feed feed) {
		super(context);
		
		setTag(feed);
		
		View v = inflate(context, R.layout.menu_item, null);
		
		TextView name = (TextView) v.findViewById(R.id.feed_name);
		name.setText(feed.getName());
		
		ImageView image = (ImageView) v.findViewById(R.id.loading);
		image.setImageBitmap(null);
		image.setBackgroundResource(R.drawable.loading);
		anim = (AnimationDrawable) image.getBackground();
		
		addView(v);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		anim.start();
		
		super.onWindowFocusChanged(hasFocus);
	}
	
	public void removeLoading(){
		anim.setVisible(false, false);
	}
	
}
