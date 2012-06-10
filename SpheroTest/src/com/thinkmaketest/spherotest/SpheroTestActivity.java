package com.thinkmaketest.spherotest;

import orbotix.robot.app.StartupActivity;
import orbotix.robot.base.FrontLEDOutputCommand;
import orbotix.robot.base.RGBLEDOutputCommand;
import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;
import orbotix.robot.base.RotationRateCommand;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.ccpcreations.android.WiiUseAndroid.IWiiControllerPublicService;
import com.ccpcreations.android.WiiUseAndroid.IWiiControllerPublicServiceCallback;
import com.thinkmaketest.wiimotespherocontrol.WiimoteController;

public class SpheroTestActivity extends Activity {
	private final static int WIIMOTE_KEY_A = KeyEvent.KEYCODE_DPAD_CENTER;
	private final static int WIIMOTE_KEY_B = KeyEvent.KEYCODE_BACK;
	private final static int WIIMOTE_KEY_1 = KeyEvent.KEYCODE_1;
	private final static int WIIMOTE_KEY_2 = KeyEvent.KEYCODE_2;
	
	private final static int WIIMOTE_KEY_PLUS = KeyEvent.KEYCODE_P;
	private final static int WIIMOTE_KEY_MINUS = KeyEvent.KEYCODE_M;
	private final static int WIIMOTE_KEY_HOME = KeyEvent.KEYCODE_H;
	
	private final static int WIIMOTE_KEY_UP = KeyEvent.KEYCODE_DPAD_UP;
	private final static int WIIMOTE_KEY_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	private final static int WIIMOTE_KEY_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	private final static int WIIMOTE_KEY_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;

	private final static int WIIMOTE_NUNCHUCK_C = KeyEvent.KEYCODE_C;
	private final static int WIIMOTE_NUNCHUCK_Z = KeyEvent.KEYCODE_Z;

	private TextView status_str;
	public static TextView heading_str;
	public static int CONTROL = 0;

	// Tags for Activity Results
	private final static int REQUEST_ENABLE_BT = 0;
	private final static int STARTUP_ACTIVITY = 1;
	private static Robot mRobot;
	
	// Variables needed by WiimoteConnect service
	private IWiiControllerPublicService wcService=null;
	private static final int DIALOG_NOWIIMOTES=1000;
	private static final int DIALOG_NOOROLDWIIMOTECONTROLLERAPP=1001;
	private static final String APP_ID="WiimoteController Usage Demo";
	private Handler handler=new Handler();
	
	
	protected void onPause(){
		RollCommand.sendStop(mRobot);
		
		super.onPause();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);

/* Step 2 */
		if (requestCode == REQUEST_ENABLE_BT){
			if (resultCode == RESULT_OK){
				//TODO: Dialog w/ instructions
				
				// Connect to Wiimote using Wiimote Connect service
				
				//Check if the WiimoteController app is installed before continuing
				if(WiimoteController.isWiimoteControllerInstalled(SpheroTestActivity.this)) {
					//Intent receiver exists, go ahead and connect
					boolean success = WiimoteController.connectToWCService(SpheroTestActivity.this, wcServiceConnection);
					if(!success) {
						//Oops...
						Toast.makeText(SpheroTestActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
					}
				} else {
					//Intent receiver doesn't exist, offer the user to install WiimoteController
					showDialog(DIALOG_NOOROLDWIIMOTECONTROLLERAPP);
				}
		        
		        // Connect to Sphero
		        /**Intent i = new Intent(this, StartupActivity.class);  
		        startActivityForResult(i, STARTUP_ACTIVITY);*/
			}else{
				Log.v("Sphero Wiimote Control", "Could not start Bluetooth Adapter. Result code: " + Integer.toString(resultCode));
			}
/* Step 3 */
		}//else if (){
			
		//}
		
		
		if(requestCode == STARTUP_ACTIVITY && resultCode == RESULT_OK){ //Get the connected Robot
			final String robot_id = data.getStringExtra(StartupActivity.EXTRA_ROBOT_ID);
			
			if(robot_id != null && !robot_id.equals("")){
		        new Thread(
		    		new Runnable(){
		    			@Override
		    			public void run(){
		    				float heading = 0.0f;
		    				float velocity = 0.0f;
		    				float inc = 5f;
		    				float max_vel = 0.75f;
		    				
		    				mRobot = RobotProvider.getDefaultProvider().findRobot(robot_id);	// Force the robot to stop
		    	        	RollCommand.sendStop(mRobot);
		    	            System.out.println("Stop command sent.");
		    	            
							RotationRateCommand.sendCommand(mRobot, 1.0f);
					        
					        FrontLEDOutputCommand.sendCommand(mRobot, 1.0f);
							
		    				while(true){
		    					heading = RollCommand.getCurrentHeading();
		    					//heading_str.setText(Double.toString(heading));
		    					
		    					switch(CONTROL){
									case WIIMOTE_KEY_UP:
										velocity = max_vel;
										
										RollCommand.sendCommand(mRobot, 0, velocity);
										
										break;
									case WIIMOTE_KEY_RIGHT:
										velocity = max_vel;
										
										// Handle 360 -> 0 case
										//if (heading + inc >= 360){
											//RollCommand.sendCommand(mRobot, 0.0f, velocity);
										//}
										
				    					// Send command
										RollCommand.sendCommand(mRobot, 90, velocity);
										
										break;
									case WIIMOTE_KEY_LEFT:
										velocity = max_vel;
										
										// Handle 0 -> 360 case
										//if (heading - inc <= 0){
											//RollCommand.sendCommand(mRobot, 0.0f, velocity);
					    				//}
										
										// Send command
										RollCommand.sendCommand(mRobot, 270, velocity);
										
										break;
									case WIIMOTE_KEY_DOWN:
										velocity = max_vel;
										
										RollCommand.sendCommand(mRobot, 180, max_vel);
										
										try {
											Thread.sleep(250);
										} catch (InterruptedException e1) {
											e1.printStackTrace();
										}
										
										break;
									default:
										velocity = 0.0f;
										RollCommand.sendCommand(mRobot, heading, velocity);
		    					}
		    					
		    					try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
		    				}
		    			}
		    		}).start();
			}
			
	    	RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 0);
			
			//Start blinking
			//blink(true);
			//drive();
		}
	}
   	
   
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
/* Step 1 */
        // Enable Bluetooth
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
        if (!mBluetoothAdapter.isEnabled()) {
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //status_str = (TextView)findViewById(R.id.status);
        //heading_str = (TextView)findViewById(R.id.heading);
    }
    
    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
    	
        if(keyCode == WIIMOTE_KEY_UP)
        {
        	status_str.setText("Rolling Forward.");
        	RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 0);

            // Set CONTROL for Control Thread
        	CONTROL += keyCode;
        	
        	return true;
        } else if(keyCode == WIIMOTE_KEY_RIGHT) {
        	status_str.setText("Rolling Right.");
        	RGBLEDOutputCommand.sendCommand(mRobot, 0, 255, 255);

            // Set CONTROL for Control Thread
        	CONTROL += keyCode;
        	
        	return true;
        } else if(keyCode == WIIMOTE_KEY_LEFT) {
        	status_str.setText("Rolling Left.");
        	RGBLEDOutputCommand.sendCommand(mRobot, 255, 0, 255);

            // Set CONTROL for Control Thread
        	CONTROL += keyCode;
        	
        	return true;
        }else if(keyCode == WIIMOTE_KEY_DOWN) {
        	status_str.setText("Backing Up.");
        	RGBLEDOutputCommand.sendCommand(mRobot, 255, 255, 255);

            // Set CONTROL for Control Thread
        	CONTROL += keyCode;
        	
        	return true;
        }else if(keyCode == WIIMOTE_KEY_B){	// Disable accidentally exiting...
        	return false;
        }else
        	status_str.setText("Button pressed.");
    	
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
    	status_str.setText("Listening...");
    	RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 0);
    	if (keyCode == WIIMOTE_KEY_UP || keyCode == WIIMOTE_KEY_RIGHT || keyCode == WIIMOTE_KEY_LEFT || keyCode == WIIMOTE_KEY_DOWN)
    		CONTROL -= keyCode;
    	
    	return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
    	disconnectFromService();
    	super.onDestroy();
    }*/
    
    /** This function contains all dialogs */
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch(id) {
    		//Dialog to show when there are no wiimotes.
	    	case SpheroTestActivity.DIALOG_NOWIIMOTES:
	    		return new AlertDialog.Builder(SpheroTestActivity.this)
    			.setTitle("No wiimotes")
    			.setMessage("No wiimotes connected. Do you want to connect some?")
    			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Start the main activity of WiimoteController
						try {
							wcService.displayConnectActivity();
						} catch(RemoteException re) {
							re.printStackTrace();
							Toast.makeText(SpheroTestActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();			
						}
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						disconnectFromService();
					}
				})
				.setCancelable(false)
				.create();
	    	//Dialog to show when the WiimoteController app is missing or not updated
	    	case SpheroTestActivity.DIALOG_NOOROLDWIIMOTECONTROLLERAPP:
	    		return new AlertDialog.Builder(SpheroTestActivity.this)
	    		.setTitle("WiimoteController app missing or old")
	    		.setMessage("You have to get or update WiimoteController from the Android Market. Do you want to do this now?")
	    		.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//Show the WiimoteController Android Market Page
						WiimoteController.wiimoteControllerOnMarket(SpheroTestActivity.this);
					}
				})
				.setNegativeButton("No", null)
				.create();
	    	default:
	        	return super.onCreateDialog(id);	    		
    	}
    }
        
    //This function is used to gracefully disconnect from the WiimoteController service
    private void disconnectFromService() {
		if(wcService!=null) {
			//Unregister the stub
			try {
				wcService.unregisterFromCallback(wcCallbackStub);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			//Unbind from the service
			try {
				unbindService(wcServiceConnection);
				wcService=null;
				Toast.makeText(SpheroTestActivity.this, "Disconnected.", Toast.LENGTH_SHORT).show();						
			} catch(IllegalArgumentException iae) {
				Toast.makeText(SpheroTestActivity.this, "Not connected.", Toast.LENGTH_SHORT).show();
			}
		}    	
    }
    
    /**
     * Create a WiimoteController callback stub. This object will get registered with the
     * WiimoteController's public service so that we will get notified of wiimote events.
     * This is where all event functions that react to wiimotes are written.
     * Note that these functions get called by the thread that is separate from your
     * Activity's thread so you might have to use Handlers to dispatch events to your
     * Activity. An example to this is shown in the wiimoteDisconnected callback below.
     */
    private IWiiControllerPublicServiceCallback.Stub wcCallbackStub = new IWiiControllerPublicServiceCallback.Stub() {
		@Override
		public void wiimotesDisconnected() throws RemoteException {
			Log.v(APP_ID, "All wiimotes disconnected.");
			
			//Also show a Toast about this event. Use the activity's handler to make the activity thread show the Toast.
			//This will get executed by whoever owns the "handler" object
			handler.post(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(SpheroTestActivity.this, "All wiimotes disconnected.", Toast.LENGTH_SHORT).show();						
				}
			});			
		}
		
		@Override
		public void wiimotesConnected(int numberOfConnectedWiimotes) throws RemoteException {
			Log.v(APP_ID, numberOfConnectedWiimotes+" wiimote(s) connected.");
		}
		
		@Override
		public void wiimoteDisconnected(int whichWiimote) throws RemoteException {
			Log.v(APP_ID, "WM" + (whichWiimote+1) + " disconnected.");
		}
		
		@Override
		public void newPeripheralDetected(int whichWiimote, int whichPeripheral) throws RemoteException {
			String perName = whichPeripheral==0 ? ": peripheral disconnected." :
                whichPeripheral==1 ? ": nunchuk connected." :
                whichPeripheral==2 ? ": classic controller connected." :
                /* else */           ": unknown peripheral (code "+whichPeripheral+") connected.";
			Log.v(APP_ID, "WM" + (whichWiimote+1) + perName);								
		}
		
		@Override
		public void buttonReleased(int whichWiimote, int whichButton) throws RemoteException {
			Log.v(APP_ID, "WM" + (whichWiimote+1) + ": released button " + whichButton);
		}
		
		@Override
		public void buttonPressed(int whichWiimote, int whichButton) throws RemoteException {
			Log.v(APP_ID, "WM" + (whichWiimote+1) + ": pressed button " + whichButton);
		}
		
		@Override
		public void analogInputStatusChanged(int wiimote, int analogId, double newValue) throws RemoteException {
			Log.v(APP_ID, "WM" + (wiimote+1) + ": AN" + analogId + ": value changed to " + newValue);
		}
	};
    
	/**
	 * This object handles the WiimoteController's service connection. You get notified when the 
	 * service connects (IBinder should be remembered because it is used to send commands to). As
	 * soon as the service connects, you should check version (see checkVersion()) with the 
	 * version number 
	 */
	private ServiceConnection wcServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//Connect to the WiimoteController service
			wcService = IWiiControllerPublicService.Stub.asInterface(service);
			try {				
				if(!wcService.checkVersion(1)) {
					//The current WiimoteController app must be outdated so ask the user to update!
					unbindService(wcServiceConnection);
					Toast.makeText(SpheroTestActivity.this, "Failed. Please update the WiimoteController app!", Toast.LENGTH_LONG).show();
					showDialog(SpheroTestActivity.DIALOG_NOOROLDWIIMOTECONTROLLERAPP);
					return;
				}
				
				//Register the callback stub and check if we will understand each other
				wcService.registerToCallback(wcCallbackStub);

				//Check number of connected wiimotes and report to the user
				int wiimotes = wcService.getNumberOfConnectedWiimotes();
				if(wiimotes<=0) {
					showDialog(SpheroTestActivity.DIALOG_NOWIIMOTES);
				} else {
					Toast.makeText(SpheroTestActivity.this, "Connected to " + wiimotes + " wiimotes.", Toast.LENGTH_SHORT).show();
					Toast.makeText(SpheroTestActivity.this, "See logcat to watch button events.", Toast.LENGTH_SHORT).show();
				}
				
				Log.v("Sphero Wiimote Control", "Ready for step 3?");
			} catch (RemoteException e) {
				//Report the exception to the user
				e.printStackTrace();
				Toast.makeText(SpheroTestActivity.this, "Service crashed! Try again.", Toast.LENGTH_SHORT).show();
			}			
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			wcService=null;
			Toast.makeText(SpheroTestActivity.this, "Service disconnected.", Toast.LENGTH_SHORT).show();
		}
	};
    
    
    
    /*private void blink(final boolean lit){

        if(mRobot != null){

            //If not lit, send command to show blue light, or else, send command to show no light
            if(lit){
                RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 0);        // 1
            }else{
                RGBLEDOutputCommand.sendCommand(mRobot, 0, 0, 255);      // 2
            }

            //Send delayed message on a handler to run blink again
            final Handler handler = new Handler();                       // 3
            handler.postDelayed(new Runnable() {
                public void run() {
                    blink(!lit);
                }
            }, 1000);
        }
    }
    
    private void drive() {
        if(mRobot != null) {
            // Send a roll command to Sphero so it goes forward at full speed.
            RollCommand.sendCommand(mRobot, 0.0f, 1.0f);                         // 1

            // Send a delayed message on a handler
            final Handler handler = new Handler();                               // 2
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // Send a stop to Sphero
                    RollCommand.sendStop(mRobot);                               // 3
                }
            }, 1000);

        }
    }*/
}