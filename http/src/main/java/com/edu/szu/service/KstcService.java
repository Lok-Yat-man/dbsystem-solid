package com.edu.szu.service;

import cn.edu.szu.cs.Query;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;

import java.util.List;

public interface KstcService {


    GeoJson loadGeoJson(Query query);

    List<Marker> loadMarkers(Query query);
}
