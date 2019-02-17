package com.example.brunovsiq.mapchat.models;

public class User {

    private String username;
    private double latitude;
    private double longitude;

    private static User userObj;


    public static User getInstance () {
        if (userObj == null) {
            userObj = new User();
        }
        return userObj;
    }

    private User() {
        //request user infos

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public static User getUserObj() {
        return userObj;
    }

    public static void setUserObj(User userObj) {
        User.userObj = userObj;
    }
}
