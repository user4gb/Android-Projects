package com.avai.wpzoo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.flurry.android.FlurryAgent;

public class LocationsActivity extends ListActivity{
	private String searchQuery;
	private boolean nearMe;
	private ArrayList<HashMap<String, Object>> locationItems;
	private MyPoint currLocUgs;
//	private ProgressDialog pd;
	private final int SEARCH = 1;
    
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
//	   if(pd != null)
//		   pd.dismiss();
	} 
    
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		System.out.println("Updating locations list with search query: "+searchQuery + " and nearMe = "+nearMe);		
        setContentView(R.layout.locations);
		nearMe = false;
		
		currLocUgs = new MyPoint(getIntent().getDoubleExtra("nearMeLat", 0), getIntent().getDoubleExtra("nearMeLon", 0));
		RadioGroup group = (RadioGroup)findViewById(R.id.toggle);
		if(currLocUgs == null) {
			//Disable the near me feature
			group.setVisibility(View.GONE);
		}
		else {
			// Configure the near me toggle
			nearMe = getIntent().getBooleanExtra("nearMe", nearMe);
	        group.setVisibility(View.VISIBLE);
	        RadioButton leftButton = (RadioButton) findViewById(R.id.leftButton);
	        RadioButton rightButton = (RadioButton) findViewById(R.id.rightButton);
	        
	        System.out.println("Setting right button to : " + nearMe);
	        if(nearMe)
	        	rightButton.toggle();
	        leftButton.setText("All Locations");
	        rightButton.setText("Near Me");
	        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(RadioGroup group, int checkedId) {
	        		nearMe = (checkedId == R.id.rightButton);
	        		updateListContent();
				}
	        });
		}
		
		searchQuery = getIntent().getStringExtra(SearchManager.QUERY);
		if(searchQuery == null)
			searchQuery = "";
        
        RelativeLayout locationsLayout = (RelativeLayout) findViewById(R.id.locations_layout);
        locationsLayout.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
        
        updateListContent();
        
        ListView lv = getListView();
        lv.setDividerHeight(0);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		if(locationItems.size() > 0) {
	        		Intent intent = new Intent();
	        		HashMap item = (HashMap) locationItems.get((int) id);
	        		NavigationHelper.printHashMap(item);
	        		intent = NavigationHelper.getIntentForItem(getApplicationContext(), item);
	        		if(intent != null)
//	        			showLoadingDialog(intent.getStringExtra("Name"));
	        			startActivity(intent);
        		}
        	}
        });
        
        final TextView searchbox = (TextView)findViewById(R.id.searchbox);
        if(searchbox != null) {
        	searchbox.setText("    "+searchQuery);
	        searchbox.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
			    	setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
			        onSearchRequested();
				}
	        });
        } else {
        	System.out.println("searchbox is null");
        }
        
        Button cancelbutton = (Button) findViewById(R.id.cancelbutton);
        cancelbutton.setOnClickListener(new OnClickListener() {
        	public void onClick(View arg0) {
        		if(!searchQuery.equals("")) {
        			String message = "Loading all locations";
        			if(nearMe) message = message + " near me";
//       			showLoadingDialog(message);
        			searchQuery = "";
        			searchbox.setText(searchQuery);
        			updateListContent();
        		}
        	}
        });
	}
/*	
	private void showLoadingDialog(String text) {
		System.out.println("showLoadingDialog   "+text);
		pd = ProgressDialog.show(this, "Loading...", text, true);
	}
*/	
	@Override
	public boolean onSearchRequested() {
		Bundle bundle = new Bundle();
		bundle.putBoolean("nearMe", false);
		bundle.putDouble("nearMeLat", currLocUgs.x);
		bundle.putDouble("nearMeLon", currLocUgs.y);
		startSearch(searchQuery, false, bundle, false);
		updateListContent();
		return true;
	}
	
	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
	    menu.add(0, SEARCH, 0, "Search").setIcon(android.R.drawable.ic_search_category_default).setAlphabeticShortcut(SearchManager.MENU_KEY);
	    return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case SEARCH:
	    	setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
	        onSearchRequested();
	        return true;
	    }
	    return false;
	}
	
	private void updateListContent() {
//		pd = ProgressDialog.show(LocationsActivity.this, " " , " Loading. Please wait ... ", true);
		int rootId = getIntent().getIntExtra("Id",0);
		MyPoint nearMeLocation = (nearMe ? currLocUgs : null);
		locationItems = NavigationHelper.getItemsForTree(getLocationTree(getLocationPaths(rootId, searchQuery, nearMeLocation)));
		if(locationItems.size() > 0) {
			setListAdapter(new MenuAdapter(this, locationItems));
		} else {
			if(nearMe) {
				if(searchQuery.equals("")){
					setListAdapter(new MenuAdapter(this, getNoItemsTable("No locations are near you.")));
				} else {
					setListAdapter(new MenuAdapter(this, getNoItemsTable("No results for locations near you.")));
				}
			} else {
				if(searchQuery.equals("")){
		        	setListAdapter(new MenuAdapter(this, getNoItemsTable("No locations to display.")));
		        } else {
		        	setListAdapter(new MenuAdapter(this, getNoItemsTable("No search results found.")));
		        }
			}
		}
		//Dismiss the loading dialog box
//		if(pd != null)
//			pd.dismiss();
	}
 
	private ArrayList<HashMap<String, String>> getNoItemsTable(String text) {
		ArrayList<HashMap<String, String>> noItems = new ArrayList<HashMap<String, String>>();
		HashMap<String, String> noItemsTable = new HashMap<String, String>();
		noItemsTable.put("ItemType", "NoItems");
		noItemsTable.put("Name", text);
		noItems.add(noItemsTable);
		return noItems;
	}
	
	private ArrayList<String> getLocationPaths(int rootId, String searchQuery, MyPoint currLocUgs) {
		String query = "";
		if(searchQuery == null)
			searchQuery = "";
		if(currLocUgs == null) {
			query =
				"SELECT Path || MatchingLocationId || '/' FROM" +
				" (SELECT DISTINCT Event.LocationIds AS MatchingLocationId FROM" +
				
				" (SELECT Item._id AS MatchingItemId FROM Item" +
				" LEFT JOIN ItemKeywords ON ItemKeywords.ItemID = Item._id " +
				" LEFT JOIN Keywords ON Keywords._id = ItemKeywords.KeywordID " +
				" WHERE (Item.Name LIKE '%%" + searchQuery +"%%' OR Item.Content LIKE '%%" + searchQuery +"%%'"+ 
				" OR Keywords.Keyword LIKE '%%" + searchQuery +"%%') AND Item.ItemType = 'Event')" +
				" INNER JOIN Event on Event.ItemId = MatchingItemId" +
				
				" UNION" +
				" SELECT Item._id AS MatchingLocationId FROM Item " +
				" LEFT JOIN ItemKeywords ON ItemKeywords.ItemID = Item._id " +
				" LEFT JOIN Keywords ON Keywords._id = ItemKeywords.KeywordID " +
				" WHERE (Item.Name LIKE '%%" + searchQuery +"%%' OR Item.Content LIKE '%%" + searchQuery +"%%'" +
				" OR Keywords.Keyword LIKE '%%" + searchQuery +"%%') AND Item.ItemType = 'Location' " +
				
				" UNION " +
				" SELECT DISTINCT ItemLocation.LocationId AS MatchingLocationId FROM " +
				" (SELECT Item._id AS MatchingItemId FROM Item " +
				" LEFT JOIN ItemKeywords ON ItemKeywords.ItemID = Item._id " +
				" LEFT JOIN Keywords ON Keywords._id = ItemKeywords.KeywordID " +
				" WHERE (Item.Name LIKE '%%" + searchQuery +"%%' OR Item.Content LIKE '%%" + searchQuery +"%%'" +
				" OR Keywords.Keyword LIKE '%%" + searchQuery +"%%') AND Item.ItemType = 'Page') " +
				
				" INNER JOIN ItemLocation ON ItemId = MatchingItemId) " +
				
				" INNER JOIN ItemSubItem ON ItemSubItem.ChildId = MatchingLocationId ORDER BY Path";
			
		/*	query = "SELECT DISTINCT ItemSubItem.Path || Item._id || '/' FROM Item" +
				" JOIN ItemSubItem ON ItemSubItem.ChildId = Item._id" +  
				" JOIN ItemKeywords ON ItemKeywords.ItemId = Item._id" +
				" JOIN Keywords ON Keywords._id = ItemKeywords.KeywordID " +
				" WHERE (Item.ItemType = 'Page' OR Item.ItemType = 'Location')" +
				" AND (Item.Name LIKE '%%" + searchQuery +"%%' OR Item.Content LIKE '%%" + searchQuery +"%%' OR Keywords.Keyword LIKE '%%" + searchQuery +"%%') " +
				" ORDER BY ItemSubItem.Path";*/
		} else { 
			double nearMeLat = Double.parseDouble(DatabaseHelper.getAppDomainSetting("NearMeLat"));
			double nearMeLon = Double.parseDouble(DatabaseHelper.getAppDomainSetting("NearMeLon"));
			nearMeLat = nearMeLon = 0.001;
			
			double westBound = (double) (currLocUgs.x - nearMeLat);
			double eastBound = (double) (currLocUgs.x + nearMeLat);
			double southBound = (double) (currLocUgs.y - nearMeLon);
			double northBound = (double) (currLocUgs.y + nearMeLon);
			query = 
				"SELECT Path || MatchingLocationId || '/' FROM" +
				" (SELECT DISTINCT ItemLocation.LocationId AS MatchingLocationId FROM" +
				" (SELECT Item._id AS MatchingItemId FROM Item" +
				" LEFT JOIN ItemKeywords ON ItemKeywords.ItemID = Item._id" +
				" LEFT JOIN Keywords ON Keywords._id = ItemKeywords.KeywordID" +
				" WHERE (Item.Name LIKE '%%"+searchQuery+"%%' OR Item.Content LIKE '%%"+searchQuery+"%%'"+
				" OR keywords.keyword LIKE '%%"+searchQuery+"%%') AND Item.ItemType = 'Page')" +
				" INNER JOIN ItemLocation ON ItemLocation.ItemId = MatchingItemId" +
				" UNION" +
				" SELECT Item._id AS MatchingLocationId FROM Item" +
				" LEFT JOIN ItemKeywords on ItemKeywords.ItemID = Item._id" +
				" LEFT JOIN Keywords on Keywords._id = ItemKeywords.KeywordID" +
				" WHERE (Item.Name LIKE '%%"+searchQuery+"%%' OR Item.Content LIKE '%%"+searchQuery+"%%'"+
				" OR Keywords.Keyword LIKE '%%"+searchQuery+"%%') AND Item.ItemType = 'Location')" +
				" INNER JOIN ItemSubItem on ItemSubItem.ChildId = MatchingLocationId" +
				" inner join Location on MatchingLocationId = Location.ItemId WHERE" +
				" (Location.Latitude BETWEEN " + westBound + " AND " + eastBound + ")" +
				" AND (Location.Longitude BETWEEN " + southBound + " AND " + northBound + ") ORDER BY Path";
		} 
		Cursor cursor = DatabaseHelper.sharedDb().rawQuery(query, null);
		System.out.println("Found " + cursor.getCount() + " items in response to searchQuery " + searchQuery + " and nearMe " + (currLocUgs != null));
		ArrayList<String> paths = new ArrayList<String>();
		while(cursor.moveToNext()) {
			String path = cursor.getString(0);
			paths.add(removeMapPrefix(path));
		}
		cursor.close();
		return paths;
	}
	
	private String removeMapPrefix(String path) {
		return path.substring(7, path.length());
	}

	private HashMap<Integer, Object> getLocationTree(ArrayList<String> paths) {
		HashMap<Integer, Object> locationTree = new HashMap<Integer, Object>();
		for(int i=0;i<paths.size();i++) {
			String path = paths.get(i);
			String[] pathComponents = path.substring(1,path.length()-1).split("/");
			locationTree = insertPath(locationTree, pathComponents);
		}
		return locationTree;
	}
	
	//Recursive function
	private HashMap<Integer, Object> insertPath(HashMap<Integer, Object> branch, String[] path) {
		Integer id = 0;
		if(path[0] != null)
			id = Integer.valueOf(path[0]);
		
		// Base Case
		if(path.length == 1) {
				branch.put(id, searchQuery);
			return branch;
		}
		// Recursive Case
		else {			
			//Remove the first element from the path array
			String[] newPath = new String[path.length-1]; 
			for(int i=0;i<path.length-1;i++) {
				newPath[i] = path[i+1];
			}
			
			//Get the new branch
			HashMap<Integer, Object> newBranch;
			if(branch.containsKey(id)) {
				newBranch = (HashMap<Integer, Object>) branch.get(id);
			} else {
				newBranch = new HashMap<Integer, Object>();
			}
			branch.put(id, insertPath(newBranch, newPath));
			return branch;
		}
	}
}
