package com.edu.szu.config;

import cn.edu.szu.cs.*;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import com.edu.szu.service.KstcService;
import com.edu.szu.service.impl.KstcServiceImpl;
import com.github.davidmoten.rtree.InternalStructure;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Serializers;
import com.github.davidmoten.rtree.geometry.Geometry;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class KSTCConfig {

    /**
     * relatedObjectService
     * @return
     */
    @Bean
    public IRelatedObjectService relatedObjectService(){
        return new DefaultRelatedObjectServiceImpl();
    }

    /**
     * rTree
     * @return
     */
    @SneakyThrows
    @Bean
    public RTree<String, Geometry> rTree(){
        InputStream inputStream = new ClassPathResource("rtree.txt").getStream();
        int available = inputStream.available();
        return Serializers.flatBuffers().utf8().read(inputStream, available, InternalStructure.DEFAULT);
    }

    /**
     *
     * @param rTree
     * @param relatedObjectService
     * @return
     */
    @Bean
    public IRTree<RelatedObject> irTree(RTree<String,Geometry> rTree,IRelatedObjectService relatedObjectService){
        return new SimpleIRTree(rTree,relatedObjectService);
    }

    /**
     *
     * @param relatedObjectService
     * @return
     */
    @Bean
    public InvertedIndex<RelatedObject> invertedIndex(IRelatedObjectService relatedObjectService){
        return new DefaultInvertedIndex(relatedObjectService);
    }

    /**
     * kstc alg
     * @return
     */
    @Bean
    public KSTC<RelatedObject> kstc(IRTree<RelatedObject> irTree,InvertedIndex<RelatedObject> invertedIndex){
        return new SimpleKSTC<>(irTree,invertedIndex);
    }


    @Bean
    public KstcService kstcService(KSTC<RelatedObject> kstc){
        return new KstcServiceImpl(kstc);
    }

}
