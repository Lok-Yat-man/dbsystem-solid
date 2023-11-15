package com.edu.szu.entity;

import com.edu.szu.api.NamedPoint;
import com.edu.szu.util.CheckInDistanceCalculator;
import com.edu.szu.util.EuclideanDistanceCalculator;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.Objects;

@Data
@AllArgsConstructor
public class CheckIn implements NamedPoint {

    /**
     * user id
     *
     */
    long userId;

    /**
     * check in time
     *
     */
    String checkInTime;

    /**
     * latitude 维度
     *
     */
    @Expose
    double latitude;

    /**
     * longitude 精度
     *
     */
    @Expose
    double longitude;

    /**
     * location id
     *
     */
    String locationId;

    @Override
    public String toString() {
        return userId + "\t" + checkInTime + "\t" + latitude + "\t" + longitude + "\t" + locationId;
    }

    @Override
    public String getName() {
        return getUserId() + getLocationId();
    }

    @SneakyThrows
    @Override
    public double distance(Rectangle rectangle) {
        return EuclideanDistanceCalculator.calculateDistance(this.latitude,this.longitude
        ,((CheckIn) rectangle).latitude,((CheckIn) rectangle).longitude);
    }

    @Override
    public Rectangle mbr() {
        return this;
    }

    @Override
    public boolean intersects(Rectangle rectangle) {
        return false;
    }

    @Override
    public double x1() {
        return getLongitude();
    }

    @Override
    public double y1() {
        return getLatitude();
    }

    @Override
    public double x2() {
        return getLongitude();
    }

    @Override
    public double y2() {
        return getLatitude();
    }

    @Override
    public double area() {
        return 0.0;
    }

    @Override
    public double intersectionArea(Rectangle rectangle) {
        return 0.0;
    }

    @Override
    public double perimeter() {
        return 0.0;
    }

    @Override
    public Rectangle add(Rectangle rectangle) {
        return this;
    }

    @Override
    public boolean contains(double latitude, double longitude) {
        return this.latitude == latitude && this.longitude == longitude;
    }

    @Override
    public boolean isDoublePrecision() {
        return true;
    }

    @Override
    public double x() {
        return longitude;
    }

    @Override
    public double y() {
        return latitude;
    }

    @Override
    public Geometry geometry() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CheckIn checkIn = (CheckIn) o;
        return Objects.equals(locationId, checkIn.locationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(locationId);
    }

    public CheckInJson.GeoPair getPair(){
        return new CheckInJson.GeoPair(getLatitude(),getLongitude());
    }
}
