package com.texasstudentmedia;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class FeedViewItemListAdapter extends BaseAdapter{
	private Context context;
	private ArrayList<FeedItem> items;
	
	public FeedViewItemListAdapter(Context context, ArrayList<FeedItem> items){
		this.context = context;
		this.items = items;
	}
	
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int pos) {
		return items.get(pos);
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {
		return new FeedViewItem(context, items.get(pos));
	}
}
