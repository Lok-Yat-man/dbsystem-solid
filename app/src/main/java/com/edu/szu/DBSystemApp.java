package com.edu.szu;

import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.entity.GeoPointDouble;
import com.edu.szu.config.DCPGSConfig;

import com.edu.szu.config.KSTCConfig;
import com.github.davidmoten.rtree.Entries;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Import({
        // config
        DCPGSConfig.class,
        KSTCConfig.class,

        // endpoint
        DCPGSEndpoint.class,
        KSTCEndpoint.class,
        KDVEndpoint.class,
})
@SpringBootApplication
@Log4j2
public class DBSystemApp {

    public static void main(String[] args) {
//        SpringApplication.run(DBSystemApp.class, args);
        test();
    }

    private static void test(){
        // 1. 随机构造一千万个坐标点
        List<Entry<String, GeoPointDouble>> geoPointDoubleList = new ArrayList<>(100000);

        for (int i = 0; i < 1000000; i++) {

            double lon = Math.random() + -75.08;
            double lat = Math.random() + 39.91;

            geoPointDoubleList.add(
                    Entries.entry(
                            i+"",
                            GeoPointDouble.create(lon,lat)
                    )
            );
        }

        System.out.println("构造数量级"+geoPointDoubleList.size());

        RTree<String, GeoPointDouble> rTree = RTree
                .star()
                .maxChildren(30)
                .create(geoPointDoubleList);


        GeoPointDouble geoPointDouble = GeoPointDouble.create(
                -75.08,
                39.91
        );
        Coordinate coordinate = Coordinate.create(
                -75.08,
                39.91
        );

        long s = System.currentTimeMillis();

        ArrayList<Entry<String, GeoPointDouble>> list = geoPointDoubleList.stream()
                .filter(entry -> entry.geometry().distance(geoPointDouble) < 1000)
                .collect(Collectors.toCollection(ArrayList::new));
        System.out.println("结果数量"+list.size());
        System.out.println("线性计算 时间消耗： "+(System.currentTimeMillis() - s));

        s = System.currentTimeMillis();

        List<GeoPointDouble> list2 = new ArrayList<>(list.size());
        rTree.search(geoPointDouble, 1000).forEach(entry -> list2.add(entry.geometry()));
        System.out.println("结果数量"+list2.size());
        System.out.println("rtree 时间消耗： "+(System.currentTimeMillis() - s));
    }

}
