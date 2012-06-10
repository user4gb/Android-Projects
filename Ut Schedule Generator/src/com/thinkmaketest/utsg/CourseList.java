package com.thinkmaketest.utsg;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class CourseList extends Activity {
	private static final int ADD_NEW_COURSE = 0;
	@SuppressWarnings("unused")
	private LinearLayout root;
	private Button add, clear;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        print("CourseList activity started!");
        setContentView(R.layout.main);
		
        // Set "add class" action
        this.add = (Button) this.findViewById(R.id.courselist_addnewcourse);
        this.add.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent i = new Intent(CourseList.this, AddNewCourse.class);
				print("Starting activity: AddCourseDialog");
				CourseList.this.startActivityForResult(i, ADD_NEW_COURSE);
			}
		});
        
        this.clear = (Button) this.findViewById(R.id.courselist_clearprefs);
        this.clear.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				print("Clearing Preferences!");
				
				SharedPreferences.Editor editor = getSharedPreferences("UT Schedule Generator", 0).edit();
				editor.clear();
				editor.commit();
			}
		});
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if (requestCode == ADD_NEW_COURSE){
			if (resultCode != RESULT_CANCELED){
				// Add course selected to list
			}
		}
	}
    
    private static void print(String str) { Log.v("CourseList", str); }
}