package com.example.fablerun;

import java.util.concurrent.TimeUnit;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements GpsStatus.Listener {
	// Global variables
	Context context;
	private TextView lblLat, lblLong, lblTotalSpeed, lblAvgSpeed, lblTotalTime,lblDistance;
	private double actualLatitude, actualLongitude; 
	private double lastLatitude = 0.0;
	private double lastLongitude = 0.0;
	private float [] results = new float [0];
	private float finalDistance = 0;
	private float currentDistance;
	private float currentSpeed;
	private Button butStartPause, butStop;
	private boolean isRunning = false;
	private long startTime, stopTime;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private boolean isGPSFix = false;
	private Location mLastLocation = null;
	private long mLastLocationMillis = 0;
	private double avgSpeed;
	final static String EXTRA_AVGSPEED = "avgSpeed";
	// flag for GPS status
    private boolean isGPSEnabled = false;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
 	// The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// No Title and Fullscreen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Find elements
		lblLat = (TextView) findViewById(R.id.lblLat);
		lblLong = (TextView)findViewById(R.id.lblLong);
		lblTotalSpeed = (TextView)findViewById(R.id.lblTotalSpeed);
		butStartPause = (Button)findViewById(R.id.butStartPause);
		lblAvgSpeed = (TextView)findViewById(R.id.lblAvgSpeed);
		lblTotalTime = (TextView)findViewById(R.id.lblTotalTime);
		lblDistance = (TextView)findViewById(R.id.lblDistance);
		butStop = (Button)findViewById(R.id.butStop);
		
		// getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
		// Button StartPause
		butStartPause.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // Measurement not running yet
            	 if(!isRunning) {
            		 // GPS enabled?
            		 if(!isGPSEnabled) {
            			 buildAlertMessageNoGps();
            		 // GPS signal found?
            		 } else if(!isGPSFix) {
            			 Toast.makeText(getApplicationContext(), "No GPS signal found.", Toast.LENGTH_SHORT).show();
            		 } else {
	            		 isRunning = true;
	            		 butStartPause.setText("Pause");
	            		 butStop.setClickable(true);
	            		 startTime = System.currentTimeMillis();
            		 }
            	 // Measurement already running
            	 } else {
            		 isRunning = false;
            		 butStartPause.setText("Resume");
            		 stopTime = System.currentTimeMillis();
            		 lblAvgSpeed.setText("Avg Speed: ");
            		 lblTotalTime.setText("Total Time: " + TimeUnit.MILLISECONDS.toSeconds(stopTime - startTime) + " seconds");
            	 }
             }
         });
		
		
		// Button Stop
		butStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
            	// send data with the intent
            	intent.putExtra(EXTRA_AVGSPEED, avgSpeed);
            	// open resultView
            	startActivity(intent);
            	// finish this activity
            	finish();
            }
        });
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			
			@SuppressLint("NewApi") // needed for .toHours method calculating km/h
			@Override
			public void onLocationChanged(Location location) {
				// has GPS signal? If not stop here.
				if(location == null) return;
				// for isGPSFix
			    mLastLocationMillis = SystemClock.elapsedRealtime();
			    
			    // get current location
				actualLatitude = location.getLatitude();
				actualLongitude = location.getLongitude();
				if((lastLatitude == 0.0)&&(lastLongitude == 0.0)){
					lastLongitude = actualLongitude;
					lastLatitude = actualLatitude;	
				}
				
				// get travelled distance since last update
				try{
					Location.distanceBetween(actualLatitude, actualLongitude, lastLatitude, lastLongitude,results);
				} catch(IllegalArgumentException e){
					// ....
				}
				currentDistance = results[0];
				finalDistance += currentDistance;
				
				currentSpeed = location.getSpeed();
				
				// update labels
				lblLat.setText("Lat: " + actualLatitude);
				lblLong.setText("Long: " + actualLongitude);
				lblTotalSpeed.setText("Total Speed: " + currentSpeed);
				lblDistance.setText("Distance: " + currentDistance + " (" + finalDistance + ")");
				double finalKilometer = finalDistance * 0.001;
				double finalHours = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - startTime);
				avgSpeed = finalKilometer/finalHours;
				lblAvgSpeed.setText("Avg Speed: " + avgSpeed + " km/h");
				
				// save current location for next update
				lastLongitude = actualLongitude;
				lastLatitude = actualLatitude;
				
				// Show elapsed time since measurement started
				if(isRunning) {
					lblTotalTime.setText("Total Time: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime) + " seconds");
				}
				
				// for isGPSFix
				mLastLocation = location;
			}
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
			@Override
			public void onProviderEnabled(String provider) {}
			@Override
			public void onProviderDisabled(String provider) {}
		};
		// Check current location once per second
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
	}
	
	// Monitor if phone has GPS signal
	@Override
	public void onGpsStatusChanged(int event) {
		switch (event) {
	    	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	            if(mLastLocation != null) {
	                isGPSFix = (SystemClock.elapsedRealtime() - mLastLocationMillis) < 3000;
	            }
	            if(isGPSFix) { // A fix has been acquired.
	                Toast.makeText(getApplicationContext(), "GPS signal found. Happy running!", Toast.LENGTH_SHORT).show();
	            } else { // The fix has been lost.
	            	Toast.makeText(getApplicationContext(), "GPS Signal has been lost.", Toast.LENGTH_SHORT).show();
	            }
	        break;
	        
	    	case GpsStatus.GPS_EVENT_FIRST_FIX:
	    		isGPSFix = true;
	        break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onPause() {
	    super.onPause();
	    locationManager.removeUpdates(locationListener);
	}
	
	@Override
	protected void onDestroy() {
	    super.onPause();
	    locationManager.removeUpdates(locationListener);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1001, 0, locationListener);
	}
	
	// No GPS message
	private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
}