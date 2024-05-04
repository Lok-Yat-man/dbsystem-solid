package com.edu.szu.service.impl;


import com.edu.szu.entity.GeoJsonSkyline;
import com.edu.szu.entity.ObjectPoint;
import com.edu.szu.service.STDService;
import entity.Coordinate;
import entity.Query;
import entity.RelevantObject;
import std.ASTD;
import std.BSTD;

import java.util.LinkedList;
import java.util.List;

public class STDServiceImpl implements STDService {

    private BSTD bstd;
    private ASTD astd;

    public STDServiceImpl(BSTD bstd, ASTD astd) {
        this.bstd = bstd;
        this.astd = astd;
    }

    @Override
    public List<ObjectPoint> loadBstdObjectPoint(Query query) {

        List<RelevantObject> relevantObjectList = this.bstd.bstd(query);

        return getObjectPoints(relevantObjectList);
    }

    @Override
    public List<ObjectPoint> loadAstdObjectPoint(Query query) {

        List<RelevantObject> relevantObjectList = this.astd.astd(query);

        return getObjectPoints(relevantObjectList);
    }

    private List<ObjectPoint> getObjectPoints(List<RelevantObject> relevantObjectList) {
        List<ObjectPoint> res = new LinkedList<>();
        for (int i = 0; i < relevantObjectList.size(); i++) {
            ObjectPoint objectPoint = new ObjectPoint();
            objectPoint.setObjId(relevantObjectList.get(i).getObjectId());
            objectPoint.setName(relevantObjectList.get(i).getName());
            objectPoint.setCoordinate(Coordinate.create(
                    relevantObjectList.get(i).getCoordinate().getLongitude(),
                    relevantObjectList.get(i).getCoordinate().getLatitude()
            ));
            objectPoint.setKeywords(relevantObjectList.get(i).getLabels());
            res.add(objectPoint);
        }
        return res;
    }

    @Override
    public GeoJsonSkyline loadBstdGeoJsonSkyline(Query query) {

        List<RelevantObject> relevantObjectList = this.bstd.bstd(query);

        return getGeoJsonSkyline(relevantObjectList);
    }

    @Override
    public GeoJsonSkyline loadAstdGeoJsonSkyline(Query query) {

        List<RelevantObject> relevantObjectList = this.astd.astd(query);

        return getGeoJsonSkyline(relevantObjectList);
    }

    private GeoJsonSkyline getGeoJsonSkyline(List<RelevantObject> relevantObjectList) {
        GeoJsonSkyline geoJsonSkyline = new GeoJsonSkyline();
        for (int i = 0; i < relevantObjectList.size(); i++) {
            RelevantObject relevantObject = relevantObjectList.get(i);
            String skylineId = i + "";
            GeoJsonSkyline.Geometry geometry = new GeoJsonSkyline.Geometry(
                    relevantObject.getCoordinate().getLongitude(),
                    relevantObject.getCoordinate().getLatitude()
            );
            GeoJsonSkyline.Properties properties = new GeoJsonSkyline.Properties(skylineId, relevantObject.getName(), relevantObject.getLabels());
            GeoJsonSkyline.Feature feature = new GeoJsonSkyline.Feature(geometry, properties);
            geoJsonSkyline.getFeatures().add(feature);
        }
        return geoJsonSkyline;
    }
}
