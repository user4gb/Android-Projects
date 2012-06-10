package com.avai.wpzoo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

public class FriendFinderActivity extends Activity {
    private int mySessionId;
    private boolean sessionActive;
	String serviceName;
	ArrayList<HashMap<String, Object>> friends;
	AsyncTask<Void, Void, Void> task;
	private static final String TAG = "FriendFinderActivity";
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
        String activityName = (String)(getIntent().getStringExtra("Name"));
        setTitle(activityName);

        // Report a Flurry event
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Id", Integer.toString(getIntent().getIntExtra("Id", 0)));
        FlurryAgent.onEvent("ItemVisited", map);
        
        setContentView(R.layout.friendfinder);
        
        // Set the background
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
        scrollView.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
        
        LinearLayout friendFinderLayout = (LinearLayout) findViewById(R.id.friend_finder_layout);
       
        if(!HttpHelper.networkAvailable(getApplicationContext())) {
        	friendFinderLayout.setVisibility(View.GONE);
        	if(savedInstanceState == null) {
        		HttpHelper.presentAlert(this, getResources().getText(R.string.no_network_connection), activityName + " " + getResources().getText(R.string.activity_requires_network_connection));
        	}
       	} else {
	        //Initialization
	        SharedPreferences settings = this.getSharedPreferences(Constants.sharedConstants().prefsFileName, 0);
	        sessionActive = false;
	        serviceName = Constants.sharedConstants().friendFinderServiceName;
            
	        // Check to see if FriendFinder location updater is running
	        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
	        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);
	         
	        for (int i=0; i<rs.size(); i++) {
	            ActivityManager.RunningServiceInfo rsi = rs.get(i);
	            if(rsi.service.getClassName().equals(getPackageName() + ".FriendFinderService"))
	            	sessionActive = true;
	            Log.i("FriendFinderActivity", "Process " + rsi.process + " with component " + rsi.service.getClassName());
	        }
	        if(sessionActive)
            	mySessionId = settings.getInt("FriendFinderPin", 0);
            else
            	mySessionId = 0;
	        configureMyPinUI();
	        LinearLayout yourPinLayout = (LinearLayout) findViewById(R.id.your_pin_layout);
	        if(!sessionActive && !FriendFinderActivity.locationAvailable(this)) {
	        	yourPinLayout.setVisibility(View.INVISIBLE);
	        	HttpHelper.presentAlert(this, getResources().getText(R.string.location_unavailable), getResources().getText(R.string.friend_finder_location_unavailable));
	        } else {
	        	yourPinLayout.setVisibility(View.VISIBLE);
	        }
	        // Populate listview with current friends
	        friends = DatabaseHelper.getFriends();
	        updateFriendsList();
	
	        // INSTRUCTIONS
	        WebView instructions = (WebView) findViewById(R.id.instructions_text);
	         
	        instructions.setBackgroundColor(0);
	        // Check preferences first, if not there
	        String content = settings.getString("FriendFinderInstructions", null);
	        if(content == null) {
	        	content = getIntent().getStringExtra("Content");
	        	SharedPreferences.Editor editor = settings.edit();
	        	editor.putString("FriendFinderInstructions", content);
	        	editor.commit();
	        }
	        instructions.loadData(PageActivity.getReformattedContent(getApplicationContext(), getIntent().getIntExtra("Id", 0), content), "text/html", "utf-8");
	        
	        // PIN BUTTON
	        Button pinButton = (Button) findViewById(R.id.pin_button);
	        pinButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(sessionActive) {
						//("Releasing pin");
						endFriendFinderSession();
						Log.d(TAG, "about to dismiss dialog");
						//pd.cancel();
						sessionActive = false;
					} else {  
						EditText yourNameField = (EditText) findViewById(R.id.your_name_field);
						String name = yourNameField.getText().toString().trim();
						name = name.replace(" ", "");
						if(yourNameField.getText() != null && name.compareTo("") != 0) {
							if(startFriendFinderSession(name)) {
								sessionActive = true;
							}
						} else {
							Toast.makeText(getApplicationContext(), "You must enter your name before you can get a PIN.", Toast.LENGTH_SHORT).show();
						}
					}
				}
	        }); 
	        
	        //Set the action for the follow friend button
	        Button followButton = (Button) findViewById(R.id.follow_button);
	        followButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
						EditText friendsPinField = (EditText) findViewById(R.id.friends_pin_field);
						if(friendsPinField.getText() != null)
							try {
								int id = Integer.parseInt(friendsPinField.getText().toString());
								//myShowDialog("Following friend");								
								followFriend(id);
								
								Log.d(TAG, "about to dismiss dialog");
								//pd.cancel();
							} catch (NumberFormatException nfe) {
								Toast.makeText(getApplicationContext(), "Please enter a pin number.", Toast.LENGTH_SHORT).show();
							}
						else
							Toast.makeText(getApplicationContext(), "You must enter a valid PIN before you can follow a friend.", Toast.LENGTH_SHORT).show();
				}
	        });
	        
	        //Set the action for the remove friends button
	        Button removeFriendsButton = (Button) findViewById(R.id.remove_friends_button);
	        removeFriendsButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					friends = new ArrayList<HashMap<String, Object>>();
					int count = DatabaseHelper.sharedDb().delete("Friends", "1", null);
					Log.d(TAG, "removed " + count + " records from Friends table");
					updateFriendsList();
				}
	        });
	    }
	}
/*	
	private void myShowDialog(String message) {	
		Log.d(TAG, "showing dialog with message: "+message);
		pd = ProgressDialog.show(this, "Loading...", message, true);
		Log.d(TAG, "message shown");
	}
*/	
	public static boolean locationAvailable(Context ctx) {
		LocationManager manager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = manager.getProviders(true);
		
		if(providers!=null && providers.size() > 0) {
			for(String provider : providers) {
				Log.d(TAG, "location provider available: "+provider);
			}
			return true;
		}
		return false;
	}

	private void updateFriendsList() {
		LinearLayout friendsListHeader = (LinearLayout) findViewById(R.id.friends_list_header);
		LinearLayout friendsList= (LinearLayout)findViewById(R.id.friends_list);
        if(friends.size() > 0) {
        	friendsListHeader.setVisibility(View.VISIBLE);
        	friendsList.setVisibility(View.VISIBLE);
	        friendsList.removeAllViews();
        	for(int i=0;i<friends.size();i++) {
	        	HashMap<String, Object> friend = friends.get(i);
	        	TextView friendText = new TextView(this);
	        	friendText.setText((String)friend.get("Name"));
	        	float scale = getResources().getDisplayMetrics().density;
	        	int padding = (int)(6*scale);
	        	friendText.setPadding(padding, padding, padding, padding);
	        	friendText.setTextAppearance(this, R.style.default_text);
	        	friendsList.addView(friendText);
	        	
	        }
	    } else {
	    	friendsListHeader.setVisibility(View.INVISIBLE);
	    	friendsList.setVisibility(View.INVISIBLE);
	    }
        //pd.dismiss();
	}

	private void followFriend(int friendId) {
		// Clear the text field
		EditText friendsPinField = (EditText) findViewById(R.id.friends_pin_field);
		friendsPinField.setText("");	
		boolean alreadyFollowing = false;
		for(int i=0;i<friends.size();i++) {
			if(Integer.parseInt(friends.get(i).get("Pin").toString()) == friendId) {
				alreadyFollowing = true;
				break;
			}
		}
		if(alreadyFollowing) {
			Toast.makeText(getApplicationContext(), "You are already following this friend.", Toast.LENGTH_SHORT).show();
		} else {
			final String url = serviceName + friendId + "/currentlocation";
			//myShowDialog("Following friend");
			HashMap<String, Object> friend = null;
			friend = FriendFinderActivity.getFriendFinderObject(getApplicationContext(), url);
			
			if(friend != null) {
				friend.put("Pin", friendId);
				friends.add(friend);
				 
		    	//Save friend in database
				DatabaseHelper.saveFriend(friend);
				updateFriendsList();
			}
		}
	}

	private boolean startFriendFinderSession(String name) {	
		String url = serviceName + "start?name=" + name;		
//		AsyncTask<Object, Void, HashMap<String, Object>> friendFinderTask = new DownloadFriendFinderObjectTask().execute(new Object[]{getApplicationContext(), url});
//		HashMap<String, Object> object = null;
//		try {
//			object = friendFinderTask.get();
//		} catch (InterruptedException e) {}
//		catch (ExecutionException e) {}
		
		HashMap<String, Object> object = FriendFinderActivity.getFriendFinderObject(getApplicationContext(), url);
		if(object != null) {
			mySessionId = Integer.parseInt(object.get("Data").toString());
			configureMyPinUI();
			
			//Save PIN number
	    	SharedPreferences settings = getSharedPreferences(Constants.sharedConstants().prefsFileName, 0);
	    	SharedPreferences.Editor editor = settings.edit();
	    	editor.putInt("FriendFinderPin", mySessionId);
	    	editor.commit();
			
			//Start FriendFinderService
	    	Intent friendFinderIntent = new Intent(this, FriendFinderService.class);
	    	friendFinderIntent.putExtra("sessionId", mySessionId);
            startService(friendFinderIntent);
 
            return true;
		} else {
			return false;
		}
	}
	
	private boolean endFriendFinderSession() {
		String url = serviceName + mySessionId + "/end";
//		AsyncTask<Object, Void, HashMap<String, Object>> friendFinderTask = new DownloadFriendFinderObjectTask().execute(new Object[]{getApplicationContext(), url});
		HashMap<String, Object> object = FriendFinderActivity.getFriendFinderObject(getApplicationContext(), url);
//		try {
//			object = friendFinderTask.get();
//		} catch (InterruptedException e) {}
//		catch (ExecutionException e) {}
		if(object != null) {
			mySessionId = 0;
			configureMyPinUI();	
			
			// Kill FriendFinderService
			stopService(new Intent(this, FriendFinderService.class));
			return true;
		} else {
			return false;
		}
	}
	
	private void configureMyPinUI() {
		EditText yourNameField = (EditText) findViewById(R.id.your_name_field);
		TextView yourName = (TextView) findViewById(R.id.your_name);
		Button pinButton = (Button) findViewById(R.id.pin_button);
		TextView yourPin = (TextView) findViewById(R.id.your_pin);
		if(mySessionId == 0) {
			yourNameField.setVisibility(View.VISIBLE);
			yourName.setText("Your Name:");
			pinButton.setText("Get My PIN");
			yourPin.setText("");
		} else {
			yourNameField.setText("");
			yourNameField.setVisibility(View.INVISIBLE);
			yourName.setText("Your PIN is");
			pinButton.setText("Release PIN");
			yourPin.setText(Integer.toString(mySessionId));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, Object> getFriendFinderObject(final Context ctx, final String url) {
//		new Thread() {
//			public void run() {
				final JSONObject response = HttpHelper.getJSONObject(ctx, url);
				try {
					if(response != null && response.has("Success")) {
						if((response.getString("Success")).compareTo("false") == 0) {
							Log.d(TAG, "no success :(      " + response.getString("ErrorMessage"));
							try {
//								((FriendFinderActivity) ctx).runOnUiThread(new Runnable() {
//									public void run() {
										try {
											Toast.makeText(ctx, response.getString("ErrorMessage"), Toast.LENGTH_SHORT).show();
										} catch (JSONException e) {}
//									}
//								});
							} catch (ClassCastException cce) {
								//If this is called from AmpMapActivity, we don't want to display a Toast.
							}
							return null;
							//handler.sendEmptyMessage(0);
						}
						HashMap<String, Object> object = new HashMap<String, Object>();
						Iterator<String> iter = response.keys();
						while(iter.hasNext()) {
							String key = iter.next(); 
							object.put(key, response.get(key));
						}
						return object;
						//Message msg = new Message();
						//msg.obj = object;
						//handler.sendMessage(msg);
					}
					return null;
					//handler.sendEmptyMessage(0);
				} catch (JSONException e) {
					return null;
					//handler.sendEmptyMessage(0);
				}
//			}
//		}.start();
	}
/*	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(pd != null)
				pd.dismiss();
		}
	};
	
	
	public class DownloadFriendFinderObjectTask extends AsyncTask<Object, Void, HashMap<String, Object>> {
		//private ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			//Log.d(TAG, "showing dialog with message: ");
			//pd = ProgressDialog.show(FriendFinderActivity.this, "Loading...", null, true);
			//Log.d(TAG, "message shown");
		}
		
		@Override
		protected HashMap<String, Object> doInBackground(Object...params) {
			Log.d(TAG, "doInBackground DownloadFriendFinderObjectTask");
			return getFriendFinderObject((Context)params[0], (String)params[1]);
		}
		
		@SuppressWarnings("unchecked")
		public HashMap<String, Object> getFriendFinderObject(final Context ctx, String url) {
			final JSONObject response = HttpHelper.getJSONObject(ctx, url);
			try {
				if(response != null && response.has("Success")) {
					if((response.getString("Success")).compareTo("false") == 0) {
						Log.d(TAG, "no success :(      " + response.getString("ErrorMessage"));
						try {
							((FriendFinderActivity) ctx).runOnUiThread(new Runnable() {
								public void run() {
									try {
										Toast.makeText(ctx, response.getString("ErrorMessage"), Toast.LENGTH_SHORT).show();
									} catch (JSONException e) {}
								}
							});
						} catch (ClassCastException cce) {
							//If this is called from AmpMapActivity, we don't want to display a Toast.
						}
						return null;
					}
					HashMap<String, Object> object = new HashMap<String, Object>();
					Iterator<String> iter = response.keys();
					while(iter.hasNext()) {
						String key = iter.next(); 
						object.put(key, response.get(key));
					}
					return object;			
				}
				return null;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		@Override
		protected void onPostExecute(HashMap<String, Object> friend) {
	        if(pd!=null) {
	        	Log.d(TAG, "DownloadFriendFinderObjectTask onPostExecute dismissing pd");
	        	pd.dismiss();
	        }
		}
	}*/
}
