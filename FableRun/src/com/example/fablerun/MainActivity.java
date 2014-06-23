package com.example.fablerun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements GpsStatus.Listener {
	// Global variables
	Context context;
	private TextView lblAvgSpeed, lblTotalTime, lblDistance, lblHelpText, lblResultText;
	private ImageButton iconButton;
	private ImageView bannerView, anglesUpView;
	private double actualLatitude, actualLongitude; 
	private double lastLatitude = 0.0;
	private double lastLongitude = 0.0;
	private float [] results = new float [0];
	private float finalDistance = 0;
	private float currentDistance;
	private Button butStartPause, butStop;
	private boolean isRunning = false;
	private long startTime = 0;
	private long totalTime;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private boolean isGPSFix = false;
	private Location mLastLocation = null;
	private long mLastLocationMillis = 0;
	private double avgSpeedInKmh;
	// flag that tells the iconButton if it shall reset for a
	// 	2nd run or initialize the UI for a first run
	private boolean repeat = false;
	// flag that shows pause status
	private boolean paused = false;
	// for resultView
	ArrayList<Animal>animals = new ArrayList<Animal>();
	Animal resultAnimal = null;
	// flag for GPS status
    private boolean isGPSEnabled = false;
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
 	// The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// no title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// find views
		butStartPause = (Button)findViewById(R.id.butStartPause);
		butStop = (Button)findViewById(R.id.butStop);
		lblTotalTime = (TextView)findViewById(R.id.lblTotalTime);
		lblAvgSpeed = (TextView)findViewById(R.id.lblAvgSpeed);
		iconButton = (ImageButton)findViewById(R.id.iconButton);
		bannerView = (ImageView)findViewById(R.id.bannerView);
		anglesUpView = (ImageView)findViewById(R.id.anglesUpView);
		lblHelpText = (TextView)findViewById(R.id.lblHelpText);
		lblResultText = (TextView)findViewById(R.id.lblResultText);
		
		// initialize animations
		final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
		final Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		
		// create animals (will be sorted automatically; parameters: "fileName", "screenName", speedInKmh)
		Animal baer = new Animal("baer", "Bär", 30);
		animals.add(baer);
		Animal eichhoernchen = new Animal("eichhoernchen", "Eichhörnchen", 24);
		animals.add(eichhoernchen);
		Animal elefant = new Animal("elefant", "Elefant", 40);
		animals.add(elefant);
		Animal fliege = new Animal("fliege", "Fliege", 6);
		animals.add(fliege);
		Animal hase = new Animal("hase", "Kaninchen", 34);
		animals.add(hase);
		Animal huhn = new Animal("huhn", "Huhn", 15);
		animals.add(huhn);
		Animal hund = new Animal("hund", "Hund", 36);
		animals.add(hund);
		Animal katze = new Animal("katze", "Katze", 32);
		animals.add(katze);
		Animal schildkroete = new Animal("schildkroete", "Schildkröte", 2);
		animals.add(schildkroete);
		Animal schlange = new Animal("schlange", "Schlange", 21);
		animals.add(schlange);
		Animal schnecke = new Animal("schnecke", "Schnecke", 1);
		animals.add(schnecke);
		Animal schwein = new Animal("schwein", "Schwein", 18);
		animals.add(schwein);
		Animal spinne = new Animal("spinne", "Spinne", 3);
		animals.add(spinne);
		Animal stein = new Animal("stein", "Stein", 0);
		animals.add(stein);
		Animal maus = new Animal("maus", "Maus", 12);
		animals.add(maus);
		Animal meerschweinchen = new Animal("meerschweinchen", "Meerschweinchen", 9);
		animals.add(meerschweinchen);
		Animal eidechse = new Animal("eidechse", "Eidechse", 27);
		animals.add(eidechse);
		// sort by speed in ascending order using the comparable interface
		Collections.sort(animals);
		
		// hide several ui elements for first view
		bannerView.setAlpha(0);
		butStop.setAlpha(0);
		butStartPause.setAlpha(0);
		lblDistance.setAlpha(0);
		lblAvgSpeed.setAlpha(0);
		lblTotalTime.setAlpha(0);
		lblResultText.setAlpha(0);
		
		// getting GPS status
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		
        // iconButton
        iconButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// first click just shows the ui
				if(!repeat) {
					bannerView.startAnimation(animationFadeIn);
					butStartPause.startAnimation(animationFadeIn);
					butStop.startAnimation(animationFadeIn);
					
					anglesUpView.startAnimation(animationFadeOut);
					lblHelpText.startAnimation(animationFadeOut);
					
					iconButton.setClickable(false);
				// further clicks reset the app for a new run
				} else {
					// reset values
					finalDistance = 0;
					isRunning = false;
					avgSpeedInKmh = 0;
					startTime = 0;
					totalTime = 0;
					paused = false;
					// reset ui
					lblResultText.startAnimation(animationFadeOut);
					
					butStartPause.startAnimation(animationFadeIn);
					butStop.startAnimation(animationFadeIn);
					
					iconButton.setImageResource(R.drawable.logo_raw);
				}
			}
		});
        
		// button StartPause
		butStartPause.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // measurement not running yet
            	 if(!isRunning) {
            		 // GPS enabled?
            		 if(!isGPSEnabled) {
            			 buildAlertMessageNoGps();
            		 // GPS signal found?
            		 } else if(!isGPSFix) {
            			 Toast.makeText(getApplicationContext(), R.string.no_gps_text, Toast.LENGTH_SHORT).show();
            		 } else {
            			 isRunning = true;
	            		 butStartPause.setText(R.string.pause_button_text);
	            		 butStop.setClickable(true);
	            		 // resume from paused mode
	            		 if(paused) {
	            			 paused = false;
	            			 // calculate total time
	            			 totalTime += System.currentTimeMillis() - startTime;
	            			 startTime = System.currentTimeMillis();
	            		 // start a new run
            			 } else {
            				startTime = System.currentTimeMillis();
            				lblDistance.startAnimation(animationFadeIn);
							lblAvgSpeed.startAnimation(animationFadeIn);
							lblTotalTime.startAnimation(animationFadeIn);
            			 }
            		 }
            	 // measurement already running
            	 } else {
            		 isRunning = false;
            		 paused = true;
            		 butStartPause.setText(R.string.resume_button_text);
            		 iconButton.setImageResource(R.drawable.pause);
            	 }
             }
         });
		
		
		// Button Stop
		butStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	isRunning = false;
            	
            	// show result
        		resultAnimal = findSlowerAnimal((int)avgSpeedInKmh);
        		if(resultAnimal != null) {
        			// show image of resultAnimal and it's name
        			iconButton.setImageResource(getImageId(context, resultAnimal.getFileName()));
        			lblResultText.setText(R.string.result_text + resultAnimal.getScreenName() + "!");
        		} else {
        			lblResultText.setText(R.string.error_text);
        		}
        		// fade views
        		lblResultText.startAnimation(animationFadeIn);
            	butStartPause.startAnimation(animationFadeOut);
            	butStop.startAnimation(animationFadeOut);
            	
            	// activate middle icon to act as a restart button
            	repeat = true;
            	iconButton.setClickable(true);
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
			    
			    // things to do only when measurement is running
				if(isRunning) {
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
					
					// calculate total time
	            	totalTime += System.currentTimeMillis() - startTime;
	            	startTime = System.currentTimeMillis();
					
					// update labels
					double finalKilometer = finalDistance * 0.001;
					double finalHours = TimeUnit.MILLISECONDS.toHours(totalTime);
					avgSpeedInKmh = finalKilometer/finalHours;
					lblAvgSpeed.setText("Ø km/h " + avgSpeedInKmh);
					lblDistance.setText(finalDistance + "m");
					lblTotalTime.setText(TimeUnit.MILLISECONDS.toSeconds(totalTime)/60 + "m " + TimeUnit.MILLISECONDS.toSeconds(totalTime)%60 + "s");
					
					// update iconButton with the correct animal
	        		resultAnimal = findSlowerAnimal((int)avgSpeedInKmh);
        			iconButton.setImageResource(getImageId(context, resultAnimal.getFileName()));
					
					// save current location for next update
					lastLongitude = actualLongitude;
					lastLatitude = actualLatitude;
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
	                Toast.makeText(getApplicationContext(), R.string.gps_found_text, Toast.LENGTH_SHORT).show();
	            } else { // The fix has been lost.
	            	Toast.makeText(getApplicationContext(), R.string.gps_lost_text, Toast.LENGTH_SHORT).show();
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
	    builder.setMessage(R.string.enable_gps_dialog)
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
	
	// construct source paths of images
	public static int getImageId(Context context, String imageName) {
	    return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
	}
	
	// find the one animal that is slightly slower than you
	public Animal findSlowerAnimal(int yourSpeed){
		for (int i = 0; i < animals.size(); i++){
			if(animals.get(i).getSpeed() < yourSpeed){
				resultAnimal = animals.get(i);
			} else{
				return resultAnimal;
			}
		}
		return null;
	}
}