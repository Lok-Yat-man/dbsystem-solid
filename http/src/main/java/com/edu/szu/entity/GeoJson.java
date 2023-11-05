package com.edu.szu.entity;

import java.util.ArrayList;
import java.util.List;


public class GeoJson {
    private String type = "FeatureCollection";
    private List<Feature> features;
    public GeoJson(){
        features=new ArrayList<>();
    }
    public List<Feature> getFeatures() {
        return features;
    }
    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public static class Feature{
        private String type = "Feature";
        private Geometry geometry;
        private Properties properties;
        public Feature(Geometry geometry,Properties properties){
            this.geometry = geometry;
            this.properties = properties;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public Geometry getGeometry() {
            return geometry;
        }
        public void setGeometry(Geometry geometry) {
            this.geometry = geometry;
        }
        public Properties getProperties() {
            return properties;
        }
        public void setProperties(Properties properties) {
            this.properties = properties;
        }
    }


    public static class Geometry{
        private String type = "Point";
        private double[] coordinates = new double[2];
        public Geometry(double lon,double lat){
            this.coordinates[0] = lon;
            this.coordinates[1] = lat;
        }
        public String getType() {
            return type;
        }
        public void setType(String type) {
            this.type = type;
        }
        public double[] getCoordinates() {
            return coordinates;
        }
        public void setCoordinates(double[] coordinates) {
            this.coordinates = coordinates;
        }
    }


    public static class Properties{
        private String clusterId;
        private List<String> labels;
        public Properties(String clusterId, List<String> labels) {
            this.clusterId = clusterId;
            this.labels = labels;
        }
        public String getClusterId() {
            return clusterId;
        }
        public void setClusterId(String clusterId) {
            this.clusterId = clusterId;
        }
        public List<String> getLabels() {
            return labels;
        }
        public void setLabels(List<String> labels) {
            this.labels = labels;
        }
    }
}
