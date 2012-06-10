import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class CookieTest {
	private String user = "cm33462",
				   pass = "";
	
	public static void main(String[] args) throws Exception {
		// Get html of login page
	    BufferedReader in = new BufferedReader(
                new InputStreamReader(
                		new URL("https://utdirect.utexas.edu/registration/chooseSemester.wbx").
                		openConnection().
                		getInputStream()));

	    String html = "", line;
	    while ((line = in.readLine()) != null)
			html += line;
	    html.toLowerCase();
	    
	    URL url = new URL("http://utdirect.utexas.edu/security-443/logon_check.logonform");
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("POST");
	    conn.setDoOutput(true);
	    conn.setDoInput(true);
	    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
	    
	    String post_str = "";
	    // Populate post_str
	    /*
	    Set keys = data.keySet();
		Iterator keyIter = keys.iterator();
		String content = "";
		for(int i=0; keyIter.hasNext(); i++) {
			Object key = keyIter.next();
			if(i!=0) {
				content += "&";
			}
			content += key + "=" + URLEncoder.encode(data.get(key), "UTF-8");
		}
		System.out.println(content);
		out.writeBytes(content);
		out.flush();
		out.close();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line = "";
		while((line=in.readLine())!=null) {
			System.out.println(line);
		}
		in.close();
	    */
	    
	    
		// Scrub for hidden fields
	    html.indexOf("");
	    
	    
		// Put found data into POST Request
		
		// Send data
		
		// Print response

	}

}
