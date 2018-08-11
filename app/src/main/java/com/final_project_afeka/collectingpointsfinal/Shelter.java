package com.final_project_afeka.collectingpointsfinal;


public class Shelter {

    private String uId;
    private double longitude;
    private double lalatitudet;
    private String address;
    private int isApproved;
    private String email;
    /**
     *  isApproved logic:
     *  1 = approved
     *  0 = waiting approval
     *  -1 = declined
     */

    public Shelter() {
    }

    public Shelter(String uId, double longitude, double lalatitudet, String address, int isApproved , String email) {
        this.uId = uId;
        this.email = email;
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

    public void setApproved(int approved) {
        isApproved = approved;
    }

    public void setEmail(String email) { this.email = email; }

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

    public String getEmail() {
        return email;
    }

    public int isApproved() {
        return isApproved;
    }
}
