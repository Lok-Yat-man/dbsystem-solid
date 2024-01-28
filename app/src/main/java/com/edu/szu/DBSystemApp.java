package com.edu.szu;

import cn.edu.szu.cs.entity.Coordinate;
import cn.edu.szu.cs.entity.GeoPointDouble;
import com.edu.szu.config.DCPGSConfig;

import com.edu.szu.config.KDVConfig;
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
        KDVConfig.class,

        // endpoint
        DCPGSEndpoint.class,
        KSTCEndpoint.class,
        KDVEndpoint.class,
})
@SpringBootApplication
@Log4j2
public class DBSystemApp {

    public static void main(String[] args) {
        SpringApplication.run(DBSystemApp.class, args);
    }
}
