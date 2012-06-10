package com.thinkmaketest.utsg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class CourseListOption extends LinearLayout {

	public CourseListOption(Context context, String course, String title) {
		super(context);

		LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.courselistoption, this);
	}
}
