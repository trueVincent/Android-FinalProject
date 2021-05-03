package com.example.finalproject;

public class Location {
    private int id;
    private double longitude;
    private double latitude;
    private String name;

    public Location(double longitude, double latitude, String name){
        this(0, longitude, latitude, name);
    }

    public Location(int id, double longitude, double latitude, String name){
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.name = name;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getId(){
        return id;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public String getName(){
        return name;
    }
}
