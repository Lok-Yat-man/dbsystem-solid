package com.edu.szu.service;

import cn.edu.szu.cs.entity.KstcQuery;
import com.edu.szu.api.Result;
import com.edu.szu.dto.DataCoordinateRangeDTO;
import com.edu.szu.entity.GeoJson;
import com.edu.szu.entity.Marker;

import java.util.List;

public interface KstcService {


    GeoJson loadGeoJson(KstcQuery query);

    List<Marker> loadMarkers(KstcQuery query);

    Result<DataCoordinateRangeDTO> getDataCoordinateRange(KstcQuery kstcQuery);

    List<String> getKeyWords(String keywords);
}
