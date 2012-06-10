package com.texasstudentmedia;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

public class FeedMenuItem extends TableLayout{
	private AnimationDrawable anim = null; 
	private ImageView image = null;
	
	public FeedMenuItem(Context context, final Feed feed) {
		super(context);
		
		setTag(feed);
		
		View v = inflate(context, R.layout.menu_item, null);
		
		TextView name = (TextView) v.findViewById(R.id.feed_name);
		name.setText(feed.getName());
		
		image = (ImageView) v.findViewById(R.id.loading);
		image.setImageBitmap(null);
		if (feed.getState() == Feed.FETCHING){
			image.setBackgroundResource(R.drawable.loading);
			anim = (AnimationDrawable) image.getBackground();
		}

		addView(v);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		if (anim != null)
			anim.start();
		
		super.onWindowFocusChanged(hasFocus);
	}
	
	public void removeLoadingAnim(){
		// Remove the loading bar for the item
		if (anim != null){
			anim.stop();
			image.setBackgroundResource(0);
		}
	}
}
