package com.example.safetyapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.safetyapp.HealthActivity.Type;
import com.google.gson.Gson;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;

public class GovernmentActivity extends Activity {
	enum Type {cityHall, courthouse, localGovtOffice};
	double latitude = 0, longitude = 0;
	String latString, longString;
	String cityHallUrl = "";
	String detailedCityHallUrl = "";
	String courthouseUrl = "";
	String detailedCourthouseUrl = "";
	String localGovtOfficeUrl = "";
	String detailedLocalGovtOfficeUrl = "";
	
	MapsResponse cityHallMapsResponse;
	DetailedMapResponse detailedCityHallMapsResponse;
	MapsResponse courthouseMapsResponse;
	DetailedMapResponse detailedCourthouseMapsResponse;
	MapsResponse localGovtOfficeMapsResponse;
	DetailedMapResponse detailedLocalGovtOfficeMapsResponse;

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_government);

		setupActionBar();

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
		//makeUseOfNewLocation(location);
		latitude = location.getLatitude();
		longitude = location.getLongitude();


		//System.out.println("Latitude: " + latitude + "\tLongitude: " + longitude);
		latString = latitude + "";
		longString = longitude + "";

		//url = url + "https://maps.googleapis.com/maps/api/place/textsearch/json?query=cityHall&sensor=true&key=AIzaSyBq5yC-gVXwWV_Noqif-jFTet-JBYkPYXU&types=cityHall&radius=15000&location=";
		cityHallUrl = cityHallUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=cityHall&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=city_hall&radius=15000&location=";
		cityHallUrl = cityHallUrl + latString + "," + longString;

		courthouseUrl = courthouseUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=courthouse&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=courthouse&radius=15000&location=";
		courthouseUrl = courthouseUrl + latString + "," + longString;
		
		localGovtOfficeUrl = localGovtOfficeUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=localGovtOffice&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=local_government_office&radius=15000&location=";
		localGovtOfficeUrl = localGovtOfficeUrl + latString + "," + longString;
		
		URL u1, u2, u3;
		try {
			u1 = new URL(cityHallUrl);
			u2 = new URL(courthouseUrl);
			u3 = new URL(localGovtOfficeUrl);
			
			HttpGetter get1 = new HttpGetter(Type.cityHall);
			HttpGetter get2 = new HttpGetter(Type.courthouse);
			HttpGetter get3 = new HttpGetter(Type.localGovtOffice);
			get1.execute(u1);
			get2.execute(u2);
			get3.execute(u3);

			//get.onPostExecute();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private class HttpGetter extends AsyncTask<URL, Void, Void> {
		private Type type;

		private HttpGetter(Type t) {
			type = t;
		}

		@Override
		protected Void doInBackground(URL... params) {
			// Create an HTTP client
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet getRequest;
			HttpResponse response;

			if(type == Type.cityHall)
				getRequest = new HttpGet(cityHallUrl);
			else if(type == Type.courthouse)
				getRequest = new HttpGet(courthouseUrl);
			else
				getRequest = new HttpGet(localGovtOfficeUrl);
			
			// Execute the request and get an input stream of the response


			try {
				response = client.execute(getRequest);
				InputStream stream = response.getEntity().getContent();

				// Use GSON to to convert the stream into Java objects
				Reader reader = new InputStreamReader(stream);

				//Log.d("Here", "About to parse JSON");
				// Create the MapsResponse class, Gson will handle all the hard work.
				Gson gson = new Gson();
				if(type == Type.cityHall) {
					cityHallMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", cityHallMapsResponse == null ? "null response" : "cityHall response found");
				} else if(type == Type.courthouse) {
					courthouseMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", courthouseMapsResponse == null ? "null response" : "courthouse response found");
				} else {
					localGovtOfficeMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", localGovtOfficeMapsResponse == null ? "null response" : "localGovtOffice response found");
				}

				//	TextView thingy = (TextView) findViewById(R.id.thingy);
				//	thingy.setText(/*mapsResponse.results.toString()*/"HI MORGAN!!!!!");

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}


		@Override
		protected void onPostExecute(Void result) {
			//Log.d("executed: ", mapsResponse.toString());
			//Parse mapsResponse for text
			if(type == Type.cityHall) {
				String cityHallName = cityHallMapsResponse.results.get(0).name;
				String cityHallAddress = cityHallMapsResponse.results.get(0).vicinity;
				
				TextView cityHall = (TextView)findViewById(R.id.cityHallText);
				cityHall.setText("City Hall: " + cityHallName);

				TextView cityHallAddr = (TextView)findViewById(R.id.cityHallInfoText);
				cityHallAddr.setText(cityHallAddress);

				detailedCityHallUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedCityHallUrl += cityHallMapsResponse.results.get(0).reference;
				detailedCityHallUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";
				
				URL u;
				try {
					u = new URL(detailedCityHallUrl);
					HttpDetailedGetter get = new HttpDetailedGetter(type);
					get.execute(u);

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(type == Type.courthouse) {
				String courthouseName = courthouseMapsResponse.results.get(0).name;
				String courthouseAddress = courthouseMapsResponse.results.get(0).vicinity;
				
				TextView courthouse = (TextView)findViewById(R.id.courthouseText);
				courthouse.setText("Courthouse: " + courthouseName);

				TextView courthouseAddr = (TextView)findViewById(R.id.courthouseInfoText);
				courthouseAddr.setText(courthouseAddress);

				detailedCourthouseUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedCourthouseUrl += courthouseMapsResponse.results.get(0).reference;
				detailedCourthouseUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";
				
				URL u;
				try {
					u = new URL(detailedCourthouseUrl);
					HttpDetailedGetter get = new HttpDetailedGetter(type);
					get.execute(u);

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String localGovtOfficeName = localGovtOfficeMapsResponse.results.get(0).name;
				String localGovtOfficeAddress = localGovtOfficeMapsResponse.results.get(0).vicinity;
				
				TextView localGovtOffice = (TextView)findViewById(R.id.localGovtOfficeText);
				localGovtOffice.setText("Local Government Office: " + localGovtOfficeName);

				TextView localGovtOfficeAddr = (TextView)findViewById(R.id.localGovtOfficeInfoText);
				localGovtOfficeAddr.setText(localGovtOfficeAddress);

				detailedLocalGovtOfficeUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedLocalGovtOfficeUrl += localGovtOfficeMapsResponse.results.get(0).reference;
				detailedLocalGovtOfficeUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";
				
				URL u;
				try {
					u = new URL(detailedLocalGovtOfficeUrl);
					HttpDetailedGetter get = new HttpDetailedGetter(type);
					get.execute(u);

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			
		}

	}

	private class HttpDetailedGetter extends AsyncTask<URL, Void, Void> {
		private Type type;
		
		private HttpDetailedGetter(Type t) {
			type = t;
		}

		@Override
		protected Void doInBackground(URL... params) {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpGet getRequest;
			HttpResponse response;

			if(type == Type.cityHall) {
				getRequest = new HttpGet(detailedCityHallUrl);
			} else if(type == Type.courthouse) {
				getRequest = new HttpGet(detailedCourthouseUrl);
			} else {
				getRequest = new HttpGet(detailedLocalGovtOfficeUrl);
			}

			
			try {
				response = client.execute(getRequest);
				InputStream stream = response.getEntity().getContent();

				// Use GSON to to convert the stream into Java objects
				Reader reader = new InputStreamReader(stream);

				//Log.d("Here", "About to parse JSON");
				// Create the MapsResponse class, Gson will handle all the hard work.
				Gson gson = new Gson();
				if(type == Type.cityHall) {
					detailedCityHallMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedCityHallMapsResponse == null ? "null response" : "cityHall response found");
				} else if(type == Type.courthouse) {
					detailedCourthouseMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedCourthouseMapsResponse == null ? "null response" : "courthouse response found");
				} else {
					detailedLocalGovtOfficeMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedLocalGovtOfficeMapsResponse == null ? "null response" : "localGovtOffice response found");
				}
				
				//	TextView thingy = (TextView) findViewById(R.id.thingy);
				//	thingy.setText(/*mapsResponse.results.toString()*/"HI MORGAN!!!!!");

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
	//		Log.d("execute: ", mapsResponse.toString());
			//Parse mapsResponse for text
			if(type == Type.cityHall) {
				String cityHallPhoneNumber = detailedCityHallMapsResponse.result.formatted_phone_number;


				TextView cityHallNumber = (TextView)findViewById(R.id.cityHallCallButton);
				cityHallNumber.setText(cityHallPhoneNumber);
			} else  if(type == Type.courthouse) {
				String courthousePhoneNumber = detailedCourthouseMapsResponse.result.formatted_phone_number;

				TextView courthouseNumber = (TextView)findViewById(R.id.courthouseCallButton);
				courthouseNumber.setText(courthousePhoneNumber);
			} else {
				String localGovtOfficePhoneNumber = detailedLocalGovtOfficeMapsResponse.result.formatted_phone_number;

				TextView localGovtOfficeNumber = (TextView)findViewById(R.id.localGovtOfficeCallButton);
				localGovtOfficeNumber.setText(localGovtOfficePhoneNumber);
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.government, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}
	
	public void callCityHall(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.cityHallCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}
	
	public void callCourthouse(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.courthouseCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}
	
	public void callLocalGovtOffice(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.localGovtOfficeCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}
	
	public void launchCityHallMaps(View view) {
		TextView address = (TextView) findViewById(R.id.cityHallInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}
	
	public void launchCourthouseMaps(View view) {
		TextView address = (TextView) findViewById(R.id.courthouseInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}
	
	public void launchLocalGovtOfficeMaps(View view) {
		TextView address = (TextView) findViewById(R.id.localGovtOfficeInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}

}
