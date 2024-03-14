package cn.edu.szu.cs.fatory.impl;

import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.entity.SimpleRelatedObject;
import cn.edu.szu.cs.fatory.KSTCFactory;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;
import cn.edu.szu.cs.service.DefaultRelatedObjectServiceImpl;
import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.edu.szu.cs.strategy.WeightCalculationStrategy;
import cn.edu.szu.cs.strategy.impl.TfIdfStrategy;

import java.util.List;

public class SimpleDBScanBasedApproachFactory implements KSTCFactory<SimpleRelatedObject> {

    @Override
    public TopKSpatialTextualClustersRetrieval createTopKSpatialTextualClustersRetrieval() {


        IRelatedObjectService relatedObjectService = new DefaultRelatedObjectServiceImpl();
        List<RelatedObject> objects = relatedObjectService.getAll();
        WeightCalculationStrategy<RelatedObject> weightCalculationStrategy = new TfIdfStrategy<>();
        weightCalculationStrategy.calculate(objects);


        return null;
    }


}
