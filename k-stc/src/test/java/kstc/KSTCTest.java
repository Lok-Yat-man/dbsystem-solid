package kstc;

import cn.edu.szu.cs.*;
import cn.hutool.core.io.resource.ClassPathResource;
import com.github.davidmoten.rtree.InternalStructure;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Geometry;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class KSTCTest {



    @Test
    public void simpleKSTCTest() throws Exception {


        InputStream inputStream = new ClassPathResource("rtree.txt").getStream();
        int available = inputStream.available();
        RTree<String, Geometry> rTree = Serializers.flatBuffers().utf8().read(inputStream, available, InternalStructure.DEFAULT);
        DefaultRelatedObjectServiceImpl defaultRelatedObjectService = new DefaultRelatedObjectServiceImpl();
        IRTree<RelatedObject> simpleIRTree = new SimpleIRTree(rTree, defaultRelatedObjectService);
        InvertedIndex<RelatedObject> defaultInvertedIndex = new DefaultInvertedIndex(defaultRelatedObjectService);
        KSTC<RelatedObject> simpleKSTC = new SimpleKSTC<>(simpleIRTree, defaultInvertedIndex);
        Query query = Query.builder()
                .location(
                        Coordinate.create(
                                -75,
                                39.9
                        )
                )
                .k(5)
                .maxDistance(-1)
                .minPts(10)
                .epsilon(1000.0)
                .keyword(Arrays.asList("Food"))
                .build();


        List<Set<RelatedObject>> sets = simpleKSTC.kstcSearch(query);




    }

}
