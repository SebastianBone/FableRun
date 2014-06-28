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
import android.os.Handler;
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

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends Activity implements GpsStatus.Listener {
	// global variables
	Context context;
	private TextView lblAvgSpeed, lblTotalTime, lblDistance, lblHelpText, lblResultText;
	private ImageButton iconButton;
	private ImageView bannerView, anglesUpView;
	private float finalDistance = 0;
	private float currentDistance;
	private Location locationNow = new Location("actual");
	private Location locationBefore = new Location("last");
	private Button butStartPause, butStop;
	private boolean isRunning = false;
	//new timer variables
	private long startTime = 0L;
	private Handler customHandler = new Handler();
	long timeInMilliseconds = 0L;
	long timeSwapBuff = 0L;
	long updatedTime = 0L;
	//stop timer thread flag
	private volatile boolean stopThread = false;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private GpsStatus mStatus;
	private boolean isGPSFix = false;
	private Location mLastLocation = null;
	private long mLastLocationMillis = 0;
	private float avgSpeedInKmh;
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
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2; // 0 meters
 	// The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000; // 1 second
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
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
		lblDistance = (TextView)findViewById(R.id.lblDistance);
		
		// initialize animations
		final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
		final Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
		
		// create animals (will be sorted automatically; parameters: "fileName", "screenName", speedInKmh)
		Animal baer = new Animal("baer", "ein Bär", 30);
		animals.add(baer);
		Animal eichhoernchen = new Animal("eichhoernchen", "ein Eichhörnchen", 24);
		animals.add(eichhoernchen);
		Animal elefant = new Animal("elefant", "ein Elefant", 40);
		animals.add(elefant);
		Animal fliege = new Animal("fliege", "eine Fliege", 6);
		animals.add(fliege);
		Animal hase = new Animal("hase", "ein Kaninchen", 34);
		animals.add(hase);
		Animal huhn = new Animal("huhn", "ein Huhn", 15);
		animals.add(huhn);
		Animal hund = new Animal("hund", "ein Hund", 36);
		animals.add(hund);
		Animal katze = new Animal("katze", "eine Katze", 32);
		animals.add(katze);
		Animal schildkroete = new Animal("schildkroete", "eine Schildkröte", 2);
		animals.add(schildkroete);
		Animal schlange = new Animal("schlange", "eine Schlange", 21);
		animals.add(schlange);
		Animal schnecke = new Animal("schnecke", "eine Schnecke", 1);
		animals.add(schnecke);
		Animal schwein = new Animal("schwein", "ein Schwein", 18);
		animals.add(schwein);
		Animal spinne = new Animal("spinne", "eine Spinne", 3);
		animals.add(spinne);
		Animal stein = new Animal("stein", "ein Stein", 0);
		animals.add(stein);
		Animal maus = new Animal("maus", "eine Maus", 12);
		animals.add(maus);
		Animal meerschweinchen = new Animal("meerschweinchen", "ein Meerschweinchen", 9);
		animals.add(meerschweinchen);
		Animal eidechse = new Animal("eidechse", "eine Eidechse", 27);
		animals.add(eidechse);
		// sort by speed in ascending order using the comparable interface
		Collections.sort(animals);
		
		// hide several ui elements for first view
		bannerView.setImageAlpha(0);
		butStop.setAlpha(0);
		butStop.setClickable(false);
		butStartPause.setAlpha(0);
		butStartPause.setClickable(false);
		lblDistance.setAlpha(0);
		lblAvgSpeed.setAlpha(0);
		lblTotalTime.setAlpha(0);
		lblResultText.setAlpha(0);
		
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.addGpsStatusListener(this);
		
        // iconButton
        iconButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// first click just shows the ui
				if(!repeat) {
					bannerView.startAnimation(animationFadeIn);
					bannerView.setImageAlpha(255);
					
					butStartPause.startAnimation(animationFadeIn);
					butStartPause.setAlpha(255);
					butStartPause.setClickable(true);
					
					butStop.startAnimation(animationFadeIn);
					butStop.setAlpha(255);
					butStop.setClickable(true);
					
					anglesUpView.startAnimation(animationFadeOut);
					anglesUpView.setImageAlpha(0);
					
					lblHelpText.startAnimation(animationFadeOut);
					lblHelpText.setAlpha(0);
					
					iconButton.setClickable(false);
				// further clicks reset the app for a new run
				} else {
					// reset values
					finalDistance = 0;
					isRunning = false;
					avgSpeedInKmh = 0;
					paused = false;
					updatedTime = 0L;
					timeSwapBuff = 0L;
					stopThread = false;
					
					// reset ui
					butStartPause.setText(R.string.start_button_text);
					
					lblResultText.startAnimation(animationFadeOut);
					lblResultText.setAlpha(0);
					
					butStartPause.startAnimation(animationFadeIn);
					butStartPause.setAlpha(255);
					butStartPause.setClickable(true);
					
					butStop.startAnimation(animationFadeIn);
					butStop.setAlpha(255);
					butStop.setClickable(true);
					
					iconButton.setImageResource(R.drawable.logo_raw);
					iconButton.setClickable(false);
					
					lblTotalTime.setText("0:00");
					lblAvgSpeed.setText("Ø 0 km/h");
					lblDistance.setText("0m");
				}
			}
		});
        
		// button StartPause
		butStartPause.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 // measurement not running yet (will be started or resumed)
            	 if(!isRunning) {
            		 // update if gps is enabled
            		 isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            		 
            		 // GPS enabled?
            		 if(!isGPSEnabled) {
            			 buildAlertMessageNoGps();
            			 
            		 // GPS signal found?
            		 } else if(!isGPSFix) {
            			 Toast.makeText(getApplicationContext(), R.string.no_gps_text, Toast.LENGTH_SHORT).show();
            		 
            		 // everything ready to go
            		 } else {
            			 isRunning = true;
	            		 butStartPause.setText(R.string.pause_button_text);
	            		 butStop.setClickable(true);
	            		 
	            		 //new Timer start
	            		 startTime = SystemClock.uptimeMillis();
	            		 customHandler.post(updateTimerThread);

	            		 // resume from paused mode
	            		 if(paused) {
	            			 paused = false;
	            			 // show logo to avoid pause icon being shown when not moving
	            			 iconButton.setImageResource(R.drawable.logo_raw);
	            		 // start a new run
            			 } else {
            				lblDistance.startAnimation(animationFadeIn);
            				lblDistance.setAlpha(255);
            				
							lblAvgSpeed.startAnimation(animationFadeIn);
							lblAvgSpeed.setAlpha(255);
							
							lblTotalTime.startAnimation(animationFadeIn);
							lblTotalTime.setAlpha(255);
            			 }
            		 }
            	 // measurement already running (will be paused)
            	 } else {
            		 isRunning = false;
            		 paused = true;
            		 butStop.setClickable(false);
            		 butStartPause.setText(R.string.resume_button_text);
            		 iconButton.setImageResource(R.drawable.pause);
            		
            		 // new paused Time
            		 timeSwapBuff += timeInMilliseconds;
            		 customHandler.removeCallbacks(updateTimerThread);
            	 }
             }
         });
		
		
		// Button Stop
		butStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	isRunning = false;
            	
            	//Timer stop
            	stopThread = true;
            	customHandler.removeCallbacks(updateTimerThread);
            	
            	// show result
        		resultAnimal = findSlowerAnimal((int)avgSpeedInKmh);
        		if(resultAnimal != null) {
        			// show image of resultAnimal and it's name
        			int identifier = getResources().getIdentifier(resultAnimal.getFileName(), "drawable", "com.example.fablerun");
        			iconButton.setImageResource(identifier);
        			lblResultText.setText("Du warst schon so schnell wie " + resultAnimal.getScreenName() + "!");
        		} else {
        			lblResultText.setText(R.string.error_text);
        		}
        		
        		// fade views
        		lblResultText.startAnimation(animationFadeIn);
        		lblResultText.setAlpha(255);
        		
            	butStartPause.startAnimation(animationFadeOut);
            	butStartPause.setAlpha(0);
            	butStartPause.setClickable(false);
            	
            	butStop.startAnimation(animationFadeOut);
            	butStop.setAlpha(0);
            	butStop.setClickable(false);
            	
            	// activate middle icon to act as a restart button
            	repeat = true;
            	iconButton.setClickable(true);
            }
        });
		
		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			
			@SuppressLint("NewApi") // needed for .toHours method calculating km/h
			@Override
			public void onLocationChanged(Location location) {
				// has GPS signal? If not stop here.
				if(location == null) return;
				// for isGPSFix
			    mLastLocationMillis = System.currentTimeMillis();
			    
			    // things to do only when measurement is running
				if(isRunning) {
				    // get current location
					locationNow.setLatitude(location.getLatitude());
					locationNow.setLongitude(location.getLongitude());
					
					// happens on first time usage only
					if((locationBefore.getLatitude() == 0)&&(locationBefore.getLongitude() == 0)){
						locationBefore.setLatitude(location.getLatitude());
						locationBefore.setLongitude(location.getLongitude());
					}
					
					// calculate total distance
					currentDistance = locationNow.distanceTo(locationBefore);
					finalDistance += currentDistance;
	            	
					finalDistance = Math.round(finalDistance * 100f)/100f; 
					// update labels
					float finalKilometer = finalDistance * 0.001f;
					float finalHours = TimeUnit.MILLISECONDS.toHours(updatedTime);
					avgSpeedInKmh = finalKilometer/finalHours;
					int avgSpeedInKmh2 = Math.round(avgSpeedInKmh);
					lblAvgSpeed.setText("Ø " + avgSpeedInKmh2 + " km/h");
					lblDistance.setText(finalDistance + "m");
					
					// DEBUGGING
					Toast.makeText(getApplicationContext(),
							"km: " + finalKilometer +
							"h: " + finalHours +
							"kmhF: " + avgSpeedInKmh2 +
							"kmhI: " + avgSpeedInKmh,
							Toast.LENGTH_LONG).show();
					
					// update iconButton with the correct animal
	        		resultAnimal = findSlowerAnimal((int)avgSpeedInKmh2);
	        		if(resultAnimal != null) {
	        			//iconButton.setImageResource(getImageId(context, resultAnimal.getFileName()));
	        			int identifier = getResources().getIdentifier(resultAnimal.getFileName(), "drawable", "com.example.fablerun");
	        			iconButton.setImageResource(identifier);
	        		} else {
	        			Toast.makeText(getApplicationContext(), "resultAnimal ist null", Toast.LENGTH_SHORT).show();
	        		}
					
					// save current location for next update
					locationBefore.setLatitude(location.getLatitude());
					locationBefore.setLongitude(location.getLongitude());
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
	
	//Timer clock-Thread
	private Runnable updateTimerThread = new Runnable(){
		public void run(){
			if(!stopThread) {
				timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
				
				updatedTime = timeSwapBuff + timeInMilliseconds;
				
				int secs = (int) (updatedTime / 1000);
				int mins = secs / 60;
				secs = secs % 60;
				//int milliseconds = (int) (updatedTime % 1000);
				lblTotalTime.setText(""+mins+":"
									+String.format("%02d", secs));
				customHandler.postDelayed(this, 0);
			}
		}
	};
	
	
	// Monitor if phone has GPS signal
	@Override
	public void onGpsStatusChanged(int event) {
		mStatus = locationManager.getGpsStatus(mStatus);
		switch (event) {
	    	case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
	            if(mLastLocation != null) {
	                isGPSFix = (System.currentTimeMillis() - mLastLocationMillis) < 3000;
	            }
	        break;
	        
	    	case GpsStatus.GPS_EVENT_FIRST_FIX:
	    		Toast.makeText(getApplicationContext(), R.string.gps_found_text, Toast.LENGTH_SHORT).show();
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
	    // stop using gps only if no measurement is running
	    if(!isRunning) {
	    	locationManager.removeUpdates(locationListener);
	    // if paused while a measurement is running
	    } else {
	    	/*
	    	 *  TO DO: MAKE "STILL RUNNING" NOTIFICATION !
	    	 */
	    }
	}
	
	@Override
	protected void onDestroy() {
	    super.onPause();
	    locationManager.removeUpdates(locationListener);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		// only needs to reactivate usage of gps if it has been deactivated before (see onPause() )
		if(!isRunning) {
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
		}
	}
	
	// No GPS message
	private void buildAlertMessageNoGps() {
	    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage(R.string.enable_gps_dialog)
	           .setCancelable(false)
	           .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                   startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    final AlertDialog alert = builder.create();
	    alert.show();
	}
	
	// find the one animal that is slightly slower than you
	public Animal findSlowerAnimal(int yourSpeed) {
		for(int i = 0; i < animals.size(); i++) {
			if(animals.get(i).getSpeed() <= yourSpeed){
				resultAnimal = animals.get(i);
			} else{
				return resultAnimal;
			}
		}
		return null;
	}
}