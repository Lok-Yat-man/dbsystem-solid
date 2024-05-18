package com.edu.szu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



@Data
public class KDVGeoJson {
    String type = "FeatureCollection";

    List<Feature> features;

    public KDVGeoJson(){
        this.features = new ArrayList<>();
    }

    public KDVGeoJson(List<Feature> features){
        this.features = features;
    }

    public void addFeature(Feature feature){
        this.features.add(feature);
    }

    @Data
    public static class Feature{
        String type = "Feature";
        Geometry geometry;
        Properties properties;
        public Feature(Geometry geometry, Properties properties){
            this.geometry = geometry;
            this.properties = properties;
        }
    }

    @Data
    public static class Geometry{
        String type = "Point";
        double[] coordinates;
        public Geometry(double[] coordinates){
            this.coordinates = coordinates;
        }
    }

    @Data
    public static class Properties{
        double dph;
        public Properties(double dph) {
            this.dph = dph;
        }
    }
}
