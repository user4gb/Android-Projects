package com.texasstudentmedia;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.ViewFlipper;


public class TabMenuActivity extends TabActivity{
	private final String TAG = "Texas Student Media";
	private TabHost tab_host;
	@SuppressWarnings("unused")
	private String[] titles = {"Breaking Articles", "Popular Articles", "Live Radio", "Settings"};
	private ListView feed_list;
	private ViewFlipper flipper;
	private Timer loading_timer;
	private int last_feed_selected;
	private boolean was_viewing_subfeed;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);
		Log.v(TAG, "Inside of " + this.getClass().getName());
		setTitle("Texas Student Media v" + ((TexasStudentMediaApp) getApplication()).getVersion());
		
		// Just in case
		last_feed_selected = 0;
		
		// Blame the i*hone for this
		flipper = (ViewFlipper) findViewById(R.id.flipper);
		was_viewing_subfeed = false;
		
		// Setup tabs
		tab_host = getTabHost();
		
		TabSpec ts = tab_host.newTabSpec("SECTIONS")
			.setIndicator("Sections")
			.setContent(R.id.tab1);
		tab_host.addTab(ts);
		ts = tab_host.newTabSpec("BREAKING")
			.setIndicator("Breaking")
			.setContent(R.id.tab23);
		tab_host.addTab(ts);
		ts = tab_host.newTabSpec("POPULAR")
			.setIndicator("Popular")
			.setContent(R.id.tab23);
		tab_host.addTab(ts);
		ts = tab_host.newTabSpec("RADIO")
			.setIndicator("Radio")
			.setContent(R.id.tab4);
		tab_host.addTab(ts);
		ts = tab_host.newTabSpec("SETTINGS")
			.setIndicator("Settings")
			.setContent(R.id.tab5);
		tab_host.addTab(ts);
		tab_host.setCurrentTab(0);
		
		// Setting up list of feeds (for Tab 1)
		feed_list = (ListView) findViewById(R.id.feed_list);
		try{
			feed_list.setAdapter(new FeedMenuItemListAdapter(this, ((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds()));
			feed_list.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
					// Get selected feed's name
					last_feed_selected = pos;
					String feed_name = ((Feed) feed_list.getItemAtPosition(pos)).getName();
					Log.v(TAG, feed_name);
					
					// Setup data in next view
					ListView list = (ListView) findViewById(R.id.feed_view_list);
					list.setAdapter(new FeedViewItemListAdapter(TabMenuActivity.this, (((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds()).get(pos).getItems()));
					list.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
							// Get selected feed's name
							String feed_name = ((Feed) (((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds()).get(last_feed_selected)).getName();
							Log.v(TAG, feed_name);
							
							// Launch feed viewer w/ data from feed
							Intent intent = new Intent();
							// Pass index of current feed
							intent.putExtra("feed_index", last_feed_selected);
							// Pass index of feed item to be viewed
							intent.putExtra("feed_item_index", pos);
							intent.setClass(TabMenuActivity.this, FeedItemActivity.class);
							TabMenuActivity.this.startActivity(intent);
						}
					});

					flipper.setInAnimation(AnimationUtils.loadAnimation(TabMenuActivity.this, R.anim.push_right_in));
					flipper.setOutAnimation(AnimationUtils.loadAnimation(TabMenuActivity.this, R.anim.push_left_out));
					flipper.showNext();

					// Blame the i*hone for this
					was_viewing_subfeed = true;
				}
			});
			loading_timer = new Timer();
			loading_timer.scheduleAtFixedRate(new TimerTask(){
				@Override
				public void run() {
					int num = 0;
					ArrayList<Feed> feeds = ((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds();
					for(int i=0; i<feeds.size(); i++){
						if (feeds.get(i).getState() != Feed.FETCHING){
							//Log.v(TAG, "Removing animation from " + feeds.get(i).getName());
							// Can't modify UI components from other threads...
							// Workaround
							Message msg = new Message();
							msg.arg1 = i;
							messageHandler.sendMessage(msg);
							num++;
						}
					}
					
					if (num == feeds.size()){
						Log.v(TAG, "Removing animation listener");
						this.cancel();
					}
				}
			}, 1000, 1000);
		}catch (Exception e){
			e.printStackTrace();
			Log.e(TAG, "", e);
		}
	}
	
	private Handler messageHandler = new Handler() {
	      @Override
	      public void handleMessage(Message msg) {  
	  		try{
	  			((FeedMenuItem) feed_list.getChildAt(msg.arg1)).removeLoadingAnim();
	  		}catch (Exception e){
	  			Log.e(TAG, "", e);
	  		}
	      }

	  };

			/*
			// Add Tab Listener to set the various FeedView instances accordingly
			tab_host.setOnTabChangedListener(new TabHost.OnTabChangeListener(){
				@Override
				public void onTabChanged(String tabId) {
					try{	
						Log.v(TAG, "Switching to: " + tabId);
						
						// Set title
						TextView title = (TextView) findViewById(R.id.tab_title);
						if (tab_host.getCurrentTab() == 0){
							// Add image
							title.setText("TEXAS STUDENT MEDIA");
							
							// Resume sub-feed view
							if (was_viewing_subfeed == true){
								Log.v(TAG, "Resuming sub-view display");

								// Get selected feed's name
								String feed_name = ((Feed) feed_list.getItemAtPosition(last_feed_selected)).getName();
								Log.v(TAG, feed_name);
								
								// Setup data in next view
								ListView list = (ListView) findViewById(R.id.feed_view_list);
								list.setAdapter(new FeedViewItemListAdapter(TabMenuActivity.this, (((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds()).get(last_feed_selected).getItems()));
								list.setOnItemClickListener(new OnItemClickListener(){
									@Override
									public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
										// Get selected feed's name
										String feed_name = ((Feed) (((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds()).get(last_feed_selected)).getName();
										Log.v(TAG, feed_name);
										
										// Launch feed viewer w/ data from feed
										Intent intent = new Intent();
										// Pass index of current feed
										intent.putExtra("feed_index", last_feed_selected);
										// Pass index of feed item to be viewed
										intent.putExtra("feed_item_index", pos);
										intent.setClass(TabMenuActivity.this, FeedItemActivity.class);
										TabMenuActivity.this.startActivity(intent);
									}
								});

								flipper.setInAnimation(AnimationUtils.loadAnimation(TabMenuActivity.this, R.anim.push_right_in));
								flipper.setOutAnimation(AnimationUtils.loadAnimation(TabMenuActivity.this, R.anim.push_left_out));
								flipper.setDisplayedChild(1);
							}
							
							return;
						}else
							title.setText(titles[tab_host.getCurrentTab()-1]);

						// A simple precaution
						//was_viewing_subfeed = false;
						
						// Lets make some lists
						final ListView list = (ListView) findViewById(R.id.feed_view_list);
						final int ndx = tabId.equals("BREAKING") ? 0 : tabId.equals("POPULAR") ? 1 : -1;
						if (ndx == -1)
							return;
						
						list.setAdapter(new FeedViewItemListAdapter(TabMenuActivity.this, (((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds()).get(ndx).getItems()));
						list.setOnItemClickListener(new OnItemClickListener(){
							@Override
							public void onItemClick(AdapterView<?> av, View v, int pos, long id) {
								// Get selected feed's name
								String feed_name = ((Feed) (((TexasStudentMediaApp) getApplication()).getFeedManager().getFeeds()).get(ndx)).getName();
								Log.v(TAG, feed_name);
								
								// Launch feed viewer w/ data from feed
								Intent intent = new Intent();
								// Pass index of current feed
								intent.putExtra("feed_index", ndx);
								// Pass index of feed item to be viewed
								intent.putExtra("feed_item_index", pos);
								intent.setClass(TabMenuActivity.this, FeedItemActivity.class);
								TabMenuActivity.this.startActivity(intent);
							}
						});
					}catch (Exception e){
						Log.e(TAG, "Failed to change tabs correctly", e);
					}
				}
			});
			
			// Set tab width
			//tab_host.getTabWidget().getChildAt(0).getLayoutParams().width = 100;
			
		}catch (Exception e){
			e.printStackTrace();
			Log.e(TAG, "", e);
		}
	}*/
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && was_viewing_subfeed && tab_host.getCurrentTab() == 0) {
			// Lets go back to the feed list
			flipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
			flipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
			flipper.showPrevious();

			// Reset back button
			was_viewing_subfeed = false;
			
			// Stop the propagation (not sure if this matters)
	    	return false;
	    }else
	    	return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
}