package com.edu.szu;

import com.edu.szu.entity.KDVData;
import com.edu.szu.entity.KDVGeoJson;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KDVManager {
    private static final String[] heatMapColors = new String[]{
            "rgb(158,1,66)",
            "rgb(213,62,79)",
            "rgb(244,109,67)",
            "rgb(253,174,97)",
            "rgb(254,224,139)",
            "rgb(230,245,152)",
            "rgb(171,221,164)",
            "rgb(102,194,165)",
            "rgb(50,136,189)",
            "rgb(94,79,162)",
            "rgba(0,0,0,0)"
    };

    public KDVGeoJson getKDVGeoJson(String request){
        String decodedRequest = URLDecoder.decode(request, StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        List<KDVData> kdvDataList = new ArrayList<>();
        double maxIndex = -1;
        for (int i = 0; i < decodedRequest.length(); i++) {
            char ch = decodedRequest.charAt(i);
            if(ch == '\n'){
                String line = sb.toString();
                sb = new StringBuilder();
                if(line.equals("=")){
                    continue;
                }
                KDVData data = KDVData.parse(line);
                kdvDataList.add(data);
                if(data.getIndex() > maxIndex){
                    maxIndex = data.getIndex();
                }
            }else{
                sb.append(ch);
            }
        }
        List<KDVGeoJson.Feature> features = new ArrayList<>();
        for (KDVData kdvData : kdvDataList) {
            features.add(new KDVGeoJson.Feature(
                    new KDVGeoJson.Geometry(new double[]{kdvData.getLongitude(),kdvData.getLatitude()}),
                    new KDVGeoJson.Properties(kdvData.getIndex()/maxIndex)
            ));
        }
        return new KDVGeoJson(features);
    }

    private int getIndex(double index,double maxIndex){
        double[] step = new double[]{0.95,0.85,0.75,0.65,0.55,0.45,0.35,0.25,0.15,0.06};
        for (int i = 0; i < step.length; i++) {
            if(index > maxIndex * step[i]){
                return i;
            }
        }
        return step.length;
    }
}
