package com.edu.szu.service;

import cn.edu.szu.cs.entity.KstcQuery;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;

import java.util.List;

public interface KstcService<T> {


    GeoJson loadGeoJson(KstcQuery KSTCQuery);

    List<Marker> loadMarkers(KstcQuery KSTCQuery);
}
