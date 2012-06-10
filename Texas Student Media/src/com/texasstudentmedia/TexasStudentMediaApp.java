package com.texasstudentmedia;

import java.util.ArrayList;

import android.app.Application;

public class TexasStudentMediaApp extends Application {
    private ArrayList<Feed> feeds = new ArrayList<Feed>();
    private String version = "0.4.3";

    public ArrayList<Feed> getFeeds() {
        return feeds;
    }

    public void setFeeds(ArrayList<Feed> feeds) {
        this.feeds = feeds;
    }

	public String getVersion() {
		return version;
	}
}