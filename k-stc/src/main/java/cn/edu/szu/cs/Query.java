package cn.edu.szu.cs;

import java.io.Serializable;
import java.util.ArrayList;
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
    private double epsilon;

    /**
     * @mock 5
     */
    private int minPts;


    private double maxDistance;

    public static QueryBuilder builder(){
        return new QueryBuilder();
    }
    public QueryBuilder toBuilder(){
        return new QueryBuilder(this);
    }

    public static class QueryBuilder{

        private Query query;

        public QueryBuilder(){
            query=new Query();
        }
        public QueryBuilder(Query query){
            this.query=query;
        }

        public QueryBuilder location(Coordinate coordinate){
            query.setLocation(coordinate);
            return this;
        }

        public QueryBuilder k(int k){
            query.setK(k);
            return this;
        }
        public QueryBuilder epsilon(double epsilon){
            query.setEpsilon(epsilon);
            return this;
        }

        public QueryBuilder minPts(int minPts){
            query.setMinPts(minPts);
            return this;
        }

        public QueryBuilder keyword(List<String> kwds){
            query.setKeywords(kwds);
            return this;
        }

        public QueryBuilder maxDistance(double maxDistance){
            query.setMaxDistance(maxDistance<0?Double.MAX_VALUE:maxDistance);
            return this;
        }

        public Query build(){
            return query;
        }

    }


    public Query(){}

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

    public int getMinPts() {
        return minPts;
    }

    public void setMinPts(int minPts) {
        this.minPts = minPts;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    @Override
    public String toString() {
        return "Query{" +
                "location=" + location +
                ", keywords=" + keywords +
                ", k=" + k +
                ", epsilon=" + epsilon +
                ", minPts=" + minPts +
                '}';
    }
}
