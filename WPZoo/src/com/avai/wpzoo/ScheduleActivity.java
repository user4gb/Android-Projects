package com.avai.wpzoo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.avai.wpzoo.Constants.ScheduleType;
import com.flurry.android.FlurryAgent;

public class ScheduleActivity extends ListActivity {
	private ProgressDialog pd;
	private ArrayList<HashMap<String, Object>> events, filteredEvents, noEvents;
    static boolean filteredSchedule;
    
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
        
        // Get the data
        System.out.println("Getting events...");
        Date today = new Date();
        today.setHours(0);
        today.setMinutes(0);
        today.setSeconds(0);
        
        //Create events, filteredEvents, and noEvents
        events = NavigationHelper.getEvents(today, Constants.sharedConstants().scheduleNumDays);
        System.out.println("Got events");
        filteredEvents = new ArrayList();
        for(int i=0;i<events.size();i++) {
        	if(((Integer)((HashMap)events.get(i)).get("MySchedule")).intValue() == 1)
        		filteredEvents.add(events.get(i));
        }
        
        noEvents = new ArrayList();
		HashMap noEventsTable = new HashMap();
		noEventsTable.put("EventId", -1);
		noEventsTable.put("Name", "No Events.");
		noEvents.add(noEventsTable);
        
        //When the view first loads, show all events.
        filteredSchedule = false;
        
        setContentView(R.layout.schedule);
        
        // Set the background
        LinearLayout scheduleLayout = (LinearLayout) findViewById(R.id.schedule_layout);
        scheduleLayout.setBackgroundDrawable(Constants.sharedConstants().backgroundImage);
        
        
        setListAdapter(new MenuAdapter(this, events));
        
        RadioGroup group = (RadioGroup)findViewById(R.id.toggle);
        if(Constants.sharedConstants().scheduleType == ScheduleType.NO_MY_SCHEDULE) {
        	group.setVisibility(View.GONE);
        } else {
	        group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(RadioGroup group, int checkedId) {
	        		filteredSchedule = (checkedId == R.id.rightButton) ? true : false;
	        		System.out.println("filteredSchedule is: "+(filteredSchedule?"true":"false"));
	        		if(filteredSchedule) {
	        			System.out.println("Mode: My Schedule");
	        			if(filteredEvents.size()==0) {
	        				setListAdapter(new MenuAdapter(getApplicationContext(), noEvents));
	        			} else {
	        				setListAdapter(new MenuAdapter(getApplicationContext(), filteredEvents));
	        			}        			
	        		}
	        		else {
	        			System.out.println("Mode: All Activities");
	        			setListAdapter(new MenuAdapter(getApplicationContext(), events));
	        		}
				}
	        });
        }
        ListView lv = getListView();
        lv.setDividerHeight(0);
        lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener()  {
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            	if(Constants.sharedConstants().scheduleType == ScheduleType.DEFAULT) {
	            	HashMap row = null;
	            	if(filteredSchedule) {
	            		row = (HashMap)filteredEvents.get(((AdapterContextMenuInfo)menuInfo).position);
	            	} else {
	            		row = (HashMap)events.get(((AdapterContextMenuInfo)menuInfo).position);
	            	}
	            	boolean mySchedule = DatabaseHelper.getMySchedule(((Integer) row.get("EventId")).intValue());
	            	menu.add((mySchedule) ? "Remove from My Schedule" : "Add to My Schedule");
            	}
            }
        });
        
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              	Intent intent = new Intent();
              	System.out.println("cell "+ position +" clicked");
              	HashMap row = null;
              	if(filteredSchedule) {
              		if(filteredEvents.size() > 0) {
              			row = (HashMap)filteredEvents.get(position);
              		}
            	} else {
            		row = (HashMap)events.get(position);
            	}
              	intent = NavigationHelper.getIntentForItem(getApplicationContext(), row);
              	if(intent != null) {
              		showLoadingDialog(intent.getStringExtra("Name"));
              		startActivity(intent);
              	}
            }
        });
    }

    @Override
	public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

         // Switch on the ID of the item, to get what the user selected.
         switch (item.getItemId()) {
              case 0:
            	   toggleMySchedule(menuInfo.position);
                   return true; // true means: "we handled the event"
              default:
            	   return super.onContextItemSelected(item);
         }
    }
    
	private void toggleMySchedule(int position) {
 	   HashMap<String, Object> row = null;
	   if(filteredSchedule) {
		   row = (HashMap<String, Object>) filteredEvents.get(position);
	   } else {
		   row = (HashMap<String, Object>) events.get(position);
	   }
	   boolean mySchedule = DatabaseHelper.getMySchedule(((Integer) row.get("EventId")).intValue());
       int eventId = ((Integer) row.get("EventId")).intValue();
       DatabaseHelper.updateMySchedule(eventId, !mySchedule);
       if(mySchedule) {  //Current state is selected, must remove.
    	   for(int i=0;i<filteredEvents.size();i++) {
    		   if(((Integer)(((HashMap<String, Object>) filteredEvents.get(i)).get("EventId"))).intValue() == eventId) {
    			   filteredEvents.remove(i);   
    		   }
    		   if(filteredEvents.size() == 0 && filteredSchedule)
    			   setListAdapter(new MenuAdapter(getApplicationContext(), noEvents));
    	   }
       } else { //current state is not selected, must add
    	   int i=-1;
    	   long newEventEpoch = ((Long)row.get("Epoch")).longValue();
    	   long iterEventEpoch = 0;
    	   while(iterEventEpoch < newEventEpoch && ++i < filteredEvents.size()) {
    		   iterEventEpoch = ((Long)((HashMap<String, Object>)filteredEvents.get(i)).get("Epoch")).longValue();
    	   }
    	   System.out.println("i: "+i);
    	   filteredEvents.add(i, row);
    	   System.out.println("Done adding event");
       }
       ((MenuAdapter) getListView().getAdapter()).notifyDataSetChanged();
	}
	
    @SuppressWarnings("unchecked")
  	private class MenuAdapter extends BaseAdapter {
      	private Context ctx;
      	private LayoutInflater mInflater;
          private ArrayList menuItems;
          
  		public MenuAdapter(Context context, ArrayList items) {
  			mInflater = LayoutInflater.from(context);
  			menuItems = items;
  			ctx = context;
  		}
      	
      	public int getCount() {
  			return menuItems.size();
  		}

  		public Object getItem(int position) {
  			return position;
  		}

  		public long getItemId(int position) {
  			return position;
  		}

  		public View getView(final int position, View convertView, ViewGroup parent) {
  			final HashMap row = (HashMap) menuItems.get(position);
  			int eventId = ((Integer) row.get("EventId")).intValue();
  			boolean mySchedule = DatabaseHelper.getMySchedule(eventId);
			final EventViewHolder eventHolder;
			if(convertView == null) {
				convertView = mInflater.inflate(R.layout.cell_schedule, null);
				eventHolder = new EventViewHolder();
				eventHolder.icon = (ImageView) convertView.findViewById(R.id.image);
				eventHolder.nextarrow = (ImageView) convertView.findViewById(R.id.nextarrow);
				eventHolder.name = (TextView) convertView.findViewById(R.id.name);
				eventHolder.date = (TextView) convertView.findViewById(R.id.date);
				eventHolder.time = (TextView) convertView.findViewById(R.id.time);
				
				convertView.setTag(eventHolder);
			} else {
				eventHolder = (EventViewHolder) convertView.getTag();
			}
			eventHolder.name.setText((CharSequence) row.get("Name")); 
			if(eventId >= 0) {
				eventHolder.icon.setImageDrawable(ImageFinder.getDrawable(ctx, (String)row.get("ImageFileName"), (String)row.get("ImageUrl")));
				if(filteredSchedule || Constants.sharedConstants().scheduleType == ScheduleType.NO_MY_SCHEDULE) {
					eventHolder.nextarrow.setImageResource(R.drawable.com_nextarrow);
				} else {
					if(mySchedule) {
						eventHolder.nextarrow.setImageResource(R.drawable.com_check_on);
					} else {
						eventHolder.nextarrow.setImageResource(R.drawable.com_check_off);
					}
					eventHolder.nextarrow.setOnClickListener(new OnClickListener() {
					    public void onClick(View v) {
					    	toggleMySchedule(position);			              						
						}
					});
				}
				eventHolder.date.setText((CharSequence) row.get("Date"));
				eventHolder.time.setText((CharSequence) row.get("Time"));
			} else {
				eventHolder.icon.setImageDrawable(null);
				eventHolder.nextarrow.setImageDrawable(null);
				eventHolder.date.setText(null);
				eventHolder.time.setText(null);
			}
			
			convertView.setBackgroundDrawable(Constants.sharedConstants().cellGradient);
  			return convertView;
  		}
  		
  		class EventViewHolder {
  			ImageView icon;
  			ImageView nextarrow;
  			TextView name;
  			TextView date;
  			TextView time;
  		}
    }
	private void showLoadingDialog(String text) { 
		pd = ProgressDialog.show(this, "Loading...", text, true, false);
	}
}
