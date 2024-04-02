package com.edu.szu;

import com.edu.szu.entity.PAGeoJson;
import com.edu.szu.entity.PAJson;
import com.edu.szu.entity.CheckInJson;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

import lombok.extern.log4j.Log4j2;

/**
 * @author 86136 Email:a@wk2.cn
 * @since 2024/03/18 20:56
 */
@Log4j2
@RestController
@RequestMapping("/data/pa")
public class PAEndPoint {
    private final ConvergeManager manager =new ConvergeManager();

    private final PAGeoJson paGeoJson =new PAGeoJson();

    private final PAJson clusters =new PAJson();

    @PostMapping("/{inputPath}/run/{dataset}")
    public String paRun(@PathVariable String inputPath,@PathVariable String dataset) throws Exception {
        var outputPath="PA/src/main/result/"+dataset;
        var dataPath="app/src/main/resources/static/data/pa/"+inputPath;
        manager.function(dataPath, outputPath);
        log.info("runpa");
        return "run over";
    }

    @PostMapping("/{dataset}")
    public String getGeoJson(@PathVariable String dataset) throws Exception{
        log.info("getGeoJson dataset:"+dataset);
        var jsonPath="app/src/main/resources/static/data/pa/"+dataset;
        var result=paGeoJson.getGeojson(jsonPath,dataset);
        return result;
    }

    @GetMapping("/{dataset}")
    public CheckInJson getCluster(@PathVariable String dataset)throws Exception{
        log.info("getJson dataset:"+dataset);
        var jsonPath="app/src/main/resources/static/data/pa/"+dataset;
        return clusters.readJsonFile(jsonPath);
    }
}