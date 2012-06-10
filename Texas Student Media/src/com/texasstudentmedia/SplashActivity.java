package com.texasstudentmedia;

import java.util.ArrayList;

import com.texasstudentmedia.helper.SafeThread;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends Activity {
	private final String TAG = "Texas Student Media";
	private SafeThread thread;
	private ArrayList<Feed> feeds;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		Log.v(TAG, "Inside of " + this.getClass().getName());
		setTitle("Texas Student Media v" + ((TexasStudentMediaApp) getApplication()).getVersion());
		
		// Safely retrieve, parse, and save data from RSS feeds
		thread = new SafeThread() {
			public void doStuff() {
				// Minimum number of items to retrieve for each feed
				final int min_items = 10;

				// URLs for feeds
				String feed_urls[][] = {
						{ "The Daily Texan: Breaking",
								"http://dailytexanonline.com/rss/spreed/main.xml" },
						{ "The Daily Texan: News",
								"http://dailytexanonline.com/rss/spreed/news.xml" },
						{ "The Daily Texan: Sports",
								"http://dailytexanonline.com/rss/spreed/sports.xml" },
						{ "The Daily Texan: Opinion",
								"http://dailytexanonline.com/rss/spreed/opinion.xml" },
						{ "The Daily Texan: Life & Arts",
								"http://dailytexanonline.com/rss/spreed/lifeandarts.xml" },
						// {"Texas Travesty", ""},
						{ "Inside Our Campus",
								"http://insideourcampus.com/feed/" }
						// {"Tuesday Coupons", ""}
				};
				
				// Make sure data hasn't already been loaded
				feeds = ((TexasStudentMediaApp) getApplication()).getFeeds();
				Log.v(TAG, "Items in global array: " + feeds.size());
				if (feeds.size() == 0){
					// Create each feed item and start fetching data
					feeds = new ArrayList<Feed>();
					for(int i=0; i<feed_urls.length; i++){
						Feed feed = new Feed(feed_urls[i][0], feed_urls[i][1]);
						// Set number of items to download until continuing
						feed.setMaxItems(min_items);
						// Start downloading items
						feed.fetchData();
						// Keep track of feeds
						feeds.add(feed);
					}
					
					//Monitor feeds' progress
					/* Now that all feeds are doing their thing, wait until at least one is done
					 * As long as all feeds are downloading their content, they will still be fetching data.
					 * Once at least one feed is done, the for loop will exit normally, causing the while loop to exit
					 * */
					while (true){
						int i=0;
						for(; i<feeds.size(); i++)
							if (!feeds.get(i).isFetchingData())
								i = feeds.size();	// Set i to exit to of loop
						
						// At least one feed finished
						if (i > feeds.size())
							break;
					}
	
					// Feeds are done! Let's save them.
					((TexasStudentMediaApp) getApplication()).setFeeds(feeds);
				}
				
				// Display list of feeds
				try {
					Intent intent = new Intent();
					intent.setClass(SplashActivity.this, TabMenuActivity.class);
					SplashActivity.this.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// No need to fetch data again
				SplashActivity.this.finish();
				this.kill();
			}
		};
		thread.start();
	}

	@SuppressWarnings("unused")
	private void displayErrorMessage(String str) {
	}

	@Override
	public void onStop() {
		// Kill off all threads created
		for(int i=0; i<feeds.size(); i++)
			feeds.get(i).stop();
		thread.kill();
		super.onStop();
	}
}
