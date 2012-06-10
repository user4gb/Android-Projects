package com.avai.wpzoo;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuAdapter extends BaseAdapter {
	private Context ctx;
	private LayoutInflater mInflater;
    @SuppressWarnings("unchecked")
	private ArrayList menuItems;
    
	@SuppressWarnings("unchecked")
	public MenuAdapter(Context context, ArrayList items) {
		mInflater = LayoutInflater.from(context);
		menuItems = items;
		ctx = context;
	}
	
	public int getCount() {
		return menuItems.size();
	}
	
	public int getViewTypeCount() {
		return 2; //Headers and Menu Items
	}

	public Object getItem(int position) {
		return position;
	}
	
	@SuppressWarnings("unchecked")
	public int getItemViewType(int position) {
		HashMap row = (HashMap) menuItems.get(position);
		if(row.get("ItemType").equals("Header")) {
			return 0;
		}
		else
			return 1;
	}

	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("unchecked")
	public View getView(int position, View convertView, ViewGroup parent) {
		HashMap row = (HashMap) menuItems.get(position);
		if(row.get("ItemType").equals("Header")) {
			HeaderViewHolder headerHolder;
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.cell_header, null);
				headerHolder = new HeaderViewHolder();
				headerHolder.webview = (WebView) convertView.findViewById(R.id.webview);
				convertView.setTag(headerHolder);
			} else {

				headerHolder = (HeaderViewHolder) convertView.getTag();
			}
			headerHolder.webview.setHorizontalScrollBarEnabled(false);
			String content = (String) row.get("Content");
			String html = "<html><head><style>* {margin:0;padding:0;}</style></head><body>"+content+"</body></html>";
			headerHolder.webview.loadData(html, "text/html", "utf-8");
		} else {
			MenuViewHolder menuHolder;
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.cell_menu, null);
				menuHolder = new MenuViewHolder();
				menuHolder.text = (TextView) convertView.findViewById(R.id.name);
				menuHolder.icon = (ImageView) convertView.findViewById(R.id.image);
				menuHolder.nextarrow = (ImageView) convertView.findViewById(R.id.nextarrow);		
				convertView.setTag(menuHolder);
			} else {
				menuHolder = (MenuViewHolder) convertView.getTag();
			}
			
			menuHolder.text.setText((CharSequence) row.get("Name"));
			if(row.get("ItemType").equals("NoItems")) {
				menuHolder.icon.setImageDrawable(null);
				menuHolder.nextarrow.setImageDrawable(null);
			} else if(row.get("ItemType").equals("SurveyAnswer")) {
				menuHolder.icon.setImageDrawable(ctx.getResources().getDrawable(((Integer)row.get("CheckmarkDrawable")).intValue()));
				menuHolder.nextarrow.setImageDrawable(null);
			} else {
				menuHolder.icon.setImageDrawable(ImageFinder.getDrawable(ctx, (String)row.get("ImageFileName"), (String)row.get("ImageUrl")));
				menuHolder.nextarrow.setImageResource(R.drawable.com_nextarrow);
			}
			convertView.setBackgroundDrawable(Constants.sharedConstants().cellGradient);
		}
		return convertView;
	}
	
	static class MenuViewHolder {
		TextView text;
		ImageView icon;
		ImageView nextarrow;
	}
	static class HeaderViewHolder {
		WebView webview;
	}
	
}