package com.avai.wpzoo;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ContentSyncer {
	
    private SQLiteDatabase sharedDb;
    public Context mCtx;
	final String localIdName = "_id";
    
	/**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     * @throws SQLException if the database could be neither opened or created
     */
    public ContentSyncer(Context ctx) throws SQLException {
    	this.mCtx = ctx;
    	System.out.println("ContentSyncer constructor.  Creating DatabaseHelper with dbName " + Constants.sharedConstants().dbName);
    	while(!DatabaseHelper.databaseReady());
    	sharedDb = DatabaseHelper.sharedDb(mCtx, Constants.sharedConstants().dbName);    	
    }

	@SuppressWarnings("unchecked")
	public boolean syncWebService(String tableName, String service, String serviceIdName, String serviceRevisionName) {
		// Determine the current revision
		int revNum = 0;
		Cursor cursor = sharedDb.rawQuery("SELECT MAX(" + serviceRevisionName + ") FROM " + tableName, null);
		while(cursor.moveToNext()){
			revNum = cursor.getInt(0);
		}
		cursor.close();
		String serviceUrl = String.format(service, revNum);
		System.out.println("Syncing " + tableName + " with service: " + serviceUrl);

		JSONObject obj = HttpHelper.getJSONObject(mCtx, serviceUrl);
		if(obj == null) {
			return false;
		}else {
	        try{
	        	JSONArray addedOrUpdated = null;
	            JSONArray deleted = null;
				addedOrUpdated = obj.getJSONArray("AddedOrUpdated");
	        	deleted = obj.getJSONArray("Deleted");
	            sharedDb.beginTransaction();
	         
	            // CHECK IF LOCAL TABLE HAS NECESSARY COLUMNS
	        	cursor = sharedDb.rawQuery("PRAGMA table_info("+tableName+")", null);
	        	ArrayList columnNames = new ArrayList();
	        	while(cursor.moveToNext()) {
	        		columnNames.add(cursor.getString(1)); //column 1 of the table_info query is the column name.
	        	}
	        	cursor.close();
	            
	            for(int i=0; i<addedOrUpdated.length(); i++){
	            	JSONObject row = addedOrUpdated.getJSONObject(i);
	            	
	            	//GET VALUES
	            	ContentValues values = new ContentValues();
	            	Iterator iter = row.keys();
	            	while(iter.hasNext()) {
	            		String key = (String)iter.next();
	            		if(!key.equals(serviceIdName))
	            		{
	    	            	if(columnNames.contains(key)) {
	    	            		values.put(key, row.getString(key));
	    	            	} else {
	    	            		try {
	    	            			JSONObject subRow = row.getJSONObject(key);
    	            				Iterator subIter = subRow.keys();
    	            				while(subIter.hasNext()) {
    	            					String subKey = (String)subIter.next();
    	            					if(columnNames.contains(key+subKey)) {
    	            						values.put(key+subKey, subRow.getString(subKey));
    	            					} else {
    	            						System.out.println("Warning: Not saving " + key+subKey + " because it doesn't exist in the local database.");
    	            					}
	    	            			}
	    	            		} catch(JSONException jse) {
	    	            			System.out.println("Warning: Not saving " + key + " because it doesn't exist in the local database.");
	    	            		}
	    	            	}
	            		}
	            	}
	            	// CHECK IF RECORD EXISTS
	            	cursor = sharedDb.query(tableName, new String[]{localIdName}, localIdName + "=" + row.getInt(serviceIdName), null, null, null, null, null);
	            	boolean recordExists = (cursor.getCount() > 0);
	            	cursor.close();
	            	//IF RECORD EXISTS, USE UPDATE
	            	if(recordExists){
	            		if(sharedDb.update(tableName, values, localIdName + "=" + row.getInt(serviceIdName), null) == 0)
	            			System.out.println("Update failed");
	            		else{
	            			System.out.println("Update succeeded");
	            		}
	            	} else {
	            	// IF RECORD DOESN'T EXIST, USE INSERT
	            		values.put(localIdName, row.getString(serviceIdName));
	            		if(sharedDb.insert(tableName, null, values) == 0){
	            			System.out.println("Insert failed");
	            		}
	            		else{
	            			System.out.println("Insert succeeded");
	            		}
	            	}
	            }
	            
	            for(int i=0; i<deleted.length(); i++) {
	            	int id = deleted.getInt(i);
	            	sharedDb.delete(tableName, localIdName + "=" + id, null);
	            }
	            
	            sharedDb.setTransactionSuccessful();
	        }
	        catch(JSONException e) {
	        	throw new Error("Error parsing JSON data");
	        }
	        finally{
	        	sharedDb.endTransaction();
	        	System.out.println("Done syncing " + tableName);
	        }
		}
		return true;
	}
	

}
