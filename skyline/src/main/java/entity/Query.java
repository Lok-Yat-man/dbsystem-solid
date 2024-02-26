package entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class Query implements Serializable {

    private Coordinate location;

    private List<String> keywords;

    private int k;

    private double distanceConstraint;

    private int minPts;

    public Query(){}

    public Query(Coordinate location, List<String> keywords, int k, double distanceConstraint, int minPts) {
        this.location = location;
        this.keywords = keywords;
        this.k = k;
        this.distanceConstraint = distanceConstraint;
        this.minPts = minPts;
    }

    public static Query create(Coordinate location, List<String> keywords, int k, double distanceConstraint, int minPts){
        return new Query(location, keywords, Math.min(k,10), Math.min(distanceConstraint,50.0), minPts);
    }

    @Override
    public String toString() {
        return "Query{" +
                "location=" + location +
                ", keywords=" + keywords +
                ", k=" + k +
                ", distanceConstraint=" + distanceConstraint +
                ", minPts=" + minPts +
                '}';
    }
}