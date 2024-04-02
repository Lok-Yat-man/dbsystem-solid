package com.edu.szu.entity;

import com.edu.szu.util.CheckInReader;
import com.edu.szu.entity.CheckInJson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NoArgsConstructor;

import java.io.FileReader;
@NoArgsConstructor
/**
 * @author 86136 Email:a@wk2.cn
 * @since 2024/03/19 16:44
 */
public class PAGeoJson {
    public String getGeojson(String jsonPath,String dataset) throws Exception{
        try {
            CheckInJson checkInJson = readJsonFile(jsonPath);
            var geoJson = CheckInReader.parseGeoJson(checkInJson);
            var targetPath="app/src/main/resources/static/data/paGeoJson/" + dataset;
            CheckInReader.parseGeoJsonTo(geoJson,targetPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "app/src/main/resources/static/data/paGeoJson/" + dataset;
    }

    public static CheckInJson readJsonFile(String filePath) throws Exception {
        // 创建 Gson 对象，用于解析 JSON
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

        // 读取 JSON 文件并解析为 CheckInJson 对象
        try (FileReader reader = new FileReader(filePath)) {
            CheckInJson checkInJson = gson.fromJson(reader, CheckInJson.class);
            return checkInJson;
        }
    }
}
