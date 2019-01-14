package com.uhfdemo2longer;

import java.io.Serializable;

public class Location implements Serializable {
    private String locationID;
    private String locationName;

    public Location() {
    }

    public Location(String locationID, String locationName) {
        this.locationID = locationID;
        this.locationName = locationName;
    }

    public String getLocationID() {
        return locationID;
    }

    public void setLocationID(String locationID) {
        this.locationID = locationID;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @Override
    public String toString() {
        return locationName;
    }
}
