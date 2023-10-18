package com.edu.szu;

import com.edu.szu.entity.CheckInJson;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.util.CheckInReader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/dcpgs")
public class DCPGSEndpoint {

    @GetMapping("/json/{location}")
    public CheckInJson gowallaTop100(@PathVariable String location) throws IOException {
        String path = "";
        switch (location){
            case "Australia":
                path = "gowalla/result/Australia.json";
                break;
            case "Europe":
                path = "gowalla/result/EuropeTop20.json";
                break;
            case "NorthernAmerica":
                path = "gowalla/result/NorthernAmericaTop40.json";
                break;
            case "SouthAfrica":
                path = "gowalla/result/SouthAfrica.json";
                break;
            case "SoutheastAsia":
                path = "gowalla/result/SoutheastAsia.json";
                break;
            case "WesternAsia":
                path = "gowalla/result/WesternAsia.json";
                break;
        }
        return CheckInReader.parseJson(path);
    }

    @GetMapping("/geoJson/{location}")
    public GeoJson dcpgsGeoJson(@PathVariable String location) throws IOException {
        String path = "";
        switch (location){
            case "Australia":
                path = "gowalla/geojson/Australia.geojson";
                break;
            case "Europe":
                path = "gowalla/geojson/Europe.geojson";
                break;
            case "NorthernAmerica":
                path = "gowalla/geojson/NorthernAmerica.geojson";
                break;
            case "SouthAfrica":
                path = "gowalla/geojson/SouthAfrica.geojson";
                break;
            case "SoutheastAsia":
                path = "gowalla/geojson/SoutheastAsia.geojson";
                break;
            case "WesternAsia":
                path = "gowalla/geojson/WesternAsia.geojson";
                break;
        }
        return CheckInReader.readGeoJsonFromFile(path);
    }
}
