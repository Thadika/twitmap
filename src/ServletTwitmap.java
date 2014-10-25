
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

public class ServletTwitmap extends HttpServlet{
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	    response.setContentType("application/json");
	    PrintWriter out = response.getWriter();
	    try {
	    //Get the entered keyword, search for appropriate tweets
	    String key = request.getParameter("keyword");
	    //Populate with latitude and longitude of tweets
	    JSONObject result = new JSONObject();
	    String marker = "40.807536,-73.962573";
	    result.put("marker",marker);
	    String jsonResult = JSONObject.toJSONString(result);
	    out.println(jsonResult);
	    } catch (Exception ex) {
	        out.println("{\"message\":\"Error - caught exception in ExportServlet\"}");
	    } finally {
	        out.flush();
	        out.close();
	    }
	}

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }


}
