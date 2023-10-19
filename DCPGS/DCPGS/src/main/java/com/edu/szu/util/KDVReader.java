package com.edu.szu.util;

import com.edu.szu.entity.GeoJson;
import com.google.gson.Gson;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class KDVReader {
    public static GeoJson readFromFile(String path){
        ClassPathResource classPathResource = new ClassPathResource(path);
        GeoJson geoJson = new GeoJson();
        try(var br = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()))){
            String line = br.readLine();
            List<GeoJson.Feature> features = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                GeoJson.Feature feature = new GeoJson.Feature(
                        new GeoJson.Geometry(Double.parseDouble(split[0]), Double.parseDouble(split[1])),
                        new GeoJson.Properties(split[2])
                );
                features.add(feature);
            }
            geoJson.setFeatures(features);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return geoJson;
    }

    public static void writeTo(String path,String target){
        GeoJson geoJson = readFromFile(path);
        try(var bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                target)))){
            bw.write(new Gson().toJson(geoJson));
            bw.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        writeTo("kdv/index.csv","kdv3.geojson");
    }
}
