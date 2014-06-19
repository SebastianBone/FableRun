package com.example.fablerun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

public class StartActivity extends Activity {
	
	Button button;
	ImageView background;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// window settings
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
		
		// find views
		button = (Button)findViewById(R.id.button1);
		background = (ImageView)findViewById(R.id.imageView2);
		
		// button handler
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
			}
		});
	}
}
