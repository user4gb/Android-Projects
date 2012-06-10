package com.thinkmaketest.AudioTrackTest;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AudioTrackTest extends Activity{
	private static String TAG = "AudioTrack Test";
	private SafeThread process;
	private TextView text;
	private Button inc, dec, control;
	private AudioTrack track;
	short tmp = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		text = (TextView) this.findViewById(R.id.display);
		text.setText("NULL");
		
		inc = (Button) this.findViewById(R.id.inc);
		inc.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				log("Increase Button disabled");
			}
			
		});
		
		dec = (Button) this.findViewById(R.id.dec);
		dec.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				log("Decrease Button disabled");
			}
			
		});

		control = (Button) this.findViewById(R.id.control);
		control.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
			    if (control.getText().toString().equals("Start")){
			    	log("Starting thread");
				    process.unpause();
			    	control.setText("Stop");
			    }else{
			    	log("Pausing thread");
				    process.pause();
			    	control.setText("Start");
			    	track.flush();
			    }

				//print("Sending: " + message);
			}
			
		});
		
		// Setup stream
    	int min_size = AudioTrack.getMinBufferSize( 9600, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT );        
        track = new AudioTrack(AudioManager.STREAM_MUSIC, 9600, 
        					   AudioFormat.CHANNEL_OUT_MONO,
                               AudioFormat.ENCODING_PCM_8BIT,
                               min_size,
                               AudioTrack.MODE_STREAM);
        track.setStereoVolume(1, 1);
        track.play();
        
        //print(Byte.toString(Byte.MAX_VALUE));
                
        process = new SafeThread(){
        	public void doStuff(){
        		log("Writing data");
        	    track.write(generateData(), 0, 1024);
        	}
        	private short[] generateData(){
        		short buffer[] = new short[1024];
        		short MAX = (short) (Short.MAX_VALUE);
        		
        		char message = 0xFFFF;
        		boolean tmp = true;
        		int pulses = 64;
        		for(int k=0; k<pulses; k++){
	        		for(int i=0; i<1024/pulses; i++){
	        			int pos = i + 1024/pulses*k;
	        			if (!tmp)
	        				buffer[pos] = MAX;
	        			else{
	        				//buffer[i + 1024/pulses*k] = 0;
	        				if (i == 0)
	        					buffer[pos] = 0;
	        				else
	        					buffer[pos] = (message & (1 << (i-1))) == 0 ? 0: MAX;
	        			}
	        		}
	        		tmp = !tmp;
        		}
        		
        		return buffer;
        	}
        };
        process.start();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	this.process.pause();
    	print("Thread paused");
    }
    @Override
    public void onResume(){
    	super.onResume();
    	//if (process.isPaused())
    	//	this.process.unpause();
    	//print("Thread resumed");
    }
    @Override
    public void onStop(){
    	super.onStop();
    	this.process.kill();
    	log("Bye bye...");
    }
    public void print(String s){
    	Log.v(TAG, s);
    	text.setText(s);
    }
    public void log(String s){
    	Log.v(TAG, s);
    }
    
    /*
    public void writeSamples(){
    	
       track.write(fillBuffer(value), 0, sizeOf() );
    }
  
    private short[] fillBuffer(float value){
    	short[] buffer = new short[1024];

    	for( int i = 0; i < 1024; i++ )
    		buffer[i] = (short)(value * Short.MAX_VALUE);

    	return buffer;
    }	*/
}

