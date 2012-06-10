package com.avai.wpzoo;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
	
    private final String myDbPath;
    private final String myDbName; 
    private static SQLiteDatabase myDatabase; 
    private final Context mCtx;
    private static DatabaseHelper ref;
    /**
     * Constructor
     * Takes and keeps a reference of the passed ctx in order to access to the application assets and resources.
     * @param ctx
     */
    private DatabaseHelper(Context ctx, String dbName) {
    	super(ctx, dbName, null, 1);
        this.mCtx = ctx;
        this.myDbPath = "/data/data/" + mCtx.getPackageName() + "/databases/";
        this.myDbName = dbName;
        try {
        	System.out.println("Trying to create database...");
        	this.createDatabase();
        } catch (IOException ioe){
        	throw new Error("Unable to create database");
        }
        try {
        	myDatabase = this.openDatabase(); 
        }catch (SQLException sqle) {
        	throw sqle;
        }
    }
    
    public static boolean databaseReady() {
    	return (myDatabase != null);
    }
 
    public static SQLiteDatabase sharedDb(Context ctx, String dbName){
    	if(ref == null) {
    		ref = new DatabaseHelper(ctx, dbName);
    	}
    	return myDatabase; 
    }
    
    public static SQLiteDatabase sharedDb(){
    	if(ref == null)
    		return null;
    	return myDatabase;
    }  
    
    public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }
    
    // Creates a empty database on the system and rewrites it with your own database.
    private void createDatabase() throws IOException{
     	if(checkDatabase()){
     		// If database already exists
     		System.out.println("Database already exists");
     	}else{
    		//By calling this method an empty database will be created into the default system path
            //of your application so we will be able to overwrite that database with our database.
    		this.getReadableDatabase();
        	try {
    			copyDatabase();
    		} catch (IOException e) {
        		System.out.println("Error copying database");
        	}
    		this.close();
    	}
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDatabase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = myDbPath + myDbName;
    		System.out.println("myPath: "+myPath);
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    		
    	}catch(SQLiteException e){
    		//database does't exist yet.
    	}
 
    	if(checkDB != null){
    		checkDB.close();
    	}
    	else {
    	}
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transferring bytestream.
     * */
    private void copyDatabase() throws IOException{
    	System.out.println("Trying to copy database with name: "+myDbName);
    	
    	//Open the empty db as the output stream
    	String outFileName = myDbPath + myDbName;
    	OutputStream myOutput = new FileOutputStream(outFileName);
   	
    	InputStream myInput;
    	char suffix = 'a';
    	//Open your local db as the input stream
    	try {
	    	while(true) {  // Loop until we get an IOException for a file that doesn't exist.
	    		String dbPieceName = myDbName + 'a' + suffix;
	    		System.out.println("dbPieceName: " + dbPieceName);
	    		myInput = mCtx.getAssets().open(dbPieceName);
		    	System.out.println("successfully opened "+dbPieceName);
		    	//transfer bytes from the input file to the output file
		    	byte[] buffer = new byte[1024];
		    	int length;
		    	while ((length = myInput.read(buffer))>0){
		    		myOutput.write(buffer, 0, length);
		    	}
		    	System.out.println("successfully wrote "+dbPieceName+ " to output stream");
		    	++suffix;
		    	myInput.close();
		    	
		    	
	    	}
	    } catch (IOException ioe) {
	    	System.out.println("IOException thrown when trying to open an asset");
	    }
	    
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	
    }
 
    private SQLiteDatabase openDatabase() throws SQLException{
 
    	//Open the database
        String myPath = myDbPath + myDbName;
    	return SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDatabase != null)
    		    myDatabase.close();
 
    	    super.close();
 
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}

	public static void updateMySchedule(int id, boolean checked) {
		SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
		ContentValues values = new ContentValues();
		values.put("MySchedule", checked ? 1 : 0);
		if(sharedDb.update("Event", values, "_id="+id, null) == 0)
			System.out.println("Update of my schedule failed!");
	}

	public static boolean getMySchedule(int id) {
		SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
		Cursor cursor = sharedDb.query("Event", new String[]{"_id", "MySchedule"}, "_id="+id, null, null, null, null);
		int state = 0;
		while(cursor.moveToNext()){
			state = cursor.getInt(cursor.getColumnIndex("MySchedule"));
		}
		cursor.close();
		return (state == 1) ? true : false;
	}
	
	public static String getItemExtraProperty(int itemId, String propertyName) {
		String propValue = null;
		SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
		Cursor cursor = sharedDb.query("ItemExtraProperties", new String[]{"ItemId", "PropertyName", "Value"}, "PropertyName=\""+propertyName+"\" AND ItemId="+itemId, null, null, null, null);
		while(cursor.moveToNext()){
			propValue = cursor.getString(cursor.getColumnIndex("Value"));
		}
		cursor.close();
		return propValue;
	}

	public static String getAppDomainSetting(String propertyName) {
		String propValue = null;
		SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
		Cursor cursor = sharedDb.query("AppDomainSettings", new String[]{"Name", "Value"}, "Name=\""+propertyName+"\"", null, null, null, null);
		while(cursor.moveToNext()){
			propValue = cursor.getString(cursor.getColumnIndex("Value"));
		}
		cursor.close();
		return propValue;
	}

	public static MyPoint getLocationForItem(int landmarkId) {
		SQLiteDatabase sharedDb = DatabaseHelper.sharedDb();
		Cursor cursor = sharedDb.query("Location", new String[]{"ItemId", "Latitude", "Longitude"}, "ItemId=\""+landmarkId+"\"", null, null, null, null);
		double latitude = 0, longitude = 0;
		while(cursor.moveToNext()){
			latitude = cursor.getDouble(cursor.getColumnIndex("Latitude"));
			longitude = cursor.getDouble(cursor.getColumnIndex("Longitude"));
		}
		cursor.close();
		return new MyPoint(latitude, longitude);
	}

	public static int getMapId(int locationId) {
		System.out.println("DatabaseHelper getMapId: "+locationId);
		String path = null;
		
		Cursor cursor = DatabaseHelper.sharedDb().query("ItemSubItem", new String[]{"Path"}, "ChildId=\""+locationId+"\"", null, null, null, null);
		while(cursor.moveToNext()){
			path = cursor.getString(cursor.getColumnIndex("Path"));
		}
		cursor.close();
		String pathComponents[] = path.split("/");
		for(int i=0;i<pathComponents.length;i++) {
			try {
				int id = Integer.parseInt(pathComponents[i]);
				if(isLocationAwareMap(id))
					return id;
			} catch (NumberFormatException nfe) {}
		}
		return 0;
	}

	private static boolean isLocationAwareMap(int id) {
		boolean aware = false;
		Cursor cursor = DatabaseHelper.sharedDb().rawQuery("SELECT Item._id FROM Item" +
				" INNER JOIN ItemExtraProperties on Item._id = ItemExtraProperties.ItemId" +
				" WHERE Item._id=" + id + " AND ItemExtraProperties.PropertyName = 'LocationAware'" +
				" AND (ItemExtraProperties.Value = 'True' OR ItemExtraProperties.Value = 'true')", null);
		while(cursor.moveToNext()) {
			aware = true;
		}
		cursor.close();
		return aware;
	}

	public static int getLocationIdForId(int id) {
		int locationId = -1;
		Cursor cursor = DatabaseHelper.sharedDb().rawQuery("SELECT ItemLocation.LocationId FROM ItemLocation" +
				" INNER JOIN Item ON Item._id=ItemLocation.ItemId WHERE ItemLocation.ItemId=" + id, null);
		while(cursor.moveToNext()) {
			locationId = cursor.getInt(cursor.getColumnIndex("LocationId"));
		}
		cursor.close();
		return locationId;
	}
	
	public static int getLocationIdForEventId(int id) {
		int locationId = -1;
		Cursor cursor = DatabaseHelper.sharedDb().rawQuery("SELECT LocationIds FROM Event WHERE ItemId=" + id, null);
		while(cursor.moveToNext()) {
			String locationIdStr = cursor.getString(cursor.getColumnIndex("LocationIds"));
			locationId = Integer.parseInt(locationIdStr.substring(1, locationIdStr.length()-1));
		}
		cursor.close();
		return locationId;
	}

	public static ArrayList<HashMap<String, Object>> getFriends() {
		ArrayList<HashMap<String, Object>> friends = new ArrayList<HashMap<String, Object>>();
		Cursor cursor = DatabaseHelper.sharedDb().rawQuery("SELECT * FROM Friends", null);
		while(cursor.moveToNext()) {
			HashMap<String, Object> friend = new HashMap<String, Object>();
			friend.put("Name", cursor.getString(cursor.getColumnIndex("Name")));
			friend.put("Latitude", new Double(cursor.getDouble(cursor.getColumnIndex("Latitude"))));
			friend.put("Longitude", new Double(cursor.getDouble(cursor.getColumnIndex("Longitude"))));
			friend.put("Pin", new Integer(cursor.getInt(cursor.getColumnIndex("Pin"))));
			friend.put("SessionTimedOut", cursor.getString(cursor.getColumnIndex("SessionTimedOut")));
			friend.put("LogTime", cursor.getString(cursor.getColumnIndex("LogTime")));
			friends.add(friend);
		}
		cursor.close();
		return friends;
	}

	public static void saveFriend(HashMap<String, Object> friend) {
		boolean friendExists = false;
		Integer pin = (Integer) friend.get("Pin");
		
		Cursor cursor = DatabaseHelper.sharedDb().rawQuery("SELECT Pin FROM Friends WHERE Pin=" + pin.intValue(), null);
		while(cursor.moveToNext()) {
			friendExists = true;
		}
		cursor.close();
		ContentValues values = new ContentValues();
		values.put("Pin", pin);
		values.put("Name", (String) friend.get("Name"));
		values.put("Latitude", (Double) friend.get("Latitude"));
		values.put("Longitude", (Double) friend.get("Longitude"));
		values.put("SessionTimedOut", (Boolean) friend.get("SessionTimedOut"));
		values.put("LogTime", (String) friend.get("LogTime"));
		if(friendExists) { 
			DatabaseHelper.sharedDb().update("Friends", values, "Pin=" + pin.intValue(), null);
		} else {
			DatabaseHelper.sharedDb().insert("Friends", null, values);
		}
	}
}
