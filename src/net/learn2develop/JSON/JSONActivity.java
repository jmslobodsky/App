package net.learn2develop.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/* Open Weather Data API
   - example: "http://api.openweathermap.org/data/2.5/group?id=524901,703448,2643743&units=metric" 
   - more API details: http://bugs.openweathermap.org/projects/api/wiki/Api_2_5_weather
   - Examples and Guides (e.g. Android):
     http://bugs.openweathermap.org/projects/api/wiki/Projects
 */
public class JSONActivity extends Activity  {
	private TextView textView;


	public String readJSONFeed( String urlString ) {
		
		StringBuilder stringBuilder = new StringBuilder();
		
		// 1. HTTP processing
		
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet( urlString );
		
		try {
			
			Log.d("JSON", "HTTPClinet: execute " + urlString);
			HttpResponse response = client.execute( httpGet );
			
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			
			if (statusCode == 200) {
				
				HttpEntity entity = response.getEntity();
				
				InputStream content = entity.getContent();
				
				BufferedReader reader = new BufferedReader(
						new InputStreamReader( content ) );
				
				String line;
				
				// build the JSON string
				while ((line = reader.readLine()) != null) {
					
					    stringBuilder.append(line);
				}
			} else {
				Log.d( "JSON", "Failed to download file" );
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Log.e ("JSON", stringBuilder.toString() );
		return stringBuilder.toString();
	}
	
	// asynchronous processing: AsyncTask
    private class ReadJSONFeedTask extends AsyncTask<String, Void, String> {
    	
        protected String doInBackground( String... urls ) {
        	
            return readJSONFeed( urls[0] );
        }
        
        // post-processing
        // - result: the JSON string returned by doInBackground( )
        protected void onPostExecute( String result ) {
        	
        	String finalResult="";
        	
        	try {
        		
        		Log.d( "JSON", result );
    			
        		/* parsing of the JSON file sent from OpenWeatherMap  
        		   - JSONObject, JSONArray
        		 */
        		JSONObject jsonObject = new JSONObject( result ); // {
        		
        		//JSONArray jsonArray = jsonObject.getJSONArray( "list" ); // "list":[
        		        			
        			String cityName, currentTemperature, description, low, high;
        			currentTemperature = "";
        			description = "";
        			low = "";
        			high = "";
        			cityName = jsonObject.getString("name");   // "name":
        			
        			//JSONObject jsonObject1 = jsonObject.getJSONObject( "weather" ); // "coord":{
        			JSONArray jsonArray = jsonObject.getJSONArray("weather");
        			JSONObject jsonObject1 = jsonArray.getJSONObject(0);
        			
        			description = jsonObject1.getString("description");
        			
        			JSONObject jsonObject4 = jsonObject.getJSONObject( "main" );   // "main":{
        			
        			currentTemperature = jsonObject4.getString( "temp" );           // "temp":
        			low = jsonObject4.getString( "temp_min" );           // "temp":
        			high = jsonObject4.getString( "temp_max" );           // "temp":
        			
            		
            		finalResult += cityName + " , current temperature: " +
        					        currentTemperature + "\ndescription: " +
					                description + " , low/high: " +
					                low + "/" + high + "\n";		
        		
    		} catch ( Exception e ) { Log.d( "JSON", e.toString() ); }  
        	
        	textView.setText( finalResult ); // interaction: UI and AsyncTask
        }
    } // end class ReadJSONFeedTask

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Acquire a reference to the system Location Manager

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
//		      makeUseOfNewLocation(location);
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		
		
		Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		textView = (TextView) findViewById(R.id.textView );
		
		//Log.d("JSON", "before execute");
		
		// execute a worker's thread (AsyncTask)
		
		new ReadJSONFeedTask().execute(
				
			"http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&units=metric"
		);
        
	}
}