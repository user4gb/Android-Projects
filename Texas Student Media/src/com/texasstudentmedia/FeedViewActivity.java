package com.texasstudentmedia;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class FeedViewActivity extends Activity{
	private final String TAG = "Texas Student Media";
	private ListView feed_list;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feed_view);
		Log.v(TAG, "Inside of " + this.toString());
		setTitle("Texas Student Media v" + ((TexasStudentMediaApp) getApplication()).getVersion());
		
		// Set title
		

		// Get list to attach data to
		feed_list = (ListView) findViewById(R.id.feed_preview_list);
		
		// Load & display feed items
		try{
			feed_list.setAdapter(new FeedViewItemListAdapter(this, (((TexasStudentMediaApp) getApplication()).getFeeds()).get(getIntent().getExtras().getInt("feed_index", -1)).getItems()));
			feed_list.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
					// Get selected feed item's name
					String feed_name = ((Feed) (((TexasStudentMediaApp) getApplication()).getFeeds()).get(getIntent().getExtras().getInt("feed_index", -1))).getName();
					Log.v(TAG, feed_name);
					
					// Launch feed viewer w/ data from feed
					Intent intent = new Intent();
					// Pass index of current feed
					intent.putExtra("feed_index", getIntent().getExtras().getInt("feed_index", -1));
					// Pass index of feed item to be viewed
					intent.putExtra("feed_item_index", pos);
					intent.setClass(FeedViewActivity.this, FeedItemActivity.class);
					FeedViewActivity.this.startActivity(intent);
				}
			});
		}catch (Exception e){
			e.printStackTrace();
			Log.e(TAG, "", e);
		}
	}
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

	}
}