package uk.ac.mmu.advprog.hackathon;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;


/**
 * Handles database access from within your web service
 * @author You, Mainly!
 */
public class DB implements AutoCloseable {
	
	//allows us to easily change the database used
	private static final String JDBC_CONNECTION_STRING = "jdbc:sqlite:./data/NaPTAN.db";
	
	//allows us to re-use the connection between queries if desired
	private Connection connection = null;
	
	/**
	 * Creates an instance of the DB object and connects to the database
	 */
	public DB() {
		try {
			connection = DriverManager.getConnection(JDBC_CONNECTION_STRING);
		}
		catch (SQLException sqle) {
			error(sqle);
		}
	}
	
	/**
	 * Returns the number of entries in the database, by counting rows
	 * @return The number of entries in the database, or -1 if empty
	 */
	public int getNumberOfEntries() {
		int result = -1;
		try {
			Statement s = connection.createStatement();
			ResultSet results = s.executeQuery("SELECT COUNT(*) AS count FROM Stops");
			while(results.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				result = results.getInt(results.findColumn("count"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	
	/**
	 * Returns the number of stops in the database by locaction, by counting rows
	 * @Param The string request of user for location
	 * @return The number of entries in the database for amount stops for localilty
	 */
	public int getNumberOfStops(String LocalityName) {
		int result = -1;
		try {
			
			PreparedStatement s = connection.prepareStatement("SELECT COUNT(*) AS NUMBER FROM Stops WHERE LocalityName = ?");
			s.setString(1, LocalityName); //What is after the first question mark
			System.out.println(LocalityName);
			ResultSet rs = s.executeQuery();
			
			while(rs.next()) { //will only execute once, because SELECT COUNT(*) returns just 1 number
				result = rs.getInt(rs.findColumn("NUMBER"));
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	
	
	/**
	 * Returns the number of stops in the database by locaction, by counting rows
	 * @Param The string request of user for Location name and stoptype
	 * @return The number of entries in the database for amount stops for localilty depending on StopType
	 */
	public JSONArray getNumberOfStopsTransport(String LocalityName ,String StopType) {
		JSONArray result = new JSONArray();
		try {
			
			PreparedStatement s = connection.prepareStatement("SELECT * FROM Stops WHERE LocalityName = ? AND StopType = ?;");
			s.setString(1, LocalityName); 
			s.setString(2, StopType);
			ResultSet rs = s.executeQuery();
			
			while(rs.next()) {
				
				JSONObject locationNameobj = new JSONObject();
				
				String commonName = rs.getString("CommonName");
			    String locality = rs.getString("LocalityName");
			    String stopType = rs.getString("StopType");
			    
			    // Set name, locality, and type to empty strings if they are null
			    locationNameobj.put("name", commonName != null ? commonName : "");
			    locationNameobj.put("locality", locality != null ? locality : "");
			    locationNameobj.put("type", stopType != null ? stopType : "");
			    
				
				JSONObject locationVariableObj = new JSONObject();
				
				String indicator = rs.getString("Indicator");
			    String bearing = rs.getString("Bearing");
			    String street = rs.getString("Street");
			    String landmark = rs.getString("Landmark");

				
			    locationVariableObj.put("indicator", indicator != null ? indicator : "");
			    locationVariableObj.put("bearing", bearing != null ? bearing : "");
			    locationVariableObj.put("street", street != null ? street : "");
			    locationVariableObj.put("landmark", landmark != null ? landmark : "");
			    
				
				//Place the variable
				locationNameobj.put("location", locationVariableObj);
				
				//Place the variable
				locationNameobj.put("type", rs.getString("StopType"));
				
				result.put(locationNameobj);
	
				
				
			}
		}
		catch (SQLException sqle) {
			error(sqle);
			
		}
		return result;
	}
	
	/**
	 * Returns the number of stops in the database by locaction, by counting rows
	 * @Param The string request of user for logitiude and latitude and stoptype
	 * @return The details of the nearest stops in XMl based format
	 */
	
	public String getNearestRoutes(String latitude, String longitude, String stopType) {
		
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder;
	    
	    try {
	    	
	        
	        dBuilder = dbFactory.newDocumentBuilder();
	        Document doc = (Document) dBuilder.newDocument();
	        
	        Element rootElement = doc.createElement("NearestStops");
	        doc.appendChild(rootElement);
	        
	        PreparedStatement s = connection.prepareStatement("SELECT * FROM Stops WHERE StopType = ? AND Latitude IS NOT NULL AND Longitude IS NOT NULL ORDER BY (((? - Latitude) * (? - Latitude)) + (0.595 * ((? - Longitude) * (? - Longitude)))) ASC LIMIT 5");
	        s.setString(1, stopType);
	        s.setString(2, latitude);
	        s.setString(3, latitude);
	        s.setString(4, longitude);
	        s.setString(5, longitude);
	        
	        ResultSet rs = s.executeQuery();
	        while(rs.next()) {
	            Element stop = doc.createElement("Stop");
	            stop.setAttribute("code", rs.getString("NaptanCode"));
	            
	            Element name = doc.createElement("Name");
	            name.appendChild(doc.createTextNode(rs.getString("CommonName")));
	            
	            Element locality = doc.createElement("Locality");
	            locality.appendChild(doc.createTextNode(rs.getString("LocalityName")));
	            
	            Element location = doc.createElement("Location");
	            Element street = doc.createElement("Street");
	            
	            street.appendChild(doc.createTextNode(rs.getString("Street")));
	            
	            Element landmark = doc.createElement("Landmark");
	            landmark.appendChild(doc.createTextNode(rs.getString("Landmark")));
	            
	            Element latitudeElement = doc.createElement("Latitude");
	            latitudeElement.appendChild(doc.createTextNode(rs.getString("Latitude")));
	            
	            Element longitudeElement = doc.createElement("Longitude");
	            longitudeElement.appendChild(doc.createTextNode(rs.getString("Longitude")));
	            
	            location.appendChild(street);
	            location.appendChild(landmark);
	            location.appendChild(latitudeElement);
	            location.appendChild(longitudeElement);
	            stop.appendChild(name);
	            stop.appendChild(locality);
	            stop.appendChild(location);
	            rootElement.appendChild(stop);
	            
	        
	            
	        }
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        Element root = doc.getDocumentElement();
	        DOMSource source = new DOMSource(root);
	        StringWriter writer = new StringWriter();
	        StreamResult result = new StreamResult(writer);
	        transformer.transform(source, result);
	        return writer.toString();

	        
	    } catch (Exception e) {
	        error((SQLException) e);
	        return null;
	    }
	}

	







	
	
	/**
	 * Closes the connection to the database, required by AutoCloseable interface.
	 */
	@Override
	public void close() {
		try {
			if ( !connection.isClosed() ) {
				connection.close();
			}
		}
		catch(SQLException sqle) {
			error(sqle);
		}
	}

	/**
	 * Prints out the details of the SQL error that has occurred, and exits the programme
	 * @param sqle Exception representing the error that occurred
	 */
	private void error(SQLException sqle) {
		System.err.println("Problem Opening Database! " + sqle.getClass().getName());
		sqle.printStackTrace();
		System.exit(1);
	}

	

	
}
