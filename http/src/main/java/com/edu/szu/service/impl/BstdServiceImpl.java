package com.edu.szu.service.impl;

import com.edu.szu.entity.ObjectPoint;
import com.edu.szu.service.BstdService;
import entity.Coordinate;
import entity.Query;
import entity.RelevantObject;
import std.BSTD;

import java.util.LinkedList;
import java.util.List;

public class BstdServiceImpl implements BstdService {

    private BSTD bstd;

    public BstdServiceImpl(BSTD bstd) {
        this.bstd = bstd;
    }

    @Override
    public List<ObjectPoint> loadObjectPoint(Query query) {
        List<Query> queries = new LinkedList<>();
        queries.add(query);
        List<RelevantObject> relevantObjectList = this.bstd.bstd(queries);
        List<ObjectPoint> res = new LinkedList<>();
        for (int i = 0; i < relevantObjectList.size(); i++) {
            ObjectPoint objectPoint = new ObjectPoint();
            objectPoint.setObjId(relevantObjectList.get(i).getObjectId());
            objectPoint.setCoordinate(Coordinate.create(
                    relevantObjectList.get(i).getCoordinate().getLongitude(),
                    relevantObjectList.get(i).getCoordinate().getLatitude()
            ));
            objectPoint.setKeywords(relevantObjectList.get(i).getWeightKey());
            res.add(objectPoint);
        }
        return res;
    }
}
