package cn.edu.szu.cs.entity;

import java.io.Serializable;

/**
 * @author Whitence
 * @date 2023/9/30 22:31
 * @version 1.0
 */
public class Coordinate implements Serializable {

    private double longitude;

    private double latitude;

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Coordinate(){

    }

    public static Coordinate create(double longitude, double latitude){
        return new Coordinate(longitude,latitude);
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "longitude=" + longitude +
                ", latitude=" + latitude +
                '}';
    }
}
