package com.thinkmaketest.sensorstest;

import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SensorsTest extends Activity{
	@SuppressWarnings("unused")
	private String TAG = "Sensors Test";
	private DOFContainer dof;
	private TextView accel_x,
					 accel_y,
					 accel_z,
					 orien_x,
					 orien_y,
					 orien_z,
					 //true_accel_x,
					 //true_accel_y,
					 //true_accel_z,
					 vel_x,
					 vel_y,
					 vel_z;
	private Button control;
	private boolean listenToChanges;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
         
        // Set areas to write to
        this.accel_x = (TextView) findViewById(R.id.accel_x);
        this.accel_y = (TextView) findViewById(R.id.accel_y);
        this.accel_z = (TextView) findViewById(R.id.accel_z);
        this.orien_x = (TextView) findViewById(R.id.orien_x);
        this.orien_y = (TextView) findViewById(R.id.orien_y);
        this.orien_z = (TextView) findViewById(R.id.orien_z);
        //this.true_accel_x = (TextView) findViewById(R.id.true_accel_x);
        //this.true_accel_y = (TextView) findViewById(R.id.true_accel_y);
        //this.true_accel_z = (TextView) findViewById(R.id.true_accel_z);
        this.vel_x = (TextView) findViewById(R.id.vel_x);
        this.vel_y = (TextView) findViewById(R.id.vel_y);
        this.vel_z = (TextView) findViewById(R.id.vel_z);
        
        // Setup button
        this.control = (Button) findViewById(R.id.control);
        this.control.setOnClickListener(new OnClickListener(){
        	public void onClick(View v) {
        		if (control.getText().toString().equals("Start")){
        			control.setText("Stop");
        			listenToChanges = true;
        		}else{
        			control.setText("Start");
        			listenToChanges = false;
        		}
			}
        	
        });
        
        // Setup sensors
        this.dof = new DOFContainer((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        this.dof.addObserver(new Observer(){
			public void update(Observable observable, Object data) {
				if (listenToChanges){
					double a[] = dof.getValues("Acceleration"),
					       o[] = dof.getValues("Orientation"),
					       v[] = dof.getValues("Velocity")/*,
					       t[] = dof.getValues("True Acceleration")*/;
				
					if (a != null){
						accel_x.setText(Double.toString(a[0]));
						accel_y.setText(Double.toString(a[1]));
						accel_z.setText(Double.toString(a[2]));
					}
					if (o != null){
						orien_x.setText(Double.toString(o[0]));
						orien_y.setText(Double.toString(o[1]));
						orien_z.setText(Double.toString(o[2]));
					}
					/*if (t != null){
						true_accel_x.setText(Double.toString(t[0]));
						true_accel_y.setText(Double.toString(t[1]));
						true_accel_z.setText(Double.toString(t[2]));
					}*/
					if (v != null){
						vel_x.setText(Double.toString(Math.round(v[0] * 100)/100.0) + " m/s");
						vel_y.setText(Double.toString(Math.round(v[1] * 100)/100.0) + " m/s");
						vel_z.setText(Double.toString(Math.round(v[2] * 100)/100.0) + " m/s");
					}
				}
			}
        });
        this.listenToChanges = false;
    }
}