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
import com.github.davidmoten.rtree.Node;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometry;
import com.github.davidmoten.rtree.internal.LeafDefault;
import com.github.davidmoten.rtree.internal.NonLeafDefault;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;


public class KSTCTest {



    @Test
    public void simpleKSTCTest() throws Exception {
        // 当前位置
        GeoPointDouble geoPointDouble = GeoPointDouble.create(
                -75.08,
                39.91
        );
        Coordinate coordinate = Coordinate.create(
                -75.08,
                39.91
        );

        // 1. 随机构造一千万个坐标点
        List<Entry<String, GeoPointDouble>> geoPointDoubleList = new ArrayList<>(1_000_000);

        for (int i = 0; i < 1_000_000; i++) {

            double random = Math.random();

            if(random < 0.5) {
                // 生成一个在当前位置附近的点
                double lon = geoPointDouble.x() + random * 0.01;
                double lat = geoPointDouble.y() + random * 0.01;
                geoPointDoubleList.add(
                        Entries.entry(
                                i + "",
                                GeoPointDouble.create(lon, lat)
                        )
                );
            }else{
                // 生成一个在当前位置附近的点
                double lon = -geoPointDouble.x() + (Math.random() - 0.5) * 0.1;
                double lat = -geoPointDouble.y() + (Math.random() - 0.5) * 0.1;
                geoPointDoubleList.add(
                        Entries.entry(
                                i + "",
                                GeoPointDouble.create(lon, lat)
                        )
                );
            }

        }

        System.out.println("构造数量级"+geoPointDoubleList.size());

        long s = System.currentTimeMillis();

        RTree<String, GeoPointDouble> rTree = RTree
                .star()
                //.maxChildren(3000)
                .maxChildren(30)
                .create(geoPointDoubleList);
        System.out.println("构造时间消耗： "+(System.currentTimeMillis() - s));
        s = System.currentTimeMillis();
        GeoPointDouble.cnt = 0;

        ArrayList<Entry<String, GeoPointDouble>> list = geoPointDoubleList.stream()
                .filter(entry -> entry.geometry().distance(geoPointDouble) < 10_0)
                .collect(Collectors.toCollection(ArrayList::new));

        System.out.println("结果数量"+list.size());
        System.out.println("线性计算 时间消耗： "+(System.currentTimeMillis() - s));
        System.out.println("线性计算 距离计算次数： "+GeoPointDouble.cnt);


        GeoPointDouble.cnt = 0;
        s = System.currentTimeMillis();

        List<GeoPointDouble> list2 = new ArrayList<>();
        for (Entry<String, GeoPointDouble> stringGeoPointDoubleEntry : rTree.search(geoPointDouble, 10_0).toBlocking().toIterable()) {
            list2.add(stringGeoPointDoubleEntry.geometry());
        }
        System.out.println("结果数量"+list2.size());
        System.out.println("rtree 时间消耗： "+(System.currentTimeMillis() - s));
        System.out.println("rtree 距离计算次数： "+GeoPointDouble.cnt);


        GeoPointDouble.cnt = 0;
        s = System.currentTimeMillis();
        Node<String, GeoPointDouble> node = rTree.root().get();
        List<Entry<String,GeoPointDouble>> result = new ArrayList<>();
        Queue<Node<String, GeoPointDouble>> queue = new LinkedList<>();

        queue.add(node);

        while(!queue.isEmpty()){

            Node<String, GeoPointDouble> curNode = queue.poll();

            double distance = curNode.geometry().distance(geoPointDouble);

            if(distance > 10_0){
                continue;
            }

            if(curNode instanceof LeafDefault){

                LeafDefault<String, GeoPointDouble> leafDefault = (LeafDefault<String, GeoPointDouble>) curNode;

                for (Entry<String, GeoPointDouble> entry : leafDefault.entries()) {

                    if(entry.geometry().distance(geoPointDouble) < 10_0){
                        result.add(entry);
                    }

                }

            }

            if(curNode instanceof NonLeafDefault){

                NonLeafDefault<String, GeoPointDouble> nonLeafDefault = (NonLeafDefault<String, GeoPointDouble>) curNode;

                queue.addAll(nonLeafDefault.children());

            }

        }

        System.out.println("结果数量"+result.size());
        System.out.println("rtree 时间消耗： "+(System.currentTimeMillis() - s));
        System.out.println("rtree 距离计算次数： "+GeoPointDouble.cnt);

    }

}
