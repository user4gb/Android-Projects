package com.avai.wpzoo;

import org.apache.http.auth.UsernamePasswordCredentials;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

public class TwitterHelper {
    private Context mCtx;
	private static TwitterHelper ref;
	public UsernamePasswordCredentials creds;
    public boolean loggedIn;

    // Constructor
	private TwitterHelper(Context ctx) {
		this.mCtx = ctx;
		SharedPreferences settings = mCtx.getSharedPreferences(Constants.sharedConstants().prefsFileName, 0);
	    String username = settings.getString("username", null);
	    String password = settings.getString("password", null);
	    if(username != null && password != null) {
	    	this.creds = new UsernamePasswordCredentials(username, password);
	    	loggedIn = true;
	    }
	    else {
	    	loggedIn = false;
	    }
	}
    
	public static TwitterHelper sharedHelper(Context ctx) {
    	if(ref == null) {
    		ref = new TwitterHelper(ctx);
    	}
    	return ref;
    }
	
    public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }
    
    public boolean setCredentials(String username, String password) {
    	this.creds = new UsernamePasswordCredentials(username, password);
    	boolean success  = (HttpHelper.get(mCtx, "http://twitter.com/account/verify_credentials.xml", null, creds) != null);
    	if(success) {
        	//Save credentials in shared preferences
        	SharedPreferences settings = mCtx.getSharedPreferences(Constants.sharedConstants().prefsFileName, 0);
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putString("username", username);
        	editor.putString("password", password);
        	editor.commit();
    		this.loggedIn = true;
    	}
    	return this.loggedIn;
    }
    
    public boolean postTweet(String tweet) {
    	if(!this.loggedIn) {
    		System.out.println("Credentials must be entered on the profile page before you can make twitter requests");
    		return false;
    	}
    	return (HttpHelper.get(mCtx, "http://twitter.com/statuses/update.xml", "status=" + tweet, creds) != null);
    }

	public void logout() {
		this.loggedIn = false;
		this.creds = null;
	}

	public void promptToTweet(Context ctx, final String message) {
		System.out.println("message: " + message + "   loggedIn:" + loggedIn);
		if(message != null && loggedIn) {
			AlertDialog alertDialog = new AlertDialog.Builder(ctx).create();
        	alertDialog.setTitle("Would you like to post to twitter?");
			alertDialog.setMessage(message);
        	alertDialog.setCancelable(false);
        	alertDialog.setButton("Not now.", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int id) {
	        	dialog.cancel();
	        	}
        	});
        	alertDialog.setButton2("Tweet!", new DialogInterface.OnClickListener() {
	        	public void onClick(DialogInterface dialog, int id) {
	        		postTweet(message);
	        	}
        	});
        	alertDialog.setIcon(Constants.sharedConstants().appIconId);
        	alertDialog.show();
		}
	}
}
