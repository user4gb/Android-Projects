package com.texasstudentmedia;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class FeedMenuItemListAdapter extends BaseAdapter{
	private Context context;
	private ArrayList<Feed> feeds;
	
	public FeedMenuItemListAdapter(Context context, ArrayList<Feed> feeds){
		this.context = context;
		this.feeds = feeds;
	}
	
	@Override
	public int getCount() {
		return feeds.size();
	}

	@Override
	public Object getItem(int pos) {
		return feeds.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		return new FeedMenuItem(context, feeds.get(pos));
	}
}
