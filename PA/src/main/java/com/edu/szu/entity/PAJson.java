package com.edu.szu.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NoArgsConstructor;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
@NoArgsConstructor
/**
 * @author 86136 Email:a@wk2.cn
 * @since 2024/03/31 16:41
 */
public class PAJson {
    public static CheckInJson readJsonFile(String filePath) throws Exception {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        try (FileReader reader = new FileReader(filePath)) {
            return gson.fromJson(reader, CheckInJson.class);
        }
    }

}
