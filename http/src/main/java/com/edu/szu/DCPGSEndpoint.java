package com.edu.szu;

import com.edu.szu.entity.CheckInJson;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.entity.DCPGSGeoJson;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/dcpgs/{dataSet}")
public class DCPGSEndpoint {

    private final DCPGSManager manager;

    public DCPGSEndpoint(DCPGSManager manager){
        this.manager = manager;
    }

    @PutMapping("/params/{location}")
    public boolean updateDCPGSParams(@PathVariable String location,@RequestBody DCPGSParams params,
                                     @PathVariable String dataSet){
        manager.setAllParams(location,params,dataSet);
        return true;
    }

    @GetMapping("/params/{location}")
    public DCPGSParams dcpgsParams(@PathVariable String location,@PathVariable String dataSet){
        return manager.getParams(location,dataSet);
    }

    @PostMapping("/run/{location}")
    public boolean dcpgsRun(@PathVariable String dataSet,@PathVariable String location) throws Exception {
        String path = dataSet + "/splittedCheckIn/" + location + ".txt";
        manager.dcpgsRun(path, location, dataSet);
        return true;
    }

    @GetMapping("/json/{location}")
    public CheckInJson dcpgsJson(@PathVariable String dataSet, @PathVariable String location) throws IOException {
        return manager.getJson(location, dataSet);
    }

    @GetMapping("/geoJson/{location}")
    public DCPGSGeoJson dcpgsGeoJson(@PathVariable String dataSet, @PathVariable String location) throws IOException {
        return manager.getGeoJson(location, dataSet);
    }
}
