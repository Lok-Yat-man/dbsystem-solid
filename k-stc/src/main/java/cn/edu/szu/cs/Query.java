package cn.edu.szu.cs;

import java.io.Serializable;
import java.util.List;

/**
 * @author Whitence
 * @date 2023/9/30 22:29
 * @version 1.0
 */
public class Query implements Serializable {

    /**
     * @mock 112,23
     */
    private Coordinate location;

    /**
     * @mock ["Water"]
     */
    private List<String> keywords;

    /**
     * @mock 5
     */
    private int k;

    /**
     * @mock 50.0
     */
    private double distanceConstraint;

    /**
     * @mock 5
     */
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

    public Coordinate getLocation() {
        return location;
    }

    public void setLocation(Coordinate location) {
        this.location = location;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public double getDistanceConstraint() {
        return distanceConstraint;
    }

    public void setDistanceConstraint(double distanceConstraint) {
        this.distanceConstraint = distanceConstraint;
    }

    public int getMinPts() {
        return minPts;
    }

    public void setMinPts(int minPts) {
        this.minPts = minPts;
    }
}
