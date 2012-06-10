package com.avai.wpzoo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NavigationHelper {	
	
	public static ArrayList<HashMap<String, Object>> getMenuItems(Context context, int rootId) {
		DatabaseHelper.sharedDb(context, "amp");
		return getMenuItems(rootId);
	}	
	
	public static ArrayList<HashMap<String, Object>> getMenuItems(int rootId){
		// Get shared database reference
	    SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();

	    String query = "SELECT Item.* from Item JOIN ItemSubItem on Item._id = ItemSubItem.ChildId" + 
		" WHERE ItemSubItem.ParentId="+rootId+" ORDER BY ItemSubItem.Rank"; 
	    
	    System.out.println("Query: "+query);
	    Cursor cursor = sharedDb.rawQuery(query, null);
	    System.out.println("Found "+cursor.getCount()+" items");
	    
	    ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
	    int itemIndex = 0;
	    while(cursor.moveToNext())
	    {
	    	HashMap<String, Object> item = new HashMap<String, Object>();
	    	item.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
	    	item.put("ImageFileName", cursor.getString(cursor.getColumnIndex("ImageFileName")));
	    	item.put("ImageFolderName", cursor.getString(cursor.getColumnIndex("ImageFolderName")));
	    	item.put("ImageId", cursor.getInt(cursor.getColumnIndex("ImageId")));
	    	item.put("ImageUrl", cursor.getString(cursor.getColumnIndex("ImageUrl")));
	    	item.put("ItemType", cursor.getString(cursor.getColumnIndex("ItemType")));
	    	item.put("Name", cursor.getString(cursor.getColumnIndex("Name")));
	    	item.put("Content", cursor.getString(cursor.getColumnIndex("Content")));
	    	itemIndex++;
	    	items.add(item);
	    }
	    cursor.close();
	    return items;	
	}
	
	public static ArrayList<HashMap<String, Object>> getItemsForTree(HashMap<Integer, Object> path) {
		Iterator<Integer> iter = path.keySet().iterator();
		System.out.println("Getting Item contents for " + path.size() + " items");
		int itemIndex = 0;
		ArrayList<HashMap<String, Object>> menuItems = new ArrayList<HashMap<String, Object>>();
		while(iter.hasNext()) {
			Integer itemId = iter.next();
			String query = 
				"SELECT Item.* from Item JOIN ItemSubItem on Item._id = ItemSubItem.ChildId" + 
				" WHERE Item._id IN (" + itemId + ") ORDER BY ItemSubItem.Rank";
			Cursor cursor = DatabaseHelper.sharedDb().rawQuery(query, null);
			while(cursor.moveToNext()) {
				String itemType = cursor.getString(cursor.getColumnIndex("ItemType"));
				if(!itemType.equals("NearestLocationFinder")) {
					HashMap<String, Object> item = new HashMap<String, Object>();
			    	item.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
			    	item.put("ImageFileName", cursor.getString(cursor.getColumnIndex("ImageFileName")));
			    	item.put("ImageFolderName", cursor.getString(cursor.getColumnIndex("ImageFolderName")));
			    	item.put("ImageId", cursor.getInt(cursor.getColumnIndex("ImageId")));
			    	item.put("ImageUrl", cursor.getString(cursor.getColumnIndex("ImageUrl")));
			    	item.put("ItemType", itemType);
			    	item.put("Name", cursor.getString(cursor.getColumnIndex("Name")));
			    	item.put("Content", cursor.getString(cursor.getColumnIndex("Content")));
			    	item.put("Path", path.get(itemId));
			    	itemIndex++;
			    	menuItems.add(item);
				}
			}
			cursor.close();
		}
		return menuItems;
	}

	private static ArrayList<HashMap<String, Object>> getEventsAndItemsAtLocation(Integer id, String searchQuery) {
		int itemIndex = 0;
		ArrayList<HashMap<String, Object>> menuItems = new ArrayList<HashMap<String, Object>>();
		if(searchQuery == null) 
			searchQuery = "";
		String query = 
			"SELECT DISTINCT Item._id, Item.* from Item" +
			" INNER JOIN ItemLocation ON ItemLocation.ItemId = Item._id" +
			" LEFT JOIN Event ON Item._id = Event.ItemId" +
			" LEFT JOIN ItemKeywords ON Item._id = ItemKeywords.ItemID" +
			" LEFT JOIN Keywords on Keywords._id = ItemKeywords.KeywordID" +
			" INNER JOIN ItemSubItem on Item._id = ItemSubItem.ChildId" +
			" WHERE (Event.LocationIds=" + id + " OR" +
			" ItemLocation.LocationId=" + id + ") AND" +
			" (Item.Name LIKE '%%" + searchQuery + "%%' OR Item.Content LIKE '%%" + searchQuery + "%%' OR Keywords.Keyword LIKE '%%" + searchQuery + "%%')" +
			" ORDER BY Rank";
		Cursor cursor = DatabaseHelper.sharedDb().rawQuery(query, null);
		while(cursor.moveToNext()) {
			HashMap<String, Object> item = new HashMap<String, Object>();
	    	item.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
	    	item.put("ImageFileName", cursor.getString(cursor.getColumnIndex("ImageFileName")));
	    	item.put("ImageFolderName", cursor.getString(cursor.getColumnIndex("ImageFolderName")));
	    	item.put("ImageId", cursor.getInt(cursor.getColumnIndex("ImageId")));
	    	item.put("ImageUrl", cursor.getString(cursor.getColumnIndex("ImageUrl")));
	    	item.put("ItemType", cursor.getString(cursor.getColumnIndex("ItemType")));
	    	item.put("Name", cursor.getString(cursor.getColumnIndex("Name")));
	    	item.put("Content", cursor.getString(cursor.getColumnIndex("Content")));
	    	itemIndex++;
	    	menuItems.add(item);
		}
		cursor.close(); 
		System.out.println("Found " + menuItems.size() + " events and items at the location " + id + " with the query: " + query);
		if(menuItems.size() == 0)
			return null;
		return menuItems;
	}
	
	public static ArrayList<HashMap<String, Object>> getEvents(Date startDate, int numDays) {
		// Get shared database reference
	    SQLiteDatabase sharedDb = DatabaseHelper.sharedDb(); 
	    
	    ArrayList<HashMap<String, Object>> events = new ArrayList<HashMap<String, Object>>();
	    
	    // Set default timezone to GMT.
	    TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
	    
	    // Calculate start and end dates	    
	    long start = startDate.getTime();
	    String query = "SELECT StartTimeWithOffsetOffsetMinutes FROM Event";
	    Cursor cursor = sharedDb.rawQuery(query, null);
	    long offset = 0;
	    while(cursor.moveToNext()) {
	    	 offset = (cursor.getInt(cursor.getColumnIndex("StartTimeWithOffsetOffsetMinutes")) * 60 * 1000);
	    	 break;
	    }
	    cursor.close();
	    start -= offset;
	    long end = start + numDays * 24 * 60 * 60 * 1000;
	    
	    // Get events to display
	    query = "SELECT CAST(substr(Event.StartTimeWithOffsetDateTime, 7, 13) as INTEGER), Event.StartTimeWithOffsetOffsetMinutes, Event._id, Event.MySchedule, Event.ItemId, Item.* " +
	    				"FROM Item JOIN Event on Item._id = Event.ItemId " +
	    				"WHERE CAST(substr(Event.StartTimeWithOffsetDateTime, 7, 13) as INTEGER) BETWEEN " + start + " AND "+ end + " ORDER BY Event.StartTime";
	    System.out.println("Query: "+ query);
	    cursor = sharedDb.rawQuery(query, null);
	    System.out.println("Found "+cursor.getCount()+" events");
	    while(cursor.moveToNext())
	    {
	    	HashMap<String, Object> item = new HashMap<String, Object>();
	    	item.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
	    	item.put("ItemId", cursor.getString(cursor.getColumnIndex("ItemId")));
	    	item.put("ImageFileName", cursor.getString(cursor.getColumnIndex("ImageFileName")));
	    	item.put("ImageFolderName", cursor.getString(cursor.getColumnIndex("ImageFolderName")));
	    	item.put("ImageId", cursor.getInt(cursor.getColumnIndex("ImageId")));
	    	item.put("ImageUrl", cursor.getString(cursor.getColumnIndex("ImageUrl")));
	    	item.put("ItemType", cursor.getString(cursor.getColumnIndex("ItemType")));
	    	item.put("Name", cursor.getString(cursor.getColumnIndex("Name")));   	
	    	item.put("Content", cursor.getString(cursor.getColumnIndex("Content")));
	    	System.out.println("Name:"+cursor.getString(cursor.getColumnIndex("Name")));
	    	
	    	Long dateNum = cursor.getLong(0) + cursor.getLong(1) * 60 * 1000;    // Multiply offset by 60 and 1000 to convert minutes to milliseconds	
	    	Date date = new Date(dateNum);
	    	SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
	    	item.put("Epoch", date.getTime());
	    	item.put("Date", sdf.format(date));
	    	sdf = new SimpleDateFormat("h:mm a");
	    	item.put("Time", sdf.format(date));
	    	item.put("EventId", cursor.getInt(2));
	    	item.put("MySchedule", cursor.getInt(3));
	    	events.add(item);
	    }
	    cursor.close();
	    
		return events;
	}
	
	public static Intent getIntentForItem(Context ctx, int id) {
		// Get shared database reference
	    SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
		HashMap<String, Object> item = new HashMap<String, Object>();
	    Cursor cursor = sharedDb.rawQuery("SELECT * FROM Item WHERE _id="+id, null);
		while(cursor.moveToNext())
		{
			item.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
			item.put("ImageFileName", cursor.getString(cursor.getColumnIndex("ImageFileName")));
			item.put("ImageFolderName", cursor.getString(cursor.getColumnIndex("ImageFolderName")));
			item.put("ImageId", cursor.getInt(cursor.getColumnIndex("ImageId")));
			item.put("ImageUrl", cursor.getString(cursor.getColumnIndex("ImageUrl")));
			item.put("ItemType", cursor.getString(cursor.getColumnIndex("ItemType")));
			item.put("Name", cursor.getString(cursor.getColumnIndex("Name")));
			item.put("Content", cursor.getString(cursor.getColumnIndex("Content")));
		}
		cursor.close();
		return getIntentForItem(ctx, item);
	}
	
	@SuppressWarnings("unchecked")
	public static Intent getIntentForItem(Context ctx, HashMap<String, Object> item) {
		if(item == null)
			return null;
		String itemType = (String) item.get("ItemType");
    	Intent intent = new Intent();
    	intent.putExtra("Name", (String)item.get("Name"));
   		intent.putExtra("Content", (String)item.get("Content"));
    	intent.putExtra("ItemType", itemType);
    	Integer id = ((Integer) item.get("_id"));
        intent.putExtra("Id",id.intValue());
        Object path = null;
        try {
        	path = (HashMap<Integer, Object>)(item.get("Path"));
        	if(path != null) {
        		intent.putExtra("Path", (HashMap<Integer, Object>)path);
        	}
        } catch (ClassCastException cce)  {
        	path = (String) item.get("Path");
        }
        
    	//Create the activities for each tab.
    	if(itemType.equals("Map")) {
    		intent.setClass(ctx, AmpMapActivity.class);
    	}
    	else if(itemType.equals("Menu")) {
    		intent.setClass(ctx, MenuActivity.class);
    	}
    	else if(itemType.equals("Schedule")) {
    		intent.setClass(ctx, ScheduleActivity.class);
    	}
    	else if(itemType.equals("Location")) {
    		ArrayList<HashMap<String, Object>> locationDetailItems = null;
    		System.out.println("ItemType is location, path is: " + path);
    		if(path != null) {
    			locationDetailItems = NavigationHelper.getEventsAndItemsAtLocation((Integer)item.get("_id"), (String)path);
    			if(locationDetailItems != null) {
    				System.out.println("got "+locationDetailItems.size()+" locationDetailItems!");
    				HashMap<String, Object> newItem = new HashMap<String, Object>();
    				newItem.putAll(item);
    				newItem.remove("Path");
    				locationDetailItems.add(0, newItem);
    				intent.setClass(ctx, MenuActivity.class);
    				intent.putExtra("LocationDetailItems", locationDetailItems);
    			}else {
    				intent.setClass(ctx, PageActivity.class);
    			}
    		} else {
    			intent.setClass(ctx, PageActivity.class);
    		}
    	}
    	else if(itemType.equals("Page") || itemType.equals("Event")) {
    		intent.setClass(ctx, PageActivity.class);
    	}
    	else if(itemType.equals("Twitter")) {
    		intent.setClass(ctx, TwitterActivity.class);
    	} 
    	else if(itemType.equals("Url")) {
    		intent.setClass(ctx, UrlActivity.class);
    	}
    	else if(itemType.equals("Header") || itemType.equals("NoItems")) {
    		intent = null;
    	}
    	else if(itemType.equals("Profile")) {
    		intent.setClass(ctx, ProfileActivity.class);
    	}
    	else if(itemType.equals("FriendFinder")) {
    		intent.setClass(ctx, FriendFinderActivity.class);
    	}
    	else if(itemType.equals("Survey")) {
    		intent.setClass(ctx, SurveyActivity.class);
    	}
    	else{
    		System.out.println("Hmm, itemType is \"" + itemType + ".\"  What am I supposed to do with that?");
    	}

        return intent;
	}
	
	public static void printHashMap(HashMap<String, Object> hashmap) {
		Iterator<String> iter = hashmap.keySet().iterator();
		while(iter.hasNext()) {
			String key = (String) iter.next();
			System.out.println(key + ": " + hashmap.get(key));
		}
	}

	public static ArrayList<HashMap<String, Object>> getSurveyQuestions(int rootId) {
		// Get shared database reference
	    SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();

	    String questionQuery = "SELECT _id, Text from SurveyQuestion WHERE SurveyId=" + rootId + " ORDER BY Rank";
	    Cursor questionCursor = sharedDb.rawQuery(questionQuery, null);
	    System.out.println("Found "+questionCursor.getCount()+" questions");
	    
	    ArrayList<HashMap<String, Object>> questions = new ArrayList<HashMap<String, Object>>();
	    while(questionCursor.moveToNext())
	    {
	    	HashMap<String, Object> question = new HashMap<String, Object>();

	    	int questionId = questionCursor.getInt(questionCursor.getColumnIndex("_id"));
	    	question.put("_id", questionId);
	    	question.put("Text", questionCursor.getString(questionCursor.getColumnIndex("Text")));
	    	
	    	ArrayList<HashMap<String, Object>> answers = new ArrayList<HashMap<String, Object>>();
	    	String answerQuery = "SELECT _id, Text from SurveyAnswer WHERE SurveyQuestionId=" + questionId + " ORDER BY Rank";
	 	    Cursor answerCursor = sharedDb.rawQuery(answerQuery, null);
	 	    System.out.println("Found "+answerCursor.getCount()+" answers");
	 	    while(answerCursor.moveToNext())
	 	    {
	 	    	HashMap<String, Object> answer = new HashMap<String, Object>();
	 	    	answer.put("_id", answerCursor.getInt(answerCursor.getColumnIndex("_id")));
	 	    	answer.put("Name", answerCursor.getString(answerCursor.getColumnIndex("Text")));
	 	    	answer.put("ItemType", "SurveyAnswer");
	 	    	answer.put("CheckmarkDrawable", R.drawable.com_butt_checkbox_off);
	 	    	answers.add(answer);
	 	    }
	 	    answerCursor.close();
	 	    question.put("Answers", answers);
	    	questions.add(question);
	    }
	    questionCursor.close();
	    return questions;	
	}
}
