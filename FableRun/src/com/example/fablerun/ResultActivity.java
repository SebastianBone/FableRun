package com.example.fablerun;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ResultActivity extends Activity {
	
	//global Variables
	ArrayList<Animal>animals = new ArrayList<Animal>();
	Animal resultAnimal = null;
	private int yourSpeed = 0;
	private TextView animalNameTextView;
	private Button newRunButton;
	private ImageView animalImageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		
		animalNameTextView = (TextView) findViewById(R.id.animalNameTextView);
		animalImageView = (ImageView) findViewById(R.id.animalImageView);
		newRunButton = (Button) findViewById(R.id.newRunButton);
		
		
		//Create Animals
		Animal schnecke = new Animal("Schnecke",1,"...");
		Animal turtle = new Animal("Turtle",2,"...");
		animals.add(schnecke);
		animals.add(turtle);
		
		// show result
		resultAnimal = findSlowerAnimal(yourSpeed);
		if(resultAnimal != null) {
			/*
			 *  BILD ANZEIGEN
			 *  NAME UND GESCHWINDIGKEIT AUSGEBEN
			 */
			//animalImageView.setImageResource(R.drawable.);
			animalNameTextView.setText(resultAnimal.getName());
			
		} else {
			Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
		}
		
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

