package com.edu.szu.service;


import com.edu.szu.entity.GeoJsonSkyline;
import com.edu.szu.entity.ObjectPoint;
import entity.Query;

import java.util.List;

public interface STDService {

    List<ObjectPoint> loadBstdObjectPoint(Query query);

    GeoJsonSkyline loadBstdGeoJsonSkyline(Query query);

    List<ObjectPoint> loadAstdObjectPoint(Query query);

    GeoJsonSkyline loadAstdGeoJsonSkyline(Query query);

}
