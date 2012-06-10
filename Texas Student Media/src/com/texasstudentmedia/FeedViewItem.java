package com.texasstudentmedia;

import android.content.Context;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

public class FeedViewItem extends TableLayout{
	public FeedViewItem(Context context, FeedItem item) {
		super(context);
		
		this.setTag(item);
		
		View v = inflate(context, R.layout.feed_view_item, null);
		
		TextView name = (TextView)v.findViewById(R.id.feed_preview_item_name);
		name.setText(item.getTitle());
		
		addView(v);
	}

}
