package com.edu.szu.config;

import cn.edu.szu.cs.service.IRelatedObjectService;
import cn.hutool.core.io.resource.ClassPathResource;
import com.edu.szu.service.BstdService;
import com.edu.szu.service.impl.BstdServiceImpl;
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
import std.BSTD;

import java.io.InputStream;

@Configuration
public class BSTDConfig {

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
    public RTree<String, Geometry> rTreeBSTD() {
        InputStream inputStream = new ClassPathResource("rtree.txt").getStream();
        int available = inputStream.available();
        return Serializers.flatBuffers().utf8().read(inputStream, available, InternalStructure.DEFAULT);
    }

    /**
     * @param relatedObjectService
     * @return IR-tree
     */
    @Bean
    public IRTree irTreeBSTD(IRelatedObjectService relatedObjectService) {
        return new IRTree(relevantObjectService());
    }

    /**
     * InvertedIndex
     * @param relevantObjectService
     * @return Inverted file index
     */
    @Bean
    public InvertedIndex<RelevantObject> invertedIndexBSTD(IRelevantObjectService relevantObjectService) {
        return new DefaultLeafInvertedIndex(relevantObjectService);
    }

    /**
     * Basic algorithm for std
     * @return bstd
     */
    @Bean("bstd")
    public BSTD bstd(IRTree irTree, IRelevantObjectService relevantObjectService, InvertedIndex<RelevantObject> invertedIndexBSTD) {
        return new BSTD(irTree, relevantObjectService, invertedIndexBSTD);
    }

    @Bean
    public BstdService bstdService(BSTD bstd) {
        return new BstdServiceImpl(bstd);
    }
}
