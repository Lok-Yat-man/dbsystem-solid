package com.edu.szu;

import com.edu.szu.entity.CheckInJson;
import com.edu.szu.entity.DCPGSParams;
import com.edu.szu.entity.GeoJson;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RestController
@RequestMapping("/dcpgs")
public class DCPGSEndpoint {

    private final DCPGSManager manager;

    public DCPGSEndpoint(DCPGSManager manager){
        this.manager = manager;
    }

    @PutMapping("/params/{location}")
    public boolean updateDCPGSParams(@PathVariable String location,@RequestBody DCPGSParams params){
        manager.setAllParams(location,params);
        return true;
    }

    @GetMapping("/params/{location}")
    public DCPGSParams dcpgsParams(@PathVariable String location){
        return manager.getParams(location);
    }

    @GetMapping("/{dataSet}/run/{location}")
    public boolean dcpgsRun(@PathVariable String dataSet,@PathVariable String location) throws Exception {
        String path = dataSet + "/splittedCheckIn/" + location + ".txt";
        manager.dcpgsRun(path, location, dataSet);
        return true;
    }

    @GetMapping("/{dataSet}/json/{location}")
    public CheckInJson dcpgsJson(@PathVariable String dataSet, @PathVariable String location) throws IOException {
        return manager.getJson(location, dataSet);
    }

    @GetMapping("/{dataSet}/geoJson/{location}")
    public GeoJson dcpgsGeoJson(@PathVariable String dataSet, @PathVariable String location) throws IOException {
        return manager.getGeoJson(location, dataSet);
    }
}
