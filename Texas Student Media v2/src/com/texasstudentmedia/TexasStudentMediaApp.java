package com.texasstudentmedia;

import android.app.Application;

public class TexasStudentMediaApp extends Application {
	private FeedManager feed_manager = new FeedManager();
    private String version = "0.5.0";

    public FeedManager getFeedManager() {
    	return feed_manager;
    }

	public String getVersion() {
		return version;
	}
}