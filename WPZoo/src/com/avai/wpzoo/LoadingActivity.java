package com.avai.wpzoo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.flurry.android.FlurryAgent;

public class LoadingActivity extends Activity implements Runnable {
    private final int APP_DOMAIN = 5;
    private final String DB_NAME = "amp";
    private final String HOSTNAME = "http://amp.avai.com/";
    
    final Handler mHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
			ProgressBar bar = (ProgressBar) findViewById(R.id.progressBar);
			bar.setMax(12); 
			bar.setProgress(msg.arg1);
    	}
    };
    Thread mSplashThread; 
 
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
		
	   	System.out.println("LoadingActivity onCreate 0");
	   	//Initialize constants
    	Constants.sharedConstants(getApplicationContext(), DB_NAME, HOSTNAME, APP_DOMAIN);
	   	
    	System.out.println("LoadingActivity onCreate 1");
    	
	   	setContentView(R.layout.splash);
	   	
	   	ImageView splash = (ImageView) findViewById(R.id.splash_image_view);
		splash.setImageDrawable(Constants.sharedConstants().splashImage);
    	
		// Resume facebook session      
		FacebookHelper.sharedHelper(this).session.resume(this);
		
		System.out.println("LoadingActivity onCreate 2");
		
		mSplashThread = new Thread(this);
        mSplashThread.start();
        
        System.out.println("LoadingActivity onCreate 3");
	}

	public void run() {
        // Sync content
        ContentSyncer syncer = new ContentSyncer(getApplicationContext());
        String surveyService = Constants.sharedConstants().hostName + "Data/Survey.svc/app/" + Constants.sharedConstants().appDomainId;
        String contentService = Constants.sharedConstants().hostName + "Data/Content.svc/app/" + Constants.sharedConstants().appDomainId;
        
        while(!DatabaseHelper.databaseReady());
        syncer.syncWebService("AppDomainSettings", contentService + "/appdomainsettings/since/%d", "Id", "Revision");
        updateProgress(1);
        syncer.syncWebService("Image", contentService + "/images/since/%d", "Id", "Revision");
        updateProgress(2);
        syncer.syncWebService("Item", contentService  + "/items/since/%d", "Id", "Revision");
        updateProgress(3);
        syncer.syncWebService("ItemExtraProperties", contentService + "/itemextraproperties/since/%d", "Id", "Revision");
        updateProgress(4);
        syncer.syncWebService("ItemKeywords", contentService +"/itemkeywords/since/%d", "ID", "Revision");
        updateProgress(5);
        syncer.syncWebService("ItemLocation", contentService + "/itemlocations/since/%d", "Id", "RevisionNumber");
        updateProgress(6);
        syncer.syncWebService("ItemSubItem", contentService + "/itemrelationships/since/%d", "Id", "Revision");
        updateProgress(7);
        syncer.syncWebService("Keywords", contentService + "/keywords/since/%d", "Id", "Revision");
        updateProgress(8);
        syncer.syncWebService("Location", contentService + "/locations/since/%d", "Id", "Revision");
        updateProgress(9);
        syncer.syncWebService("SurveyAnswer", surveyService + "/surveys/answers/since/%d", "Id", "Revision");
        updateProgress(10);
        syncer.syncWebService("SurveyQuestion", surveyService + "/surveys/questions/since/%d", "Id", "Revision");
        updateProgress(11);
	    
        // Set default timezone to GMT.
	    TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	    SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
        syncer.syncWebService("Event", contentService + "/events/since/%d?startDate=" + sdf.format(new Date())  + "&numDays=7", "Id", "Revision");
        updateProgress(12);
        finish();
        startActivity(new Intent().setClass(getApplicationContext(), MainActivity.class));
    }
	
	private void updateProgress(int i) {
        Message msg = Message.obtain();
        msg.arg1 = i;
        mHandler.sendMessage(msg);
	}
}
