import javax.servlet.*;
import java.io.*;
import java.util.*;
import javax.servlet.http.*;
import java.net.*; 
import java.io.IOException;
import java.net.URL;  
import java.net.MalformedURLException;
import java.net.URLEncoder;
import org.json.XML;
import org.json.JSONObject;
import org.json.JSONException;


public class myServletWeather extends HttpServlet 
{ 
	public static boolean parse(String location)  
    {  
        try 
		{
            Integer.parseInt(location);
        }
		catch (NumberFormatException nfe) 
        { 
            return false;
        }

        return true;  
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		  String location = request.getParameter("location");
        
        String type = null;
        if(parse(location))
        {
			
            type = "zip";
        }
        else
        {
            type = "city";
        }
		String tempUnit = request.getParameter("tempUnit");
        String url = "http://default-environment-esmwe35kbt.elasticbeanstalk.com/?location="+URLEncoder.encode(location, "UTF-8")+"&type="+type+"&tempUnit="+tempUnit;
		StringBuffer sb = new StringBuffer();
		String line;
		URLConnection connection = new URL(url).openConnection();
		
		InputStream response1 = connection.getInputStream();
		
		 
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
		PrintWriter out = response.getWriter(); 
		String jsonString = sb.toString();
		int indent=4;
		response.setContentType("application/json; charset=UTF-8");
		
		
		try {
            JSONObject xmlJSONObj = XML.toJSONObject(jsonString);
            String jsonPrettyPrintString = xmlJSONObj.toString(indent);
            out.write(jsonPrettyPrintString);
        } 
		catch (JSONException je) 
		{
            System.out.println(je.toString());
        }
		
		
	
	}
	
}
