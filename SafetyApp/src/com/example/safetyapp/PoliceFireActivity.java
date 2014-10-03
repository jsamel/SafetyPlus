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

import com.google.gson.Gson;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PoliceFireActivity extends Activity {	

	enum Type {police, fire};
	double latitude = 0, longitude = 0;
	String latString, longString;
	String policeUrl = "";
	String detailedPoliceUrl = "";
	String fireUrl = "";
	String detailedFireUrl = "";
	MapsResponse policeMapsResponse;
	DetailedMapResponse detailedPoliceMapsResponse;
	MapsResponse fireMapsResponse;
	DetailedMapResponse detailedFireMapsResponse;

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_police_fire);
		// Show the Up button in the action bar.
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

		//url = url + "https://maps.googleapis.com/maps/api/place/textsearch/json?query=police&sensor=true&key=AIzaSyBq5yC-gVXwWV_Noqif-jFTet-JBYkPYXU&types=police&radius=15000&location=";
		policeUrl = policeUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=police&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=police&radius=15000&location=";
		policeUrl = policeUrl + latString + "," + longString;

		fireUrl = fireUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=fire&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=fire_station&radius=15000&location=";
		fireUrl = fireUrl + latString + "," + longString;

		URL u1, u2;
		try {
			u1 = new URL(policeUrl);
			u2 = new URL(fireUrl);
			HttpGetter get1 = new HttpGetter(Type.police);
			HttpGetter get2 = new HttpGetter(Type.fire);
			get1.execute(u1);
			get2.execute(u2);

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

			if(type == Type.police) {
				getRequest = new HttpGet(policeUrl);
			} else
				getRequest = new HttpGet(fireUrl);
			// Execute the request and get an input stream of the response


			try {
				response = client.execute(getRequest);
				InputStream stream = response.getEntity().getContent();

				// Use GSON to to convert the stream into Java objects
				Reader reader = new InputStreamReader(stream);

				//Log.d("Here", "About to parse JSON");
				// Create the MapsResponse class, Gson will handle all the hard work.
				Gson gson = new Gson();
				if(type == Type.police) {
					policeMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", policeMapsResponse == null ? "null response" : "police response found");
				} else {
					fireMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", fireMapsResponse == null ? "null response" : "fire response found");
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
			if(type == Type.police) {
				String policeName = policeMapsResponse.results.get(0).name;
				String policeAddress = policeMapsResponse.results.get(0).vicinity;

				TextView police = (TextView)findViewById(R.id.policeText);
				police.setText("Police Station: " + policeName);

				TextView policeAddr = (TextView)findViewById(R.id.policeInfoText);
				policeAddr.setText(policeAddress);

				detailedPoliceUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedPoliceUrl += policeMapsResponse.results.get(0).reference;
				detailedPoliceUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";

				URL u;
				try {
					u = new URL(detailedPoliceUrl);
					HttpDetailedGetter get = new HttpDetailedGetter(type);
					get.execute(u);

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String fireName = fireMapsResponse.results.get(0).name;
				String fireAddress = fireMapsResponse.results.get(0).vicinity;

				TextView fire = (TextView)findViewById(R.id.fireText);
				fire.setText("Fire Station: " + fireName);

				TextView fireAddr = (TextView)findViewById(R.id.fireInfoText);
				fireAddr.setText(fireAddress);

				detailedFireUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedFireUrl += fireMapsResponse.results.get(0).reference;
				detailedFireUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";

				URL u;
				try {
					u = new URL(detailedFireUrl);
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

			if(type == Type.police) {
				getRequest = new HttpGet(detailedPoliceUrl);
			} else {
				getRequest = new HttpGet(detailedFireUrl);
			}


			try {
				response = client.execute(getRequest);
				InputStream stream = response.getEntity().getContent();

				// Use GSON to to convert the stream into Java objects
				Reader reader = new InputStreamReader(stream);

				//Log.d("Here", "About to parse JSON");
				// Create the MapsResponse class, Gson will handle all the hard work.
				Gson gson = new Gson();
				if(type == Type.police) {
					detailedPoliceMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedPoliceMapsResponse == null ? "null response" : "police response found");
				} else {
					detailedFireMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedFireMapsResponse == null ? "null response" : "fire response found");
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
			if(type == Type.police) {
				String policePhoneNumber = detailedPoliceMapsResponse.result.formatted_phone_number;


				TextView policeNumber = (TextView)findViewById(R.id.policeCallButton);
				policeNumber.setText(policePhoneNumber);
			} else {
				String firePhoneNumber = detailedFireMapsResponse.result.formatted_phone_number;


				TextView policeNumber = (TextView)findViewById(R.id.fireCallButton);
				policeNumber.setText(firePhoneNumber);
			}

		}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.police_fire, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void callPolice(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.policeCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}

	public void callFire(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.fireCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}
	
	public void launchPoliceMaps(View view) {
		TextView address = (TextView) findViewById(R.id.policeInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}
	
	public void launchFireMaps(View view) {
		TextView address = (TextView) findViewById(R.id.fireInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}


}
