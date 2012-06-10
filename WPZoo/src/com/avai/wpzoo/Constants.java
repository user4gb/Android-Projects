package com.avai.wpzoo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;

public class Constants {
	private static Constants ref;
    private final Context ctx;
    
    public String dbName;
    public String hostName;
    public int appDomainId;
    public String snImageUrl;
    public String snImageLink;
    public String snTitleAction;
    public String snActionLink;
    public String snActionText;
    public String kFbConnectApiKey;
    public String kFbConnectApiSecret;
    public String flurryKey;
    public String prefsFileName;
    public String friendFinderServiceName;
    public Drawable backgroundImage;
    public Drawable placeHolderImage;
    public Drawable cellGradient; 
    public Drawable splashImage; 
    public int appIconId;   
	public int friendLocationUpdateInterval;
	public int scheduleNumDays;
	public ScheduleType scheduleType;
    MyPoint ref1Ugs;
    MyPoint ref1Pxl;
    MyPoint ref2Ugs;
    MyPoint ref2Pxl;

    enum ScheduleType {
    	DEFAULT,
    	NO_MY_SCHEDULE;
    };
	
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
	private Constants(Context ctx, String dbName, String hostName, int appDomainId) {
		this.ctx = ctx;
		System.out.println("Constants constructor.  Saving the dbName constant to "+dbName);
		this.dbName = dbName;
		this.hostName = hostName;
        this.appDomainId = appDomainId;
        this.kFbConnectApiKey = "2f2d0504ce41bb17543485f86fec18b1";
        this.kFbConnectApiSecret = "2de41091a3291d9814f50b7fcbeffdb0";
        this.prefsFileName = "AmpPrefs";
        this.friendLocationUpdateInterval = 30; //seconds between friend location updates on the map
        this.friendFinderServiceName = this.hostName + "Data/friendfinder.svc/app/" + this.appDomainId + "/session/";
        this.scheduleType = ScheduleType.DEFAULT;
        this.scheduleNumDays = 1;
        initConstants();
    }
	
	private void initConstants() {
        switch (appDomainId) {
    	case 5: //Woodland Park Zoo
    		int[] colors5 = new int[12];
    		colors5[0] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_light);
			colors5[1] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[2] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[3] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[4] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[5] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[6] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[7] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[8] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[9] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[10] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_orange);
			colors5[11] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_dark);
			cellGradient = new GradientDrawable(Orientation.TOP_BOTTOM, colors5);
			flurryKey = "FBPIE1EH4KZ8JDS5QTQB";
		    snImageUrl = "http://www.zoo.org/view.image?Id=439";
		    snImageLink = "http://www.zoo.org";
		    snTitleAction = " is using the Woodland Park Zoo Android app.";
		    snActionLink = "http://www.zoo.org";
		    snActionText = "Visit the Woodland Park Zoo website";
		    backgroundImage = ctx.getResources().getDrawable(R.drawable.wz_bkg_long);
		    placeHolderImage = ctx.getResources().getDrawable(R.drawable.wz_placeholder);
		    splashImage = ctx.getResources().getDrawable(R.drawable.wz_splash);
		    appIconId = R.drawable.wz_icon;
            ref1Ugs = new MyPoint(47.66713516, -122.3533689);  //Red Panda
            //ref1Pxl = new MyPoint(224,776);
            ref1Pxl = new MyPoint(193,971);
            ref2Ugs = new MyPoint(47.67104649, -122.3497639);  //Elk (Roosevelt)
            //ref2Pxl = new MyPoint(660,116);
            ref2Pxl = new MyPoint(686,174);
            
            
            this.scheduleType = ScheduleType.NO_MY_SCHEDULE;
    		break;
    	case 7: // Houston Zoo
    		int[] colors7 = new int[4];
    		colors7[0] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_light);
			colors7[1] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_light);
			colors7[2] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_dark);
			colors7[3] = ctx.getResources().getColor(R.drawable.cell_menu_gradient_dark);
			cellGradient = new GradientDrawable(Orientation.TOP_BOTTOM, colors7);
			flurryKey = "MXBU9UY66LDT46HQZDFS";
			break;
    	case 15:  // New York Auto Show
		    snImageUrl = "http://amp.avai.com/Images/15/icon_57x57.png";
		    snImageLink = "http://www.autoshowny.com/";
		    snTitleAction = " is using the New York International Auto Show iPhone app.";
		    snActionLink = "http://www.autoshowny.com/";
		    snActionText = "Visit the NYIAS website";
		    break;
    	case 19: // Detroit Zoo
    		int[] colors19 = new int[4];
    		colors19[0] = Color.rgb(56, 151, 240);
			colors19[1] = Color.rgb(39, 120, 196);
			colors19[2] = Color.rgb(21, 89, 152);
			colors19[3] = Color.rgb(4, 57, 107);
			cellGradient = new GradientDrawable(Orientation.TOP_BOTTOM, colors19);
    		snImageUrl = "http://amp.avai.com/Images/20/";
		    snImageLink = "http://www.detroitzoo.org/";
		    snTitleAction = " is using the Detroit Zoo iPhone app.";
		    snActionLink = "http://www.detroitzoo.org/";
		    snActionText = "Visit the Detroit Zoo website";
		    //backgroundImage = ctx.getResources().getDrawable(R.drawable.wz_bkg_long);
    	default:
    		cellGradient = new ColorDrawable(R.drawable.cell_menu_gradient_dark);
    		flurryKey = "Z9D9Q65Q3CPXCPY4Z6R1"; 
    		break;
        }
	}
	
 
    public static Constants sharedConstants(Context ctx, String dbName, String hostName, int appDomainId){
    	if(ref == null) {
    		ref = new Constants(ctx, dbName, hostName, appDomainId);
    	}
    	return ref;
    }
    
    public static Constants sharedConstants(){
    	return ref;
    }  
    
    public Object clone() throws CloneNotSupportedException {
    	throw new CloneNotSupportedException();
    }
}
