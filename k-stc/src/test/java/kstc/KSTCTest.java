package kstc;

import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.entity.GeoPointDouble;
import cn.edu.szu.cs.entity.Query;
import cn.edu.szu.cs.entity.RelatedObject;
import cn.edu.szu.cs.ivtidx.DefaultInvertedIndex;
import cn.edu.szu.cs.ivtidx.InvertedIndex;
import cn.edu.szu.cs.kstc.KSTC;
import cn.edu.szu.cs.kstc.SimpleKSTC2;
import cn.edu.szu.cs.service.DefaultRelatedObjectServiceImpl;
import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.edu.szu.cs.util.CommonAlgorithm;
import cn.hutool.core.thread.ConcurrencyTester;
import cn.hutool.core.thread.ThreadUtil;
import com.github.davidmoten.rtree.Entries;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;


public class KSTCTest {



    @Test
    public void simpleKSTCTest() throws Exception {

        IRelatedObjectService relatedObjectService = new DefaultRelatedObjectServiceImpl();

        List<RelatedObject> objects = relatedObjectService.getAll();
        System.out.println("objects.size() = " + objects.size());
        List<Entry<String, GeoPointDouble>> entryList = objects.stream()
                .map(
                        object -> Entries.entry(
                                object.getObjectId(),
                                GeoPointDouble.create(
                                        object.getCoordinate().getLongitude(),
                                        object.getCoordinate().getLatitude()
                                )
                        )
                )
                .collect(Collectors.toList());

        RTree<String, GeoPointDouble> rTree = RTree
                .star()
                .maxChildren(1500)
                .minChildren(1000)
                .create(entryList);

        GeoPointDouble geoPointDouble = GeoPointDouble.create(
                -75.08,
                39.91
        );
        Coordinate coordinate = Coordinate.create(
                -75.08,
                39.91
        );



    }

}
