package com.example.brunovsiq.mapchat.models;

import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

public class Partner implements Comparable<Partner>{

    private String username;
    private double latitude;
    private double longitude;
    private double distanceToUser;

    public Partner(JSONObject jsonObject) throws JSONException {
        this.username = jsonObject.getString("username");
        this.latitude = jsonObject.getDouble("latitude");
        this.longitude = jsonObject.getDouble("longitude");

        Location pLocation = new Location("");
        pLocation.setLongitude(longitude);
        pLocation.setLatitude(latitude);
        Location location = new Location("");
        location.setLatitude(User.getInstance().getLatitude());
        location.setLongitude(User.getInstance().getLongitude());

        this.distanceToUser = pLocation.distanceTo(location);

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



    @Override
    public int compareTo(Partner p) {
        return  (int) (p.distanceToUser - distanceToUser);
    }
}
