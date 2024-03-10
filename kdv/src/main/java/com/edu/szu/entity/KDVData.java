package com.edu.szu.entity;

import lombok.Data;

@Data
public class KDVData {
    private double longitude;
    private double latitude;
    private double index;
    public static KDVData parse(String line){
        String[] fields = line.split(",");
        KDVData kdvData = new KDVData();
        if(fields.length == 4){
            kdvData.setIndex(Double.parseDouble(fields[3]));
        }else{
            kdvData.setIndex(Double.parseDouble(fields[2]));
        }
        kdvData.setLongitude(Double.parseDouble(fields[0]));
        kdvData.setLatitude(Double.parseDouble(fields[1]));

        return kdvData;
    }
}
