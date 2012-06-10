package com.avai.wpzoo;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HttpHelper {
	
	public static Integer getInteger(Context ctx, String url)
	{
		try {
			return Integer.parseInt(HttpHelper.getString(ctx, url));
		} catch(NumberFormatException nfe) {
			System.out.println("Could not parse response as an Integer");
			return null;
		}
	}
	
	public static JSONObject getJSONObject(Context ctx, String service)
	{
		String str = HttpHelper.getString(ctx, service);
		if (str == null)
			return null;
		JSONObject obj;
        try{
        	obj = new JSONObject(str);
        } catch (JSONException e) {
        	System.out.println("Error: Unable to convert downloaded data to JSON");
        	return null;
        }
        return obj;
	}
 
	public static String getString(Context ctx, String url) {
		InputStream content = HttpHelper.get(ctx, url);
		if(content == null) {
			return null;
		}else {
			StringBuffer sb = null;
			try{
				int ch;
				sb = new StringBuffer();
				while((ch = content.read()) != -1) {
					sb.append((char)ch);
				}
			}
			catch(Exception e){
				Log.e("StringBuffer", "failed to turn input into string");
			}
			
			try {
				content.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return sb.toString();
		}
	}
	
	public static InputStream get(Context ctx, String url) {
		return HttpHelper.get(ctx, url, null, null);
	}

    public static InputStream get(Context ctx, String url, String body, final UsernamePasswordCredentials creds) {
        System.out.println("Getting url: " + url);
        	if(networkAvailable(ctx)) {
	    	try{
	        	DefaultHttpClient httpClient = new DefaultHttpClient();
	        	if(creds != null) {
	        		httpClient.getCredentialsProvider().setCredentials(new AuthScope("twitter.com", 443), creds);
		        	httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
		                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		                    AuthState authState = (AuthState) context.getAttribute(
		                    ClientContext.TARGET_AUTH_STATE);
		                    if (authState.getAuthScheme() == null) {
		                        authState.setAuthScheme(new BasicScheme());
		                        authState.setCredentials(creds);
		                    }
		                }
		        	}, 0);
	        	}
	        	
	        	HttpResponse response = null; 
	        	if(body == null) { 
	        		HttpGet request = new HttpGet(url);
	        		response = httpClient.execute(request);
	        	}
	        	else 
	        	{
	        		HttpPost request = new HttpPost(url);
	        		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
	        		request.getParams().setBooleanParameter("http.protocol.expect-continue", false);
	        		request.setEntity(new StringEntity(body, HTTP.UTF_8));
	        		response = httpClient.execute(request);
	        	}
	        	int status = response.getStatusLine().getStatusCode();
	         
	        	// we assume that the response body contains the error message
	        	if (status != HttpStatus.SC_OK) {
	        		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
	        		response.getEntity().writeTo(ostream);
	        		Log.e("HTTP CLIENT", ostream.toString());
	        		return null;
	        	} else {
	        		return response.getEntity().getContent();
	        	}
	        }
	        catch(ClientProtocolException cpe) {
	        	Log.e("ClientProtocolException", "HttpClient Failed!");
	        	cpe.printStackTrace();
	        	return null;
	        }catch(IOException ioe){
	        	Log.e("IOException", "HttpClient Failed!");
	        	ioe.printStackTrace();
	        	return null;
	        }  catch(Exception e) {
	        	Log.e("Exception", "HttpClient Failed!");
	        	e.printStackTrace();
	        	return null;
	        }
    	} else {
    		System.out.println("Network connection unavailable");
    		return null;
    	}
    }
	
	public static boolean save(Context ctx, InputStream iStream, String fileName){
		FileOutputStream fOut = null;
	    try{
	    	fOut = ctx.openFileOutput(fileName, 0);
    		byte buf[]=new byte[1024];
 		    int len;
 		    while((len=iStream.read(buf))>0)
 		    	fOut.write(buf,0,len);
			fOut.flush();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
		        fOut.close();
		    } catch (IOException e) {
		    	e.printStackTrace();
		    	return false;
		    }
		}
		return true;
	}
	
    public static boolean networkAvailable(Context ctx) {
        ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        if(activeInfo != null && activeInfo.isConnected()) {
        	return true;
        }
        return false;
    }

	public static void presentAlert(Context ctx, CharSequence title, CharSequence message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(message);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.setTitle(title);
		alert.show();
	}
}
