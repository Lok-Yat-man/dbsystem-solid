package com.edu.szu.service;


import com.edu.szu.entity.Marker;
import com.edu.szu.entity.ObjectPoint;
import entity.Query;

import java.util.List;

public interface BstdService {

    List<ObjectPoint> loadObjectPoint(Query query);

}
