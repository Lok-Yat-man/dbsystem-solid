package com.edu.szu.config;

import cn.hutool.core.io.resource.ClassPathResource;
import com.edu.szu.service.STDService;
import com.edu.szu.service.impl.STDServiceImpl;
import com.github.davidmoten.rtree.InternalStructure;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Geometry;
import entity.RelevantObject;
import irtree.IRTree;
import ivtidx.DefaultLeafInvertedIndex;
import ivtidx.InvertedIndex;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import service.DefaultRelevantObjectServiceImpl;
import service.IRelevantObjectService;
import std.ASTD;
import std.BSTD;

import java.io.InputStream;

@Configuration
public class STDConfig {

    /**
     * relevantObjectService
     * @return get point object service
     */
    @Bean
    public IRelevantObjectService relevantObjectService() {
        return new DefaultRelevantObjectServiceImpl();
    }

    /**
     * @return R-tree
     */
    @SneakyThrows
    @Bean
    public RTree<String, Geometry> rTreeSTD() {
        InputStream inputStream = new ClassPathResource("rtreeSkyline.txt").getStream();
        int available = inputStream.available();
        return Serializers.flatBuffers().utf8().read(inputStream, available, InternalStructure.DEFAULT);
    }

    /**
     * @return IR-tree
     */
    @Bean
    public IRTree irTreeSTD(RTree<String, Geometry> rTree, IRelevantObjectService relevantObjectService) {
        return new IRTree(rTree, relevantObjectService);
    }

    /**
     * InvertedIndex
     * @return Inverted file index
     */
    @Bean
    public InvertedIndex<RelevantObject> invertedIndexSTD(IRelevantObjectService relevantObjectService) {
        return new DefaultLeafInvertedIndex(relevantObjectService);
    }

    /**
     * Basic algorithm for std
     * @return bstd
     */
    @Bean("bstd")
    public BSTD bstd(IRTree irTree, IRelevantObjectService relevantObjectService, InvertedIndex<RelevantObject> invertedIndexSTD) {
        return new BSTD(irTree, relevantObjectService, invertedIndexSTD);
    }

    @Bean("astd")
    public ASTD astd(IRTree irTree, IRelevantObjectService relevantObjectService, InvertedIndex<RelevantObject> invertedIndexSTD) {
        return new ASTD(irTree, relevantObjectService, invertedIndexSTD);
    }

    @Bean
    public STDService stdService(BSTD bstd, ASTD astd) {
        return new STDServiceImpl(bstd, astd);
    }
}
