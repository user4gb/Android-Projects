package com.texasstudentmedia;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.texasstudentmedia.helper.SafeThread;

public class Feed{
	private final String TAG = "Texas Student Media";
	public static final int IDLE = 0,
					 FETCHING = 1,
					 FETCHING_EXTRA = 2,
					 SAVING = 3;
	private String name;
	private String url;
	private int state;
	private ArrayList<FeedItem> feed_items;
	private SafeThread thread;
	
	public Feed(String name, String url){
		this.name = name;
		this.url = url;
		this.state = IDLE;
		feed_items = new ArrayList<FeedItem>();
	}
	
	public boolean loadFromMemory(){
		/* TODO: Load from memory conditions:
		 * 1) Does file exist?
		 * 2) Is file age young enough?
		 * */
		return false;
	}
	
	public void loadFromURL(final int min){
		thread = new SafeThread(){
			public void doStuff(){
				state = FETCHING;
				// Go through data stream reading the items
				try {
					XMLReader xml_reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
					xml_reader.setContentHandler(new DefaultHandler() {
						private ArrayList<FeedItem> new_feed_items = new ArrayList<FeedItem>();
						private FeedItem feed_item = new FeedItem();
						private String value = "";
						private final int IN_NOTHING = 0,
										  IN_ITEM = 1,
										  IN_TITLE = 2, 
										  IN_DESC = 3;
						private int state = IN_NOTHING;

						@Override
						public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
							if (localName.equalsIgnoreCase("item"))
								state = IN_ITEM;
							else if (state == IN_ITEM) {
								if (localName.equalsIgnoreCase("title"))
									state = IN_TITLE;
								else if (localName.equalsIgnoreCase("description"))
									state = IN_DESC;
							}
						}

						@Override
						public void endElement(String uri, String localName, String qName) throws SAXException {
							if (localName.equalsIgnoreCase("title") && state == IN_TITLE) {
								feed_item.setTitle(value);
								value = "";
								state = IN_ITEM;
							} else if (localName.equalsIgnoreCase("description") && state == IN_DESC) {
								feed_item.setContent(value);
								value = "";
								state = IN_ITEM;
							} else if (localName.equalsIgnoreCase("item") && state == IN_ITEM) {
								// Save item to list
								new_feed_items.add(feed_item);
								//Log.v(TAG, "  " + feed_item.getTitle());
								
								// Reset state
								state = IN_NOTHING;
								
								// Check to see if minimum has been reached
								if (new_feed_items.size() >= min && Feed.this.state == FETCHING){
									// Minimum number of items was retrieved
									feed_items = new_feed_items;
									Log.v(TAG, Feed.this.name + " has minimum amount of items loaded");
									//Feed.this.state = FETCHING_EXTRA;
									//Feed.this.thread.setPriority(MIN_PRIORITY);
									throw new SAXException();
								}
								else
									feed_item = new FeedItem();
							}
						}
						
						@Override
						public void endDocument(){
							feed_items = new_feed_items;
						}

						@Override
						public void characters(char[] ch, int start,int length) throws SAXException {
							if (state > IN_ITEM)
								value += new String(ch, start, length);
						}
					});
					xml_reader.parse(new InputSource(new URL(Feed.this.url).openStream()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					//e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (FactoryConfigurationError e) {
					e.printStackTrace();
				}
				
				Log.v(TAG, Feed.this.name + " is complete");
				// TODO: Save data to internal memory
				state = SAVING;
				
				pause();
				state = IDLE;
			}
		};
		thread.start();
	}

	public void stop() {
		if (thread.isAlive())
			thread.kill();
	}
	
	public int getState(){
		return state;
	}
	
	
	public String getName() {
		return name;
	}
	
	public ArrayList<FeedItem> getItems(){
		return feed_items;
	}
	
	public int getItemListSize(){
		return feed_items.size();
	}
}
