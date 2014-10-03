package com.example.safetyapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.*;

import com.google.gson.Gson;

public class MainActivity extends Activity {
	double latitude = 0, longitude = 0;
	String latString, longString;
	String weatherUrl = "";
	static WeatherResponse weatherResponse;
	public final static String EXTRA_MESSAGE = "com.example.safetyapp.MESSAGE";
	Significance sig = null;

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Log.d("thingy", "DOING ONCREATE IN MAIN");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if(!isNetworkAvailable()) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);  
			alertDialog.setTitle("Alert");  
			alertDialog.setMessage("Location is not available");  
			alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {  
					return;
				} });  
			AlertDialog alert = alertDialog.create();
			alert.show();
		} else{

			// Acquire a reference to the system Location Manager
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

			// Register the listener with the Location Manager to receive location updates
			//		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);

			Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

			

			if(location == null) {
				//Log.d("Location: ", "AAAAAAAAAHHHH!!!");
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);  
				alertDialog.setTitle("Alert");  
				alertDialog.setMessage("Location is not available");  
				alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {  
					public void onClick(DialogInterface dialog, int which) {  
						return;
					} });  
				AlertDialog alert = alertDialog.create();
				alert.show();
			} else {
				//Log.d("Location: ", location.toString());
				onLocationFound(location);
			}
		}
	}

	public void onLocationFound(Location location) {

		// Called when a new location is found by the network location provider.
		
		latitude = location.getLatitude();
		longitude = location.getLongitude();


		//System.out.println("Latitude: " + latitude + "\tLongitude: " + longitude);
		latString = latitude + "";
		longString = longitude + "";

		//url = url + "https://maps.googleapis.com/maps/api/place/textsearch/json?query=police&sensor=true&key=AIzaSyBq5yC-gVXwWV_Noqif-jFTet-JBYkPYXU&types=police&radius=15000&location=";
		weatherUrl = weatherUrl + "http://api.wunderground.com/api/e6e5804701ec0a4a/alerts/q/";
		weatherUrl = weatherUrl + latString + "," + longString + ".json";

		URL u;
		try {
			u = new URL(weatherUrl);
			HttpGetter get1 = new HttpGetter();
			get1.execute(u);

			//get.onPostExecute();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	private class HttpGetter extends AsyncTask<URL, Void, Void> {

		@Override
		protected Void doInBackground(URL... params) {
			// Create an HTTP client
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet getRequest;
			HttpResponse response;

			getRequest = new HttpGet(weatherUrl);

			// Execute the request and get an input stream of the response

			try {
				response = client.execute(getRequest);
				InputStream stream = response.getEntity().getContent();

				// Use GSON to to convert the stream into Java objects
				Reader reader = new InputStreamReader(stream);

				//Log.d("Here", "About to parse JSON");
				// Create the MapsResponse class, Gson will handle all the hard work.
				Gson gson = new Gson();

				weatherResponse = gson.fromJson(reader, WeatherResponse.class);
				//Log.d("parse JSON ", weatherResponse == null ? "null response" : "weather response found");


				//	TextView thingy = (TextView) findViewById(R.id.thingy);
				//	thingy.setText(/*mapsResponse.results.toString()*/"HI MORGAN!!!!!");

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return null;
		}


		@Override
		protected void onPostExecute(Void result) {
			//Log.d("executed: ", mapsResponse.toString());
			//Parse mapsResponse for text
			String alerts = "";
			Significance mostSevere = null;

			for(int i = 0; i < weatherResponse.alerts.size(); i++) {
				Alert a = weatherResponse.alerts.get(i);
				alerts = alerts + a.description + "      ";
				
				Significance temp = null;
				if(a.significance.equals("W"))
					temp = Significance.warning;
				else if(a.significance.equals("A"))
					temp = Significance.watch;
				else if(a.significance.equals("Y"))
					temp = Significance.advisory;
				else if(a.significance.equals("S"))
					temp = Significance.statement;
				else if(a.significance.equals("F"))
					temp = Significance.forecast;
				else if(a.significance.equals("O"))
					temp = Significance.outlook;
				else if(a.significance.equals("N"))
					temp = Significance.synopsis;
				
				if(temp != null) {
					if(mostSevere == null)
						mostSevere = temp;
					else {
						if(temp.compareTo(mostSevere) < 0)
							mostSevere = temp;
					}
				}
			}
			
			if(mostSevere != null) {
				LinearLayout l = (LinearLayout)findViewById(R.id.safetyAlerts);
				//l.setBackground(getResources().getDrawable(R.drawable.warning));
				switch(mostSevere) {
				case warning:
					l.setBackgroundDrawable(getResources().getDrawable(R.drawable.warning));
					break;
				case watch:
					l.setBackgroundDrawable(getResources().getDrawable(R.drawable.watch));
					break;
				case advisory:
					l.setBackgroundDrawable(getResources().getDrawable(R.drawable.advisory));
					break;
				//case statement:
				//	l.setBackgroundDrawable(getResources().getDrawable(R.drawable.advisory));
				//	break;
				default:
					break;
				}
				
			}
			
			TextView alertMessageTextView = (TextView)findViewById(R.id.textView2);
			if(alerts.equals(""))
				alerts = "No Alerts For Your Area";
			alertMessageTextView.setText(alerts);

		}

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	public void openPoliceFireActivity(View view) {
		Intent intent = new Intent(this, PoliceFireActivity.class);
		startActivity(intent);
	}

	public void openHealthActivity(View view) {
		Intent intent = new Intent(this, HealthActivity.class);
		startActivity(intent);
	}

	public void openGovernmentActivity(View view) {
		Intent intent = new Intent(this, GovernmentActivity.class);
		startActivity(intent);
	}
	
	public void openAlertsActivity(View view) {
		//TODO: pass in the WeatherResponse object!!!!
		Intent intent = new Intent(this, AlertsActivity.class);
//		intent.putExtra(EXTRA_MESSAGE, weatherResponse);
		startActivity(intent);
	}

}
