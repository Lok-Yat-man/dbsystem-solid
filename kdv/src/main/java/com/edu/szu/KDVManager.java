package com.edu.szu;

import com.edu.szu.entity.KDVData;
import com.edu.szu.entity.KDVGeoJson;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class KDVManager {

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
}
