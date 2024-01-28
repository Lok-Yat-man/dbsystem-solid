package com.edu.szu.entity;

import lombok.Data;

@Data
public class KDVData {
    private double longitude;
    private double latitude;
    private double index;
    public static KDVData parse(String line){
        String[] fields = line.split("%2C");
        KDVData kdvData = new KDVData();
        kdvData.setLongitude(Double.parseDouble(fields[0]));
        kdvData.setLatitude(Double.parseDouble(fields[1]));
        kdvData.setIndex(Double.parseDouble(fields[2]));
        return kdvData;
    }
}
