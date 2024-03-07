package entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Coordinate implements Serializable {

    private double longitude;

    private double latitude;

    public Coordinate(){}

    public Coordinate(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
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
