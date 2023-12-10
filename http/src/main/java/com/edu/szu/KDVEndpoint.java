package com.edu.szu;

import com.edu.szu.entity.KDVGeoJson;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kdv")
@Log4j2
public class KDVEndpoint {

    private final KDVManager kdvManager;

    public KDVEndpoint(KDVManager kdvManager){
        this.kdvManager = kdvManager;
    }

    @PostMapping("/geojson")
    public KDVGeoJson getGeoJson(@RequestBody String request){
        log.info("request: \n{}",request);
        return kdvManager.getKDVGeoJson(request);
    }

}
