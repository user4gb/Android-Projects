/*
 * Copyright 2009 Codecarpet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.avai.wpzoo;

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.codecarpet.fbconnect.FBLoginButton;
import com.codecarpet.fbconnect.FBLoginButton.FBLoginButtonStyle;
import com.flurry.android.FlurryAgent;

public class ProfileActivity extends Activity {

    private FBLoginButton _loginButton;
    private TextView _label;
    private ProgressDialog pd;
	
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
        String activityName = getIntent().getStringExtra("Name");
        setTitle(activityName);
        
        // Report a Flurry event
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Id", Integer.toString(getIntent().getIntExtra("Id", 0)));
        FlurryAgent.onEvent("ItemVisited", map);
        
        setContentView(R.layout.profile);
        
        // Set the background.
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
        scrollView.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
        
        LinearLayout profileLayout = (LinearLayout) findViewById(R.id.profile_layout);
        
        if(!HttpHelper.networkAvailable(getApplicationContext())) {
        	profileLayout.setVisibility(View.GONE);
        	if(savedInstanceState == null) {
        		HttpHelper.presentAlert(this, getResources().getText(R.string.no_network_connection), activityName + " " + getResources().getText(R.string.activity_requires_network_connection));
        	}
	    } else {
	    	// INSTRUCTIONS
	    	WebView instructions = (WebView) findViewById(R.id.instructions_text);
	    	SharedPreferences settings = this.getSharedPreferences(Constants.sharedConstants().prefsFileName, 0);
	        instructions.setBackgroundColor(0);
	        
	        // Check preferences first, if not there
	        String content = settings.getString("ProfileInstructions", null);
	        if(content == null) {
	        	content = getIntent().getStringExtra("Content");
	        	SharedPreferences.Editor editor = settings.edit();
	        	editor.putString("ProfileInstructions", content);
	        	editor.commit();
	        }
	        instructions.loadData(PageActivity.getReformattedContent(getApplicationContext(), getIntent().getIntExtra("Id", 0), content), "text/html", "utf-8");
	        
	    	// FACEBOOK
	    	FacebookHelper helper = FacebookHelper.sharedHelper(this);
	        _label = (TextView) findViewById(R.id.facebook_status);
	        _loginButton = (FBLoginButton) findViewById(R.id.facebook_button);
	        _loginButton.setStyle(FBLoginButtonStyle.FBLoginButtonStyleWide);
	        _loginButton.setSession(helper.session);
	        
	        helper.session.resume(this);
	      
	        //TWITTER
	        //If twitter credentials are saved from a previous session, update the twitter UI elements.
	        if(TwitterHelper.sharedHelper(getApplicationContext()).loggedIn)
	        	updateTwitterElements(true); 
	        
		    //Set the action for the twitter button
		    Button twitterButton = (Button) findViewById(R.id.twitter_button);
		    twitterButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if(TwitterHelper.sharedHelper(getApplicationContext()).loggedIn) {
						logoutTwitter();
					}
					else {
						loginTwitter();
					}
				}
		    });
		}
    }
    
    // TWITTER METHODS
    
    protected void logoutTwitter() {
		TwitterHelper.sharedHelper(this).logout();
		EditText usernameField = (EditText) findViewById(R.id.twitter_username_field);
		usernameField.setVisibility(View.VISIBLE);
		EditText passwordField = (EditText) findViewById(R.id.twitter_password_field);
		passwordField.setVisibility(View.VISIBLE);
		TextView twitterStatus = (TextView) findViewById(R.id.twitter_status);
		twitterStatus.setText("");
		Button twitterButton = (Button) findViewById(R.id.twitter_button);
		twitterButton.setText("Login");
	}
	
	protected void loginTwitter() {
		EditText usernameField = (EditText) findViewById(R.id.twitter_username_field);
		EditText passwordField = (EditText) findViewById(R.id.twitter_password_field);
		if(usernameField.getText().toString().compareTo("") == 0) {
			Toast.makeText(getApplicationContext(), "Please enter a username.", Toast.LENGTH_SHORT).show();
		} else if(passwordField.getText().toString().compareTo("") == 0) {
			Toast.makeText(getApplicationContext(), "Please enter a password.", Toast.LENGTH_SHORT).show();
		} else {
			showLoadingDialog("Logging In", "Verifying credentials...");
			boolean loggedIn = TwitterHelper.sharedHelper(this).setCredentials(usernameField.getText().toString(), passwordField.getText().toString());
			if(pd != null)
				pd.dismiss();
			updateTwitterElements(loggedIn); 
		}
	}
	
	private void updateTwitterElements(boolean loggedIn) {
		EditText usernameField = (EditText) findViewById(R.id.twitter_username_field);
		EditText passwordField = (EditText) findViewById(R.id.twitter_password_field);
		TextView twitterStatus = (TextView) findViewById(R.id.twitter_status);
		Button twitterButton = (Button) findViewById(R.id.twitter_button);
		if(loggedIn) {
			usernameField.setText("");
			passwordField.setText("");
			usernameField.setVisibility(View.INVISIBLE);
			passwordField.setVisibility(View.INVISIBLE);
			twitterStatus.setText("Logged in as " + TwitterHelper.sharedHelper(this).creds.getUserName());
			twitterButton.setText("Logout");
		} else {
			twitterStatus.setText("Login failed.");
		}
	}
	
	// FACEBOOK METHODS
	
	public void setFacebookStatusLabel(String text) {
		_label.setText(text);
	}
	
	private void showLoadingDialog(String title, String message) { 
		pd = ProgressDialog.show(this, title, message, true, false);
	}
}