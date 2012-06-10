package com.avai.wpzoo;

import java.util.Collections;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.codecarpet.fbconnect.FBDialog;
import com.codecarpet.fbconnect.FBFeedActivity;
import com.codecarpet.fbconnect.FBRequest;
import com.codecarpet.fbconnect.FBSession;
import com.codecarpet.fbconnect.FBDialog.FBDialogDelegate;
import com.codecarpet.fbconnect.FBRequest.FBRequestDelegate;
import com.codecarpet.fbconnect.FBSession.FBSessionDelegate;


public class FacebookHelper {
	private Activity mAct;
	private static FacebookHelper ref;
    public String username;
    public String displayName;
    public boolean loggedIn;
    private static final String kGetSessionProxy = null; // "<YOUR SESSION CALLBACK)>";
    //private static final int PERMISSIONREQUESTCODE = 1;
    private static final int MESSAGEPUBLISHED = 2; 
    public FBSession session;
    private Handler mHandler;
    
    // Constructor
	private FacebookHelper(Activity act) {
		this.mAct = act;
		loggedIn = false;
		mHandler = new Handler();
        
		if (kGetSessionProxy != null) {
            this.session = FBSession.getSessionForApplication_getSessionProxy(Constants.sharedConstants().kFbConnectApiKey, kGetSessionProxy, new FBSessionDelegateImpl());
        } else {
            this.session = FBSession.getSessionForApplication_secret(Constants.sharedConstants().kFbConnectApiKey, Constants.sharedConstants().kFbConnectApiSecret, new FBSessionDelegateImpl());
        }
    }

	public static FacebookHelper sharedHelper(Activity act) {
    	if(ref == null) {
    		ref = new FacebookHelper(act);
    	}
    	else {
    		ref.mAct = act;
    	}
    	return ref;
    }
	
	public static FacebookHelper sharedHelper() {
		return ref;
	}
	
    public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }
    
    public boolean postToWall(String message) {
    	return false;
    }
    
	public void logout() {
		this.loggedIn = false;
		this.username = null;
		this.displayName = null;
	}

	public void promptToPost(String message) {		
		if(message != null && this.loggedIn) { 
			Intent intent = new Intent(mAct.getApplicationContext(), FBFeedActivity.class);
	        intent.putExtra("userMessagePrompt", "Example prompt");
	        String imageUrl = Constants.sharedConstants().snImageUrl; // http://www.zoo.org/view.image?Id=439
	        String imageLink = Constants.sharedConstants().snImageLink;  // http://www.zoo.org
	        String titleAction = displayName + " " + Constants.sharedConstants().snTitleAction; //  is using the Woodland Park Zoo iPhone app.
	        String actionLink = Constants.sharedConstants().snActionLink; // http://www.zoo.org
	        String attachment = "{\"name\":\""+titleAction+"\",\"href\":\""+actionLink+"\",\"caption\":\""+message+"\",\"description\":\"\",\"media\":[{\"type\":\"image\",\"src\":\""+imageUrl+"\",\"href\":\""+imageLink+"\"}]}"; //,\"properties\":{\"another link\":{\"text\":\"Facebook home page\",\"href\":\"http://www.facebook.com\"}}
	        intent.putExtra("attachment", attachment);
	        mAct.startActivityForResult(intent, MESSAGEPUBLISHED);
	    }
	}

    // /////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("unused")
	private class FBDialogDelegateImpl extends FBDialogDelegate {

        @Override
		public void didFailWithError(FBDialog dialog, Throwable error) {
        	((ProfileActivity) mAct).setFacebookStatusLabel(error.toString());
        }

    }
    
    private void checkPermission() {
    	String fql = "select publish_stream from permissions where uid == " + String.valueOf(session.getUid());
		Map<String, String> params = Collections.singletonMap("query", fql);
		FBRequest.requestWithDelegate(new FBHasPermissionRD()).call("facebook.fql.query", params);
	}    

    private class FBSessionDelegateImpl extends FBSessionDelegate {

        @Override
		public void session_didLogin(FBSession session, Long uid) {
            // we check if the user already has the permissions before displaying permission button
        	checkPermission();
        	
        	mHandler.post(new Runnable() {
                public void run() {
                	loggedIn = true;
                }
             });

            String fql = "select uid,name from user where uid == " + session.getUid();

            Map<String, String> params = Collections.singletonMap("query", fql);
            FBRequest.requestWithDelegate(new FBRequestDelegateImpl()).call("facebook.fql.query", params);
        }

		@Override
		public void sessionDidLogout(FBSession session) {
            mHandler.post(new Runnable() {
               public void run() {
            	   loggedIn = false;
            	   ((ProfileActivity) mAct).setFacebookStatusLabel("");
               }
            });
        }

    }

    private class FBRequestDelegateImpl extends FBRequestDelegate {

        @Override
		public void request_didLoad(FBRequest request, Object result) {
            
            String name = null;
            
            if (result instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) result;
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    name = jo.getString("name");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            
			try {
				((ProfileActivity) mAct).setFacebookStatusLabel("Logged in as " + name);
			} catch (ClassCastException e) {
				// Exception will be thrown when LoadingActivity calls FacebookHelper to resume a session, because LoadingActivity is not a ProfileActivity.
			}
            
            FacebookHelper.sharedHelper().displayName = name;
        }

        @Override
		public void request_didFailWithError(FBRequest request, Throwable error) {
            //_label.setText(error.toString());
        }
    }
    
    private class FBHasPermissionRD extends FBRequestDelegate {

        @Override
		protected void request_didFailWithError(FBRequest request,
				Throwable error) {
			super.request_didFailWithError(request, error);
		}

		@Override
		public void request_didLoad(FBRequest request, Object result) {
            int hasPermission = 0;
            
            if (result instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) result;
                try {
                    JSONObject jo = jsonArray.getJSONObject(0);
                    hasPermission = jo.getInt("publish_stream");
                    if (hasPermission == 0)
                    {
                        mHandler.post(new Runnable() {
                            public void run() {
                                //_permissionButton.setVisibility(View.VISIBLE);
                            }
                         });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
