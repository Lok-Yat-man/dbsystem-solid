package cn.edu.szu.cs;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.InternalStructure;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Geometry;
import rx.Observable;
import rx.functions.Func2;

import java.io.InputStream;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        try(
                InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("rtree.txt");
        ){
            // deserialize rTree
            RTree<String, Geometry> rTree = Serializers.flatBuffers().utf8().read(
                    inputStream,
                    inputStream.available(),
                    InternalStructure.DEFAULT
            );

            GeoPointDouble geoPointDouble = GeoPointDouble.create(
                    112.1,
                    23.1
            );

            IRelatedObjectService relatedObjectService = new DefaultRelatedObjectServiceImpl();

            SimpleIRTree simpleIRTree = new SimpleIRTree(rTree, relatedObjectService);

            DefaultInvertedIndex defaultInvertedIndex = new DefaultInvertedIndex(relatedObjectService);

            KSTC<RelatedObject> kstc = new SimpleKSTC<>(simpleIRTree,defaultInvertedIndex);



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
