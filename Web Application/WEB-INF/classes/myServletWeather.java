import java.io.IOException;
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*; 

import java.net.URL;  
import java.net.MalformedURLException;
import java.net.URLEncoder;
import org.json.XML;
import org.json.JSONObject;
import org.json.JSONException;


public class myServletWeather extends HttpServlet 
{ 
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		String url = "http://default-environment-esmwe35kbt.elasticbeanstalk.com/?location=90007&type=zip&tempUnit=c";
		
		URLConnection connection = new URL(url).openConnection();
		
		InputStream response1 = connection.getInputStream();
		StringBuffer sb = new StringBuffer();
		String line; 
		try 
		{
 
			BufferedReader bfr = new BufferedReader(new InputStreamReader(response1));
			while ((line = bfr.readLine()) != null) 
			{
				sb.append(line);
			}
 
		}
		catch (IOException e) 
		{
			System.out.println(e.toString());
		} 
		
		String jsonString = sb.toString();
		int indent=4;
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter out = response.getWriter(); 
		
		try {
            JSONObject xmlJSONObj = XML.toJSONObject(jsonString);
            String jsonPrettyPrintString = xmlJSONObj.toString(indent);
            out.write(jsonPrettyPrintString);
        } 
		catch (JSONException je) 
		{
            out.println(je.toString());
        }
		
		
	
	}
	
}
