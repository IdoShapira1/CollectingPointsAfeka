package com.final_project_afeka.collectingpointsfinal;


public class Shelter {

    private String uId;
    private double longitude;
    private double lalatitudet;
    private String address;
    private boolean isApproved;

    public Shelter() {
    }

    public Shelter(String uId, double longitude, double lalatitudet, String address, boolean isApproved) {
        this.uId = uId;
        this.longitude = longitude;
        this.lalatitudet = lalatitudet;
        this.address = address;
        this.isApproved = isApproved;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLalatitudet(double lalatitudet) {
        this.lalatitudet = lalatitudet;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public String getuId() {
        return uId;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLalatitudet() {
        return lalatitudet;
    }

    public String getAddress() {
        return address;
    }

    public boolean isApproved() {
        return isApproved;
    }
}
