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

        RTree<String, GeoPointDouble> rTree = RTree.create(entryList);

        GeoPointDouble geoPointDouble = GeoPointDouble.create(
                -75.08,
                39.91
        );
        Coordinate coordinate = Coordinate.create(
                -75.08,
                39.91
        );

        long s = System.currentTimeMillis();

        Iterable<String> strings = rTree.search(geoPointDouble, 10000)
                .map(Entry::value)
                .toBlocking()
                .toIterable();
        Set<String> hashSet = new HashSet<>();
        strings.forEach(hashSet::add);

        long e = System.currentTimeMillis();

        System.out.println("1: "+(e-s));

        s = System.currentTimeMillis();
        Set<String> stringSet = objects.stream()
                .filter(object -> CommonAlgorithm.calculateDistance(object.getCoordinate(), coordinate) <= 10000)
                .map(RelatedObject::getObjectId)
                .collect(Collectors.toSet());
        e = System.currentTimeMillis();

        System.out.println("2: "+(e-s));


    }

}
