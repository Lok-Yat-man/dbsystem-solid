package com.edu.szu;

import com.edu.szu.entity.PAGeoJson;
import com.edu.szu.entity.PAJson;
import com.edu.szu.entity.CheckInJson;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import lombok.extern.log4j.Log4j2;

/**
 * @author 86136 Email:a@wk2.cn
 * @since 2024/03/18 20:56
 */
@Log4j2
@RestController
@RequestMapping("/data/pa")
@Component
public class PAEndPoint {
    private final ConvergeManager manager =new ConvergeManager();

    private final PAGeoJson paGeoJson =new PAGeoJson();

    private final PAJson clusters =new PAJson();

    private final String basePath;

    public PAEndPoint(@Value("${PA.basePath}") String basePath){
        this.basePath = basePath;
    }

    @PostMapping("/{inputPath}/runpa")
    public String paRun(@PathVariable String inputPath) throws Exception {
        var outputPath="PA/src/main/result/"+inputPath;
        var dataPath=basePath+inputPath;
        manager.function(dataPath, outputPath);
        log.info("runpa");
        return "run over";
    }

    @PostMapping("/geojson/{dataset}")
    public String getGeoJson(@PathVariable String dataset) throws Exception{
        log.info("getGeoJson dataset:"+dataset);
        var jsonPath=basePath+dataset;
        var result=paGeoJson.getGeojson(jsonPath,dataset);
        return result;
    }

    @GetMapping("/cluster/{dataset}")
    public CheckInJson getCluster(@PathVariable String dataset)throws Exception{
        log.info("getJson dataset:"+dataset);
        var jsonPath=basePath+dataset;
        return clusters.readJsonFile(jsonPath);
    }
}