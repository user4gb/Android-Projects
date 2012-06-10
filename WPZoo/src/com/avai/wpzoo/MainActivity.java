package com.avai.wpzoo;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.TabActivity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.flurry.android.FlurryAgent;

public class MainActivity extends TabActivity {
  	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, Constants.sharedConstants().flurryKey);
	}
	
	@Override
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	} 
  
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

		// Create the tab bar
	    TabHost tabHost = getTabBar();
	    
	    // Response to tab changes
	    tabHost.setOnTabChangedListener(new OnTabChangeListener() {
	    	public void onTabChanged(String arg0) {
	    		System.out.println("Tab Changed: " + arg0);
	    	}
	    });
	    
	    if(!HttpHelper.networkAvailable(getApplicationContext())) {
        	if(savedInstanceState == null) {
        		HttpHelper.presentAlert(this, getResources().getText(R.string.no_network_connection), getResources().getText(R.string.recommend_network_for_benefits));
        	}
	    } else {
		    //Find list of images, and spawn a thread to down load images
		    ArrayList<HashMap<String, Object>> images = ImageFinder.getUpdatedImageList();
		    new DownloadImageTask().execute(images);
	    }
	    
    }
    
	
	@SuppressWarnings("unchecked")
	private TabHost getTabBar(){
        //Initialize the tab bar
        @SuppressWarnings("unused")
		Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Reusable TabSpec for each tab
        
        //Find the root item
        SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
        Cursor cursor = sharedDb.query("Item", new String[]{"Name", "_id"}, "Name=\"Root\"", null, null, null, null);
       	int root = 0;
        while(cursor.moveToNext()){
    	   root = Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")));
       	}
        cursor.close();
        
        //Find the tab items under the root item
        ArrayList tabItems = NavigationHelper.getMenuItems(root);
        for(int i=0;i<tabItems.size();i++){
        	HashMap tabItem = (HashMap) tabItems.get(i);
        	String imageName = (String) tabItem.get("ImageFileName");
        	spec = tabHost.newTabSpec("tab"+ ((Integer)i).toString());
        	Drawable icon = ImageFinder.getDrawable(getApplicationContext(), imageName, (String) tabItem.get("ImageUrl"));
 
            spec.setIndicator((CharSequence)tabItem.get("Name"), icon);
        	Intent intent = NavigationHelper.getIntentForItem(getApplicationContext(), tabItem);
            spec.setContent(intent);
            tabHost.addTab(spec);
        }
        return tabHost;
	}
	
	@SuppressWarnings("unchecked")
	private class DownloadImageTask extends AsyncTask<ArrayList, HashMap, Integer> {
		@Override
		protected Integer doInBackground(ArrayList... imageList) {
			ArrayList<HashMap<String, Object>> images = imageList[0];
			for(int i=0;i<images.size();i++){
				String url = (String) ((HashMap<String, Object>) images.get(i)).get("Url");
				url = url.replaceAll(" ", "%20");
				InputStream sb = HttpHelper.get(getApplicationContext(), url);
				String[] splitUrl = url.split("/");
				String imageName = splitUrl[splitUrl.length-1];
				if(!HttpHelper.save(getApplicationContext(), sb, imageName))
					System.out.println("Failed to save " + imageName);
				else
					publishProgress(images.get(i));
			}
			return images.size();
		}
		
		@Override
		protected void onProgressUpdate(HashMap... image) {
			SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
			ContentValues values = new ContentValues();
			values.put("Location", "filesDir");
			sharedDb.update("Image", values, "_id="+image[0].get("_id"), null);	
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			System.out.println("Downloaded " + result + " images.");
		}
	}	
}