package com.thinkmaketest.sensorstest;

import java.util.HashMap;
import java.util.Observable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DOFContainer extends Observable{
	private SensorManager manager;
	private HashMap<String, double[]> last_values;

	public DOFContainer(SensorManager _manager){
		this.last_values = new HashMap<String, double[]>();
		
		// Setup sensors
	    this.manager = _manager;
	    this.manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
	    this.manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
	    
	    // Put dummy velocity
		last_values.put("Velocity", new double[]{0,0,0,0});
    }
	public synchronized double[] getValues(String s){ return last_values.get(s); }

	SensorEventListener listener = new SensorEventListener(){
		public void onAccuracyChanged(Sensor arg0, int arg1) {}
		public void onSensorChanged(SensorEvent event) {
			switch(event.sensor.getType()){
				case Sensor.TYPE_ACCELEROMETER:
					event.values[1] *= -1;	// Fix y direction
					/*
					double t[] = new double[3];
					double o[] = last_values.get("Orientation");
					if (o != null){
						t[0] = event.values[0]*(c(o[0])*c(o[1])*c(o[2])-s(o[0])*s(o[2])) + event.values[1]*(-c(o[0])*c(o[1])*s(o[2])-s(o[0])*c(o[2])) + event.values[2]*(c(o[0])*s(o[1]));
						t[1] = event.values[0]*(s(o[0])*c(o[1])*c(o[2])+c(o[0])*s(o[2])) + event.values[1]*(-s(o[0])*c(o[1])*s(o[2])+c(o[0])*c(o[2])) + event.values[2]*(s(o[0])*s(o[1]));
						t[2] = event.values[0]*(-s(o[1])*c(o[2]))                        + event.values[1]*(s(o[1])*s(o[2]))                          + event.values[2]*(c(o[1]));
						
						last_values.put("True Acceleration", appendTimestamp(t, event.timestamp));
					}*/
					double a[] = last_values.get("Acceleration");
					if (a != null){
						double t = (event.timestamp - Double.doubleToLongBits(a[3])) / Math.pow(10, 9);
						double v[] = last_values.get("Velocity");
						
						
						v[0] += (event.values[0] - a[0]) * t;
						v[1] += (event.values[1] - a[1]) * t;
						v[2] += (event.values[2] - a[2]) * t;
						v[3] = event.timestamp;
						
						last_values.put("Velocity", v);
					}

					last_values.put("Acceleration", appendTimestamp(event.values, event.timestamp));
					break;
				case Sensor.TYPE_ORIENTATION:
					event.values[1] *= -1; // Fix Pitch direction
					event.values[2] *= -1; // Fix Roll direction
					last_values.put("Orientation", appendTimestamp(event.values, event.timestamp));
					break;
			}
			
			// Notify of Change
			setChanged();
			notifyObservers();
		}
		//private double c(double d){ return Math.cos(Math.toRadians(d)); }
		//private double s(double d){ return Math.sin(Math.toRadians(d)); }
		private double[] appendTimestamp(float[] t, long timestamp){
			double tmp[] = new double[t.length + 1];
			
			for(int i=0; i< t.length; i++)
				tmp[i] = t[i];
			tmp[t.length] = Double.longBitsToDouble(timestamp);
			return tmp;
		}
	};
	
	public synchronized void setChanged() {
        super.setChanged();
    }
}
