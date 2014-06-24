package com.example.fablerun;

import android.content.Context;

public class Animal implements Comparable<Animal> {
	
	private String fileName;
	private String screenName;
	private int speed;
	
	public Animal(String fileName, String screenName, int speed){
		this.fileName = fileName;
		this.screenName = screenName;
		this.speed= speed;
	}
	
	@Override
	public int compareTo(Animal otherAnimal) {
		if(this.speed > otherAnimal.speed) {
			return 1;
		}
		else if(this.speed < otherAnimal.speed) {
			return -1;
		}
		else {
			return 0;
		}
	}

	public String getFileName() {
		return fileName;
	}

	public int getSpeed() {
		return speed;
	}
	
	public String getScreenName() {
		return screenName;
	}
}
