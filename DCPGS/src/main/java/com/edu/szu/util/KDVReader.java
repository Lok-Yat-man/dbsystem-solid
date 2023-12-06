package com.edu.szu.util;

import com.edu.szu.entity.DCPGSGeoJson;
import com.edu.szu.entity.KDVGeoJson;
import com.google.gson.Gson;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

public class KDVReader {
    public static DCPGSGeoJson readFromFile(String path){
        ClassPathResource classPathResource = new ClassPathResource(path);
        DCPGSGeoJson DCPGSGeoJson = new DCPGSGeoJson();
        try(var br = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()))){
            String line = br.readLine();
            List<DCPGSGeoJson.Feature> features = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                DCPGSGeoJson.Feature feature = new DCPGSGeoJson.Feature(
                        new DCPGSGeoJson.Geometry(Double.parseDouble(split[0]), Double.parseDouble(split[1])),
                        new DCPGSGeoJson.Properties(split[2])
                );
                features.add(feature);
            }
            DCPGSGeoJson.setFeatures(features);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return DCPGSGeoJson;
    }

    public static void writeTo(Object object, String target){
        try(var bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                target)))){
            bw.write(new Gson().toJson(object));
            bw.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static KDVGeoJson transToPolygon(String path){
        ClassPathResource classPathResource = new ClassPathResource(path);
        KDVGeoJson kdvGeoJson = new KDVGeoJson();
        try(var br = new BufferedReader(new InputStreamReader(classPathResource.getInputStream()))){
            String line = br.readLine();
            Map<Long, KDVGeoJson.Geometry> featureMap = new HashMap<>();
            while ((line = br.readLine()) != null) {
                String[] split = line.split(",");
                long index = Long.parseLong(split[2]);
                featureMap.computeIfAbsent(index, k -> new KDVGeoJson.Geometry());
                featureMap.get(index).addPoint(Double.parseDouble(split[0]),Double.parseDouble(split[1]));
            }
            featureMap.forEach((index,geometry) -> {
                geometry.addPoint(geometry.getCoordinates().get(0).get(0)[0],geometry.getCoordinates().get(0).get(0)[1]);
                kdvGeoJson.addFeature(new KDVGeoJson.Feature(geometry, new KDVGeoJson.Properties(index)));
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return kdvGeoJson;
    }

    public static void main(String[] args) {
        KDVGeoJson kdvGeoJson = transToPolygon("kdv/kdv.data");
        writeTo(kdvGeoJson,"DCPGS/src/main/resources/kdv/kdvPolygon.json");
    }
}
