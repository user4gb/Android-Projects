package com.texasstudentmedia;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class FeedItemActivity extends Activity {
	private final String TAG = "Texas Student Media";
	private Feed feed;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_view_item);
		Log.v(TAG, "Inside of " + this.toString());
		setTitle("Texas Student Media v" + ((TexasStudentMediaApp) getApplication()).getVersion());
		
		// Set feed being browsed
		feed = (Feed)((TexasStudentMediaApp) getApplication()).getFeeds().get(getIntent().getExtras().getInt("feed_index", -1));
		
		//TODO: Display item content
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_view);

	}
}
