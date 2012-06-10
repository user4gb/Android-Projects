package com.avai.wpzoo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.flurry.android.FlurryAgent;

public class PageActivity extends Activity {
	private final int MAP = 2;
    private int landmarkId = -1;
    private static final String TAG = "PageActivity";
	@Override
	public void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, Constants.sharedConstants().flurryKey);
       
	   // Report a Flurry event
       HashMap<String, String> map = new HashMap<String, String>();
       int Id = getIntent().getIntExtra("Id", 0); 
       map.put("Id", Integer.toString(Id));
       
       Log.d(TAG, "Reporting an ItemVisited event with id: " + Id + " to Flurry");
       FlurryAgent.setLogEvents(true);
       FlurryAgent.onEvent("ItemVisited");
	}
	
	@Override
	public void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	} 

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle((CharSequence)(getIntent().getStringExtra("Name")));
        
        // If this page has a social networking message, prompt the user for posting.
        if(HttpHelper.networkAvailable(getApplicationContext())) {
        	if(savedInstanceState == null) {
	        	String socialNetworkingMessage = DatabaseHelper.getItemExtraProperty(getIntent().getIntExtra("Id", 0), "SocialNetworkingMessage");
		        Log.d(TAG, "socialNetworkingMessage: " + socialNetworkingMessage);
		        FacebookHelper.sharedHelper(this).promptToPost(socialNetworkingMessage);
		        TwitterHelper.sharedHelper(this).promptToTweet(this, socialNetworkingMessage);
	        }
        }
        
        // Check if there is a location associated with this page
        int Id = getIntent().getIntExtra("Id", 0); 
        Log.d(TAG, "Id: "+Id);
        if(getIntent().getStringExtra("ItemType").equals("Location")) {
        	landmarkId = Id;
        }
        else if(getIntent().getStringExtra("ItemType").equals("Event")){
        	Log.d(TAG, "ItemType is Event, Id is: "+Id);
        	landmarkId = DatabaseHelper.getLocationIdForEventId(Id);
        	Log.d(TAG, "getLocationIdForEventId("+Id+") returns "+landmarkId+" for landmarkId");
        }
        else {
        	landmarkId = DatabaseHelper.getLocationIdForId(Id);
        }
        
        // Set up webview
        WebView webview = new WebView(this); 
        webview.setBackgroundColor(0);
        webview.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
        webview.getSettings().setJavaScriptEnabled(true);
        
        setContentView(webview);
        
        // Check if the content has been saved as html
        String fileName = String.format("%d.html", Id);
        String fileUrl = "file://" + this.getFilesDir() + "/" + fileName;
        File file = new File(fileUrl);
        if(!file.exists()) {
	        //If not, get the content from the intent, format and save it.
	        String content = getIntent().getStringExtra("Content");
	        
	        content = getReformattedContent(getApplicationContext(), Id, content);
	        Log.d(TAG, "reformatted content: "+content);
	        InputStream iStream = null;
			try {
				iStream = new ByteArrayInputStream(content.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException thrown trying to convert the content to UTF-8");
			}
	        HttpHelper.save(getApplicationContext(), iStream, fileName);
        }
        
        Log.d(TAG, "Loading fileUrl: "+fileUrl);
        webview.loadUrl(fileUrl);
        
        webview.setWebViewClient(new WebViewClient() {
        	@Override
        	public  boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http://www.youtube.com/")){
                     startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                     return true;
                } else {
                     return false;
                }
            }
        	
        	@Override
        	public void onPageFinished(WebView view, String url) {
        		
        	}
        });
    }
	
	public static String getReformattedContent(Context ctx, int itemId, String content) {
		// Replace all percent signs with their html equivalent so the data is not interpreted as a URL.
        content = content.replaceAll("%(?![0-9*])", "&#37;");
        // Download images locally and replace src path	
        Pattern p = Pattern.compile("(?:<img.*?src=\")([^\"]*)(?:\".*?>)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(content);
        Log.v(TAG, "Printing all matches...");
        while(m.find()) {
        	String remoteUrl = m.group(1);
        	Log.v(TAG, remoteUrl + "    ");
        	String[] urlPieces = remoteUrl.split("/");
     		String imageName = new Integer(itemId).toString() + "_" + urlPieces[urlPieces.length-1];
     		imageName = imageName.replaceAll("%[0-9][0-9]", "_");
     		Log.v(TAG, "imageName: "+imageName);
     		ImageFinder.preloadImage(ctx, imageName, remoteUrl);
     		content = content.replaceAll(remoteUrl, imageName);
		}
        
        // Replace embedded YouTube objects with links.
        Pattern objectPattern = Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE);
        Matcher objectMatcher = objectPattern.matcher(content);
        while(objectMatcher.find()) {
        	Log.v(TAG, "Found an object tag set");
        	String youTubeObject = objectMatcher.group();
    		Pattern linkPattern = Pattern.compile("(?:<embed.*?src=\")([^\"]*)(?:\".*?>)", Pattern.CASE_INSENSITIVE);
    		Matcher linkMatcher = linkPattern.matcher(youTubeObject);
    		while(linkMatcher.find()) {
	    		String youTubeAddress = linkMatcher.group(1);
	    		String[] addressPieces = youTubeAddress.split("/");
	    		String videoId = addressPieces[addressPieces.length-1].split("&")[0];
	    		
	    		String thumbnailAddress = "http://img.youtube.com/vi/" + videoId + "/0.jpg";
	    		Log.v(TAG, "thumbnailAddress: "+thumbnailAddress);
	    		String youTubeLink = "YouTube video - Tap to view<br> <a href=\"" + youTubeAddress + "\"><img width=\"300\" src=\"" + thumbnailAddress + "\"></a>";
	    		content = content.replaceAll(youTubeObject, youTubeLink);
    		}
    		
        }
        
        // What a YouTube video starts as:
        //content = "<object height=\"240\" width=\"300\"><param name=\"movie\" value=\"http://www.youtube.com/v/txzcPyj_6x0\"><param name=\"allowFullScreen\" value=\"true\"><param name=\"allowscriptaccess\" value=\"always\"><embed src=\"http://www.youtube.com/v/txzcPyj_6x0\" type=\"application/x-shockwave-flash\" allowscriptaccess=\"always\" allowfullscreen=\"true\" height=\"240\" width=\"300\"></object></body>";
        
        // What a YouTube video should look like:
        //content = "YouTube video - Tap to view<br> <a href=\"http://www.youtube.com/v/txzcPyj_6x0\"><img width=\"300\" src=\"http://img.youtube.com/vi/txzcPyj_6x0/0.jpg\"></a>";
        
        //content = "<img src=\"content://com.avai.amp/37_WZ_head_home.jpg\">";   
        //Check for youtube videos
        //if(content.contains("<embed "))
        //	content = 
        
        //Add stylesheet
        SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
        Cursor cursor = sharedDb.query("AppDomainSettings", new String[]{"Value"}, "Name=\"DefaultStyleSheet\"", null, null, null, null);
        String stylesheet = "";
        while(cursor.moveToNext()){
        	stylesheet = cursor.getString(cursor.getColumnIndex("Value"));
        }
        cursor.close();
        if(content.startsWith("<body>"))
        	content = content.substring(6);
        if(content.endsWith("</body>"))
        	content = content.substring(0,content.length()-7);
        content = "<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"" + stylesheet + "\" /></head><body style=\"margin-top:0px;margin-left:0px;\">"+content+"</body></html>";
        return content;
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    if(landmarkId >= 0) {
	    	menu.add(0, MAP, 0, "Map").setIcon(android.R.drawable.ic_menu_compass);
	    	return true;
	    }
	    return false;
	}
	
	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		    case MAP:
		    	Intent intent = new Intent().setClass(this, AmpMapActivity.class);
		    	intent.putExtra("Id", DatabaseHelper.getMapId(landmarkId));
		    	intent.putExtra("LandmarkId", landmarkId);
		    	intent.putExtra("LandmarkName", getIntent().getStringExtra("Name"));
		    	startActivity(intent);
		        return true;
	    }
	    return false;
	}
	

}
