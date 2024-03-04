package com.edu.szu.service;

import cn.edu.szu.cs.entity.KSTCQuery;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;

import java.util.List;

public interface KstcService {


    GeoJson loadGeoJson(KSTCQuery KSTCQuery);

    List<Marker> loadMarkers(KSTCQuery KSTCQuery);
}
