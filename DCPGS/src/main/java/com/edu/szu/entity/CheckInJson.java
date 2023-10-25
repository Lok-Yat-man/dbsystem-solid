package com.edu.szu.entity;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CheckInJson {

    @Expose
    private List<Cluster> data;

    @Data
    @AllArgsConstructor
    public static class Cluster{
        @Expose
        private long clusterId;
        @Expose
        private List<GeoPair> checkIns;
    }

    @Data
    @AllArgsConstructor
    public static class GeoPair{
        @Expose
        private double latitude;
        @Expose
        private double longitude;
    }
}
