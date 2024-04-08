package com.edu.szu;

import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.entity.GeoPointDouble;
import com.edu.szu.config.*;

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
        KDVConfig.class,
        BSTDConfig.class,
        PAConfig.class,

        // endpoint
        DCPGSEndpoint.class,
        KSTCEndpoint.class,
        KDVEndpoint.class,
        BSTDEndPoint.class,
        topkMain.class
})
@SpringBootApplication
@Log4j2
public class DBSystemApp {

    public static void main(String[] args) {
        SpringApplication.run(DBSystemApp.class, args);
    }
}
