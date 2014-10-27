
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;

public class ServletTwitmap extends HttpServlet{
	private TwitterApi api;
	
	public void init(){
		api = new TwitterApi();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	    response.setContentType("application/json");
	    PrintWriter out = response.getWriter();
	    try {
	    //Get the entered keyword, search for appropriate tweets
	    String key = request.getParameter("keyword");
	    List<String> locations =  api.getTweetsByHashtag(key);
	    JSONArray array = new JSONArray(locations);
	    //Populate with latitude and longitude of tweets
	    JSONObject result = new JSONObject();
	    //String marker = "40.807536,-73.962573";
	    result.put("success", true);
	    result.put("markers",array);
	    String jsonResult = result.toString();
	    out.println(jsonResult);
	    } catch (Exception ex) {
	    	//String err = ex.getMessage();
	        out.println("{\"message\":\"Error - caught exception in ExportServlet\", \"success\":\"" + false +"\"}");
	    } finally {
	        out.flush();
	        out.close();
	    }
	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }


}