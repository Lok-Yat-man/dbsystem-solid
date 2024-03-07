package com.edu.szu.entity;

import java.util.ArrayList;
import java.util.List;

public class GeoJsonSkyline {
    private String type = "FeatureCollection";
    private List<GeoJsonSkyline.Feature> features;

    public GeoJsonSkyline() {
        features = new ArrayList<>();
    }

    public List<GeoJsonSkyline.Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<GeoJsonSkyline.Feature> features) {
        this.features = features;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static class Feature {
        private String type = "Feature";
        private GeoJsonSkyline.Geometry geometry;
        private GeoJsonSkyline.Properties properties;

        public Feature(GeoJsonSkyline.Geometry geometry, GeoJsonSkyline.Properties properties) {
            this.geometry = geometry;
            this.properties = properties;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public GeoJsonSkyline.Geometry getGeometry() {
            return geometry;
        }

        public void setGeometry(GeoJsonSkyline.Geometry geometry) {
            this.geometry = geometry;
        }

        public GeoJsonSkyline.Properties getProperties() {
            return properties;
        }

        public void setProperties(GeoJsonSkyline.Properties properties) {
            this.properties = properties;
        }
    }


    public static class Geometry {
        private String type = "Point";
        private double[] coordinates = new double[2];

        public Geometry(double lon, double lat) {
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


    public static class Properties {
        private String skylineId;
        private String name;
        private List<String> labels;

        public Properties(String skylineId, String name, List<String> labels) {
            this.skylineId = skylineId;
            this.name = name;
            this.labels = labels;
        }

        public String getSkylineId() {
            return skylineId;
        }

        public void setSkylineId(String skylineId) {
            this.skylineId = skylineId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }
    }
}
