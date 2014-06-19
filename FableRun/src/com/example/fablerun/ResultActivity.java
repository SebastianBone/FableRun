package com.example.fablerun;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class ResultActivity extends Activity {
	
	//global Variables
	ArrayList<Animal>animals = new ArrayList<Animal>();
	Animal resultAnimal = null;
	private int yourSpeed;
	private TextView animalNameTextView;
	private Button butNewRun;
	private ImageView animalImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// window settings
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		
		// find views
		animalNameTextView = (TextView) findViewById(R.id.animalNameTextView);
		animalImageView = (ImageView) findViewById(R.id.animalImageView);
		butNewRun = (Button) findViewById(R.id.newRunButton);
		
		// get intent's extras
		Intent intent = new Intent();
		yourSpeed = (int)intent.getDoubleExtra(MainActivity.EXTRA_AVGSPEED, 0);
		
		// create Animals
		Animal schnecke = new Animal("Schnecke",1);
		Animal turtle = new Animal("Turtle",2);
		animals.add(schnecke);
		animals.add(turtle);
		
		// show result
		resultAnimal = findSlowerAnimal(yourSpeed);
		if(resultAnimal != null) {
			// show image of resultAnimal and it's name
			animalImageView.setImageResource(getImageId(this, resultAnimal.getName()));
			animalNameTextView.setText(resultAnimal.getName());
		} else {
			animalNameTextView.setText("Something went wrong...");
		}
		
		// new run button
		butNewRun.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	// show main activity
            	Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            	// open mainView
            	startActivity(intent);
            	// finish this activity
            	finish();
            }
        });
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

