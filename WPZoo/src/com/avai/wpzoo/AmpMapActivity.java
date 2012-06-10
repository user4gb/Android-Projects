package com.avai.wpzoo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

public class AmpMapActivity extends Activity implements LocationListener, SensorListener{

	private LocationManager locationManager;
	private String bestProvider;
	private AmpMapView mapView;
	MyPoint currLocUgs;
	private Timer friendUpdateTimer;
	private Context mCtx;
	private static final String TAG = "AmpMapActivity";
	
	private final int SEARCH = 1;
	private final int NEAR_ME = 2;
	private static int mapId = 0;
    
	@Override 
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "AmpMapActivity onCreate");
        this.mCtx = getApplicationContext();
        
        /*
        if(Intent.ACTION_VIEW.equals(getIntent().getAction())) {
        	Intent intent = NavigationHelper.getIntentForItem(getApplicationContext(), 445);  //elk exhibit
        	if(intent != null)
        		startActivity(intent);
        }
        else*/
        if(Intent.ACTION_SEARCH.equals(getIntent().getAction())) { 
        	Log.d(TAG, "rootId: "+mapId);
            Intent intent = NavigationHelper.getIntentForItem(getApplicationContext(), mapId);
            intent.setClass(this, LocationsActivity.class);
            intent.putExtra(SearchManager.QUERY, getIntent().getStringExtra(SearchManager.QUERY));
            if(intent != null) {
            	startActivity(intent);
            }
		}
        
        if(getIntent().getIntExtra("Id", 0) != 0)
        	mapId = getIntent().getIntExtra("Id", 0);
    	
        // Report a Flurry event
        HashMap<String, String> flurryMap = new HashMap<String, String>();
        flurryMap.put("Id", Integer.toString(mapId));
        FlurryAgent.onEvent("ItemVisited", flurryMap);
        	
        //Set the content view
        setContentView(R.layout.map);
		// Create the mapView
    	// Austin Area    	
/*      MyPoint ref1Ugs = new MyPoint(30.3362, -97.8278);
        MyPoint ref1Pxl = new MyPoint(0,0);
        MyPoint ref2Ugs = new MyPoint(30.2197, -97.6723);
        MyPoint ref2Pxl = new MyPoint(907, 787);
*/          	
    	// AVAI Office
/*      MyPoint ref1Ugs = new MyPoint(30.243367, -97.800075);  //Building 1 Back Door
        MyPoint ref1Pxl = new MyPoint(376,157);
        MyPoint ref2Ugs = new MyPoint(30.242553, -97.798281);  //Building 3 Back Door
        MyPoint ref2Pxl = new MyPoint(875,397);
*/
    	// Get map from web service

        MyPoint ref1Ugs = Constants.sharedConstants().ref1Ugs;
        MyPoint ref1Pxl = Constants.sharedConstants().ref1Pxl;
        MyPoint ref2Ugs = Constants.sharedConstants().ref2Ugs;
        MyPoint ref2Pxl = Constants.sharedConstants().ref2Pxl;

    	mapView = new AmpMapView(getApplicationContext(), ref1Ugs, ref1Pxl, ref2Ugs, ref2Pxl);

        FrameLayout layout = (FrameLayout) findViewById(R.id.map_frame);
        layout.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
        layout.addView(mapView);
        layout.bringChildToFront((TextView) findViewById(R.id.status_text));
        
        //Get the location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	}
	
	@Override
	protected void onStart()
	{
	   super.onStart();
	   FlurryAgent.onStartSession(this, Constants.sharedConstants().flurryKey);
	} 
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "AmpMapActivity onResume");
		
//		Bitmap map = BitmapFactory.decodeResource(getResources(), R.drawable.austin_map);
//    	Bitmap map = BitmapFactory.decodeResource(getResources(), R.drawable.avai_map);
		
		String mapUrl = DatabaseHelper.getItemExtraProperty(mapId, "MapUrl");
    	String[] urlPieces = mapUrl.split("/");
    	String imageName = urlPieces[urlPieces.length-1];
    	Bitmap map = ImageFinder.getBitmap(getApplicationContext(), imageName, mapUrl);

		mapView.setBitmap(map);
		
		// Put current location on map
		if(!FriendFinderActivity.locationAvailable(this)) {
			HttpHelper.presentAlert(this, getResources().getText(R.string.location_unavailable), getResources().getText(R.string.maps_location_unavailable));
		} else {
			// Update the map with the last known location			
			bestProvider = locationManager.getBestProvider(new Criteria(), true);
			//Toast.makeText(getApplicationContext(), "Getting last known location from: "+bestProvider, Toast.LENGTH_SHORT).show();
			Location loc = locationManager.getLastKnownLocation(bestProvider);
			if(loc!=null) {
				currLocUgs = new MyPoint(loc.getLatitude(), loc.getLongitude());
				currLocUgs.println("lastKnownLocation: ");
				mapView.updateCurrentLocation(currLocUgs, loc.getAccuracy());
			}
			
	        // Start location listener
			locationManager.removeUpdates(this);
			locationManager.requestLocationUpdates("network", 3000, 0, this);
			locationManager.requestLocationUpdates("gps", 3000, 0, this);
		}	
			
		// Start periodic friend location updates
	   	startPeriodicFriendLocationUpdates();
   	
        // Add landmark if loading from a location aware page
        int landmarkId = getIntent().getIntExtra("LandmarkId", 0);
        if(landmarkId != 0) {
        	mapView.removeAllLandmarks();
        	mapView.addLandmark(DatabaseHelper.getLocationForItem(landmarkId), getIntent().getStringExtra("LandmarkName"));
        }
		
		// Start the compass
		((SensorManager) getSystemService(SENSOR_SERVICE)).registerListener(this,
                SensorManager.SENSOR_ORIENTATION |SensorManager.SENSOR_ACCELEROMETER,
                SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		
		// Stop friend location updates
		friendUpdateTimer.cancel();
		
		// Stop the compass
		((SensorManager) getSystemService(SENSOR_SERVICE)).unregisterListener(this);
		
		// Stop the location listener
		locationManager.removeUpdates(this);
		
		// Release the map object
		FrameLayout layout = (FrameLayout) findViewById(R.id.map_frame);
        
        mapView.releaseMap();
	}
	
	@Override
	protected void onStop()
	{
	   super.onStop();
	   FlurryAgent.onEndSession(this);
	}

	public void onSensorChanged(int sensor, float[] values) {
		switch (sensor) {
    	    case SensorManager.SENSOR_ORIENTATION:
    	    	mapView.updateCurrentBearing(values[0]);	        	        
    	        break;
    	    case SensorManager.SENSOR_ACCELEROMETER:
    	        break;
    	    default: 
    	        break;
	    }   
	}
	
    // Location Listener methods
	public void onLocationChanged(Location loc) {
		//Toast.makeText(getApplicationContext(), "Location changed via provider: " + loc.getProvider(), Toast.LENGTH_SHORT).show();
		currLocUgs = new MyPoint(loc.getLatitude(), loc.getLongitude());
		currLocUgs.println("onLocationChanged: ");
        mapView.updateCurrentLocation(currLocUgs, loc.getAccuracy());
	}

	public void onProviderDisabled(String provider) {
		//Toast.makeText(getApplicationContext(), provider+" disabled.", Toast.LENGTH_SHORT).show();
	}

	public void onProviderEnabled(String provider) {
		//Toast.makeText(getApplicationContext(), provider+" enabled.", Toast.LENGTH_SHORT).show();
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		
	}
	
	public void onAccuracyChanged(int sensor, int accuracy) {

	}
	
	private void startPeriodicFriendLocationUpdates() {
		// Start periodic friend location updates and display them on the map.
    	Log.d(TAG, "starting periodic friend location updates");
     	int updatePeriod = Constants.sharedConstants().friendLocationUpdateInterval;
     	friendUpdateTimer = new Timer();
		friendUpdateTimer.schedule(new FriendLocationUpdaterTask(), 0, updatePeriod * 1000);
	}
	
	private class FriendLocationUpdaterTask extends TimerTask {
		public void run() {
			Log.d(TAG, "updating friend locations!");
			Cursor cursor = DatabaseHelper.sharedDb().query("Friends", new String[]{"Pin"}, null, null, null, null, null);
			while(cursor.moveToNext()) {
	        	Integer pin = new Integer(cursor.getInt(cursor.getColumnIndex("Pin")));
	        	Log.d(TAG, "Found friend in database with pin: "+pin);
	        	
	        	String url = Constants.sharedConstants().friendFinderServiceName + pin.intValue() + "/currentlocation";
//	        	AsyncTask<Object, Void, HashMap<String, Object>> friendFinderTask = ffa.new DownloadFriendFinderObjectTask().execute(new Object[]{getApplicationContext(), url});
	    		HashMap<String, Object> friend = FriendFinderActivity.getFriendFinderObject(getApplicationContext(), url);
//	    		try {
//	    			friend = friendFinderTask.get();
//	    		} catch (InterruptedException e) {}
//	    		catch (ExecutionException e) {}
	    		
	        	Log.d(TAG, "got friend finder object: " + friend);
	        	if(friend != null) {
	        		Log.d(TAG, "found alive friend with pin " + pin + " and name " + (String)friend.get("Name"));
	        		friend.put("Pin", pin);
	        		DatabaseHelper.saveFriend(friend);
	        	} else {
	        		Log.d(TAG, "Friend was dead.");
	        		DatabaseHelper.sharedDb().delete("Friends", "Pin="+pin.toString(), null);
	        	}
			}
			cursor.close();
			
	        ArrayList<HashMap<String, Object>> friends = DatabaseHelper.getFriends();
	        Log.d(TAG, "Current Friend Count: "+ friends.size());
	        mapView.removeAllFriends();
	        ArrayList<String> friendsOffMap = new ArrayList<String>();
	        for(HashMap<String, Object> friend : friends) {
	        	double latitude = ((Double)friend.get("Latitude")).doubleValue();
	        	double longitude = ((Double)friend.get("Longitude")).doubleValue();
	        	String name = (String)friend.get("Name");
	        	MyPoint friendLocation = new MyPoint(latitude, longitude);
	        	if(mapView.locationIsOnMap(friendLocation)) {
	        		mapView.addFriend(friendLocation, name);
	        	} else {
	        		friendsOffMap.add(name);
	        	}
	        }
	        if(!mapView.locationIsOnMap(currLocUgs))
	        	friendsOffMap.add("you");
	        
	        
	        Log.d(TAG, "friendsOffMap count: "+friendsOffMap.size());
	        // Set the status text
	        TextView statusText = (TextView) findViewById(R.id.status_text);
	        String status = "";
    		if(friendsOffMap.size() > 1) {
		    	Log.d(TAG, "friendsOffMap size > 1");
    			for(int i=0;i<friendsOffMap.size()-1;i++) {
	    			status += friendsOffMap.get(i);
	    			status += ", ";
	    		}
	    		status += "and ";
	    		status += friendsOffMap.get(friendsOffMap.size()-1);
	    		status += " are off the map.";
			} else if(friendsOffMap.size() == 1){
				Log.d(TAG, "friendsOffMap size == 1");
				if(friendsOffMap.get(0).equals("you")) {
					status = "You are off the map";
				} else {
					status = friendsOffMap.get(0) + " is off the map";
				}
			}
    		Log.d(TAG, "setting status to: "+status);
    		statusText.setText((CharSequence)status);
    		Log.d(TAG, "blah");
    		if(status.equals("")) {
    			Log.d(TAG, "hiding status text");
    			statusText.setVisibility(View.GONE);
    		} else {
    			Log.d(TAG, "showing status text");
    			statusText.setVisibility(View.VISIBLE);
    		}
    		
    		
    		Log.d(TAG, "status text set");
    		
    		// Cancel the update timer if there are no friends.
	        if(friends.size() == 0) {
	        	Log.d(TAG, "cancelling friendUpdateTimer!");
				friendUpdateTimer.cancel();
			}
			
			//Redraw the map
			mapView.invalidate();
		}
	}

	// Creates the menu items
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, SEARCH, 0, "Search").setIcon(android.R.drawable.ic_search_category_default).setAlphabeticShortcut(SearchManager.MENU_KEY);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.removeItem(NEAR_ME);
		if (currLocUgs != null) {
			menu.add(0, NEAR_ME, 0, "Near Me").setIcon(android.R.drawable.ic_menu_compass);
		}
		return true;
		
	}

	// Handles item selections
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case SEARCH:
	    	setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
	        onSearchRequested();
	        return true;
	    case NEAR_ME:
	    	Intent intent  = new Intent().setClass(this, LocationsActivity.class);
	    	intent.putExtra("nearMe", true);
	    	if(currLocUgs != null) {
	    		intent.putExtra("nearMeLat", currLocUgs.x);
	    		intent.putExtra("nearMeLon", currLocUgs.y);
	    	}
	    	startActivity(intent);
	        return true;
	    }
	    return false;
	}
	
	@Override
	public boolean onSearchRequested() {
		Bundle bundle = new Bundle();
		bundle.putBoolean("nearMe", false);
		if(currLocUgs != null) {
			bundle.putDouble("nearMeLat", currLocUgs.x);
			bundle.putDouble("nearMeLon", currLocUgs.y);
		}
		startSearch(null, false, bundle, false);
		return true;
	}
}
