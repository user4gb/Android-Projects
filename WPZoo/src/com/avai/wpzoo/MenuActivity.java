package com.avai.wpzoo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.flurry.android.FlurryAgent;

public class MenuActivity extends ListActivity {
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
	   if(pd != null)
		   pd.dismiss();
	} 
    
	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setTitle((CharSequence)(getIntent().getStringExtra("Name")));
      
      // Get the data
      int rootId = getIntent().getIntExtra("Id", 0);
      final ArrayList<HashMap<String, Object>> menuItems;
      HashMap<Integer, Object> path = null;
      try {
    	  path =  (HashMap<Integer, Object>) getIntent().getExtras().get("Path");     	  
      } catch (ClassCastException cce) {
    	  System.out.println("ClassCastException!!!");
      }
      ArrayList<HashMap<String, Object>> locationDetailItems = (ArrayList<HashMap<String, Object>>)getIntent().getExtras().get("LocationDetailItems");    	  
      if(locationDetailItems == null) {
    	  if(path == null) {
    		  menuItems = NavigationHelper.getMenuItems(rootId);
    	  } else {
    		  menuItems = NavigationHelper.getItemsForTree(path);
    	  }
      } else {
    	  menuItems = locationDetailItems;
      }
      
      System.out.println("Got "+menuItems.size()+" menu items");
      setContentView(R.layout.menu);
      setListAdapter(new MenuAdapter(this, menuItems));
      
      ListView lv = getListView();
      lv.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
      lv.setDividerHeight(0);
      lv.setOnItemClickListener(new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Intent intent = new Intent();
			HashMap<String, Object> item = (HashMap<String, Object>) menuItems.get((int) id);
			NavigationHelper.printHashMap(item);
			intent = NavigationHelper.getIntentForItem(getApplicationContext(), item);
			if(intent != null) {
				showLoadingDialog(intent.getStringExtra("Name"));
				startActivity(intent);
			}
		}
      }); 
    }
	
	private void showLoadingDialog(String text) { 
		pd = ProgressDialog.show(this, "Loading...", text, true, false);
	}
}
