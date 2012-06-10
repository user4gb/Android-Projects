package com.texasstudentmedia;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends Activity {
	private final String TAG = "Texas Student Media";
	private Timer timer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		Log.v(TAG, "Inside of " + this.getClass().getName());
		setTitle("Texas Student Media v" + ((TexasStudentMediaApp) getApplication()).getVersion());
		
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
		
		// Load feed manager with the feeds
		for(int i=0; i<feed_urls.length; i++)
			((TexasStudentMediaApp) getApplication()).getFeedManager().addFeed(feed_urls[i][0], feed_urls[i][1]);
		
		// Display splash screen until at least one feed is done loading
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
				if (((TexasStudentMediaApp) getApplication()).getFeedManager().getDoneLoading() == ((TexasStudentMediaApp) getApplication()).getFeedManager().getFeedListSize()){
					// Wait for all feeds to get the minimum amount loaded. We're done here.
					try {
						Intent intent = new Intent();
						intent.setClass(SplashActivity.this, TabMenuActivity.class);
						SplashActivity.this.startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}

					// No need for splash screen again
					SplashActivity.this.finish();
					this.cancel();
				}
			}
		}, 1000, 1000);
	}

	@SuppressWarnings("unused")
	private void displayErrorMessage(String str) {
	}

	@Override
	public void onStop() {
		// Kill off all threads created
		((TexasStudentMediaApp) getApplication()).getFeedManager().stopAllFeeds();
		timer.cancel();
		super.onStop();
	}
}
