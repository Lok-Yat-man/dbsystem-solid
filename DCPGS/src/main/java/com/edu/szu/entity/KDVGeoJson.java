package com.edu.szu.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KDVGeoJson {
    String type = "FeatureCollection";

    List<KDVGeoJson.Feature> features;

    public KDVGeoJson(){
        this.features = new ArrayList<>();
    }

    public void addFeature(KDVGeoJson.Feature feature){
        this.features.add(feature);
    }

    @Data
    public static class Feature{
        String type = "Feature";
        KDVGeoJson.Geometry geometry;
        KDVGeoJson.Properties properties;
        public Feature(KDVGeoJson.Geometry geometry, KDVGeoJson.Properties properties){
            this.geometry = geometry;
            this.properties = properties;
        }
    }

    @Data
    public static class Geometry{
        String type = "Polygon";
        List<List<double[]>> coordinates = new ArrayList<>();
        public Geometry(){
            this.coordinates.add(new ArrayList<>());
        }

        public void addPoint(double lon,double lat){
            this.coordinates.get(0).add(new double[]{lon,lat});
        }
    }

    @Data
    @AllArgsConstructor
    public static class Properties{
        Long index;
    }
}
