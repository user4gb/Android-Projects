package com.avai.wpzoo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.flurry.android.FlurryAgent;

public class TwitterActivity extends ListActivity {
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
    
	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String activityName = getIntent().getStringExtra("Name");
        setTitle(activityName);
        String name = getIntent().getStringExtra("Content");
        int pageId = getIntent().getIntExtra("Id", 0);
        
        // Report a Flurry event
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("Id", Integer.toString(pageId));
        FlurryAgent.onEvent("ItemVisited", map);
        
        setContentView(R.layout.twitter);
        ListView lv = getListView();
        lv.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
        
        if(!HttpHelper.networkAvailable(getApplicationContext())) {
        	if(savedInstanceState == null) {
        		HttpHelper.presentAlert(this, getResources().getText(R.string.no_network_connection), activityName + " " + getResources().getText(R.string.activity_requires_network_connection));
        	}
	    } else {  
	        AsyncTask<String, Void, String[]> twitterDownloader = new DownloadTwitterTask().execute(new String[]{name});
	        ArrayList tweetMaps = new ArrayList();
			String[] tweets = null;
			try {
				System.out.println("Trying to download");
			
				tweets = twitterDownloader.get();
				System.out.println("downloaded tweets");
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (CancellationException e) {
				e.printStackTrace();
			}
	        if(tweets != null && tweets.length > 0) {
				for(int i=0;i<tweets.length;i++) {
					System.out.println("Tweet " + i + ": " + tweets[i]);
					Hashtable tweetMap = new Hashtable();
					tweetMap.put("Name", tweets[i]);
					tweetMaps.add(i,tweetMap);
				}
			}
	        
	        String twitterImageUrl = DatabaseHelper.getItemExtraProperty(pageId, "twitterpicture");
	        if(twitterImageUrl != null)
	        {
	        	String[] splitUrl = twitterImageUrl.split("/");
				String twitterImageName = splitUrl[splitUrl.length-1];
	        	
				ImageView image = new ImageView(this);
	        	image.setScaleType(ImageView.ScaleType.FIT_XY);
	        	image.setAdjustViewBounds(true);
	        	image.setImageBitmap(ImageFinder.getBitmap(getApplicationContext(), twitterImageName, twitterImageUrl));
	        	lv.addHeaderView(image);
	        }
	        
	        SimpleAdapter twitterAdapter = new SimpleAdapter(this, tweetMaps, R.layout.cell_twitter, new String[] {"Name"}, new int[] {R.id.name});
	        setListAdapter(twitterAdapter);
	    }
    }
    
	private class DownloadTwitterTask extends AsyncTask<String, Void, String[]> {
		@Override
		protected String[] doInBackground(String...name) {
			System.out.println("doInBackground twitter reader task");
			String url = "http://twitter.com/statuses/user_timeline/" + name[0] + ".rss";
			InputStream is = HttpHelper.get(getApplicationContext(), url);
			if(is == null) {
				System.out.println("is is null");
				return null;
			}
			System.out.println("downloaded twitter feed, now trying to parse...");
			return parseTwitterXml(is);
		}
		
		private String[] parseTwitterXml(InputStream twitter) {
			
			System.out.println("parsing twitter xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			Document dom = null;
			try{
				DocumentBuilder db = dbf.newDocumentBuilder();
				dom = db.parse(twitter);
			} catch(IllegalArgumentException iae) {
				iae.printStackTrace();
			} catch (SAXException se) {
				se.printStackTrace();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (ParserConfigurationException pce) {
				pce.printStackTrace();
			}
			String[] tweets = null;
			try {
				System.out.println("converted inputstream to document object");
				Element root = dom.getDocumentElement();
				NodeList nl = root.getElementsByTagName("item");
				tweets = new String[nl.getLength()];
				System.out.println("blah");
				if(nl != null && nl.getLength() > 0){
					for(int i=0;i<nl.getLength();i++) {
						System.out.println("Adding tweet # "+ i);
						Element tweet = (Element) nl.item(i);
						NodeList tweetProperties = tweet.getElementsByTagName("title");
						Element tweetText = (Element)tweetProperties.item(0);
						tweets[i] = tweetText.getFirstChild().getNodeValue();
					}
				}
			} catch (NullPointerException npe) {
				npe.printStackTrace();
			}
			return tweets;
		}
		@Override
		protected void onPostExecute(String[] tweets) {

		}
	}
}
