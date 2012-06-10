package com.avai.wpzoo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.R;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ImageFinder {

	public static Drawable getDrawable(Context ctx, String imageName, String remoteUrl)
	{
		Drawable img = null;
		
		// CHECK RESOURCES
		try{
			Field field = R.drawable.class.getDeclaredField(imageName);
			int value = field.getInt(R.drawable.class);
			img = ctx.getResources().getDrawable(value);
		}
		catch(NoSuchFieldException e) {}
		catch (IllegalAccessException e) {}
		catch (IllegalArgumentException e) {}
		
		// CHECK ASSETS
		if(img == null) {
			try {
				InputStream is = ctx.getAssets().open(imageName);
				img = Drawable.createFromStream(is, imageName);
			} catch (IOException e) {}
		}
		
		// CHECK FILES DIRECTORY
		String path = ctx.getFilesDir() + "/" + imageName;
		if(img == null) {
			// Look in files directory
			
			try {
				img = Drawable.createFromPath(path);
			} catch(OutOfMemoryError oome) {
				img = null;
			}
		}
		
		// DOWNLOAD IMAGE
		if(img == null && HttpHelper.networkAvailable(ctx)){
			// Download image to files directory
			InputStream is = HttpHelper.get(ctx, remoteUrl.replaceAll(" ", "%20"));
			if(!HttpHelper.save(ctx, is, imageName)) {
				System.out.println("Failed to save " + imageName);
			} else {
				img = Drawable.createFromPath(path);
			}
		}
	
		// USE PLACEHOLDER
		if(img == null) {		
			System.out.println("Couldn't find image: " + imageName + ", using placeholder image");
			img = Constants.sharedConstants().placeHolderImage;
		}
		return img;
	}	
 
	public static Bitmap getBitmap(Context ctx, String imageName, String remoteUrl) {
		BitmapDrawable imgDrawable = (BitmapDrawable) ImageFinder.getDrawable(ctx, imageName, remoteUrl);
		return imgDrawable.getBitmap();
	}
/*	
	public static Bitmap getBitmap(Context ctx, String imageName, String remoteUrl) {
		BitmapFactory.Options bfo = new BitmapFactory.Options();
		//bfo.inDensity = 0;
		Bitmap img = null;
		// Look for image in resources bundle
		try{
			Field field = R.drawable.class.getDeclaredField(imageName);
			int value = field.getInt(R.drawable.class);
			img = BitmapFactory.decodeResource(ctx.getResources(), value, bfo);
		}
		catch(NoSuchFieldException e) {}
		catch (IllegalAccessException e) {}
	    catch (IllegalArgumentException e) {}
	    
		if(img == null) {
			// Look in files directory
			String path = ctx.getFilesDir() + "/" + imageName;
			img = BitmapFactory.decodeFile(path, bfo);
			if(img == null) {
				if(HttpHelper.networkAvailable(ctx)) {
					// Download image to files directory
					InputStream is = HttpHelper.get(ctx, remoteUrl);
					if(!HttpHelper.save(ctx, is, imageName)) {
						System.out.println("Failed to save " + imageName);
					} else {
						img = BitmapFactory.decodeFile(path, bfo);
					}
				} else {
					// Use a placeholder image
					System.out.println("Couldn't find image at path: " + path + ", using placeholder image");
					img = ((BitmapDrawable)Constants.sharedConstants().placeHolderImage).getBitmap();
				}
			}
		}
		return img;
	}*/
/*	
	@SuppressWarnings("unchecked")
	public static Drawable getDrawable(Class clazz, String imageName) {
		InputStream is = clazz.getClassLoader().getResourceAsStream("com/avai/amp/drawable/" + imageName);
        if(is != null)
        	return Drawable.createFromStream(is, imageName);
        return null;
    }
    
    @SuppressWarnings("unchecked")
	public static BitmapDrawable getBitmapDrawable(Class clazz, String imageName) {
        InputStream is = clazz.getClassLoader().getResourceAsStream("com/avai/amp/drawable/" + imageName);
        if(is != null)
        	return (BitmapDrawable) BitmapDrawable.createFromStream(is, imageName);
        return null;
    }
    
    @SuppressWarnings("unchecked")
	public static Bitmap getBitmap(Class clazz, String imageName) {
        InputStream is = clazz.getClassLoader().getResourceAsStream("com/avai/amp/drawable/" + imageName);
        if(is != null)
        	return (Bitmap) BitmapFactory.decodeStream(is);
        return null;
    }
	*/
	public static void preloadImage(Context ctx, String imageName, String remoteUrl) {
		String path = ctx.getFilesDir() + "/" + imageName;
		File file = new File(path);
		if(!file.exists() && HttpHelper.networkAvailable(ctx)) {
			InputStream is = HttpHelper.get(ctx, remoteUrl);
			HttpHelper.save(ctx, is, imageName);
		}
	}
	
	public static ArrayList<HashMap<String, Object>> getUpdatedImageList() {		
		Cursor cursor = DatabaseHelper.sharedDb().rawQuery("SELECT _id, Url FROM Image WHERE Location IS NULL", null);
	    ArrayList<HashMap<String, Object>> images = new ArrayList<HashMap<String, Object>>();
	    while(cursor.moveToNext())
	    {
	    	HashMap<String, Object> image = new HashMap<String, Object>();
	    	image.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
	    	image.put("Url", cursor.getString(cursor.getColumnIndex("Url")));
	    	images.add(image);
	    }
	    cursor.close();
		return images;
	}
}
