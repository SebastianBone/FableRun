package com.example.fablerun;

public class Animal {
	
	private String name, imgSource;
	private int speed;
	
	public Animal(String name, int speed, String imgSource){
		
		this.imgSource = imgSource;
		this.name = name;
		this.speed= speed;
		
	}

	public String getName() {
		return name;
	}

	public int getSpeed() {
		return speed;
	}

	public String getImgSource() {
		return imgSource;
	}


}
