package com.final_project_afeka.collectingpointsfinal;

public class SafePoint {

    private String email;
    private int id;
    private double latitude;
    private double longitude;
    private int approved;
    private String address;

    public SafePoint(){

    }

    public SafePoint(int id, String email, double latitude, double longitude, int approved, String address) {
        this.id = id;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.approved = approved;
        this.address = address;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }




    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getApproved() {
        return approved;
    }

    public void setApproved(int approved) {
        this.approved = approved;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
