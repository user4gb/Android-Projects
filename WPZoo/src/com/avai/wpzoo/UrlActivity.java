package com.avai.wpzoo;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.flurry.android.FlurryAgent;

public class UrlActivity extends Activity {

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
        
        // Report a Flurry event
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Id", Integer.toString(getIntent().getIntExtra("Id", 0)));
        FlurryAgent.onEvent("ItemVisited", map);
        
        WebView webview = new WebView(this);
        setContentView(webview);
        
        String url = getIntent().getStringExtra("Content");
        webview.loadUrl(url);
    }
} 
