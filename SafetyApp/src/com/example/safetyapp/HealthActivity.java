package com.example.safetyapp;

//import android.R;
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

import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
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

public class HealthActivity extends Activity {

	enum Type {hospital, pharmacy, dentist, vet};
	double latitude = 0, longitude = 0;
	String latString, longString;
	String hospitalUrl = "";
	String detailedHospitalUrl = "";
	String pharmacyUrl = "";
	String detailedPharmacyUrl = "";
	String dentistUrl = "";
	String detailedDentistUrl = "";
	String vetUrl = "";
	String detailedVetUrl = "";
	MapsResponse hospitalMapsResponse;
	DetailedMapResponse detailedHospitalMapsResponse;
	MapsResponse pharmacyMapsResponse;
	DetailedMapResponse detailedPharmacyMapsResponse;
	MapsResponse dentistMapsResponse;
	DetailedMapResponse detailedDentistMapsResponse;
	MapsResponse vetMapsResponse;
	DetailedMapResponse detailedVetMapsResponse;

	
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_health);
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

		//url = url + "https://maps.googleapis.com/maps/api/place/textsearch/json?query=hospital&sensor=true&key=AIzaSyBq5yC-gVXwWV_Noqif-jFTet-JBYkPYXU&types=hospital&radius=15000&location=";
		hospitalUrl = hospitalUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=hospital&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=hospital&radius=15000&location=";
		hospitalUrl = hospitalUrl + latString + "," + longString;

		pharmacyUrl = pharmacyUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=pharmacy&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=pharmacy&radius=15000&location=";
		pharmacyUrl = pharmacyUrl + latString + "," + longString;
		
		dentistUrl = dentistUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=dentist&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=dentist&radius=15000&location=";
		dentistUrl = dentistUrl + latString + "," + longString;
		
		vetUrl = vetUrl + "https://maps.googleapis.com/maps/api/place/nearbysearch/json?query=veterinary&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM&types=veterinary_care&radius=15000&location=";
		vetUrl = vetUrl + latString + "," + longString;

		URL u1, u2, u3, u4;
		try {
			u1 = new URL(hospitalUrl);
			u2 = new URL(pharmacyUrl);
			u3 = new URL(dentistUrl);
			u4 = new URL(vetUrl);
			HttpGetter get1 = new HttpGetter(Type.hospital);
			HttpGetter get2 = new HttpGetter(Type.pharmacy);
			HttpGetter get3 = new HttpGetter(Type.dentist);
			HttpGetter get4 = new HttpGetter(Type.vet);
			get1.execute(u1);
			get2.execute(u2);
			get3.execute(u3);
			get4.execute(u4);

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

			if(type == Type.hospital)
				getRequest = new HttpGet(hospitalUrl);
			else if(type == Type.pharmacy)
				getRequest = new HttpGet(pharmacyUrl);
			else if(type == Type.dentist)
				getRequest = new HttpGet(dentistUrl);
			else
				getRequest = new HttpGet(vetUrl);
			// Execute the request and get an input stream of the response


			try {
				response = client.execute(getRequest);
				InputStream stream = response.getEntity().getContent();

				// Use GSON to to convert the stream into Java objects
				Reader reader = new InputStreamReader(stream);

				//Log.d("Here", "About to parse JSON");
				// Create the MapsResponse class, Gson will handle all the hard work.
				Gson gson = new Gson();
				if(type == Type.hospital) {
					hospitalMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", hospitalMapsResponse == null ? "null response" : "hospital response found");
				} else if(type == Type.pharmacy) {
					pharmacyMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", pharmacyMapsResponse == null ? "null response" : "pharmacy response found");
				} else if(type == Type.dentist) {
					dentistMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", dentistMapsResponse == null ? "null response" : "dentist response found");
				} else {
					vetMapsResponse = gson.fromJson(reader, MapsResponse.class);
					//Log.d("parse JSON ", vetMapsResponse == null ? "null response" : "vet response found");
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
			if(type == Type.hospital) {
				String hospitalName = hospitalMapsResponse.results.get(0).name;
				String hospitalAddress = hospitalMapsResponse.results.get(0).vicinity;
				
				TextView hospital = (TextView)findViewById(R.id.hospitalText);
				hospital.setText("Hospital: " + hospitalName);

				TextView hospitalAddr = (TextView)findViewById(R.id.hospitalInfoText);
				hospitalAddr.setText(hospitalAddress);

				detailedHospitalUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedHospitalUrl += hospitalMapsResponse.results.get(0).reference;
				detailedHospitalUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";
				
				URL u;
				try {
					u = new URL(detailedHospitalUrl);
					HttpDetailedGetter get = new HttpDetailedGetter(type);
					get.execute(u);

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(type == Type.pharmacy) {
				String pharmacyName = pharmacyMapsResponse.results.get(0).name;
				String pharmacyAddress = pharmacyMapsResponse.results.get(0).vicinity;
				
				TextView pharmacy = (TextView)findViewById(R.id.pharmacyText);
				pharmacy.setText("Pharmacy: " + pharmacyName);

				TextView pharmacyAddr = (TextView)findViewById(R.id.pharmacyInfoText);
				pharmacyAddr.setText(pharmacyAddress);

				detailedPharmacyUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedPharmacyUrl += pharmacyMapsResponse.results.get(0).reference;
				detailedPharmacyUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";
				
				URL u;
				try {
					u = new URL(detailedPharmacyUrl);
					HttpDetailedGetter get = new HttpDetailedGetter(type);
					get.execute(u);

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if(type == Type.dentist) {
				String dentistName = dentistMapsResponse.results.get(0).name;
				String dentistAddress = dentistMapsResponse.results.get(0).vicinity;
				
				TextView dentist = (TextView)findViewById(R.id.dentistText);
				dentist.setText("Dentist: " + dentistName);

				TextView dentistAddr = (TextView)findViewById(R.id.dentistInfoText);
				dentistAddr.setText(dentistAddress);

				detailedDentistUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedDentistUrl += dentistMapsResponse.results.get(0).reference;
				detailedDentistUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";
				
				URL u;
				try {
					u = new URL(detailedDentistUrl);
					HttpDetailedGetter get = new HttpDetailedGetter(type);
					get.execute(u);

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				String vetName = vetMapsResponse.results.get(0).name;
				String vetAddress = vetMapsResponse.results.get(0).vicinity;
				
				TextView vet = (TextView)findViewById(R.id.vetText);
				vet.setText("Veterinary Care: " + vetName);

				TextView vetAddr = (TextView)findViewById(R.id.vetInfoText);
				vetAddr.setText(vetAddress);

				detailedVetUrl = "https://maps.googleapis.com/maps/api/place/details/json?reference=";
				detailedVetUrl += vetMapsResponse.results.get(0).reference;
				detailedVetUrl += "&sensor=true&key=AIzaSyA02Lk1P_Jg6MTeaBgJvnP7DdnUaGwcsuM";
				
				URL u;
				try {
					u = new URL(detailedVetUrl);
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

			if(type == Type.hospital) {
				getRequest = new HttpGet(detailedHospitalUrl);
			} else if(type == Type.pharmacy) {
				getRequest = new HttpGet(detailedPharmacyUrl);
			} else if(type == Type.dentist) {
				getRequest = new HttpGet(detailedDentistUrl);
			} else {
				getRequest = new HttpGet(detailedVetUrl);
			}

			
			try {
				response = client.execute(getRequest);
				InputStream stream = response.getEntity().getContent();

				// Use GSON to to convert the stream into Java objects
				Reader reader = new InputStreamReader(stream);

				//Log.d("Here", "About to parse JSON");
				// Create the MapsResponse class, Gson will handle all the hard work.
				Gson gson = new Gson();
				if(type == Type.hospital) {
					detailedHospitalMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedHospitalMapsResponse == null ? "null response" : "hospital response found");
				} else if(type == Type.pharmacy) {
					detailedPharmacyMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedPharmacyMapsResponse == null ? "null response" : "pharmacy response found");
				} else if(type == Type.dentist) {
					detailedDentistMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedDentistMapsResponse == null ? "null response" : "dentist response found");
				} else {
					detailedVetMapsResponse = gson.fromJson(reader, DetailedMapResponse.class);
					//Log.d("parse JSON ", detailedVetMapsResponse == null ? "null response" : "vet response found");
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
			if(type == Type.hospital) {
				String hospitalPhoneNumber = detailedHospitalMapsResponse.result.formatted_phone_number;


				TextView hospitalNumber = (TextView)findViewById(R.id.hospitalCallButton);
				hospitalNumber.setText(hospitalPhoneNumber);
			} else  if(type == Type.pharmacy) {
				String pharmacyPhoneNumber = detailedPharmacyMapsResponse.result.formatted_phone_number;

				TextView pharmacyNumber = (TextView)findViewById(R.id.pharmacyCallButton);
				pharmacyNumber.setText(pharmacyPhoneNumber);
			} else if(type == Type.dentist) {
				String dentistPhoneNumber = detailedDentistMapsResponse.result.formatted_phone_number;

				TextView dentistNumber = (TextView)findViewById(R.id.dentistCallButton);
				dentistNumber.setText(dentistPhoneNumber);
			} else {
				String vetPhoneNumber = detailedVetMapsResponse.result.formatted_phone_number;

				TextView vetNumber = (TextView)findViewById(R.id.vetCallButton);
				vetNumber.setText(vetPhoneNumber);
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

	public void callHospital(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.hospitalCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}
	
	public void callPharmacy(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.pharmacyCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}
	
	public void callDentist(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.dentistCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}
	
	public void callVet(View view) {
		// Do something in response to button
		Button num = (Button) findViewById(R.id.vetCallButton);
		String number = "tel:" + num.getText().toString().trim();

		Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number)); 

		startActivity(callIntent);
	}
	
	public void launchHospitalMaps(View view) {
		TextView address = (TextView) findViewById(R.id.hospitalInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}
	
	public void launchPharmacyMaps(View view) {
		TextView address = (TextView) findViewById(R.id.pharmacyInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}
	
	public void launchDentistMaps(View view) {
		TextView address = (TextView) findViewById(R.id.dentistInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}
	
	public void launchVetMaps(View view) {
		TextView address = (TextView) findViewById(R.id.vetInfoText);
		
		String url = "http://maps.google.com/maps?saddr="+latString+","+longString+"&daddr="+address.getText().toString();
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
		intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
		startActivity(intent);
	}

}
