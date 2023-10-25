package com.edu.szu;

import com.edu.szu.entity.GeoJson;
import com.edu.szu.util.KDVReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kdv")
public class KDVEndpoint {

    @GetMapping("/geojson")
    public GeoJson getGeoJson(){
        return KDVReader.readFromFile("kdv/kdv.data");
    }

}
