package com.thinkmaketest.utsg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import android.app.Activity;
import android.app.ProgressDialog;
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

public class AddNewCourse_Backup extends Activity implements Runnable{
	private Button ok, cancel;
	private Spinner department_list, course_list;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	private ProgressDialog progress_dialog;
	private Thread fetch_data;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        print("AddNewCourse activity started!");
        setContentView(R.layout.addnewcourse);
        
        // Fetch UI objects
        this.ok = (Button) this.findViewById(R.id.addnewcourse_ok);
        this.cancel = (Button) this.findViewById(R.id.addnewcourse_cancel);
        this.department_list = (Spinner) this.findViewById(R.id.addnewcourse_departmentlist);
        this.course_list = (Spinner) this.findViewById(R.id.addnewcourse_courselist);
        this.prefs = getSharedPreferences("UT Schedule Generator", 0);
        this.editor =  prefs.edit();
        this.fetch_data = new Thread(this);
        
        print("Initializing objects");
        // Setup each object
        if (prefs.getString("department_list", "") != ""){
        	// Initialize list since it's already been fetched
        	print("Loading department list from memory");
        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddNewCourse_Backup.this,
    				android.R.layout.simple_spinner_item,
        				getPreferences(0)
        				.getString("department_list", "")
        				.split(":"));
        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	department_list.setAdapter(adapter);
        	
            //Set to last selected item
            this.department_list.setSelection(prefs.getInt("departments_list_choice", 0), false);
        }
        this.department_list.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// Save last selected preference
				editor.putInt("departments_list_choice", pos);

		        if (isFetchNeeded()){
		            print("Fetching data\n" + prefs.getString("department_list", "Empty") + "\n" + prefs.getString("course_list_cache", "Empty"));
		            fetch_data.start();
		        	progress_dialog = ProgressDialog.show(AddNewCourse_Backup.this, "", "...");
		        }else{
					// Load list from memory
					String tmp = prefs.getString("course_list_cache_cache", "");
					tmp = tmp.substring(tmp.indexOf(department_list.getSelectedItem().toString()) + department_list.getSelectedItem().toString().length());
					tmp = tmp.substring(0, tmp.indexOf(":"));
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddNewCourse_Backup.this, android.R.layout.simple_spinner_item, tmp.split("|"));
	        		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        		course_list.setAdapter(adapter);
					
					// Save course_list position
					editor.putInt("course_list_choice", 0);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
        });
        
        this.course_list.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				editor.putInt("courses_list_choice", pos);
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
        });
        
        if (prefs.getString("course_list_cache", "") != ""){
        	// Initialize list since it's already been fetched
        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddNewCourse_Backup.this,
    				android.R.layout.simple_spinner_item,
        				getPreferences(0)
        				.getString("course_list_cache", "")
        				.split(":"));
        	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        	course_list.setAdapter(adapter);
            
            //Set to last selected item
            this.course_list.setSelection(prefs.getInt("departments_list_choice", 0), false);
        }
        
        this.ok.setEnabled(false);
        this.ok.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("course_list", prefs.getString("course_list", "") + department_list.getSelectedItem().toString() + "|" + course_list.getSelectedItem().toString() + ":");
				editor.commit();
				finish();
			}
        });
        this.cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) { finish(); }
        });
        
        // Fetch necessary data
        if (isFetchNeeded()){
            print("Fetching data\n" + prefs.getString("department_list", "Empty") + "\n" + prefs.getString("course_list_cache", "Empty"));
            fetch_data.start();
        	this.progress_dialog = ProgressDialog.show(this, "", "...");
        }
    }
	
	private boolean isFetchNeeded() { return prefs.getString("department_list", "") == "" ? true : prefs.getString("course_list_cache", "").contains(department_list.getSelectedItem().toString()); }
	
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
            if (prefs.getString("department_list", "") == ""){
            	// Request list of departments
        		msg.arg1 = 0;
    			progress_dialog.setMessage("Fetching departments...");
        		
    			print("Retrieving departments");
    			
    	        BufferedReader in = new BufferedReader(
    	                                new InputStreamReader(
    	                                		new URL("http://ras.ece.utexas.edu/test.txt").
    	                                		openConnection().
    	                                		getInputStream()));
    	        
    	        String line = "",
    	        list = "";
    	        while ((line = in.readLine()) != null)
    	            list += line + ":";
    	        in.close();
    	        
    	        // Save list to Message
    	        msg.obj = list;
    	        print("Sending data to handler");
            }else{
            	// Request list of courses in selected department
        		msg.arg1 = 1;
    			progress_dialog.setMessage("Fetching courses...");
    			
    			print("Retrieving courses");
    			
    	        BufferedReader in = new BufferedReader(
    	                                new InputStreamReader(
    	                                		new URL("http://ras.ece.utexas.edu/" + department_list.getSelectedItem().toString() + ".txt").
    	                                		openConnection().
    	                                		getInputStream()));
    	        
    	        String line = "",
    	        list = "";
    	        while ((line = in.readLine()) != null)
    	            list += line + "|";
    	        in.close();
    	        
    	        // Save list to Message
    	        msg.obj = list;
    	        print("Sending data to handler");
            }
        } catch (Exception e) {
        	msg.arg1 = -1;
        }
        
        // Send the message that data has been retrieved
        fetch_data_handler.sendMessage(msg);
	}
	private Handler fetch_data_handler = new Handler() {
		@Override
        public void handleMessage(Message msg) {
    		ArrayAdapter<String> adapter;
    		
        	switch(msg.arg1){
        	case 0:// Save new data	
        		print("Saving directories to preferences");
				editor.putString("departments_list", (String) msg.obj);
				editor.putInt("departments_list_choice", 0);
				editor.commit();
				
				print("Initializing departments list");
				// Initialize department_list with new data
        		adapter = new ArrayAdapter<String>(AddNewCourse_Backup.this, android.R.layout.simple_spinner_item, ((String) msg.obj).split(":"));
        		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		department_list.setAdapter(adapter);
        		
        		break;
        	case 1:
        		// Save new data
        		print("Saving courses to preferences");
				editor.putString("course_list_cache", prefs.getString("course_list_cache", "") + department_list.getSelectedItem().toString() + "|" + (String) msg.obj + ":");
				editor.putInt("course_list_choice", 0);
				editor.commit();
        		
				print("Initializing course list");
				// Initialize course_list with new data
        		adapter = new ArrayAdapter<String>(AddNewCourse_Backup.this, android.R.layout.simple_spinner_item, ((String) msg.obj).substring(((String) msg.obj).indexOf("|" + 1)).split("|"));
        		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        		course_list.setAdapter(adapter);
        		break;
        	case -1:
        		print("Recovering from error");
        		break;
        	}
        	
    		print("Removing dialog");
        	
        	//Remove progress dialog
    		AddNewCourse_Backup.this.progress_dialog.dismiss();
    		
            print("Complete!\n");
        }
	};

	private static void print(String str) { Log.v("AddNewCourse", str); }
}