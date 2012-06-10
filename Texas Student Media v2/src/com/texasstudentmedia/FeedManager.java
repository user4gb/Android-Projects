package com.texasstudentmedia;

import java.util.ArrayList;

import android.util.Log;


public class FeedManager {
	private final String TAG = "Texas Student Media";
	private final int min_items = 10;
	private ArrayList<Feed> feeds;

	public FeedManager(){
		feeds = new ArrayList<Feed>();
	}
	
	public void addFeed(String name, String url){
		Feed new_feed = new Feed(name, url);
		if (new_feed.loadFromMemory() == false){
			// Start retrieving data and save it when done
			new_feed.loadFromURL(min_items);
		}
		feeds.add(new_feed);
	}
	public boolean isEmpty(){
		return feeds.size() == 0;
	}
	public int getFeedListSize(){
		return feeds.size();
	}
	public ArrayList<Feed> getFeeds(){
		return feeds;
	}
	
	public int getMimimumDoneLoading(){
		int num=0;
		for(int i=0; i<feeds.size(); i++)
			if (feeds.get(i).getState() != Feed.FETCHING)
				num++;
		
		return num;
	}
	public int getDoneLoading(){
		int num=0;
		for(int i=0; i<feeds.size(); i++)
			if (feeds.get(i).getState() == Feed.IDLE)
				num++;
		
		return num;
	}
	
	public void refreshFeed(int ndx, int min){
		feeds.get(ndx).loadFromURL(min);
	}
	public void refreshFeed(String name, int min){
		for(int i=0; i<feeds.size(); i++){
			if (feeds.get(i).getName().equals(name)){
				refreshFeed(i, min);
				return;
			}
		}
		Log.v(TAG, "\"" + name + "\" not found in feeds");
	}
	
	public int getFeedStatus(int ndx){
		return feeds.get(ndx).getState();
	}
	public int getFeedStatus(String name){
		for(int i=0; i<feeds.size(); i++){
			if (feeds.get(i).getName().equals(name)){
				return getFeedStatus(i);
			}
		}
		Log.v(TAG, "\"" + name + "\" not found in feeds");
		return -1;
	}
	
	public void stopAllFeeds(){
		for(int i=0; i<feeds.size(); i++)
			feeds.get(i).stop();
	}
}