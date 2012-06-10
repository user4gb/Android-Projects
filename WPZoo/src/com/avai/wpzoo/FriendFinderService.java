package com.avai.wpzoo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class FriendFinderService extends Service {
    private NotificationManager mNM;
    private final String SERVICE_STARTED = "Friend Finder is now sharing your location.";
    private final String SERVICE_STOPPED = "Friend Finder stopped.";
    private final String SERVICE_RUNNING = "Friend Finder";
    private final int SERVICE_ID = 32;
    private String friendFinderService;
    public static int sessionId;
    
	AsyncTask<Void, Void, Void> task;

    public class LocalBinder extends Binder {
    	FriendFinderService getService() {
            return FriendFinderService.this;
        }
    } 
    
    @Override
    public void onStart(Intent intent, int startId) {
    	sessionId = intent.getIntExtra("sessionId", 0);
    	friendFinderService = Constants.sharedConstants().friendFinderServiceName;
    	System.out.println("FriendFinderService onStartCommand  service: " + friendFinderService + "   sessionId: " + sessionId);
       
    	//Start the UpdateLocationTask
        task = new UpdateLocationTask().execute((Void[])null);
    }
    
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.  We put an icon in the status bar.
        showNotification();
    }

    @Override
    public void onDestroy() {
    	// Kill the UpdateLocationTask
    	task.cancel(true);
        
    	// Cancel the persistent notification.
        mNM.cancel(SERVICE_ID);

        // Tell the user we stopped.
        Toast.makeText(this, SERVICE_STOPPED, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = SERVICE_STARTED;

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(Constants.sharedConstants().appIconId, text,
                System.currentTimeMillis());
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, FriendFinderActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, SERVICE_RUNNING,
                       text, contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNM.notify(SERVICE_ID, notification);
    }
    
    private class UpdateLocationTask extends AsyncTask<Void, Void, Void> implements LocationListener{
		LocationManager locationManager;
		private String bestProvider;
		
		@Override
		protected void onPreExecute() {
			
			// Get the location
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			bestProvider = locationManager.getBestProvider(new Criteria(), false);
			Location location = locationManager.getLastKnownLocation(bestProvider);
			
			// Post the last known location
			updateLocation (location);
			
			// Request updates
			locationManager.requestLocationUpdates(bestProvider, 0, 0, this);
		}
		
		@Override
		protected Void doInBackground(Void... arg0) {
			// Post update with last known location
			// updateLocation(location);
			// Start a 5 min timer
			return null;
		}
		
		public void updateLocation(Location location) {
			String url = friendFinderService + sessionId + "/?latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude();
			HttpHelper.get(getApplicationContext(), url);
		}
		
		// LocationListener methods
		public void onLocationChanged(Location location) {
			updateLocation (location);
		}

		public void onProviderDisabled(String arg0) {
			bestProvider = locationManager.getBestProvider(new Criteria(), false);
		}

		public void onProviderEnabled(String provider) {
			bestProvider = locationManager.getBestProvider(new Criteria(), false);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			
		}
	}
}