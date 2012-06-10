package com.texasstudentmedia;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

public class FeedItemActivity extends Activity {
	private final String TAG = "Texas Student Media";
	private Feed feed;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_item);
		Log.v(TAG, "Inside of " + this.toString());
		setTitle("Texas Student Media v" + ((TexasStudentMediaApp) getApplication()).getVersion());
		
		// Set feed being browsed
		/*feed = (Feed)((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds().get(getIntent().getExtras().getInt("feed_index", -1));
		TextView feedbtn = (TextView) findViewById(R.id.item_view_parent_feed);
		feedbtn.setText(feed.getName());*/
		

		// Display item content
		try{
			WebView webview = (WebView) findViewById(R.id.item_view_content);
			webview.loadData(feed.getItems().get(getIntent().getExtras().getInt("feed_item_index", 0)).getContent(), "text/html", "utf-8");
		}catch (Exception e){
			Log.e(TAG, "", e);
		}
		
		// TODO: Set Next & Prev button functions
		
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_view);

	}
}
