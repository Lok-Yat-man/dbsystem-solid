package cn.edu.szu.cs.fatory.impl;

import cn.edu.szu.cs.ds.irtree.DefaultIRTree;
import cn.edu.szu.cs.entity.DefaultRelatedObject;
import cn.edu.szu.cs.entity.GeoPointDouble;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.fatory.KSTCFactory;
import cn.edu.szu.cs.kstc.TopKSpatialTextualClustersRetrieval;
import cn.edu.szu.cs.service.DefaultRelatedObjectServiceImpl;
import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.edu.szu.cs.strategy.WeightCalculationStrategy;
import cn.edu.szu.cs.strategy.impl.TfIdfStrategy;
import com.github.davidmoten.rtree.Entries;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;

import java.util.List;
import java.util.stream.Collectors;

/**
 *  DefaultDBScanBasedApproachFactory
 * @author Whitence
 * @date 2024/3/9 19:29
 * @version 1.0
 */
public class DefaultDBScanBasedApproachFactory implements KSTCFactory<DefaultRelatedObject> {
    @Override
    public TopKSpatialTextualClustersRetrieval<DefaultRelatedObject> createTopKSpatialTextualClustersRetrieval() {

        IRelatedObjectService<DefaultRelatedObject> relatedObjectService = new DefaultRelatedObjectServiceImpl();
        // Get all the objects
        List<DefaultRelatedObject> objects = relatedObjectService.getAll();
        // Calculate the weight of the object
        WeightCalculationStrategy<DefaultRelatedObject> weightCalculationStrategy = new TfIdfStrategy<>();
        weightCalculationStrategy.calculate(objects);
        // Create RTree
        List<Entry<DefaultRelatedObject, GeoPointDouble>> entries = objects.stream().map(obj -> {
            GeoPointDouble geoPointDouble = GeoPointDouble.create(obj.getCoordinate()[0], obj.getCoordinate()[1]);
            return Entries.entry(obj, geoPointDouble);
        }).collect(Collectors.toList());
        RTree<DefaultRelatedObject, GeoPointDouble> rTree = RTree.create(entries);

        // Create IRtree
        DefaultIRTree iRtree = new DefaultIRTree(rTree);


        return null;
    }
}
