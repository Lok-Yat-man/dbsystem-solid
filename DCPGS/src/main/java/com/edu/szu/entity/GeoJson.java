package com.edu.szu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class GeoJson {
    String type = "FeatureCollection";

    List<Feature> features;

    @Data
    public static class Feature{
        String type = "Feature";
        Geometry geometry;
        Properties properties;
        public Feature(Geometry geometry,Properties properties){
            this.geometry = geometry;
            this.properties = properties;
        }
    }

    @Data
    public static class Geometry{
        String type = "Point";
        double[] coordinates = new double[2];
        public Geometry(double lon,double lat){
            this.coordinates[0] = lon;
            this.coordinates[1] = lat;
        }
    }

    @Data
    @AllArgsConstructor
    public static class Properties{
        String clusterId;
    }
}
