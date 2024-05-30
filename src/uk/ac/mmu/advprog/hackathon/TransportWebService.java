package uk.ac.mmu.advprog.hackathon;
import static spark.Spark.get;
import static spark.Spark.port;

import java.io.Serializable;
import java.util.Set;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles the setting up and starting of the web service
 * You will be adding additional routes to this class, and it might get quite large
 * Feel free to distribute some of the work to additional child classes, like I did with DB
 * @author You, Mainly!
 */
public class TransportWebService {

	/**
	 * Main program entry point, starts the web service
	 * @param args not used
	 */
	public static void main(String[] args) {		
		port(8088);
		
		//Simple route so you can check things are working...
		//Accessible via http://localhost:8088/test in your browser
		get("/test", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					return "Number of Entries: " + db.getNumberOfEntries();
				}
			}			
		});
		
		/**
		 * Get request to using url credentials
		 * 
		 * @return The number of stops by locality
		 */
		
		get("/stopcount", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				
				String localityName = request.queryParams("locality"); //after the url part with 'locality'
				
				if(localityName == null || localityName.isEmpty() ) {
					return ("Invalid Request");
				}
				
				try (DB db = new DB()) {
					return  db.getNumberOfStops(localityName);//'request the bit you have typed after 'locality' and run it through the method
				}
			}			
		});
		
		/**
		 * Get request to using url credentials
		 * 
		 * @return The number of stops by locality and stoptype
		 */
		
		get("/stops", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				
				String localityName = request.queryParams("locality");
				String stopType = request.queryParams("type");
				
				try (DB db = new DB()) {
					response.header("Content-Type","application/json");
					
					return  db.getNumberOfStopsTransport(localityName , stopType);
					
				}
			}			
		});
		
		/**
		 * Get request to using url credentials
		 * 
		 * @return The number difference between longtitude and latitude between two locations
		 */
		
		get("/nearest", new Route() {
		    @Override
		    public Object handle(Request request, Response response) throws Exception {
		    	
		        String latitude = request.queryParams("latitude");
		        String longitude = request.queryParams("longitude");
		        String stopType = request.queryParams("type");
		        try (DB db = new DB()) {
		            response.type("application/json");
		            return db.getNearestRoutes(latitude, longitude, stopType);
		        }
		    }
		});

		
		
		System.out.println("Server up! Don't forget to kill the program when done!");
	}

}
