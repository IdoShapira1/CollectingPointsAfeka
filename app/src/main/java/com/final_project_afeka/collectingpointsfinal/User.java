package com.final_project_afeka.collectingpointsfinal;

/**
 * Created by idoshapira-mbp on 08/06/2018.
 */

public class User {
    private int isAdmin;
    private int pointsCollected;
    private String email;
    private int pointsApproved;
    private int pointsDeclined;

    public User(){
    }
    public User(int isAdmin,int pointsCollected,String email,int pointsApproved,int pointsDeclined){
        this.isAdmin = isAdmin;
        this.pointsCollected = pointsCollected;
        this.email = email;
        this.pointsApproved = pointsApproved;
        this.pointsDeclined = pointsDeclined;
    }

    public void setIsAdmin(int isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setPointsCollected(int pointsCollected) {
        this.pointsCollected = pointsCollected;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPointsApproved(int pointsApproved) {
        this.pointsApproved = pointsApproved;
    }

    public void setPointsDeclined(int pointsDeclined) {
        this.pointsDeclined = pointsDeclined;
    }

    public int getIsAdmin() {
        return isAdmin;
    }

    public int getPointsCollected() {
        return pointsCollected;
    }

    public String getEmail() {
        return email;
    }

    public int getPointsApproved() {
        return pointsApproved;
    }

    public int getPointsDeclined() {
        return pointsDeclined;
    }




}
