package com.example.safetyapp;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class AlertsActivity extends Activity {

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager 
		= (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alerts);
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
		} else {
			WeatherResponse w = MainActivity.weatherResponse;

			if(w != null) {
				TextView t = (TextView)findViewById(R.id.alertsText);
				String alertString = "";
				List<Alert> alerts = w.alerts;

				for(int i = 0; i < alerts.size(); i++) {
					Alert a = alerts.get(i);
					alertString = alertString + (a.description.toUpperCase(Locale.US)) + "\n\n" + a.message + "\n\n";
				}

				//t.setMovementMethod(new ScrollingMovementMethod());
				t.setText(alertString);
			}
		}

	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alerts, menu);
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

}
