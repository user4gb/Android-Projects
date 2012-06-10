package com.thinkmaketest.utsg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class AddNewCourse extends Activity implements Runnable{
	private Button ok, cancel;
	private Spinner department_list, course_list;
	private SharedPreferences prefs;
	@SuppressWarnings("unused")
	private SharedPreferences.Editor editor;
	private ProgressDialog progress_dialog;
	@SuppressWarnings("unused")
	private Thread fetch_data;
	private ArrayList<String> department_data;
	private HashMap<String, ArrayList<String>> course_data;

	@SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        print("AddNewCourse activity started!");
        setContentView(R.layout.addnewcourse);
        
        // Fetch UI objects
        print("Loading ui objects");
        this.ok = (Button) this.findViewById(R.id.addnewcourse_ok);
        this.cancel = (Button) this.findViewById(R.id.addnewcourse_cancel);
        this.department_list = (Spinner) this.findViewById(R.id.addnewcourse_departmentlist);
        this.course_list = (Spinner) this.findViewById(R.id.addnewcourse_courselist);
        this.department_data = null;
        this.course_data = new HashMap();
        
        this.department_list.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// Load course list specific to item selected
				
				ArrayList<String> list = course_data.get(department_list.getSelectedItem()); 
				if (list != null){
					// Load course from memory
					print("Loading course list for " + department_list.getSelectedItem());
		    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddNewCourse.this, android.R.layout.simple_spinner_item, list);
		    		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    		course_list.setAdapter(adapter);
				}else{
					// Fetch and save requested course list
					print("Download Started!");
					progress_dialog = ProgressDialog.show(AddNewCourse.this, "", "Fetching courses for " + department_list.getSelectedItem());
			        new Thread(AddNewCourse.this).start();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
        });
        
        this.course_list.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		        ok.setEnabled(true);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
        });
        
        this.ok.setEnabled(false);
        this.ok.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// Save Cached Data
				// To Preferences (?)
				
				Intent i = new Intent();
				
				// Send selected data
				setResult(RESULT_OK, new Intent());

				finish();
			}
        });
        this.cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) { finish(); }
        });
        
        //Restoring state
		if (savedInstanceState != null){
			print("Restoring department list");
			department_data = savedInstanceState.getStringArrayList("department_data");
			
			print("Restoring set of courses");
			course_data = (HashMap<String, ArrayList<String>>) savedInstanceState.getSerializable("course_data");
			
			print("Loading department list");
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddNewCourse.this, android.R.layout.simple_spinner_item, department_data);
    		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    		course_list.setAdapter(adapter);
		}else{
			print("Begining download process");
			progress_dialog = ProgressDialog.show(this, "", "Fetching departments...");
	        new Thread(AddNewCourse.this).start();
		}
	}

	protected void onDestroy(){
		super.onDestroy();
		print("Destruction imminent!");
	}

	protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        
        print("Saving state!");
        
        // Save department list
        outState.putStringArrayList("department_list_data", department_data);
        
        // Save course data
        outState.putSerializable("course_list", course_data);
	}
	
	@Override
	public void run() {
		// Create message to send off
		Message msg = new Message();
		
		try {
			/** TODO:
			 * - Create UT cookie(s)
			 * - Get URL(s)
			 * - Decode HTML
			 */
            if (department_data == null){
            	// Request list of departments
    			print(" Retrieving departments");
        		msg.arg1 = 0;
    			
    	        BufferedReader in = new BufferedReader(
    	                                new InputStreamReader(
    	                                		new URL("http://ras.ece.utexas.edu/test.txt").
    	                                		openConnection().
    	                                		getInputStream()));
    	        
    	        String line = "";
    	        ArrayList<String> list = new ArrayList<String>();
    	        while ((line = in.readLine()) != null)
    	            list.add(line);
    	        in.close();
    	        
    	        // Save list to Message
    	        msg.obj = list;
    	        print(" Sending data to handler");
            }else{
            	// Request list of courses in selected department
    			print(" Retrieving courses for " + department_list.getSelectedItem().toString());
        		msg.arg1 = 1;
    			
    	        BufferedReader in = new BufferedReader(
    	                                new InputStreamReader(
    	                                		new URL("http://ras.ece.utexas.edu/" + department_list.getSelectedItem().toString() + ".txt").
    	                                		openConnection().
    	                                		getInputStream()));
    	        
    	        String line = "";
    	        ArrayList<String> list = new ArrayList<String>();
    	        while ((line = in.readLine()) != null)
    	            list.add(line);
    	        in.close();
    	        
    	        // Save list to Message
    	        msg.obj = list;
    	        print(" Sending data to handler");
            }
        } catch (Exception e) {
        	msg.arg1 = -1;
        }
        
        // Send the message that data has been retrieved
        fetch_data_handler.sendMessage(msg);
	}
	private Handler fetch_data_handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
        public void handleMessage(Message msg) {
    		ArrayAdapter<String> adapter;
    		
        	switch(msg.arg1){
        	case 0:// Save new data	
        		print(" Cahcing directories");
        		department_data = (ArrayList<String>) msg.obj;
				
				print(" Initializing departments list");
				// Initialize department_list with new data
        		adapter = new ArrayAdapter<String>(AddNewCourse.this, android.R.layout.simple_spinner_item, department_data);
        		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		department_list.setAdapter(adapter);
        		
        		break;
        	case 1:
        		// Save new data
        		print(" Caching courses");
        		course_data.put(department_list.getSelectedItem().toString(), (ArrayList<String>) msg.obj);
        		
				// Initialize course_list with new data
				print(" Initializing course list");
        		adapter = new ArrayAdapter<String>(AddNewCourse.this, android.R.layout.simple_spinner_item, (ArrayList<String>) msg.obj);
        		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		course_list.setAdapter(adapter);
        		break;
        	case -1:
        		print(" Recovering from error");
        		break;
        	}
        	
    		print(" Removing dialog");
        	
        	//Remove progress dialog
    		AddNewCourse.this.progress_dialog.dismiss();
    		
            print("Download Complete!");
        }
	};
	
	private static void print(String str) { Log.v("AddNewCourse", str); }
}